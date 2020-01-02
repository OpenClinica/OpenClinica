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

import core.org.akaza.openclinica.bean.core.DiscrepancyNoteType;
import core.org.akaza.openclinica.bean.core.ResolutionStatus;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.*;
import core.org.akaza.openclinica.dao.hibernate.*;
import core.org.akaza.openclinica.domain.datamap.*;
import core.org.akaza.openclinica.service.PermissionService;
import org.akaza.openclinica.control.AbstractTableFactory;
import org.akaza.openclinica.control.DefaultActionsEditor;
import org.akaza.openclinica.control.DropdownFilter;
import core.org.akaza.openclinica.dao.admin.CRFDAO;
import core.org.akaza.openclinica.dao.login.UserAccountDAO;
import core.org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import core.org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import core.org.akaza.openclinica.dao.managestudy.ListNotesFilter;
import core.org.akaza.openclinica.dao.managestudy.ListNotesSort;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import core.org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import core.org.akaza.openclinica.dao.submit.CRFVersionDAO;
import core.org.akaza.openclinica.dao.submit.EventCRFDAO;
import core.org.akaza.openclinica.dao.submit.ItemDAO;
import core.org.akaza.openclinica.dao.submit.ItemDataDAO;
import core.org.akaza.openclinica.dao.submit.SubjectDAO;
import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import core.org.akaza.openclinica.service.DiscrepancyNotesSummary;
import core.org.akaza.openclinica.service.managestudy.ViewNotesFilterCriteria;
import core.org.akaza.openclinica.service.managestudy.ViewNotesService;
import core.org.akaza.openclinica.service.managestudy.ViewNotesSortCriteria;
import org.akaza.openclinica.service.ViewStudySubjectService;
import org.jmesa.core.filter.FilterMatcher;
import org.jmesa.facade.TableFacade;
import org.jmesa.limit.Filter;
import org.jmesa.limit.FilterSet;
import org.jmesa.limit.Limit;
import org.jmesa.limit.Sort;
import org.jmesa.limit.SortSet;
import org.jmesa.util.ItemUtils;
import org.jmesa.view.component.Row;
import org.jmesa.view.editor.CellEditor;
import org.jmesa.view.editor.DateCellEditor;
import org.jmesa.view.html.HtmlBuilder;
import org.jmesa.view.html.editor.DroplistFilterEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class ListNotesTableFactory extends AbstractTableFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ListNotesTableFactory.class.getName());

    private AuditUserLoginDao auditUserLoginDao;
    private StudySubjectDAO studySubjectDao;
    private UserAccountDAO userAccountDao;
    private DiscrepancyNoteDAO discrepancyNoteDao;
    private SubjectDAO subjectDao;
    private StudyEventDefinitionDAO studyEventDefinitionDao;
    private EventDefinitionCRFDAO eventDefinitionCRFDao;
    private EventCRFDAO eventCRFDao;
    private Study currentStudy;
    private ResourceBundle resword = ResourceBundleProvider.getWordsBundle();
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
    private static final String QUERY_FLAVOR = "-query";
    public static final String SINGLE_ITEM_FLAVOR = "-single_item";
    private List<String> userTags = null;
    private HttpServletRequest request;

    private ViewStudySubjectService viewStudySubjectService;
    private PermissionService permissionService;
    public static final String PAGE_NAME = "queries-table";
    public static final String COMPONENT_NAME="queries-table-table";
    public static final String DOT = ".";

    private static final String CHECKBOX = "checkbox";
    private static final String MULTI_SELECT = "multi-select";
    private static final String RADIO = "radio";
    private static final String SINGLE_SELECT = "single-select";
    public static final String ITEM_DATA = "itemData";
    public static final String STUDY_EVENT = "studyEvent";


    private ItemDataDao itemDataDao;
    private ItemDao itemDao;
    private ItemFormMetadataDao itemFormMetadataDao;
    private ResponseSetDao responseSetDao;
    private EventCrfDao eventCrfDao;
    private StudyEventDao studyEventDao;
    private CrfDao crfDao;
    private CrfVersionDao crfVersionDao;
    private EventDefinitionCrfDao eventDefinitionCrfDao;
    private StudyEventDefinitionDao studyEventDefinitionHibDao;
    private EventDefinitionCrfPermissionTagDao permissionTagDao;
    List<String> permissionTagsList = null;
    private final String  PARTICIPATE_STATUS="participate.status";
    private String[] columnNames = new String[]{};
    private ResponseSet responseSet;


    public ListNotesTableFactory(boolean showMoreLink, List<String> userTags) {
        this.showMoreLink = showMoreLink;
        this.userTags = userTags;
    }

    @Override
    protected String getTableName() {
        return "listNotes";
    }

    @Override
    protected void configureColumns(TableFacade tableFacade, Locale locale) {


        getColumnNamesMap();
        tableFacade.setColumnProperties(columnNames);

        Row row = tableFacade.getTable().getRow();
        configureColumn(row.getColumn("discrepancyNoteBean.threadNumber"), resword.getString("query_id"), null, null, true, true);
        configureColumn(row.getColumn("studySubject.label"), resword.getString("study_subject_ID"), null, null, true, true);
        configureColumn(row.getColumn("siteId"), resword.getString("site_id"), null, null, true, false);


        String[] tableColumns = getViewStudySubjectService().getTableColumns(ListNotesTableFactory.PAGE_NAME,ListNotesTableFactory.COMPONENT_NAME);
        if (tableColumns != null) {
            for (String column : tableColumns) {
                if (permissionService.isUserHasPermission(column, request, currentStudy)) {
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
                            configureColumn(row.getColumn(column), item.getBriefDescription()!=null? item.getBriefDescription() :itemFormMetadata.getLeftItemText(), new ItemIdCellEditor(), new CustomColumnDroplistFilterEditor(),true,true);
                        } else {
                            configureColumn(row.getColumn(column), item.getBriefDescription()!=null? item.getBriefDescription() :itemFormMetadata.getLeftItemText(), new ItemIdCellEditor(), null,true,true);
                        }
                    }
                }
            }
        }




        configureColumn(row.getColumn("discrepancyNoteBean.createdDate"), resword.getString("date_created"), new DateCellEditor(getDateFormat()), null, true,
                true);
        configureColumn(row.getColumn("discrepancyNoteBean.updatedDate"), resword.getString("date_updated"), new DateCellEditor(getDateFormat()), null, true,
                false);
        configureColumn(row.getColumn("age"), resword.getString("days_open"), null, null);
        configureColumn(row.getColumn("days"), resword.getString("days_since_updated"), null, null);
        configureColumn(row.getColumn("eventStartDate"), resword.getString("event_date"), new DateCellEditor(getDateFormat()), null, false, false);
        configureColumn(row.getColumn("eventName"), resword.getString("event_name"), null, null, true, false);
        configureColumn(row.getColumn("crfName"), resword.getString("CRF"), null, null, true, false);
        configureColumn(row.getColumn("crfStatus"), resword.getString("CRF_status"), null, null, false, false);
        configureColumn(row.getColumn("entityName"), resword.getString("entity_name"), new EntityNameCellEditor(), null, true, false);
        configureColumn(row.getColumn("entityValue"), resword.getString("entity_value"), null, null, true, false);
        configureColumn(row.getColumn("discrepancyNoteBean.resolutionStatus"), resword.getString("resolution_status"), new ResolutionStatusCellEditor(),
                resolutionStatusDropdown, true, false);
        configureColumn(row.getColumn("discrepancyNoteBean.detailedNotes"), resword.getString("detailed_notes"), null, null, true, false);
        configureColumn(row.getColumn("numberOfNotes"), resword.getString("of_notes"), null, null, false, false);
        configureColumn(row.getColumn("discrepancyNoteBean.user"), resword.getString("assigned_user"), new AssignedUserCellEditor(), null, true, false);
        configureColumn(row.getColumn("discrepancyNoteBean.entityType"), resword.getString("entity_type"), null, null, true, false);
        configureColumn(row.getColumn("discrepancyNoteBean.disType"), resword.getString("type"), new DiscrepancyNoteTypeCellEditor(),
                discrepancyNoteTypeDropdown, true, false);

        String actionsHeader = resword.getString("actions") + "&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;";
        configureColumn(row.getColumn("actions"), actionsHeader, new ActionsCellEditor(), new DefaultActionsEditor(locale), true, false);
    }

    @Override
    public void configureTableFacadePostColumnConfiguration(TableFacade tableFacade) {
        ListNotesTableToolbar toolbar = new ListNotesTableToolbar(showMoreLink,viewStudySubjectService,permissionService,currentStudy,request );
        toolbar.setStudyHasDiscNotes(studyHasDiscNotes);

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
        ViewNotesFilterCriteria filter = ViewNotesFilterCriteria.buildFilterCriteria(limit, getDateFormat(), discrepancyNoteTypeDropdown.getDecoder(),
                resolutionStatusDropdown.getDecoder());
        List<DiscrepancyNoteBean> items = getViewNotesService().listNotes(getCurrentStudy(), filter,
                ViewNotesSortCriteria.buildFilterCriteria(limit.getSortSet(), itemDao), userTags);
        return items;
    }

    @Override
    public void setDataAndLimitVariables(TableFacade tableFacade) {
        // initialize i18n
        resword = ResourceBundleProvider.getWordsBundle(getLocale());
        resformat = ResourceBundleProvider.getFormatBundle(getLocale());
        int parentStudyId = 0;

        // https://jira.openclinica.com/browse/OC-9952
        tableFacade.setMaxRows(50);

        Limit limit = tableFacade.getLimit();

        if (!limit.isComplete()) {
            parentStudyId = currentStudy.getStudyId();

            // Build row count of various DN types
            int totalRows = getDiscrepancyNoteDao().getSubjectDNCountWithFilter(getListNoteFilter(limit), parentStudyId);
            totalRows += getDiscrepancyNoteDao().getStudySubjectDNCountWithFilter(getListNoteFilter(limit), parentStudyId);
            totalRows += getDiscrepancyNoteDao().getStudyEventDNCountWithFilter(getListNoteFilter(limit), parentStudyId);
            totalRows += getDiscrepancyNoteDao().getEventCrfDNCountWithFilter(getListNoteFilter(limit), parentStudyId);
            totalRows += getDiscrepancyNoteDao().getItemDataDNCountWithFilter(getListNoteFilter(limit), parentStudyId);
            tableFacade.setTotalRows(totalRows);
        }

        ViewNotesFilterCriteria filter = ViewNotesFilterCriteria.buildFilterCriteria(limit, getDateFormat(), discrepancyNoteTypeDropdown.getDecoder(),
                resolutionStatusDropdown.getDecoder());

        notesSummary = getViewNotesService().calculateNotesSummary(getCurrentStudy(), filter, false, userTags);

        int pageSize = limit.getRowSelect().getMaxRows();
        int firstRecordShown = (limit.getRowSelect().getPage() - 1) * pageSize;
        if (firstRecordShown > notesSummary.getTotal() && notesSummary.getTotal() != 0) { // The page selected goes
                                                                                          // beyond the dataset size
            // Move to the last page
            limit.getRowSelect().setPage((int) Math.ceil((double) notesSummary.getTotal() / pageSize));
            filter = ViewNotesFilterCriteria.buildFilterCriteria(limit, getDateFormat(), discrepancyNoteTypeDropdown.getDecoder(),
                    resolutionStatusDropdown.getDecoder());
        }

        List<DiscrepancyNoteBean> items = getViewNotesService().listNotes(getCurrentStudy(), filter,
                ViewNotesSortCriteria.buildFilterCriteria(limit.getSortSet(), itemDao), userTags);

        this.setAllNotes(items);

        Collection<HashMap<Object, Object>> theItems = new ArrayList<HashMap<Object, Object>>();

        DiscrepancyNoteBean dNBean;

        for (DiscrepancyNoteBean discrepancyNoteBean : items) {
            int item_data_id = 0;
            int study_event_id = 0;
            ItemData itemData = null;
            StudyEvent studyEvent = null;
            StudyEventDefinition studyEventDefinition = null;

            if (discrepancyNoteBean.getEntityType().equals("itemData")) {
                item_data_id = discrepancyNoteBean.getEntityId();
                itemData = itemDataDao.findById(item_data_id);
                discrepancyNoteBean.setEntityName(itemData.getItem().getName());
                discrepancyNoteBean.setEntityValue(itemData.getValue());
                EventCrf eventCrf = itemData.getEventCrf();
                CrfBean crf = eventCrf.getCrfVersion().getCrf();
                discrepancyNoteBean.setCrfName(crf.getName());
                discrepancyNoteBean.setCrfStatus(crf.getStatus().getName());
                studyEvent = eventCrf.getStudyEvent();
                studyEventDefinition = studyEvent.getStudyEventDefinition();
                discrepancyNoteBean.setEventName(studyEventDefinition.getName());
                discrepancyNoteBean.setEventStart(studyEvent.getDateStart());

            } else if (discrepancyNoteBean.getEntityType().equals("studyEvent")) {
                study_event_id = discrepancyNoteBean.getEntityId();
                studyEvent = studyEventDao.findById(study_event_id);
                discrepancyNoteBean.setEventName(studyEvent.getStudyEventDefinition().getName());
            }


            HashMap<Object, Object> h = new HashMap<Object, Object>();

            h.put("studySubject", discrepancyNoteBean.getStudySub());
            h.put("discrepancyNoteBean.disType", discrepancyNoteBean.getDisType());
            h.put("studySubject.label", discrepancyNoteBean.getStudySub().getLabel());
            h.put("discrepancyNoteBean.resolutionStatus", discrepancyNoteBean.getResStatus());
            h.put("age", discrepancyNoteBean.getAge());
            h.put("days", discrepancyNoteBean.getDays());
            h.put("siteId", discrepancyNoteBean.getSiteId());
            h.put("discrepancyNoteBean", discrepancyNoteBean);
            if (discrepancyNoteBean.getDisType().equals(DiscrepancyNoteType.QUERY) &&
                    (discrepancyNoteBean.getResStatus().equals(ResolutionStatus.UPDATED)) ||
                    discrepancyNoteBean.getResStatus().equals(ResolutionStatus.CLOSED)) {
                // OC-10617 After update, Queries Table displays incorrect date created.
                // use the first child createdDate
                if (discrepancyNoteBean.getParentDnId() > 0) {
                    dNBean = (DiscrepancyNoteBean) discrepancyNoteDao.findFirstChildByParent(
                            discrepancyNoteBean.getParentDnId());
                } else {
                    // this entity is parent
                    dNBean = (DiscrepancyNoteBean) discrepancyNoteDao.findFirstChildByParent(
                            discrepancyNoteBean.getId());
                }
                h.put("discrepancyNoteBean.createdDate", dNBean.getCreatedDate());
            } else {
                h.put("discrepancyNoteBean.createdDate", discrepancyNoteBean.getCreatedDate());
            }
            h.put("discrepancyNoteBean.updatedDate", discrepancyNoteBean.getUpdatedDate());
            h.put("eventName", discrepancyNoteBean.getEventName());
            h.put("eventStartDate", discrepancyNoteBean.getEventStart());
            h.put("crfName", discrepancyNoteBean.getCrfName());
            h.put("crfStatus", discrepancyNoteBean.getCrfStatus());
            h.put("entityName", discrepancyNoteBean.getEntityName());
            h.put("entityValue", discrepancyNoteBean.getEntityValue());
            DiscrepancyNoteBean parentdNBean;
            if (discrepancyNoteBean.getParentDnId() > 0) {
                dNBean = (DiscrepancyNoteBean) discrepancyNoteDao.findLatestChildByParent(
                        discrepancyNoteBean.getParentDnId());
                parentdNBean = (DiscrepancyNoteBean) discrepancyNoteDao.findByPK(discrepancyNoteBean.getParentDnId());
            } else {
                // this entity is parent
                dNBean = (DiscrepancyNoteBean) discrepancyNoteDao.findLatestChildByParent(
                        discrepancyNoteBean.getId());
                parentdNBean = discrepancyNoteBean;
            }
            // if DisType is QUERY(3) and ResolutionStatus is UPDATE or CLOSED
            // then find latest detailedNotes
            if (discrepancyNoteBean.getDisType().equals(DiscrepancyNoteType.QUERY) &&
                    (discrepancyNoteBean.getResStatus().equals(ResolutionStatus.UPDATED)) ||
                    discrepancyNoteBean.getResStatus().equals(ResolutionStatus.CLOSED)) {

                h.put("discrepancyNoteBean.detailedNotes", dNBean.getDetailedNotes());
            } else {
                h.put("discrepancyNoteBean.detailedNotes", discrepancyNoteBean.getDetailedNotes());
            }
            if (parentdNBean.getThreadNumber() == null || parentdNBean.getThreadNumber() == 0) {
                h.put("discrepancyNoteBean.threadNumber", resword.getString("na"));
            } else {
                h.put("discrepancyNoteBean.threadNumber", parentdNBean.getThreadNumber());
            }
            h.put("numberOfNotes", discrepancyNoteBean.getNumChildren());
            h.put("discrepancyNoteBean.user", discrepancyNoteBean.getAssignedUser());
            h.put("discrepancyNoteBean.entityType", discrepancyNoteBean.getEntityType());

            List<CustomColumn> customColumns = getCustomColumns(discrepancyNoteBean, currentStudy, request);
            for (CustomColumn customColumn : customColumns) {
                h.put(customColumn.getName(), customColumn.getValue());
            }

            theItems.add(h);
            setStudyHasDiscNotes(true);
        }
        tableFacade.setItems(theItems);
    }

    @Override
    public TableFacade getTableFacadeImpl(HttpServletRequest request, HttpServletResponse response) {
        TableFacade facade = super.getTableFacadeImpl(request, response);
        facade.autoFilterAndSort(false); // Filtering and sorting performed on the DB layer
        this.request=request;
        return facade;
    }

    /**
     * A very custom way to filter the items. The AuditUserLoginFilter acts as a command for the Hibernate criteria
     * object. Take the Limit information and
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
            // Checking if the given date format is valid
            if ("discrepancyNoteBean.createdDate".equalsIgnoreCase(property) || "discrepancyNoteBean.updatedDate".equalsIgnoreCase(property)) {
                try {
                    String date = formatDate(new Date(value));
                    value = date;
                } catch (Exception ex) {
                    value = "01-Jan-1700";
                }
            } else if ("discrepancyNoteBean.disType".equalsIgnoreCase(property)) {
                ResourceBundle reterm = ResourceBundleProvider.getTermsBundle();
                if (reterm.getString("Query_and_Failed_Validation_Check").equals(value)) {
                    value = ListNotesFilter.filterDnTypeQueryAndFailedValidationCheck + "";
                } else {
                    value = DiscrepancyNoteType.getByName(value).getId() + "";
                }
            } else if ("discrepancyNoteBean.resolutionStatus".equalsIgnoreCase(property)) {
                ResourceBundle reterm = ResourceBundleProvider.getTermsBundle();
                if (reterm.getString("New_and_Updated").equalsIgnoreCase(value)) {
                    value = ListNotesFilter.filterResStatusNewAndUpdated + "";
                } else if (reterm.getString("Closed_And_Closed_Modified").equalsIgnoreCase(value)) {
                    value = ListNotesFilter.filterResStatusClosedAndClosedModified + "";
                } else {
                    value = ResolutionStatus.getByNameResStatus(value).getId() + "";
                }
            }
            //
            listNotesFilter.addFilter(property, value);
        }

        return listNotesFilter;
    }

    /**
     * A very custom way to sort the items. The AuditUserLoginSort acts as a command for the Hibernate criteria object.
     * Take the Limit information and sort the
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

    private class CustomColumnDroplistFilterEditor extends DroplistFilterEditor {
        List<String> optionsText = Arrays.asList(responseSet.getOptionsText().split("\\s*,\\s*"));
        @Override
        protected List<DroplistFilterEditor.Option> getOptions() {
            List<Option> options = new ArrayList<Option>();
            for (String optionText : optionsText) {
                options.add(new Option( optionText, optionText));
            }
            return options;
        }
    }
    private class ItemIdCellEditor implements CellEditor {
        public Object getValue(Object item, String property, int rowcount) {
            Object itemValue = ItemUtils.getItemValue(item, property);
            return itemValue;
        }
    }

    private class ResolutionStatusDroplistFilterEditor extends DropdownFilter {

        public ResolutionStatusDroplistFilterEditor() {
            ResourceBundle reterm = ResourceBundleProvider.getTermsBundle();
            for (ResolutionStatus status : ResolutionStatus.listResStatus) {
                this.addOption(Integer.toString(status.getId()), status.getName());
            }
            this.addOption("1,2", reterm.getString("New_and_Updated"));
            this.addOption("4,6", reterm.getString("Closed_And_Closed_Modified"));
        }
    }

    private class TypeDroplistFilterEditor extends DropdownFilter {

        public TypeDroplistFilterEditor() {
            ResourceBundle reterm = ResourceBundleProvider.getTermsBundle();
            for (DiscrepancyNoteType type : DiscrepancyNoteType.list) {
                // filter only show query and reason_for_change type
                if (type.getId() == 2 || type.getId() == 3 || type.getId() == 4) {
                    this.addOption(Integer.toString(type.getId()), type.getName());
                }
            }
            // this.addOption("1,3", reterm.getString("Query_and_Failed_Validation_Check"));
        }
    }

    private class ResolutionStatusCellEditor implements CellEditor {
        @Override
        @SuppressWarnings("unchecked")
        public Object getValue(Object item, String property, int rowcount) {
            String value = "";
            ResolutionStatus status = (ResolutionStatus) ((HashMap<Object, Object>) item).get("discrepancyNoteBean.resolutionStatus");

            if (status != null) {
                value = "<span class=\"" + status.getIconFilePath() + "\" border=\"0\" align=\"left\"> <text>" + status.getName() + "</text>";
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
                entityName = bean.getEntityName();
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

            StudySubjectBean studySubjectBean = (StudySubjectBean) ((HashMap<Object, Object>) item).get("studySubject");
            Integer studySubjectId = studySubjectBean.getId();
            if (studySubjectId != null) {
                StringBuilder url = new StringBuilder();
                url.append(downloadNotesLinkBuilder(studySubjectBean));
                value = url.toString();
            }
            // for "view" as action
            // This createNoteURL uses the same method as in ResolveDiscrepancyServlet
            if (dnb.getEntityType().equals(DiscrepancyNoteBean.ITEM_DATA)) {
                builder.a().href("ResolveDiscrepancy?noteId=" + dnb.getId() + "&flavor=" + QUERY_FLAVOR);
            } else if (dnb.getEntityType().equals(DiscrepancyNoteBean.STUDY_EVENT)) {
                builder.a().href("EnterDataForStudyEvent?eventId=" + dnb.getEntityId());
            } else if (dnb.getEntityType().equals(DiscrepancyNoteBean.SUBJECT)) {
                builder.a().href("ViewStudySubject?id=" + studySubjectId);
            } else if (dnb.getEntityType().equals(DiscrepancyNoteBean.EVENT_CRF)) {
                builder.a().href("ViewStudySubject?id=" + studySubjectId);
            } else if (!dnb.getEntityType().equals(DiscrepancyNoteBean.SUBJECT) && !dnb.getEntityType().equals(DiscrepancyNoteBean.ITEM_DATA) && !dnb.getEntityType().equals(DiscrepancyNoteBean.STUDY_EVENT) && !dnb.getEntityType().equals(DiscrepancyNoteBean.EVENT_CRF)){
               builder.a().href("ViewStudySubject?id=" + studySubjectId); 
            }
            builder.close();
            builder.append("<span title='" + resword.getString("View_Query_Within_Record")
                    + "' border=\"0\" align=\"left\" class=\"icon icon-view-within\" hspace=\"6\"/>");
            builder.append("&nbsp;");
            builder.aEnd();

            if (dnb.getEntityType().equals(DiscrepancyNoteBean.ITEM_DATA)) {
                builder.a().href("ResolveDiscrepancy?noteId=" + dnb.getId() + "&flavor=" + SINGLE_ITEM_FLAVOR);
            } else {
                String createNoteURL = CreateDiscrepancyNoteServlet.getAddChildURL(dnb, ResolutionStatus.CLOSED, true);
                builder.a().href("javascript:openDNWindow('" + createNoteURL + "&viewAction=1" + "');");
            }
            builder.close();
            builder.append("<span title='" + resword.getString("View_Query_Only") + "' border=\"0\" align=\"left\" class=\"icon icon-search\" hspace=\"6\"/>");
            builder.append("&nbsp;");
            builder.aEnd();

            return builder.toString();
        }

    }

    private String downloadNotesLinkBuilder(StudySubjectBean studySubject) {
        HtmlBuilder actionLink = new HtmlBuilder();
        if (this.isStudyHasDiscNotes()) {
            if (this.getResolutionStatus() >= 1 && this.getResolutionStatus() <= 5) {
                actionLink.a().href("javascript:openDocWindow('ChooseDownloadFormat?subjectId=" + studySubject.getId() + "&discNoteType=" + discNoteType
                        + "&resolutionStatus=" + resolutionStatus + "')");
                actionLink.append(
                        "<span title=\"Download queries for all subjects\" border=\"0\" align=\"left\" class=\"icon icon-download\" hspace=\"4\" width=\"24 \" height=\"15\"/>");
                actionLink.append("&nbsp;&nbsp;&nbsp;");
            } else {
                actionLink.a().href("javascript:openDocWindow('ChooseDownloadFormat?subjectId=" + studySubject.getId() + "&discNoteType=" + discNoteType
                        + "&module=" + module + "')");
                actionLink.append(
                        "<span title=\"Download queries for all subjects\" border=\"0\" align=\"left\" class=\"icon icon-download\" hspace=\"4\" width=\"24 \" height=\"15\"/>");
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

    public Study getCurrentStudy() {
        return currentStudy;
    }

    public void setCurrentStudy(Study currentStudy) {
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

    public EventDefinitionCRFDAO getEventDefinitionCRFDao() {
        return eventDefinitionCRFDao;
    }

    public void setEventDefinitionCRFDao(EventDefinitionCRFDAO eventDefinitionCRFDao) {
        this.eventDefinitionCRFDao = eventDefinitionCRFDao;
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
        ViewNotesFilterCriteria.itemDao=itemDao;
    }

    public void setItemFormMetadataDao(ItemFormMetadataDao itemFormMetadataDao) {
        this.itemFormMetadataDao = itemFormMetadataDao;
        ViewNotesFilterCriteria.itemFormMetadataDao=itemFormMetadataDao;

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
        ViewNotesFilterCriteria.crfDao=crfDao;

    }

    public void setStudyEventDefinitionDAO(StudyEventDefinitionDAO studyEventDefinitionDAO) {
        this.studyEventDefinitionDao = studyEventDefinitionDAO;
    }

    public void setCrfVersionDao(CrfVersionDao crfVersionDao) {
        this.crfVersionDao = crfVersionDao;
        ViewNotesFilterCriteria.crfVersionDao=crfVersionDao;

    }

    public void setEventDefinitionCrfDao(EventDefinitionCrfDao eventDefinitionCrfDao) {
        this.eventDefinitionCrfDao = eventDefinitionCrfDao;
    }

    public void setPermissionTagDao(EventDefinitionCrfPermissionTagDao permissionTagDao) {
        this.permissionTagDao = permissionTagDao;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public void setStudyEventDefinitionHibDao(StudyEventDefinitionDao studyEventDefinitionHibDao) {
        this.studyEventDefinitionHibDao = studyEventDefinitionHibDao;
    }

    private void getColumnNamesMap() {
        ArrayList<String> columnNamesList = new ArrayList<String>();
        columnNamesList.add("discrepancyNoteBean.threadNumber");
        columnNamesList.add("studySubject.label");
        columnNamesList.add("siteId");

        String [] tableColumns= getViewStudySubjectService().getTableColumns(ListNotesTableFactory.PAGE_NAME,ListNotesTableFactory.COMPONENT_NAME);
        if(tableColumns!=null){
            for (String column : tableColumns) {
                if (permissionService.isUserHasPermission(column, request, currentStudy)) {
                    columnNamesList.add(column);
                }
            }
        }


        columnNamesList.add("discrepancyNoteBean.disType");
        columnNamesList.add("discrepancyNoteBean.resolutionStatus");
        columnNamesList.add("discrepancyNoteBean.createdDate");
        columnNamesList.add("discrepancyNoteBean.updatedDate");
        columnNamesList.add("age");
        columnNamesList.add("days");
        columnNamesList.add("eventName");
        columnNamesList.add("eventStartDate");
        columnNamesList.add("crfName");
        columnNamesList.add("crfStatus");
        columnNamesList.add("entityName");
        columnNamesList.add("entityValue");
        columnNamesList.add("discrepancyNoteBean.entityType");
        columnNamesList.add("discrepancyNoteBean.detailedNotes");
        columnNamesList.add("numberOfNotes");
        columnNamesList.add("discrepancyNoteBean.user");
        columnNamesList.add("actions");
        columnNames = columnNamesList.toArray(columnNames);
    }

    public PermissionService getPermissionService() {
        return permissionService;
    }

    public List<CustomColumn> getCustomColumns(DiscrepancyNoteBean discrepancyNoteBean, Study studyBean, HttpServletRequest request) {
        List<CustomColumn> customColumns = new ArrayList<>();
        String[] tableColumns = getViewStudySubjectService().getTableColumns(PAGE_NAME, COMPONENT_NAME);
        if (tableColumns != null
                && discrepancyNoteBean.getEntityType().equals(ITEM_DATA)) {
            for (String column : tableColumns) {
                String itemValue = null;

                if (getPermissionService().isUserHasPermission(column, request, studyBean)) {
                    String sedOid = column.split("\\.")[0];
                    String formOid = column.split("\\.")[1];
                    String itemOid = column.split("\\.")[2];
                    StudyEventDefinition studyEventDefinition = null;
                    List<StudyEvent> studyEvents = null;
                    StudyEvent studyEvent = null;
                    List<EventCrf> eventCrfs = null;
                    EventCrf eventCrf = null;
                    List<ItemData> itemDatas;
                    ItemData itemData = null;
                    Item item = null;
                    if (!StringUtils.isEmpty(itemOid))
                        item = itemDao.findByOcOID(itemOid);
                    if (!StringUtils.isEmpty(sedOid)) {
                        studyEventDefinition = studyEventDefinitionHibDao.findByOcOID(sedOid);
                        if (studyEventDefinition != null && !studyEventDefinition.getRepeating()) {
                            studyEvents = studyEventDao.fetchListByStudyEventDefOID(sedOid, discrepancyNoteBean.getStudySub().getId());

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

                    CustomColumn customColumn = new CustomColumn();
                    customColumn.setName(column);
                    customColumn.setDescription(item.getName());
                    customColumn.setValue(itemValue);
                    customColumns.add(customColumn);
                }
            }
        }
        return customColumns;
    }

    public int getNetCountCustomColumns(Study studyBean, HttpServletRequest request) {
        int columnCount = 0;
        String[] tableColumns = getViewStudySubjectService().getTableColumns(PAGE_NAME, COMPONENT_NAME);
        if (tableColumns != null) {
            for (String column : tableColumns) {
                if (getPermissionService().isUserHasPermission(column, request, studyBean)) {
                    columnCount++;
                }
            }
        }
        return columnCount;
    }
}
