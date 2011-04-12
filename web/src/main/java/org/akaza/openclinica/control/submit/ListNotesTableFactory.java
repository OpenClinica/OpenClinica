package org.akaza.openclinica.control.submit;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletResponse;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.AuditableEntityBean;
import org.akaza.openclinica.bean.core.DiscrepancyNoteType;
import org.akaza.openclinica.bean.core.ResolutionStatus;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.control.AbstractTableFactory;
import org.akaza.openclinica.control.DefaultActionsEditor;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.hibernate.AuditUserLoginDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.ListNotesFilter;
import org.akaza.openclinica.dao.managestudy.ListNotesSort;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.jmesa.core.filter.DateFilterMatcher;
import org.jmesa.core.filter.FilterMatcher;
import org.jmesa.core.filter.MatcherKey;
import org.jmesa.core.filter.StringFilterMatcher;
import org.jmesa.facade.TableFacade;
import org.jmesa.limit.Filter;
import org.jmesa.limit.FilterSet;
import org.jmesa.limit.Limit;
import org.jmesa.limit.Sort;
import org.jmesa.limit.SortSet;
import org.jmesa.view.component.Row;
import org.jmesa.view.editor.CellEditor;
import org.jmesa.view.editor.DateCellEditor;
import org.jmesa.view.html.HtmlBuilder;
import org.jmesa.view.html.editor.DroplistFilterEditor;

public class ListNotesTableFactory extends AbstractTableFactory {

    private AuditUserLoginDao auditUserLoginDao;
    private StudySubjectDAO studySubjectDao;
    private UserAccountDAO userAccountDao;
    private DiscrepancyNoteDAO discrepancyNoteDao;
    private StudyDAO studyDao;
    private SubjectDAO subjectDao;
    private CRFVersionDAO crfVersionDao;
    private CRFDAO crfDao;
    private StudyEventDAO studyEventDao;
    private StudyEventDefinitionDAO studyEventDefinitionDao;
    private EventDefinitionCRFDAO eventDefinitionCRFDao;
    private ItemDataDAO itemDataDao;
    private ItemDAO itemDao;
    private EventCRFDAO eventCRFDao;
    private StudyBean currentStudy;
    private ResourceBundle resword;
    private ResourceBundle resformat;
    private ArrayList<DiscrepancyNoteBean> allNotes = new ArrayList<DiscrepancyNoteBean>();
    private ArrayList<StudyEventDefinitionBean> studyEventDefinitions;
    private String module;
    private Integer resolutionStatus;
    private Integer discNoteType;
    private Boolean studyHasDiscNotes = new Boolean(false);
    private final boolean showMoreLink;
    public static ArrayList notesForPrintPop;

    public ListNotesTableFactory(boolean showMoreLink){
        this.showMoreLink = showMoreLink;
    }

    @Override
    protected String getTableName() {
        return "listNotes";
    }

    @Override
    protected void configureColumns(TableFacade tableFacade, Locale locale) {

        tableFacade.setColumnProperties("studySubject.label", "discrepancyNoteBean.disType",
                "discrepancyNoteBean.resolutionStatus", "siteId", "discrepancyNoteBean.createdDate",
                "discrepancyNoteBean.updatedDate", "age", "days", "eventName",
                "eventStartDate", "crfName", "entityName", "entityValue", "discrepancyNoteBean.entityType",
                "discrepancyNoteBean.description", "discrepancyNoteBean.detailedNotes",
                "numberOfNotes", "discrepancyNoteBean.user",
                 "discrepancyNoteBean.owner", "actions");
        Row row = tableFacade.getTable().getRow();
        configureColumn(row.getColumn("studySubject.label"), resword.getString("study_subject_ID"), null, null, true, true);
        configureColumn(row.getColumn("discrepancyNoteBean.createdDate"), resword.getString("date_created"), new DateCellEditor(getDateFormat()), null, true,
                true);
        configureColumn(row.getColumn("discrepancyNoteBean.updatedDate"), resword.getString("date_updated"), new DateCellEditor(getDateFormat()), null, true,
                true);
        configureColumn(row.getColumn("eventStartDate"), resword.getString("event_date"), new DateCellEditor(getDateFormat()), null, false, false);
        configureColumn(row.getColumn("eventName"), resword.getString("event_name"), null, null, true, true);
        configureColumn(row.getColumn("crfName"), resword.getString("CRF"), null, null, true, true);
        configureColumn(row.getColumn("entityName"), resword.getString("entity_name"), null, null, true, true);
        configureColumn(row.getColumn("entityValue"), resword.getString("entity_value"), null, null, true, true);
        configureColumn(row.getColumn("discrepancyNoteBean.description"), resword.getString("description"), null, null, true, false);
        configureColumn(row.getColumn("discrepancyNoteBean.detailedNotes"), resword.getString("detailed_notes"), null, null, false, false);
        configureColumn(row.getColumn("numberOfNotes"), resword.getString("of_notes"), null, null, false, false);
        configureColumn(row.getColumn("discrepancyNoteBean.user"), resword.getString("assigned_user"), new AssignedUserCellEditor(), null, true, false);
        configureColumn(row.getColumn("discrepancyNoteBean.resolutionStatus"), resword.getString("resolution_status"), new ResolutionStatusCellEditor(),
                new ResolutionStatusDroplistFilterEditor(), true, false);
        configureColumn(row.getColumn("discrepancyNoteBean.disType"), resword.getString("type"), new DiscrepancyNoteTypeCellEditor(),
                new TypeDroplistFilterEditor(), true, false);
        configureColumn(row.getColumn("discrepancyNoteBean.entityType"), resword.getString("entity_type"), null, null, true, false);
        configureColumn(row.getColumn("discrepancyNoteBean.owner"), resword.getString("owner"), new OwnerCellEditor(), null, false, false);
        String actionsHeader = resword.getString("actions") + "&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;";
        configureColumn(row.getColumn("actions"), actionsHeader, new ActionsCellEditor(), new DefaultActionsEditor(locale), true, false);
        configureColumn(row.getColumn("age"), resword.getString("days_open"), null, null);
        configureColumn(row.getColumn("days"), resword.getString("days_since_updated"), null, null);
    }

    @Override
    public void configureTableFacade(HttpServletResponse response, TableFacade tableFacade) {
        super.configureTableFacade(response, tableFacade);
        tableFacade.addFilterMatcher(new MatcherKey(Date.class, "discrepancyNoteBean.createdDate"), new DateFilterMatcher(getDateFormat()));
        tableFacade.addFilterMatcher(new MatcherKey(Date.class, "discrepancyNoteBean.updatedDate"), new DateFilterMatcher(getDateFormat()));
        tableFacade.addFilterMatcher(new MatcherKey(UserAccountBean.class, "discrepancyNoteBean.user"), new GenericFilterMatecher());
        tableFacade.addFilterMatcher(new MatcherKey(UserAccountBean.class, "studySubject.label"), new GenericFilterMatecher());
        tableFacade.addFilterMatcher(new MatcherKey(String.class, "eventName"), new StringFilterMatcher());
        tableFacade.addFilterMatcher(new MatcherKey(String.class, "crfName"), new StringFilterMatcher());
        tableFacade.addFilterMatcher(new MatcherKey(String.class, "entityName"), new StringFilterMatcher());
        tableFacade.addFilterMatcher(new MatcherKey(String.class, "entityValue"), new StringFilterMatcher());
        tableFacade.addFilterMatcher(new MatcherKey(String.class, "age"), new AgeDaysFilterMatcher());
        tableFacade.addFilterMatcher(new MatcherKey(String.class, "days"), new AgeDaysFilterMatcher());
        tableFacade.addFilterMatcher(new MatcherKey(String.class, "discrepancyNoteBean.disType"), new DNTypeFilterMatcher());
        tableFacade.addFilterMatcher(new MatcherKey(String.class, "discrepancyNoteBean.resolutionStatus"), new DNResolutionStatusFilterMatcher());
    }

    @Override
    public void configureTableFacadePostColumnConfiguration(TableFacade tableFacade) {
        ListNotesTableToolbar toolbar = new ListNotesTableToolbar(showMoreLink);
        tableFacade.setToolbar(toolbar);
        toolbar.setStudyHasDiscNotes(studyHasDiscNotes);
        toolbar.setDiscNoteType(discNoteType);
        toolbar.setResolutionStatus(resolutionStatus);
        toolbar.setModule(module);
        toolbar.setResword(resword);
        tableFacade.setToolbar(toolbar);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setDataAndLimitVariables(TableFacade tableFacade) {
        // initialize i18n
        resword = ResourceBundleProvider.getWordsBundle(getLocale());
        resformat = ResourceBundleProvider.getFormatBundle(getLocale());

        Limit limit = tableFacade.getLimit();
        ListNotesFilter listNotesFilter = getListNoteFilter(limit);
        if (!limit.isComplete()) {
            int totalRows = getDiscrepancyNoteDao().getViewNotesCountWithFilter(listNotesFilter, getCurrentStudy());
            tableFacade.setTotalRows(totalRows);
        }

        ListNotesSort listNotesSort = getListSubjectSort(limit);
        int rowStart = limit.getRowSelect().getRowStart();
        int rowEnd = limit.getRowSelect().getRowEnd();

        ArrayList<DiscrepancyNoteBean> items =
            getDiscrepancyNoteDao().getViewNotesWithFilterAndSort(getCurrentStudy(), listNotesFilter, listNotesSort, rowStart, rowEnd);
        Collection<HashMap<Object, Object>> theItems = new ArrayList<HashMap<Object, Object>>();
        this.setAllNotes(populateRowsWithAttachedData(items));

        //Keeping all notes without pagination to be shown in print popup.
        notesForPrintPop = getDiscrepancyNoteDao().getViewNotesWithFilterAndSort(getCurrentStudy(), listNotesFilter, listNotesSort);

        // for (DiscrepancyNoteBean discrepancyNoteBean : items) {
        for (DiscrepancyNoteBean discrepancyNoteBean : allNotes) {
            UserAccountBean owner = (UserAccountBean) getUserAccountDao().findByPK(discrepancyNoteBean.getOwnerId());
//            UserAccountBean assignedUser =
//                discrepancyNoteBean.getUpdaterId() == 0 ? null : (UserAccountBean) getUserAccountDao().findByPK(discrepancyNoteBean.getAssignedUserId());

            HashMap<Object, Object> h = new HashMap<Object, Object>();

            h.put("studySubject", discrepancyNoteBean.getStudySub());
            h.put("studySubject.label", discrepancyNoteBean.getStudySub().getLabel());
            h.put("discrepancyNoteBean.disType", discrepancyNoteBean.getDisType());
            h.put("discrepancyNoteBean.resolutionStatus", discrepancyNoteBean.getResStatus());
            h.put("age", discrepancyNoteBean.getResolutionStatusId()==5?null:discrepancyNoteBean.getAge());
            h.put("days", (discrepancyNoteBean.getResolutionStatusId()==4 || discrepancyNoteBean.getResolutionStatusId()==5)?null:discrepancyNoteBean.getDays());
            h.put("siteId", ((StudyBean) getStudyDao().findByPK(discrepancyNoteBean.getStudySub().getStudyId())).getIdentifier());
            h.put("discrepancyNoteBean", discrepancyNoteBean);
            h.put("discrepancyNoteBean.createdDate", discrepancyNoteBean.getCreatedDate());
            h.put("discrepancyNoteBean.updatedDate", discrepancyNoteBean.getUpdatedDate());
            h.put("eventName", discrepancyNoteBean.getEventName());
            h.put("eventStartDate", discrepancyNoteBean.getEventStart());
            h.put("crfName", discrepancyNoteBean.getCrfName());
            h.put("entityName", discrepancyNoteBean.getEntityName());
            h.put("entityValue", discrepancyNoteBean.getEntityValue());
            h.put("discrepancyNoteBean", discrepancyNoteBean);
            h.put("discrepancyNoteBean.description", discrepancyNoteBean.getDescription());
            h.put("discrepancyNoteBean.detailedNotes", discrepancyNoteBean.getDetailedNotes());
            h.put("numberOfNotes", discrepancyNoteBean.getNumChildren());
            h.put("discrepancyNoteBean.user", discrepancyNoteBean.getAssignedUser());
            h.put("discrepancyNoteBean.entityType", discrepancyNoteBean.getEntityType());
            h.put("discrepancyNoteBean.owner", owner);

            theItems.add(h);
            setStudyHasDiscNotes(true);
        }
        tableFacade.setItems(theItems);

    }

    private ArrayList<DiscrepancyNoteBean> populateRowsWithAttachedData(ArrayList noteRows) {
        DiscrepancyNoteDAO dndao = getDiscrepancyNoteDao();
        ArrayList<DiscrepancyNoteBean> allNotes = new ArrayList<DiscrepancyNoteBean>();
        // Set<String> hiddenCrfIds = new TreeSet<String>();
        // if (currentStudy.isSite(currentStudy.getParentStudyId())) {
        // hiddenCrfIds =
        // this.getEventDefinitionCRFDao().findHiddenCrfIdsBySite(currentStudy);
        // }

        for (int i = 0; i < noteRows.size(); i++) {
            // DiscrepancyNoteRow dnr = (DiscrepancyNoteRow) noteRows.get(i);
            DiscrepancyNoteBean dnb = (DiscrepancyNoteBean) noteRows.get(i);
            dnb.setAssignedUser((UserAccountBean) getUserAccountDao().findByPK(dnb.getAssignedUserId()));
            if (dnb.getParentDnId() == 0) {
                // System.out.println("running query on study id " + currentStudy.getId() + " and dnb " + dnb.getId());
                ArrayList children = dndao.findAllByStudyAndParent(currentStudy, dnb.getId());
                // dnr.setNumChildren(children.size());
                dnb.setNumChildren(children.size());

                for (int j = 0; j < children.size(); j++) {
                    DiscrepancyNoteBean child = (DiscrepancyNoteBean) children.get(j);

                    /*
                     * if (child.getResolutionStatusId() > dnb.getResolutionStatusId()) { // dnr.setStatus(ResolutionStatus.get(child. //
                     * getResolutionStatusId())); dnb.setResStatus(ResolutionStatus.get(child.getResolutionStatusId())); }
                     */
                    /*
                     * The update date is the date created of the latest child note
                     */
                    // dnb.setUpdatedDate(((DiscrepancyNoteBean) children.get(0)).getCreatedDate());
                    dnb.setUpdatedDate(child.getCreatedDate());
                    // << one line change tbh, 02/2010
                    // the previous line didnt work since we always create two notes on creation.
                }
            }

            String entityType = dnb.getEntityType();

            if (dnb.getEntityId() > 0 && !entityType.equals("")) {
                AuditableEntityBean aeb = dndao.findEntity(dnb);
                dnb.setEntityName(aeb.getName());
                if (entityType.equalsIgnoreCase("subject")) {
                    allNotes.add(dnb);
                    SubjectBean sb = (SubjectBean) aeb;
                    StudySubjectBean ssb = studySubjectDao.findBySubjectIdAndStudy(sb.getId(), currentStudy);
                    dnb.setStudySub(ssb);
                    String column = dnb.getColumn().trim();
                    if (!StringUtil.isBlank(column)) {
                        if ("gender".equalsIgnoreCase(column)) {
                            dnb.setEntityValue(sb.getGender() + "");
                            dnb.setEntityName(resword.getString("gender"));
                        } else if ("date_of_birth".equals(column)) {
                            if (sb.getDateOfBirth() != null) {
                                dnb.setEntityValue(sb.getDateOfBirth().toString());

                            }
                            dnb.setEntityName(resword.getString("date_of_birth"));
                        } else if ("unique_identifier".equalsIgnoreCase(column)) {
                            dnb.setEntityName(resword.getString("unique_identifier"));
                            dnb.setEntityValue(sb.getUniqueIdentifier());
                        }
                    }
                } else if (entityType.equalsIgnoreCase("studySub")) {
                    allNotes.add(dnb);
                    StudySubjectBean ssb = (StudySubjectBean) aeb;
                    dnb.setStudySub(ssb);
                    String column = dnb.getColumn().trim();
                    if (!StringUtil.isBlank(column)) {
                        if ("enrollment_date".equals(column)) {
                            if (ssb.getEnrollmentDate() != null) {
                                dnb.setEntityValue(ssb.getEnrollmentDate().toString());

                            }
                            dnb.setEntityName(resword.getString("enrollment_date"));

                        }
                    }
                } else if (entityType.equalsIgnoreCase("eventCRF")) {
                    StudyEventDAO sed = getStudyEventDao();
                    StudyEventBean se = (StudyEventBean) sed.findByPK(dnb.getEntityId());

                    EventCRFBean ecb = (EventCRFBean) aeb;
                    CRFVersionDAO cvdao = getCrfVersionDao();
                    CRFDAO cdao = getCrfDao();
                    CRFVersionBean cvb = (CRFVersionBean) cvdao.findByPK(ecb.getCRFVersionId());
                    CRFBean cb = (CRFBean) cdao.findByPK(cvb.getCrfId());

                    // if (currentStudy.getParentStudyId() > 0 &&
                    // hiddenCrfIds.contains(cb.getId())) {
                    // } else {
                    dnb.setStageId(ecb.getStage().getId());
                    dnb.setEntityName(cb.getName() + " (" + cvb.getName() + ")");

                    StudySubjectBean ssub = (StudySubjectBean) getStudySubjectDao().findByPK(ecb.getStudySubjectId());
                    dnb.setStudySub(ssub);
                    if (se != null) {
                        dnb.setEventStart(se.getDateStarted());
                        dnb.setEventName(se.getName());
                    }
                    dnb.setCrfName(cb.getName());

                    String column = dnb.getColumn().trim();
                    if (!StringUtil.isBlank(column)) {
                        if ("date_interviewed".equals(column)) {
                            if (ecb.getDateInterviewed() != null) {
                                dnb.setEntityValue(ecb.getDateInterviewed().toString());

                            }
                            dnb.setEntityName(resword.getString("date_interviewed"));
                        } else if ("interviewer_name".equals(column)) {
                            dnb.setEntityValue(ecb.getInterviewerName());
                            dnb.setEntityName(resword.getString("interviewer_name"));
                        }
                    }
                    // }
                } else if (entityType.equalsIgnoreCase("studyEvent")) {
                    allNotes.add(dnb);
                    StudyEventDAO sed = getStudyEventDao();
                    StudyEventBean se = (StudyEventBean) sed.findByPK(dnb.getEntityId());
                    // EventCRFBean ecb = eventCRFDao.findBy;
                    // CRFVersionDAO cvdao = getCrfVersionDao();
                    // CRFDAO cdao = getCrfDao();
                    // CRFVersionBean cvb = (CRFVersionBean)
                    // cvdao.findByPK(ecb.getCRFVersionId());
                    // CRFBean cb = (CRFBean) cdao.findByPK(cvb.getCrfId());
                    StudyEventDefinitionDAO seddao = getStudyEventDefinitionDao();
                    StudyEventDefinitionBean sedb = (StudyEventDefinitionBean) seddao.findByPK(se.getStudyEventDefinitionId());
                    se.setName(sedb.getName());
                    dnb.setEntityName(sedb.getName());
                    StudySubjectBean ssub = (StudySubjectBean) getStudySubjectDao().findByPK(se.getStudySubjectId());
                    dnb.setStudySub(ssub);
                    dnb.setEventStart(se.getDateStarted());
                    dnb.setEventName(se.getName());
                    // dnb.setCrfName(cb.getName());
                    String column = dnb.getColumn().trim();
                    if (!StringUtil.isBlank(column)) {
                        if ("date_start".equals(column)) {
                            if (se.getDateStarted() != null) {
                                dnb.setEntityValue(se.getDateStarted().toString());
                            }
                            dnb.setEntityName(resword.getString("start_date"));
                        } else if ("date_end".equals(column)) {
                            if (se.getDateEnded() != null) {
                                dnb.setEntityValue(se.getDateEnded().toString());
                            }
                            dnb.setEntityName(resword.getString("end_date"));
                        } else if ("location".equals(column)) {
                            dnb.setEntityValue(se.getLocation());
                            dnb.setEntityName(resword.getString("location"));
                        }
                    }
                } else if (entityType.equalsIgnoreCase("itemData")) {
                    ItemDataDAO iddao = getItemDataDao();
                    ItemDAO idao = getItemDao();

                    ItemDataBean idb = (ItemDataBean) iddao.findByPK(dnb.getEntityId());
                    ItemBean ib = (ItemBean) idao.findByPK(idb.getItemId());

                    EventCRFDAO ecdao = getEventCRFDao();
                    EventCRFBean ec = (EventCRFBean) ecdao.findByPK(idb.getEventCRFId());

                    CRFVersionDAO cvdao = getCrfVersionDao();
                    CRFDAO cdao = getCrfDao();
                    CRFVersionBean cvb = (CRFVersionBean) cvdao.findByPK(ec.getCRFVersionId());
                    CRFBean cb = (CRFBean) cdao.findByPK(cvb.getCrfId());

                    // if (currentStudy.getParentStudyId() > 0 &&
                    // hiddenCrfIds.contains(cb.getId())) {
                    // } else {
                    allNotes.add(dnb);
                    dnb.setStageId(ec.getStage().getId());
                    dnb.setEntityName(ib.getName());
                    dnb.setEntityValue(idb.getValue());
                    dnb.setItemId(ib.getId());

                    StudyEventDAO sed = getStudyEventDao();
                    StudyEventBean se = (StudyEventBean) sed.findByPK(ec.getStudyEventId());

                    StudyEventDefinitionDAO seddao = getStudyEventDefinitionDao();
                    StudyEventDefinitionBean sedb = (StudyEventDefinitionBean) seddao.findByPK(se.getStudyEventDefinitionId());

                    se.setName(sedb.getName());

                    StudySubjectDAO ssdao = getStudySubjectDao();
                    StudySubjectBean ssub = (StudySubjectBean) ssdao.findByPK(ec.getStudySubjectId());
                    dnb.setStudySub(ssub);
                    dnb.setEventStart(se.getDateStarted());
                    dnb.setEventName(se.getName());
                    dnb.setCrfName(cb.getName());
                    // }
                }
                //Because all places set DiscrepancyNoteBean subjectId  as its studySub's Id.
                dnb.setSubjectId(dnb.getStudySub().getId());
            }
            dnb.setSiteId(((StudyBean) getStudyDao().findByPK(dnb.getStudySub().getStudyId())).getIdentifier());
        }
        return allNotes;
    }

    /**
     * A very custom way to filter the items. The AuditUserLoginFilter acts as a command for the Hibernate criteria object. Take the Limit information and
     * filter the rows.
     *
     * @param limit
     *            The Limit to use.
     */
    public ListNotesFilter getListNoteFilter(Limit limit) {
        ListNotesFilter listNotesFilter = new ListNotesFilter();
        FilterSet filterSet = limit.getFilterSet();
        Collection<Filter> filters = filterSet.getFilters();
        for (Filter filter : filters) {
            String property = filter.getProperty();
            String value = filter.getValue();
            //Checking if the given date format is valid
            if("discrepancyNoteBean.createdDate".equalsIgnoreCase(property)
                    || "discrepancyNoteBean.updatedDate".equalsIgnoreCase(property)){
                 try{
                    String date = formatDate(new Date(value));
                     value = date;
                   }catch(Exception ex){
                     value = "01-Jan-1700";
                   }
            }
            //
            listNotesFilter.addFilter(property, value);
        }

        return listNotesFilter;
    }

    /**
     * A very custom way to sort the items. The AuditUserLoginSort acts as a command for the Hibernate criteria object. Take the Limit information and sort the
     * rows.
     *
     * @param limit
     *            The Limit to use.
     */
    protected ListNotesSort getListSubjectSort(Limit limit) {
        ListNotesSort listNotesSort = new ListNotesSort();
        SortSet sortSet = limit.getSortSet();
        Collection<Sort> sorts = sortSet.getSorts();
        for (Sort sort : sorts) {
            String property = sort.getProperty();
            String order = sort.getOrder().toParam();
            listNotesSort.addSort(property, order);
        }

        return listNotesSort;
    }

    public AuditUserLoginDao getAuditUserLoginDao() {
        return auditUserLoginDao;
    }

    public void setAuditUserLoginDao(AuditUserLoginDao auditUserLoginDao) {
        this.auditUserLoginDao = auditUserLoginDao;
    }

    private class ResolutionStatusDroplistFilterEditor extends DroplistFilterEditor {
        @Override
        protected List<Option> getOptions() {
            List<Option> options = new ArrayList<Option>();
            for (Object status : ResolutionStatus.toArrayList()) {
                options.add(new Option(String.valueOf(((ResolutionStatus) status).getId()), ((ResolutionStatus) status).getName()));
            }
            options.add(new Option("21", "New and Updated"));
            return options;
        }
    }

    private class TypeDroplistFilterEditor extends DroplistFilterEditor {
        @Override
        protected List<Option> getOptions() {
            List<Option> options = new ArrayList<Option>();
            for (Object type : DiscrepancyNoteType.toArrayList()) {
                options.add(new Option(String.valueOf(((DiscrepancyNoteType) type).getId()), ((DiscrepancyNoteType) type).getName()));
            }
            options.add(new Option("31", "Query and Failed Validation Check"));
            return options;
        }
    }

    private class AssignedUserDroplistFilterEditor extends DroplistFilterEditor {
        @Override
        protected List<Option> getOptions() {
            List<Option> options = new ArrayList<Option>();
            Collection currentStudyUsers = userAccountDao.findAll();
            for (Object type : currentStudyUsers) {
                options.add(new Option(String.valueOf(((UserAccountBean) type).getId()), ((UserAccountBean) type).getName()));
            }
            return options;
        }
    }

    private class GenericFilterMatecher implements FilterMatcher {
        public boolean evaluate(Object itemValue, String filterValue) {
            return true;
        }
    }
    
    private class DNTypeFilterMatcher implements FilterMatcher {
        public boolean evaluate(Object itemValue, String filterValue) {
            int itemDNTypeId = ((DiscrepancyNoteType)itemValue).getId();
            int filterDNTypeId = Integer.valueOf(filterValue).intValue();
            if(filterDNTypeId==31) {
                return itemDNTypeId==1 || itemDNTypeId==3;
            } else {
                return itemDNTypeId == filterDNTypeId;
            }
        }
    }

    private class DNResolutionStatusFilterMatcher implements FilterMatcher {
        public boolean evaluate(Object itemValue, String filterValue) {
            int itemDNTypeId = ((ResolutionStatus)itemValue).getId();
            int filterDNTypeId = Integer.valueOf(filterValue).intValue();
            if(filterDNTypeId==21) {
                return itemDNTypeId==1 || itemDNTypeId==2;
            } else {
                return itemDNTypeId==filterDNTypeId;
            }
        }
    }
    private class ResolutionStatusCellEditor implements CellEditor {
        @SuppressWarnings("unchecked")
        public Object getValue(Object item, String property, int rowcount) {
            String value = "";
            ResolutionStatus status = (ResolutionStatus) ((HashMap<Object, Object>) item).get("discrepancyNoteBean.resolutionStatus");

            if (status != null) {
                value = status.getName();
            }
            return value;
        }
    }

    private class DiscrepancyNoteTypeCellEditor implements CellEditor {
        @SuppressWarnings("unchecked")
        public Object getValue(Object item, String property, int rowcount) {
            String value = "";
            DiscrepancyNoteType type = (DiscrepancyNoteType) ((HashMap<Object, Object>) item).get("discrepancyNoteBean.disType");

            if (type != null) {
                value = type.getName();
            }
            return value;
        }
    }

    private class StatusCellEditor implements CellEditor {
        @SuppressWarnings("unchecked")
        public Object getValue(Object item, String property, int rowcount) {
            String value = "";
            Status status = (Status) ((HashMap<Object, Object>) item).get("subject.status");

            if (status != null) {
                value = status.getName();
            }
            return value;
        }
    }

    private class OwnerCellEditor implements CellEditor {
        @SuppressWarnings("unchecked")
        public Object getValue(Object item, String property, int rowcount) {
            String value = "";
            UserAccountBean user = (UserAccountBean) ((HashMap<Object, Object>) item).get("discrepancyNoteBean.owner");

            if (user != null) {
                value = user.getName();
            }
            return value;
        }
    }

//    private class AgeCellEditor implements CellEditor {
//        @SuppressWarnings("unchecked")
//        public Object getValue(Object item, String property, int rowcount) {
//            String value = "";
//            int age = (Integer) ((HashMap<Object, Object>) item).get("age");
//
//            if (age != 0) {
//                value = age + "";
//            }
//            return value;
//        }
//    }
//    private class DaysCellEditor implements CellEditor {
//        @SuppressWarnings("unchecked")
//        public Object getValue(Object item, String property, int rowcount) {
//            String value = "";
//            int days = (Integer) ((HashMap<Object, Object>) item).get("days");
//
//            if (days != 0) {
//                value = days + "";
//            }
//            return value;
//        }
//    }



    private class AssignedUserCellEditor implements CellEditor {
        @SuppressWarnings("unchecked")
        public Object getValue(Object item, String property, int rowcount) {
            String value = "";
            UserAccountBean user = (UserAccountBean) ((HashMap<Object, Object>) item).get("discrepancyNoteBean.user");

            if (user != null) {
                value = user.getFirstName() + " " + user.getLastName() + " (" + user.getName() + ")";
            }
            return value;
        }
    }

    private class UpdaterCellEditor implements CellEditor {
        @SuppressWarnings("unchecked")
        public Object getValue(Object item, String property, int rowcount) {
            String value = "";
            UserAccountBean user = (UserAccountBean) ((HashMap<Object, Object>) item).get("subject.updater");

            if (user != null) {
                value = user.getName();
            }
            return value;
        }
    }

    private class ActionsCellEditor implements CellEditor {
        @SuppressWarnings("unchecked")
        public Object getValue(Object item, String property, int rowcount) {
            String value = "";
            DiscrepancyNoteBean dnb = (DiscrepancyNoteBean) ((HashMap<Object, Object>) item).get("discrepancyNoteBean");
            HtmlBuilder builder = new HtmlBuilder();
            //for "view" as action
            //This createNoteURL uses the same method as in ResolveDiscrepancyServlet
            String createNoteURL = CreateDiscrepancyNoteServlet.getAddChildURL(dnb, ResolutionStatus.CLOSED, true);
            builder.a().href("javascript:openDNWindow('" + createNoteURL + "&viewAction=1" + "');");
            builder.close();
            builder.img().name("bt_View1").src("images/bt_View_d.gif").border("0").alt(resword.getString("view")).title(resword.getString("view"))
                    .align("left").append("hspace=\"6\"").close();
            builder.aEnd();
            if (!getCurrentStudy().getStatus().isLocked()) {
                if (!dnb.getResStatus().isNotApplicable()) {
                    if (dnb.getEntityType() != "eventCrf") {
                        builder.a().href("ResolveDiscrepancy?noteId=" + dnb.getId());
                        builder.close();
                        builder.img().name("bt_Reassign1").src("images/bt_Reassign_d.gif").border("0").alt(resword.getString("view_within_crf"))
                                .title(resword.getString("view_within_crf")).align("left").append("hspace=\"6\"").close();
                        builder.aEnd();
                    } else {
                        if (dnb.getStageId() == 5) {
                            builder.a().href("ResolveDiscrepancy?noteId=" + dnb.getId());
                            builder.close();
                            builder.img().name("bt_Reassign1").src("images/bt_Reassign_d.gif").border("0").alt(resword.getString("view_within_crf"))
                                    .title(resword.getString("view_within_crf")).align("left").append("hspace=\"6\"").close();
                            builder.aEnd();
                        }
                    }
                }
            }

            StudySubjectBean studySubjectBean = (StudySubjectBean) ((HashMap<Object, Object>) item).get("studySubject");
            Integer studySubjectId = studySubjectBean.getId();
            if (studySubjectId != null) {
                StringBuilder url = new StringBuilder();
                url.append(downloadNotesLinkBuilder(studySubjectBean));
                value = url.toString();
            }


            return builder.toString();
        }
    }

    private String downloadNotesLinkBuilder(StudySubjectBean studySubject) {
        HtmlBuilder actionLink = new HtmlBuilder();
        if (this.isStudyHasDiscNotes()) {
            if (this.getResolutionStatus() >= 1 && this.getResolutionStatus() <= 5) {
                actionLink.a().href(
                        "javascript:openDocWindow('ChooseDownloadFormat?subjectId=" + studySubject.getId() + "&discNoteType=" + discNoteType
                            + "&resolutionStatus=" + resolutionStatus + "')");
                actionLink.img().name("bt_View1").src("images/bt_Download.gif").border("0").alt(resword.getString("download_discrepancy_notes")).title(
                        resword.getString("download_discrepancy_notes")).append("hspace=\"4\" width=\"24 \" height=\"15\"").end().aEnd();
                actionLink.append("&nbsp;&nbsp;&nbsp;");
            } else {
                actionLink.a().href(
                        "javascript:openDocWindow('ChooseDownloadFormat?subjectId=" + studySubject.getId() + "&discNoteType=" + discNoteType + "&module="
                            + module + "')");
                actionLink.img().name("bt_View1").src("images/bt_Download.gif").border("0").alt(resword.getString("download_discrepancy_notes")).title(
                        resword.getString("download_discrepancy_notes")).append("hspace=\"2\" width=\"24 \" height=\"15\"").end().aEnd();
                actionLink.append("&nbsp;&nbsp;&nbsp;");
            }
        }
        return actionLink.toString();
    }

    private String updateSubjectLink(Integer subjectId) {
        HtmlBuilder builder = new HtmlBuilder();
        builder.a().href("UpdateSubject?action=show&id=" + subjectId);
        builder.onmouseout("javascript:setImage('bt_Edit1','images/bt_Edit_d.gif');");
        builder.onmouseover("javascript:setImage('bt_Edit1','images/bt_Edit.gif');");
        builder.close();
        builder.img().name("bt_Edit1").src("images/bt_Edit.gif").border("0").alt(resword.getString("edit")).title(resword.getString("edit")).align("left")
                .append("hspace=\"6\"").close();
        builder.aEnd();
        return builder.toString();
    }

    private String removeSubjectLink(Integer subjectId) {
        HtmlBuilder builder = new HtmlBuilder();
        builder.a().href("RemoveSubject?action=confirm&id=" + subjectId);
        builder.onmouseout("javascript:setImage('bt_Remove1','images/bt_Remove_d.gif');");
        builder.onmouseover("javascript:setImage('bt_Remove1','images/bt_Remove.gif');");
        builder.close();
        builder.img().name("bt_Remove1").src("images/bt_Remove.gif").border("0").alt(resword.getString("remove")).title(resword.getString("remove"))
                .align("left").append("hspace=\"6\"").close();
        builder.aEnd();
        return builder.toString();
    }

    private String viewSubjectLink(Integer subjectId) {
        HtmlBuilder builder = new HtmlBuilder();
        builder.a().href("UpdateSubject?action=show&id=" + subjectId);
        builder.onmouseout("javascript:setImage('bt_View1','images/bt_View_d.gif');");
        builder.onmouseover("javascript:setImage('bt_View1','images/bt_View.gif');");
        builder.close();
        builder.img().name("bt_View1").src("images/bt_View.gif").border("0").alt(resword.getString("view")).title(resword.getString("view")).align("left")
                .append("hspace=\"6\"").close();
        builder.aEnd();
        return builder.toString();
    }

    private String restoreSubjectLink(Integer subjectId) {
        HtmlBuilder builder = new HtmlBuilder();
        builder.a().href("RestoreSubject?action=confirm&id=" + subjectId);
        builder.onmouseout("javascript:setImage('bt_Restor3','images/bt_Restore_d.gif');");
        builder.onmouseover("javascript:setImage('bt_Restore3','images/bt_Restore.gif');");
        builder.close();
        builder.img().name("bt_Restore3").src("images/bt_Restore.gif").border("0").alt(resword.getString("restore")).title(resword.getString("restore"))
                .align("left").append("hspace=\"6\"").close();
        builder.aEnd();
        return builder.toString();
    }
    // Ignore the mathing values with filter 
    public class AgeDaysFilterMatcher implements FilterMatcher {
    public boolean evaluate(Object itemValue, String filterValue) {
        return true;
    }
}

    private String formatDate(Date date) {
        String format = resformat.getString("date_format_string");
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    private String getDateFormat() {
        return resformat.getString("date_format_string");
    }

    public StudySubjectDAO getStudySubjectDao() {
        return studySubjectDao;
    }

    public void setStudySubjectDao(StudySubjectDAO studySubjectDao) {
        this.studySubjectDao = studySubjectDao;
    }

    public SubjectDAO getSubjectDao() {
        return subjectDao;
    }

    public void setSubjectDao(SubjectDAO subjectDao) {
        this.subjectDao = subjectDao;
    }

    public StudyDAO getStudyDao() {
        return studyDao;
    }

    public void setStudyDao(StudyDAO studyDao) {
        this.studyDao = studyDao;
    }

    public StudyBean getCurrentStudy() {
        return currentStudy;
    }

    public void setCurrentStudy(StudyBean currentStudy) {
        this.currentStudy = currentStudy;
    }

    public UserAccountDAO getUserAccountDao() {
        return userAccountDao;
    }

    public void setUserAccountDao(UserAccountDAO userAccountDao) {
        this.userAccountDao = userAccountDao;
    }

    public DiscrepancyNoteDAO getDiscrepancyNoteDao() {
        return discrepancyNoteDao;
    }

    public void setDiscrepancyNoteDao(DiscrepancyNoteDAO discrepancyNoteDao) {
        this.discrepancyNoteDao = discrepancyNoteDao;
    }

    public CRFVersionDAO getCrfVersionDao() {
        return crfVersionDao;
    }

    public void setCrfVersionDao(CRFVersionDAO crfVersionDao) {
        this.crfVersionDao = crfVersionDao;
    }

    public CRFDAO getCrfDao() {
        return crfDao;
    }

    public void setCrfDao(CRFDAO crfDao) {
        this.crfDao = crfDao;
    }

    public StudyEventDAO getStudyEventDao() {
        return studyEventDao;
    }

    public void setStudyEventDao(StudyEventDAO studyEventDao) {
        this.studyEventDao = studyEventDao;
    }

    public EventDefinitionCRFDAO getEventDefinitionCRFDao() {
        return eventDefinitionCRFDao;
    }

    public void setEventDefinitionCRFDao(EventDefinitionCRFDAO eventDefinitionCRFDao) {
        this.eventDefinitionCRFDao = eventDefinitionCRFDao;
    }

    public ItemDataDAO getItemDataDao() {
        return itemDataDao;
    }

    public void setItemDataDao(ItemDataDAO itemDataDao) {
        this.itemDataDao = itemDataDao;
    }

    public ItemDAO getItemDao() {
        return itemDao;
    }

    public void setItemDao(ItemDAO itemDao) {
        this.itemDao = itemDao;
    }

    public EventCRFDAO getEventCRFDao() {
        return eventCRFDao;
    }

    public void setEventCRFDao(EventCRFDAO eventCRFDao) {
        this.eventCRFDao = eventCRFDao;
    }

    public StudyEventDefinitionDAO getStudyEventDefinitionDao() {
        return studyEventDefinitionDao;
    }

    public void setStudyEventDefinitionDao(StudyEventDefinitionDAO studyEventDefinitionDao) {
        this.studyEventDefinitionDao = studyEventDefinitionDao;
    }

    public ArrayList<DiscrepancyNoteBean> getAllNotes() {
        return allNotes;
    }

    public void setAllNotes(ArrayList<DiscrepancyNoteBean> allNotes) {
        this.allNotes = allNotes;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
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

    public Integer getResolutionStatus() {
        return resolutionStatus;
    }

    public void setResolutionStatus(Integer resolutionStatus) {
        this.resolutionStatus = resolutionStatus;
    }

    public ArrayList<DiscrepancyNoteBean> populateDataInNote(List notes) {
        return populateRowsWithAttachedData((ArrayList) notes);
    }

    public static ArrayList getNotesForPrintPop() {
        return notesForPrintPop;
    }

    public static void setNotesForPrintPop(ArrayList notesForPrintPop) {
        ListNotesTableFactory.notesForPrintPop = notesForPrintPop;
    }
}
