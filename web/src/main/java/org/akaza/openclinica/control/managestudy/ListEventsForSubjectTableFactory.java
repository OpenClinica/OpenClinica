package org.akaza.openclinica.control.managestudy;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudyGroupBean;
import org.akaza.openclinica.bean.managestudy.StudyGroupClassBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.bean.submit.SubjectGroupMapBean;
import org.akaza.openclinica.control.AbstractTableFactory;
import org.akaza.openclinica.control.DefaultActionsEditor;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.ListEventsForSubjectFilter;
import org.akaza.openclinica.dao.managestudy.ListEventsForSubjectSort;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupClassDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.dao.submit.SubjectGroupMapDAO;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.apache.commons.lang.StringUtils;
import org.jmesa.core.filter.FilterMatcher;
import org.jmesa.core.filter.MatcherKey;
import org.jmesa.facade.TableFacade;
import org.jmesa.limit.Filter;
import org.jmesa.limit.FilterSet;
import org.jmesa.limit.Limit;
import org.jmesa.limit.Sort;
import org.jmesa.limit.SortSet;
import org.jmesa.view.component.Row;
import org.jmesa.view.editor.BasicCellEditor;
import org.jmesa.view.editor.CellEditor;
import org.jmesa.view.html.HtmlBuilder;
import org.jmesa.view.html.editor.DroplistFilterEditor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletResponse;

public class ListEventsForSubjectTableFactory extends AbstractTableFactory {

    private StudyEventDefinitionDAO studyEventDefinitionDao;
    private StudySubjectDAO studySubjectDAO;
    private SubjectDAO subjectDAO;
    private StudyEventDAO studyEventDAO;
    private StudyGroupClassDAO studyGroupClassDAO;
    private SubjectGroupMapDAO subjectGroupMapDAO;
    private StudyDAO studyDAO;
    private StudyGroupDAO studyGroupDAO;
    private EventCRFDAO eventCRFDAO;
    private EventDefinitionCRFDAO eventDefintionCRFDAO;
    private CRFDAO crfDAO;
    private CRFVersionDAO crfVersionDAO;
    public CRFVersionDAO getCrfVersionDAO() {
		return crfVersionDAO;
	}

	public void setCrfVersionDAO(CRFVersionDAO crfVersionDAO) {
		this.crfVersionDAO = crfVersionDAO;
	}

	private StudyBean studyBean;
    private String[] columnNames = new String[] {};
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


    final HashMap<Integer, String> imageIconPaths = new HashMap<Integer, String>(8);
    final HashMap<Integer, String> crfColumnImageIconPaths = new HashMap<Integer, String>(8);

    public ListEventsForSubjectTableFactory(boolean showMoreLink) {
        imageIconPaths.put(1, "images/icon_Scheduled.gif");
        imageIconPaths.put(2, "images/icon_NotStarted.gif");
        imageIconPaths.put(3, "images/icon_InitialDE.gif");
        imageIconPaths.put(4, "images/icon_DEcomplete.gif");
        imageIconPaths.put(5, "images/icon_Stopped.gif");
        imageIconPaths.put(6, "images/icon_Skipped.gif");
        imageIconPaths.put(7, "images/icon_Locked.gif");
        imageIconPaths.put(8, "images/icon_Signed.gif");

        crfColumnImageIconPaths.put(0, "images/CRF_status_icon_Invalid.gif");
        crfColumnImageIconPaths.put(1, "images/CRF_status_icon_Scheduled.gif");
        crfColumnImageIconPaths.put(2, "images/CRF_status_icon_Started.gif");
        crfColumnImageIconPaths.put(3, "images/CRF_status_icon_InitialDEcomplete.gif");
        crfColumnImageIconPaths.put(4, "images/CRF_status_icon_DDE.gif");
        crfColumnImageIconPaths.put(5, "images/CRF_status_icon_Complete.gif");
        crfColumnImageIconPaths.put(6, "images/CRF_status_icon_Complete.gif");
        crfColumnImageIconPaths.put(7, "images/CRF_status_icon_Locked.gif");
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
                new SubjectEventStatusDroplistFilterEditor(), true, false);
        ++index;
        configureColumn(row.getColumn(columnNames[index + studyGroupClasses.size()]), resword.getString("event_date"), new EventStartDateCellEditor(), null);
        ++index;

        // crf columns
        for (int i = index + studyGroupClasses.size(); i < columnNames.length - 1; i++) {
            CRFBean crfBean = crfBeans.get(i - (index + studyGroupClasses.size()));
            configureColumn(row.getColumn(columnNames[i]), crfBean.getName(), new EventCrfCellEditor(), new SubjectEventCRFStatusDroplistFilterEditor(), true,
                    false);
        }

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
        Collection<StudySubjectBean> items =
            getStudySubjectDAO().getWithFilterAndSort(getStudyBean(), eventsForSubjectFilter, eventsForSubjectSort, rowStart, rowEnd);
        Collection<HashMap<Object, Object>> theItems = new ArrayList<HashMap<Object, Object>>();

        for (StudySubjectBean studySubjectBean : items) {
            HashMap<Object, Object> theItem = new HashMap<Object, Object>();
            theItem.put("studySubject", studySubjectBean);
            theItem.put("studySubject.label", studySubjectBean.getLabel());
            theItem.put("studySubject.status", studySubjectBean.getStatus());
            theItem.put("enrolledAt", ((StudyBean) getStudyDAO().findByPK(studySubjectBean.getStudyId())).getIdentifier());

            SubjectBean subjectBean = (SubjectBean) getSubjectDAO().findByPK(studySubjectBean.getSubjectId());
            theItem.put("subject", subjectBean);
            theItem.put("subject.charGender", subjectBean.getGender());

            //study group classes
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
            List<StudyEventBean> eventsForStudySubjectAndEventDefinitions =
                getStudyEventDAO().findAllByDefinitionAndSubject(selectedStudyEventDefinition, studySubjectBean);
            List<DisplayBean> events = new ArrayList<DisplayBean>();
            // study event size < 1
            if (eventsForStudySubjectAndEventDefinitions.size() < 1) {
                DisplayBean d = new DisplayBean();
                d.getProps().put("event", null);
                d.getProps().put("event.status", SubjectEventStatus.NOT_SCHEDULED);
                d.getProps().put("studySubject.createdDate", null);
                for (int i = 0; i < getCrfs(selectedStudyEventDefinition).size(); i++) {
                    CRFBean crf = getCrfs(selectedStudyEventDefinition).get(i);
                    d.getProps().put("crf_" + crf.getId(), DataEntryStage.UNCOMPLETED);
                    d.getProps().put("crf_" + crf.getId() + "_eventCrf", null);
                    d.getProps().put("crf_" + crf.getId() + "_crf", crf);
                    //d.getProps().put("crf_" + crf.getId() + "_eventDefinitionCrf", eventDefinitionCrfs.get(i));
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
                d.getProps().put("event.status", studyEventBean.getSubjectEventStatus());
                d.getProps().put("studySubject.createdDate", studyEventBean.getDateStarted());
                for (int i = 0; i < getCrfs(selectedStudyEventDefinition).size(); i++) {
                    CRFBean crf = getCrfs(selectedStudyEventDefinition).get(i);
                    EventCRFBean eventCRFBean = crfAsKeyEventCrfAsValue.get(crf.getId() + "_" + studyEventBean.getId());
                    if (eventCRFBean != null) {
                        d.getProps().put("crf_" + crf.getId(), eventCRFBean.getStage());
                        d.getProps().put("crf_" + crf.getId() + "_eventCrf", eventCRFBean);

                    } else {
                        d.getProps().put("crf_" + crf.getId(), DataEntryStage.UNCOMPLETED);
                        d.getProps().put("crf_" + crf.getId() + "_eventCrf", null);
                    }
                    d.getProps().put("crf_" + crf.getId() + "_crf", crf);
                    //d.getProps().put("crf_" + crf.getId() + "_eventDefinitionCrf", eventDefinitionCrfs.get(i));
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
        EventDefinitionCRFBean eventDefinitionCrf =
            getEventDefintionCRFDAO().findByStudyEventDefinitionIdAndCRFIdAndStudyId(studyEventDefinitionId, crfBean.getId(), studySubject.getStudyId());
        if (eventDefinitionCrf.getId() == 0) {
            eventDefinitionCrf = getEventDefintionCRFDAO().findForStudyByStudyEventDefinitionIdAndCRFId(studyEventDefinitionId, crfBean.getId());
        }
        CRFVersionBean defaultVersion = (CRFVersionBean) getCrfVersionDAO().findByPK(eventDefinitionCrf.getDefaultVersionId());
        eventDefinitionCrf.setDefaultCRF(defaultVersion);
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
            if("studySubject.status".equalsIgnoreCase(property)) {
                value = Status.getByName(value).getId()+"";
            } else if("event.status".equalsIgnoreCase(property)) {
                value = SubjectEventStatus.getByName(value).getId()+"";
            } else if(property.startsWith("sgc_")){
                int studyGroupClassId = property.endsWith("_")? 0 : Integer.valueOf(property.split("_")[1]);
                value = studyGroupDAO.findByNameAndGroupClassID(value, studyGroupClassId).getId()+"";
            } else if(property.startsWith("crf_")) {
                value = DataEntryStage.getByName(value).getId()+"";
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
    private ArrayList<StudyEventDefinitionBean> getStudyEventDefinitions() {
        if (this.studyEventDefinitions == null) {
            if (studyBean.getParentStudyId() > 0) {
                StudyBean parentStudy = (StudyBean) getStudyDAO().findByPK(studyBean.getParentStudyId());
                studyEventDefinitions = getStudyEventDefinitionDao().findAllByStudy(parentStudy);
            } else {
                studyEventDefinitions = getStudyEventDefinitionDao().findAllByStudy(studyBean);
            }
        }
        return this.studyEventDefinitions;
    }

    @SuppressWarnings("unchecked")
    private ArrayList<CRFBean> getCrfs(StudyEventDefinitionBean eventDefinition) {
        if (this.crfBeans == null) {
            crfBeans = new ArrayList<CRFBean>();
            eventDefinitionCrfs = new ArrayList<EventDefinitionCRFBean>();
            for (EventDefinitionCRFBean eventDefinitionCrf : (List<EventDefinitionCRFBean>) getEventDefintionCRFDAO().findAllActiveByEventDefinitionId(
                    eventDefinition.getId())) {
                CRFBean crfBean = (CRFBean) getCrfDAO().findByPK(eventDefinitionCrf.getCrfId());
                ArrayList<CRFVersionBean> crfVersions = (ArrayList<CRFVersionBean>)getCrfVersionDAO().findAllByCRFId(eventDefinitionCrf.getCrfId());
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
            if (studyBean.getParentStudyId() > 0) {
                StudyBean parentStudy = (StudyBean) getStudyDAO().findByPK(studyBean.getParentStudyId());
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

    public StudyBean getStudyBean() {
        return studyBean;
    }

    public void setStudyBean(StudyBean studyBean) {
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

    public StudyDAO getStudyDAO() {
        return studyDAO;
    }

    public void setStudyDAO(StudyDAO studyDAO) {
        this.studyDAO = studyDAO;
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

    private class SubjectEventStatusDroplistFilterEditor extends DroplistFilterEditor {
        @Override
        protected List<Option> getOptions() {
            List<Option> options = new ArrayList<Option>();
            for (Object subjectEventStatus : SubjectEventStatus.toArrayList()) {
                options.add(new Option(((SubjectEventStatus) subjectEventStatus).getName(), ((SubjectEventStatus) subjectEventStatus).getName()));
            }
            return options;
        }
    }

    private class SubjectEventCRFStatusDroplistFilterEditor extends DroplistFilterEditor {
        @Override
        protected List<Option> getOptions() {
            List<Option> options = new ArrayList<Option>();
            for (Object eventCRFStatus : DataEntryStage.toArrayList()) {
                if (((DataEntryStage) eventCRFStatus).getId() != 0) {
                    options.add(new Option(((DataEntryStage) eventCRFStatus).getName(), ((DataEntryStage) eventCRFStatus).getName()));
                }
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

        SubjectEventStatus subjectEventStatus;
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
                subjectEventStatus = (SubjectEventStatus) display.getProps().get("event.status");
                studyEvent = (StudyEventBean) display.getProps().get("event");
                studyEvents = new ArrayList<StudyEventBean>();
                if (studyEvent != null) {
                    studyEvents.add(studyEvent);
                }
                url.append(eventDivBuilder(subject, Integer.valueOf(rowcount + String.valueOf(i)), studyEvents, studyEventDefinition, studySubjectBean));
                url.append("<img src='" + imageIconPaths.get(subjectEventStatus.getId()) + "' border='0' style='position: relative; left: 7px;'>");
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
        DataEntryStage dataEntryStage;
        StudyEventBean studyEvent;
        StudySubjectBean studySubjectBean;
        List<DisplayBean> events;
        SubjectBean subject;
        CRFBean crf;
        EventCRFBean eventCrf;
        EventDefinitionCRFBean eventDefintionCrf;
        StudyEventDefinitionBean studyEventDefinition;

        @SuppressWarnings("unchecked")
        public Object getValue(Object item, String property, int rowcount) {
            events = (List<DisplayBean>) ((HashMap<Object, Object>) item).get("events");
            studySubjectBean = (StudySubjectBean) ((HashMap<Object, Object>) item).get("studySubject");
            subject = (SubjectBean) ((HashMap<Object, Object>) item).get("subject");
            studyEventDefinition = selectedStudyEventDefinition;
            List<StudyEventBean> studyEvents;
            String path = (String)((HashMap<Object, Object>) item).get("webappContext"); 
            StringBuilder url = new StringBuilder();
            for (int i = 0; i < events.size(); i++) {

                DisplayBean display = events.get(i);
                dataEntryStage = (DataEntryStage) display.getProps().get(property);
                crf = (CRFBean) display.getProps().get(property + "_crf");
                eventDefintionCrf = (EventDefinitionCRFBean) display.getProps().get(property + "_eventDefinitionCrf");
                eventCrf = (EventCRFBean) display.getProps().get(property + "_eventCrf");
                subjectEventStatus = (SubjectEventStatus) display.getProps().get("event.status");
                studyEvent = (StudyEventBean) display.getProps().get("event");
                studyEvents = new ArrayList<StudyEventBean>();
               
                if (studyEvent != null) {
                    studyEvents.add(studyEvent);
                }

                EventCrfLayerBuilder eventCrfLayerBuilder =
                    new EventCrfLayerBuilder(subject, Integer.valueOf(rowcount + String.valueOf(i)), studyEvents, dataEntryStage, eventCrf, studySubjectBean,
                            studyBean, currentRole, currentUser, eventDefintionCrf, crf,studyEventDefinition,path);

                url.append(eventCrfLayerBuilder.buid());
                url.append("<img src='" + crfColumnImageIconPaths.get(dataEntryStage.getId()) + "' border='0'>");
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
            Integer studySubjectId = studySubjectBean.getId();
            if (studySubjectId != null) {
                StringBuilder url = new StringBuilder();

                url.append(viewStudySubjectLinkBuilder(studySubjectBean));
                if (getCurrentRole().getRole() != Role.MONITOR) {
                    if (getStudyBean().getStatus() == Status.AVAILABLE && studySubjectBean.getStatus() != Status.DELETED) {
                        url.append(removeStudySubjectLinkBuilder(studySubjectBean));
                    }
                    if (getStudyBean().getStatus() == Status.AVAILABLE && studySubjectBean.getStatus() == Status.DELETED) {
                        url.append(restoreStudySubjectLinkBuilder(studySubjectBean));
                    }
                    if (getStudyBean().getStatus() == Status.AVAILABLE && studySubjectBean.getStatus() == Status.AVAILABLE) {
                        if (currentRole.getRole() != Role.INVESTIGATOR && currentRole.getRole() != Role.RESEARCHASSISTANT && currentRole.getRole() != Role.RESEARCHASSISTANT2 ) {
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
        HtmlBuilder actionLink = new HtmlBuilder();
        actionLink.a().href("ViewStudySubject?id=" + studySubject.getId());
        actionLink.append("onMouseDown=\"javascript:setImage('bt_View1','images/bt_View_d.gif');\"");
        actionLink.append("onMouseUp=\"javascript:setImage('bt_View1','images/bt_View.gif');\"").close();
        actionLink.img().name("bt_View1").src("images/bt_View.gif").border("0").alt(resword.getString("view")).title(resword.getString("view")).append("hspace=\"2\"").end().aEnd();
        actionLink.append("&nbsp;&nbsp;&nbsp;");
        return actionLink.toString();
    }

    private String removeStudySubjectLinkBuilder(StudySubjectBean studySubject) {
        HtmlBuilder actionLink = new HtmlBuilder();
        actionLink.a().href(
                "RemoveStudySubject?action=confirm&id=" + studySubject.getId() + "&subjectId=" + studySubject.getSubjectId() + "&studyId="
                    + studySubject.getStudyId());
        actionLink.append("onMouseDown=\"javascript:setImage('bt_Remove1','images/bt_Remove_d.gif');\"");
        actionLink.append("onMouseUp=\"javascript:setImage('bt_Remove1','images/bt_Remove.gif');\"").close();
        actionLink.img().name("bt_Remove1").src("images/bt_Remove.gif").border("0").alt(resword.getString("remove")).title(resword.getString("remove")).append("hspace=\"2\"").end().aEnd();
        actionLink.append("&nbsp;&nbsp;&nbsp;");
        return actionLink.toString();
    }

    private String reAssignStudySubjectLinkBuilder(StudySubjectBean studySubject) {
        HtmlBuilder actionLink = new HtmlBuilder();
        actionLink.a().href("ReassignStudySubject?id=" + studySubject.getId());
        actionLink.append("onMouseDown=\"javascript:setImage('bt_Reassign1','images/bt_Reassign_d.gif');\"");
        actionLink.append("onMouseUp=\"javascript:setImage('bt_Reassign1','images/bt_Reassign.gif');\"").close();
        actionLink.img().name("bt_Reassign1").src("images/bt_Reassign.gif").border("0").alt(resword.getString("reassign")).title(resword.getString("reassign")).append("hspace=\"2\"").end().aEnd();
        actionLink.append("&nbsp;&nbsp;&nbsp;");
        return actionLink.toString();
    }

    private String restoreStudySubjectLinkBuilder(StudySubjectBean studySubject) {
        HtmlBuilder actionLink = new HtmlBuilder();
        actionLink.a().href(
                "RestoreStudySubject?action=confirm&id=" + studySubject.getId() + "&subjectId=" + studySubject.getSubjectId() + "&studyId="
                    + studySubject.getStudyId());
        actionLink.append("onMouseDown=\"javascript:setImage('bt_Restor3','images/bt_Restore_d.gif');\"");
        actionLink.append("onMouseUp=\"javascript:setImage('bt_Restor3','images/bt_Restore_d.gif');\"").close();
        actionLink.img().name("bt_Restore1").src("images/bt_Restore.gif").border("0").alt(resword.getString("restore")).title(resword.getString("restore")).append("hspace=\"2\"").end().aEnd();
        return actionLink.toString();
    }

    private String eventDivBuilder(SubjectBean subject, Integer rowCount, List<StudyEventBean> studyEvents, StudyEventDefinitionBean sed,
            StudySubjectBean studySubject) {

        String studySubjectLabel = studySubject.getLabel();

        String divWidth = studyEvents.size() >= 3 ? "540" : studyEvents.size() == 2 ? "360" : "180";

        HtmlBuilder eventDiv = new HtmlBuilder();

        eventDiv.table(0).border("0").cellpadding("0").cellspacing("0").close();
        // Lock Div
        eventDiv.div().id("S_Lock_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount).style(
                "position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;").close();
        lockLinkBuilder(eventDiv, studySubjectLabel, rowCount, studyEvents, sed);
        eventDiv.divEnd();

        eventDiv.tr(0).valign("top").close().td(0).close();
        // Event Div
        eventDiv.div().id("S_Event_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount).style(
                "position: absolute; visibility: hidden; z-index: 3;width:" + divWidth + "px; top: 0px; float: left;").close();
        eventDiv.div().styleClass("box_T").close().div().styleClass("box_L").close().div().styleClass("box_R").close().div().styleClass("box_B").close().div()
                .styleClass("box_TL").close().div().styleClass("box_TR").close().div().styleClass("box_BL").close().div().styleClass("box_BR").close();

        eventDiv.div().styleClass("tablebox_center").close();
        eventDiv.div().styleClass("ViewSubjectsPopup").style("color: rgb(91, 91, 91);").close();

        eventDiv.table(0).border("0").cellpadding("0").cellspacing("0").close();
        eventDiv.tr(0).valign("top").close();

        singleEventDivBuilder(eventDiv, subject, rowCount, studyEvents, sed, studySubject);

        return eventDiv.toString();
    }

    private void singleEventDivBuilder(HtmlBuilder eventDiv, SubjectBean subject, Integer rowCount, List<StudyEventBean> studyEvents,
            StudyEventDefinitionBean sed, StudySubjectBean studySubject) {

        String tableHeaderRowLeftStyleClass = "table_header_row_left";
        String click_for_more_options = resword.getString("click_for_more_options");
        String schedule = resword.getString("schedule");
        String view = resword.getString("view")+"/"+resword.getString("enter_data");
        String edit = resword.getString("edit");;
        String remove = resword.getString("remove");;
        String subjectText = resword.getString("subject");;
        String eventText = resword.getString("event");;

        SubjectEventStatus eventStatus = studyEvents.size() == 0 ? SubjectEventStatus.NOT_SCHEDULED : studyEvents.get(0).getSubjectEventStatus();
        // String studyEventName = studyEvents.size() == 0 ? "" : studyEvents.get(0).getName();
        String studyEventId = studyEvents.size() == 0 ? "" : String.valueOf(studyEvents.get(0).getId());
        Status eventSysStatus = studySubject.getStatus();
        String studySubjectLabel = studySubject.getLabel();

        eventDiv.td(0).styleClass(tableHeaderRowLeftStyleClass).close();
        eventDiv.append(subjectText).append(": ").append(studySubjectLabel).br();
        eventDiv.append(eventText).append(": ").append(sed.getName()).br();

        eventDiv.append(resword.getString("status")).append(":").append(eventStatus.getName()).br();
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

            if (eventStatus == SubjectEventStatus.NOT_SCHEDULED && currentRole.getRole() != Role.MONITOR && !studyBean.getStatus().isFrozen()) {
                eventDiv.tr(0).valign("top").close();
                eventDiv.td(0).styleClass("table_cell_left").close();
                createNewStudyEventLinkBuilder(eventDiv, studySubject.getId(), sed, schedule);
                eventDiv.tdEnd().trEnd(0);
            }

            else if (eventStatus == SubjectEventStatus.COMPLETED) {
                eventDiv.tr(0).valign("top").close();
                eventDiv.td(0).styleClass("table_cell_left").close();
                enterDataForStudyEventLinkBuilder(eventDiv, studyEventId, view);
                eventDiv.tdEnd().trEnd(0);
                if ((currentRole.getRole() == Role.STUDYDIRECTOR || currentUser.isSysAdmin()) && studyBean.getStatus() == Status.AVAILABLE) {
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

            else if (eventStatus == SubjectEventStatus.LOCKED) {
                eventDiv.tdEnd().trEnd(0);
                if (currentRole.getRole() == Role.STUDYDIRECTOR || currentUser.isSysAdmin()) {
                    eventDiv.tr(0).valign("top").close();
                    eventDiv.td(0).styleClass("table_cell_left").close();
                    enterDataForStudyEventLinkBuilder(eventDiv, studyEventId, view);
                    eventDiv.tdEnd().trEnd(0);
                    if (studyBean.getStatus() == Status.AVAILABLE) {
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
                if ((currentRole.getRole() == Role.STUDYDIRECTOR || currentUser.isSysAdmin()) && studyBean.getStatus() == Status.AVAILABLE) {
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
        builder.img().src("images/bt_Edit.gif").border("0").align("left").close().aEnd();
        builder.nbsp().nbsp().a().href(href1);
        builder.close().append(edit).aEnd();

    }

    private void removeStudyEventLinkBuilder(HtmlBuilder builder, Integer studySubjectId, String studyEventId, String remove) {
        String href1 = "RemoveStudyEvent?action=confirm&id=" + studyEventId + "&studySubId=" + studySubjectId;
        builder.a().href(href1);
        builder.close();
        builder.img().src("images/bt_Remove.gif").border("0").align("left").close().aEnd();
        builder.nbsp().nbsp().a().href(href1);
        builder.close().append(remove).aEnd();

    }

    private void createNewStudyEventLinkBuilder(HtmlBuilder builder, Integer studySubjectId, StudyEventDefinitionBean sed, String schedule) {
        String href1 = "CreateNewStudyEvent?studySubjectId=" + studySubjectId + "&studyEventDefinition=" + sed.getId();
        builder.a().href(href1);
        builder.close();
        builder.img().src("images/bt_Schedule.gif").border("0").align("left").close().aEnd();
        builder.nbsp().nbsp().a().href(href1);
        builder.close().append(schedule).aEnd();

    }

    private void enterDataForStudyEventLinkBuilder(HtmlBuilder builder, String studyEventId, String view) {
        String href1 = "EnterDataForStudyEvent?eventId=" + studyEventId;
        builder.a().href(href1);
        builder.close();
        builder.img().src("images/bt_View.gif").border("0").align("left").close().aEnd();
        builder.nbsp().nbsp().a().href(href1);
        builder.close().append(view).aEnd();

    }

    private void lockLinkBuilder(HtmlBuilder builder, String studySubjectLabel, Integer rowCount, List<StudyEventBean> studyEvents, StudyEventDefinitionBean sed) {
        String href1 = "javascript:leftnavExpand('S_Menu_on_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        String href2 = "javascript:leftnavExpand('S_Menu_off_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        String onmouseover = "layersShowOrHide('visible','S_Event_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        onmouseover += "javascript:setImage('ExpandIcon_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "','images/icon_collapse.gif');";
        String onClick1 = "layersShowOrHide('hidden','Lock_all'); ";
        String onClick2 = "layersShowOrHide('hidden','S_Event_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        String onClick3 = "layersShowOrHide('hidden','S_Lock_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        String onClick4 = "javascript:setImage('ExpandIcon_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "','images/icon_blank.gif'); ";
        builder.a().href(href1 + href2);
        builder.onmouseover(onmouseover);
        builder.onclick(onClick1 + onClick2 + onClick3 + onClick4);
        builder.close();
        builder.img().src("images/spacer.gif").border("0").append("height=\"30\"").width("50").close().aEnd();

    }

    private void iconLinkBuilder(HtmlBuilder builder, String studySubjectLabel, Integer rowCount, List<StudyEventBean> studyEvents, StudyEventDefinitionBean sed) {
        String href1Repeating =
            "javascript:ExpandEventOccurrences('" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'," + studyEvents.size() + "); ";
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
        builder.onmouseover(onmouseover);
        builder.onmouseout(onmouseout);
        builder.onclick(onClick1 + onClick2);
        builder.close();

    }

    private void linkBuilder(HtmlBuilder builder, String studySubjectLabel, Integer rowCount, List<StudyEventBean> studyEvents, StudyEventDefinitionBean sed) {
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
}
