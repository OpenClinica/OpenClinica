package org.akaza.openclinica.control;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.jmesa.facade.TableFacade;
import org.jmesa.facade.TableFacadeImpl;
import org.jmesa.limit.ExportType;
import org.jmesa.limit.Limit;
import org.jmesa.limit.LimitImpl;
import org.jmesa.limit.RowSelect;
import org.jmesa.limit.RowSelectImpl;
import org.jmesa.view.component.Column;
import org.jmesa.view.editor.CellEditor;
import org.jmesa.view.editor.FilterEditor;
import org.jmesa.view.html.component.HtmlColumn;
import org.jmesa.view.html.component.HtmlTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTableFactory {

    protected Locale locale;

    protected HttpSession session;

    protected Logger logger = LoggerFactory.getLogger(getClass().getName());

    protected abstract String getTableName();

    protected String getCaptionName() {
        return "";
    }

    protected abstract void configureColumns(TableFacade tableFacade, Locale locale);

    protected void configureExportColumns(TableFacade tableFacade, Locale locale) {
        configureColumns(tableFacade, locale);
    }

    public TableFacade getTableFacadeImpl(HttpServletRequest request, HttpServletResponse response) {
        return new TableFacadeImpl(getTableName(), request);
    }

    public abstract void setDataAndLimitVariables(TableFacade tableFacade);

    public TableFacade createTable(HttpServletRequest request, HttpServletResponse response) {
        locale = LocaleResolver.getLocale(request);
        session = request.getSession();
        TableFacade tableFacade = getTableFacadeImpl(request, response);
        setStateAttr(tableFacade);
        setDataAndLimitVariables(tableFacade);
        configureTableFacade(response, tableFacade);
        if (!tableFacade.getLimit().isExported()) {
            configureColumns(tableFacade, locale);
            tableFacade.setMaxRowsIncrements(getMaxRowIncrements());
            configureTableFacadePostColumnConfiguration(tableFacade);
            configureTableFacadeCustomView(tableFacade);
            configureUnexportedTable(tableFacade, locale);
        } else {
            configureExportColumns(tableFacade, locale);
        }
        return tableFacade;
    }

    /**
     * Use this method to export all data from table. 1. filters/sorts will be ignored 2. Whole table will be exported
     * page by page 3. Configure getSize(Limit limit)
     * 
     * @param request
     * @param response
     * @see getSize(Limit limit), createLimits()
     * @see filter & sort methods in implementations
     */
    public void exportCSVTable(HttpServletRequest request, HttpServletResponse response, String path) {
        locale = LocaleResolver.getLocale(request);
        String DATE_FORMAT = "yyyyMMddHHmmss";
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        String fileName = getTableName() + "_" + sdf.format(new Date());

        for (Limit limit : createLimits()) {
            TableFacade tableFacade = new OCTableFacadeImpl(getTableName(), request, response, path + File.separator + fileName);
            tableFacade.setStateAttr("restore");
            tableFacade.setLimit(limit);
            tableFacade.autoFilterAndSort(false);
            setDataAndLimitVariables(tableFacade);
            configureTableFacade(response, tableFacade);
            configureExportColumns(tableFacade, locale);
            tableFacade.render();
        }
    }

    private ArrayList<Limit> createLimits() {
        Limit limit = new LimitImpl(getTableName());
        ArrayList<Limit> limits = new ArrayList<Limit>();
        int size = getSize(limit);
        for (RowSelect rowSelect : getRowSelects(size)) {
            Limit theLimit = new LimitImpl(getTableName());
            theLimit.setRowSelect(rowSelect);
            theLimit.setExportType(ExportType.CSV);
            limits.add(theLimit);
        }
        return limits;
    }

    private ArrayList<RowSelect> getRowSelects(int size) {
        ArrayList<RowSelect> rowSelects = new ArrayList<RowSelect>();
        int i = 0;
        for (i = 0; i < size / 50; i++) {
            RowSelect rowSelect = new RowSelectImpl(i + 1, 50, size);
            rowSelects.add(rowSelect);
        }
        if (size % 50 > 0) {
            RowSelect rowSelect = new RowSelectImpl(i + 1, size % 50, size);
            rowSelects.add(rowSelect);
        }
        return rowSelects;
    }

    public int getSize(Limit limit) {
        return 0;
    }

    public void configureTableFacade(HttpServletResponse response, TableFacade tableFacade) {
        tableFacade.setExportTypes(response, getExportTypes());
    }

    public int[] getMaxRowIncrements() {
        return new int[] { 15, 25, 50 };
    }

    /**
     * By Default we configure a default toolbar. Overwrite this method if you need to provide a custom toolbar and
     * configure other options.
     * 
     * @param tableFacade
     */
    public void configureTableFacadePostColumnConfiguration(TableFacade tableFacade) {
        tableFacade.setToolbar(new DefaultToolbar());
    }

    /**
     * By Default we configure a default view. Overwrite this method if you need to provide a custom view.
     * 
     * @param tableFacade
     * @see http://code.google.com/p/jmesa/wiki/CustomViewTotalsTutorial
     */
    public void configureTableFacadeCustomView(TableFacade tableFacade) {
        tableFacade.setView(new DefaultView(locale));
    }

    protected void configureUnexportedTable(TableFacade tableFacade, Locale locale) {
        HtmlTable table = (HtmlTable) tableFacade.getTable();
        table.setCaption(getCaptionName());
    }

    protected ExportType[] getExportTypes() {
        return null;
    }

    public Locale getLocale() {
        return locale;
    }

    protected void configureColumn(Column column, String title, CellEditor editor, FilterEditor filterEditor) {
        configureColumn(column, title, editor, filterEditor, true, true);
    }

    protected void configureColumn(Column column, String title, CellEditor editor, FilterEditor filterEditor, boolean filterable, boolean sortable) {
        column.setTitle(title);
        if (editor != null) {
            column.getCellRenderer().setCellEditor(editor);
        }

        if (column instanceof HtmlColumn) {
            HtmlColumn htmlColumn = (HtmlColumn) column;
            htmlColumn.setFilterable(filterable);
            htmlColumn.setSortable(sortable);
            if (filterEditor != null) {
                htmlColumn.getFilterRenderer().setFilterEditor(filterEditor);
            }
        }
    }

    public static String getDNFlagIconName(int dnResolutionStatusId) {
        String name = "";
        switch (dnResolutionStatusId) {
        case 0:
            name = "icon_noNote";
            break;
        case 1:
            name = "icon_Note";
            break;
        case 2:
            name = "icon_flagYellow";
            break;
        case 3:
            name = "icon_flagGreen";
            break;
        case 4:
            name = "icon_flagBlack";
            break;
        case 5:
            name = "icon_flagWhite";
            break;
        default:
            name = "icon_noNote";
            break;
        }

        return name;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List paginateData(List list, int rowStart, int rowEnd) {
        ArrayList mainList = new ArrayList();
        if (rowStart > 0) {
            rowStart = rowStart + 1;
        }
        for (int i = rowStart; i <= rowEnd; i++) {
            if (i < list.size()) {
                mainList.add(list.get(i));
            } else {
                break;
            }

        }
        return mainList;
    }

    public void setStateAttr(TableFacade tableFacade) {
        if (getTableName() != null) {
            tableFacade.setStateAttr(getTableName() + "_restore");
        } else {
            tableFacade.setStateAttr("restore");
            logger.debug("getTableName() returned null, so tableFacade.setStateAttr = restore");
        }
    }
}