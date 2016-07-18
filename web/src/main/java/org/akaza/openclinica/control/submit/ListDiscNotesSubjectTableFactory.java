package org.akaza.openclinica.control.submit;

import org.akaza.openclinica.bean.core.ResolutionStatus;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.*;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.control.AbstractTableFactory;
import org.akaza.openclinica.control.DefaultActionsEditor;
import org.akaza.openclinica.dao.managestudy.*;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.dao.submit.SubjectGroupMapDAO;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
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

public class ListDiscNotesSubjectTableFactory extends AbstractTableFactory {

    private StudyEventDefinitionDAO studyEventDefinitionDao;
    private StudySubjectDAO studySubjectDAO;
    private SubjectDAO subjectDAO;
    private StudyEventDAO studyEventDAO;
    private StudyGroupClassDAO studyGroupClassDAO;
    private SubjectGroupMapDAO subjectGroupMapDAO;
    private StudyGroupDAO studyGroupDAO;
    private StudyDAO studyDAO;
    private EventCRFDAO eventCRFDAO;
    private EventDefinitionCRFDAO eventDefintionCRFDAO;
    private DiscrepancyNoteDAO discrepancyNoteDAO;
    private StudyBean studyBean;
    private String[] columnNames = new String[] {};
    private ArrayList<StudyEventDefinitionBean> studyEventDefinitions;
    private ArrayList<StudyGroupClassBean> studyGroupClasses;
    private StudyUserRoleBean currentRole;
    private UserAccountBean currentUser;
    private ResourceBundle resword;
    private ResourceBundle resformat;
    private ResourceBundle resterm;
    private String module;
    private Integer resolutionStatus;
    private Integer discNoteType;
    private Boolean studyHasDiscNotes;
    private Set<Integer> resolutionStatusIds;

    final HashMap<Integer, String> imageIconPaths = new HashMap<Integer, String>(8);
    final HashMap<Integer, String> discNoteIconPaths = new HashMap<Integer, String>(8);

    public ListDiscNotesSubjectTableFactory(ResourceBundle resterm) {
        this.resterm = resterm;
        imageIconPaths.put(1, "images/icon_Scheduled.gif");
        imageIconPaths.put(2, "images/icon_NotStarted.gif");
        imageIconPaths.put(3, "images/icon_InitialDE.gif");
        imageIconPaths.put(4, "images/icon_DEcomplete.gif");
        imageIconPaths.put(5, "images/icon_Stopped.gif");
        imageIconPaths.put(6, "images/icon_Skipped.gif");
        imageIconPaths.put(7, "images/icon_Locked.gif");
        imageIconPaths.put(8, "images/icon_Signed.gif");

        discNoteIconPaths.put(1, "<img name='icon_Note' src='images/icon_Note.gif' border='0' alt='" + resterm.getString("Open") + "' title='"
            + resterm.getString("Open") + "' align='left'/>");
        discNoteIconPaths.put(2, "<img name='icon_flagYellow' src='images/icon_flagYellow.gif' border='0' alt='" + resterm.getString("Updated") + "' title='"
            + resterm.getString("Updated") + "' align='left'/>");
        discNoteIconPaths.put(3, "<img name='icon_flagGreen' src='images/icon_flagGreen.gif' border='0' alt='" + resterm.getString("Resolved") + "' title='"
            + resterm.getString("Resolved") + "' align='left'/>");
        discNoteIconPaths.put(4, "<img name='icon_flagBlack' src='images/icon_flagBlack.gif' border='0' alt='" + resterm.getString("Closed") + "' title='"
            + resterm.getString("Closed") + "' align='left'/>");
        discNoteIconPaths.put(5, "<img name='icon_flagWhite' src='images/icon_flagWhite.gif' border='0' alt='" + resterm.getString("Not_Applicable")
            + "' title='" + resterm.getString("Not_Applicable") + "' align='left'/>");

    }

    @Override
    protected String getTableName() {
        return "listDiscNotes";
    }

    @Override
    protected void configureColumns(TableFacade tableFacade, Locale locale) {
        resword = ResourceBundleProvider.getWordsBundle(locale);
        resformat = ResourceBundleProvider.getFormatBundle(locale);
        tableFacade.setColumnProperties(columnNames);
        Row row = tableFacade.getTable().getRow();
        configureColumn(row.getColumn(columnNames[0]), resword.getString("study_subject_ID"), null, null);
        configureColumn(row.getColumn(columnNames[1]), resword.getString("subject_status"), new StatusCellEditor(), new StatusDroplistFilterEditor());
        configureColumn(row.getColumn(columnNames[2]), "Site ID", null, null);

        // study event definition columns
        for (int i = 3; i < columnNames.length - 1; i++) {
            StudyEventDefinitionBean studyEventDefinition = studyEventDefinitions.get(i - 3);
            configureColumn(row.getColumn(columnNames[i]), studyEventDefinition.getName(), new StudyEventDefinitionMapCellEditor(),
                    new SubjectEventStatusDroplistFilterEditor(), true, false);
        }
        String actionsHeader = resword.getString("rule_actions") + "&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;";
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
        // tableFacade.addFilterMatcher(new MatcherKey(Integer.class), new
        // SubjectEventStatusFilterMatcher());

        for (int i = 3; i < columnNames.length - 1; i++) {
            tableFacade.addFilterMatcher(new MatcherKey(Integer.class, columnNames[i]), new SubjectEventStatusFilterMatcher());
        }

    }

    @Override
    public void configureTableFacadePostColumnConfiguration(TableFacade tableFacade) {
        ListDiscNotesSubjectTableToolbar toolbar = new ListDiscNotesSubjectTableToolbar(getStudyEventDefinitions());
        toolbar.setStudyHasDiscNotes(studyHasDiscNotes);
        toolbar.setDiscNoteType(discNoteType);
        toolbar.setResolutionStatus(resolutionStatus);
        toolbar.setResword(resword);
        toolbar.setModule(module);
        tableFacade.setToolbar(toolbar);
    }

    @Override
    public void setDataAndLimitVariables(TableFacade tableFacade) {
        StudyBean study = this.getStudyBean();
        Limit limit = tableFacade.getLimit();

        ListDiscNotesSubjectFilter subjectFilter = getSubjectFilter(limit);
        subjectFilter.addFilter("dn.discrepancy_note_type_id", this.discNoteType);
        StringBuffer constraints = new StringBuffer();
        if (this.discNoteType > 0 && this.discNoteType < 10) {
            constraints.append(" and dn.discrepancy_note_type_id=" + this.discNoteType);
        }
        if (this.resolutionStatusIds != null && this.resolutionStatusIds.size() > 0) {
            String s = " and (";
            for (Integer resolutionStatusId : this.resolutionStatusIds) {
                s += "dn.resolution_status_id = " + resolutionStatusId + " or ";
            }
            s = s.substring(0, s.length() - 3) + " )";
            subjectFilter.addFilter("dn.resolution_status_id", s);
            constraints.append(s);
        }

        if (!limit.isComplete()) {
            int totalRows = getStudySubjectDAO().getCountWithFilter(subjectFilter, study);
            tableFacade.setTotalRows(totalRows);
        }

        ListDiscNotesSubjectSort subjectSort = getSubjectSort(limit);

        int rowStart = limit.getRowSelect().getRowStart();
        int rowEnd = limit.getRowSelect().getRowEnd();
        Collection<StudySubjectBean> items = getStudySubjectDAO().getWithFilterAndSort(study, subjectFilter, subjectSort, rowStart, rowEnd);

        Collection<HashMap<Object, Object>> theItems = new ArrayList<HashMap<Object, Object>>();

        boolean hasDN = false;
        for (StudySubjectBean studySubjectBean : items) {
            HashMap<Object, Object> theItem = new HashMap<Object, Object>();
            theItem.put("studySubject", studySubjectBean);
            theItem.put("studySubject.label", studySubjectBean.getLabel());
            theItem.put("studySubject.status", studySubjectBean.getStatus());
            theItem.put("enrolledAt", ((StudyBean) getStudyDAO().findByPK(studySubjectBean.getStudyId())).getIdentifier());

            // Get All study events for this study subject and then put list in
            // HashMap with study event definition id as
            // key and a list of study events as the value.
            List<StudyEventBean> allStudyEventsForStudySubject = getStudyEventDAO().findAllByStudySubject(studySubjectBean);
            HashMap<Integer, List<StudyEventBean>> allStudyEventsForStudySubjectBySedId = new HashMap<Integer, List<StudyEventBean>>();
            theItem.put("isSignable", isSignable(allStudyEventsForStudySubject));

            for (StudyEventBean studyEventBean : allStudyEventsForStudySubject) {
                if (allStudyEventsForStudySubjectBySedId.get(studyEventBean.getStudyEventDefinitionId()) == null) {
                    ArrayList<StudyEventBean> a = new ArrayList<StudyEventBean>();
                    a.add(studyEventBean);
                    allStudyEventsForStudySubjectBySedId.put(studyEventBean.getStudyEventDefinitionId(), a);
                } else {
                    allStudyEventsForStudySubjectBySedId.get(studyEventBean.getStudyEventDefinitionId()).add(studyEventBean);
                }

            }

            for (StudyEventDefinitionBean studyEventDefinition : getStudyEventDefinitions()) {

                List<StudyEventBean> studyEvents = allStudyEventsForStudySubjectBySedId.get(studyEventDefinition.getId());
                SubjectEventStatus subjectEventStatus = null;
                HashMap<ResolutionStatus, Integer> discCounts = new HashMap<ResolutionStatus, Integer>();

                studyEvents = studyEvents == null ? new ArrayList<StudyEventBean>() : studyEvents;
                if (studyEvents.size() < 1) {
                    subjectEventStatus = SubjectEventStatus.NOT_SCHEDULED;
                } else {
                    for (StudyEventBean studyEventBean : studyEvents) {
                        discCounts = countAll(discCounts, studyEventBean, constraints, study.isSite(study.getParentStudyId()));
                        hasDN = hasDN == false ? discCounts.size() > 0 : hasDN;
                        if (studyEventBean.getSampleOrdinal() == 1) {
                            subjectEventStatus = studyEventBean.getSubjectEventStatus();
                            // break;
                        }
                    }

                }
                theItem.put("sed_" + studyEventDefinition.getId() + "_discCounts", discCounts);
                theItem.put("sed_" + studyEventDefinition.getId(), subjectEventStatus.getId());
                theItem.put("sed_" + studyEventDefinition.getId() + "_studyEvents", studyEvents);
                theItem.put("sed_" + studyEventDefinition.getId() + "_object", studyEventDefinition);

            }

            theItems.add(theItem);
        }

        // Do not forget to set the items back on the tableFacade.
        tableFacade.setItems(theItems);

        setStudyHasDiscNotes(hasDN);
    }

    private Boolean isSignable(List<StudyEventBean> allStudyEventsForStudySubject) {
        boolean isSignable = true;
        boolean isRequiredUncomplete;
        for (StudyEventBean studyEventBean : allStudyEventsForStudySubject) {
            if (studyEventBean.getSubjectEventStatus() == SubjectEventStatus.DATA_ENTRY_STARTED) {
                isSignable = false;
                break;
            } else {
                isRequiredUncomplete = eventHasRequiredUncompleteCRFs(studyEventBean);
                if (isRequiredUncomplete) {
                    isSignable = false;
                    break;
                }
            }
        }
        return isSignable;
    }

    @SuppressWarnings("unchecked")
    private boolean eventHasRequiredUncompleteCRFs(StudyEventBean studyEventBean) {

        List<EventCRFBean> eventCrfBeans = new ArrayList<EventCRFBean>();
        eventCrfBeans.addAll(getEventCRFDAO().findAllByStudyEvent(studyEventBean));
        // If the EventCRFBean has a completionStatusId of 0 (indicating that it
        // is not complete),
        // then find out whether it's required. If so, then return from the
        // method false.
        for (EventCRFBean crfBean : eventCrfBeans) {
            if (crfBean != null && crfBean.getCompletionStatusId() == 0) {
                if (getEventDefintionCRFDAO().isRequiredInDefinition(crfBean.getCRFVersionId(), studyEventBean)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void getColumnNamesMap() {
        ArrayList<String> columnNamesList = new ArrayList<String>();
        columnNamesList.add("studySubject.label");
        columnNamesList.add("studySubject.status");
        columnNamesList.add("enrolledAt");

        for (StudyEventDefinitionBean studyEventDefinition : getStudyEventDefinitions()) {
            columnNamesList.add("sed_" + studyEventDefinition.getId());
        }
        columnNamesList.add("actions");
        columnNames = columnNamesList.toArray(columnNames);
    }

    protected ListDiscNotesSubjectFilter getSubjectFilter(Limit limit) {
        ListDiscNotesSubjectFilter listDiscNotesSubjectFilter = new ListDiscNotesSubjectFilter();
        FilterSet filterSet = limit.getFilterSet();
        Collection<Filter> filters = filterSet.getFilters();
        for (Filter filter : filters) {
            String property = filter.getProperty();
            String value = filter.getValue();
            listDiscNotesSubjectFilter.addFilter(property, value);
        }

        return listDiscNotesSubjectFilter;
    }

    /**
     * A very custom way to sort the items. The PresidentSort acts as a command
     * for the Hibernate criteria object. There are probably many ways to do
     * this, but this is the most flexible way I have found. The point is you
     * need to somehow take the Limit information and sort the rows.
     *
     * @param limit
     *            The Limit to use.
     */
    protected ListDiscNotesSubjectSort getSubjectSort(Limit limit) {
        ListDiscNotesSubjectSort listDiscNotesSubjectSort = new ListDiscNotesSubjectSort();
        SortSet sortSet = limit.getSortSet();
        Collection<Sort> sorts = sortSet.getSorts();
        for (Sort sort : sorts) {
            String property = sort.getProperty();
            String order = sort.getOrder().toParam();
            listDiscNotesSubjectSort.addSort(property, order);
        }

        return listDiscNotesSubjectSort;
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

    public DiscrepancyNoteDAO getDiscrepancyNoteDAO() {
        return discrepancyNoteDAO;
    }

    public void setDiscrepancyNoteDAO(DiscrepancyNoteDAO discrepancyNoteDAO) {
        this.discrepancyNoteDAO = discrepancyNoteDAO;
    }

    public ResourceBundle getResword() {
        return resword;
    }

    public void setResword(ResourceBundle resword) {
        this.resword = resword;
    }

    public ResourceBundle getResterm() {
        return resterm;
    }

    public void setResterm(ResourceBundle resterm) {
        this.resterm = resterm;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public UserAccountBean getCurrentUser() {
        return currentUser;
    }

    public Integer getResolutionStatus() {
        return resolutionStatus;
    }

    public void setResolutionStatus(Integer resolutionStatus) {
        this.resolutionStatus = resolutionStatus;
    }

    public Integer getDiscNoteType() {
        return discNoteType;
    }

    public void setDiscNoteType(Integer discNoteType) {
        this.discNoteType = discNoteType;
    }

    public Boolean isStudyHasDiscNotes() {
        return studyHasDiscNotes;
    }

    public void setStudyHasDiscNotes(Boolean studyHasDiscNotes) {
        this.studyHasDiscNotes = studyHasDiscNotes;
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

            String item = StringUtils.lowerCase(String.valueOf(((Status) itemValue).getId()));
            String filter = StringUtils.lowerCase(String.valueOf(filterValue));

            if (filter.equals(item)) {
                return true;
            }
            return false;
        }
    }

    public class SubjectEventStatusFilterMatcher implements FilterMatcher {
        public boolean evaluate(Object itemValue, String filterValue) {
            String item = StringUtils.lowerCase(String.valueOf(itemValue));
            String filter = StringUtils.lowerCase(String.valueOf(filterValue));
            if (filter.equals(item)) {
                return true;
            }
            return false;
        }
    }

    public class SubjectGroupFilterMatcher implements FilterMatcher {

        public boolean evaluate(Object itemValue, String filterValue) {

            String item = StringUtils.lowerCase(String.valueOf(itemValue).trim());
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

    private class StatusDroplistFilterEditor extends DroplistFilterEditor {
        @Override
        protected List<Option> getOptions() {
            List<Option> options = new ArrayList<Option>();
            for (Object status : Status.toActiveArrayList()) {
                ((Status) status).getName();
                options.add(new Option(String.valueOf(((Status) status).getId()), ((Status) status).getName()));
            }
            return options;
        }
    }

    private class SubjectEventStatusDroplistFilterEditor extends DroplistFilterEditor {
        @Override
        protected List<Option> getOptions() {
            List<Option> options = new ArrayList<Option>();
            for (Object subjectEventStatus : SubjectEventStatus.toArrayList()) {
                ((SubjectEventStatus) subjectEventStatus).getName();
                options.add(new Option(String.valueOf(((SubjectEventStatus) subjectEventStatus).getId()), ((SubjectEventStatus) subjectEventStatus).getName()));
            }
            return options;
        }
    }

    private class StudyEventDefinitionMapCellEditor implements CellEditor {

        StudyEventDefinitionBean studyEventDefinition;
        StudySubjectBean studySubjectBean;
        SubjectEventStatus subjectEventStatus;
        List<StudyEventBean> studyEvents;
        SubjectBean subject;
        HashMap<ResolutionStatus, Integer> discCounts;

        private String getCount() {
            return studyEvents.size() < 2 ? "" : "&nbsp;x" + String.valueOf(studyEvents.size() + "");
        }

        @SuppressWarnings("unchecked")
        public Object getValue(Object item, String property, int rowcount) {

            studyEvents = (List<StudyEventBean>) ((HashMap<Object, Object>) item).get(property + "_studyEvents");
            studyEventDefinition = (StudyEventDefinitionBean) ((HashMap<Object, Object>) item).get(property + "_object");
            subjectEventStatus = SubjectEventStatus.get((Integer) ((HashMap<Object, Object>) item).get(property));
            subject = (SubjectBean) ((HashMap<Object, Object>) item).get("subject");
            studySubjectBean = (StudySubjectBean) ((HashMap<Object, Object>) item).get("studySubject");
            discCounts = (HashMap<ResolutionStatus, Integer>) ((HashMap<Object, Object>) item).get(property + "_discCounts");

            StringBuilder url = new StringBuilder();
            url.append("<table><tr><td><img src='" + imageIconPaths.get(subjectEventStatus.getId()) + "' border='0'>");
            url.append(getCount() + "</td></tr>");
            url.append("<tr>");
            for (ResolutionStatus key : discCounts.keySet()) {
                url.append("<td>");
                url.append(discNoteIconPaths.get(key.getId()) + "(" + discCounts.get(key) + ")");
                url.append("</td>");
            }
            if (discCounts.keySet().size() <= 1) {
                url.append("<td>");
                url.append("<img border=\"0\" src=\"images/icon_transparent.gif\"/>&nbsp;&nbsp;");
                url.append("</td>");
                url.append("<td>");
                url.append("<img border=\"0\" src=\"images/icon_transparent.gif\"/>&nbsp;&nbsp;");
                if (studyEventDefinition.getName().length() > 9) {
                    int totNumOfSpaces = studyEventDefinition.getName().length() - 9;
                    for (int i = 0; i < totNumOfSpaces; i++) {
                        url.append("&nbsp;");
                    }
                }
                url.append("</td>");
            }
            url.append("</tr></table>");

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
                url.append(viewNotesLinkBuilder(studySubjectBean));
                url.append(downloadNotesLinkBuilder(studySubjectBean));
                value = url.toString();
            }

            return value;
        }
    }

    private String viewNotesLinkBuilder(StudySubjectBean studySubject) {
        HtmlBuilder actionLink = new HtmlBuilder();
        if (this.getResolutionStatus() >= 1 && this.getResolutionStatus() <= 5) {
            actionLink.a().href(
                    "ViewNotes?viewForOne=y&id=" + studySubject.getId() + "&resolutionStatus=" + resolutionStatus + "discNoteType=" + discNoteType + "&module="
                        + module + "&listNotes_f_studySubject.label=" + studySubject.getLabel());
            actionLink.append("onMouseDown=\"javascript:setImage('bt_View1','images/bt_View_d.gif');\"");
            actionLink.append("onMouseUp=\"javascript:setImage('bt_View1','images/bt_View.gif');\"").close();
            actionLink.img().name("bt_View1").src("images/bt_View.gif").border("0").alt(resword.getString("view")).title(resword.getString("view")).append(
                    "hspace=\"4\" style=\"float:left\" width=\"24 \" height=\"15\" align=\"left\"").end().aEnd();
            actionLink.append("&nbsp;&nbsp;&nbsp;");
        } else {
            actionLink.a().href(
                    "ViewNotes?viewForOne=y&id=" + studySubject.getId() + "&module=" + module + "&listNotes_f_studySubject.label=" + studySubject.getLabel());
            actionLink.append("onMouseDown=\"javascript:setImage('bt_View1','images/bt_View_d.gif');\"");
            actionLink.append("onMouseUp=\"javascript:setImage('bt_View1','images/bt_View.gif');\"").close();
            actionLink.img().name("bt_View1").src("images/bt_View.gif").border("0").alt(resword.getString("view")).title(resword.getString("view")).append(
                    "hspace=\"2\" style=\"float:left\" width=\"24 \" height=\"15\" align=\"left\"").end().aEnd();
            actionLink.append("&nbsp;&nbsp;&nbsp;");
        }
        return actionLink.toString();
    }

    private String downloadNotesLinkBuilder(StudySubjectBean studySubject) {
        HtmlBuilder actionLink = new HtmlBuilder();
        if (this.isStudyHasDiscNotes()) {
            if (this.getResolutionStatus() >= 1 && this.getResolutionStatus() <= 5) {
                actionLink.a().href(
                        "javascript:openDocWindow('ChooseDownloadFormat?subjectId=" + studySubject.getId() + "&discNoteType=" + discNoteType
                            + "&resolutionStatus=" + resolutionStatus + "')").close();
                actionLink.img().name("bt_Download").src("images/bt_Download.gif").border("0").alt(resword.getString("download_discrepancy_notes")).title(
                        resword.getString("download_discrepancy_notes")).append("hspace=\"4\" width=\"24 \" height=\"15\"").end().aEnd();
                actionLink.append("&nbsp;&nbsp;&nbsp;");
            } else {
                actionLink.a().href(
                        "javascript:openDocWindow('ChooseDownloadFormat?subjectId=" + studySubject.getId() + "&discNoteType=" + discNoteType + "&module="
                            + module + "')").close();
                actionLink.img().name("bt_View1").src("images/bt_Download.gif").border("0").alt(resword.getString("download_discrepancy_notes")).title(
                        resword.getString("download_discrepancy_notes")).append("hspace=\"2\" width=\"24 \" height=\"15\"").end().aEnd();
                actionLink.append("&nbsp;&nbsp;&nbsp;");
            }
        }
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
        actionLink.img().name("bt_Restore1").src("images/bt_Remove.gif").border("0").alt(resword.getString("restore")).title(resword.getString("restore")).align("left").append("hspace=\"6\"").end()
                .aEnd();
        return actionLink.toString();

    }

    private String eventDivBuilder(SubjectBean subject, Integer rowCount, List<StudyEventBean> studyEvents, StudyEventDefinitionBean sed,
            StudySubjectBean studySubject) {

        String studySubjectLabel = studySubject.getLabel();

        String divWidth = studyEvents.size() >= 3 ? "540" : studyEvents.size() == 2 ? "360" : "180";

        HtmlBuilder eventDiv = new HtmlBuilder();

        eventDiv.table(0).border("0").cellpadding("0").cellspacing("0").close();
        // Lock Div
        eventDiv.div().id("Lock_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount).style(
                "position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;").close();
        lockLinkBuilder(eventDiv, studySubjectLabel, rowCount, studyEvents, sed);
        eventDiv.divEnd();

        eventDiv.tr(0).valign("top").close().td(0).close();
        // Event Div
        eventDiv.div().id("Event_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount).style(
                "position: absolute; visibility: hidden; z-index: 3;width:" + divWidth + "px; top: 0px; float: left;").close();
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
        String view = resword.getString("view")+"/"+resword.getString("enter_data");
        String edit = resword.getString("edit");
        String remove = resword.getString("remove");
        String occurrence_x_of = resword.getString("ocurrence");
        String subjectText = resword.getString("subject");
        String eventText = resword.getString("event");
        String status = resword.getString("status");

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
        if (eventSysStatus != Status.DELETED && eventSysStatus != Status.AUTO_DELETED && studyBean.getStatus() == Status.AVAILABLE) {
            eventDiv.span().styleClass("font-weight: normal;").close();
            eventDiv.ahref("CreateNewStudyEvent?studySubjectId=" + studySubject.getId() + "&studyEventDefinition=" + sed.getId(), add_another_occurrence);
        }
        eventDiv.nbsp().nbsp().nbsp();
        for (int i = 1; i <= studyEventsSize; i++) {
            eventDiv.ahref("javascript:StatusBoxSkip('" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'," + studyEventsSize + "," + i + ");",
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
        eventDiv.img().src("images/arrow_status_back_dis.gif").border("0").close();
        eventDiv.tdEnd();
        // <td>...</td>
        eventDiv.td(0).id("Scroll_on_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "_back").styleClass("statusbox_scroll_L").width("20").style(
                "display: none;").close();
        // <div>...</div>
        eventDiv.div().id("bt_Scroll_Event_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "_back").style("display: none;").close();
        eventDiv.a().href("javascript:StatusBoxBack('" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'," + studyEventsSize + ");").close();
        eventDiv.img().src("images/arrow_status_back.gif").border("0").close();
        eventDiv.aEnd();
        eventDiv.divEnd();
        // <div>...</div>
        eventDiv.div().id("bt_Scroll_Event_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "_back_dis").close();
        eventDiv.img().src("images/arrow_status_back_dis.gif").border("0").close();
        eventDiv.divEnd();
        eventDiv.tdEnd();

        for (int i = 0; i < studyEvents.size(); i++) {
            StudyEventBean studyEventBean = studyEvents.get(i);
            // <td>...</td>
            eventDiv.td(0).id("Event_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "_" + (i + 1)).valign("top").width("180");
            if (i + 1 > 3) {
                eventDiv.style("display: none;");
            }
            eventDiv.close();
            // <table>...</table>
            eventDiv.table(0).border("0").cellpadding("0").cellspacing("0").close();
            // <tr><td>...</td></tr>
            eventDiv.tr(0).valign("top").close();
            eventDiv.td(0).styleClass(tableHeaderRowStyleClass).colspan("2").close();
            eventDiv.bold().append(occurrence_x_of).append("#" + (i + 1) + " of " + studyEventsSize).br();
            eventDiv.append(formatDate(studyEventBean.getDateStarted())).br();
            eventDiv.append(status + ": " + studyEventBean.getSubjectEventStatus().getName());
            eventDiv.boldEnd().tdEnd().trEnd(0);
            // <tr><td><table>...</table></td></tr>
            eventDiv.tr(0).id("Menu_on_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "_" + (i + 1)).styleClass("display: none").close();
            eventDiv.td(0).colspan("2").close();
            eventDiv.table(0).border("0").cellpadding("0").cellspacing("0").close();

            linksDivBuilder(eventDiv, subject, rowCount, studyEvents, sed, studySubject, studyEventBean);
            eventDiv.tableEnd(0).tdEnd().trEnd(0);
            eventDiv.tableEnd(0);
            eventDiv.tdEnd();
        }

        // <td>...</td>
        eventDiv.td(0).id("Scroll_off_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "_next").styleClass("statusbox_scroll_R_dis").width("20")
                .close();
        eventDiv.img().src("images/arrow_status_next_dis.gif").border("0").close();
        eventDiv.tdEnd();
        // <td>...</td>
        eventDiv.td(0).id("Scroll_on_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "_next").styleClass("statusbox_scroll_R").width("20").style(
                "display: none;").close();
        // <div>...</div>
        eventDiv.div().id("bt_Scroll_Event_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "_next").close();
        eventDiv.a().href("javascript:StatusBoxNext('" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'," + studyEventsSize + ");").close();
        eventDiv.img().src("images/arrow_status_next.gif").border("0").close();
        eventDiv.aEnd();
        eventDiv.divEnd();
        // <div>...</div>
        eventDiv.div().id("bt_Scroll_Event_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "_next_dis").style("display: none;").close();
        eventDiv.img().src("images/arrow_status_next_dis.gif").border("0").close();
        eventDiv.divEnd();
        eventDiv.tdEnd().trEnd(0);

        eventDiv.tr(0).id("Menu_off_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount).style("").close();
        eventDiv.td(0).styleClass("table_cell_left").colspan(String.valueOf(studyEventsSize)).close().append("<i>").append(click_for_more_options).append(
                "</i>").tdEnd();
        eventDiv.trEnd(0);

        eventDiv.tableEnd(0);
        eventDiv.divEnd().divEnd().divEnd().divEnd().divEnd().divEnd().divEnd().divEnd().divEnd().divEnd().divEnd();
        repeatingIconLinkBuilder(eventDiv, studySubjectLabel, rowCount, studyEvents, sed);

    }

    private void linksDivBuilder(HtmlBuilder eventDiv, SubjectBean subject, Integer rowCount, List<StudyEventBean> studyEvents, StudyEventDefinitionBean sed,
            StudySubjectBean studySubject, StudyEventBean currentEvent) {

        Status eventSysStatus = studySubject.getStatus();
        SubjectEventStatus eventStatus = currentEvent.getSubjectEventStatus();
        String studyEventId = String.valueOf(currentEvent.getId());

        String view = resword.getString("view")+"/"+resword.getString("enter_data");
        String edit = resword.getString("edit");
        String remove = resword.getString("remove");;

        if (eventSysStatus.getId() == Status.AVAILABLE.getId() || eventSysStatus == Status.SIGNED) {

            if (eventStatus == SubjectEventStatus.COMPLETED) {
                eventDiv.tr(0).valign("top").close();
                eventDiv.td(0).styleClass("table_cell").close();
                enterDataForStudyEventLinkBuilder(eventDiv, studyEventId, view);
                eventDiv.tdEnd().trEnd(0);

                if ((currentRole.getRole() == Role.STUDYDIRECTOR || currentUser.isSysAdmin()) && studyBean.getStatus() == Status.AVAILABLE) {
                    eventDiv.tr(0).valign("top").close();
                    eventDiv.td(0).styleClass("table_cell").close();
                    updateStudyEventLinkBuilder(eventDiv, studySubject.getId(), studyEventId, edit);
                    eventDiv.tdEnd().trEnd(0);
                    eventDiv.tr(0).valign("top").close();
                    eventDiv.td(0).styleClass("table_cell").close();
                    removeStudyEventLinkBuilder(eventDiv, studySubject.getId(), studyEventId, remove);
                    eventDiv.tdEnd().trEnd(0);
                }
            } else if (eventStatus == SubjectEventStatus.LOCKED) {
                if (currentRole.getRole() == Role.STUDYDIRECTOR || currentUser.isSysAdmin()) {
                    eventDiv.tr(0).valign("top").close();
                    eventDiv.td(0).styleClass("table_cell").close();
                    enterDataForStudyEventLinkBuilder(eventDiv, studyEventId, view);
                    eventDiv.tdEnd().trEnd(0);
                    if (studyBean.getStatus() == Status.AVAILABLE) {
                        eventDiv.tr(0).valign("top").close();
                        eventDiv.td(0).styleClass("table_cell").close();
                        removeStudyEventLinkBuilder(eventDiv, studySubject.getId(), studyEventId, remove);
                        eventDiv.tdEnd().trEnd(0);
                    }
                }
            } else {
                eventDiv.tr(0).valign("top").close();
                eventDiv.td(0).styleClass("table_cell_left");
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
        String view = resword.getString("view")+"/"+resword.getString("enter_data");
        String edit = resword.getString("edit");
        String remove = resword.getString("remove");
        String occurrence_x_of = resword.getString("ocurrence");
        String subjectText = resword.getString("subject");
        String eventText = resword.getString("event");
        String status = resword.getString("status");

        SubjectEventStatus eventStatus = studyEvents.size() == 0 ? SubjectEventStatus.NOT_SCHEDULED : studyEvents.get(0).getSubjectEventStatus();
        String studyEventName = studyEvents.size() == 0 ? "" : studyEvents.get(0).getName();
        String studyEventId = studyEvents.size() == 0 ? "" : String.valueOf(studyEvents.get(0).getId());
        Status eventSysStatus = studySubject.getStatus();
        String studySubjectLabel = studySubject.getLabel();

        eventDiv.td(0).styleClass(tableHeaderRowLeftStyleClass).close();
        eventDiv.append(subjectText).append(": ").append(studySubjectLabel).br();
        eventDiv.append(eventText).append(": ").append(sed.getName()).br();

        if (!sed.isRepeating()) {
            eventDiv.append(resword.getString("status")).append(":").append(eventStatus.getName()).br();
            eventDiv.tdEnd();
            eventDiv.td(0).styleClass(tableHeaderRowLeftStyleClass).align("right").close();
            linkBuilder(eventDiv, studySubjectLabel, rowCount, studyEvents, sed);
            eventDiv.tdEnd();

        } else {
            eventDiv.tdEnd();
            eventDiv.td(0).styleClass(tableHeaderRowLeftStyleClass).align("right").close();
            linkBuilder(eventDiv, studySubjectLabel, rowCount, studyEvents, sed);
            eventDiv.tdEnd();

            eventDiv.tr(0).valign("top").close();
            eventDiv.td(0).styleClass(tableHeaderRowStyleClass).colspan("2").close();
            eventDiv.bold().append(occurrence_x_of).append("#1 of 1").br();
            if (studyEvents.size() > 0) {
                eventDiv.append(formatDate(studyEvents.get(0).getDateStarted())).br();
                eventDiv.append(status + " : " + studyEvents.get(0).getSubjectEventStatus().getName());
            } else {
                eventDiv.append(status + " : " + SubjectEventStatus.NOT_SCHEDULED.getName());
            }
            eventDiv.boldEnd().tdEnd().trEnd(0);
            if (eventStatus != SubjectEventStatus.NOT_SCHEDULED && eventSysStatus != Status.DELETED && eventSysStatus != Status.AUTO_DELETED) {
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
        eventDiv.table(0).border("0").cellpadding("0").cellspacing("0").close();

        if (eventSysStatus.getId() == Status.AVAILABLE.getId() || eventSysStatus == Status.SIGNED) {

            if (eventStatus == SubjectEventStatus.NOT_SCHEDULED && currentRole.getRole() != Role.MONITOR) {
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
                eventDiv.td(0).styleClass("table_cell_left");
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
        String href1 = "javascript:leftnavExpand('Menu_on_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        String href2 = "javascript:leftnavExpand('Menu_off_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        String onmouseover = "layersShowOrHide('visible','Event_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        onmouseover += "javascript:setImage('ExpandIcon_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "','images/icon_collapse.gif');";
        String onClick1 = "layersShowOrHide('hidden','Lock_all'); ";
        String onClick2 = "layersShowOrHide('hidden','Event_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        String onClick3 = "layersShowOrHide('hidden','Lock_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        String onClick4 = "javascript:setImage('ExpandIcon_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "','images/icon_blank.gif'); ";
        builder.a().href(href1 + href2);
        builder.onmouseover(onmouseover);
        builder.onclick(onClick1 + onClick2 + onClick3 + onClick4);
        builder.close();
        builder.img().src("images/spacer.gif").border("0").append("height=\"30\"").width("50").close().aEnd();

    }

    private void repeatingIconLinkBuilder(HtmlBuilder builder, String studySubjectLabel, Integer rowCount, List<StudyEventBean> studyEvents,
            StudyEventDefinitionBean sed) {
        String href1 = "javascript:ExpandEventOccurrences('" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'," + studyEvents.size() + "); ";
        // String href1 = "javascript:leftnavExpand('Menu_on_" +
        // studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        String href2 = "javascript:leftnavExpand('Menu_off_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        String onmouseover = "moveObject('Event_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "', event); ";
        onmouseover += "setImage('ExpandIcon_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "','images/icon_expand.gif');";
        String onmouseout = "layersShowOrHide('hidden','Event_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        onmouseout += "setImage('ExpandIcon_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "','images/icon_blank.gif');";
        String onClick1 = "layersShowOrHide('visible','Lock_all'); ";
        String onClick2 = "LockObject('Lock_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "',event); ";
        builder.a().href(href1 + href2);
        builder.onmouseover(onmouseover);
        builder.onmouseout(onmouseout);
        builder.onclick(onClick1 + onClick2);
        builder.close();

    }

    private void iconLinkBuilder(HtmlBuilder builder, String studySubjectLabel, Integer rowCount, List<StudyEventBean> studyEvents, StudyEventDefinitionBean sed) {
        String href1Repeating =
            "javascript:ExpandEventOccurrences('" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'," + studyEvents.size() + "); ";
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
        builder.onmouseover(onmouseover);
        builder.onmouseout(onmouseout);
        builder.onclick(onClick1 + onClick2);
        builder.close();

    }

    private void divCloseRepeatinglinkBuilder(HtmlBuilder builder, String studySubjectLabel, Integer rowCount, List<StudyEventBean> studyEvents,
            StudyEventDefinitionBean sed) {
        String href1 = "javascript:ExpandEventOccurrences('" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'," + studyEvents.size() + "); ";
        String href2 = "javascript:leftnavExpand('Menu_off_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        String onClick1 = "layersShowOrHide('hidden','Lock_all'); ";
        String onClick2 = "layersShowOrHide('hidden','Event_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        String onClick3 = "layersShowOrHide('hidden','Lock_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        String onClick4 = "javascript:setImage('ExpandIcon_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "','images/icon_blank.gif'); ";
        builder.a().href(href1 + href2);
        builder.onclick(onClick1 + onClick2 + onClick3 + onClick4);
        builder.close().append("X").aEnd();

    }

    private void linkBuilder(HtmlBuilder builder, String studySubjectLabel, Integer rowCount, List<StudyEventBean> studyEvents, StudyEventDefinitionBean sed) {
        String href1 = "javascript:leftnavExpand('Menu_on_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        String href2 = "javascript:leftnavExpand('Menu_off_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        String onClick1 = "layersShowOrHide('hidden','Lock_all'); ";
        String onClick2 = "layersShowOrHide('hidden','Event_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
        String onClick3 = "layersShowOrHide('hidden','Lock_" + studySubjectLabel + "_" + sed.getId() + "_" + rowCount + "'); ";
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

    public Set<Integer> getResolutionStatusIds() {
        return resolutionStatusIds;
    }

    public void setResolutionStatusIds(Set<Integer> resolutionStatusIds) {
        this.resolutionStatusIds = resolutionStatusIds;
    }
    
    public HashMap<ResolutionStatus, Integer> countAll(HashMap<ResolutionStatus, Integer> discCounts, StudyEventBean studyEvent, StringBuffer constraints, boolean isSite) {
        HashMap<ResolutionStatus, Integer> temp = new HashMap<ResolutionStatus, Integer>();
        temp =
            getDiscrepancyNoteDAO().countByEntityTypeAndStudyEventWithConstraints("itemData", studyEvent, constraints, isSite);
        this.getTotal(discCounts, temp);
        temp =
            getDiscrepancyNoteDAO().countByEntityTypeAndStudyEventWithConstraints("subject", studyEvent, constraints, isSite);
        this.getTotal(discCounts, temp);
        temp =
            getDiscrepancyNoteDAO().countByEntityTypeAndStudyEventWithConstraints("eventCrf", studyEvent, constraints, isSite);
        this.getTotal(discCounts, temp);
        temp =
            getDiscrepancyNoteDAO().countByEntityTypeAndStudyEventWithConstraints("StudySub", studyEvent, constraints, isSite);
        this.getTotal(discCounts, temp);
        temp =
            getDiscrepancyNoteDAO().countByEntityTypeAndStudyEventWithConstraints("studyEvent", studyEvent, constraints, isSite);
        this.getTotal(discCounts, temp);
        
        return discCounts;
    }
    
    public HashMap<ResolutionStatus, Integer> getTotal(HashMap<ResolutionStatus, Integer> discCounts, HashMap<ResolutionStatus, Integer> discCountsTemp) {
        if(discCountsTemp.size()>0) {
            for(int i=1; i<6; ++i) {
                Integer c = 0;
                if(discCounts.get(ResolutionStatus.get(i))!=null) {
                    c = discCounts.get(ResolutionStatus.get(i));
                }
                if(discCountsTemp.get(ResolutionStatus.get(i))!=null) {
                    discCounts.put(ResolutionStatus.get(i), c+discCountsTemp.get(ResolutionStatus.get(i)));
                }
            }
        }
        return discCounts;
    }

}
