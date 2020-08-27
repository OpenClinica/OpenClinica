package org.akaza.openclinica.control.managestudy;

import core.org.akaza.openclinica.bean.admin.CRFBean;
import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.bean.core.Status;
import core.org.akaza.openclinica.bean.core.SubjectEventStatus;
import core.org.akaza.openclinica.bean.login.StudyUserRoleBean;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.*;
import core.org.akaza.openclinica.bean.submit.*;
import core.org.akaza.openclinica.dao.admin.CRFDAO;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.dao.managestudy.*;
import core.org.akaza.openclinica.dao.submit.*;
import core.org.akaza.openclinica.domain.EventCrfStatusEnum;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.control.AbstractTableFactory;
import org.akaza.openclinica.control.DefaultActionsEditor;
import org.akaza.openclinica.domain.enumsupport.EventCrfWorkflowStatusEnum;
import org.akaza.openclinica.domain.enumsupport.StudyEventWorkflowStatusEnum;
import org.apache.commons.lang.StringUtils;
import org.jmesa.core.filter.FilterMatcher;
import org.jmesa.core.filter.MatcherKey;
import org.jmesa.facade.TableFacade;
import org.jmesa.limit.*;
import org.jmesa.view.component.Row;
import org.jmesa.view.editor.BasicCellEditor;
import org.jmesa.view.editor.CellEditor;
import org.jmesa.view.html.HtmlBuilder;
import org.jmesa.view.html.editor.DroplistFilterEditor;

import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.*;

public class ListEventsForSubjectTableFactory extends AbstractTableFactory {

    private StudyEventDefinitionDAO studyEventDefinitionDao;
    private StudySubjectDAO studySubjectDAO;
    private SubjectDAO subjectDAO;
    private StudyEventDAO studyEventDAO;
    private StudyGroupClassDAO studyGroupClassDAO;
    private SubjectGroupMapDAO subjectGroupMapDAO;
    private StudyGroupDAO studyGroupDAO;
    private EventCRFDAO eventCRFDAO;
    private EventDefinitionCRFDAO eventDefintionCRFDAO;
    private CRFDAO crfDAO;
    private CRFVersionDAO crfVersionDAO;
    private FormLayoutDAO formLayoutDAO;
    private final String COMMON = "common";

    public StudyDao studyDao;

    public CRFVersionDAO getCrfVersionDAO() {
        return crfVersionDAO;
    }

    public void setCrfVersionDAO(CRFVersionDAO crfVersionDAO) {
        this.crfVersionDAO = crfVersionDAO;
    }

    private Study studyBean;
    private String[] columnNames = new String[]{};
    private ArrayList<StudyEventDefinitionBean> studyEventDefinitions;
    private ArrayList<CRFBean> crfBeans;
    private ArrayList<EventDefinitionCRFBean> eventDefinitionCrfs;
    private ArrayList<StudyGroupClassBean> studyGroupClasses;
    private StudyUserRoleBean currentRole;
    private UserAccountBean currentUser;
    private boolean showMoreLink;
    private ResourceBundle resword;
    private ResourceBundle resformat;
    private StudyEventDefinitionBean selectedStudyEventDefinition;

    final HashMap<String, String> imageIconPaths = new HashMap<String, String>(8);
    final HashMap<String, String> crfColumnImageIconPaths = new HashMap<String, String>(8);

    public ListEventsForSubjectTableFactory(boolean showMoreLink) {
        imageIconPaths.put(StudyEventWorkflowStatusEnum.NOT_SCHEDULED.toString(), "icon icon-clock");
        imageIconPaths.put(StudyEventWorkflowStatusEnum.SCHEDULED.toString(), "icon icon-clock2");
        imageIconPaths.put(StudyEventWorkflowStatusEnum.DATA_ENTRY_STARTED.toString(), "icon icon-pencil-squared orange");
        imageIconPaths.put(StudyEventWorkflowStatusEnum.COMPLETED.toString(), "icon icon-checkbox-checked green");
        imageIconPaths.put(StudyEventWorkflowStatusEnum.STOPPED.toString(), "icon icon-stop-circle red");
        imageIconPaths.put(StudyEventWorkflowStatusEnum.SKIPPED.toString(), "icon icon-redo");
        imageIconPaths.put(EventCrfStatusEnum.REMOVED.toString(), "icon icon-file-excel red");

        //  crfColumnImageIconPaths.put(0, "icon icon-file-excel red");
        crfColumnImageIconPaths.put(EventCrfWorkflowStatusEnum.NOT_STARTED.toString(), "icon icon-doc");
        crfColumnImageIconPaths.put(EventCrfWorkflowStatusEnum.INITIAL_DATA_ENTRY.toString(), "icon icon-pencil-squared orange");
        crfColumnImageIconPaths.put(EventCrfWorkflowStatusEnum.COMPLETED.toString(), "icon icon-checkbox-checked green");
        crfColumnImageIconPaths.put(EventCrfWorkflowStatusEnum.LOCKED.toString(), "icon icon-lock");
        crfColumnImageIconPaths.put(EventCrfStatusEnum.REMOVED.toString(), "icon icon-file-excel red");
        this.showMoreLink = showMoreLink;
    }

    @Override
    protected String getTableName() {
        return "listEventsForSubject";
    }

    @Override
    protected void configureColumns(TableFacade tableFacade, Locale locale) {
        resword = ResourceBundleProvider.getWordsBundle(locale);
        resformat = ResourceBundleProvider.getFormatBundle(locale);

        tableFacade.setColumnProperties(columnNames);
        Row row = tableFacade.getTable().getRow();
        int index = 0;
        configureColumn(row.getColumn(columnNames[index]), resword.getString("study_subject_ID"), null, null);
        ++index;
        configureColumn(row.getColumn(columnNames[index]), resword.getString("subject_status"), new StatusCellEditor(), new StatusDroplistFilterEditor());
        ++index;
        configureColumn(row.getColumn(columnNames[index]), resword.getString("site_id"), null, null);
        ++index;
        configureColumn(row.getColumn(columnNames[index]), resword.getString("gender"), null, null, true, false);
        ++index;

        // group class columns
        for (int i = index; i < index + studyGroupClasses.size(); i++) {
            StudyGroupClassBean studyGroupClass = studyGroupClasses.get(i - index);
            configureColumn(row.getColumn(columnNames[i]), studyGroupClass.getName(), new StudyGroupClassCellEditor(studyGroupClass),
                    new SubjectGroupClassDroplistFilterEditor(studyGroupClass), true, false);
        }

        configureColumn(row.getColumn(columnNames[index + studyGroupClasses.size()]), resword.getString("event_status"), new EventStatusCellEditor(),
                new StudyEventWorkflowStatusDroplistFilterEditor(), true, false);
        ++index;
        configureColumn(row.getColumn(columnNames[index + studyGroupClasses.size()]), resword.getString("event_date_started"), new EventStartDateCellEditor(), null);
        ++index;

        // crf columns
        for (int i = index + studyGroupClasses.size(); i < columnNames.length - 1; i++) {
            CRFBean crfBean = crfBeans.get(i - (index + studyGroupClasses.size()));
            configureColumn(row.getColumn(columnNames[i]), crfBean.getName(), new EventCrfCellEditor(), new EventCrfWorkflowStatusDroplistFilterEditor(), true,
                    false);
        }

        // actions column
        String actionsHeader = resword.getString("rule_actions") + "&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;";
        configureColumn(row.getColumn(columnNames[columnNames.length - 1]), actionsHeader, new ActionsCellEditor(), new DefaultActionsEditor(locale), true,
                false);
    }

    @Override
    public void configureTableFacade(HttpServletResponse response, TableFacade tableFacade) {
        super.configureTableFacade(response, tableFacade);
        getColumnNamesMap();
        tableFacade.addFilterMatcher(new MatcherKey(Character.class), new CharFilterMatcher());
        tableFacade.addFilterMatcher(new MatcherKey(Status.class), new StatusFilterMatcher());

        tableFacade.addFilterMatcher(new MatcherKey(String.class, "event.status"), new SubjectEventStatusFilterMatcher());

        // subject group class filter matcher
        for (int i = 4; i < 4 + studyGroupClasses.size(); i++) {
            tableFacade.addFilterMatcher(new MatcherKey(String.class, columnNames[i]), new SubjectGroupFilterMatcher());
        }

        // crf columns filtering
        for (int i = 6 + studyGroupClasses.size(); i < columnNames.length - 1; i++) {
            tableFacade.addFilterMatcher(new MatcherKey(String.class, columnNames[i]), new SubjectEventCRFStatusFilterMatcher());
        }
    }

    @Override
    public void configureTableFacadePostColumnConfiguration(TableFacade tableFacade) {
        Role r = currentRole.getRole();
        boolean addSubjectLinkShow = studyBean.getStatus().isAvailable() && !r.equals(Role.MONITOR);
        tableFacade.setToolbar(new ListEventsForSubjectTableToolbar(getStudyEventDefinitions(), getStudyGroupClasses(), selectedStudyEventDefinition,
                addSubjectLinkShow, showMoreLink));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setDataAndLimitVariables(TableFacade tableFacade) {
        Limit limit = tableFacade.getLimit();

        ListEventsForSubjectFilter eventsForSubjectFilter = getListEventsForSubjectFilter(limit);

        if (!limit.isComplete()) {
            int totalRows = getStudySubjectDAO().getCountWithFilter(eventsForSubjectFilter, getStudyBean());
            tableFacade.setTotalRows(totalRows);
        }

        ListEventsForSubjectSort eventsForSubjectSort = getListEventsForSubjectSort(limit);

        int rowStart = limit.getRowSelect().getRowStart();
        int rowEnd = limit.getRowSelect().getRowEnd();
        Collection<StudySubjectBean> items = getStudySubjectDAO().getWithFilterAndSort(getStudyBean(), eventsForSubjectFilter, eventsForSubjectSort, rowStart,
                rowEnd);
        Collection<HashMap<Object, Object>> theItems = new ArrayList<HashMap<Object, Object>>();

        for (StudySubjectBean studySubjectBean : items) {
            HashMap<Object, Object> theItem = new HashMap<Object, Object>();
            theItem.put("studySubject", studySubjectBean);
            theItem.put("studySubject.label", studySubjectBean.getLabel());
            theItem.put("studySubject.status", studySubjectBean.getStatus());
            Study study = studyDao.findByPK(studySubjectBean.getStudyId());
            theItem.put("enrolledAt", study.getUniqueIdentifier());

            SubjectBean subjectBean = (SubjectBean) getSubjectDAO().findByPK(studySubjectBean.getSubjectId());
            theItem.put("subject", subjectBean);
            theItem.put("subject.charGender", subjectBean.getGender());

            // study group classes
            SubjectGroupMapBean subjectGroupMapBean = new SubjectGroupMapBean();
            for (StudyGroupClassBean studyGroupClass : getStudyGroupClasses()) {
                subjectGroupMapBean = getSubjectGroupMapDAO().findAllByStudySubjectAndStudyGroupClass(studySubjectBean.getId(), studyGroupClass.getId());
                if (null != subjectGroupMapBean) {
                    theItem.put("sgc_" + studyGroupClass.getId(), subjectGroupMapBean.getStudyGroupId());
                    theItem.put("grpName_sgc_" + studyGroupClass.getId(), subjectGroupMapBean.getStudyGroupName());
                }
            }
            subjectGroupMapBean = null;

            // Get EventCrfs for study Subject
            List<EventCRFBean> eventCrfs = getEventCRFDAO().findAllByStudySubject(studySubjectBean.getId());
            HashMap<String, EventCRFBean> crfAsKeyEventCrfAsValue = new HashMap<String, EventCRFBean>();
            for (EventCRFBean eventCRFBean : eventCrfs) {
                CRFBean crf = getCrfDAO().findByVersionId(eventCRFBean.getCRFVersionId());
                crfAsKeyEventCrfAsValue.put(crf.getId() + "_" + eventCRFBean.getStudyEventId(), eventCRFBean);
            }

            // Get the event Status
            List<StudyEventBean> eventsForStudySubjectAndEventDefinitions = getStudyEventDAO().findAllByDefinitionAndSubject(selectedStudyEventDefinition,
                    studySubjectBean);
            List<DisplayBean> events = new ArrayList<DisplayBean>();
            // study event size < 1
            if (eventsForStudySubjectAndEventDefinitions.size() < 1) {
                DisplayBean d = new DisplayBean();
                d.getProps().put("event", null);
                d.getProps().put("event.status", StudyEventWorkflowStatusEnum.NOT_SCHEDULED);
                d.getProps().put("studySubject.createdDate", null);
                for (int i = 0; i < getCrfs(selectedStudyEventDefinition).size(); i++) {
                    CRFBean crf = getCrfs(selectedStudyEventDefinition).get(i);
                    d.getProps().put("crf_" + crf.getId(), EventCrfWorkflowStatusEnum.NOT_STARTED);
                    d.getProps().put("crf_" + crf.getId() + "_eventCrf", null);
                    d.getProps().put("crf_" + crf.getId() + "_crf", crf);
                    // d.getProps().put("crf_" + crf.getId() + "_eventDefinitionCrf", eventDefinitionCrfs.get(i));
                    d.getProps().put("crf_" + crf.getId() + "_eventDefinitionCrf",
                            getEventDefinitionCRFBean(selectedStudyEventDefinition.getId(), crf, studySubjectBean));
                    theItem.put("crf_" + crf.getId(), "");
                }
                events.add(d);
            }
            // study event size >0
            for (StudyEventBean studyEventBean : eventsForStudySubjectAndEventDefinitions) {
                DisplayBean d = new DisplayBean();
                d.getProps().put("event", studyEventBean);
                d.getProps().put("event.status", studyEventBean.getWorkflowStatus());
                d.getProps().put("studySubject.createdDate", studyEventBean.getDateStarted());
                for (int i = 0; i < getCrfs(selectedStudyEventDefinition).size(); i++) {
                    CRFBean crf = getCrfs(selectedStudyEventDefinition).get(i);
                    EventCRFBean eventCRFBean = crfAsKeyEventCrfAsValue.get(crf.getId() + "_" + studyEventBean.getId());
                    if (eventCRFBean != null) {
                        d.getProps().put("crf_" + crf.getId() + "_eventCrf", eventCRFBean);
                        FormLayoutBean formLayoutBean = (FormLayoutBean) formLayoutDAO.findByPK(eventCRFBean.getFormLayoutId());
                        if (formLayoutBean.getStatus().equals(Status.LOCKED)) {
                            d.getProps().put("crf_" + crf.getId(), EventCrfWorkflowStatusEnum.LOCKED);
                        } else {
                            d.getProps().put("crf_" + crf.getId(), eventCRFBean.getWorkflowStatus());
                        }

                    } else {
                        d.getProps().put("crf_" + crf.getId(), EventCrfWorkflowStatusEnum.NOT_STARTED);
                        d.getProps().put("crf_" + crf.getId() + "_eventCrf", null);
                    }
                    d.getProps().put("crf_" + crf.getId() + "_crf", crf);
                    // d.getProps().put("crf_" + crf.getId() + "_eventDefinitionCrf", eventDefinitionCrfs.get(i));
                    d.getProps().put("crf_" + crf.getId() + "_eventDefinitionCrf",
                            getEventDefinitionCRFBean(selectedStudyEventDefinition.getId(), crf, studySubjectBean));

                    theItem.put("crf_" + crf.getId(), "");
                }
                events.add(d);
            }
            theItem.put("events", events);
            theItem.put("event.status", "");
            theItem.put("studySubject.createdDate", "");
            theItem.put("webappContext", tableFacade.getWebContext().getContextPath());
            theItems.add(theItem);
        }

        // Do not forget to set the items back on the tableFacade.
        tableFacade.setItems(theItems);

    }

    private EventDefinitionCRFBean getEventDefinitionCRFBean(Integer studyEventDefinitionId, CRFBean crfBean, StudySubjectBean studySubject) {
        EventDefinitionCRFBean eventDefinitionCrf = getEventDefintionCRFDAO().findByStudyEventDefinitionIdAndCRFIdAndStudyId(studyEventDefinitionId,
                crfBean.getId(), studySubject.getStudyId());
        if (eventDefinitionCrf.getId() == 0) {
            eventDefinitionCrf = getEventDefintionCRFDAO().findForStudyByStudyEventDefinitionIdAndCRFId(studyEventDefinitionId, crfBean.getId());
        }
        FormLayoutBean defaultVersion = (FormLayoutBean) getFormLayoutDAO().findByPK(eventDefinitionCrf.getDefaultVersionId());
        eventDefinitionCrf.setDefaultCRF(defaultVersion);
        //set versions
        ArrayList<FormLayoutBean> versions = (ArrayList<FormLayoutBean>) getFormLayoutDAO().findAllActiveByCRF(eventDefinitionCrf.getCrfId());
        eventDefinitionCrf.setVersions(versions);
        return eventDefinitionCrf;
    }

    private void getColumnNamesMap() {
        ArrayList<String> columnNamesList = new ArrayList<String>();
        columnNamesList.add("studySubject.label");
        columnNamesList.add("studySubject.status");
        columnNamesList.add("enrolledAt");
        columnNamesList.add("subject.charGender");

        for (StudyGroupClassBean studyGroupClass : getStudyGroupClasses()) {
            columnNamesList.add("sgc_" + studyGroupClass.getId());
        }
        columnNamesList.add("event.status");
        columnNamesList.add("studySubject.createdDate");

        for (CRFBean crfBean : getCrfs(selectedStudyEventDefinition)) {
            columnNamesList.add("crf_" + crfBean.getId());
        }
        columnNamesList.add("actions");
        columnNames = columnNamesList.toArray(columnNames);
    }

    protected ListEventsForSubjectFilter getListEventsForSubjectFilter(Limit limit) {
        ListEventsForSubjectFilter listEventsForSubjectFilter = new ListEventsForSubjectFilter(selectedStudyEventDefinition.getId());
        FilterSet filterSet = limit.getFilterSet();
        Collection<Filter> filters = filterSet.getFilters();
        for (Filter filter : filters) {
            String property = filter.getProperty();
            String value = filter.getValue();
            if ("studySubject.status".equalsIgnoreCase(property)) {
                value = Status.getByName(value).getId() + "";
            } else if ("event.status".equalsIgnoreCase(property)) {
                if (StudyEventWorkflowStatusEnum.getByI18nDescription(value) != null)
                    value = StudyEventWorkflowStatusEnum.getByI18nDescription(value) + "";
            } else if (property.startsWith("sgc_")) {
                int studyGroupClassId = property.endsWith("_") ? 0 : Integer.valueOf(property.split("_")[1]);
                value = studyGroupDAO.findByNameAndGroupClassID(value, studyGroupClassId).getId() + "";
            } else if (property.startsWith("crf_")) {
                value = EventCrfWorkflowStatusEnum.getByI18nDescription(value) + "";
            }
            listEventsForSubjectFilter.addFilter(property, value);
        }

        return listEventsForSubjectFilter;
    }

    protected ListEventsForSubjectSort getListEventsForSubjectSort(Limit limit) {
        ListEventsForSubjectSort listEventsForSubjectSort = new ListEventsForSubjectSort();
        SortSet sortSet = limit.getSortSet();
        Collection<Sort> sorts = sortSet.getSorts();
        for (Sort sort : sorts) {
            String property = sort.getProperty();
            String order = sort.getOrder().toParam();
            listEventsForSubjectSort.addSort(property, order);
        }

        return listEventsForSubjectSort;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<StudyEventDefinitionBean> getStudyEventDefinitions() {
        ArrayList<StudyEventDefinitionBean> tempList = new ArrayList<>();
        if (this.studyEventDefinitions == null) {
            if (studyBean.isSite()) {
                Study parentStudy = (Study) studyDao.findByPK(studyBean.getStudy().getStudyId());
                studyEventDefinitions = getStudyEventDefinitionDao().findAllActiveByStudy(parentStudy);
            } else {
                studyEventDefinitions = getStudyEventDefinitionDao().findAllActiveByStudy(studyBean);
            }
        }
        for (StudyEventDefinitionBean studyEventDefinition : this.studyEventDefinitions) {
            if (!studyEventDefinition.getType().equals(COMMON)) {
                tempList.add(studyEventDefinition);
            }
        }

        return tempList;
    }

    @SuppressWarnings("unchecked")
    private ArrayList<CRFBean> getCrfs(StudyEventDefinitionBean eventDefinition) {
        if (this.crfBeans == null) {
            crfBeans = new ArrayList<CRFBean>();
            eventDefinitionCrfs = new ArrayList<EventDefinitionCRFBean>();
            for (EventDefinitionCRFBean eventDefinitionCrf : (List<EventDefinitionCRFBean>) getEventDefintionCRFDAO()
                    .findAllActiveByEventDefinitionId(eventDefinition.getId())) {
                CRFBean crfBean = (CRFBean) getCrfDAO().findByPK(eventDefinitionCrf.getCrfId());
                ArrayList<CRFVersionBean> crfVersions = (ArrayList<CRFVersionBean>) getCrfVersionDAO().findAllByCRFId(eventDefinitionCrf.getCrfId());
                crfBean.setVersions(crfVersions);
                if (eventDefinitionCrf.getParentId() == 0) {
                    crfBeans.add(crfBean);
                    eventDefinitionCrfs.add(eventDefinitionCrf);
                }

            }
            return crfBeans;
        }
        return crfBeans;
    }

    @SuppressWarnings("unchecked")
    private ArrayList<StudyGroupClassBean> getStudyGroupClasses() {
        if (this.studyGroupClasses == null) {
            if (studyBean.isSite()) {
                Study parentStudy = (Study) studyDao.findByPK(studyBean.getStudy().getStudyId());
                studyGroupClasses = getStudyGroupClassDAO().findAllActiveByStudy(parentStudy);
            } else {
                studyGroupClasses = getStudyGroupClassDAO().findAllActiveByStudy(studyBean);
            }
        }
        return studyGroupClasses;
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

    public StudyGroupDAO getStudyGroupDAO() {
        return studyGroupDAO;
    }

    public void setStudyGroupDAO(StudyGroupDAO studyGroupDAO) {
        this.studyGroupDAO = studyGroupDAO;
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

    public CRFDAO getCrfDAO() {
        return crfDAO;
    }

    public StudyDao getStudyDao() {
        return studyDao;
    }

    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    public void setCrfDAO(CRFDAO crfDAO) {
        this.crfDAO = crfDAO;
    }

    public UserAccountBean getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(UserAccountBean currentUser) {
        this.currentUser = currentUser;
    }

    public StudyEventDefinitionBean getSelectedStudyEventDefinition() {
        return selectedStudyEventDefinition;
    }

    public void setSelectedStudyEventDefinition(StudyEventDefinitionBean selectedStudyEventDefinition) {
        this.selectedStudyEventDefinition = selectedStudyEventDefinition;
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

            String item = StringUtils.lowerCase(((Status) itemValue).getName());
            String filter = StringUtils.lowerCase(filterValue);

            if (filter.equals(item)) {
                return true;
            }
            return false;
        }
    }

    public class SubjectEventStatusFilterMatcher implements FilterMatcher {
        public boolean evaluate(Object itemValue, String filterValue) {
            // No need to evaluate itemValue and filterValue.
            return true;
        }
    }

    public class SubjectGroupFilterMatcher implements FilterMatcher {

        public boolean evaluate(Object itemValue, String filterValue) {
            String itemSGName = studyGroupDAO.findByPK(Integer.valueOf(itemValue.toString())).getName();
            return filterValue.equalsIgnoreCase(itemSGName);
        }
    }

    public class SubjectEventCRFStatusFilterMatcher implements FilterMatcher {

        public boolean evaluate(Object itemValue, String filterValue) {
            // No need to evaluate itemValue and filterValue.
            return true;
        }
    }

    private class StatusCellEditor implements CellEditor {
        public Object getValue(Object item, String property, int rowcount) {
            StudySubjectBean studySubject = (StudySubjectBean) new BasicCellEditor().getValue(item, "studySubject", rowcount);
            return studySubject.getStatus().getName();
        }
    }

    private class StatusDroplistFilterEditor extends DroplistFilterEditor {
        @Override
        protected List<Option> getOptions() {
            List<Option> options = new ArrayList<Option>();
            for (Object status : Status.toDropDownArrayList()) {
                options.add(new Option(((Status) status).getName(), ((Status) status).getName()));
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
            options.add(new Option(resterm.getString(SIGNED.toLowerCase()).toLowerCase(),resterm.getString(SIGNED.toLowerCase()).toLowerCase()));
            options.add(new Option(resterm.getString(LOCKED.toLowerCase()).toLowerCase(),resterm.getString(LOCKED.toLowerCase()).toLowerCase()));
            options.add(new Option(resterm.getString(NOT_SIGNED.toLowerCase()).toLowerCase(),resterm.getString(NOT_SIGNED.toLowerCase()).toLowerCase()));
            options.add(new Option(resterm.getString(NOT_LOCKED.toLowerCase()).toLowerCase(),resterm.getString(NOT_LOCKED.toLowerCase()).toLowerCase()));
            return options;
        }
    }

    private class EventCrfWorkflowStatusDroplistFilterEditor extends DroplistFilterEditor {
        @Override
        protected List<Option> getOptions() {
            List<Option> options = new ArrayList<Option>();
            List<EventCrfWorkflowStatusEnum> eventWorkflowStatuses = new ArrayList<>(Arrays.asList(EventCrfWorkflowStatusEnum.values()));

            for (EventCrfWorkflowStatusEnum workflow : eventWorkflowStatuses) {
                if (!workflow.name().equals("LOCKED"))
                    options.add(new Option(workflow.getDisplayValue(), workflow.getDisplayValue()));
            }
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
            ArrayList<StudyGroupBean> groups = studyGroupDAO.findAllByGroupClass(this.studyGroupClass);
            for (Object subjectStudyGroup : groups) {
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

    private class EventStatusCellEditor implements CellEditor {

        StudyEventBean studyEvent;
        StudySubjectBean studySubjectBean;
        List<DisplayBean> events;
        SubjectBean subject;
        StudyEventDefinitionBean studyEventDefinition;

        @SuppressWarnings("unchecked")
        public Object getValue(Object item, String property, int rowcount) {

            events = (List<DisplayBean>) ((HashMap<Object, Object>) item).get("events");
            studySubjectBean = (StudySubjectBean) ((HashMap<Object, Object>) item).get("studySubject");
            subject = (SubjectBean) ((HashMap<Object, Object>) item).get("subject");
            studyEventDefinition = selectedStudyEventDefinition;
            List<StudyEventBean> studyEvents;

            StringBuilder url = new StringBuilder();
            for (int i = 0; i < events.size(); i++) {
                DisplayBean display = events.get(i);
                StudyEventWorkflowStatusEnum eventWorkflowStatus = (StudyEventWorkflowStatusEnum) display.getProps().get("event.status");
                studyEvent = (StudyEventBean) display.getProps().get("event");
                studyEvents = new ArrayList<StudyEventBean>();
                String iconStatus = eventWorkflowStatus.toString();
                if (studyEvent != null) {
                    studyEvents.add(studyEvent);
                    if (studyEvent.isRemoved()) {
                        iconStatus = EventCrfStatusEnum.REMOVED.toString();
                    }
                }
                url.append(eventDivBuilder(subject, Integer.valueOf(rowcount + String.valueOf(i)), studyEvents, studyEventDefinition, studySubjectBean));
                url.append("<span class='" + imageIconPaths.get(iconStatus) + "' border='0' style='position: relative; left: 7px;'>");
                url.append("</a></td></tr></table>");
            }

            return url.toString();
        }

    }

    private class EventStartDateCellEditor implements CellEditor {
        Date eventStartDate;
        List<DisplayBean> events;

        @SuppressWarnings("unchecked")
        public Object getValue(Object item, String property, int rowcount) {
            events = (List<DisplayBean>) ((HashMap<Object, Object>) item).get("events");
            StringBuilder url = new StringBuilder();

            for (DisplayBean display : events) {
                eventStartDate = (Date) display.getProps().get("studySubject.createdDate");
                url.append("<table border='0'  cellpadding='0'  cellspacing='0' ><tr valign='top' ><td>");
                url.append(eventStartDate == null ? "" : formatDate(eventStartDate));
                url.append("</td></tr></table>");
            }
            return url.toString();
        }
    }

    private class EventCrfCellEditor implements CellEditor {

        SubjectEventStatus subjectEventStatus;
        StudyEventBean studyEvent;
        StudySubjectBean studySubjectBean;
        List<DisplayBean> events;
        SubjectBean subject;
        CRFBean crf;
        EventCRFBean eventCrf;
        FormLayoutBean formLayoutBean;
        EventDefinitionCRFBean eventDefintionCrf;
        StudyEventDefinitionBean studyEventDefinition;

        @SuppressWarnings("unchecked")
        public Object getValue(Object item, String property, int rowcount) {
            events = (List<DisplayBean>) ((HashMap<Object, Object>) item).get("events");
            studySubjectBean = (StudySubjectBean) ((HashMap<Object, Object>) item).get("studySubject");
            subject = (SubjectBean) ((HashMap<Object, Object>) item).get("subject");
            studyEventDefinition = selectedStudyEventDefinition;
            List<StudyEventBean> studyEvents;
            String path = (String) ((HashMap<Object, Object>) item).get("webappContext");
            StringBuilder url = new StringBuilder();
            for (int i = 0; i < events.size(); i++) {

                DisplayBean display = events.get(i);
                EventCrfWorkflowStatusEnum crfWorkflowStatus = (EventCrfWorkflowStatusEnum) display.getProps().get(property);
                crf = (CRFBean) display.getProps().get(property + "_crf");
                eventDefintionCrf = (EventDefinitionCRFBean) display.getProps().get(property + "_eventDefinitionCrf");
                eventCrf = (EventCRFBean) display.getProps().get(property + "_eventCrf");
                studyEvent = (StudyEventBean) display.getProps().get("event");
                if(eventCrf != null)
                    formLayoutBean = (FormLayoutBean) formLayoutDAO.findByPK(eventCrf.getFormLayoutId());
                studyEvents = new ArrayList<StudyEventBean>();

                if (studyEvent != null) {
                    studyEvents.add(studyEvent);
                }

                EventCrfLayerBuilder eventCrfLayerBuilder = new EventCrfLayerBuilder(subject, Integer.valueOf(rowcount + String.valueOf(i)), studyEvents,
                        crfWorkflowStatus, eventCrf, formLayoutBean, studySubjectBean, studyBean, currentRole, currentUser, eventDefintionCrf, crf, studyEventDefinition, path, studyDao);

                String iconStatus = crfWorkflowStatus.toString();
                if ((eventCrf != null && eventCrf.isRemoved())
                        || (studyEvent != null && studyEvent.isRemoved() && eventCrf != null
                        && eventCrf.getWorkflowStatus() != EventCrfWorkflowStatusEnum.NOT_STARTED)) {
                    iconStatus = EventCrfStatusEnum.REMOVED.toString();
                }
                url.append(eventCrfLayerBuilder.buid());
                url.append("<span class='" + crfColumnImageIconPaths.get(iconStatus) + "' border='0'>");
                url.append("</a></td></tr></table>");
            }

            return url.toString();
        }
    }

    private class ActionsCellEditor implements CellEditor {
        @SuppressWarnings("unchecked")
        public Object getValue(Object item, String property, int rowcount) {
            String value = "";
            StudySubjectBean studySubjectBean = (StudySubjectBean) ((HashMap<Object, Object>) item).get("studySubject");
            Study subjectStudy = studyDao.findByPK(studySubjectBean.getStudyId());

            Integer studySubjectId = studySubjectBean.getId();
            if (studySubjectId != null) {
                StringBuilder url = new StringBuilder();

                url.append(viewStudySubjectLinkBuilder(studySubjectBean));
                if (getCurrentRole().getRole() != Role.MONITOR) {
                    if (subjectStudy.getStatus() == core.org.akaza.openclinica.domain.Status.AVAILABLE && studySubjectBean.getStatus() != Status.DELETED) {
                        url.append(removeStudySubjectLinkBuilder(studySubjectBean));
                    }
                    if (subjectStudy.getStatus() == core.org.akaza.openclinica.domain.Status.AVAILABLE && studySubjectBean.getStatus() == Status.DELETED) {
                        url.append(restoreStudySubjectLinkBuilder(studySubjectBean, null));
                    }
                    if (subjectStudy.getStatus() == core.org.akaza.openclinica.domain.Status
                            .AVAILABLE && studySubjectBean.getStatus() == Status.AVAILABLE) {
                        if (currentRole.getRole() != Role.INVESTIGATOR && currentRole.getRole() != Role.RESEARCHASSISTANT
                                && currentRole.getRole() != Role.RESEARCHASSISTANT2) {
                            url.append(reAssignStudySubjectLinkBuilder(studySubjectBean));
                        }
                    }
                }
                value = url.toString();
            }

            return value;
        }

    }

    class DisplayBean {

        HashMap<String, Object> props = new HashMap<String, Object>();

        public HashMap<String, Object> getProps() {
            return props;
        }

        public void setProps(HashMap<String, Object> props) {
            this.props = props;
        }
    }

    private String viewStudySubjectLinkBuilder(StudySubjectBean studySubject) {
        HtmlBuilder builder = new HtmlBuilder();
        builder.append(
                "<a onmouseup=\"javascript:setImage('bt_View1','icon icon-search');\" onmousedown=\"javascript:setImage('bt_View1','icon icon-search');\" href=\"ViewStudySubject?id="
                        + studySubject.getId());
        builder.append("\"><span hspace=\"2\" border=\"0\" title=\"View\" alt=\"View\" class=\"icon icon-search\" name=\"bt_Reassign1\"/></a>");
        builder.append("&nbsp;&nbsp;&nbsp;");
        return builder.toString();
    }

    private String removeStudySubjectLinkBuilder(StudySubjectBean studySubject) {
        HtmlBuilder builder = new HtmlBuilder();
        builder.append(
                "<a onmouseup=\"javascript:setImage('bt_View1','icon icon-cancel');\" onmousedown=\"javascript:setImage('bt_View1','icon icon-cancel');\" href=\"RemoveStudySubject?action=confirm&id="
                        + studySubject.getId() + "&subjectId=" + studySubject.getSubjectId() + "&studyId=" + studySubject.getStudyId());
        builder.append("\"><span hspace=\"2\" border=\"0\" title=\"Remove\" alt=\"View\" class=\"icon icon-cancel\" name=\"bt_Reassign1\"/></a>");
        builder.append("&nbsp;&nbsp;&nbsp;");
        return builder.toString();
    }

    private String reAssignStudySubjectLinkBuilder(StudySubjectBean studySubject) {
        HtmlBuilder builder = new HtmlBuilder();
        builder.append(
                "<a onmouseup=\"javascript:setImage('bt_View1','icon icon-icon-reassign3');\" onmousedown=\"javascript:setImage('bt_View1','icon icon-icon-reassign3');\" href=\"ReassignStudySubject?id="
                        + studySubject.getId());
        builder.append("\"><span hspace=\"2\" border=\"0\" title=\"Reassign\" alt=\"Reassign\" class=\"icon icon-icon-reassign3\" name=\"bt_Reassign1\"/></a>");
        builder.append("&nbsp;&nbsp;&nbsp;");
        return builder.toString();
    }

    private String restoreStudySubjectLinkBuilder(StudySubjectBean studySubject, String text) {
        HtmlBuilder builder = new HtmlBuilder();
        String link = "RestoreStudySubject?action=confirm&id="
                + studySubject.getId() + "&subjectId" + studySubject.getSubjectId() + "&studyId=" + studySubject.getStudyId();
        builder.append(
                "<a onmouseup=\"javascript:setImage('bt_View1','icon icon-ccw');\" " +
                        "onmousedown=\"javascript:setImage('bt_View1','icon icon-ccw');\" href=\" " + link + " \" ");
        builder.append("\"><span hspace=\"2\" border=\"0\" title=\"Restore\" alt=\"Restore\" " +
                "class=\"icon icon-ccw\" name=\"bt_Reassign1\"/></a>");
        builder.append("&nbsp;&nbsp;&nbsp;");
        if (text != null) {
            builder.nbsp().a().href(link);
            builder.close().append(text).aEnd();
        }
        return builder.toString();
    }

    private String eventDivBuilder(SubjectBean subject, Integer rowCount, List<StudyEventBean> studyEvents, StudyEventDefinitionBean sed,
                                   StudySubjectBean studySubject) {

        String studySubjectLabel = studySubject.getLabel();

        String divWidth = studyEvents.size() >= 3 ? "540" : studyEvents.size() == 2 ? "360" : "180";

        HtmlBuilder eventDiv = new HtmlBuilder();

        eventDiv.table(0).border("0").cellpadding("0").cellspacing("0").close();
        // Lock Div
        eventDiv.div().id("S_Lock_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount)
                .style("position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;").close();
        lockLinkBuilder(eventDiv, studySubjectLabel, rowCount, studyEvents, sed);
        eventDiv.divEnd();

        eventDiv.tr(0).valign("top").close().td(0).close();
        // Event Div
        eventDiv.div().id("S_Event_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount)
                .style("position: absolute; visibility: hidden; z-index: 3;width:" + divWidth + "px; top: 0px; float: left;").close();
        eventDiv.div().styleClass("box_T").close().div().styleClass("box_L").close().div().styleClass("box_R").close().div().styleClass("box_B").close().div()
                .styleClass("box_TL").close().div().styleClass("box_TR").close().div().styleClass("box_BL").close().div().styleClass("box_BR").close();

        eventDiv.div().styleClass("tablebox_center").close();
        eventDiv.div().styleClass("ViewSubjectsPopup").style("color: rgb(91, 91, 91);").close();

        eventDiv.table(0).border("0").cellpadding("0").cellspacing("0").close();
        eventDiv.tr(0).valign("top").close();

        singleEventDivBuilder(eventDiv, subject, rowCount, studyEvents, sed, studySubject);

        return eventDiv.toString();
    }

    // for event status
    private void singleEventDivBuilder(HtmlBuilder eventDiv, SubjectBean subject, Integer rowCount, List<StudyEventBean> studyEvents,
                                       StudyEventDefinitionBean sed, StudySubjectBean studySubject) {

        String tableHeaderRowLeftStyleClass = "table_header_row_left";
        String click_for_more_options = resword.getString("click_for_more_options");
        String schedule = resword.getString("schedule");
        String view = resword.getString("view") + "/" + resword.getString("enter_data");
        String edit = resword.getString("edit");
        String remove = resword.getString("remove");
        String subjectText = resword.getString("subject");
        String eventText = resword.getString("event");
        String signText = resword.getString("signed");
        String lockText = resword.getString("locked");
        String archiveText = resword.getString("archived");
        String restoreText = resword.getString("restore");

        StudyEventWorkflowStatusEnum eventStatus = studyEvents.size() == 0 ? StudyEventWorkflowStatusEnum.NOT_SCHEDULED : studyEvents.get(0).getWorkflowStatus();
        // String studyEventName = studyEvents.size() == 0 ? "" : studyEvents.get(0).getName();
        String studyEventId = studyEvents.size() == 0 ? "" : String.valueOf(studyEvents.get(0).getId());
        Status eventSysStatus = studySubject.getStatus();
        String studySubjectLabel = studySubject.getLabel();

        eventDiv.td(0).styleClass(tableHeaderRowLeftStyleClass).close();
        eventDiv.append(subjectText).append(": ").append(studySubjectLabel).br();
        eventDiv.append(eventText).append(": ").append(sed.getName()).br();
        if (studyEvents.size() > 0 && studyEvents.get(0).isRemoved()) {
            eventDiv.append(resword.getString("status")).append(": ").append(EventCrfStatusEnum.REMOVED.toString().toLowerCase()).br();
        } else {
            eventDiv.append(resword.getString("status")).append(": ").append(eventStatus.getDisplayValue()).br();
        }
        if (studyEvents.size() > 0) {
            if (studyEvents.get(0).isSigned()) {
                eventDiv.append("<span class=\"icon icon-stamp-new status\" alt=" + signText + " title=" + signText + " style=\"margin-right: 5px;\"></span>");
            }
            if (studyEvents.get(0).isLocked()) {
                eventDiv.append("<span class=\"icon icon-lock-new status\" alt=" + lockText + " title=" + lockText + " style=\"margin-right: 5px;\"></span>");
            }
            if (studyEvents.get(0).isArchived()) {
                eventDiv.append("<span class=\"icon icon-archived-new status\" alt=" + archiveText + " title=" + archiveText + " style=\"margin-right: 5px;\"></span>");
            }
        }
        eventDiv.tdEnd();
        eventDiv.td(0).styleClass(tableHeaderRowLeftStyleClass).align("right").close();
        linkBuilder(eventDiv, studySubjectLabel, rowCount, studyEvents, sed);
        eventDiv.tdEnd();

        eventDiv.trEnd(0);
        eventDiv.tr(0).id("S_Menu_off_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount).style("display: all").close();
        eventDiv.td(0).styleClass("table_cell_left").colspan("2").close().append("<i>").append(click_for_more_options).append("</i>").tdEnd();
        eventDiv.trEnd(0);

        eventDiv.tr(0).id("S_Menu_on_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount).style("display: none").close();
        eventDiv.td(0).colspan("2").close();
        eventDiv.table(0).border("0").cellpadding("0").cellspacing("0").width("100%").close();

        if (eventSysStatus.getId() == Status.AVAILABLE.getId() || eventSysStatus == Status.SIGNED) {

            if (eventStatus.equals(StudyEventWorkflowStatusEnum.NOT_SCHEDULED) && currentRole.getRole() != Role.MONITOR && !studyBean.getStatus().isFrozen()) {
                eventDiv.tr(0).valign("top").close();
                eventDiv.td(0).styleClass("table_cell_left").close();
                createNewStudyEventLinkBuilder(eventDiv, studySubject.getId(), sed, schedule);
                eventDiv.tdEnd().trEnd(0);
            } else if (eventStatus.equals(StudyEventWorkflowStatusEnum.COMPLETED)) {
                eventDiv.tr(0).valign("top").close();
                eventDiv.td(0).styleClass("table_cell_left").close();
                enterDataForStudyEventLinkBuilder(eventDiv, studyEventId, view);
                eventDiv.tdEnd().trEnd(0);
                if ((currentRole.getRole() == Role.STUDYDIRECTOR || currentUser.isSysAdmin()) && studyBean.getStatus() == core.org.akaza.openclinica.domain.Status.AVAILABLE) {
                    eventDiv.tr(0).valign("top").close();
                    eventDiv.td(0).styleClass("table_cell_left").close();
                    updateStudyEventLinkBuilder(eventDiv, studySubject.getId(), studyEventId, edit);
                    eventDiv.tdEnd().trEnd(0);
                    eventDiv.tr(0).valign("top").close();
                    eventDiv.td(0).styleClass("table_cell_left").close();
                    removeStudyEventLinkBuilder(eventDiv, studySubject.getId(), studyEventId, remove);
                    eventDiv.tdEnd().trEnd(0);
                }
            } else if (studyEvents.get(0).isLocked()) {
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
                    }
                }
            } else {
                eventDiv.tr(0).valign("top").close();
                eventDiv.td(0).styleClass("table_cell_left").close();
                enterDataForStudyEventLinkBuilder(eventDiv, studyEventId, view);
                eventDiv.tdEnd().trEnd(0);

                if (studyEvents.size() > 0 && studyEvents.get(0).isRemoved()) {
                    eventDiv.tr(0).valign("top").close();
                    eventDiv.td(0).styleClass("table_cell_left").close();
                    eventDiv.append(restoreStudySubjectLinkBuilder(studySubject, restoreText));
                    eventDiv.tdEnd().trEnd(0);
                } else {
                    if ((currentRole.getRole() == Role.STUDYDIRECTOR || currentUser.isSysAdmin()) &&
                            studyBean.getStatus() == core.org.akaza.openclinica.domain.Status.AVAILABLE) {
                        eventDiv.tr(0).valign("top").close();
                        eventDiv.td(0).styleClass("table_cell_left").close();
                        updateStudyEventLinkBuilder(eventDiv, studySubject.getId(), studyEventId, edit);
                        eventDiv.tdEnd().trEnd(0);
                        eventDiv.tr(0).valign("top").close();
                        eventDiv.td(0).styleClass("table_cell_left").close();
                        removeStudyEventLinkBuilder(eventDiv, studySubject.getId(), studyEventId, remove);
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
        builder.append("<a>");
        builder.append("<span hspace=\"2\" border=\"0\" align=\"left\" class=\"icon icon-pencil\"/></a>");
        builder.nbsp().nbsp().a().href(href1);
        builder.close().append(edit).aEnd();

    }

    private void removeStudyEventLinkBuilder(HtmlBuilder builder, Integer studySubjectId, String studyEventId, String remove) {
        String href1 = "RemoveStudyEvent?action=confirm&id=" + studyEventId + "&studySubId=" + studySubjectId;
        builder.append("<a>");
        builder.append("<span hspace=\"2\" border=\"0\" align=\"left\" class=\"icon icon-cancel\"/></a>");
        builder.nbsp().nbsp().a().href(href1);
        builder.close().append(remove).aEnd();

    }

    private void createNewStudyEventLinkBuilder(HtmlBuilder builder, Integer studySubjectId, StudyEventDefinitionBean sed, String schedule) {
        String href1 = "CreateNewStudyEvent?studySubjectId=" + studySubjectId + "&studyEventDefinition=" + sed.getId();
        builder.append("<a>");
        builder.append("<span hspace=\"2\" border=\"0\" align=\"left\" class=\"icon icon-clock2\"/></a>");
        builder.nbsp().nbsp().a().href(href1);
        builder.close().append(schedule).aEnd();

    }

    private void enterDataForStudyEventLinkBuilder(HtmlBuilder builder, String studyEventId, String view) {
        String href1 = "EnterDataForStudyEvent?eventId=" + studyEventId;
        builder.append("<a>");
        builder.append("<span hspace=\"2\" border=\"0\" align=\"left\" class=\"icon icon-search\"/></a>");
        builder.nbsp().nbsp().a().href(href1);
        builder.close().append(view).aEnd();

    }

    private void lockLinkBuilder(HtmlBuilder builder, String studySubjectLabel, Integer rowCount, List<StudyEventBean> studyEvents,
                                 StudyEventDefinitionBean sed) {
        studySubjectLabel = studySubjectLabel.replaceAll("'", "\\\\'");
        String href1 = "javascript:leftnavExpand('S_Menu_on_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        String href2 = "javascript:leftnavExpand('S_Menu_off_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        String onmouseover = "layersShowOrHide('visible','S_Event_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        onmouseover += "javascript:setImage('ExpandIcon_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "','images/icon_collapse.gif');";
        String onClick1 = "layersShowOrHide('hidden','Lock_all'); ";
        String onClick2 = "layersShowOrHide('hidden','S_Event_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        String onClick3 = "layersShowOrHide('hidden','S_Lock_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        String onClick4 = "javascript:setImage('ExpandIcon_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "','images/icon_blank.gif'); ";
        builder.a().href(href1 + href2);
        builder.onclick(onmouseover + onClick1 + onClick2 + onClick3 + onClick4);
        builder.close();
        builder.img().src("images/spacer.gif").border("0").append("height=\"30\"").width("50").close().aEnd();

    }

    private void iconLinkBuilder(HtmlBuilder builder, String studySubjectLabel, Integer rowCount, List<StudyEventBean> studyEvents,
                                 StudyEventDefinitionBean sed) {
        studySubjectLabel = studySubjectLabel.replaceAll("'", "\\\\'");
        String href1Repeating = "javascript:ExpandEventOccurrences('" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'," + studyEvents.size()
                + "); ";
        String href1 = "javascript:leftnavExpand('S_Menu_on_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        String href2 = "javascript:leftnavExpand('S_Menu_off_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        String onmouseover = "moveObject('S_Event_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "', event); ";
        onmouseover += "setImage('ExpandIcon_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "','images/icon_expand.gif');";
        String onmouseout = "layersShowOrHide('hidden','S_Event_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        onmouseout += "setImage('ExpandIcon_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "','images/icon_blank.gif');";
        String onClick1 = "layersShowOrHide('visible','Lock_all'); ";
        String onClick2 = "LockObject('S_Lock_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "',event); ";
        String href = studyEvents.size() > 1 ? href1Repeating + href2 : href1 + href2;
        builder.a().href(href);
        builder.onclick(onmouseover + onClick1 + onClick2);
        builder.close();

    }

    private void linkBuilder(HtmlBuilder builder, String studySubjectLabel, Integer rowCount, List<StudyEventBean> studyEvents, StudyEventDefinitionBean sed) {
        studySubjectLabel = studySubjectLabel.replaceAll("'", "\\\\'");
        String href1 = "javascript:leftnavExpand('S_Menu_on_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        String href2 = "javascript:leftnavExpand('S_Menu_off_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        String onClick1 = "layersShowOrHide('hidden','Lock_all'); ";
        String onClick2 = "layersShowOrHide('hidden','S_Event_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        String onClick3 = "layersShowOrHide('hidden','S_Lock_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        String onClick4 = "javascript:setImage('ExpandIcon_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "','images/icon_blank.gif'); ";
        builder.a().href(href1 + href2);
        builder.onclick(onClick1 + onClick2 + onClick3 + onClick4);
        builder.close().append("X").aEnd();

    }

    private String formatDate(Date date) {
        String format = resformat.getString("date_format_string");
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    public FormLayoutDAO getFormLayoutDAO() {
        return formLayoutDAO;
    }

    public void setFormLayoutDAO(FormLayoutDAO formLayoutDAO) {
        this.formLayoutDAO = formLayoutDAO;
    }

}
