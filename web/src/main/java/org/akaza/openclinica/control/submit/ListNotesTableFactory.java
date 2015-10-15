package org.akaza.openclinica.control.submit;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.akaza.openclinica.bean.core.DiscrepancyNoteType;
import org.akaza.openclinica.bean.core.ResolutionStatus;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.control.AbstractTableFactory;
import org.akaza.openclinica.control.DefaultActionsEditor;
import org.akaza.openclinica.control.DropdownFilter;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.hibernate.AuditUserLoginDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.FindSubjectsFilter;
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
import org.akaza.openclinica.service.DiscrepancyNotesSummary;
import org.akaza.openclinica.service.managestudy.ViewNotesFilterCriteria;
import org.akaza.openclinica.service.managestudy.ViewNotesService;
import org.akaza.openclinica.service.managestudy.ViewNotesSortCriteria;
import org.jmesa.core.filter.FilterMatcher;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListNotesTableFactory extends AbstractTableFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ListNotesTableFactory.class.getName());

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
    private List<DiscrepancyNoteBean> allNotes = new ArrayList<DiscrepancyNoteBean>();
    private ArrayList<StudyEventDefinitionBean> studyEventDefinitions;
    private String module;
    private Integer resolutionStatus;
    private Integer discNoteType;
    private Boolean studyHasDiscNotes = new Boolean(false);
    private ViewNotesService viewNotesService;
    private final boolean showMoreLink;
    private DiscrepancyNotesSummary notesSummary;
    private final TypeDroplistFilterEditor discrepancyNoteTypeDropdown = new TypeDroplistFilterEditor();
    private final ResolutionStatusDroplistFilterEditor resolutionStatusDropdown = new ResolutionStatusDroplistFilterEditor();

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
 "discrepancyNoteBean.resolutionStatus", "siteId",
                "discrepancyNoteBean.createdDate",
                "discrepancyNoteBean.updatedDate", "age", "days", "eventName",
                "eventStartDate", "crfName","crfStatus", "entityName", "entityValue", "discrepancyNoteBean.entityType",
                "discrepancyNoteBean.description", "discrepancyNoteBean.detailedNotes",
                "numberOfNotes", "discrepancyNoteBean.user",
                 "discrepancyNoteBean.owner", "actions");
        Row row = tableFacade.getTable().getRow();
        configureColumn(row.getColumn("studySubject.label"), resword.getString("study_subject_ID"), null, null, true, true);
        configureColumn(row.getColumn("siteId"), resword.getString("site_id"), null, null, true, false);
        configureColumn(row.getColumn("discrepancyNoteBean.createdDate"), resword.getString("date_created"), new DateCellEditor(getDateFormat()), null, true,
                true);
        configureColumn(row.getColumn("discrepancyNoteBean.updatedDate"), resword.getString("date_updated"), new DateCellEditor(getDateFormat()), null, true,
                false);
        configureColumn(row.getColumn("eventStartDate"), resword.getString("event_date"), new DateCellEditor(getDateFormat()), null, false, false);
        configureColumn(row.getColumn("eventName"), resword.getString("event_name"), null, null, true, false);
        configureColumn(row.getColumn("crfName"), resword.getString("CRF"), null, null, true, false);
        configureColumn(row.getColumn("crfStatus"), resword.getString("CRF_status"), null, null, false, false);
        configureColumn(row.getColumn("entityName"), resword.getString("entity_name"), new EntityNameCellEditor(), null, true, false);
        configureColumn(row.getColumn("entityValue"), resword.getString("entity_value"), null, null, true, false);
        configureColumn(row.getColumn("discrepancyNoteBean.description"), resword.getString("description"), null, null, true, false);
        configureColumn(row.getColumn("discrepancyNoteBean.detailedNotes"), resword.getString("detailed_notes"), null, null, false, false);
        configureColumn(row.getColumn("numberOfNotes"), resword.getString("of_notes"), null, null, false, false);
        configureColumn(row.getColumn("discrepancyNoteBean.user"), resword.getString("assigned_user"), new AssignedUserCellEditor(), null, true, false);
        configureColumn(row.getColumn("discrepancyNoteBean.resolutionStatus"), resword.getString("resolution_status"), new ResolutionStatusCellEditor(),
                resolutionStatusDropdown, true, false);
        configureColumn(row.getColumn("discrepancyNoteBean.disType"), resword.getString("type"), new DiscrepancyNoteTypeCellEditor(),
                discrepancyNoteTypeDropdown, true, false);
        configureColumn(row.getColumn("discrepancyNoteBean.entityType"), resword.getString("entity_type"), null, null, true, false);
        configureColumn(row.getColumn("discrepancyNoteBean.owner"), resword.getString("owner"), new OwnerCellEditor(), null, false, false);
        String actionsHeader = resword.getString("actions") + "&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;";
        configureColumn(row.getColumn("actions"), actionsHeader, new ActionsCellEditor(), new DefaultActionsEditor(locale), true, false);
        configureColumn(row.getColumn("age"), resword.getString("days_open"), null, null);
        configureColumn(row.getColumn("days"), resword.getString("days_since_updated"), null, null);
    }

    @Override
    public void configureTableFacadePostColumnConfiguration(TableFacade tableFacade) {
        ListNotesTableToolbar toolbar = new ListNotesTableToolbar(showMoreLink);
        toolbar.setStudyHasDiscNotes(studyHasDiscNotes);
        toolbar.setDiscNoteType(discNoteType);
        toolbar.setResolutionStatus(resolutionStatus);
        toolbar.setModule(module);
        toolbar.setResword(resword);

        Limit limit = tableFacade.getLimit();
        toolbar.setFilterSet(limit.getFilterSet());
        toolbar.setSortSet(limit.getSortSet());
        tableFacade.setToolbar(toolbar);
    }

    public List<DiscrepancyNoteBean> findAllNotes(TableFacade tableFacade) {
    	Limit limit = tableFacade.getLimit();
    	limit.setRowSelect(null);
    	ViewNotesFilterCriteria filter = ViewNotesFilterCriteria.buildFilterCriteria(
    			limit,
    			getDateFormat(),
                discrepancyNoteTypeDropdown.getDecoder(),
                resolutionStatusDropdown.getDecoder());
        List<DiscrepancyNoteBean> items = getViewNotesService().listNotes(
        		getCurrentStudy(),
        		filter,
                ViewNotesSortCriteria.buildFilterCriteria(limit.getSortSet()));
        return items;
    }

    @Override
    public void setDataAndLimitVariables(TableFacade tableFacade) {
        // initialize i18n
        resword = ResourceBundleProvider.getWordsBundle(getLocale());
        resformat = ResourceBundleProvider.getFormatBundle(getLocale());

        Limit limit = tableFacade.getLimit();
        if (!limit.isComplete()) {

            int totalRows = getDiscrepancyNoteDao().getCountWithFilter(getListNoteFilter(limit), currentStudy);
            tableFacade.setTotalRows(totalRows);
        }

        ViewNotesFilterCriteria filter = ViewNotesFilterCriteria.buildFilterCriteria(limit, getDateFormat(),
                discrepancyNoteTypeDropdown.getDecoder(), resolutionStatusDropdown.getDecoder());

        notesSummary = getViewNotesService().calculateNotesSummary(getCurrentStudy(), filter);

        int pageSize = limit.getRowSelect().getMaxRows();
        int firstRecordShown = (limit.getRowSelect().getPage() - 1) * pageSize;
        if (firstRecordShown > notesSummary.getTotal()) { // The page selected goes beyond the dataset size
            // Move to the last page
            limit.getRowSelect().setPage((int) Math.ceil((double) notesSummary.getTotal() / pageSize));
            filter = ViewNotesFilterCriteria.buildFilterCriteria(limit, getDateFormat(),
                    discrepancyNoteTypeDropdown.getDecoder(), resolutionStatusDropdown.getDecoder());
        }

        List<DiscrepancyNoteBean> items = getViewNotesService().listNotes(getCurrentStudy(), filter,
                ViewNotesSortCriteria.buildFilterCriteria(limit.getSortSet()));

        this.setAllNotes(items);

        Collection<HashMap<Object, Object>> theItems = new ArrayList<HashMap<Object, Object>>();

        for (DiscrepancyNoteBean discrepancyNoteBean : items) {

            HashMap<Object, Object> h = new HashMap<Object, Object>();

            h.put("studySubject", discrepancyNoteBean.getStudySub());
            h.put("studySubject.label", discrepancyNoteBean.getStudySub().getLabel());
            h.put("discrepancyNoteBean.disType", discrepancyNoteBean.getDisType());
            h.put("discrepancyNoteBean.resolutionStatus", discrepancyNoteBean.getResStatus());
            h.put("age", discrepancyNoteBean.getAge());
            h.put("days", discrepancyNoteBean.getDays());
            h.put("siteId", discrepancyNoteBean.getSiteId());
            h.put("discrepancyNoteBean", discrepancyNoteBean);
            h.put("discrepancyNoteBean.createdDate", discrepancyNoteBean.getCreatedDate());
            h.put("discrepancyNoteBean.updatedDate", discrepancyNoteBean.getUpdatedDate());
            h.put("eventName", discrepancyNoteBean.getEventName());
            h.put("eventStartDate", discrepancyNoteBean.getEventStart());
            h.put("crfName", discrepancyNoteBean.getCrfName());
            h.put("crfStatus", discrepancyNoteBean.getCrfStatus());
            h.put("entityName", discrepancyNoteBean.getEntityName());
            h.put("entityValue", discrepancyNoteBean.getEntityValue());
            h.put("discrepancyNoteBean", discrepancyNoteBean);
            h.put("discrepancyNoteBean.description", discrepancyNoteBean.getDescription());
            h.put("discrepancyNoteBean.detailedNotes", discrepancyNoteBean.getDetailedNotes());
            h.put("numberOfNotes", discrepancyNoteBean.getNumChildren());
            h.put("discrepancyNoteBean.user", discrepancyNoteBean.getAssignedUser());
            h.put("discrepancyNoteBean.entityType", discrepancyNoteBean.getEntityType());
            h.put("discrepancyNoteBean.owner", discrepancyNoteBean.getOwner());

            theItems.add(h);
            setStudyHasDiscNotes(true);
        }
        tableFacade.setItems(theItems);
    }

    @Override
    public TableFacade getTableFacadeImpl(HttpServletRequest request, HttpServletResponse response) {
        TableFacade facade = super.getTableFacadeImpl(request, response);
        facade.autoFilterAndSort(false); // Filtering and sorting performed on the DB layer
        return facade;
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
            }else if("discrepancyNoteBean.disType".equalsIgnoreCase(property)) {
                ResourceBundle reterm = ResourceBundleProvider.getTermsBundle();
                if(reterm.getString("Query_and_Failed_Validation_Check").equals(value)) {
                    value = 31 + "";
                } else {
                    value = DiscrepancyNoteType.getByName(value).getId()+"";
                }
            }else if("discrepancyNoteBean.resolutionStatus".equalsIgnoreCase(property)) {
                ResourceBundle reterm = ResourceBundleProvider.getTermsBundle();
                if(reterm.getString("New_and_Updated").equalsIgnoreCase(value)){
                    value = 21 + "";
                } else {
                    value = ResolutionStatus.getByName(value).getId()+"";
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

    private class ResolutionStatusDroplistFilterEditor extends DropdownFilter {

        public ResolutionStatusDroplistFilterEditor() {
            ResourceBundle reterm = ResourceBundleProvider.getTermsBundle();
            for (ResolutionStatus status : ResolutionStatus.list) {
                this.addOption(Integer.toString(status.getId()), status.getName());
            }
            this.addOption("1,2", reterm.getString("New_and_Updated"));
        }
    }

    private class TypeDroplistFilterEditor extends DropdownFilter {

        public TypeDroplistFilterEditor() {
            ResourceBundle reterm = ResourceBundleProvider.getTermsBundle();
            for (DiscrepancyNoteType type : DiscrepancyNoteType.list) {
                this.addOption(Integer.toString(type.getId()), type.getName());
            }
            this.addOption("1,3", reterm.getString("Query_and_Failed_Validation_Check"));
        }
    }

    private class ResolutionStatusCellEditor implements CellEditor {
        @Override
        @SuppressWarnings("unchecked")
        public Object getValue(Object item, String property, int rowcount) {
            String value = "";
            ResolutionStatus status = (ResolutionStatus) ((HashMap<Object, Object>) item).get("discrepancyNoteBean.resolutionStatus");

            if (status != null) {
                value = "<img src=\""+status.getIconFilePath() +"\" border=\"0\" align=\"left\"> &nbsp;&nbsp;" + status.getName() ;
            }
            return value;
        }
    }

    private class DiscrepancyNoteTypeCellEditor implements CellEditor {
        @Override
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

    private class OwnerCellEditor implements CellEditor {
        @Override
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

    private class AssignedUserCellEditor implements CellEditor {
        @Override
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

    private class EntityNameCellEditor implements CellEditor {




        @Override
        @SuppressWarnings("rawtypes")
        public Object getValue(Object item, String property, int rowcount) {
            DiscrepancyNoteBean bean = (DiscrepancyNoteBean) ((Map) item).get("discrepancyNoteBean");
            String entityName = "";
            if (bean.getEntityType().equals("itemData")) {
                entityName =  bean.getEntityName();
            } else {
                try {
                    entityName = resword.getString(bean.getEntityName());
                } catch (MissingResourceException e) {
                    logger.warn("Missing translation for key '" + bean.getEntityName() + "'", e);
                    entityName = "###" + bean.getEntityName() + "###";
                }

            }
            return entityName;

        }
    }

    private class ActionsCellEditor implements CellEditor {
        @Override
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

    // Ignore the mathing values with filter
    public class AgeDaysFilterMatcher implements FilterMatcher {
        @Override
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

    public List<DiscrepancyNoteBean> getAllNotes() {
        return allNotes;
    }

    public void setAllNotes(List<DiscrepancyNoteBean> allNotes) {
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

    public ViewNotesService getViewNotesService() {
        return viewNotesService;
    }

    public void setViewNotesService(ViewNotesService viewNotesService) {
        this.viewNotesService = viewNotesService;
    }

    public DiscrepancyNotesSummary getNotesSummary() {
        return notesSummary;
    }

}
