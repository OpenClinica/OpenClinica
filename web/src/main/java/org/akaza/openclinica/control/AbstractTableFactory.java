package org.akaza.openclinica.control;

import org.jmesa.facade.TableFacade;
import org.jmesa.facade.TableFacadeImpl;
import org.jmesa.limit.ExportType;
import org.jmesa.view.component.Column;
import org.jmesa.view.editor.CellEditor;
import org.jmesa.view.editor.FilterEditor;
import org.jmesa.view.html.component.HtmlColumn;
import org.jmesa.view.html.component.HtmlTable;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class AbstractTableFactory {

    private Locale locale;

    protected abstract String getTableName();

    protected String getCaptionName() {
        return "";
    }

    protected abstract void configureColumns(TableFacade tableFacade, Locale locale);

    protected void configureExportColumns(TableFacade tableFacade, Locale locale) {
        configureColumns(tableFacade, locale);
    }

    public abstract void setDataAndLimitVariables(TableFacade tableFacade);

    public TableFacade createTable(HttpServletRequest request, HttpServletResponse response) {
        locale = request.getLocale();
        TableFacade tableFacade = new TableFacadeImpl(getTableName(), request);
        tableFacade.setStateAttr("restore");
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

    public void configureTableFacade(HttpServletResponse response, TableFacade tableFacade) {
        tableFacade.setExportTypes(response, getExportTypes());
    }

    public int[] getMaxRowIncrements() {
        return new int[] { 15, 25, 50 };
    }

    /**
     * By Default we configure a default toolbar. Overwrite this method if you
     * need to provide a custom toolbar and configure other options.
     * 
     * @param tableFacade
     */
    public void configureTableFacadePostColumnConfiguration(TableFacade tableFacade) {
        tableFacade.setToolbar(new DefaultToolbar());
    }

    /**
     * By Default we configure a default view. Overwrite this method if you need
     * to provide a custom view.
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
}