package org.akaza.openclinica.web.table.sdv;

import static org.jmesa.facade.TableFacadeFactory.createTableFacade;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.control.DefaultActionsEditor;
import org.akaza.openclinica.controller.helper.SdvFilterDataBean;
import org.akaza.openclinica.controller.helper.table.SDVToolbar;
import org.akaza.openclinica.controller.helper.table.SubjectSDVContainer;
import org.akaza.openclinica.dao.EventCRFSDVFilter;
import org.akaza.openclinica.dao.EventCRFSDVSort;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.domain.SourceDataVerification;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.i18n.util.I18nFormatUtil;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.jmesa.core.filter.MatcherKey;
import org.jmesa.facade.TableFacade;
import org.jmesa.limit.Filter;
import org.jmesa.limit.FilterSet;
import org.jmesa.limit.Limit;
import org.jmesa.limit.Sort;
import org.jmesa.limit.SortSet;
import org.jmesa.util.ItemUtils;
import org.jmesa.view.editor.DateCellEditor;
import org.jmesa.view.html.AbstractHtmlView;
import org.jmesa.view.html.HtmlBuilder;
import org.jmesa.view.html.HtmlSnippets;
import org.jmesa.view.html.component.HtmlColumn;
import org.jmesa.view.html.component.HtmlRow;
import org.jmesa.view.html.component.HtmlTable;
import org.jmesa.view.html.editor.HtmlCellEditor;
import org.jmesa.web.WebContext;
import org.springframework.validation.BindingResult;

/**
 * A utility class that implements the details of the Source Data Verification (SDV) Jmesa tables.
 */
public class SDVUtil {

    private final static String VIEW_ICON_FORSUBJECT_PREFIX = "<a onmouseup=\"javascript:setImage('bt_View1','images/bt_View.gif');\" onmousedown=\"javascript:setImage('bt_View1','images/bt_View_d.gif');\" href=\"ViewStudySubject?id=";
    private final static String VIEW_ICON_FORSUBJECT_SUFFIX = "\"><img hspace=\"6\" border=\"0\" align=\"left\" title=\"View\" alt=\"View\" src=\"../images/bt_View.gif\" name=\"bt_View1\"/></a>";
    private final static String ICON_FORCRFSTATUS_PREFIX = "<img hspace='2' border='0'  title='Event CRF Status' alt='Event CRF Status' src='../images/icon_";

    private final static String ICON_FORCRFSTATUS_SUFFIX = ".gif'/>";
    public final static String CHECKBOX_NAME = "sdvCheck_";
    public final static String VIEW_ICON_HTML = "<img src=\"../images/bt_View.gif\" border=\"0\" />";
    private ResourceBundle resformat;
    private String pathPrefix;

    String getIconForSdvStatusPrefix() {
        String prefix = pathPrefix == null ? "../" : pathPrefix;
        return "<img hspace='2' border='0'  title='SDV Complete' alt='SDV Complete' src='" + prefix + "images/icon_";
    }

    String getIconForCrfStatusPrefix() {
        String prefix = pathPrefix == null ? "../" : pathPrefix;
        return "<img hspace='2' border='0'  title='Event CRF Status' alt='Event CRF Status' src='" + prefix + "images/icon_";
    }

    String getIconForSubjectSufix() {
        String prefix = pathPrefix == null ? "../" : pathPrefix;
        return "\"><img hspace=\"6\" border=\"0\" align=\"left\" title=\"View\" alt=\"View\" src=\"" + prefix + "images/bt_View.gif\" name=\"bt_View1\"/></a>";
    }

    String getIconForViewHtml() {
        String prefix = pathPrefix == null ? "../" : pathPrefix;
        return "<img src=\"" + prefix + "images/bt_View.gif\" border=\"0\" />";
    }

    public final static Map<Integer, String> SUBJECT_EVENT_STATUS_ICONS = new HashMap<Integer, String>();
    public final static Map<Integer, String> CRF_STATUS_ICONS = new HashMap<Integer, String>();
    static {
        SUBJECT_EVENT_STATUS_ICONS.put(0, "Invalid");
        SUBJECT_EVENT_STATUS_ICONS.put(1, "Scheduled");
        SUBJECT_EVENT_STATUS_ICONS.put(2, "NotStarted");
        SUBJECT_EVENT_STATUS_ICONS.put(3, "InitialDE");
        SUBJECT_EVENT_STATUS_ICONS.put(4, "DEcomplete");
        SUBJECT_EVENT_STATUS_ICONS.put(5, "Stopped");
        SUBJECT_EVENT_STATUS_ICONS.put(6, "Skipped");
        SUBJECT_EVENT_STATUS_ICONS.put(7, "Locked");
        SUBJECT_EVENT_STATUS_ICONS.put(8, "Signed");

        CRF_STATUS_ICONS.put(0, "Invalid");
        CRF_STATUS_ICONS.put(1, "NotStarted");
        CRF_STATUS_ICONS.put(2, "InitialDE");
        CRF_STATUS_ICONS.put(3, "InitialDEComplete");
        CRF_STATUS_ICONS.put(4, "DDE");
        CRF_STATUS_ICONS.put(5, "DEcomplete");
        CRF_STATUS_ICONS.put(6, "InitialDE");
        CRF_STATUS_ICONS.put(7, "Locked");
    }

    private DataSource dataSource;

    public NoEscapeHtmlCellEditor getCellEditorNoEscapes() {
        return new NoEscapeHtmlCellEditor();
    }

    private String getDateFormat() {
        return resformat.getString("date_format_string");
    }

    public int setDataAndLimitVariablesSubjects(TableFacade tableFacade, int studyId, int studySubjectId, HttpServletRequest request) {
        Limit limit = tableFacade.getLimit();
        FilterSet filterSet = limit.getFilterSet();
        int totalRows = getTotalRowCountSubjects(filterSet, studyId, studySubjectId);

        tableFacade.setTotalRows(totalRows);
        SortSet sortSet = limit.getSortSet();
        int rowStart = limit.getRowSelect().getRowStart();
        int rowEnd = limit.getRowSelect().getRowEnd();
        Collection<SubjectSDVContainer> items = getFilteredItemsSubject(filterSet, sortSet, rowStart, rowEnd, studyId, studySubjectId, request);

        tableFacade.setItems(items);

        return totalRows;

    }

    public int getTotalRowCountSubjects(FilterSet filterSet, int studyId, int studySubjectId) {

        EventCRFDAO eventCRFDAO = new EventCRFDAO(dataSource);

        if (filterSet.getFilters().size() == 0) {
            return eventCRFDAO.countEventCRFsByStudySubject(studySubjectId, studyId, studyId);
        }

        int count = 0;
        // Filter for study subject label
        StudySubjectDAO studySubjectDAO = new StudySubjectDAO(dataSource);
        StudySubjectBean studySubjectBean = new StudySubjectBean();
        studySubjectBean = (StudySubjectBean) studySubjectDAO.findByPK(studySubjectId);
        String label = studySubjectBean.getLabel();
        String eventNameValue = "";
        String eventDateValue = "";
        String crfStatus = "";
        String sdvStatus = "";

        for (Filter filter : filterSet.getFilters()) {

            if (filter.getProperty().equalsIgnoreCase("eventName")) {
                eventNameValue = filter.getValue();
                continue;
            }

            if (filter.getProperty().equalsIgnoreCase("eventDate")) {
                eventDateValue = filter.getValue();
                continue;
            }

            if (filter.getProperty().equalsIgnoreCase("crfStatus")) {
                crfStatus = filter.getValue();
                continue;
            }

            if (filter.getProperty().equalsIgnoreCase("sdvStatusActions")) {
                sdvStatus = filter.getValue();
            }
        }

        if (eventNameValue.length() > 0) {
            return eventCRFDAO.countEventCRFsByEventNameSubjectLabel(eventNameValue, label);
        }

        if (eventDateValue.length() > 0) {
            // return eventCRFDAO.countEventCRFsByEventDate(studyId,eventDateValue);
        }

        if (crfStatus.length() > 0) {
            // return eventCRFDAO.countEventCRFsByCRFStatus(studyId,
            // SubjectEventStatus.getSubjectEventStatusIdByName(crfStatus));
        }

        if (sdvStatus.length() > 0) {
            // return eventCRFDAO.countEventCRFsByStudySDV(studyId,
            // ("complete".equalsIgnoreCase(sdvStatus)));
        }

        return eventCRFDAO.countEventCRFsByStudySubject(studySubjectId, studyId, studyId);
    }

    public void setDataAndLimitVariables(TableFacade tableFacade, int studyId, HttpServletRequest request) {

        Limit limit = tableFacade.getLimit();

        EventCRFSDVFilter eventCRFSDVFilter = getEventCRFSDVFilter(limit, studyId);
        WebContext context = tableFacade.getWebContext();

        String restore = request.getAttribute(limit.getId() + "_restore") + "";
        if (!limit.isComplete()) {
            int totalRows = getTotalRowCount(eventCRFSDVFilter, studyId);
            tableFacade.setTotalRows(totalRows);
        } else if (restore != null && "true".equalsIgnoreCase(restore)) {
            int totalRows = getTotalRowCount(eventCRFSDVFilter, studyId);
            int pageNum = limit.getRowSelect().getPage();
            int maxRows = limit.getRowSelect().getMaxRows();
            tableFacade.setMaxRows(maxRows);
            tableFacade.setTotalRows(totalRows);
            limit.getRowSelect().setPage(pageNum);
        }

        EventCRFSDVSort eventCRFSDVSort = getEventCRFSDVSort(limit);

        int rowStart = limit.getRowSelect().getRowStart();
        int rowEnd = limit.getRowSelect().getRowEnd();
        // Collection<StudySubjectBean> items = getStudySubjectDAO().getWithFilterAndSort(getStudyBean(),
        // studySubjectSDVFilter, subjectSort, rowStart, rowEnd);
        Collection<SubjectSDVContainer> items = getFilteredItems(eventCRFSDVFilter, eventCRFSDVSort, rowStart, rowEnd, studyId, request);
        tableFacade.setItems(items);
        /*
         * Limit limit = tableFacade.getLimit();
         * FilterSet filterSet = limit.getFilterSet();
         * int totalRows = getTotalRowCount(filterSet, studyId);
         * 
         * tableFacade.setTotalRows(totalRows);
         * SortSet sortSet = limit.getSortSet();
         * int rowStart = limit.getRowSelect().getRowStart();
         * int rowEnd = limit.getRowSelect().getRowEnd();
         * Collection<SubjectSDVContainer> items = getFilteredItems(filterSet, sortSet, rowStart, rowEnd, studyId,
         * request);
         * 
         * tableFacade.setItems(items);
         */
    }

    private void updateLimitRowSelect(TableFacade tableFacade, HttpServletRequest request) {
        Limit limit = tableFacade.getLimit();
        String p = request.getParameter(limit.getId() + "_p_");
        int pn = p != null && p.length() > 0 ? Integer.parseInt(p) : 1;
    }

    public int getTotalRowCount(EventCRFSDVFilter eventCRFSDVFilter, Integer studyId) {

        EventCRFDAO eventCRFDAO = new EventCRFDAO(dataSource);
        return eventCRFDAO.getCountWithFilter(studyId, studyId, eventCRFSDVFilter);

    }

    protected EventCRFSDVFilter getEventCRFSDVFilter(Limit limit, Integer studyId) {
        EventCRFSDVFilter eventCRFSDVFilter = new EventCRFSDVFilter(studyId);
        FilterSet filterSet = limit.getFilterSet();
        Collection<Filter> filters = filterSet.getFilters();
        for (Filter filter : filters) {
            String property = filter.getProperty();
            String value = filter.getValue();
            eventCRFSDVFilter.addFilter(property, value);
        }

        return eventCRFSDVFilter;
    }

    protected EventCRFSDVSort getEventCRFSDVSort(Limit limit) {
        EventCRFSDVSort eventCRFSDVSort = new EventCRFSDVSort();
        SortSet sortSet = limit.getSortSet();
        Collection<Sort> sorts = sortSet.getSorts();
        for (Sort sort : sorts) {
            String property = sort.getProperty();
            String order = sort.getOrder().toParam();
            eventCRFSDVSort.addSort(property, order);
        }

        return eventCRFSDVSort;
    }

    @SuppressWarnings("unchecked")
    private Collection<SubjectSDVContainer> getFilteredItems(EventCRFSDVFilter filterSet, EventCRFSDVSort sortSet, int rowStart, int rowEnd, int studyId,
            HttpServletRequest request) {

        EventCRFDAO eventCRFDAO = new EventCRFDAO(dataSource);
        List<EventCRFBean> eventCRFBeans = new ArrayList<EventCRFBean>();
        /*
         * StudyEventDAO studyEventDAO = new StudyEventDAO(dataSource);
         * 
         * StudyDAO studyDAO = new StudyDAO(dataSource);
         * StudyBean studyBean = (StudyBean) studyDAO.findByPK(studyId);
         * 
         * String label = "";
         * String eventName = "";
         * String eventDate = "";
         * String sdvStatus = "";
         * String crfStatus = "";
         * String studyIdentifier = "";
         * String sdvRequirement = "";
         * SourceDataVerification sourceDataVerification = null;
         * 
         * if (filterSet.getFilter("studySubjectId") != null) {
         * 
         * label = filterSet.getFilter("studySubjectId").getValue().trim();
         * eventCRFBeans = eventCRFDAO.getEventCRFsByStudySubjectLabelLimit(label, studyId, studyId, rowEnd - rowStart,
         * rowStart);
         * 
         * } else if (filterSet.getFilter("eventName") != null) {
         * 
         * eventName = filterSet.getFilter("eventName").getValue().trim();
         * eventCRFBeans = eventCRFDAO.getEventCRFsByEventNameLimit(eventName, rowEnd - rowStart, rowStart);
         * 
         * } else if (filterSet.getFilter("eventDate") != null) {
         * 
         * eventDate = filterSet.getFilter("eventDate").getValue().trim();
         * eventCRFBeans = eventCRFDAO.getEventCRFsByEventDateLimit(studyId, eventDate, rowEnd - rowStart, rowStart);
         * 
         * } else if (filterSet.getFilter("crfStatus") != null) {
         * //
         * //SubjectEventStatus.getSubjectEventStatusIdByName(crfStatus)
         * crfStatus = filterSet.getFilter("crfStatus").getValue().trim();
         * //Get the study event for the event crf
         * eventCRFBeans = eventCRFDAO.getEventCRFsByCRFStatus(studyId, Integer.parseInt(crfStatus), rowEnd - rowStart,
         * rowStart);
         * 
         * } else if (filterSet.getFilter("sdvStatus") != null) {
         * 
         * sdvStatus = filterSet.getFilter("sdvStatus").getValue().trim();
         * eventCRFBeans = eventCRFDAO.getEventCRFsByStudySDV(studyId, ("complete".equalsIgnoreCase(sdvStatus)), rowEnd
         * - rowStart, rowStart);
         * 
         * } else if (filterSet.getFilter("studyIdentifier") != null) {
         * 
         * studyIdentifier = filterSet.getFilter("studyIdentifier").getValue().trim();
         * eventCRFBeans = eventCRFDAO.getEventCRFsByStudyIdentifier(studyId, studyId, studyIdentifier, rowEnd -
         * rowStart, rowStart);
         * 
         * } else if (filterSet.getFilter("sdvRequirementDefinition") != null) {
         * 
         * ArrayList<Integer> reqs = new ArrayList<Integer>();
         * sdvRequirement = filterSet.getFilter("sdvRequirementDefinition").getValue().trim();
         * if (sdvRequirement.contains("&")) {
         * for (String requirement : sdvRequirement.split("&")) {
         * reqs.add(SourceDataVerification.getByI18nDescription(requirement.trim()).getCode());
         * }
         * } else {
         * reqs.add(SourceDataVerification.getByI18nDescription(sdvRequirement.trim()).getCode());
         * }
         * if (reqs.size() > 0) {
         * Integer[] a = { 1 };
         * eventCRFBeans = eventCRFDAO.getEventCRFsBySDVRequirement(studyId, studyId, rowEnd - rowStart, rowStart,
         * reqs.toArray(a));
         * }
         * 
         * } else {
         * eventCRFBeans = eventCRFDAO.getEventCRFsByStudy(studyId, studyId, rowEnd - rowStart, rowStart);
         * 
         * }
         */
        eventCRFBeans = eventCRFDAO.getWithFilterAndSort(studyId, studyId, filterSet, sortSet, rowStart, rowEnd);
        return getSubjectRows(eventCRFBeans, request);
    }

    @SuppressWarnings("unchecked")
    private Collection<SubjectSDVContainer> getFilteredItemsSubject(FilterSet filterSet, SortSet sortSet, int rowStart, int rowEnd, int studyId,
            int studySubjectId, HttpServletRequest request) {

        EventCRFDAO eventCRFDAO = new EventCRFDAO(dataSource);
        StudyEventDAO studyEventDAO = new StudyEventDAO(dataSource);
        List<EventCRFBean> eventCRFBeans = new ArrayList<EventCRFBean>();

        String label = "";
        String eventName = "";
        String eventDate = "";
        String sdvStatus = "";
        String crfStatus = "";

        if (filterSet.getFilter("studySubjectId") != null) {

            label = filterSet.getFilter("studySubjectId").getValue().trim();
            eventCRFBeans = eventCRFDAO.getEventCRFsByStudySubjectLabelLimit(label, studyId, studyId, rowEnd - rowStart, rowStart);

        } else if (filterSet.getFilter("eventName") != null) {

            eventName = filterSet.getFilter("eventName").getValue().trim();
            // eventCRFBeans = eventCRFDAO.getEventCRFsByEventNameLimit(eventName,
            // rowEnd-rowStart,rowStart);

        } else if (filterSet.getFilter("eventDate") != null) {

            eventDate = filterSet.getFilter("eventDate").getValue().trim();
            // eventCRFBeans = eventCRFDAO.getEventCRFsByEventDateLimit(studyId,eventDate,
            // rowEnd-rowStart,rowStart);

        } else if (filterSet.getFilter("crfStatus") != null) {

            crfStatus = filterSet.getFilter("crfStatus").getValue().trim();
            // Get the study event for the event crf
            // eventCRFBeans = eventCRFDAO.getEventCRFsByCRFStatus(studyId,
            // SubjectEventStatus.getSubjectEventStatusIdByName(crfStatus),
            // rowEnd-rowStart,rowStart);

        } else if (filterSet.getFilter("sdvStatusActions") != null) {

            sdvStatus = filterSet.getFilter("sdvStatusActions").getValue().trim();
            // eventCRFBeans = eventCRFDAO.getEventCRFsByStudySDV(studyId,
            // ("complete".equalsIgnoreCase(sdvStatus)),
            // rowEnd-rowStart,rowStart);

        } else {
            eventCRFBeans = eventCRFDAO.getEventCRFsByStudySubjectLimit(studySubjectId, studyId, studyId, rowEnd - rowStart, rowStart);

        }

        return getSubjectRows(eventCRFBeans, request);
    }

    /*
     * private int getTotalRowCount(FilterSet filterSet, int studyId) {
     * 
     * 
     * EventCRFDAO eventCRFDAO = new EventCRFDAO(dataSource);
     * 
     * if (filterSet.getFilters().size() == 0) {
     * return eventCRFDAO.countEventCRFsByStudy(studyId, studyId);
     * }
     * 
     * int count = 0;
     * //Filter for study subject label
     * StudySubjectDAO studySubjectDAO = new StudySubjectDAO(dataSource);
     * StudySubjectBean studySubjectBean = new StudySubjectBean();
     * StudyDAO studyDAO = new StudyDAO(dataSource);
     * StudyBean studyBean = (StudyBean) studyDAO.findByPK(studyId);
     * String subjectValue = "";
     * String eventNameValue = "";
     * String eventDateValue = "";
     * String crfStatus = "";
     * String sdvStatus = "";
     * String studyIdentifier = "";
     * String sdvRequirement = "";
     * 
     * for (Filter filter : filterSet.getFilters()) {
     * 
     * if (filter.getProperty().equalsIgnoreCase("studySubjectId")) {
     * subjectValue = filter.getValue();
     * continue;
     * }
     * if (filter.getProperty().equalsIgnoreCase("eventName")) {
     * eventNameValue = filter.getValue();
     * continue;
     * }
     * 
     * if (filter.getProperty().equalsIgnoreCase("eventDate")) {
     * eventDateValue = filter.getValue();
     * continue;
     * }
     * 
     * if (filter.getProperty().equalsIgnoreCase("crfStatus")) {
     * crfStatus = filter.getValue();
     * continue;
     * }
     * 
     * if (filter.getProperty().equalsIgnoreCase("sdvStatus")) {
     * sdvStatus = filter.getValue();
     * }
     * 
     * if (filter.getProperty().equalsIgnoreCase("studyIdentifier")) {
     * studyIdentifier = filter.getValue();
     * }
     * 
     * if (filter.getProperty().equalsIgnoreCase("sdvRequirementDefinition")) {
     * sdvRequirement = filter.getValue();
     * }
     * }
     * 
     * SourceDataVerification sourceDataVerification = null;
     * 
     * if (subjectValue.length() > 0) {
     * return eventCRFDAO.countEventCRFsByStudySubjectLabel(subjectValue, studyId, studyId);
     * }
     * 
     * if (eventNameValue.length() > 0) {
     * return eventCRFDAO.countEventCRFsByEventName(eventNameValue);
     * }
     * 
     * if (eventDateValue.length() > 0) {
     * return eventCRFDAO.countEventCRFsByEventDate(studyId, eventDateValue);
     * }
     * //SubjectEventStatus.getSubjectEventStatusIdByName(crfStatus)
     * if (crfStatus.length() > 0) {
     * return eventCRFDAO.countEventCRFsByCRFStatus(studyId, Integer.parseInt(crfStatus));
     * }
     * 
     * if (sdvStatus.length() > 0) {
     * return eventCRFDAO.countEventCRFsByStudySDV(studyId, ("complete".equalsIgnoreCase(sdvStatus)));
     * }
     * 
     * if (studyIdentifier.length() > 0) {
     * return eventCRFDAO.countEventCRFsByStudyIdentifier(studyId, studyId, studyIdentifier);
     * }
     * 
     * if (sdvRequirement.length() > 0) {
     * ArrayList<Integer> reqs = new ArrayList<Integer>();
     * if (sdvRequirement.contains("&")) {
     * for (String requirement : sdvRequirement.split("&")) {
     * reqs.add(SourceDataVerification.getByI18nDescription(requirement.trim()).getCode());
     * }
     * } else {
     * reqs.add(SourceDataVerification.getByI18nDescription(sdvRequirement.trim()).getCode());
     * }
     * if (reqs.size() > 0) {
     * Integer[] a = { 1 };
     * return eventCRFDAO.countEventCRFsBySDVRequirement(studyId, studyId, reqs.toArray(a));
     * }
     * }
     * 
     * return eventCRFDAO.countEventCRFsByStudy(studyId, studyId);
     * }
     */

    /*
     * public String renderAllEventCRFTable(List<EventCRFBean> eventCRFBeans,
     * HttpServletRequest request){
     * 
     * Collection<SubjectSDVContainer> items = getSubjectRows(eventCRFBeans,request);
     * 
     * //The number of items represents the total number of returned rows
     * int totalRowCount =0;
     * if(items != null && items.size() > 0) {
     * totalRowCount = items.size();
     * }
     * TableFacade tableFacade = createTableFacade("sdv", request);
     * tableFacade.setStateAttr("restore");
     * 
     * String[] allColumns = new String[]{"studySubjectId","personId","secondaryId",
     * "eventName", "eventDate","enrollmentDate","studySubjectStatus","crfNameVersion","crfStatus",
     * "lastUpdatedDate","lastUpdatedBy","sdvStatusActions"};
     * 
     * tableFacade.setColumnProperties("studySubjectId","personId","secondaryId",
     * "eventName",
     * "eventDate","enrollmentDate","studySubjectStatus","crfNameVersion","crfStatus",
     * "lastUpdatedDate",
     * "lastUpdatedBy","sdvStatusActions");
     * 
     * tableFacade.addFilterMatcher(new MatcherKey(String.class, "studySubjectStatus"),
     * new SubjectStatusMatcher());
     * tableFacade.setItems(items);
     * 
     * HtmlRow row = (HtmlRow) tableFacade.getTable().getRow();
     * HtmlColumn studySubjectStatus = row.getColumn("studySubjectStatus");
     * studySubjectStatus.getFilterRenderer().setFilterEditor(new SubjectStatusFilter());
     * 
     * 
     * //fix HTML in columns
     * setHtmlCellEditors(tableFacade,allColumns,true);
     * 
     * 
     * //Create the custom toolbar
     * SDVToolbar sDVToolbar = new SDVToolbar();
     * 
     * // if(totalRowCount > 0){
     * if(totalRowCount <= 25){
     * sDVToolbar.setMaxRowsIncrements(new int[]{10,15,totalRowCount});}
     * else if(totalRowCount <= 50){
     * sDVToolbar.setMaxRowsIncrements(new int[]{10,25,totalRowCount});
     * } else if(totalRowCount > 0) {
     * sDVToolbar.setMaxRowsIncrements(new int[]{15,50,100});
     * }
     * tableFacade.setToolbar(sDVToolbar);
     * 
     * //Fix column titles
     * HtmlTable table = (HtmlTable) tableFacade.getTable();
     * //i18n caption; TODO: convert to Spring messages
     * ResourceBundle resourceBundle = ResourceBundle.getBundle(
     * "org.akaza.openclinica.i18n.words",LocaleResolver.getLocale(request));
     * 
     * String[] allTitles = {"Study Subject Id","Person Id","Secondary Id" ,"Event Name",
     * "Event Date","Enrollment Date","Subject Status","CRF Name / Version","CRF Status",
     * "Last Updated Date",
     * "Last Updated By","SDV Status / Actions"};
     * 
     * setTitles(allTitles,table);
     * 
     * //format column dates
     * formatColumns(table,new String[]{"eventDate","enrollmentDate","lastUpdatedDate"},
     * request);
     * 
     * table.getTableRenderer().setWidth("800");
     * return tableFacade.render();
     * }
     */

    public String renderEventCRFTableWithLimit(HttpServletRequest request, int studyId, String pathPrefix) {

        // boolean showMoreLink = Boolean.parseBoolean(request.getAttribute("showMoreLink").toString());//commented by
        // Jamuna, throwing null pointer exception
        boolean showMoreLink = Boolean.parseBoolean(request.getAttribute("showMoreLink") == null ? "false" : request.getAttribute("showMoreLink").toString());
        TableFacade tableFacade = createTableFacade("sdv", request);
        tableFacade.setStateAttr("sdv_restore");
        resformat = ResourceBundleProvider.getFormatBundle(LocaleResolver.getLocale(request));
        this.pathPrefix = pathPrefix;

        String[] allColumns = new String[] { "sdvStatus", "studySubjectId", "studyIdentifier", "personId", "secondaryId", "eventName", "eventDate",
                "enrollmentDate", "studySubjectStatus", "crfNameVersion", "sdvRequirementDefinition", "crfStatus", "lastUpdatedDate", "lastUpdatedBy",
                "studyEventStatus", "sdvStatusActions" };

        tableFacade.setColumnProperties("sdvStatus", "studySubjectId", "studyIdentifier", "personId", "secondaryId", "eventName", "eventDate", "enrollmentDate",
                "studySubjectStatus", "crfNameVersion", "sdvRequirementDefinition", "crfStatus", "lastUpdatedDate", "lastUpdatedBy", "studyEventStatus",
                "sdvStatusActions");

        tableFacade.addFilterMatcher(new MatcherKey(String.class, "studySubjectStatus"), new SubjectStatusMatcher());

        tableFacade.addFilterMatcher(new MatcherKey(String.class, "crfStatus"), new CrfStatusMatcher());

        tableFacade.addFilterMatcher(new MatcherKey(String.class, "sdvStatus"), new SdvStatusMatcher());

        tableFacade.addFilterMatcher(new MatcherKey(String.class, "sdvRequirementDefinition"), new SDVRequirementMatcher());

        this.setDataAndLimitVariables(tableFacade, studyId, request);

        // tableFacade.setItems(items);

        HtmlRow row = (HtmlRow) tableFacade.getTable().getRow();
        HtmlColumn studySubjectStatus = row.getColumn("studySubjectStatus");
        studySubjectStatus.getFilterRenderer().setFilterEditor(new SubjectStatusFilter());

        HtmlColumn crfStatus = row.getColumn("crfStatus");
        crfStatus.getFilterRenderer().setFilterEditor(new CrfStatusFilter());

        HtmlColumn actions = row.getColumn("sdvStatusActions");
        actions.getFilterRenderer().setFilterEditor(new DefaultActionsEditor(LocaleResolver.getLocale(request)));

        HtmlColumn sdvStatus = row.getColumn("sdvStatus");
        sdvStatus.getFilterRenderer().setFilterEditor(new SdvStatusFilter());

        HtmlColumn sdvRequirementDefinition = row.getColumn("sdvRequirementDefinition");
        sdvRequirementDefinition.getFilterRenderer().setFilterEditor(new SDVRequirementFilter());

        // fix HTML in columns
        setHtmlCellEditors(tableFacade, allColumns, true);

        // temporarily disable some of the filters for now
        turnOffFilters(tableFacade, new String[] { "personId", "secondaryId", "enrollmentDate", "studySubjectStatus", "crfNameVersion", "lastUpdatedDate",
                "lastUpdatedBy", "eventDate", "studyEventStatus" });

        turnOffSorts(tableFacade,
                new String[] { "sdvStatus", "studySubjectId", "studyIdentifier", "personId", "secondaryId", "eventName", "eventDate", "enrollmentDate",
                        "studySubjectStatus", "crfNameVersion", "sdvRequirementDefinition", "crfStatus", "lastUpdatedDate", "lastUpdatedBy", "studyEventStatus",
                        "sdvStatusActions" });

        // Create the custom toolbar
        SDVToolbar sDVToolbar = new SDVToolbar(showMoreLink);

        // if(totalRowCount > 0){
        sDVToolbar.setMaxRowsIncrements(new int[] { 15, 25, 50 });
        tableFacade.setToolbar(sDVToolbar);
        tableFacade.setView(new SDVView(LocaleResolver.getLocale(request), request));

        // Fix column titles
        HtmlTable table = (HtmlTable) tableFacade.getTable();
        // i18n caption; TODO: convert to Spring messages
        ResourceBundle resword = ResourceBundle.getBundle("org.akaza.openclinica.i18n.words", LocaleResolver.getLocale(request));

        String[] allTitles = { resword.getString("SDV_status"), resword.getString("study_subject_ID"), resword.getString("site_id"),
                resword.getString("person_ID"), resword.getString("secondary_ID"), resword.getString("event_name"), resword.getString("event_date"),
                resword.getString("enrollment_date"), resword.getString("subject_status"), resword.getString("CRF_name") + " / " + resword.getString("version"),
                resword.getString("SDV_requirement"), resword.getString("CRF_status"), resword.getString("last_updated_date"),
                resword.getString("last_updated_by"), resword.getString("study_event_status"), resword.getString("actions") };

        setTitles(allTitles, table);

        // format column dates
        formatColumns(table, new String[] { "eventDate", "enrollmentDate", "lastUpdatedDate" }, request);

        table.getTableRenderer().setWidth("800");
        return tableFacade.render();
    }

    public String renderSubjectsTableWithLimit(HttpServletRequest request, int studyId, int studySubjectId) {

        TableFacade tableFacade = createTableFacade("sdv", request);
        tableFacade.setStateAttr("restore");

        /*
         * StudySubjectDAO studySubjectDAO = new StudySubjectDAO(dataSource);
         * StudySubjectBean subjectBean = (StudySubjectBean) studySubjectDAO.findByPK(studySubjectId);
         */

        String[] allColumns = new String[] { "studySubjectId", "studyIdentifier", "personId", "secondaryId", "eventName", "eventDate", "enrollmentDate",
                "studySubjectStatus", "crfNameVersion", "sdvRequirementDefinition", "crfStatus", "lastUpdatedDate", "lastUpdatedBy", "studyEventStatus",
                "sdvStatusActions" };

        tableFacade.setColumnProperties("studySubjectId", "studyIdentifier", "personId", "secondaryId", "eventName", "eventDate", "enrollmentDate",
                "studySubjectStatus", "crfNameVersion", "sdvRequirementDefinition", "crfStatus", "lastUpdatedDate", "lastUpdatedBy", "studyEventStatus",
                "sdvStatusActions");

        tableFacade.addFilterMatcher(new MatcherKey(String.class, "studySubjectStatus"), new SubjectStatusMatcher());

        tableFacade.addFilterMatcher(new MatcherKey(String.class, "crfStatus"), new CrfStatusMatcher());

        tableFacade.addFilterMatcher(new MatcherKey(String.class, "sdvStatusActions"), new SdvStatusMatcher());

        tableFacade.addFilterMatcher(new MatcherKey(String.class, "sdvRequirementDefinition"), new SDVRequirementMatcher());

        int totalRowCount = 0;
        totalRowCount = setDataAndLimitVariablesSubjects(tableFacade, studyId, studySubjectId, request);

        // tableFacade.setItems(items);

        HtmlRow row = (HtmlRow) tableFacade.getTable().getRow();
        HtmlColumn studySubjectStatus = row.getColumn("studySubjectStatus");
        studySubjectStatus.getFilterRenderer().setFilterEditor(new SubjectStatusFilter());

        HtmlColumn crfStatus = row.getColumn("crfStatus");
        crfStatus.getFilterRenderer().setFilterEditor(new CrfStatusFilter());

        HtmlColumn sdvStatus = row.getColumn("sdvStatusActions");
        sdvStatus.getFilterRenderer().setFilterEditor(new SdvStatusFilter());

        HtmlColumn sdvRequirementDefinition = row.getColumn("sdvRequirementDefinition");
        sdvRequirementDefinition.getFilterRenderer().setFilterEditor(new SDVRequirementFilter());

        // fix HTML in columns
        setHtmlCellEditors(tableFacade, allColumns, true);

        // temporarily disable some of the filters for now
        turnOffFilters(tableFacade,
                new String[] { "personId", "secondaryId", "enrollmentDate", "studySubjectStatus", "crfNameVersion", "lastUpdatedDate", "lastUpdatedBy" });

        // Create the custom toolbar
        SDVToolbar sDVToolbar = new SDVToolbar(true);

        // if(totalRowCount > 0){
        sDVToolbar.setMaxRowsIncrements(new int[] { 15, 50, totalRowCount });
        tableFacade.setToolbar(sDVToolbar);

        // Fix column titles
        HtmlTable table = (HtmlTable) tableFacade.getTable();
        // i18n caption; TODO: convert to Spring messages
        ResourceBundle resword = ResourceBundle.getBundle("org.akaza.openclinica.i18n.words", LocaleResolver.getLocale(request));

        String[] allTitles = { resword.getString("study_subject_ID"), resword.getString("site_id"), resword.getString("person_ID"),
                resword.getString("secondary_ID"), resword.getString("event_name"), resword.getString("event_date"), resword.getString("enrollment_date"),
                resword.getString("subject_status"), resword.getString("CRF_name") + " / " + resword.getString("version"), resword.getString("SDV_requirement"),
                resword.getString("CRF_status"), resword.getString("last_updated_date"), resword.getString("last_updated_by"),
                resword.getString("study_event_status"), resword.getString("SDV_status") + " / " + resword.getString("actions") };

        setTitles(allTitles, table);

        // format column dates
        formatColumns(table, new String[] { "eventDate", "enrollmentDate", "lastUpdatedDate" }, request);

        table.getTableRenderer().setWidth("800");
        return tableFacade.render();
    }

    public void turnOffFilters(TableFacade tableFacade, String[] colNames) {

        HtmlRow row = (HtmlRow) tableFacade.getTable().getRow();
        HtmlColumn col = null;

        for (String colName : colNames) {
            col = row.getColumn(colName);
            col.setFilterable(false);
        }

    }

    public void turnOffSorts(TableFacade tableFacade, String[] colNames) {

        HtmlRow row = (HtmlRow) tableFacade.getTable().getRow();
        HtmlColumn col = null;

        for (String colName : colNames) {
            col = row.getColumn(colName);
            col.setSortable(false);
        }

    }

    public void setHtmlCellEditors(TableFacade tableFacade, String[] columnNames, boolean preventHtmlEscapes) {

        HtmlRow row = ((HtmlTable) tableFacade.getTable()).getRow();
        HtmlColumn column = null;

        for (String col : columnNames) {

            column = row.getColumn(col);
            column.getCellRenderer().setCellEditor(this.getCellEditorNoEscapes());

        }

    }

    public void formatColumns(HtmlTable table, String[] columnNames, HttpServletRequest request) {

        Locale locale = ResourceBundleProvider.localeMap.get(Thread.currentThread());
        if (locale == null) {
            ResourceBundleProvider.updateLocale(LocaleResolver.getLocale(request));
        }
        ResourceBundle bundle = ResourceBundleProvider.getFormatBundle();
        String format = bundle.getString("date_time_format_string");
        HtmlRow row = table.getRow();
        HtmlColumn column = null;

        for (String colName : columnNames) {
            column = row.getColumn(colName);
            if (column != null) {
                column.getCellRenderer().setCellEditor(new DateCellEditor(format));
            }
        }

    }

    /*
     * Generate the rows for the study table. Each row represents an Event CRF.
     */
    public Collection<SubjectSDVContainer> getSubjectRows(List<EventCRFBean> eventCRFBeans, HttpServletRequest request) {

        if (eventCRFBeans == null || eventCRFBeans.isEmpty()) {
            return new ArrayList<SubjectSDVContainer>();
        }

        getEventNamesForEventCRFs(eventCRFBeans);

        StudySubjectDAO studySubjectDAO = new StudySubjectDAO(dataSource);
        SubjectDAO subjectDAO = new SubjectDAO(dataSource);
        StudyDAO studyDAO = new StudyDAO(dataSource);
        StudyEventDAO studyEventDAO = new StudyEventDAO(dataSource);

        EventDefinitionCRFDAO eventDefinitionCRFDAO = new EventDefinitionCRFDAO(dataSource);

        StudySubjectBean studySubjectBean = null;
        SubjectBean subjectBean = null;
        StudyEventBean studyEventBean = null;
        StudyBean studyBean = null;
        EventDefinitionCRFBean eventDefinitionCRFBean = null;

        Collection<SubjectSDVContainer> allRows = new ArrayList<SubjectSDVContainer>();
        SubjectSDVContainer tempSDVBean = null;
        StringBuilder actions = new StringBuilder("");

        // Changed: The first row is the "select all" checkbox row
        // tempSDVBean = new SubjectSDVContainer();
        // String firstRowActions = "Select All <input type=checkbox name='checkSDVAll'
        // onclick='selectAllChecks(this.form)'/>";
        // tempSDVBean.setSdvStatusActions(firstRowActions);
        // allRows.add(tempSDVBean);

        for (EventCRFBean crfBean : eventCRFBeans) {

            tempSDVBean = new SubjectSDVContainer();

            studySubjectBean = (StudySubjectBean) studySubjectDAO.findByPK(crfBean.getStudySubjectId());
            studyEventBean = (StudyEventBean) studyEventDAO.findByPK(crfBean.getStudyEventId());

            subjectBean = (SubjectBean) subjectDAO.findByPK(studySubjectBean.getSubjectId());
            // find out the study's identifier
            studyBean = (StudyBean) studyDAO.findByPK(studySubjectBean.getStudyId());
            tempSDVBean.setStudyIdentifier(studyBean.getIdentifier());

            eventDefinitionCRFBean = eventDefinitionCRFDAO.findByStudyEventIdAndCRFVersionId(studyBean, studyEventBean.getId(), crfBean.getCRFVersionId());
            SourceDataVerification sourceData = eventDefinitionCRFBean.getSourceDataVerification();
            if (sourceData != null) {
                tempSDVBean.setSdvRequirementDefinition(sourceData.toString());
            } else {
                tempSDVBean.setSdvRequirementDefinition("");
            }

            tempSDVBean.setCrfNameVersion(getCRFName(crfBean.getCRFVersionId()) + "/ " + getCRFVersionName(crfBean.getCRFVersionId()));

            if (crfBean.getStatus() != null) {

                Integer status = crfBean.getStage().getId();

                if (studyEventBean.getSubjectEventStatus() == SubjectEventStatus.LOCKED || studyEventBean.getSubjectEventStatus() == SubjectEventStatus.STOPPED
                        || studyEventBean.getSubjectEventStatus() == SubjectEventStatus.SKIPPED) {
                    status = DataEntryStage.LOCKED.getId();

                }

                tempSDVBean.setCrfStatus(getCRFStatusIconPath(status, request, studySubjectBean.getId(), crfBean.getId(), crfBean.getCRFVersionId()));
            }

            tempSDVBean.setStudyEventStatus(studyEventBean.getStatus().getName());

            // TODO: I18N Date must be formatted properly
            Locale locale = LocaleResolver.getLocale(request);
            SimpleDateFormat sdformat = I18nFormatUtil.getDateFormat(locale);

            if (studySubjectBean.getEnrollmentDate() != null) {
                tempSDVBean.setEnrollmentDate(sdformat.format(studySubjectBean.getEnrollmentDate()));
            } else {
                // tempSDVBean.setEnrollmentDate("unknown");

            }
            // TODO: I18N Date must be formatted properly
            // Fix OC 1888
            StudyEventDAO sedao = new StudyEventDAO(dataSource);
            StudyEventBean seBean = (StudyEventBean) sedao.findByPK(crfBean.getStudyEventId());
            tempSDVBean.setEventDate(sdformat.format(seBean.getDateStarted()));

            // if (crfBean.getCreatedDate() != null) {
            // tempSDVBean.setEventDate(sdformat.format(crfBean.getCreatedDate()));
            // } else {
            // //tempSDVBean.setEventDate("unknown");
            //
            // }
            // crfBean.getEventName()
            tempSDVBean.setEventName(crfBean.getEventName());
            // The checkbox is next to the study subject id
            StringBuilder sdvStatus = new StringBuilder("");
            // .getNexGenStatus().getCode() == 10
            // "This Event CRF has been Source Data Verified. If you uncheck this box, you are removing Source Data
            // Verification for the Event CRF and you will have to repeat the process. Select OK to continue and Cancel
            // to cancel this transaction."
            if (crfBean.isSdvStatus() && studyBean.getStatus().isLocked()) {
                sdvStatus.append(getIconForSdvStatusPrefix()).append("DoubleCheck").append(ICON_FORCRFSTATUS_SUFFIX).append("</a></center>");
            } else if (crfBean.isSdvStatus() && !studyBean.getStatus().isLocked()) {
                sdvStatus.append("<center><a href='javascript:void(0)' onclick='prompt(document.sdvForm,");
                sdvStatus.append(crfBean.getId());
                sdvStatus.append(")'>");
                sdvStatus.append(getIconForSdvStatusPrefix()).append("DoubleCheck").append(ICON_FORCRFSTATUS_SUFFIX).append("</a></center>");
            } else {
                sdvStatus.append("<center><input style='margin-right: 5px' type='checkbox' ").append("class='sdvCheck'").append(" name='").append(CHECKBOX_NAME)
                        .append(crfBean.getId()).append("' /></center>");

            }
            // sdvStatus.append(studySubjectBean.getLabel());
            tempSDVBean.setSdvStatus(sdvStatus.toString());
            tempSDVBean.setStudySubjectId(studySubjectBean.getLabel());

            if (subjectBean != null) {
                tempSDVBean.setPersonId(subjectBean.getUniqueIdentifier());
            } else {
                tempSDVBean.setPersonId("");

            }
            // studySubjectBean.getSecondaryLabel()
            tempSDVBean.setSecondaryId(studySubjectBean.getSecondaryLabel());

            String statusName = studySubjectBean.getStatus().getName();
            int statusId = studySubjectBean.getStatus().getId();

            if (statusName != null) {
                tempSDVBean.setStudySubjectStatus(statusName);
            }

            // TODO: I18N Date must be formatted properly
            if (crfBean.getUpdatedDate() != null) {
                tempSDVBean.setLastUpdatedDate(sdformat.format(crfBean.getUpdatedDate()));
            } else {
                tempSDVBean.setLastUpdatedDate("unknown");

            }

            if (crfBean.getUpdater() != null) {

                tempSDVBean.setLastUpdatedBy(crfBean.getUpdater().getFirstName() + " " + crfBean.getUpdater().getLastName());

            }

            actions = new StringBuilder("");
            // append("<input type='hidden' name='crfId' value='").append(crfBean.getId()).append("'").append("/> ")
            if (!crfBean.isSdvStatus()) {
                StringBuilder jsCodeString = new StringBuilder("this.form.method='GET'; this.form.action='").append(request.getContextPath())
                        .append("/pages/handleSDVGet").append("';").append("this.form.crfId.value='").append(crfBean.getId()).append("';")
                        .append("this.form.submit();");
                if (!studyBean.getStatus().isLocked()) {
                    actions.append("<input type=\"submit\" class=\"button_medium\" value=\"SDV\" name=\"sdvSubmit\" ").append("onclick=\"")
                            .append(jsCodeString.toString()).append("\" />");
                }
            }

            // Only implement the view icon if it is a event crf request
            /*
             * String bool = (String) request.getAttribute("isViewSubjectRequest");
             * if(! "y".equalsIgnoreCase(bool)){
             * StringBuilder urlPrefix = new
             * StringBuilder("<a href='javascript:void(0)' onclick=\"document.location.href='");
             * StringBuilder path = new
             * StringBuilder(request.getContextPath()).append("/pages/viewAllSubjectSDV?studyId=").append(
             * studySubjectBean.getStudyId()).append("&studySubjectId=");
             * path.append(studySubjectBean.getId());
             * 
             * urlPrefix.append(path).append("'\">");
             * actions.append("&nbsp;").append(urlPrefix).append(VIEW_ICON_HTML).append("</a>");
             * }
             */
            tempSDVBean.setSdvStatusActions(actions.toString());
            allRows.add(tempSDVBean);

        }

        return allRows;
    }

    private String getCRFStatusIconPath(int statusId, HttpServletRequest request, int studySubjectId, int eventDefinitionCRFId, int crfVersionId) {

        HtmlBuilder html = new HtmlBuilder();
        html.a().onclick("openDocWindow('" + request.getContextPath() + "/ViewSectionDataEntry?eventDefinitionCRFId=&ecId=" + eventDefinitionCRFId
                + "&tabId=1&studySubjectId=" + studySubjectId + "');");
        html.href("#").close();

        StringBuilder builderHref = new StringBuilder("<a href='javascript:void(0)' onclick=\"");
        // ViewSectionDataEntry?eventDefinitionCRFId=127&crfVersionId=682&tabId=1&studySubjectId=203
        builderHref.append("document.location.href='").append(request.getContextPath()).append("/");
        builderHref.append("ViewSectionDataEntry?eventDefinitionCRFId=").append(eventDefinitionCRFId);
        builderHref.append("&crfVersionId=").append(crfVersionId).append("&tabId=1&studySubjectId=").append(studySubjectId).append("'\">");

        StringBuilder builder = new StringBuilder(html.toString()).append(getIconForCrfStatusPrefix());

        String imgName = "";
        StringBuilder input = new StringBuilder("<input type=\"hidden\" statusId=\"");
        input.append(statusId).append("\" />");

        if (statusId > 0 && statusId < 8) {

            builder.append(CRF_STATUS_ICONS.get(statusId));
        } else {
            builder.append(CRF_STATUS_ICONS.get(0));

        }
        builder.append(ICON_FORCRFSTATUS_SUFFIX);
        // "<input type=\"hidden\" statusId=\"1\" />"
        builder.append("</a>");
        builder.append(" ");
        builder.append(input.toString());
        return builder.toString();
    }

    public List<Integer> getListOfSdvEventCRFIds(Collection<String> paramsContainingIds) {

        List<Integer> eventCRFWithSDV = new ArrayList<Integer>();
        if (paramsContainingIds == null || paramsContainingIds.isEmpty()) {
            return eventCRFWithSDV;
        }
        int tmpInt;
        for (String param : paramsContainingIds) {
            tmpInt = stripPrefixFromParam(param);
            if (tmpInt != 0) {
                eventCRFWithSDV.add(tmpInt);
            }
        }

        return eventCRFWithSDV;
    }

    private int stripPrefixFromParam(String param) {
        if (param != null && param.contains(CHECKBOX_NAME)) {
            return Integer.parseInt(param.substring(param.indexOf("_") + 1));
        } else {
            return 0;
        }
    }

    public List<Integer> getListOfStudySubjectIds(Set<String> paramsContainingIds) {
        List<Integer> studySubjectIds = new ArrayList<Integer>();
        int tmpInt;

        if (paramsContainingIds == null || paramsContainingIds.isEmpty()) {
            return studySubjectIds;
        }
        for (String param : paramsContainingIds) {
            tmpInt = stripPrefixFromParam(param);
            if (tmpInt != 0) {
                studySubjectIds.add(tmpInt);
            }
        }
        return studySubjectIds;
    }

    public Collection<SubjectSDVContainer> getSubjectAggregateRows(List<StudySubjectBean> studySubjectBeans) {

        if (studySubjectBeans == null || studySubjectBeans.isEmpty()) {
            return new ArrayList<SubjectSDVContainer>();
        }
        Collection<SubjectSDVContainer> allRows = new ArrayList<SubjectSDVContainer>();
        SubjectSDVContainer tempSDVBean = null;

        // The first row is the "select all" checkbox row
        tempSDVBean = new SubjectSDVContainer();
        String firstRowActions = "Select All <input type=checkbox name='checkAll' onclick='selectAllChecks(this.form)'/>";
        tempSDVBean.setSdvStatusActions(firstRowActions);
        allRows.add(tempSDVBean);
        StringBuilder actions;

        for (StudySubjectBean studySubjectBean : studySubjectBeans) {
            tempSDVBean = new SubjectSDVContainer();
            tempSDVBean.setStudySubjectId(studySubjectBean.getId() + "");
            // studySubjectBean.getStatus().getName() : TODO: fix ResourceBundle problem
            tempSDVBean.setStudySubjectStatus("subject status");
            tempSDVBean.setNumberOfCRFsSDV("0");
            tempSDVBean.setPercentageOfCRFsSDV("0");
            tempSDVBean.setGroup("group");
            actions = new StringBuilder("<input class='sdvCheckbox' type='checkbox' name=");
            actions.append("'sdvCheck").append(studySubjectBean.getId()).append("'/>&nbsp;&nbsp");
            actions.append(VIEW_ICON_FORSUBJECT_PREFIX).append(studySubjectBean.getId()).append(getIconForSubjectSufix());
            tempSDVBean.setSdvStatusActions(actions.toString());

            allRows.add(tempSDVBean);

        }

        return allRows;

    }

    public void getEventNamesForEventCRFs(List<EventCRFBean> eventCRFBeans) {
        if (eventCRFBeans == null || eventCRFBeans.isEmpty())
            return;

        StudyEventDAO studyEventDAO = new StudyEventDAO(dataSource);
        StudyEventDefinitionDAO studyEventDefinitionDAO = new StudyEventDefinitionDAO(dataSource);

        StudyEventBean studyEventBean = null;
        StudyEventDefinitionBean studyEventDefBean = null;
        // Provide a value for the eventName property of the EventCRF
        for (EventCRFBean eventCRFBean : eventCRFBeans) {
            if ("".equalsIgnoreCase(eventCRFBean.getEventName())) {
                studyEventBean = (StudyEventBean) studyEventDAO.findByPK(eventCRFBean.getStudyEventId());
                studyEventDefBean = (StudyEventDefinitionBean) studyEventDefinitionDAO.findByPK(studyEventBean.getStudyEventDefinitionId());
                eventCRFBean.setEventName(studyEventDefBean.getName() + "(" + studyEventBean.getSampleOrdinal() + ")");
            }
        }

    }

    /* Create the titles for the HTML table's rows */
    public void setTitles(String[] allTitles, HtmlTable table) {
        HtmlRow row = table.getRow();
        HtmlColumn tempColumn = null;

        for (int i = 0; i < allTitles.length; i++) {
            tempColumn = row.getColumn(i);
            tempColumn.setTitle(allTitles[i]);
        }

    }

    public String getCRFName(int crfVersionId) {
        CRFVersionDAO cRFVersionDAO = new CRFVersionDAO(dataSource);
        CRFDAO cRFDAO = new CRFDAO(dataSource);

        CRFVersionBean versionBean = (CRFVersionBean) cRFVersionDAO.findByPK(crfVersionId);
        if (versionBean != null) {
            CRFBean crfBean = (CRFBean) cRFDAO.findByPK(versionBean.getCrfId());
            if (crfBean != null)
                return crfBean.getName();
        }

        return "";
    }

    public String getCRFVersionName(int crfVersionId) {

        CRFVersionDAO cRFVersionDAO = new CRFVersionDAO(dataSource);
        CRFVersionBean versionBean = (CRFVersionBean) cRFVersionDAO.findByPK(crfVersionId);
        if (versionBean != null) {
            return versionBean.getName();
        }

        return "";

    }

    public List<EventCRFBean> getAllEventCRFs(List<StudyEventBean> studyEventBeans) {

        List<EventCRFBean> eventCRFBeans = new ArrayList<EventCRFBean>();
        List<EventCRFBean> studyEventCRFBeans = new ArrayList<EventCRFBean>();

        EventCRFDAO eventCRFDAO = new EventCRFDAO(dataSource);

        for (StudyEventBean studyEventBean : studyEventBeans) {
            eventCRFBeans = eventCRFDAO.findAllByStudyEvent(studyEventBean);
            if (eventCRFBeans != null && !eventCRFBeans.isEmpty()) {
                studyEventCRFBeans.addAll(eventCRFBeans);
            }
        }

        return studyEventCRFBeans;
    }

    public String renderSubjectsAggregateTable(int studyId, HttpServletRequest request) {

        List<StudySubjectBean> studySubjectBeans = new ArrayList<StudySubjectBean>();
        StudySubjectDAO studySubjectDAO = new StudySubjectDAO(dataSource);
        studySubjectBeans = studySubjectDAO.findAllByStudyId(studyId);

        Collection<SubjectSDVContainer> items = getSubjectAggregateRows(studySubjectBeans);

        // The number of items represents the total number of returned rows
        int totalRowCount = 0;
        if (items != null && items.size() > 0) {
            totalRowCount = items.size();
        }

        TableFacade tableFacade = createTableFacade("sdv", request);
        // The default display for the JMesa Limit select widget is 1,50,100 rows
        // We'll change this if the subject has more than one row, and have the last choice
        // set to the total row count
        if (totalRowCount > 1) {
            tableFacade.setMaxRowsIncrements(15, 50, totalRowCount);
        }
        tableFacade.setColumnProperties("studySubjectId", "studySubjectStatus", "numberOfCRFsSDV", "percentageOfCRFsSDV", "group", "sdvStatusActions");

        tableFacade.setItems(items);
        // Fix column titles
        HtmlTable table = (HtmlTable) tableFacade.getTable();
        // i18n caption; TODO: convert to Spring messages
        ResourceBundle resword = ResourceBundle.getBundle("org.akaza.openclinica.i18n.words", LocaleResolver.getLocale(request));

        String[] allTitles = { resword.getString("study_subject_ID"), resword.getString("study_subject_status"), resword.getString("num_CRFs_SDV"),
                resword.getString("porc_CRFs_SDV"), resword.getString("group") };

        setTitles(allTitles, table);

        table.getTableRenderer().setWidth("800");
        return tableFacade.render();

    }

    public boolean setSDVStatusForStudySubjects(List<Integer> studySubjectIds, int userId, boolean setVerification) {

        EventCRFDAO eventCRFDAO = new EventCRFDAO(dataSource);
        StudySubjectDAO studySubjectDAO = new StudySubjectDAO(dataSource);
        EventDefinitionCRFDAO eventDefinitionCrfDAO = new EventDefinitionCRFDAO(dataSource);
        StudyEventDAO studyEventDAO = new StudyEventDAO(dataSource);
        CRFDAO crfDAO = new CRFDAO(dataSource);

        if (studySubjectIds == null || studySubjectIds.isEmpty()) {
            return true;
        }

        for (Integer studySubjectId : studySubjectIds) {
            ArrayList<EventCRFBean> eventCrfs = eventCRFDAO.getEventCRFsByStudySubjectCompleteOrLocked(studySubjectId);
            StudySubjectBean studySubject = (StudySubjectBean) studySubjectDAO.findByPK(studySubjectId);
            for (EventCRFBean eventCRFBean : eventCrfs) {
                CRFBean crfBean = crfDAO.findByVersionId(eventCRFBean.getCRFVersionId());
                StudyEventBean studyEvent = (StudyEventBean) studyEventDAO.findByPK(eventCRFBean.getStudyEventId());
                EventDefinitionCRFBean eventDefinitionCrf = eventDefinitionCrfDAO
                        .findByStudyEventDefinitionIdAndCRFIdAndStudyId(studyEvent.getStudyEventDefinitionId(), crfBean.getId(), studySubject.getStudyId());
                if (eventDefinitionCrf.getId() == 0) {
                    eventDefinitionCrf = eventDefinitionCrfDAO.findForStudyByStudyEventDefinitionIdAndCRFId(studyEvent.getStudyEventDefinitionId(),
                            crfBean.getId());
                }
                if (eventDefinitionCrf.getSourceDataVerification() == SourceDataVerification.AllREQUIRED
                        || eventDefinitionCrf.getSourceDataVerification() == SourceDataVerification.PARTIALREQUIRED) {
                    try {
                        eventCRFDAO.setSDVStatus(setVerification, userId, eventCRFBean.getId());
                    } catch (Exception exc) {
                        // System.out.println(exc.getMessage());
                        return false;
                    }
                }

            }
            studySubjectDAO.update(studySubject);
        }
        return true;
    }

    public boolean setSDVerified(List<Integer> eventCRFIds, int userId, boolean setVerification) {

        // If no event CRFs are offered to SDV, then the transaction has not
        // caused a problem, so return true
        if (eventCRFIds == null || eventCRFIds.isEmpty()) {
            return true;
        }

        EventCRFDAO eventCRFDAO = new EventCRFDAO(dataSource);

        for (Integer eventCrfId : eventCRFIds) {
            try {
                eventCRFDAO.setSDVStatus(setVerification, userId, eventCrfId);
            } catch (Exception exc) {
                // System.out.println(exc.getMessage());
                return false;
            }
        }

        return true;
    }

    public void forwardRequestFromController(HttpServletRequest request, HttpServletResponse response, String path) {
        try {
            request.getRequestDispatcher(path).forward(request, response);
        } catch (ServletException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void prepareSDVSelectElements(HttpServletRequest request, StudyBean studyBean) {
        // Study event statuses
        List<String> studyEventStatuses = new ArrayList<String>();
        for (org.akaza.openclinica.domain.Status stat : org.akaza.openclinica.domain.Status.values()) {
            studyEventStatuses.add(stat.getDescription());
        }

        request.setAttribute("studyEventStatuses", studyEventStatuses);

        // SDV requirements
        List<String> sdvRequirements = new ArrayList<String>();
        for (SourceDataVerification sdvRequire : SourceDataVerification.values()) {
            sdvRequirements.add(sdvRequire.getDescription());
        }

        request.setAttribute("sdvRequirements", SourceDataVerification.values());

        // study event definitions
        StudyEventDefinitionDAO studyEventDefinitionDAO = new StudyEventDefinitionDAO(dataSource);

        List<StudyEventDefinitionBean> studyEventDefinitionBeans = new ArrayList<StudyEventDefinitionBean>();

        studyEventDefinitionBeans = studyEventDefinitionDAO.findAllByStudy(studyBean);
        request.setAttribute("studyEventDefinitions", studyEventDefinitionBeans);

        // study event status
        request.setAttribute("studyEventStatuses", Status.toArrayList());

        // event CRF status
        request.setAttribute("eventCRFDStatuses", SubjectEventStatus.toArrayList());

        StudyEventDAO studyEventDAO = new StudyEventDAO(dataSource);

        List<StudyEventBean> studyEventBeans = studyEventDAO.findAllByStudy(studyBean);
        List<EventCRFBean> eventCRFBeans = getAllEventCRFs(studyEventBeans);
        SortedSet<String> eventCRFNames = new TreeSet<String>();

        for (EventCRFBean bean : eventCRFBeans) {
            eventCRFNames.add(getCRFName(bean.getCRFVersionId()));
        }
        request.setAttribute("eventCRFNames", eventCRFNames);

    }

    public List<EventCRFBean> filterEventCRFs(List<EventCRFBean> eventCRFBeans, BindingResult bindingResult) {

        /*
         * study_subject_id=Subject+D&eventCRF=0&studyEventDefinition=0&
         * studyEventStatus=-1&eventCRFStatus=-1&eventcrfSDVStatus=None&
         * sdvRequirement=0&startUpdatedDate=&endDate=&submit=Apply+Filter
         */
        List<EventCRFBean> newList = new ArrayList<EventCRFBean>();

        if (eventCRFBeans == null || eventCRFBeans.isEmpty() || bindingResult == null) {
            return eventCRFBeans;
        }

        SdvFilterDataBean filterBean = (SdvFilterDataBean) bindingResult.getTarget();
        StudySubjectBean studySubjectBean = null;
        StudyEventBean studyEventBean = null;
        StudySubjectDAO studySubjectDAO = new StudySubjectDAO(dataSource);
        StudyEventDAO studyEventDAO = new StudyEventDAO(dataSource);
        boolean studySub = true, studyEventDef = true, studyEventStatus = true, eventCRFStatusBool = true, eventcrfSDVStatus = true, eventCRFNameBool = true,
                upDatedDateBool = true, sdvRequirementBool = true;

        for (EventCRFBean eventCBean : eventCRFBeans) {
            // filter study subject
            if (filterBean.getStudy_subject_id().length() > 0) {
                studySubjectBean = (StudySubjectBean) studySubjectDAO.findByPK(eventCBean.getStudySubjectId());

                studySub = filterBean.getStudy_subject_id().equalsIgnoreCase(studySubjectBean.getLabel());

            }

            // filter study event definition

            if (filterBean.getStudyEventDefinition() > 0) {
                studyEventBean = (StudyEventBean) studyEventDAO.findByPK(eventCBean.getStudyEventId());

                studyEventDef = filterBean.getStudyEventDefinition() == studyEventBean.getStudyEventDefinitionId();

            }

            // Event CRF status
            if (filterBean.getStudyEventStatus() > 0) {

                studyEventStatus = filterBean.getStudyEventStatus() == eventCBean.getStatus().getId();
            }

            // Event CRF subject event status
            if (filterBean.getEventCRFStatus() > 0) {
                studyEventBean = (StudyEventBean) studyEventDAO.findByPK(eventCBean.getStudyEventId());

                eventCRFStatusBool = filterBean.getEventCRFStatus() == studyEventBean.getSubjectEventStatus().getId();
            }

            // Event CRF SDV status; true or false
            if (!filterBean.getEventcrfSDVStatus().equalsIgnoreCase("N/A")) {
                boolean sdvBool = filterBean.getEventcrfSDVStatus().equalsIgnoreCase("complete");
                eventcrfSDVStatus = eventCBean.isSdvStatus() == sdvBool;
            }

            // Event CRF name match
            if (filterBean.getEventCRFName().length() > 0) {
                String tmpName = getCRFName(eventCBean.getCRFVersionId());
                eventCRFNameBool = tmpName.equalsIgnoreCase(filterBean.getEventCRFName());
            }

            // TODO: Event CRF SDV requirement, when the application provides a way
            // TODO: of setting this requirement in the event definition

            // event CRF updated date
            if (eventCBean.getUpdatedDate() != null && filterBean.getStartUpdatedDate() != null && filterBean.getEndDate() != null) {

                GregorianCalendar calStart = new GregorianCalendar();
                calStart.setTime(filterBean.getStartUpdatedDate());

                GregorianCalendar calendarEnd = new GregorianCalendar();
                calendarEnd.setTime(filterBean.getEndDate());

                GregorianCalendar calendarNow = new GregorianCalendar();
                calendarNow.setTime(eventCBean.getUpdatedDate());

                upDatedDateBool = calendarNow.after(calStart) && calendarNow.before(calendarEnd) || calendarNow.equals(calStart)
                        || calendarNow.equals(calendarEnd);

            }

            if (upDatedDateBool && eventCRFNameBool && eventcrfSDVStatus && eventCRFStatusBool && studyEventStatus && studyEventDef && studySub) {
                newList.add(eventCBean);
            }
        }
        return newList;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    class SDVView extends AbstractHtmlView {

        private final ResourceBundle resword;
        private boolean showTitle = false;

        public SDVView(Locale locale, HttpServletRequest request) {
            resword = ResourceBundleProvider.getWordsBundle(locale);
            if (request.getRequestURI().contains("MainMenu"))
                showTitle = true;
        }

        public Object render() {
            HtmlSnippets snippets = getHtmlSnippets();
            HtmlBuilder html = new HtmlBuilder();
            html.append(snippets.themeStart());

            html.append(snippets.tableStart());

            html.append(snippets.theadStart());
            // html.append(snippets.tableStart());
            html.append(customHeader());
            html.append(snippets.toolbar());
            html.append(selectAll());

            html.append(snippets.header());
            html.append(snippets.filter());

            html.append(snippets.tbodyStart());

            html.append(snippets.body());

            html.append(snippets.theadEnd());
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

        private String customHeader() {
            if (showTitle) {
                HtmlBuilder html = new HtmlBuilder();

                html.tr(0).styleClass("header").width("100%").close();
                html.td(0).colspan("100%").style("border-bottom: 1px solid white;background-color:white;color:black;font-size:12px;").align("left").close()
                        .append(resword.getString("source_data_verification")).tdEnd().trEnd(0);

                return html.toString();
            } else
                return "";
        }
    }

    class NoEscapeHtmlCellEditor extends HtmlCellEditor {

        @Override
        public Object getValue(Object item, String property, int rowcount) {
            return ItemUtils.getItemValue(item, property);
        }
    }

}
