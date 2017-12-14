package org.akaza.openclinica.web.table.sdv;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyGroupBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.control.AbstractTableFactory;
import org.akaza.openclinica.control.DefaultActionsEditor;
import org.akaza.openclinica.controller.helper.table.SDVToolbarSubject;
import org.akaza.openclinica.controller.helper.table.SubjectAggregateContainer;
import org.akaza.openclinica.dao.StudySubjectSDVFilter;
import org.akaza.openclinica.dao.StudySubjectSDVSort;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.domain.SourceDataVerification;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.jmesa.core.filter.MatcherKey;
import org.jmesa.facade.TableFacade;
import org.jmesa.limit.Filter;
import org.jmesa.limit.FilterSet;
import org.jmesa.limit.Limit;
import org.jmesa.limit.Sort;
import org.jmesa.limit.SortSet;
import org.jmesa.view.component.Row;
import org.jmesa.view.html.AbstractHtmlView;
import org.jmesa.view.html.HtmlBuilder;
import org.jmesa.view.html.HtmlSnippets;
import org.jmesa.view.html.component.HtmlColumn;
import org.jmesa.view.html.component.HtmlRow;
import org.jmesa.view.html.component.HtmlTable;
import org.jmesa.web.WebContext;

/**
 * A Jmesa table that represents study subjects in each row.
 */
public class SubjectIdSDVFactory extends AbstractTableFactory {

    private DataSource dataSource;

    private int studyId;
    private String contextPath;
    private ResourceBundle resword;
    private final static String ICON_FORCRFSTATUS_SUFFIX = ".gif'/>";
    public boolean showMoreLink;

    public SubjectIdSDVFactory() {
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    protected String getTableName() {
        // This name creates the underlying id of the HTML table
        return "s_sdv";
    }

    @Override
    public void configureTableFacadeCustomView(TableFacade tableFacade) {
        tableFacade.setView(new SubjectSDVView(getLocale()));
    }

    @Override
    protected void configureColumns(TableFacade tableFacade, Locale locale) {

        tableFacade.setColumnProperties("sdvStatus", "studySubjectId", "siteId", "personId", "studySubjectStatus", "group", "numberCRFComplete",
                "numberOfCRFsSDV", "totalEventCRF", "actions");

        resword = ResourceBundleProvider.getWordsBundle(locale);
        Row row = tableFacade.getTable().getRow();

        SDVUtil sdvUtil = new SDVUtil();
        String[] allTitles = new String[] { resword.getString("SDV_status"), resword.getString("study_subject_ID"), resword.getString("site_id"),
                resword.getString("person_ID"), resword.getString("study_subject_status"), resword.getString("group"), resword.getString("num_CRFs_completed"),
                resword.getString("num_CRFs_SDV"), resword.getString("total_events_CRF"), resword.getString("actions") };

        sdvUtil.setTitles(allTitles, (HtmlTable) tableFacade.getTable());
        sdvUtil.turnOffFilters(tableFacade,
                new String[] { "personId", "studySubjectStatus", "group", "numberCRFComplete", "numberOfCRFsSDV", "totalEventCRF", "actions" });
        sdvUtil.turnOffSorts(tableFacade, new String[] { "sdvStatus", "studySubjectId", "siteId", "personId", "studySubjectStatus", "group",
                "numberCRFComplete", "numberOfCRFsSDV", "totalEventCRF" });

        sdvUtil.setHtmlCellEditors(tableFacade, new String[] { "sdvStatus", "actions" }, false);

        HtmlColumn sdvStatus = ((HtmlRow) row).getColumn("sdvStatus");
        sdvStatus.getFilterRenderer().setFilterEditor(new SdvStatusFilter());

        String actionsHeader = resword.getString("rule_actions")
                + "&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;";
        configureColumn(row.getColumn("actions"), actionsHeader, sdvUtil.getCellEditorNoEscapes(), new DefaultActionsEditor(locale), true, false);

    }

    @Override
    public void configureTableFacade(HttpServletResponse response, TableFacade tableFacade) {
        super.configureTableFacade(response, tableFacade);

        tableFacade.addFilterMatcher(new MatcherKey(String.class, "sdvStatus"), new SdvStatusMatcher());

    }

    @Override
    public void setDataAndLimitVariables(TableFacade tableFacade) {

        Limit limit = tableFacade.getLimit();

        StudySubjectSDVFilter studySubjectSDVFilter = getStudySubjectSDVFilter(limit);
        WebContext context = tableFacade.getWebContext();
        if (context != null) {
            studyId = Integer.parseInt(context.getParameter("studyId"));
            contextPath = context.getContextPath();
        }

        String restore = context.getRequestAttribute(limit.getId() + "_restore") + "";
        if (!limit.isComplete()) {
            int totalRows = getTotalRowCount(studySubjectSDVFilter);
            tableFacade.setTotalRows(totalRows);
        } else if (restore != null && "true".equalsIgnoreCase(restore)) {
            int totalRows = getTotalRowCount(studySubjectSDVFilter);
            int pageNum = limit.getRowSelect().getPage();
            int maxRows = limit.getRowSelect().getMaxRows();
            tableFacade.setMaxRows(maxRows);
            tableFacade.setTotalRows(totalRows);
            limit.getRowSelect().setPage(pageNum);
        }

        StudySubjectSDVSort studySubjectSDVSort = getStudySubjectSDVSort(limit);

        int rowStart = limit.getRowSelect().getRowStart();
        int rowEnd = limit.getRowSelect().getRowEnd();
        // Collection<StudySubjectBean> items = getStudySubjectDAO().getWithFilterAndSort(getStudyBean(),
        // studySubjectSDVFilter, subjectSort, rowStart, rowEnd);
        Collection<SubjectAggregateContainer> items = getFilteredItems(studySubjectSDVFilter, studySubjectSDVSort, rowStart, rowEnd);
        tableFacade.setItems(items);

        /*
         * 
         * 
         * Limit limit = tableFacade.getLimit();
         * FilterSet filterSet = limit.getFilterSet();
         * WebContext context = tableFacade.getWebContext();
         * if (context != null) {
         * studyId = Integer.parseInt(context.getParameter("studyId"));
         * contextPath = context.getContextPath();
         * }
         * int totalRows = getTotalRowCount();
         * 
         * tableFacade.setTotalRows(totalRows);
         * SortSet sortSet = limit.getSortSet();
         * int rowStart = limit.getRowSelect().getRowStart();
         * int rowEnd = limit.getRowSelect().getRowEnd();
         * Collection<SubjectAggregateContainer> items = getFilteredItems(filterSet, sortSet, rowStart, rowEnd);
         * ;
         * 
         * tableFacade.setItems(items);
         */

    }

    protected StudySubjectSDVFilter getStudySubjectSDVFilter(Limit limit) {
        StudySubjectSDVFilter studySubjectSDVFilter = new StudySubjectSDVFilter();
        FilterSet filterSet = limit.getFilterSet();
        Collection<Filter> filters = filterSet.getFilters();
        for (Filter filter : filters) {
            String property = filter.getProperty();
            String value = filter.getValue();
            studySubjectSDVFilter.addFilter(property, value);
        }

        return studySubjectSDVFilter;
    }

    public StudySubjectSDVFilter createStudySubjectSDVFilter(Limit limit) {
        return getStudySubjectSDVFilter(limit);
    }

    protected StudySubjectSDVSort getStudySubjectSDVSort(Limit limit) {
        StudySubjectSDVSort studySubjectSDVSort = new StudySubjectSDVSort();
        SortSet sortSet = limit.getSortSet();
        Collection<Sort> sorts = sortSet.getSorts();
        for (Sort sort : sorts) {
            String property = sort.getProperty();
            String order = sort.getOrder().toParam();
            studySubjectSDVSort.addSort(property, order);
        }

        return studySubjectSDVSort;
    }

    /*
     * Returns how many subjects exist in the study.
     */
    public int getTotalRowCount(StudySubjectSDVFilter studySubjectSDVFilter) {

        StudySubjectDAO studySubDAO = new StudySubjectDAO(dataSource);
        return studySubDAO.countAllByStudySDV(studyId, studyId, studySubjectSDVFilter);

    }

    @Override
    public void configureTableFacadePostColumnConfiguration(TableFacade tableFacade) {
        tableFacade.setToolbar(new SDVToolbarSubject(showMoreLink));
    }

    @SuppressWarnings("unchecked")
    private Collection<SubjectAggregateContainer> getFilteredItems(StudySubjectSDVFilter filterSet, StudySubjectSDVSort sortSet, int rowStart, int rowEnd) {

        List<SubjectAggregateContainer> rows = new ArrayList<SubjectAggregateContainer>();
        StudySubjectDAO studySubjectDAO = new StudySubjectDAO(dataSource);
        List<StudySubjectBean> studySubjectBeans = studySubjectDAO.findAllByStudySDV(studyId, studyId, filterSet, sortSet, rowStart, rowEnd);
        SubjectAggregateContainer containerTmp = null;

        for (StudySubjectBean studSubjBean : studySubjectBeans) {
            containerTmp = getRow(studSubjBean);
            rows.add(containerTmp);
        }

        return rows;
    }

    String getIconForCrfStatusPrefix() {
        String prefix = "../";
        return "<img hspace='2' border='0'  title='SDV Complete' alt='SDV Status' src='" + prefix + "images/icon_";
    }

    private SubjectAggregateContainer getRow(StudySubjectBean studySubjectBean) {
        SubjectAggregateContainer row = new SubjectAggregateContainer();
        EventCRFDAO eventCRFDAO = new EventCRFDAO(dataSource);
        StudyDAO studyDAO = new StudyDAO(dataSource);
        StudySubjectDAO studySubjectDAO = new StudySubjectDAO(dataSource);
        StudyGroupDAO studyGroupDAO = new StudyGroupDAO(dataSource);

        row.setStudySubjectId(studySubjectBean.getLabel());
        row.setPersonId(studySubjectBean.getUniqueIdentifier());
        row.setStudySubjectStatus(studySubjectBean.getStatus().getName());
        int numberEventCRFs = eventCRFDAO.countEventCRFsByStudySubject(studySubjectBean.getId(), studySubjectBean.getStudyId(), studySubjectBean.getStudyId());
        row.setTotalEventCRF(numberEventCRFs + "");

        StudyBean studyBean = (StudyBean) studyDAO.findByPK(studySubjectBean.getStudyId());
        row.setSiteId(studyBean.getIdentifier());

        List<EventCRFBean> eventCRFBeans = eventCRFDAO.getEventCRFsByStudySubject(studySubjectBean.getId(), studySubjectBean.getStudyId(),
                studySubjectBean.getStudyId());

        HashMap<String, Integer> stats = getEventCRFStats(eventCRFBeans, studySubjectBean);

        // int numberCRFComplete = getNumberCompletedEventCRFs(eventCRFBeans);
        // row.setNumberCRFComplete(numberCRFComplete + "");
        row.setNumberCRFComplete(stats.get("numberOfCompletedEventCRFs") + "");
        row.setNumberOfCRFsSDV(stats.get("numberOfSDVdEventCRFs") + "");

        // row.setNumberOfCRFsSDV(getNumberSDVdEventCRFs(eventCRFBeans) + "");
        // boolean studySubjectSDVd =
        // eventCRFDAO.countEventCRFsByByStudySubjectCompleteOrLockedAndNotSDVd(studySubjectBean.getId()) == 0 &&
        // numberCRFComplete > 0;

        boolean studySubjectSDVd = stats.get("areEventCRFsSDVd") == -1 || stats.get("areEventCRFsSDVd") == 1 ? false : true;

        StringBuilder sdvStatus = new StringBuilder("");
        if (stats.get("shouldDisplaySDVButton") == 0) {
            sdvStatus.append("");
        } else if (studySubjectSDVd) {
            sdvStatus.append("<center><a href='javascript:void(0)' onclick='prompt(document.sdvForm,");
            sdvStatus.append(studySubjectBean.getId());
            sdvStatus.append(")'>");
            sdvStatus.append(getIconForCrfStatusPrefix()).append("DoubleCheck").append(ICON_FORCRFSTATUS_SUFFIX).append("</a></center>");
        } else {
            sdvStatus.append("<center><input style='margin-right: 5px' type='checkbox' ").append("class='sdvCheck'").append(" name='").append("sdvCheck_")
                    .append(studySubjectBean.getId()).append("' /></center>");

        }
        row.setSdvStatus(sdvStatus.toString());

        List<StudyGroupBean> studyGroupBeans = studyGroupDAO.getGroupByStudySubject(studySubjectBean.getId(), studySubjectBean.getStudyId(),
                studySubjectBean.getStudyId());

        if (studyGroupBeans != null && !studyGroupBeans.isEmpty()) {
            row.setGroup(studyGroupBeans.get(0).getName());
        }
        StringBuilder actions = new StringBuilder("<table><tr><td>");
        StringBuilder urlPrefix = new StringBuilder("<a href=\"");
        StringBuilder path = new StringBuilder(contextPath).append("/pages/viewAllSubjectSDVtmp?studyId=").append(studyId).append("&sdv_f_studySubjectId=");
        path.append(studySubjectBean.getLabel());
        urlPrefix.append(path).append("\">");
        actions.append(urlPrefix).append(SDVUtil.VIEW_ICON_HTML).append("</a></td>");

        if (!studySubjectSDVd && stats.get("shouldDisplaySDVButton") == 1) {
            StringBuilder jsCodeString = new StringBuilder("this.form.method='GET'; this.form.action='").append(contextPath).append("/pages/sdvStudySubject")
                    .append("';").append("this.form.theStudySubjectId.value='").append(studySubjectBean.getId()).append("';").append("this.form.submit();");
            if (!studyBean.getStatus().isLocked()) {
                actions.append("<td><input type=\"submit\" class=\"button\" value=\"SDV\" name=\"sdvSubmit\" ").append("onclick=\"")
                        .append(jsCodeString.toString()).append("\" /></td>");
            }
        } else if (!studySubjectSDVd) {
            actions.append("<td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;SDV N/A</td>");
        }
        actions.append("</tr></table>");

        row.setActions(actions.toString());

        return row;

    }

    private int getNumberCompletedEventCRFs(List<EventCRFBean> eventCRFBeans) {

        StudyEventDAO studyEventDAO = new StudyEventDAO(dataSource);
        StudyEventBean studyEventBean = null;
        int counter = 0;
        int statusId = 0;

        for (EventCRFBean eventBean : eventCRFBeans) {

            studyEventBean = (StudyEventBean) studyEventDAO.findByPK(eventBean.getStudyEventId());
            statusId = studyEventBean.getSubjectEventStatus().getId();
            if (statusId == 4) {
                counter++;
            }

        }
        return counter;
    }

    private int getNumberSDVdEventCRFs(List<EventCRFBean> eventCRFBeans) {

        int counter = 0;

        for (EventCRFBean eventBean : eventCRFBeans) {

            if (eventBean.isSdvStatus()) {
                counter++;
            }

        }
        return counter;

    }

    private HashMap<String, Integer> getEventCRFStats(List<EventCRFBean> eventCRFBeans, StudySubjectBean studySubject) {

        StudyEventDAO studyEventDAO = new StudyEventDAO(dataSource);
        EventDefinitionCRFDAO eventDefinitionCrfDAO = new EventDefinitionCRFDAO(dataSource);
        CRFDAO crfDAO = new CRFDAO(dataSource);
        StudyEventBean studyEventBean = null;
        Integer numberOfCompletedEventCRFs = 0;
        Integer numberOfSDVdEventCRFs = 0;
        Integer areEventCRFsSDVd = eventCRFBeans.size() > 0 ? 0 : -1;
        Boolean partialOrHundred = false;
        Integer shouldDisplaySDVButton = 0;

        for (EventCRFBean eventBean : eventCRFBeans) {
            studyEventBean = (StudyEventBean) studyEventDAO.findByPK(eventBean.getStudyEventId());
            CRFBean crfBean = crfDAO.findByVersionId(eventBean.getCRFVersionId());
            // get number of completed event crfs
            if (eventBean.getStatus() == Status.UNAVAILABLE || eventBean.getStatus() == Status.LOCKED) {
                numberOfCompletedEventCRFs++;
            }
            /*
             * if (studyEventBean.getSubjectEventStatus() == SubjectEventStatus.COMPLETED) {
             * numberOfCompletedEventCRFs++;
             * }
             */
            // get number of completed event SDVd events
            if (eventBean.isSdvStatus()) {
                numberOfSDVdEventCRFs++;
            }
            // get number of all non SDVd events that are 100% or partial required
            EventDefinitionCRFBean eventDefinitionCrf = eventDefinitionCrfDAO
                    .findByStudyEventDefinitionIdAndCRFIdAndStudyId(studyEventBean.getStudyEventDefinitionId(), crfBean.getId(), studySubject.getStudyId());
            if (eventDefinitionCrf.getId() == 0) {
                eventDefinitionCrf = eventDefinitionCrfDAO.findForStudyByStudyEventDefinitionIdAndCRFId(studyEventBean.getStudyEventDefinitionId(),
                        crfBean.getId());
            }
            if ((eventDefinitionCrf.getSourceDataVerification() == SourceDataVerification.AllREQUIRED
                    || eventDefinitionCrf.getSourceDataVerification() == SourceDataVerification.PARTIALREQUIRED)
                    && (eventBean.getStatus() == Status.UNAVAILABLE || eventBean.getStatus() == Status.LOCKED)) {
                partialOrHundred = true;
            }
            if ((eventDefinitionCrf.getSourceDataVerification() == SourceDataVerification.AllREQUIRED
                    || eventDefinitionCrf.getSourceDataVerification() == SourceDataVerification.PARTIALREQUIRED) && eventBean.isSdvStatus() == false
                    && (eventBean.getStatus() == Status.UNAVAILABLE || eventBean.getStatus() == Status.LOCKED)) {
                areEventCRFsSDVd = 1;
            }

        }

        HashMap<String, Integer> stats = new HashMap<String, Integer>();
        stats.put("numberOfCompletedEventCRFs", numberOfCompletedEventCRFs);
        stats.put("numberOfSDVdEventCRFs", numberOfSDVdEventCRFs);
        stats.put("areEventCRFsSDVd", partialOrHundred == false ? 1 : areEventCRFsSDVd);
        stats.put("shouldDisplaySDVButton", numberOfCompletedEventCRFs > 0 && partialOrHundred == true ? 1 : 0);
        return stats;
    }

    class SubjectSDVView extends AbstractHtmlView {

        private final ResourceBundle resword;

        public SubjectSDVView(Locale locale) {
            resword = ResourceBundleProvider.getWordsBundle(locale);
        }

        public Object render() {
            HtmlSnippets snippets = getHtmlSnippets();
            HtmlBuilder html = new HtmlBuilder();
            html.append(snippets.themeStart());
            html.append(snippets.tableStart());
            html.append(snippets.theadStart());
            html.append(snippets.toolbar());
            html.append(selectAll());
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
            html.append("<a name='checkSDVAll' href='javascript:selectAllChecks(document.sdvForm,true)'>" + resword.getString("table_sdv_all"));
            html.append(",</a>");
            html.append("&#160;&#160;&#160;");
            html.append("<a name='checkSDVAll' href='javascript:selectAllChecks(document.sdvForm,false)'>" + resword.getString("table_sdv_none"));
            html.append("</a>");
            html.tdEnd().trEnd(1);
            return html.toString();
        }
    }
}
