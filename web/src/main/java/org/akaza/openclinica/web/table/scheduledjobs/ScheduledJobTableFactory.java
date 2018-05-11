package org.akaza.openclinica.web.table.scheduledjobs;

import org.akaza.openclinica.control.AbstractTableFactory;
import org.akaza.openclinica.control.DefaultActionsEditor;
import org.akaza.openclinica.dao.ScheduledJobSort;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.web.table.sdv.SDVUtil;
import org.jmesa.facade.TableFacade;
import org.jmesa.limit.Limit;
import org.jmesa.limit.Sort;
import org.jmesa.limit.SortSet;
import org.jmesa.view.component.Row;
import org.jmesa.view.html.AbstractHtmlView;
import org.jmesa.view.html.HtmlBuilder;
import org.jmesa.view.html.HtmlSnippets;
import org.jmesa.view.html.component.HtmlTable;
import org.jmesa.web.WebContext;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 * View builder for the list of scheduled jobs with an ability to cancel the job
 * @author jnyayapathi
 *
 */
public class ScheduledJobTableFactory extends AbstractTableFactory {
    private ResourceBundle resword;
    private int studyId;
    private String contextPath;

    @Override
    protected String getTableName() {
        return "scheduledJobs";
    }
    @Override
    public void configureTableFacadeCustomView(TableFacade tableFacade) {
        tableFacade.setView(new ScheduledJobView(getLocale()));
    }
    @Override
    protected void configureColumns(TableFacade tableFacade, Locale locale) {
        tableFacade.setColumnProperties( "datasetId", "fireTime", "exportFileName","jobStatus", "action");
        // TODO the following is throwing null pointer, check later
         resword = ResourceBundleProvider.getWordsBundle(locale);
        Row row = tableFacade.getTable().getRow();

        String[] allTitles = new String[] {  "DataSet Name", "Fire Time", "Export File","Job Status", "Actions" };
        SDVUtil sdvUtil = new SDVUtil();// TODO check if this is viable
        sdvUtil.setTitles(allTitles, (HtmlTable) tableFacade.getTable());

//       HtmlColumn sdvStatus = ((HtmlRow) row).getColumn("checkbox");
//       sdvStatus.getFilterRenderer().setFilterEditor(new SdvStatusFilter());
        sdvUtil.setHtmlCellEditors(tableFacade, new String[] { "action" }, false);

        configureColumn(row.getColumn("action"), "Actions", sdvUtil.getCellEditorNoEscapes(), new DefaultActionsEditor(locale), true, false);

    }
/**
 * Creating table
 */
    @Override
    public TableFacade createTable(HttpServletRequest request, HttpServletResponse response) {
        locale = LocaleResolver.getLocale(request);
        TableFacade tableFacade = getTableFacadeImpl(request, response);
        tableFacade.setStateAttr("restore");
        int maxJobs = (Integer) request.getAttribute("totalJobs");
        tableFacade.setTotalRows(maxJobs);
        Limit limit = tableFacade.getLimit();
        List<ScheduledJobs> jobs = (List<ScheduledJobs>) request.getAttribute("jobs");

        int rowStart = limit.getRowSelect().getRowStart();
        int rowEnd = limit.getRowSelect().getRowEnd();
        // Collection<SubjectAggregateContainer> items =
        // getFilteredItems(studySubjectSDVFilter, studySubjectSDVSort,
        // rowStart, rowEnd);

      //  setDataAndLimitVariables(tableFacade, jobs);
        tableFacade.setItems(jobs);
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

    public void setDataAndLimitVariables(TableFacade tableFacade, List<ScheduledJobs> jobs) {
        Limit limit = tableFacade.getLimit();

        WebContext context = tableFacade.getWebContext();
        if (context != null) {
            studyId = Integer.parseInt(context.getParameter("studyId"));
            contextPath = context.getContextPath();
        }

        ScheduledJobSort scheduledJobSort = getScheduledJobSort(limit);

        int rowStart = limit.getRowSelect().getRowStart();
        int rowEnd = limit.getRowSelect().getRowEnd();

        Collection<ScheduledJobs> items = getFilteredItems(jobs, scheduledJobSort, rowStart, rowEnd);

    }
/**
 * No Filtering
 * @param jobs
 * @param scheduledJobSort
 * @param rowStart
 * @param rowEnd
 * @return
 */
    private Collection<ScheduledJobs> getFilteredItems(List<ScheduledJobs> jobs, ScheduledJobSort scheduledJobSort, int rowStart, int rowEnd) {
        return null;
    }

    protected ScheduledJobSort getScheduledJobSort(Limit limit) {
        ScheduledJobSort scheduledJobSort = new ScheduledJobSort();
        SortSet sortSet = limit.getSortSet();
        Collection<Sort> sorts = sortSet.getSorts();
        for (Sort sort : sorts) {
            String property = sort.getProperty();
            String order = sort.getOrder().toParam();
            scheduledJobSort.addSort(property, order);
        }

        return scheduledJobSort;
    }

    class ScheduledJobView extends AbstractHtmlView {

        private final ResourceBundle resword;

        public ScheduledJobView(Locale locale) {
            resword = ResourceBundleProvider.getWordsBundle(locale);
        }

        public Object render() {
            HtmlSnippets snippets = getHtmlSnippets();
            HtmlBuilder html = new HtmlBuilder();
            html.append(snippets.themeStart());
            html.append(snippets.tableStart());
            html.append(snippets.theadStart());
            html.append(snippets.toolbar());
         //   html.append(selectAll()); Not required, not selecting all the jobs
            html.append(snippets.header());
            html.append(snippets.filter());
            html.append(snippets.theadEnd());
            html.append(snippets.tbodyStart());
            html.append(snippets.body());
            html.append(snippets.tbodyEnd());
            html.append(snippets.footer());
            html.append(snippets.statusBar());
            html.append(snippets.tableEnd());
            html.append(snippets.themeEnd());
            html.append(snippets.initJavascriptLimit());
            return html.toString();
        }

        String selectAll() {
            HtmlBuilder html = new HtmlBuilder();
            html.tr(1).styleClass("logic").close().td(1).colspan("100%").style("font-size: 12px;").close();
            html.append("<b>" + resword.getString("table_sdv_select") + "</b>&#160;&#160;");
            html.append("<a name='checkSDVAll' href='javascript:selectAllChecks(document.scheduledJobsForm,true)'>" + resword.getString("table_sdv_all"));
            html.append(",</a>");
            html.append("&#160;&#160;&#160;");
            html.append("<a name='checkSDVAll' href='javascript:selectAllChecks(document.scheduledJobsForm,false)'>" + resword.getString("table_sdv_none"));
            html.append("</a>");
            html.tdEnd().trEnd(1);
            return html.toString();
        }
    }

    @Override
    public void setDataAndLimitVariables(TableFacade tableFacade) {
        // TODO Auto-generated method stub

    }

}
