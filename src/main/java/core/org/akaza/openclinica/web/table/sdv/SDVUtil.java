package core.org.akaza.openclinica.web.table.sdv;

import static org.jmesa.facade.TableFacadeFactory.createTableFacade;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import core.org.akaza.openclinica.bean.admin.CRFBean;
import core.org.akaza.openclinica.bean.core.DataEntryStage;
import core.org.akaza.openclinica.bean.core.Status;
import core.org.akaza.openclinica.bean.core.SubjectEventStatus;
import core.org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import core.org.akaza.openclinica.bean.managestudy.StudyEventBean;
import core.org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.bean.submit.CRFVersionBean;
import core.org.akaza.openclinica.bean.submit.EventCRFBean;
import core.org.akaza.openclinica.bean.submit.FormLayoutBean;
import core.org.akaza.openclinica.bean.submit.SubjectBean;
import core.org.akaza.openclinica.dao.hibernate.*;
import core.org.akaza.openclinica.domain.datamap.*;
import core.org.akaza.openclinica.domain.user.UserAccount;
import core.org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.control.DefaultActionsEditor;
import org.akaza.openclinica.controller.dto.SdvDTO;
import org.akaza.openclinica.controller.dto.SdvItemDTO;
import org.akaza.openclinica.controller.helper.table.SDVToolbar;
import org.akaza.openclinica.controller.helper.table.SubjectSDVContainer;
import core.org.akaza.openclinica.dao.EventCRFSDVFilter;
import core.org.akaza.openclinica.dao.EventCRFSDVSort;
import core.org.akaza.openclinica.dao.admin.CRFDAO;
import core.org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import core.org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import core.org.akaza.openclinica.dao.submit.CRFVersionDAO;
import core.org.akaza.openclinica.dao.submit.EventCRFDAO;
import core.org.akaza.openclinica.dao.submit.FormLayoutDAO;
import core.org.akaza.openclinica.dao.submit.SubjectDAO;
import core.org.akaza.openclinica.domain.SourceDataVerification;
import core.org.akaza.openclinica.i18n.core.LocaleResolver;
import core.org.akaza.openclinica.i18n.util.I18nFormatUtil;
import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.domain.enumsupport.EventCrfWorkflowStatusEnum;
import org.akaza.openclinica.domain.enumsupport.SdvStatus;
import org.akaza.openclinica.domain.enumsupport.StudyEventWorkflowStatusEnum;
import org.akaza.openclinica.web.restful.errors.ErrorConstants;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A utility class that implements the details of the Source Data Verification (SDV) Jmesa tables.
 */
public class SDVUtil {

    private StudyDao studyDao;
    @Autowired
    private EventCrfDao eventCrfDao;
    @Autowired
    private ItemDao itemDao;
    @Autowired
    private ItemDataDao itemDataDao;
    @Autowired
    private DiscrepancyNoteDao discrepancyNoteDao;
    @Autowired
    private ItemGroupMetadataDao itemGroupMetadataDao;
    @Autowired
    private UserAccountDao userAccountDao;
    @Autowired
    private EventDefinitionCrfDao eventDefinitionCrfDao;
    private static final Logger logger = LoggerFactory.getLogger(SDVUtil.class);
    private final static String VIEW_ICON_FORSUBJECT_PREFIX = "<a onmouseup=\"javascript:setImage('bt_View1','images/bt_View.gif');\" onmousedown=\"javascript:setImage('bt_View1','images/bt_View_d.gif');\" href=\"ViewStudySubject?id=";
    private final static String VIEW_ICON_FORSUBJECT_SUFFIX = "\"><span hspace=\"6\" border=\"0\" align=\"left\" title=\"View\" alt=\"View\" class=\"icon icon-serach\" name=\"bt_View1\"/></a>";
    private final static String ICON_FORCRFSTATUS_PREFIX = "<span hspace='2' border='0'  title='Event CRF Status' alt='Event CRF Status' class='icon icon-search'>";

    private final static String ICON_FORCRFSTATUS_SUFFIX = ".gif'/>";
    public final static String CHECKBOX_NAME = "sdvCheck_";
    public final static String VIEW_ICON_HTML = "<span class=\"icon icon-search\" border=\"0>";
    private ResourceBundle resformat;
    private ResourceBundle resWords;
    private final static String FORM_LOCKED_ICON_CLASS_NAME = "icon icon-lock";
    private final static String FORM_COMPLETED_ICON_CLASS_NAME = "icon icon-checkbox-checked green";
    private String pathPrefix;

    String getIconForCrfStatusPrefix() {
        String prefix = pathPrefix == null ? "../" : pathPrefix;
        return "<span hspace='2' border='0'  title='Event CRF Status' alt='Event CRF Status' class='icon icon-ok'>";
    }

    String getIconForSubjectSufix() {
        String prefix = pathPrefix == null ? "../" : pathPrefix;
        return "\"><span hspace=\"6\" border=\"0\" align=\"left\" title=\"View\" alt=\"View\" class=\"icon icon-search\" name=\"bt_View1\"/></a>";
    }

    String getIconForViewHtml() {
        String prefix = pathPrefix == null ? "../" : pathPrefix;
        return "<span src=\"icon icon-search\" border=\"0\" />";
    }

    public final static Map<core.org.akaza.openclinica.domain.datamap.SubjectEventStatus, String> SUBJECT_EVENT_STATUS_ICONS = new HashMap<core.org.akaza.openclinica.domain.datamap.SubjectEventStatus, String>();
    public final static Map<Integer, String> CRF_STATUS_ICONS = new HashMap<Integer, String>();
    public final static Map<StudyEventWorkflowStatusEnum, String> STUDY_EVENT_WORKFLOW_ICONS = new HashMap();
    static {
        SUBJECT_EVENT_STATUS_ICONS.put(core.org.akaza.openclinica.domain.datamap.SubjectEventStatus.INVALID, "icon icon-doc");
        SUBJECT_EVENT_STATUS_ICONS.put(core.org.akaza.openclinica.domain.datamap.SubjectEventStatus.SCHEDULED, "icon icon-clock2");
        SUBJECT_EVENT_STATUS_ICONS.put(core.org.akaza.openclinica.domain.datamap.SubjectEventStatus.NOT_SCHEDULED, "icon icon-clock");
        SUBJECT_EVENT_STATUS_ICONS.put(core.org.akaza.openclinica.domain.datamap.SubjectEventStatus.DATA_ENTRY_STARTED, "icon icon-pencil-squared orange");
        SUBJECT_EVENT_STATUS_ICONS.put(core.org.akaza.openclinica.domain.datamap.SubjectEventStatus.COMPLETED, "icon icon-checkbox-checked green");
        SUBJECT_EVENT_STATUS_ICONS.put(core.org.akaza.openclinica.domain.datamap.SubjectEventStatus.STOPPED, "icon icon-stop-circle red");
        SUBJECT_EVENT_STATUS_ICONS.put(core.org.akaza.openclinica.domain.datamap.SubjectEventStatus.SKIPPED, "icon icon-redo");
        SUBJECT_EVENT_STATUS_ICONS.put(core.org.akaza.openclinica.domain.datamap.SubjectEventStatus.LOCKED, "icon icon-lock");
        SUBJECT_EVENT_STATUS_ICONS.put(core.org.akaza.openclinica.domain.datamap.SubjectEventStatus.SIGNED, "icon con-icon-sign green");

        STUDY_EVENT_WORKFLOW_ICONS.put(StudyEventWorkflowStatusEnum.SCHEDULED, "icon icon-clock2");
        STUDY_EVENT_WORKFLOW_ICONS.put(StudyEventWorkflowStatusEnum.NOT_SCHEDULED, "icon icon-clock");
        STUDY_EVENT_WORKFLOW_ICONS.put(StudyEventWorkflowStatusEnum.DATA_ENTRY_STARTED,  "icon icon-pencil-squared orange");
        STUDY_EVENT_WORKFLOW_ICONS.put(StudyEventWorkflowStatusEnum.COMPLETED, "icon icon-checkbox-checked green");
        STUDY_EVENT_WORKFLOW_ICONS.put(StudyEventWorkflowStatusEnum.STOPPED, "icon icon-stop-circle red");
        STUDY_EVENT_WORKFLOW_ICONS.put(StudyEventWorkflowStatusEnum.SKIPPED, "icon icon-redo");
        /*******************
          STUDY_EVENT_WORKFLOW_ICONS.put(StudyEventWorkflowEnum.LOCKED, "icon icon-lock");
          */
        STUDY_EVENT_WORKFLOW_ICONS.put(StudyEventWorkflowStatusEnum.SIGNED, "icon con-icon-sign green");



        CRF_STATUS_ICONS.put(0, "icon icon-file-excel red");
        CRF_STATUS_ICONS.put(1, "icon icon-doc");
        CRF_STATUS_ICONS.put(2, "icon icon-pencil-squared orange");
        CRF_STATUS_ICONS.put(3, "icon icon-icon-dataEntryCompleted orange");
        CRF_STATUS_ICONS.put(4, "icon icon-icon-doubleDataEntry orange");
        CRF_STATUS_ICONS.put(5, "icon icon-search");
        CRF_STATUS_ICONS.put(6, "icon icon-pencil-squared orange");
        CRF_STATUS_ICONS.put(7, "icon icon-lock");
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

    public void setDataAndLimitVariables(TableFacade tableFacade, int studyId, HttpServletRequest request, String[] permissionTags) {
        // https://jira.openclinica.com/browse/OC-9952
        tableFacade.setMaxRows(50);
        Limit limit = tableFacade.getLimit();

        EventCRFSDVFilter eventCRFSDVFilter = getEventCRFSDVFilter(limit, studyId);
        WebContext context = tableFacade.getWebContext();

        String restore = request.getAttribute(limit.getId() + "_restore") + "";
        if (!limit.isComplete()) {
            int totalRows = getTotalRowCount(eventCRFSDVFilter, studyId, permissionTags);
            tableFacade.setTotalRows(totalRows);
        } else if (restore != null && "true".equalsIgnoreCase(restore)) {
            int totalRows = getTotalRowCount(eventCRFSDVFilter, studyId, permissionTags);
            int pageNum = limit.getRowSelect().getPage();
            // https://jira.openclinica.com/browse/OC-9952
            // int maxRows = limit.getRowSelect().getMaxRows();
            tableFacade.setTotalRows(totalRows);
            limit.getRowSelect().setPage(pageNum);
        }

        EventCRFSDVSort eventCRFSDVSort = getEventCRFSDVSort(limit);

        int rowStart = limit.getRowSelect().getRowStart();
        int rowEnd = limit.getRowSelect().getRowEnd();
        // Collection<StudySubjectBean> items = getStudySubjectDAO().getWithFilterAndSort(getStudyBean(),
        // studySubjectSDVFilter, subjectSort, rowStart, rowEnd);
        Collection<SubjectSDVContainer> items = getFilteredItems(eventCRFSDVFilter, eventCRFSDVSort, rowStart, rowEnd, studyId, request, permissionTags);
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

    public int getTotalRowCount(EventCRFSDVFilter eventCRFSDVFilter, Integer studyId, String[] permissionTags) {

        EventCRFDAO eventCRFDAO = new EventCRFDAO(dataSource);
        return eventCRFDAO.getCountWithFilter(studyId, studyId, eventCRFSDVFilter, permissionTags);

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
                                                             HttpServletRequest request, String[] permissionTags) {

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
        eventCRFBeans = eventCRFDAO.getWithFilterAndSort(studyId, studyId, filterSet, sortSet, rowStart, rowEnd, permissionTags);
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
     * "core.org.akaza.openclinica.i18n.words",LocaleResolver.getLocale(request));
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

    public String renderEventCRFTableWithLimit(HttpServletRequest request, int studyId, String pathPrefix, String[] permissionTags) {

        // boolean showMoreLink = Boolean.parseBoolean(request.getAttribute("showMoreLink").toString());//commented by
        // Jamuna, throwing null pointer exception
        boolean showMoreLink = Boolean.parseBoolean(request.getAttribute("showMoreLink") == null ? "false" : request.getAttribute("showMoreLink").toString());
        TableFacade tableFacade = createTableFacade("sdv", request);
        tableFacade.setStateAttr("sdv_restore");
        resformat = ResourceBundleProvider.getFormatBundle(LocaleResolver.getLocale(request));
        this.pathPrefix = pathPrefix;

        String[] allColumns = new String[]{"sdvStatus", "studySubjectId", "studyIdentifier", "openQueries", "eventName", "eventDate",
                "studySubjectStatus", "crfName", "crfVersion", "sdvRequirementDefinition", "crfStatus", "lastUpdatedDate", "lastUpdatedBy",
                "subjectEventStatus", "sdvStatusActions"};

        tableFacade.setColumnProperties("sdvStatus", "studySubjectId", "studyIdentifier", "openQueries", "eventName", "eventDate",
                "studySubjectStatus", "crfName", "crfVersion", "sdvRequirementDefinition", "crfStatus", "lastUpdatedDate", "lastUpdatedBy", "subjectEventStatus",
                "sdvStatusActions");
        tableFacade.addFilterMatcher(new MatcherKey(String.class, "studySubjectStatus"), new SubjectStatusMatcher());

        tableFacade.addFilterMatcher(new MatcherKey(String.class, "crfStatus"), new CrfStatusMatcher());

        tableFacade.addFilterMatcher(new MatcherKey(String.class, "sdvStatus"), new SdvStatusMatcher());

        tableFacade.addFilterMatcher(new MatcherKey(String.class, "sdvRequirementDefinition"), new SDVRequirementMatcher());

        tableFacade.addFilterMatcher(new MatcherKey(String.class, "openQueries"), new OpenQueriesMatcher());

        tableFacade.addFilterMatcher(new MatcherKey(String.class, "crfName"), new OpenQueriesMatcher());

        this.setDataAndLimitVariables(tableFacade, studyId, request, permissionTags);

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

        HtmlColumn subjectEventStatus = row.getColumn("subjectEventStatus");
        subjectEventStatus.getFilterRenderer().setFilterEditor(new SubjectEventStatusFilter());

        HtmlColumn openQueries = row.getColumn("openQueries");
        openQueries.getFilterRenderer().setFilterEditor(new OpenQueriesFilter());

        HtmlColumn eventDate = row.getColumn("eventDate");
        eventDate.setSortable(true);

        HtmlColumn lastUpdatedDate = row.getColumn("lastUpdatedDate");
        lastUpdatedDate.setSortable(true);

        // fix HTML in columns
        setHtmlCellEditors(tableFacade, allColumns, true);

        // temporarily disable some of the filters for now
        turnOffFilters(tableFacade, new String[]{"studySubjectStatus", "crfVersion", "lastUpdatedDate",
                "lastUpdatedBy", "eventDate"});

        turnOffSorts(tableFacade,
                new String[]{"sdvStatus", "studySubjectId", "studyIdentifier", "openQueries", "eventName",
                        "studySubjectStatus", "crfVersion", "sdvRequirementDefinition", "crfStatus", "lastUpdatedBy", "subjectEventStatus",
                        "sdvStatusActions"});

        // Create the custom toolbar
        SDVToolbar sDVToolbar = new SDVToolbar(showMoreLink);

        // if(totalRowCount > 0){
        sDVToolbar.setMaxRowsIncrements(new int[]{15, 25, 50, 100});
        tableFacade.setToolbar(sDVToolbar);
        tableFacade.setView(new SDVView(LocaleResolver.getLocale(request), request));

        // Fix column titles
        HtmlTable table = (HtmlTable) tableFacade.getTable();
        // i18n caption; TODO: convert to Spring messages
        ResourceBundle resword = ResourceBundle.getBundle("org.akaza.openclinica.i18n.words", LocaleResolver.getLocale(request));

        String[] allTitles = {resword.getString("SDV_status"), resword.getString("study_subject_ID"), resword.getString("site_id"),
                resword.getString("open_queries"), resword.getString("event_name"), resword.getString("event_date"),
                resword.getString("subject_status"), resword.getString("CRF_name"), resword.getString("CRF_version"),
                resword.getString("SDV_requirement"), resword.getString("CRF_status"), resword.getString("last_updated_date"),
                resword.getString("last_updated_by"), resword.getString("subject_event_status"), resword.getString("actions")};

        setTitles(allTitles, table);

        // format column dates
        formatColumns(table, new String[]{"eventDate", "lastUpdatedDate"}, request);
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

        String[] allColumns = new String[]{"studySubjectId", "studyIdentifier", "eventName", "eventDate",
                "studySubjectStatus", "crfNameVersion", "sdvRequirementDefinition", "crfStatus", "lastUpdatedDate", "lastUpdatedBy", "studyEventStatus",
                "sdvStatusActions"};

        tableFacade.setColumnProperties("studySubjectId", "studyIdentifier", "eventName", "eventDate",
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

        turnOffFilters(tableFacade,
                new String[]{"studySubjectStatus", "crfNameVersion", "lastUpdatedDate", "lastUpdatedBy"});

        // Create the custom toolbar
        SDVToolbar sDVToolbar = new SDVToolbar(true);

        // if(totalRowCount > 0){
        sDVToolbar.setMaxRowsIncrements(new int[]{15, 50, totalRowCount});
        tableFacade.setToolbar(sDVToolbar);

        // Fix column titles
        HtmlTable table = (HtmlTable) tableFacade.getTable();
        // i18n caption; TODO: convert to Spring messages
        ResourceBundle resword = ResourceBundle.getBundle("org.akaza.openclinica.i18n.words", LocaleResolver.getLocale(request));

        String[] allTitles = {resword.getString("study_subject_ID"), resword.getString("site_id"),
                resword.getString("event_name"), resword.getString("event_date"),
                resword.getString("subject_status"), resword.getString("CRF_name") + " / " + resword.getString("version"), resword.getString("SDV_requirement"),
                resword.getString("CRF_status"), resword.getString("last_updated_date"), resword.getString("last_updated_by"),
                resword.getString("study_event_status"), resword.getString("SDV_status") + " / " + resword.getString("actions")};

        setTitles(allTitles, table);

        // format column dates
        formatColumns(table, new String[]{"eventDate", "lastUpdatedDate"}, request);

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
        String format = bundle.getString("date_format_string");
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
        StudyEventDAO studyEventDAO = new StudyEventDAO(dataSource);

        EventDefinitionCRFDAO eventDefinitionCRFDAO = new EventDefinitionCRFDAO(dataSource);
        ResourceBundle resWords = ResourceBundleProvider.getWordsBundle();
        StudySubjectBean studySubjectBean = null;
        SubjectBean subjectBean = null;
        StudyEventBean studyEventBean = null;
        Study studyBean = null;
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

        for (EventCRFBean eventCRFBean : eventCRFBeans) {
            EventCrf eventCrf = eventCrfDao.findByPK(eventCRFBean.getId());
            tempSDVBean = new SubjectSDVContainer();

            studySubjectBean = (StudySubjectBean) studySubjectDAO.findByPK(eventCRFBean.getStudySubjectId());
            studyEventBean = (StudyEventBean) studyEventDAO.findByPK(eventCRFBean.getStudyEventId());

            subjectBean = (SubjectBean) subjectDAO.findByPK(studySubjectBean.getSubjectId());
            // find out the study's identifier
            studyBean = (Study) studyDao.findByPK(studySubjectBean.getStudyId());
            tempSDVBean.setStudyIdentifier(studyBean.getUniqueIdentifier());

            eventDefinitionCRFBean = eventDefinitionCRFDAO.findByStudyEventIdAndCRFVersionId(studyBean, studyEventBean.getId(), eventCRFBean.getCRFVersionId());
            SourceDataVerification sourceData = eventDefinitionCRFBean.getSourceDataVerification();
            if (sourceData != null) {
                tempSDVBean.setSdvRequirementDefinition(sourceData.toString());
            } else {
                tempSDVBean.setSdvRequirementDefinition("");
            }

            int openQueriesCount = discrepancyNoteDao.findNewOrUpdatedParentQueriesByEventCrfId(eventCrf.getEventCrfId()).size();

            if (openQueriesCount > 0) {
                String queriesPageUrl = request.getContextPath() + "/ViewNotes?module=submit&listNotes_f_discrepancyNoteBean.disType=Query&listNotes_f_discrepancyNoteBean.resolutionStatus=New+and+Updated&"
                        + "listNotes_f_crfName=" + eventCrf.getCrfVersion().getCrf().getName()
                        + "&listNotes_f_studySubject.label=" + eventCrf.getStudySubject().getLabel() + "&listNotes_f_eventName=" + eventCrf.getStudyEvent().getStudyEventDefinition().getName();
                tempSDVBean.setOpenQueries("<center><a href='" + queriesPageUrl + "'>" + openQueriesCount + "</a></center>");
            } else
                tempSDVBean.setOpenQueries("<center>" + openQueriesCount + "</center>");
            tempSDVBean.setCrfName(getCRFName(eventCRFBean.getCRFVersionId()));
            tempSDVBean.setCrfVersion(getFormLayoutName(eventCRFBean.getFormLayoutId()));
            if (eventCRFBean.getStatus() != null) {
                Integer status = eventCRFBean.getStage().getId();

                StringBuilder crfStatusBuilder = new StringBuilder(new HtmlBuilder().toString());
                String input = "<input type=\"hidden\" statusId=\"" + status + "\" />";
                // "<input type=\"hidden\" statusId=\"1\" />"
//                ResourceBundle resWords = ResourceBundleProvider.getWordsBundle();
                String statusTitle = "";
                String statusIconClassName = "";
                if (DataEntryStage.get(status).equals(DataEntryStage.LOCKED)) {
                    statusTitle = DataEntryStage.LOCKED.getName();
                    statusIconClassName = FORM_LOCKED_ICON_CLASS_NAME;
                } else {
                    statusTitle = resWords.getString("completed");
                    statusIconClassName = FORM_COMPLETED_ICON_CLASS_NAME;
                }
                crfStatusBuilder.append("<center><a title='" + statusTitle + "' alt='" + statusTitle + "' class='" + statusIconClassName + "' accessCheck' border='0'/></center>");
                tempSDVBean.setCrfStatus(crfStatusBuilder.toString());
            }
            tempSDVBean.setSubjectEventStatus("<center><a title='"+eventCrf.getStudyEvent().getWorkflowStatus()+"' alt='"+eventCrf.getStudyEvent().getWorkflowStatus()+"' class='"+STUDY_EVENT_WORKFLOW_ICONS.get(eventCrf.getStudyEvent().getWorkflowStatus())+"' accessCheck' border='0'/></center>");

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
            StudyEventBean seBean = (StudyEventBean) sedao.findByPK(eventCRFBean.getStudyEventId());
            if (seBean.getDateStarted() != null)
                tempSDVBean.setEventDate(seBean.getDateStarted());

            // if (eventCRFBean.getCreatedDate() != null) {
            // tempSDVBean.setEventDate(sdformat.format(eventCRFBean.getCreatedDate()));
            // } else {
            // //tempSDVBean.setEventDate("unknown");
            //
            // }
            // eventCRFBean.getEventName()
            if (eventCrf.getStudyEvent().getStudyEventDefinition().getRepeating())
                tempSDVBean.setEventName(eventCrf.getStudyEvent().getStudyEventDefinition().getName() + " (" + eventCrf.getStudyEvent().getSampleOrdinal() + ")");
            else
                tempSDVBean.setEventName(eventCrf.getStudyEvent().getStudyEventDefinition().getName());
            // The checkbox is next to the study subject id
            StringBuilder sdvStatus = new StringBuilder("");
            // .getNexGenStatus().getCode() == 10
            // "This Event CRF has been Source Data Verified. If you uncheck this box, you are removing Source Data
            // Verification for the Event CRF and you will have to repeat the process. Select OK to continue and Cancel
            // to cancel this transaction."
            String eventCRFId = Integer.toString(eventCRFBean.getId());
            String formLayoutId = Integer.toString(eventCRFBean.getFormLayoutId());
            String studyEventId = Integer.toString(eventCRFBean.getStudyEventId());
            if (eventCRFBean.getSdvStatus() == SdvStatus.VERIFIED) {
                sdvStatus.append("<center><a  class='accessCheck' href='javascript:void(0)' onclick='prompt(document.sdvForm,");
                sdvStatus.append(eventCRFBean.getId());
                sdvStatus.append(")'");
                sdvStatus.append(" data-eventCrfId='").append(eventCRFId).append("'");
                sdvStatus.append(" data-formLayoutId='").append(formLayoutId).append("'");
                sdvStatus.append(" data-studyEventId='").append(studyEventId).append("'");
                sdvStatus.append(">");
                sdvStatus.append("<span hspace='2' border='0'  title='" + resWords.getString(SdvStatus.VERIFIED.toString()) + "' alt='SDV Complete' class='icon icon-icon-SDV-doubleCheck'>").append("</a></center>");
            } else if (eventCRFBean.getSdvStatus() == SdvStatus.CHANGED_AFTER_VERIFIED) {
                sdvStatus.append("<center><span title='" + resWords.getString(SdvStatus.CHANGED_AFTER_VERIFIED.toString()) + "' class='icon-icon-sdv-change-status small-icon' border='0'></span><input style='margin-right: 1.5em' type='checkbox' ")
                        .append("class='sdvCheck'").append(" name='").append(CHECKBOX_NAME)
                        .append(eventCRFBean.getId()).append("' /></center>");
            } else {
                sdvStatus.append("<center><input style='margin-right: .4em' type='checkbox' ").append("class='sdvCheck'").append(" name='").append(CHECKBOX_NAME)
                        .append(eventCRFBean.getId()).append("' /></center>");

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
            if (eventCRFBean.getUpdatedDate() != null) {
                tempSDVBean.setLastUpdatedDate(eventCRFBean.getUpdatedDate());
            } else {
                tempSDVBean.setLastUpdatedDate(null);

            }

            if (eventCRFBean.getUpdater() != null) {

                tempSDVBean.setLastUpdatedBy(eventCRFBean.getUpdater().getFirstName() + " " + eventCRFBean.getUpdater().getLastName());

            }

            String queryString = request.getQueryString();
            if (queryString == null) {
                queryString = "";
            }
            StringBuilder actionsBuilder = new StringBuilder(new HtmlBuilder().toString());
            if (eventCRFBean.getStatus() != null){
                String queryStringEncoded = queryString;
                try {
                    queryStringEncoded = URLEncoder.encode(queryString, StandardCharsets.UTF_8.toString());
                } catch (UnsupportedEncodingException e) {
                    logger.error("Unsupported encoding");
                }
                Integer status = eventCRFBean.getStage().getId();
                actionsBuilder.append(getCRFViewIconPath(status, request, eventCRFBean.getId(), eventCRFBean.getFormLayoutId(),
                        eventCRFBean.getStudyEventId(), queryStringEncoded));
            }

            StudyEvent event = eventCrf.getStudyEvent();
            StudyEventDefinition eventDef = event.getStudyEventDefinition();
            actionsBuilder
                .append("<button style='padding:.4em 0.9em' class='accessCheck popupSdv' title='" + resWords.getString("view_sdv_item_data_hover") + "'")
                .append(" data-participant-id='").append(studySubjectBean.getLabel()).append("'")
                .append(" data-study-oid='").append(eventDef.getStudy().getOc_oid()).append("'")
                .append(" data-event-oid='").append(eventDef.getOc_oid()).append("'")
                .append(" data-event-ordinal='").append(event.getSampleOrdinal() > 0 ? event.getSampleOrdinal() : 1).append("'")
                .append(" data-form-oid='").append(eventCrf.getFormLayout().getCrf().getOcOid()).append("'")
                .append(" data-sdv-status='").append(eventCRFBean.getSdvStatus()).append("'")
                .append(">" + resWords.getString("sdv_item_data") + "</button>");
        
            if (eventCRFBean.getSdvStatus() != SdvStatus.VERIFIED) {
                // StringBuilder jsCodeString =
                // new StringBuilder("this.form.method='GET';
                // this.form.action='").append(request.getContextPath()).append("/pages/handleSDVGet").append("';")
                // .append("this.form.crfId.value='").append(eventCRFBean.getId()).append("';").append("this.form.submit();");
                // actions.append("<input type=\"submit\" class=\"button_medium\" value=\"Mark as SDV'd\"
                // name=\"sdvSubmit\" ").append("onclick=\"").append(
                // jsCodeString.toString()).append("\" />");
                actionsBuilder.append("<input type='button' name='sdvVerify' style='margin-left: 0.3em; padding:.4em 0.9em' value='"+resWords.getString("sdv_verify")+"' onclick='submitSdv(document.sdvForm,").append(eventCRFBean.getId()).append(")'")
                        .append(" data-eventCrfId='").append(eventCRFId).append("'")
                        .append(" data-formLayoutId='").append(formLayoutId).append("'")
                        .append(" data-studyEventId='").append(studyEventId).append("'")
                        .append("/>");

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
            tempSDVBean.setSdvStatusActions(actionsBuilder.toString());
            allRows.add(tempSDVBean);

        }

        return allRows;
    }

    private String getCRFViewIconPath(int statusId, HttpServletRequest request, int eventDefinitionCRFId,
                                      int formLayoutId, int studyEventId, String redirect) {

        HtmlBuilder html = new HtmlBuilder();
        // html.a().onclick(
        // "openDocWindow('" + request.getContextPath() + "/ViewSectionDataEntry?eventDefinitionCRFId=&ecId=" +
        // eventDefinitionCRFId
        // + "&tabId=1&studySubjectId=" + studySubjectId + "');");
        // html.href('#').close();

        StringBuilder builder = new StringBuilder();

        String imgName = "";
        StringBuilder input = new StringBuilder("<input type=\"hidden\" statusId=\"");
        input.append(statusId).append("\" />");
        String href = request.getContextPath() + "/EnketoFormServlet?formLayoutId=" + formLayoutId + "&studyEventId=" + studyEventId + "&eventCrfId="
                + eventDefinitionCRFId + "&originatingPage=pages/viewAllSubjectSDVtmp?" + redirect + "&mode=view";
        builder.append(
                "<a title='View CRF' alt='View CRF' class='icon icon-search' accessCheck' border='0' href='" + href + "' ></a>");
        // "<input type=\"hidden\" statusId=\"1\" />"
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

    public String getFormLayoutName(int formLayoutId) {

        FormLayoutDAO formLayoutDAO = new FormLayoutDAO(dataSource);
        FormLayoutBean formLayout = (FormLayoutBean) formLayoutDAO.findByPK(formLayoutId);
        if (formLayout != null) {
            return formLayout.getName();
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

        String[] allTitles = {resword.getString("study_subject_ID"), resword.getString("study_subject_status"), resword.getString("num_CRFs_SDV"),
                resword.getString("porc_CRFs_SDV"), resword.getString("group")};

        setTitles(allTitles, table);

        table.getTableRenderer().setWidth("800");
        return tableFacade.render();

    }

    public boolean setSDVStatusForStudySubjects(List<Integer> studySubjectIds, int userId, SdvStatus sdvStatus) {

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
                        eventCrfDao.updateSdvStatus(sdvStatus, userId, eventCRFBean.getId());
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

    public boolean setSDVerified(List<Integer> eventCRFIds, int userId, SdvStatus sdvStatus) {

        // If no event CRFs are offered to SDV, then the transaction has not
        // caused a problem, so return true
        if (eventCRFIds == null || eventCRFIds.isEmpty()) {
            return true;
        }

        EventCRFDAO eventCRFDAO = new EventCRFDAO(dataSource);

        for (Integer eventCrfId : eventCRFIds) {
            try {
                eventCrfDao.updateSdvStatus(sdvStatus, userId, eventCrfId);
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
            logger.error("Error while forwarding to other location: ", e);
        } catch (IOException e) {
            logger.error("Error while forwarding to other location: ", e);
        }
    }

    public void prepareSDVSelectElements(HttpServletRequest request, Study studyBean) {
        // Study event statuses
        List<String> studyEventStatuses = new ArrayList<String>();
        for (core.org.akaza.openclinica.domain.Status stat : core.org.akaza.openclinica.domain.Status.values()) {
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
        request.setAttribute("studyEventStatuses", StudyEventWorkflowStatusEnum.values());

        // event CRF statuseventCRFStatuses
        request.setAttribute("eventCRFDStatuses", EventCrfWorkflowStatusEnum.values());

        StudyEventDAO studyEventDAO = new StudyEventDAO(dataSource);

        List<StudyEventBean> studyEventBeans = studyEventDAO.findAllByStudy(studyBean);
        List<EventCRFBean> eventCRFBeans = getAllEventCRFs(studyEventBeans);
        SortedSet<String> eventCRFNames = new TreeSet<String>();

        for (EventCRFBean bean : eventCRFBeans) {
            eventCRFNames.add(getCRFName(bean.getCRFVersionId()));
        }
        request.setAttribute("eventCRFNames", eventCRFNames);

    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public SdvDTO getFormDetailsForSDV(String studyOID, String formOID, String studyEventOID, String studySubjectLabel, int ordinal, boolean changedAfterSdvOnlyFilter) {

        EventCrf eventCrf = getEventCrfDao().findByStudyEventOIdStudySubjectOIdCrfOId(studyEventOID, studySubjectLabel, formOID, ordinal);
        if (eventCrf != null && !eventCrf.getWorkflowStatus().equals(EventCrfWorkflowStatusEnum.COMPLETED))
                throw new OpenClinicaSystemException(ErrorConstants.ERR_EVENT_CRF_NOT_COMPLETED);
        else if (eventCrf != null) {
            SdvDTO sdvDTO = new SdvDTO();
            sdvDTO.setParticipantId(eventCrf.getStudySubject().getLabel());
            sdvDTO.setSiteName(eventCrf.getStudySubject().getStudy().getUniqueIdentifier());
            sdvDTO.setEventName(eventCrf.getStudyEvent().getStudyEventDefinition().getName());
            sdvDTO.setEventStartDate(eventCrf.getStudyEvent().getDateStart());
            sdvDTO.setEventOrdinal(eventCrf.getStudyEvent().getSampleOrdinal());
            sdvDTO.setRepeatingEvent(eventCrf.getStudyEvent().getStudyEventDefinition().getRepeating());
            Study parentStudy = studyDao.findByOcOID(studyOID);
            parentStudy = parentStudy.isSite() ? parentStudy.getStudy() : parentStudy;
            EventDefinitionCrf eventDefinitionCrf = getEventDefinitionCrfDao().findByStudyEventDefinitionIdAndCRFIdAndStudyId(eventCrf.getStudyEvent().getStudyEventDefinition().getStudyEventDefinitionId(), eventCrf.getCrfVersion().getCrf().getCrfId(), parentStudy.getStudyId());
            sdvDTO.setSdvRequirement(SourceDataVerification.getByCode(eventDefinitionCrf.getSourceDataVerificationCode()).getDescription());
            sdvDTO.setFormName(eventCrf.getFormLayout().getCrf().getName());
            if(eventCrf.getStudyEvent().getLocked()!=null && eventCrf.getStudyEvent().getLocked()) {
                sdvDTO.setFormStatus("locked");
            } else
                sdvDTO.setFormStatus("completed"); //EventCrf Status is checked to be UNAVAVAILABLE (i.e. COMPLETED) at parent If Itself
            sdvDTO.setLastVerifiedDate(eventCrf.getLastSdvVerifiedDate());
            sdvDTO.setSdvStatus(eventCrf.getSdvStatus().toString());
            List<SdvItemDTO> sdvItemDTOS = new ArrayList<>();
            for (ItemData itemData : getItemDataDao().findByEventCrfId(eventCrf.getEventCrfId())) {

                if (!changedAfterSdvOnlyFilter || getItemSdvStatus(eventCrf, itemData).equals(SdvStatus.CHANGED_AFTER_VERIFIED)) {
                    SdvItemDTO sdvItemDTO = new SdvItemDTO();
                    sdvItemDTO.setItemDataId(itemData.getItemDataId());
                    sdvItemDTO.setName(itemData.getItem().getName());
                    sdvItemDTO.setBriefDescription(itemData.getItem().getBriefDescription());
                    sdvItemDTO.setOpenQueriesCount(discrepancyNoteDao.findNewOrUpdatedParentQueriesByItemData(itemData.getItemDataId(), 3).size());
                    sdvItemDTO.setOrdinal(itemData.getOrdinal());
                    ItemGroupMetadata itemGroupMetadata = itemGroupMetadataDao.findByItemId(itemData.getItem().getItemId());
                    if (itemGroupMetadata != null)
                        sdvItemDTO.setRepeatingGroup(itemGroupMetadata.isRepeatingGroup());
                    sdvItemDTO.setValue(itemData.getValue());
                    if (itemData.getDateUpdated() != null)
                        sdvItemDTO.setLastModifiedDate(itemData.getDateUpdated());
                    else
                        sdvItemDTO.setLastModifiedDate(itemData.getDateCreated());
                    sdvItemDTO.setLastModifiedDateHasOnlyDate(isOnlyDateAvailableInItemTable(itemData));
                    int updateUserId = itemData.getUpdateId() != null ? itemData.getUpdateId() : itemData.getUserAccount().getUserId();
                    UserAccount itemUpdatedUserAccount = userAccountDao.findByUserId(updateUserId);
                    sdvItemDTO.setLastModifiedUserName(itemUpdatedUserAccount.getUserName());
                    sdvItemDTO.setLastModifiedUserFirstName(itemUpdatedUserAccount.getFirstName());
                    sdvItemDTO.setLastModifiedUserLastName(itemUpdatedUserAccount.getLastName());
                    sdvItemDTO.setSdvStatus(getItemSdvStatus(eventCrf, itemData).toString());
                    sdvItemDTOS.add(sdvItemDTO);
                }
            }
            Collections.sort(sdvItemDTOS, new Comparator<SdvItemDTO>() {
                @Override
                public int compare(SdvItemDTO o1, SdvItemDTO o2) {
                    return o1.getSdvStatus().compareTo(o2.getSdvStatus());
                }
            }.thenComparing(new Comparator<SdvItemDTO>() {
                @Override
                public int compare(SdvItemDTO o1, SdvItemDTO o2) {
                    return (-1) * (o1.getLastModifiedDate().compareTo(o2.getLastModifiedDate()));
                }
            }));
            sdvDTO.setSdvItems(sdvItemDTOS);
            return sdvDTO;
        } else
            throw new OpenClinicaSystemException(ErrorConstants.ERR_STUDYSUBJECT_STUDYEVENT_STUDYFORM_NOT_RELATED);
    }

    private SdvStatus getItemSdvStatus(EventCrf eventCrf, ItemData itemData) {
        if (eventCrf.getSdvStatus().equals(SdvStatus.CHANGED_AFTER_VERIFIED)) {

            Date lastUpdatedItemDate = eventCrf.getLastSdvVerifiedDate();
            if (isOnlyDateAvailableInItemTable(itemData)) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(lastUpdatedItemDate);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                lastUpdatedItemDate = cal.getTime();
            }

            if (lastUpdatedItemDate.compareTo(itemData.getDateCreated()) < 0)
                return SdvStatus.CHANGED_AFTER_VERIFIED;
            else {
                if (itemData.getDateUpdated() != null && lastUpdatedItemDate.compareTo(itemData.getDateUpdated()) < 0)
                    return SdvStatus.CHANGED_AFTER_VERIFIED;
                else
                    return SdvStatus.VERIFIED;
            }

        } else
            return eventCrf.getSdvStatus();
    }

    //Previously Item table had only date, This function is used inOrder to compare dateUpdated or dateCreated with lastSdvDate correctly
    public boolean isOnlyDateAvailableInItemTable(ItemData itemData) {
        Date dateForCheck = itemData.getDateUpdated() != null ? itemData.getDateUpdated() : itemData.getDateCreated();
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateForCheck);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date dateWithMidnightTime = cal.getTime();
        if (dateForCheck.getTime() == dateWithMidnightTime.getTime())
            return true;
        else
            return false;
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
            html.append("<a style='text-decoration:none;' name='checkSDVAll' href='javascript:selectAllChecks(document.sdvForm,true)'>"
                    + resword.getString("table_sdv_all"));
            html.append("</a>");
            html.append("&#160;&#160;&#160;");
            html.append("<a style='text-decoration:none;' name='checkSDVAll' href='javascript:selectAllChecks(document.sdvForm,false)'>"
                    + resword.getString("table_sdv_none"));
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

    public StudyDao getStudyDao() {
        return studyDao;
    }

    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }


    public EventCrfDao getEventCrfDao() {
        return eventCrfDao;
    }

    public void setEventCrfDao(EventCrfDao eventCrfDao) {
        this.eventCrfDao = eventCrfDao;
    }

    public ItemDao getItemDao() {
        return itemDao;
    }

    public void setItemDao(ItemDao itemDao) {
        this.itemDao = itemDao;
    }


    public EventDefinitionCrfDao getEventDefinitionCrfDao() {
        return eventDefinitionCrfDao;
    }

    public void setEventDefinitionCrfDao(EventDefinitionCrfDao eventDefinitionCrfDao) {
        this.eventDefinitionCrfDao = eventDefinitionCrfDao;
    }

    public ItemDataDao getItemDataDao() {
        return itemDataDao;
    }

    public void setItemDataDao(ItemDataDao itemDataDao) {
        this.itemDataDao = itemDataDao;
    }

    public UserAccountDao getUserAccountDao() {
        return userAccountDao;
    }

    public void setUserAccountDao(UserAccountDao userAccountDao) {
        this.userAccountDao = userAccountDao;
    }
}
