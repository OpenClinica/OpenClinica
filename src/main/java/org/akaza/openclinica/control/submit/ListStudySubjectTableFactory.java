package org.akaza.openclinica.control.submit;

import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.bean.core.Status;
import core.org.akaza.openclinica.bean.login.StudyUserRoleBean;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.*;
import core.org.akaza.openclinica.bean.submit.*;
import core.org.akaza.openclinica.domain.EventCrfStatusEnum;
import core.org.akaza.openclinica.domain.datamap.ResponseType;
import core.org.akaza.openclinica.service.managestudy.StudySubjectService;
import org.akaza.openclinica.control.AbstractTableFactory;
import org.akaza.openclinica.control.DefaultActionsEditor;
import org.akaza.openclinica.control.ListStudyView;
import core.org.akaza.openclinica.dao.hibernate.*;
import core.org.akaza.openclinica.dao.managestudy.*;
import core.org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import core.org.akaza.openclinica.dao.submit.*;
import core.org.akaza.openclinica.domain.datamap.*;
import core.org.akaza.openclinica.i18n.core.LocaleResolver;
import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import core.org.akaza.openclinica.service.*;
import core.org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import org.akaza.openclinica.domain.enumsupport.EventCrfWorkflowStatusEnum;
import org.akaza.openclinica.domain.enumsupport.StudyEventWorkflowStatusEnum;
import org.akaza.openclinica.service.UserService;
import org.akaza.openclinica.service.ViewStudySubjectService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jmesa.core.filter.FilterMatcher;
import org.jmesa.core.filter.MatcherKey;
import org.jmesa.facade.TableFacade;
import org.jmesa.limit.*;
import org.jmesa.util.ItemUtils;
import org.jmesa.view.component.Row;
import org.jmesa.view.editor.BasicCellEditor;
import org.jmesa.view.editor.CellEditor;
import org.jmesa.view.html.HtmlBuilder;
import org.jmesa.view.html.editor.DroplistFilterEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.*;

public class ListStudySubjectTableFactory extends AbstractTableFactory {

    private StudyEventDefinitionDAO studyEventDefinitionDao;
    private StudySubjectDAO studySubjectDAO;
    private SubjectDAO subjectDAO;
    private StudyEventDAO studyEventDAO;
    private StudyGroupClassDAO studyGroupClassDAO;
    private SubjectGroupMapDAO subjectGroupMapDAO;
    private StudyGroupDAO studyGroupDAO;
    private StudyDao studyDAO;
    private EventCRFDAO eventCRFDAO;
    private EventDefinitionCRFDAO eventDefintionCRFDAO;
    private HttpSession session;
    private Study studyBean;
    private String[] columnNames = new String[]{};
    private ArrayList<StudyEventDefinitionBean> studyEventDefinitions;
    private ArrayList<StudyGroupClassBean> studyGroupClasses;
    private StudyUserRoleBean currentRole;
    private UserAccountBean currentUser;
    private final boolean showMoreLink;
    private ResourceBundle resword;
    private ResourceBundle resformat;
    private final ResourceBundle resterms = ResourceBundleProvider.getTermsBundle();
    private StudyParameterValueDAO studyParameterValueDAO;
    private ParticipantPortalRegistrar participantPortalRegistrar;
    private final String COMMON = "common";
    private final String ENABLED = "enabled";
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private UserService userService;
    private HttpServletRequest request;
    private ViewStudySubjectService viewStudySubjectService;
    private PermissionService permissionService;
    public static final String PAGE_NAME = "participant-matrix";
    public static final String  COMPONENT_NAME="participant-matrix-table";
    public static final String DOT = ".";

    private static final String CHECKBOX = "checkbox";
    private static final String MULTI_SELECT = "multi-select";
    private static final String RADIO = "radio";
    private static final String SINGLE_SELECT = "single-select";


    private ItemDataDao itemDataDao;
    private ItemDao itemDao;
    private ItemFormMetadataDao itemFormMetadataDao;
    private ResponseSetDao responseSetDao;
    private EventCrfDao eventCrfDao;
    private StudySubjectDao studySubjectDao;
    private StudyEventDao studyEventDao;
    private CrfDao crfDao;
    private CrfVersionDao crfVersionDao;
    private EventDefinitionCrfDao eventDefinitionCrfDao;
    private EventDefinitionCrfPermissionTagDao permissionTagDao;
    private StudyEventDefinitionDao studyEventDefinitionHibDao;
    List<String> permissionTagsList = null;
    private final String  PARTICIPATE_STATUS="participate.status";
    private ResponseSet responseSet;
    final HashMap<String, String> imageIconPaths = new HashMap<String, String>(8);
    final HashMap<String, Integer> statusPriorities = new HashMap<String, Integer>(8);

    @Override
    // To avoid showing title in other pages, the request element is used to determine where the request came from.
    public TableFacade createTable(HttpServletRequest request, HttpServletResponse response) {
        locale = LocaleResolver.getLocale(request);
        session = request.getSession();
        this.request=request;
        TableFacade tableFacade = getTableFacadeImpl(request, response);
        tableFacade.setStateAttr("restore");
        // https://jira.openclinica.com/browse/OC-9952
        try {
            String maxrows = WebUtils.findParameterValue(request, "maxRows");
            Integer.parseInt(maxrows);
            Cookie cookie = new Cookie("maxrows", maxrows);
            cookie.setMaxAge(7 * 24 * 60 * 60);
            response.addCookie(cookie);
        }
        catch (Exception e) {
        }
        try {
            tableFacade.setMaxRows(Integer.parseInt(WebUtils.getCookie(request, "maxrows").getValue()));
        }
        catch (Exception e) {
            tableFacade.setMaxRows(50);
        }
        setDataAndLimitVariables(tableFacade);
        configureTableFacade(response, tableFacade);
        if (!tableFacade.getLimit().isExported()) {
            tableFacade.autoFilterAndSort(false);
            configureColumns(tableFacade, locale);
            tableFacade.setMaxRowsIncrements(getMaxRowIncrements());
            configureTableFacadePostColumnConfiguration(tableFacade);
            configureTableFacadeCustomView(tableFacade, request);
            configureUnexportedTable(tableFacade, locale);
        } else {
            configureExportColumns(tableFacade, locale);
        }
        return tableFacade;
    }

    public ListStudySubjectTableFactory(boolean showMoreLink) {
        this.showMoreLink = showMoreLink;
        imageIconPaths.put(StudyEventWorkflowStatusEnum.NOT_SCHEDULED.toString(), "icon icon-clock");
        imageIconPaths.put(StudyEventWorkflowStatusEnum.SCHEDULED.toString(), "icon icon-clock2");
        imageIconPaths.put(StudyEventWorkflowStatusEnum.DATA_ENTRY_STARTED.toString(), "icon icon-pencil-squared orange");
        imageIconPaths.put(StudyEventWorkflowStatusEnum.COMPLETED.toString(), "icon icon-checkbox-checked green");
        imageIconPaths.put(StudyEventWorkflowStatusEnum.STOPPED.toString(), "icon icon-stop-circle red");
        imageIconPaths.put(StudyEventWorkflowStatusEnum.SKIPPED.toString(), "icon icon-redo");
        imageIconPaths.put(EventCrfStatusEnum.REMOVED.toString(), "icon icon-file-excel red");

        statusPriorities.put(StudyEventWorkflowStatusEnum.NOT_SCHEDULED.toString(), 0);
        statusPriorities.put(StudyEventWorkflowStatusEnum.SCHEDULED.toString(), 1);
        statusPriorities.put(StudyEventWorkflowStatusEnum.DATA_ENTRY_STARTED.toString(), 2);
        statusPriorities.put(StudyEventWorkflowStatusEnum.COMPLETED.toString(), 3);
        statusPriorities.put(StudyEventWorkflowStatusEnum.STOPPED.toString(), 4);
        statusPriorities.put(StudyEventWorkflowStatusEnum.SKIPPED.toString(), 5);
        statusPriorities.put(EventCrfStatusEnum.REMOVED.toString(), 5);
    }

    @Override
    protected String getTableName() {
        return "findSubjects";
    }

    public void configureTableFacadeCustomView(TableFacade tableFacade, HttpServletRequest request) {
        tableFacade.setView(new ListStudyView(getLocale(), request));
    }

    @Override
    protected void configureColumns(TableFacade tableFacade, Locale locale) {
        resword = ResourceBundleProvider.getWordsBundle(locale);
        resformat = ResourceBundleProvider.getFormatBundle(locale);
        tableFacade.setColumnProperties(columnNames);
        Row row = tableFacade.getTable().getRow();
        int index = 0;
        configureColumn(row.getColumn(columnNames[index]), resword.getString("study_subject_ID"), new SubjectIdCellEditor(), null);
        ++index;

        configureColumn(row.getColumn(columnNames[index]), resword.getString("site_id"), null, null);
        ++index;
        EventDefinitionCrf eventDefCrf = null;


        String[] tableColumns = getViewStudySubjectService().getTableColumns(ListStudySubjectTableFactory.PAGE_NAME,ListStudySubjectTableFactory.COMPONENT_NAME);
        if (tableColumns != null) {
            for (String column : tableColumns) {
                if (permissionService.isUserHasPermission(column, request, studyBean)) {
                    String formOid = column.split("\\.")[1];
                    String itemOid = column.split("\\.")[2];
                    Item item = itemDao.findByOcOID(itemOid);
                    CrfBean crf = crfDao.findByOcOID(formOid);
                    ItemFormMetadata itemFormMetadata = itemFormMetadataDao.findByItemCrfVersion(item.getItemId(), crf.getCrfVersions().get(0).getCrfVersionId());
                    responseSet = itemFormMetadata.getResponseSet();
                    ResponseType responseType = responseSet.getResponseType();
                    if (item != null) {
                        if (responseType.getName().equals(CHECKBOX)
                                || responseType.getName().equals(MULTI_SELECT)
                                || responseType.getName().equals(RADIO)
                                || responseType.getName().equals(SINGLE_SELECT)) {
                            configureColumn(row.getColumn(columnNames[index]), item != null && item.getBriefDescription()!=null? item.getBriefDescription() :itemFormMetadata.getLeftItemText(), new ItemIdCellEditor(), new CustomColumnDroplistFilterEditor());
                            ++index;
                        } else {
                            configureColumn(row.getColumn(columnNames[index]), item != null && item.getBriefDescription()!=null? item.getBriefDescription() :itemFormMetadata.getLeftItemText() , new ItemIdCellEditor(), null);
                            ++index;
                        }
                    }
                }
            }
        }




        configureColumn(row.getColumn(columnNames[index]), resword.getString("status"), new StatusCellEditor(), new StatusDroplistFilterEditor());
        ++index;
        configureColumn(row.getColumn(columnNames[index]), resword.getString("rule_oid"), null, null);
        ++index;
        if (getParticipateModuleStatus().equals(ENABLED)) {
            configureColumn(row.getColumn(columnNames[index]), resword.getString("participate_status"), null, new ParticipateStatusDroplistFilterEditor());
            ++index;
        }
        // study event definition columns
        this.studyEventDefinitions = getStudyEventDefinitions();
        for (int i = index; i < columnNames.length - 1; i++) {
            StudyEventDefinitionBean studyEventDefinition = this.studyEventDefinitions.get(i - index);
            configureColumn(row.getColumn(columnNames[i]), studyEventDefinition.getName(), new StudyEventDefinitionMapCellEditor(),
                    new StudyEventWorkflowStatusDroplistFilterEditor(), true, false);
        }
        String actionsHeader = resword.getString("rule_actions")
                + "&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;";
        configureColumn(row.getColumn(columnNames[columnNames.length - 1]), actionsHeader, new ActionsCellEditor(), new DefaultActionsEditor(locale), true,
                false);

    }

    @Override
    public void configureTableFacade(HttpServletResponse response, TableFacade tableFacade) {
        super.configureTableFacade(response, tableFacade);
        // getColumnNames();
        getColumnNamesMap();
        tableFacade.addFilterMatcher(new MatcherKey(Character.class), new CharFilterMatcher());
        tableFacade.addFilterMatcher(new MatcherKey(Status.class), new StatusFilterMatcher());

        for (int i = 0; i < columnNames.length; i++) {
            if (columnNames[i].startsWith("sed_"))
                tableFacade.addFilterMatcher(new MatcherKey(Integer.class, columnNames[i]), new StudyEventWorkflowStatusFilterMatcher());
        }

    }

    @Override
    public void configureTableFacadePostColumnConfiguration(TableFacade tableFacade) {
        Role r = currentRole.getRole();
        boolean addSubjectLinkShow = studyBean.getStatus().isAvailable() && !r.equals(Role.MONITOR) && !isEnrollmentCapped();
        tableFacade.setToolbar(new ListStudySubjectTableToolbar(getStudyEventDefinitions(), getStudyGroupClasses(), addSubjectLinkShow, showMoreLink, getParticipateModuleStatus(), viewStudySubjectService, permissionService, studyBean, request));
    }

    private boolean isEnrollmentCapEnforced() {
        String enrollmentCapStatus = null;
        if (studyBean.isSite()) {
            enrollmentCapStatus = getStudyParameterValueDAO().findByHandleAndStudy(studyBean.getStudy().getStudyId(), "enforceEnrollmentCap").getValue();
        } else {
            enrollmentCapStatus = getStudyParameterValueDAO().findByHandleAndStudy(studyBean.getStudyId(), "enforceEnrollmentCap").getValue();
        }
        boolean capEnforced = Boolean.valueOf(enrollmentCapStatus);
        return capEnforced;
    }


    protected boolean isEnrollmentCapped() {

        boolean capIsOn = isEnrollmentCapEnforced();
        int numberOfSubjects = getStudySubjectDAO().getCountofActiveStudySubjects();

        Study sb = null;
        if (studyBean.isSite()) {
            sb = (Study) studyDAO.findByPK(studyBean.getStudy().getStudyId());
        } else {
            sb = (Study) studyDAO.findByPK(studyBean.getStudyId());
        }
        int expectedTotalEnrollment = sb.getExpectedTotalEnrollment();

        if (numberOfSubjects >= expectedTotalEnrollment && capIsOn)
            return true;
        else
            return false;
    }


    @Override
    public void setDataAndLimitVariables(TableFacade tableFacade) {
        Limit limit = tableFacade.getLimit();

        FindSubjectsFilter subjectFilter = getSubjectFilter(limit);
        List<String> userStatuses = new ArrayList<>();


        if (!limit.isComplete()) {
            Collection<StudySubjectBean> items = getStudySubjectDAO().getWithFilterAndSort(getStudyBean(), subjectFilter, null, 0, 0);
            if (items!=null)
                tableFacade.setTotalRows(items.size());
        }

        FindSubjectsSort subjectSort = getSubjectSort(limit);

        int rowStart = limit.getRowSelect().getRowStart();
        int rowEnd = limit.getRowSelect().getRowEnd();
        Collection<StudySubjectBean> items = getStudySubjectDAO().getWithFilterAndSort(getStudyBean(), subjectFilter, subjectSort, rowStart, rowEnd);

        Collection<HashMap<Object, Object>> theItems = new ArrayList<HashMap<Object, Object>>();
        Map<Integer, Study> studyMap = new HashMap<>();
        if(getStudyBean() != null)
            studyMap.put(getStudyBean().getStudyId(), getStudyBean());
        Study study = null;

        String [] tableColumns= getViewStudySubjectService().getTableColumns(PAGE_NAME,COMPONENT_NAME);
        for (StudySubjectBean studySubjectBean : items) {
            if(studyMap.get(studySubjectBean.getStudyId()) == null)
                studyMap.put(studySubjectBean.getStudyId() ,(Study) getStudyDAO().findByPK(studySubjectBean.getStudyId()));
            study = studyMap.get(studySubjectBean.getStudyId());
            HashMap<Object, Object> theItem = new HashMap<Object, Object>();
            theItem.put("studySubject", studySubjectBean);
            HtmlBuilder subjectLink = new HtmlBuilder();

            subjectLink.append("<a name=\"" + studySubjectBean.getLabel() + "\" class=\"pidVerification\" id=\"pid-" + studySubjectBean.getId() + "\" href=\"ViewStudySubject?id=" + studySubjectBean.getId());
            subjectLink.append("\">" + studySubjectBean.getLabel() + "</a>");
            theItem.put("studySubject.label", subjectLink.toString());
            theItem.put("studySubject.status", studySubjectBean.getStatus());
            theItem.put("enrolledAt", study.getUniqueIdentifier());

            if (tableColumns != null) {
                for (String column : tableColumns) {
                    String itemValue = null;
                    if (permissionService.isUserHasPermission(column, request, studyBean)) {
                        String sedOid = column.split("\\.")[0];
                        String formOid = column.split("\\.")[1];
                        String itemOid = column.split("\\.")[2];
                        //Get Item Value from database
                        StudyEventDefinition studyEventDefinition = null;
                        List<StudyEvent> studyEvents = null;
                        StudyEvent studyEvent = null;
                        List<EventCrf> eventCrfs = null;
                        EventCrf eventCrf = null;
                        List<ItemData> itemDatas = null;
                        ItemData itemData = null;
                        Item item = null;

                        if (!StringUtils.isEmpty(sedOid)) {
                            studyEventDefinition = studyEventDefinitionHibDao.findByOcOID(sedOid);if (studyEventDefinition != null && !studyEventDefinition.isRepeating()) {
                                studyEvents = studyEventDao.fetchListByStudyEventDefOID(sedOid, studySubjectBean.getId());

                                if (studyEvents != null) {
                                    for (StudyEvent se : studyEvents) {
                                        eventCrfs = se.getEventCrfs();
                                        if (eventCrfs != null) {
                                            CrfBean crf = crfDao.findByOcOID(formOid);
                                            for (EventCrf ec : eventCrfs) {
                                                if (ec.getCrfVersion().getCrf().getCrfId() == crf.getCrfId()) {
                                                    eventCrf = ec;
                                                    break;
                                                }
                                            }


                                            if (eventCrf != null) {
                                                itemDatas = eventCrf.getItemDatas();
                                                if (!StringUtils.isEmpty(itemOid))
                                                    item = itemDao.findByOcOID(itemOid);
                                                if (itemDatas != null) {
                                                    for (ItemData id : itemDatas) {
                                                        if (id.getItem().getItemId() == item.getItemId()
                                                                && id.getOrdinal() == 1) {
                                                            itemData = id;
                                                            break;
                                                        }
                                                    }

                                                    if (itemData != null && !StringUtils.isEmpty(itemData.getValue())) {
                                                        itemValue = itemData.getValue();
                                                        List<CrfVersion> crfVersions = crfVersionDao.findAllByCrfId(crf.getCrfId());
                                                        ItemFormMetadata itemFormMetadata = itemFormMetadataDao.findByItemCrfVersion(item.getItemId(), crfVersions.get(0).getCrfVersionId());
                                                        ResponseSet responseSet = itemFormMetadata.getResponseSet();
                                                        String responseType = responseSet.getResponseType().getName();

                                                        if (responseType.equals(CHECKBOX) || responseType.equals(MULTI_SELECT) || responseType.equals(RADIO) || responseType.equals(SINGLE_SELECT)) {
                                                            List<String> itemValues = Arrays.asList(itemData.getValue().split("\\s*,\\s*"));
                                                            String[] optionValues = responseSet.getOptionsValues().split("\\s*,\\s*");
                                                            String[] optionTexts = responseSet.getOptionsText().split("\\s*,\\s*");
                                                            String output = null;
                                                            for (int i = 0; i < optionValues.length; i++) {
                                                                for (String value : itemValues) {
                                                                    if (optionValues[i].equals(value)) {
                                                                        if (output == null) {
                                                                            output = optionTexts[i];
                                                                        } else {
                                                                            output = output + "," + optionTexts[i];
                                                                        }
                                                                        break;
                                                                    }
                                                                }
                                                            }
                                                            itemValue = output;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    theItem.put(column, itemValue != null ? itemValue : "");
                }
            }


            theItem.put("studySubject.oid", studySubjectBean.getOid());
            if (getParticipateModuleStatus().equals(ENABLED))
                theItem.put("participate.status", (studySubjectBean.getUserStatus() == null ? "" : studySubjectBean.getUserStatus().getValue()));
            theItem.put("studySubject.secondaryLabel", studySubjectBean.getSecondaryLabel());

            SubjectBean subjectBean = (SubjectBean) getSubjectDAO().findByPK(studySubjectBean.getSubjectId());
            theItem.put("subject", subjectBean);
            theItem.put("subject.charGender", subjectBean.getGender());

            // Get All study events for this study subject and then put list in
            // HashMap with study event definition id as
            // key and a list of study events as the value.
            List<StudyEventBean> allStudyEventsForStudySubject = getStudyEventDAO().findAllByStudySubject(studySubjectBean);
            HashMap<Integer, List<StudyEventBean>> allStudyEventsForStudySubjectBySedId = new HashMap<Integer, List<StudyEventBean>>();
            theItem.put("isSignable", isSignable( studySubjectBean.getId()));

            for (StudyEventBean studyEventBean : allStudyEventsForStudySubject) {
                if (allStudyEventsForStudySubjectBySedId.get(studyEventBean.getStudyEventDefinitionId()) == null) {
                    ArrayList<StudyEventBean> a = new ArrayList<StudyEventBean>();
                    a.add(studyEventBean);
                    allStudyEventsForStudySubjectBySedId.put(studyEventBean.getStudyEventDefinitionId(), a);
                } else {
                    allStudyEventsForStudySubjectBySedId.get(studyEventBean.getStudyEventDefinitionId()).add(studyEventBean);
                }

            }
            SubjectGroupMapBean subjectGroupMapBean;
            for (StudyGroupClassBean studyGroupClass : getStudyGroupClasses()) {
                subjectGroupMapBean = getSubjectGroupMapDAO().findAllByStudySubjectAndStudyGroupClass(studySubjectBean.getId(), studyGroupClass.getId());
                if (null != subjectGroupMapBean) {
                    theItem.put("sgc_" + studyGroupClass.getId(), subjectGroupMapBean.getStudyGroupId());
                    theItem.put("grpName_sgc_" + studyGroupClass.getId(), subjectGroupMapBean.getStudyGroupName());
                }
            }
            subjectGroupMapBean = null;
            for (StudyEventDefinitionBean studyEventDefinition : getStudyEventDefinitions()) {

                List<StudyEventBean> studyEvents = allStudyEventsForStudySubjectBySedId.get(studyEventDefinition.getId());
                StudyEventWorkflowStatusEnum workflowStatus = null;
                studyEvents = studyEvents == null ? new ArrayList<StudyEventBean>() : studyEvents;
                if (studyEvents.size() < 1) {
                    workflowStatus = StudyEventWorkflowStatusEnum.NOT_SCHEDULED;
                } else {
                    for (StudyEventBean studyEventBean : studyEvents) {
                        if (studyEventBean.getSampleOrdinal() == 1) {
                            workflowStatus = studyEventBean.getWorkflowStatus() ;
                            break;
                        }
                    }

                }
                theItem.put("sed_" + studyEventDefinition.getId(), workflowStatus);
                theItem.put("sed_" + studyEventDefinition.getId() + "_studyEvents", studyEvents);
                theItem.put("sed_" + studyEventDefinition.getId() + "_object", studyEventDefinition);

            }

            theItems.add(theItem);
        }

        // Do not forget to set the items back on the tableFacade.
        tableFacade.setItems(theItems);

    }

    private Boolean isSignable(int studySubjectId) {
        // https://jira.openclinica.com/browse/OC-13185
        StudySubjectService studySubjectService = (StudySubjectService) WebApplicationContextUtils.getWebApplicationContext(
                session.getServletContext()).getBean("studySubjectService");
        StudySubjectBean studySub = (StudySubjectBean) studySubjectDAO.findByPK(studySubjectId);
        List<DisplayStudyEventBean> displayStudyEvents = studySubjectService.getDisplayStudyEventsForStudySubject(
                studySub, currentUser, currentRole, studyBean);

        if (studySub.getStatus().isSigned() || displayStudyEvents.size() == 0)
            return false;

        for(DisplayStudyEventBean displayStudyEvent: displayStudyEvents) {
            StudyEventBean studyEventBean = displayStudyEvent.getStudyEvent();

            if (!studyEventBean.isRemoved() && !studyEventBean.isArchived()) {
                if (!studyEventBean.getWorkflowStatus().equals(StudyEventWorkflowStatusEnum.NOT_SCHEDULED)
                        && !studyEventBean.getWorkflowStatus().equals(StudyEventWorkflowStatusEnum.SKIPPED)
                        && !studyEventBean.getWorkflowStatus().equals(StudyEventWorkflowStatusEnum.STOPPED)
                        && !studyEventBean.getWorkflowStatus().equals(StudyEventWorkflowStatusEnum.COMPLETED)) {
                    return false;
                } else {
                    if (!displayStudyEvent.isSignAble()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private void getColumnNames() {
        ArrayList<String> columnNamesList = new ArrayList<String>();
        columnNamesList.add("label");
        columnNamesList.add("status");
        columnNamesList.add("enrolledAt");
        columnNamesList.add("oid");
        if (getParticipateModuleStatus().equals(ENABLED))
            columnNamesList.add("participate.status");
        for (StudyGroupClassBean studyGroupClass : getStudyGroupClasses()) {
            columnNamesList.add("sgc_" + studyGroupClass.getId());
        }
        for (StudyEventDefinitionBean studyEventDefinition : getStudyEventDefinitions()) {
            columnNamesList.add("sed_" + studyEventDefinition.getId());
        }
        columnNamesList.add("actions");
        columnNames = columnNamesList.toArray(columnNames);
    }

    private void getColumnNamesMap() {
        ArrayList<String> columnNamesList = new ArrayList<String>();
        columnNamesList.add("studySubject.label");
        columnNamesList.add("enrolledAt");


        String [] tableColumns= getViewStudySubjectService().getTableColumns(PAGE_NAME,COMPONENT_NAME);
        if(tableColumns!=null){
            for (String column : tableColumns) {
                if (permissionService.isUserHasPermission(column, request, studyBean)) {
                    columnNamesList.add(column);
                }
            }
        }



        columnNamesList.add("studySubject.status");
        columnNamesList.add("studySubject.oid");
        if(getParticipateModuleStatus().equals(ENABLED))
            columnNamesList.add("participate.status");
        for (StudyGroupClassBean studyGroupClass : getStudyGroupClasses()) {
            columnNamesList.add("sgc_" + studyGroupClass.getId());
        }
        for (StudyEventDefinitionBean studyEventDefinition : getStudyEventDefinitions()) {
            columnNamesList.add("sed_" + studyEventDefinition.getId());
        }
        columnNamesList.add("actions");
        columnNames = columnNamesList.toArray(columnNames);
    }

    protected FindSubjectsFilter getSubjectFilter(Limit limit) {
        FindSubjectsFilter auditUserLoginFilter = new FindSubjectsFilter();
        FilterSet filterSet = limit.getFilterSet();
        Collection<Filter> filters = filterSet.getFilters();
        for (Filter filter : filters) {
            String property = filter.getProperty();
            String value = filter.getValue();
            if ("studySubject.status".equalsIgnoreCase(property)) {
                value = Status.getByName(value).getId() + "";
            } else if ("participate.status".equalsIgnoreCase(property)) {
                UserStatus userStatus= UserStatus.valueOf(value.toUpperCase());
                value=userStatus.getCode()+"";
            } else if (property.startsWith("sgc_")) {
                int studyGroupClassId = property.endsWith("_") ? 0 : Integer.valueOf(property.split("_")[1]);
                value = studyGroupDAO.findByNameAndGroupClassID(value, studyGroupClassId).getId() + "";
            } else if (property.startsWith("SE_") && property.contains(".F_") && property.contains(".I_")) {
                String formOid = property.split("\\.")[1];
                String itemOid = property.split("\\.")[2];

                Item item = itemDao.findByOcOID(itemOid);
                CrfBean crf = crfDao.findByOcOID(formOid);
                List<CrfVersion> crfVersions = crfVersionDao.findAllByCrfId(crf.getCrfId());
                ItemFormMetadata itemFormMetadata = itemFormMetadataDao.findByItemCrfVersion(item.getItemId(), crfVersions.get(0).getCrfVersionId());
                ResponseSet responseSet = itemFormMetadata.getResponseSet();
                String responseType = responseSet.getResponseType().getName();
                if (responseType.equals(CHECKBOX) || responseType.equals(MULTI_SELECT) || responseType.equals(RADIO) || responseType.equals(SINGLE_SELECT)) {
                    List<String> itemTexts = Arrays.asList(filter.getValue().split("\\s*,\\s*"));
                    String[] optionValues = responseSet.getOptionsValues().split("\\s*,\\s*");
                    String[] optionTexts = responseSet.getOptionsText().split("\\s*,\\s*");
                    String output = null;
                    for (int i = 0; i < optionValues.length; i++) {
                        for (String it : itemTexts) {
                            if (optionTexts[i].equalsIgnoreCase(it.trim())) {
                                if (output == null) {
                                    output = optionValues[i];
                                } else {
                                    output = output + "," + optionValues[i];
                                }
                                break;
                            }
                        }
                    }
                    value = output != null ? output : value;
                }
            }else if(property.startsWith("sed_")){
                if (StudyEventWorkflowStatusEnum.getByI18nDescription(value) != null)
                    value = StudyEventWorkflowStatusEnum.getByI18nDescription(value) + "";
            }
            auditUserLoginFilter.addFilter(property, value);
        }

        return auditUserLoginFilter;
    }

    /**
     * A very custom way to sort the items. The PresidentSort acts as a command for the Hibernate criteria object. There
     * are probably many ways to do this, but this is the most flexible way I have found. The point is you need to
     * somehow take the Limit information and sort the rows.
     *
     * @param limit The Limit to use.
     */
    protected FindSubjectsSort getSubjectSort(Limit limit) {
        FindSubjectsSort subjectSort = new FindSubjectsSort();
        SortSet sortSet = limit.getSortSet();
        Collection<Sort> sorts = sortSet.getSorts();
        List <Sort> disableSorts = new ArrayList<>();
        for (Sort sort : sorts) {
            String property = validateProperty(sort.getProperty());
            String order = sort.getOrder().toParam();
            subjectSort.addSort(property, order);
        }

        return subjectSort;
    }

    private ArrayList<StudyEventDefinitionBean> getStudyEventDefinitions() {
        ArrayList<StudyEventDefinitionBean> tempList = new ArrayList<>();

        if (this.studyEventDefinitions == null) {
            if (studyBean.isSite()) {
                this.studyEventDefinitions = getStudyEventDefinitionDao().findAllActiveByParentStudyId(studyBean.getStudy().getStudyId());
            } else {
                this.studyEventDefinitions = getStudyEventDefinitionDao().findAllActiveByParentStudyId(studyBean.getStudyId());
            }
        }
        for (StudyEventDefinitionBean studyEventDefinition : this.studyEventDefinitions) {
            if (!studyEventDefinition.getType().equals(COMMON)) {
                tempList.add(studyEventDefinition);
            }
        }

        return tempList;
    }

    @SuppressWarnings( "unchecked" )
    private ArrayList<StudyGroupClassBean> getStudyGroupClasses() {
        if (this.studyGroupClasses == null) {
            if (studyBean.isSite()) {
                Study parentStudy =studyBean.getStudy();
                studyGroupClasses = getStudyGroupClassDAO().findAllActiveByStudy(parentStudy);
            } else {
                studyGroupClasses = getStudyGroupClassDAO().findAllActiveByStudy(studyBean);
            }
        }
        return studyGroupClasses;
    }

    public StudyParameterValueDAO getStudyParameterValueDAO() {
        return studyParameterValueDAO;
    }

    public void setStudyParameterValueDAO(StudyParameterValueDAO studyParameterValueDAO) {
        this.studyParameterValueDAO = studyParameterValueDAO;
    }

    public StudyEventDefinitionDAO getStudyEventDefinitionDao() {
        return studyEventDefinitionDao;
    }

    public void setStudyEventDefinitionDao(StudyEventDefinitionDAO studyEventDefinitionDao) {
        this.studyEventDefinitionDao = studyEventDefinitionDao;
    }

    public Study getStudyBean() {
        return studyBean;
    }

    public void setStudyBean(Study studyBean) {
        this.studyBean = studyBean;
    }

    public StudySubjectDAO getStudySubjectDAO() {
        return studySubjectDAO;
    }

    public void setStudySubjectDAO(StudySubjectDAO studySubjectDAO) {
        this.studySubjectDAO = studySubjectDAO;
    }

    public SubjectDAO getSubjectDAO() {
        return subjectDAO;
    }

    public void setSubjectDAO(SubjectDAO subjectDAO) {
        this.subjectDAO = subjectDAO;
    }

    public StudyEventDAO getStudyEventDAO() {
        return studyEventDAO;
    }

    public void setStudyEventDAO(StudyEventDAO studyEventDAO) {
        this.studyEventDAO = studyEventDAO;
    }

    public StudyGroupClassDAO getStudyGroupClassDAO() {
        return studyGroupClassDAO;
    }

    public void setStudyGroupClassDAO(StudyGroupClassDAO studyGroupClassDAO) {
        this.studyGroupClassDAO = studyGroupClassDAO;
    }

    public SubjectGroupMapDAO getSubjectGroupMapDAO() {
        return subjectGroupMapDAO;
    }

    public void setSubjectGroupMapDAO(SubjectGroupMapDAO subjectGroupMapDAO) {
        this.subjectGroupMapDAO = subjectGroupMapDAO;
    }

    public StudyDao getStudyDAO() {
        return studyDAO;
    }

    public void setStudyDAO(StudyDao studyDAO) {
        this.studyDAO = studyDAO;
    }

    public StudyUserRoleBean getCurrentRole() {
        return currentRole;
    }

    public void setCurrentRole(StudyUserRoleBean currentRole) {
        this.currentRole = currentRole;
    }

    public EventCRFDAO getEventCRFDAO() {
        return eventCRFDAO;
    }

    public void setEventCRFDAO(EventCRFDAO eventCRFDAO) {
        this.eventCRFDAO = eventCRFDAO;
    }

    public EventDefinitionCRFDAO getEventDefintionCRFDAO() {
        return eventDefintionCRFDAO;
    }

    public void setEventDefintionCRFDAO(EventDefinitionCRFDAO eventDefintionCRFDAO) {
        this.eventDefintionCRFDAO = eventDefintionCRFDAO;
    }

    public StudyGroupDAO getStudyGroupDAO() {
        return studyGroupDAO;
    }

    public void setStudyGroupDAO(StudyGroupDAO studyGroupDAO) {
        this.studyGroupDAO = studyGroupDAO;
    }

    public UserAccountBean getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(UserAccountBean currentUser) {
        this.currentUser = currentUser;
    }

    private class CharFilterMatcher implements FilterMatcher {
        public boolean evaluate(Object itemValue, String filterValue) {
            String item = StringUtils.lowerCase(String.valueOf(itemValue));
            String filter = StringUtils.lowerCase(String.valueOf(filterValue));
            if (StringUtils.contains(item, filter)) {
                return true;
            }

            return false;
        }
    }

    public class StatusFilterMatcher implements FilterMatcher {
        public boolean evaluate(Object itemValue, String filterValue) {

            String item = StringUtils.lowerCase(String.valueOf(((Status) itemValue).getName()));
            String filter = StringUtils.lowerCase(String.valueOf(filterValue));

            if (filter.equals(item)) {
                return true;
            }
            return false;
        }
    }


    public class StudyEventWorkflowStatusFilterMatcher implements FilterMatcher {
        public boolean evaluate(Object itemValue, String filterValue) {
            String item = StudyEventWorkflowStatusEnum.getByI18nDescription((String) itemValue).toString();
            String filter = StringUtils.lowerCase(String.valueOf(filterValue));// .trim().replace(" ", "_");
            if (filterValue.equals(resterms.getString(item))) {
                return true;
            }
            return false;
        }
    }

    public class SubjectGroupFilterMatcher implements FilterMatcher {

        public boolean evaluate(Object itemValue, String filterValue) {

            String item = StringUtils.lowerCase(studyGroupDAO.findByPK(Integer.valueOf(itemValue.toString())).getName());
            String filter = StringUtils.lowerCase(String.valueOf(filterValue.trim()));
            if (filter.equals(item)) {
                return true;
            }
            return false;
        }
    }

    private class StatusCellEditor implements CellEditor {
        public Object getValue(Object item, String property, int rowcount) {
            StudySubjectBean studySubject = (StudySubjectBean) new BasicCellEditor().getValue(item, "studySubject", rowcount);
            return studySubject.getStatus().getName();
        }
    }


    private class SubjectIdCellEditor implements CellEditor {
        public Object getValue(Object item, String property, int rowcount) {
            Object itemValue = ItemUtils.getItemValue(item, property);
            return itemValue;
        }
    }

    private class ItemIdCellEditor implements CellEditor {
        public Object getValue(Object item, String property, int rowcount) {
            Object itemValue = ItemUtils.getItemValue(item, property);
            return itemValue;
        }
    }

    private class StatusDroplistFilterEditor extends DroplistFilterEditor {
        @Override
        protected List<Option> getOptions() {
            List<Option> options = new ArrayList<Option>();
            for (Object status : Status.toDropDownArrayList()) {
                ((Status) status).getName();
                options.add(new Option(((Status) status).getName(), ((Status) status).getName()));
            }
            return options;
        }
    }

    private class CustomColumnDroplistFilterEditor extends DroplistFilterEditor {
        List<String> optionsText = Arrays.asList(responseSet.getOptionsText().split("\\s*,\\s*"));
        @Override
        protected List<Option> getOptions() {
            List<Option> options = new ArrayList<Option>();
            for (String optionText : optionsText) {
                options.add(new Option( optionText, optionText));
            }
            return options;
        }
    }

    private class ParticipateStatusDroplistFilterEditor extends DroplistFilterEditor {
        List<UserStatus> userStatusList =
                new ArrayList<UserStatus>(EnumSet.allOf(UserStatus.class));
        @Override
        protected List<Option> getOptions() {
            List<Option> options = new ArrayList<Option>();
            for (UserStatus status : userStatusList) {
                options.add(new Option( status.getValue(), status.getValue()));
            }
            return options;
        }
    }



    private class StudyEventWorkflowStatusDroplistFilterEditor extends DroplistFilterEditor {
        @Override
        protected List<Option> getOptions() {
            List<Option> options = new ArrayList<Option>();
            List<StudyEventWorkflowStatusEnum> eventWorkflowStatuses = new ArrayList<>(Arrays.asList(StudyEventWorkflowStatusEnum.values()));

            for (StudyEventWorkflowStatusEnum workflow : eventWorkflowStatuses) {
                if (!workflow.equals(StudyEventWorkflowStatusEnum.NOT_SCHEDULED))
                    options.add(new Option(workflow.getDisplayValue(), workflow.getDisplayValue()));
            }
            options.add(new Option(resterms.getString(SIGNED.toLowerCase()).toLowerCase(),resterms.getString(SIGNED.toLowerCase()).toLowerCase()));
            options.add(new Option(resterms.getString(LOCKED.toLowerCase()).toLowerCase(),resterms.getString(LOCKED.toLowerCase()).toLowerCase()));
            options.add(new Option(resterms.getString(NOT_SIGNED.toLowerCase()).toLowerCase(),resterms.getString(NOT_SIGNED.toLowerCase()).toLowerCase()));
            options.add(new Option(resterms.getString(NOT_LOCKED.toLowerCase()).toLowerCase(),resterms.getString(NOT_LOCKED.toLowerCase()).toLowerCase()));
            return options;
        }
    }

    private class SubjectGroupClassDroplistFilterEditor extends DroplistFilterEditor {
        private StudyGroupClassBean studyGroupClass = new StudyGroupClassBean();

        // constructor
        SubjectGroupClassDroplistFilterEditor(StudyGroupClassBean studyGroupClass) {
            this.studyGroupClass = studyGroupClass;
        }

        @Override
        protected List<Option> getOptions() {
            List<Option> options = new ArrayList<Option>();
            StudyGroupDAO studyGroupDAO = getStudyGroupDAO();
            for (Object subjectStudyGroup : studyGroupDAO.findAllByGroupClass(this.studyGroupClass)) {
                options.add(new Option(((StudyGroupBean) subjectStudyGroup).getName(), ((StudyGroupBean) subjectStudyGroup).getName()));
            }
            return options;
        }
    }

    private class StudyGroupClassCellEditor implements CellEditor {

        StudyGroupClassBean studyGroupClass;
        String groupName;

        public StudyGroupClassCellEditor(StudyGroupClassBean studyGroupClass) {
            this.studyGroupClass = studyGroupClass;
        }

        private String logic() {
            return groupName != null ? groupName : "";
        }

        public Object getValue(Object item, String property, int rowcount) {
            groupName = (String) ((HashMap<Object, Object>) item).get("grpName_sgc_" + studyGroupClass.getId());
            return logic();
        }
    }

    private class StudyEventDefinitionCellEditor implements CellEditor {

        StudyEventDefinitionBean studyEventDefinition;
        StudySubjectBean studySubjectBean;
        StudyEventWorkflowStatusEnum workflowStatus;
        List<StudyEventBean> studyEvents;

        public StudyEventDefinitionCellEditor(StudyEventDefinitionBean studyEventDefinition) {
            this.studyEventDefinition = studyEventDefinition;
        }

        @SuppressWarnings( "unchecked" )
        private void logic() {
            studyEvents = getStudyEventDAO().findAllByStudySubjectAndDefinition(studySubjectBean, studyEventDefinition);
            if (studyEvents.size() < 1) {
                workflowStatus= StudyEventWorkflowStatusEnum.NOT_SCHEDULED ;
            } else {
                workflowStatus = studyEvents.get(studyEvents.size() - 1).getWorkflowStatus();

            }
        }

        private String getCount() {
            return studyEvents.size() < 2 ? "" : "&nbsp;&nbsp;&nbsp;x" + String.valueOf(studyEvents.size() + "");
        }

        public Object getValue(Object item, String property, int rowcount) {

            studySubjectBean = (StudySubjectBean) ((HashMap<Object, Object>) item).get("studySubject");

            logic();

            StringBuilder url = new StringBuilder();
            url.append("<span class='" + imageIconPaths.get(workflowStatus.toString()) + "' border='0' style='position: relative; left: 7px;'>");
            url.append(getCount());

            return url.toString();
        }

    }

    private class StudyEventDefinitionMapCellEditor implements CellEditor {

        StudyEventDefinitionBean studyEventDefinition;
        StudySubjectBean studySubjectBean;
        String workflowStatus;
        List<StudyEventBean> studyEvents;
        SubjectBean subject;

        private String getCount() {
            return studyEvents.size() < 2 ? "" : "&nbsp;&nbsp;&nbsp;x" + String.valueOf(studyEvents.size() + "");
        }

        @SuppressWarnings( "unchecked" )
        public Object getValue(Object item, String property, int rowcount) {
            studyEvents = (List<StudyEventBean>) ((HashMap<Object, Object>) item).get(property + "_studyEvents");
            studyEventDefinition = (StudyEventDefinitionBean) ((HashMap<Object, Object>) item).get(property + "_object");
            workflowStatus = ((StudyEventWorkflowStatusEnum) ((HashMap<Object, Object>) item).get(property)).toString();
            subject = (SubjectBean) ((HashMap<Object, Object>) item).get("subject");
            studySubjectBean = (StudySubjectBean) ((HashMap<Object, Object>) item).get("studySubject");

            StringBuilder url = new StringBuilder();
            url.append(eventDivBuilder(subject, rowcount, studyEvents, studyEventDefinition, studySubjectBean));
            for (int i = 0; i < studyEvents.size(); i++) {
                StudyEventBean seb = studyEvents.get(i);
                String currentStatus = workflowStatus;
                if (seb.isRemoved()) {
                    currentStatus = EventCrfStatusEnum.REMOVED.toString();
                } else {
                    currentStatus = seb.getWorkflowStatus().toString();
                }
                if (i == 0 || (statusPriorities.get(workflowStatus) > statusPriorities.get(currentStatus))) {
                    workflowStatus = currentStatus;
                }
            }
            url.append("<span class='" + imageIconPaths.get(workflowStatus) + "' style='padding-top: 2px; padding-bottom: 3px;'>");
            url.append("<span style='color: #668cff; padding-left: 0px; font-size: 13px;'>" + getCount() + "</span></a></td></tr></table>");

            return url.toString();
        }

    }

    private class ActionsCellEditor implements CellEditor {

        @SuppressWarnings( "unchecked" )
        public Object getValue(Object item, String property, int rowcount) {
            String value = "";
            StudySubjectBean studySubjectBean = (StudySubjectBean) ((HashMap<Object, Object>) item).get("studySubject");
            Boolean isSignable = (Boolean) ((HashMap<Object, Object>) item).get("isSignable");
            Integer studySubjectId = studySubjectBean.getId();
            if (studySubjectId != null) {
                StringBuilder url = new StringBuilder();

                url.append(viewStudySubjectLinkBuilder(studySubjectBean));
                if (getCurrentRole().getRole() != Role.MONITOR) {
                    if (getStudyBean().getStatus() == core.org.akaza.openclinica.domain.Status.AVAILABLE
                            && !(studySubjectBean.getStatus() == Status.DELETED || studySubjectBean.getStatus() == Status.AUTO_DELETED)
                            && getCurrentRole().getRole() != Role.RESEARCHASSISTANT && getCurrentRole().getRole() != Role.RESEARCHASSISTANT2) {
                        url.append(removeStudySubjectLinkBuilder(studySubjectBean));
                    }
                    if (getStudyBean().getStatus() == core.org.akaza.openclinica.domain.Status.AVAILABLE
                            && (studySubjectBean.getStatus() == Status.DELETED || studySubjectBean.getStatus() == Status.AUTO_DELETED)
                            && getCurrentRole().getRole() != Role.RESEARCHASSISTANT && getCurrentRole().getRole() != Role.RESEARCHASSISTANT2) {
                        url.append(restoreStudySubjectLinkBuilder(studySubjectBean, null));
                    }
                    if (getStudyBean().getStatus() == core.org.akaza.openclinica.domain.Status.AVAILABLE && getCurrentRole().getRole() != Role.RESEARCHASSISTANT
                            && getCurrentRole().getRole() != Role.RESEARCHASSISTANT2 && getCurrentRole().getRole() != Role.INVESTIGATOR
                            && studySubjectBean.getStatus() == Status.AVAILABLE) {
                        url.append(reAssignStudySubjectLinkBuilder(studySubjectBean));
                    }

                    if (getCurrentRole().getRole() == Role.INVESTIGATOR && getStudyBean().getStatus() == core.org.akaza.openclinica.domain.Status.AVAILABLE
                            && studySubjectBean.getStatus() != Status.DELETED && isSignable) {
                        url.append(signStudySubjectLinkBuilder(studySubjectBean));
                    }

                }
                value = url.toString();
            }

            return value;
        }

    }

    private String participateStatus(StudySubjectBean studySubjectBean) {
        Study study = (Study) studyDAO.findByPK(studySubjectBean.getStudyId());
        Study pStudy = getParentStudy(study.getOc_oid());
        String participateFormStatus = getStudyParameterValueDAO().findByHandleAndStudy(pStudy.getStudyId(), "participantPortal").getValue();
        return participateFormStatus;
    }

    private String getParticipateModuleStatus() {
        Study pStudy = getParentStudy(studyBean.getOc_oid());
        String participatModuleStatus = getStudyParameterValueDAO().findByHandleAndStudy(pStudy.getStudyId(), "participantPortal").getValue();
        return participatModuleStatus;
    }

    private Study getParentStudy(String studyOid) {
        Study study = getStudy(studyOid);
        if(study.isSite())
            return study.getStudy();
        else
            return study;
    }

    private Study getStudy(String oid) {
        Study studyBean = (Study) studyDAO.findByOid(oid);
        return studyBean;
    }

    private String viewStudySubjectLinkBuilder(StudySubjectBean studySubject) {
        HtmlBuilder actionLink = new HtmlBuilder();
        actionLink.append("<a name=\"" + studySubject.getLabel() + "\" class=\"pidVerification\" id=\"pid-" + studySubject.getId() + "\" onmouseup=\"javascript:setImage('bt_View1','icon icon-search');\" onmousedown=\"javascript:setImage('bt_View1','icon icon-search');\" href=\"ViewStudySubject?id="
                + studySubject.getId());
        actionLink.append("\"><span hspace=\"2\" border=\"0\" title=\"View\" alt=\"View\" class=\"icon icon-search\" name=\"bt_Reassign1\"/></a>");
        actionLink.append("&nbsp;&nbsp;&nbsp;");
        return actionLink.toString();
    }

    private String removeStudySubjectLinkBuilder(StudySubjectBean studySubject) {
        HtmlBuilder actionLink = new HtmlBuilder();
        actionLink
                .append("<a onmouseup=\"javascript:setImage('bt_View1','icon icon-cancel');\" onmousedown=\"javascript:setImage('bt_View1','icon icon-cancel');\" href=\"RemoveStudySubject?action=confirm&id="
                        + studySubject.getId() + "&subjectId=" + studySubject.getSubjectId() + "&studyId=" + studySubject.getStudyId());
        actionLink.append("\"><span hspace=\"2\" border=\"0\" title=\"Remove\" alt=\"View\" class=\"icon icon-cancel\" name=\"bt_Reassign1\"/></a>");
        actionLink.append("&nbsp;&nbsp;&nbsp;");
        return actionLink.toString();

    }

    private String signStudySubjectLinkBuilder(StudySubjectBean studySubject) {
        HtmlBuilder builder = new HtmlBuilder();
        builder.append(
                "<a onmouseup=\"javascript:setImage('bt_View1','icon icon-icon-sign');\" onmousedown=\"javascript:setImage('bt_View1','icon icon-icon-sign');\" href=\"SignStudySubject?id="
                        + studySubject.getId());
        builder.append("\"><span hspace=\"2\" border=\"0\" title=\"Sign\" alt=\"Sign\" class=\"icon icon-icon-sign\" name=\"bt_Reassign1\"/></a>");
        builder.append("&nbsp;&nbsp;&nbsp;");
        return builder.toString();

    }

    private String reAssignStudySubjectLinkBuilder(StudySubjectBean studySubject) {
        HtmlBuilder actionLink = new HtmlBuilder();
        actionLink
                .append("<a onmouseup=\"javascript:setImage('bt_View1','icon icon-icon-reassign3');\" onmousedown=\"javascript:setImage('bt_View1','icon icon-search');\" href=\"ReassignStudySubject?id="
                        + studySubject.getId());
        actionLink.append("\"><span hspace=\"2\" border=\"0\" title=\"Reassign\" alt=\"View\" class=\"icon icon-icon-reassign3\" name=\"bt_Reassign1\"/></a>");
        actionLink.append("&nbsp;&nbsp;&nbsp;");
        return actionLink.toString();

    }

    private String restoreStudySubjectLinkBuilder(StudySubjectBean studySubject, String text) {
        HtmlBuilder builder = new HtmlBuilder();
        String link = "RestoreStudySubject?action=confirm&id="
                + studySubject.getId() + "&subjectId=" + studySubject.getSubjectId() + "&studyId=" + studySubject.getStudyId();
        builder.append(
                "<a onmouseup=\"javascript:setImage('bt_View1','icon icon-ccw');\" " +
                        "onmousedown=\"javascript:setImage('bt_View1','icon icon-ccw');\" href=\"" + link + "\" " );
        builder.append("\"><span hspace=\"2\" border=\"0\" title=\"Restore\" alt=\"Restore\" " +
                "class=\"icon icon-ccw\" name=\"bt_Reassign1\"/></a>");
        builder.nbsp().nbsp();
        if (text != null) {
            builder.a().href(link);
            builder.close().append(text).aEnd();
        }
        return builder.toString();

    }

    private String eventDivBuilder(SubjectBean subject, Integer rowCount, List<StudyEventBean> studyEvents, StudyEventDefinitionBean sed,
                                   StudySubjectBean studySubject) {

        String studySubjectLabel = studySubject.getLabel();

        String divWidth = studyEvents.size() >= 3 ? "565" : studyEvents.size() == 2 ? "395" : "180";

        HtmlBuilder eventDiv = new HtmlBuilder();

        eventDiv.table(0).border("0").cellpadding("0").cellspacing("0").close();
        // Lock Div
        eventDiv.div().id("Lock_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount)
                .style("position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;").close();
        if (studyEvents.size() > 1) {
            repeatingLockLinkBuilder(eventDiv, studySubjectLabel, rowCount, studyEvents, sed);
        } else {
            lockLinkBuilder(eventDiv, studySubjectLabel, rowCount, studyEvents, sed);
        }
        eventDiv.divEnd();

        eventDiv.tr(0).valign("top").close().td(0).close();
        // Event Div
        eventDiv.div().id("Event_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount)
                .style("position: absolute; visibility: hidden; z-index: 3;width:" + divWidth + "px; top: 0px; float: left;").close();
        eventDiv.div().styleClass("box_T").close().div().styleClass("box_L").close().div().styleClass("box_R").close().div().styleClass("box_B").close().div()
                .styleClass("box_TL").close().div().styleClass("box_TR").close().div().styleClass("box_BL").close().div().styleClass("box_BR").close();

        eventDiv.div().styleClass("tablebox_center").close();
        eventDiv.div().styleClass("ViewSubjectsPopup").style("color: rgb(91, 91, 91);").close();

        eventDiv.table(0).border("0").cellpadding("0").cellspacing("0").close();
        eventDiv.tr(0).valign("top").close();

        if (studyEvents.size() > 1) {
            repeatingEventDivBuilder(eventDiv, subject, rowCount, studyEvents, sed, studySubject);
        } else {
            singleEventDivBuilder(eventDiv, subject, rowCount, studyEvents, sed, studySubject);
        }

        return eventDiv.toString();
    }

    private void repeatingEventDivBuilder(HtmlBuilder eventDiv, SubjectBean subject, Integer rowCount, List<StudyEventBean> studyEvents,
                                          StudyEventDefinitionBean sed, StudySubjectBean studySubject) {

        String tableHeaderRowStyleClass = "table_header_row";
        String tableHeaderRowLeftStyleClass = "table_header_row_left";
        String add_another_occurrence = resword.getString("add_another_occurrence");
        String click_for_more_options = resword.getString("click_for_more_options");
        String schedule = resword.getString("schedule");
        String view = resword.getString("view") + "/" + resword.getString("enter_data");
        String edit = resword.getString("edit");
        String remove = resword.getString("remove");
        String occurrence_x_of = resword.getString("ocurrence");
        String subjectText = resword.getString("subject");
        String eventText = resword.getString("event");
        String status = resword.getString("status");
        String signText = resword.getString("signed");
        String lockText = resword.getString("locked");
        String archivedText = resword.getString("archived");

        StudyEventBean defaultEvent = studyEvents.get(0);
        String studySubjectLabel = studySubject.getLabel();
        Status eventSysStatus = studySubject.getStatus();
        Integer studyEventsSize = studyEvents.size();

        eventDiv.td(0).styleClass(tableHeaderRowLeftStyleClass).colspan("2").close();
        eventDiv.append(subjectText).append(": ").append(studySubjectLabel).br();
        eventDiv.append(eventText).append(": ").append(sed.getName()).br();
        eventDiv.tdEnd();

        eventDiv.td(0).styleClass(tableHeaderRowLeftStyleClass).align("right").colspan("3").close();
        divCloseRepeatinglinkBuilder(eventDiv, studySubjectLabel, rowCount, studyEvents, sed);
        eventDiv.br();
        if (studyBean.getStatus() == core.org.akaza.openclinica.domain.Status.AVAILABLE) {
            eventDiv.span().styleClass("font-weight: normal;").close();
            eventDiv.ahref("CreateNewStudyEvent?studySubjectId=" + studySubject.getId() + "&studyEventDefinition=" + sed.getId(), add_another_occurrence);
        }
        eventDiv.nbsp().nbsp().nbsp();
        for (int i = 1; i <= studyEventsSize; i++) {
            eventDiv.ahref("javascript:StatusBoxSkip('" + studySubjectLabel.replaceAll("'", "\\\\'") + "_" + sed.getId() + "_" + rowCount + "'," + studyEventsSize + "," + i + ");",
                    String.valueOf(i));
            if (i < studyEventsSize) {
                eventDiv.append("|");
            }
        }
        eventDiv.spanEnd();
        eventDiv.tdEnd().trEnd(0);
        eventDiv.tr(0).close();
        // <td>...</td>
        eventDiv.td(0).id("Scroll_off_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "_back").styleClass("statusbox_scroll_L_dis").width("20")
                .close();
        eventDiv.append("<span class=\"icon icon-caret-left gray clickable\" onclick=\"StatusBoxBack('" + studySubjectLabel.replaceAll("'", "\\\\'") + "_" + sed.getId() + "_" + rowCount + "'," + studyEventsSize + ");\"" + "/>");
        eventDiv.tdEnd();
        // <td>...</td>

        for (int i = 0; i < studyEvents.size(); i++) {
            StudyEventBean studyEventBean = studyEvents.get(i);
            // <td>...</td>
            eventDiv.td(0).id("Event_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "_" + (i + 1)).valign("top").width("180");
            String style = "";
            if (studyEventBean.isRemoved()) {
                style = style + "vertical-align: top;";
            }
            if (i + 1 > 3) {
                style = style + "display: none;";
            }
            if (style.length() > 1) {
                eventDiv.style(style);
            }
            eventDiv.close();
            // <table>...</table>
            eventDiv.table(0).border("0").cellpadding("0").cellspacing("0").width("100%").close();
            // <tr><td>...</td></tr>
            eventDiv.tr(0).valign("top").close();
            eventDiv.td(0).styleClass(tableHeaderRowStyleClass).colspan("2").close();
            eventDiv.bold().append(occurrence_x_of).append(" " + (i + 1) + " of " + studyEventsSize).br();
            if (studyEventBean.getDateStarted() != null)
                eventDiv.append(formatDate(studyEventBean.getDateStarted())).br();
            if (studyEventBean.isRemoved()) {
                eventDiv.append(EventCrfStatusEnum.REMOVED.toString().toLowerCase()).br();
            } else {
                eventDiv.append(studyEventBean.getWorkflowStatus().getDisplayValue()).br();
            }
            if (studyEventBean.isSigned()) {
                eventDiv.append("<span class=\"icon icon-stamp-new status\" alt=" + signText + " title=" + signText + " style=\"margin-right: 5px;\"></span>");
            }
            if (studyEventBean.isLocked()) {
                eventDiv.append("<span class=\"icon icon-lock-new status\" alt=" + lockText + " title=" + lockText + " style=\"margin-right: 5px;\"></span>");
            }
            if (studyEventBean.isArchived()) {
                eventDiv.append("<span class=\"icon icon-archived-new status\" alt=" + archivedText + " title=" + archivedText + "></span>");
            }
            eventDiv.boldEnd().tdEnd().trEnd(0);
            // <tr><td><table>...</table></td></tr>
            eventDiv.tr(0).id("Menu_on_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "_" + (i + 1)).close();
            eventDiv.td(0).colspan("2").close();
            eventDiv.table(0).border("0").cellpadding("0").cellspacing("0").width("100%").close();

            linksDivBuilder(eventDiv, subject, rowCount, studyEvents, sed, studySubject, studyEventBean);
            eventDiv.tableEnd(0).tdEnd().trEnd(0);
            eventDiv.tableEnd(0);
            eventDiv.tdEnd();
        }

        // <td>...</td>
        eventDiv.td(0).id("Scroll_off_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "_next").styleClass("statusbox_scroll_R_dis").width("20")
                .close();
        eventDiv.append("<span class=\"icon icon-caret-right gray clickable\" onclick=\"StatusBoxNext('" + studySubjectLabel.replaceAll("'", "\\\\'") + "_" + sed.getId() + "_" + rowCount + "'," + studyEventsSize + ");\"" + "/>");
        eventDiv.tdEnd();
        // <td>...</td>
        eventDiv.trEnd(0);

        eventDiv.tableEnd(0);
        eventDiv.divEnd().divEnd().divEnd().divEnd().divEnd().divEnd().divEnd().divEnd().divEnd().divEnd().divEnd();
        repeatingIconLinkBuilder(eventDiv, studySubjectLabel, rowCount, studyEvents, sed);

    }

    private void linksDivBuilder(HtmlBuilder eventDiv, SubjectBean subject, Integer rowCount, List<StudyEventBean> studyEvents, StudyEventDefinitionBean sed,
                                 StudySubjectBean studySubject, StudyEventBean currentEvent) {

        Status eventSysStatus = studySubject.getStatus();
        StudyEventWorkflowStatusEnum eventStatus = currentEvent.getWorkflowStatus();
        String studyEventId = String.valueOf(currentEvent.getId());

        String view = resword.getString("view") + "/" + resword.getString("enter_data");
        String edit = resword.getString("edit");
        String remove = resword.getString("remove");
        String delete = resword.getString("delete");
        String reassign = resword.getString("reassign");
        String restore = resword.getString("restore");

        if (eventSysStatus.getId() == Status.AVAILABLE.getId() || eventSysStatus == Status.SIGNED) {

            if (eventStatus.equals(StudyEventWorkflowStatusEnum.COMPLETED) ) {
                eventDiv.tr(0).valign("top").close();
                eventDiv.td(0).styleClass("table_cell").close();
                enterDataForStudyEventLinkBuilder(eventDiv, studyEventId, view);
                eventDiv.tdEnd().trEnd(0);

                if ((currentRole.getRole() == Role.STUDYDIRECTOR || currentUser.isSysAdmin()) && studyBean.getStatus() == core.org.akaza.openclinica.domain.Status.AVAILABLE
                        && currentRole.getRole() != Role.MONITOR) {
                    eventDiv.tr(0).valign("top").close();
                    eventDiv.td(0).styleClass("table_cell").close();
                    updateStudyEventLinkBuilder(eventDiv, studySubject.getId(), studyEventId, edit);
                    eventDiv.tdEnd().trEnd(0);
                    eventDiv.tr(0).valign("top").close();
                    eventDiv.td(0).styleClass("table_cell").close();
                    removeStudyEventLinkBuilder(eventDiv, studySubject.getId(), studyEventId, remove);
                    eventDiv.tdEnd().trEnd(0);
                    eventDiv.tr(0).valign("top").close();
                    eventDiv.td(0).styleClass("table_cell").close();
                    reassignStudyEventLinkBuilder(eventDiv, studySubject.getId(), studyEventId, reassign);
                    eventDiv.tdEnd().trEnd(0);
                }
            } else if ( currentEvent.isLocked()) {
                if (currentRole.getRole() == Role.STUDYDIRECTOR || currentUser.isSysAdmin()) {
                    eventDiv.tr(0).valign("top").close();
                    eventDiv.td(0).styleClass("table_cell").close();
                    enterDataForStudyEventLinkBuilder(eventDiv, studyEventId, view);
                    eventDiv.tdEnd().trEnd(0);
                    if (studyBean.getStatus() == core.org.akaza.openclinica.domain.Status.AVAILABLE) {
                        eventDiv.tr(0).valign("top").close();
                        eventDiv.td(0).styleClass("table_cell").close();
                        removeStudyEventLinkBuilder(eventDiv, studySubject.getId(), studyEventId, remove);
                        eventDiv.tdEnd().trEnd(0);
                        eventDiv.tr(0).valign("top").close();
                        eventDiv.td(0).styleClass("table_cell").close();
                        reassignStudyEventLinkBuilder(eventDiv, studySubject.getId(), studyEventId, reassign);
                        eventDiv.tdEnd().trEnd(0);
                    }
                }
            } else {
                eventDiv.tr(0).valign("top").close();
                eventDiv.td(0).styleClass("table_cell_left").close();
                enterDataForStudyEventLinkBuilder(eventDiv, studyEventId, view);
                eventDiv.tdEnd().trEnd(0);

                if (currentEvent.isRemoved()) {
                    eventDiv.tr(0).valign("top").close();
                    eventDiv.td(0).styleClass("table_cell_left").close();
                    eventDiv.append(restoreStudySubjectLinkBuilder(studySubject, restore));
                    eventDiv.tdEnd().trEnd(0);
                } else {
                    eventDiv.tr(0).valign("top").close();
                    eventDiv.td(0).styleClass("table_cell_left").close();
                    updateStudyEventLinkBuilder(eventDiv, studySubject.getId(), studyEventId, edit);
                    eventDiv.tdEnd().trEnd(0);
                    if ((currentRole.getRole() == Role.STUDYDIRECTOR || currentUser.isSysAdmin()) && studyBean.getStatus() == core.org.akaza.openclinica.domain.Status.AVAILABLE
                            && currentRole.getRole() != Role.MONITOR) {
                        eventDiv.tr(0).valign("top").close();
                        eventDiv.td(0).styleClass("table_cell_left").close();
                        removeStudyEventLinkBuilder(eventDiv, studySubject.getId(), studyEventId, remove);
                        eventDiv.tdEnd().trEnd(0);
                        eventDiv.tr(0).valign("top").close();
                        eventDiv.td(0).styleClass("table_cell").close();
                        reassignStudyEventLinkBuilder(eventDiv, studySubject.getId(), studyEventId, reassign);
                        eventDiv.tdEnd().trEnd(0);
                    }
                }
            }
        }

        if (eventSysStatus == Status.DELETED || eventSysStatus == Status.AUTO_DELETED) {
            eventDiv.tr(0).valign("top").close();
            eventDiv.td(0).styleClass("table_cell").close();
            enterDataForStudyEventLinkBuilder(eventDiv, studyEventId, view);
            eventDiv.tdEnd().trEnd(0);
        }

    }

    private void singleEventDivBuilder(HtmlBuilder eventDiv, SubjectBean subject, Integer rowCount, List<StudyEventBean> studyEvents,
                                       StudyEventDefinitionBean sed, StudySubjectBean studySubject) {

        String tableHeaderRowStyleClass = "table_header_row";
        String tableHeaderRowLeftStyleClass = "table_header_row_left";
        String add_another_occurrence = resword.getString("add_another_occurrence");
        String click_for_more_options = resword.getString("click_for_more_options");
        String schedule = resword.getString("schedule");
        String view = resword.getString("view") + "/" + resword.getString("enter_data");
        String edit = resword.getString("edit");
        String remove = resword.getString("remove");
        String delete = resword.getString("delete");
        String reassign = resword.getString("reassign");
        String occurrence_x_of = resword.getString("ocurrence");
        String subjectText = resword.getString("subject");
        String eventText = resword.getString("event");
        String signText = resword.getString("signed");
        String lockText = resword.getString("locked");
        String archivedText = resword.getString("archived");
        String restoreText = resword.getString("restore");

        StudyEventWorkflowStatusEnum eventStatus = studyEvents.size() == 0 ? StudyEventWorkflowStatusEnum.NOT_SCHEDULED : studyEvents.get(0).getWorkflowStatus();
        String studyEventName = studyEvents.size() == 0 ? "" : studyEvents.get(0).getName();
        String studyEventId = studyEvents.size() == 0 ? "" : String.valueOf(studyEvents.get(0).getId());
        Status eventSysStatus = studySubject.getStatus();
        String studySubjectLabel = studySubject.getLabel();

        eventDiv.td(0).styleClass(tableHeaderRowLeftStyleClass).close();
        eventDiv.append(subjectText).append(": ").append(studySubjectLabel).br();
        eventDiv.append(eventText).append(": ").append(sed.getName()).br();

        eventDiv.tdEnd();
        eventDiv.td(0).styleClass(tableHeaderRowLeftStyleClass).align("right").close();
        linkBuilder(eventDiv, studySubjectLabel, rowCount, studyEvents, sed);
        eventDiv.tdEnd();

        eventDiv.tr(0).valign("top").close();
        eventDiv.td(0).styleClass(tableHeaderRowStyleClass).colspan("2").close();

        if (!sed.isRepeating()) {
            if (studyEvents.size() > 0 && studyEvents.get(0).getDateStarted() != null) {
                eventDiv.bold().append(formatDate(studyEvents.get(0).getDateStarted())).boldEnd();
            }
            if (studyEvents.size() > 0 && studyEvents.get(0).isRemoved()) {
                eventDiv.br().bold().append(EventCrfStatusEnum.REMOVED.toString().toLowerCase()).br();
            } else {
                eventDiv.br().bold().append(eventStatus.getDisplayValue()).br();
            }
            if (studyEvents.size() > 0) {
                if (studyEvents.get(0).isSigned()) {
                    eventDiv.append("<span class=\"icon icon-stamp-new status\" alt=" + signText + " title=" + signText + " style=\"margin-right: 5px;\"></span>");
                }
                if (studyEvents.get(0).isLocked()) {
                    eventDiv.append("<span class=\"icon icon-lock-new status\" alt=" + lockText + " title=" + lockText + " style=\"margin-right: 5px;\"></span>");
                }
                if (studyEvents.get(0).isArchived()) {
                    eventDiv.append("<span class=\"icon icon-archived-new status\" alt=" + archivedText + " title=" + archivedText + "></span>");
                }
            }
            eventDiv.boldEnd().tdEnd().trEnd(0);

        } else {
            eventDiv.bold().append(occurrence_x_of).append(" 1 of 1").br();
            if (studyEvents.size() > 0) {
                if (studyEvents.get(0).getDateStarted() != null)
                    eventDiv.append(formatDate(studyEvents.get(0).getDateStarted())).br();
                if (studyEvents.get(0).isRemoved()) {
                    eventDiv.append(EventCrfStatusEnum.REMOVED.toString().toLowerCase()).br();
                } else {
                    eventDiv.append(studyEvents.get(0).getWorkflowStatus().getDisplayValue()).br();
                }
                if (studyEvents.get(0).isSigned()) {
                    eventDiv.append("<span class=\"icon icon-stamp-new status\" alt=" + signText + " title=" + signText + " style=\"margin-right: 5px;\"></span>");
                }
                if (studyEvents.get(0).isLocked()) {
                    eventDiv.append("<span class=\"icon icon-lock-new status\" alt=" + lockText + " title=" + lockText + " style=\"margin-right: 5px;\"></span>");
                }
                if (studyEvents.get(0).isArchived()) {
                    eventDiv.append("<span class=\"icon icon-archived-new status\" alt=" + archivedText + " title=" + archivedText + "></span>");
                }
            } else {
                eventDiv.append(StudyEventWorkflowStatusEnum.NOT_SCHEDULED.getDisplayValue());
            }
            eventDiv.boldEnd().tdEnd().trEnd(0);
            if (studyBean.getStatus() == core.org.akaza.openclinica.domain.Status.AVAILABLE) {
                eventDiv.tr(0).close().td(0).styleClass("table_cell_left").close();
                eventDiv.ahref("CreateNewStudyEvent?studySubjectId=" + studySubject.getId() + "&studyEventDefinition=" + sed.getId(), add_another_occurrence);
                eventDiv.tdEnd().trEnd(0);
            }

        }
        eventDiv.trEnd(0);
        eventDiv.tr(0).id("Menu_off_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount).style("display: all").close();
        eventDiv.td(0).styleClass("table_cell_left").colspan("2").close().append("<i>").append(click_for_more_options).append("</i>").tdEnd();
        eventDiv.trEnd(0);

        eventDiv.tr(0).id("Menu_on_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount).style("display: none").close();
        eventDiv.td(0).colspan("2").close();
        eventDiv.table(0).border("0").cellpadding("0").cellspacing("0").width("100%").close();

        if (eventSysStatus.getId() == Status.AVAILABLE.getId() || eventSysStatus == Status.SIGNED) {

            if (eventStatus.equals(StudyEventWorkflowStatusEnum.NOT_SCHEDULED) && currentRole.getRole() != Role.MONITOR && !studyBean.getStatus().isFrozen()) {
                eventDiv.tr(0).valign("top").close();
                eventDiv.td(0).styleClass("table_cell_left").close();
                createNewStudyEventLinkBuilder(eventDiv, studySubject.getId(), sed, schedule);
                eventDiv.tdEnd().trEnd(0);
            }

            else if (eventStatus.equals(StudyEventWorkflowStatusEnum.COMPLETED)) {
                eventDiv.tr(0).valign("top").close();
                eventDiv.td(0).styleClass("table_cell_left").close();
                enterDataForStudyEventLinkBuilder(eventDiv, studyEventId, view);
                eventDiv.tdEnd().trEnd(0);
                if ((currentRole.getRole() == Role.STUDYDIRECTOR || currentUser.isSysAdmin()) && studyBean.getStatus() == core.org.akaza.openclinica.domain.Status.AVAILABLE
                        && currentRole.getRole() != Role.MONITOR) {
                    eventDiv.tr(0).valign("top").close();
                    eventDiv.td(0).styleClass("table_cell_left").close();
                    updateStudyEventLinkBuilder(eventDiv, studySubject.getId(), studyEventId, edit);
                    eventDiv.tdEnd().trEnd(0);
                    eventDiv.tr(0).valign("top").close();
                    eventDiv.td(0).styleClass("table_cell_left").close();
                    removeStudyEventLinkBuilder(eventDiv, studySubject.getId(), studyEventId, remove);
                    eventDiv.tdEnd().trEnd(0);
                    eventDiv.tr(0).valign("top").close();
                    eventDiv.td(0).styleClass("table_cell").close();
                    reassignStudyEventLinkBuilder(eventDiv, studySubject.getId(), studyEventId, reassign);
                    eventDiv.tdEnd().trEnd(0);
                }
            }

            else if (studyEvents.size()>0 &&  studyEvents.get(0).isLocked()) {
                eventDiv.tdEnd().trEnd(0);
                if (currentRole.getRole() == Role.STUDYDIRECTOR || currentUser.isSysAdmin()) {
                    eventDiv.tr(0).valign("top").close();
                    eventDiv.td(0).styleClass("table_cell_left").close();
                    enterDataForStudyEventLinkBuilder(eventDiv, studyEventId, view);
                    eventDiv.tdEnd().trEnd(0);
                    if (studyBean.getStatus() == core.org.akaza.openclinica.domain.Status.AVAILABLE) {
                        eventDiv.tr(0).valign("top").close();
                        eventDiv.td(0).styleClass("table_cell_left").close();
                        removeStudyEventLinkBuilder(eventDiv, studySubject.getId(), studyEventId, remove);
                        eventDiv.tdEnd().trEnd(0);
                        eventDiv.tr(0).valign("top").close();
                        eventDiv.td(0).styleClass("table_cell").close();
                        reassignStudyEventLinkBuilder(eventDiv, studySubject.getId(), studyEventId, reassign);
                        eventDiv.tdEnd().trEnd(0);
                    }
                }
            } else {
                eventDiv.tr(0).valign("top").close();
                eventDiv.td(0).styleClass("table_cell_left").close();
                enterDataForStudyEventLinkBuilder(eventDiv, studyEventId, view);
                eventDiv.tdEnd().trEnd(0);

                if (studyEvents.size() > 0 &&  studyEvents.get(0).isRemoved()) {
                    eventDiv.tr(0).valign("top").close();
                    eventDiv.td(0).styleClass("table_cell_left").close();
                    eventDiv.append(restoreStudySubjectLinkBuilder(studySubject, restoreText));
                    eventDiv.tdEnd().trEnd(0);
                } else {
                    eventDiv.tr(0).valign("top").close();
                    eventDiv.td(0).styleClass("table_cell_left").close();
                    updateStudyEventLinkBuilder(eventDiv, studySubject.getId(), studyEventId, edit);
                    eventDiv.tdEnd().trEnd(0);
                    if ((currentRole.getRole() == Role.STUDYDIRECTOR || currentUser.isSysAdmin()) && studyBean.getStatus() == core.org.akaza.openclinica.domain.Status.AVAILABLE
                            && currentRole.getRole() != Role.MONITOR && !eventStatus.equals(StudyEventWorkflowStatusEnum.SCHEDULED)) {
                        eventDiv.tr(0).valign("top").close();
                        eventDiv.td(0).styleClass("table_cell_left").close();
                        removeStudyEventLinkBuilder(eventDiv, studySubject.getId(), studyEventId, remove);
                        eventDiv.tdEnd().trEnd(0);
                        eventDiv.tr(0).valign("top").close();
                        eventDiv.td(0).styleClass("table_cell").close();
                        reassignStudyEventLinkBuilder(eventDiv, studySubject.getId(), studyEventId, reassign);
                        eventDiv.tdEnd().trEnd(0);
                    }
                }
            }
        }

        if (eventSysStatus == Status.DELETED || eventSysStatus == Status.AUTO_DELETED) {
            eventDiv.tr(0).valign("top").close();
            eventDiv.td(0).styleClass("table_cell_left").close();
            enterDataForStudyEventLinkBuilder(eventDiv, studyEventId, view);
            eventDiv.tdEnd().trEnd(0);
        }
        eventDiv.tableEnd(0).tdEnd().trEnd(0);

        eventDiv.tableEnd(0);
        eventDiv.divEnd().divEnd().divEnd().divEnd().divEnd().divEnd().divEnd().divEnd().divEnd().divEnd().divEnd();
        iconLinkBuilder(eventDiv, studySubjectLabel, rowCount, studyEvents, sed);

    }

    private void updateStudyEventLinkBuilder(HtmlBuilder builder, Integer studySubjectId, String studyEventId, String edit) {
        String href1 = "UpdateStudyEvent?event_id=" + studyEventId + "&ss_id=" + studySubjectId;
        builder.a().href(href1);
        builder.close();
        builder.append("<span border=\"0\" align=\"left\" class=\"icon icon-pencil\"/>");
        builder.nbsp().nbsp().a().href(href1);
        builder.close().append(edit).aEnd();

    }

    private void removeStudyEventLinkBuilder(HtmlBuilder builder, Integer studySubjectId, String studyEventId, String remove) {
        String href1 = "RemoveStudyEvent?action=confirm&id=" + studyEventId + "&studySubId=" + studySubjectId;
        builder.a().href(href1);
        builder.close();
        builder.append("<span border=\"0\" align=\"left\" class=\"icon icon-cancel\"/>");
        builder.nbsp().nbsp().a().href(href1);
        builder.close().append(remove).aEnd();

    }

    private void reassignStudyEventLinkBuilder(HtmlBuilder builder, Integer studySubjectId, String studyEventId, String remove) {
        String href1 = "ReassignStudySubject?action=confirm&id=" + studyEventId + "&studySubId=" + studySubjectId;
        builder.a().href(href1);
        builder.close();
        builder.append("<span border=\"0\" align=\"left\" class=\"icon icon-icon-reassign3\"/>");
        builder.nbsp().nbsp().a().href(href1);
        builder.close().append(remove).aEnd();

    }

    private void createNewStudyEventLinkBuilder(HtmlBuilder builder, Integer studySubjectId, StudyEventDefinitionBean sed, String schedule) {
        String href1 = "CreateNewStudyEvent?studySubjectId=" + studySubjectId + "&studyEventDefinition=" + sed.getId();
        builder.a().href(href1);
        builder.close();
        builder.append("<span border=\"0\" align=\"left\" class=\"icon icon-clock2\"/>");
        builder.nbsp().nbsp().a().href(href1);
        builder.close().append(schedule).aEnd();

    }

    private void enterDataForStudyEventLinkBuilder(HtmlBuilder builder, String studyEventId, String view) {
        String href1 = "EnterDataForStudyEvent?eventId=" + studyEventId;
        builder.append("<a href=\"" + href1 + "\" >" );
        builder.append("<span class=\"icon icon-search\"/></a>");
        builder.nbsp().nbsp().a().href(href1);
        builder.close().append(view).aEnd();
    }

    private void lockLinkBuilder(HtmlBuilder builder, String studySubjectLabel, Integer rowCount, List<StudyEventBean> studyEvents,
                                 StudyEventDefinitionBean sed) {
        studySubjectLabel = studySubjectLabel.replaceAll("'", "\\\\'");
        String href1 = "javascript:leftnavExpand('Menu_on_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        String href2 = "javascript:leftnavExpand('Menu_off_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        String onmouseover = "layersShowOrHide('visible','Event_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        onmouseover += "javascript:setImage('ExpandIcon_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "','images/icon_collapse.gif');";
        String onClick1 = "layersShowOrHide('hidden','Lock_all'); ";
        String onClick2 = "layersShowOrHide('hidden','Event_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        String onClick3 = "layersShowOrHide('hidden','Lock_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        String onClick4 = "javascript:setImage('ExpandIcon_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "','images/icon_blank.gif'); ";
        builder.a().href(href1 + href2);
        builder.onclick(onmouseover + onClick1 + onClick2 + onClick3 + onClick4);
        builder.close();
        builder.img().src("images/spacer.gif").border("0").append("height=\"30\"").width("50").close().aEnd();

    }

    private void repeatingLockLinkBuilder(HtmlBuilder builder, String studySubjectLabel, Integer rowCount, List<StudyEventBean> studyEvents,
                                          StudyEventDefinitionBean sed) {
        studySubjectLabel = studySubjectLabel.replaceAll("'", "\\\\'");
        String href1 = "javascript:ExpandEventOccurrences('" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'," + studyEvents.size() + "); ";
        String href2 = "javascript:leftnavExpand('Menu_off_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        String onmouseover = "layersShowOrHide('visible','Event_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        onmouseover += "javascript:setImage('ExpandIcon_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "','images/icon_collapse.gif');";
        String onClick1 = "layersShowOrHide('hidden','Lock_all'); ";
        String onClick2 = "layersShowOrHide('hidden','Event_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        String onClick3 = "layersShowOrHide('hidden','Lock_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        String onClick4 = "javascript:setImage('ExpandIcon_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "','images/icon_blank.gif'); ";
        builder.a().href(href1 + href2);
        builder.onclick(onmouseover + onClick1 + onClick2 + onClick3 + onClick4);
        builder.close();
        builder.img().src("images/spacer.gif").border("0").append("height=\"30\"").width("50").close().aEnd();

    }

    private void repeatingIconLinkBuilder(HtmlBuilder builder, String studySubjectLabel, Integer rowCount, List<StudyEventBean> studyEvents,
                                          StudyEventDefinitionBean sed) {
        studySubjectLabel = studySubjectLabel.replaceAll("'", "\\\\'");
        String onclick = "moveObject('Event_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "', event); ";
        builder.a().onclick(onclick).style("cursor: pointer;");
        builder.close();
    }

    private void iconLinkBuilder(HtmlBuilder builder, String studySubjectLabel, Integer rowCount, List<StudyEventBean> studyEvents,
                                 StudyEventDefinitionBean sed) {
        studySubjectLabel = studySubjectLabel.replaceAll("'", "\\\\'");
        String href1Repeating = "javascript:ExpandEventOccurrences('" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'," + studyEvents.size()
                + "); ";
        String href1 = "javascript:leftnavExpand('Menu_on_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        String href2 = "javascript:leftnavExpand('Menu_off_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        String onmouseover = "moveObject('Event_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "', event); ";
        onmouseover += "setImage('ExpandIcon_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "','images/icon_expand.gif');";
        String onmouseout = "layersShowOrHide('hidden','Event_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        onmouseout += "setImage('ExpandIcon_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "','images/icon_blank.gif');";
        String onClick1 = "layersShowOrHide('visible','Lock_all'); ";
        String onClick2 = "LockObject('Lock_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "',event); ";
        String href = studyEvents.size() > 1 ? href1Repeating + href2 : href1 + href2;
        builder.a().href(href);
        // builder.onmouseover(onmouseover);
        // builder.onmouseout(onmouseout);
        builder.onclick(onmouseover + onClick1 + onClick2);
        builder.close();

    }

    private void divCloseRepeatinglinkBuilder(HtmlBuilder builder, String studySubjectLabel, Integer rowCount, List<StudyEventBean> studyEvents,
                                              StudyEventDefinitionBean sed) {
        studySubjectLabel = studySubjectLabel.replaceAll("'", "\\\\'");
        String onClick1 = "javascript:layersShowOrHide('hidden','Lock_all'); ";
        String onClick2 = "layersShowOrHide('hidden','Event_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        String onClick3 = "layersShowOrHide('hidden','Lock_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        String onClick4 = "setImage('ExpandIcon_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "','images/icon_blank.gif'); ";
        builder.a().href(onClick1);
        builder.onclick(onClick2 + onClick3 + onClick4);
        builder.close().append("X").aEnd();

    }

    private void linkBuilder(HtmlBuilder builder, String studySubjectLabel, Integer rowCount, List<StudyEventBean> studyEvents, StudyEventDefinitionBean sed) {
        studySubjectLabel = studySubjectLabel.replaceAll("'", "\\\\'");
        String href1 = "javascript:leftnavExpand('Menu_on_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        String href2 = "javascript:leftnavExpand('Menu_off_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        String onClick1 = "layersShowOrHide('hidden','Lock_all'); ";
        String onClick2 = "layersShowOrHide('hidden','Event_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        String onClick3 = "layersShowOrHide('hidden','Lock_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        String onClick4 = "setImage('ExpandIcon_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "','images/icon_blank.gif'); ";
        builder.a().href(href1 + href2);
        builder.onclick(onClick1 + onClick2 + onClick3 + onClick4);
        builder.close().append("X").aEnd();

    }

    private String formatDate(Date date) {
        if (date == null)
            return "";
        String format = resformat.getString("date_format_string");
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    private String[] getStringArray(List<String> userStatuses) {
        if (CollectionUtils.isEmpty(userStatuses))
            return null;
        String[] userStatusArray = userStatuses.toArray(new String[userStatuses.size()]);
        return userStatusArray;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public ViewStudySubjectService getViewStudySubjectService() {
        return viewStudySubjectService;
    }

    public void setViewStudySubjectService(ViewStudySubjectService viewStudySubjectService) {
        this.viewStudySubjectService = viewStudySubjectService;
    }

    public void setItemDataDao(ItemDataDao itemDataDao) {
        this.itemDataDao = itemDataDao;
    }

    public void setItemDao(ItemDao itemDao) {
        this.itemDao = itemDao;
    }

    public void setItemFormMetadataDao(ItemFormMetadataDao itemFormMetadataDao) {
        this.itemFormMetadataDao = itemFormMetadataDao;
    }

    public void setResponseSetDao(ResponseSetDao responseSetDao) {
        this.responseSetDao = responseSetDao;
    }

    public void setEventCrfDao(EventCrfDao eventCrfDao) {
        this.eventCrfDao = eventCrfDao;
    }

    public void setStudyEventDao(StudyEventDao studyEventDao) {
        this.studyEventDao = studyEventDao;
    }

    public void setCrfDao(CrfDao crfDao) {
        this.crfDao = crfDao;
    }


    public void setCrfVersionDao(CrfVersionDao crfVersionDao) {
        this.crfVersionDao = crfVersionDao;
    }

    public void setEventDefinitionCrfDao(EventDefinitionCrfDao eventDefinitionCrfDao) {
        this.eventDefinitionCrfDao = eventDefinitionCrfDao;
    }

    public void setStudyEventDefinitionHibDao(StudyEventDefinitionDao studyEventDefinitionHibDao) {
        this.studyEventDefinitionHibDao = studyEventDefinitionHibDao;
    }

    public void setPermissionTagDao(EventDefinitionCrfPermissionTagDao permissionTagDao) {
        this.permissionTagDao = permissionTagDao;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public void setStudySubjectDao(StudySubjectDao studySubjectDao) {
        this.studySubjectDao = studySubjectDao;
    }

    private String validateProperty(String property) {
        if (property.startsWith("SE_") && property.contains(".F_") && property.contains(".I_")) {
            String itemOid = property.split("\\.")[2];
            Item item = itemDao.findByOcOID(itemOid);
            if (item != null)
                property = property + "." + item.getItemDataType().getName();
        }
        return property;

    }
}