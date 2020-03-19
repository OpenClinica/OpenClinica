package org.akaza.openclinica.control.extract;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import core.org.akaza.openclinica.bean.admin.CRFBean;
import core.org.akaza.openclinica.bean.core.AuditableEntityBean;
import core.org.akaza.openclinica.bean.core.DiscrepancyNoteType;
import core.org.akaza.openclinica.bean.core.ResolutionStatus;
import core.org.akaza.openclinica.bean.extract.DownloadDiscrepancyNote;
import core.org.akaza.openclinica.bean.managestudy.*;
import core.org.akaza.openclinica.bean.submit.CRFVersionBean;
import core.org.akaza.openclinica.bean.submit.EventCRFBean;
import core.org.akaza.openclinica.bean.submit.ItemBean;
import core.org.akaza.openclinica.bean.submit.ItemDataBean;
import core.org.akaza.openclinica.bean.submit.ItemGroupBean;
import core.org.akaza.openclinica.bean.submit.ItemGroupMetadataBean;
import core.org.akaza.openclinica.bean.submit.SubjectBean;
import core.org.akaza.openclinica.dao.hibernate.*;
import core.org.akaza.openclinica.domain.EventCrfStatusEnum;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.service.PermissionService;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import core.org.akaza.openclinica.core.form.StringUtil;
import core.org.akaza.openclinica.core.util.Pair;
import core.org.akaza.openclinica.dao.admin.CRFDAO;
import core.org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import core.org.akaza.openclinica.dao.managestudy.ListNotesFilter;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import core.org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import core.org.akaza.openclinica.dao.submit.CRFVersionDAO;
import core.org.akaza.openclinica.dao.submit.EventCRFDAO;
import core.org.akaza.openclinica.dao.submit.ItemDAO;
import core.org.akaza.openclinica.dao.submit.ItemDataDAO;
import core.org.akaza.openclinica.dao.submit.ItemGroupDAO;
import core.org.akaza.openclinica.dao.submit.ItemGroupMetadataDAO;
import core.org.akaza.openclinica.domain.datamap.ItemData;
import core.org.akaza.openclinica.i18n.core.LocaleResolver;
import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import core.org.akaza.openclinica.service.DiscrepancyNoteThread;
import core.org.akaza.openclinica.service.DiscrepancyNoteUtil;
import core.org.akaza.openclinica.service.managestudy.ViewNotesFilterCriteria;
import core.org.akaza.openclinica.service.managestudy.ViewNotesService;
import core.org.akaza.openclinica.service.managestudy.ViewNotesSortCriteria;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.control.submit.ListNotesTableFactory;
import org.akaza.openclinica.service.ViewStudySubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * A servlet that sends via HTTP a file containing Discrepancy-Note related data.
 *
 * @author Bruce W. Perry
 * @see ChooseDownloadFormat
 * @see core.org.akaza.openclinica.bean.extract.DownloadDiscrepancyNote
 */
public class DiscrepancyNoteOutputServlet extends SecureController {
    // These are the headers that must appear in the HTTP response, when sending a
    // file back to the user
    public static String CONTENT_DISPOSITION_HEADER = "Content-Disposition";
    public static String CONTENT_DISPOSITION_VALUE = "attachment; filename=";
    private Map<String, String> discrepancyNoteTypesDecoder = makeDiscrepancyNoteTypesDecoder();
    private Map<String, String> resolutionStatusDecoder = makeResolutionStatusDecoder();
    private ViewStudySubjectService viewStudySubjectService;
    private PermissionService permissionService;
    private ItemDataDao itemDataDao;
    private ItemDao itemDao;
    private ItemFormMetadataDao itemFormMetadataDao;
    private StudyEventDao studyEventDao;
    private CrfDao crfDao;
    private CrfVersionDao crfVersionDao;
    private EventDefinitionCrfDao eventDefinitionCrfDao;
    private EventDefinitionCrfPermissionTagDao permissionTagDao;
    private StudyEventDefinitionDao studyEventDefinitionDao;

    /* Handle the HTTP Get or Post request. */
    @Override
    protected void processRequest() throws Exception {

        FormProcessor fp = new FormProcessor(request);
        // the fileName contains any subject id and study unique protocol id;
        // see: chooseDownloadFormat.jsp
        String fileName = request.getParameter("fileName");
        // replace any spaces in the study's unique protocol id, so that
        // the filename is formulated correctly
        if (fileName != null) {
            fileName = fileName.replaceAll(" ", "_");
        }
        fileName = fileName == null ? "" : fileName;
        // the format will be either csv (comma separated values) or pdf (portable document format)
        String format = request.getParameter("fmt");
        String studyIdentifier = request.getParameter("studyIdentifier");
        // Determine whether to limit the displayed DN's to a certain resolutionStatus
        // CHANGED TO LIST OF RESOLUTION STATUS IDS
        /*
         * int resolutionStatus = 0;
         * try {
         * resolutionStatus = Integer.parseInt(request.getParameter("resolutionStatus"));
         * } catch(NumberFormatException nfe){
         * //Show all DN's
         * resolutionStatus=-1;
         * }
         */
        // possibly for a later implementation: int definitionId = fp.getInt("defId");
        // here subjectId actually is study_subject_id !!!
        int subjectId = fp.getInt("subjectId");
        int discNoteType = fp.getInt("discNoteType");

        DownloadDiscrepancyNote downLoader = new DownloadDiscrepancyNote();
        if ("csv".equalsIgnoreCase(format)) {
            fileName = fileName + ".csv";
            response.setContentType(DownloadDiscrepancyNote.CSV);
        } else {
            response.setContentType(DownloadDiscrepancyNote.PDF);
            fileName = fileName + ".pdf";
        }
        response.addHeader(CONTENT_DISPOSITION_HEADER, CONTENT_DISPOSITION_VALUE + fileName);
        // Are we downloading a List of discrepancy notes or just one?
        // Not needed now: boolean isList = ("y".equalsIgnoreCase(isAList));
        Study studyBean = (Study) session.getAttribute("study");

        // Set<Integer> resolutionStatusIds = (HashSet) session.getAttribute("resolutionStatus");

        // It will also change any resolution status IDs among parents of children that have a different
        // id value (last boolean parameter; 'true' to perform the latter task)
        // In this case we want to include all the discrepancy notes, despite the res status or
        // type filtering, because we don't want to filter out parents, thus leaving out a child note
        // that might match the desired res status
        ListNotesFilter listNotesFilter = new ListNotesFilter();

        ViewNotesService viewNotesService = (ViewNotesService) WebApplicationContextUtils.getWebApplicationContext(getServletContext())
                .getBean("viewNotesService");

        ViewNotesFilterCriteria filter = ViewNotesFilterCriteria.buildFilterCriteria(getFilters(request), getDateFormat(), discrepancyNoteTypesDecoder,
                resolutionStatusDecoder);
        List<DiscrepancyNoteBean> notes = viewNotesService.listNotes(currentStudy, filter, ViewNotesSortCriteria.buildFilterCriteria(getSortOrder(request)), getPermissionTagsList());


        ListNotesTableFactory factory = new ListNotesTableFactory(true, getPermissionTagsList());
        factory.setPermissionService(getPermissionService());
        factory.setViewStudySubjectService(getViewStudySubjectService());
        factory.setItemDao(getItemDao());
        factory.setItemDataDao(getItemDataDao());
        factory.setCrfDao(getCrfDao());
        factory.setCrfVersionDao(getCrfVersionDao());
        factory.setStudyEventDao(getStudyEventDao());
        factory.setItemFormMetadataDao(getItemFormMetadataDao());
        factory.setEventDefinitionCrfDao(getEventDefinitionCrfDao());
        factory.setPermissionTagDao(getPermissionTagDao());
        factory.setStudyEventDefinitionHibDao(getStudyEventDefinitionDao());

        int columnCount = factory.getNetCountCustomColumns(currentStudy, request);
        for (DiscrepancyNoteBean note : notes) {
            if (note.getEntityType().equals(ListNotesTableFactory.ITEM_DATA)) {
                note.setCustomColumns(factory.getCustomColumns(note, currentStudy, request));
            } else if (note.getEntityType().equals(ListNotesTableFactory.STUDY_EVENT)) {
                List<CustomColumn> customColumns = new ArrayList<>();
                for (int i = 0; i < columnCount; i++) {
                    CustomColumn customColumn = new CustomColumn();
                    customColumns.add(customColumn);
                }
                note.setCustomColumns(customColumns);
            }

        }




        ArrayList<DiscrepancyNoteBean> allDiscNotes = notes instanceof ArrayList ? (ArrayList<DiscrepancyNoteBean>) notes
                : new ArrayList<DiscrepancyNoteBean>(notes);

        ArrayList<DiscrepancyNoteBean> accessList = (ArrayList<DiscrepancyNoteBean>) removeNoAccessNotes(allDiscNotes);
        accessList = populateRowsWithAttachedData(accessList);

        // Now we have to package all the discrepancy notes in DiscrepancyNoteThread objects
        // Do the filtering for type or status here
        DiscrepancyNoteUtil discNoteUtil = new DiscrepancyNoteUtil();

        Set<Integer> resolutionStatusIds = emptySet();
        List<DiscrepancyNoteThread> discrepancyNoteThreads = discNoteUtil.createThreads(accessList, sm.getDataSource(), studyBean);

        if ("csv".equalsIgnoreCase(format)) {
            /*
             * response.setContentLength(
             * downLoader.getListContentLength(allDiscNotes,DownloadDiscrepancyNote.CSV));
             */
            // 3014: this has been changed to only show the parent of the thread; then changed back again!
            int contentLen = downLoader.getThreadListContentLength(discrepancyNoteThreads);
            response.setContentLength(contentLen);

            /*
             * downLoader.downLoadDiscBeans(allDiscNotes,
             * DownloadDiscrepancyNote.CSV,response.getOutputStream(), null);
             */
            downLoader.downLoadThreadedDiscBeans(discrepancyNoteThreads, DownloadDiscrepancyNote.CSV, response, null);
        } else {
            response.setHeader("Pragma", "public");
            /*
             * downLoader.downLoadDiscBeans(allDiscNotes,
             * DownloadDiscrepancyNote.PDF,
             * response.getOutputStream(), studyIdentifier);
             */
            downLoader.downLoadThreadedDiscBeans(discrepancyNoteThreads, DownloadDiscrepancyNote.PDF, response, studyIdentifier);
        }
    }

    private boolean checkNoteAccess(ItemDataDao itemDataDao, String entityType, int itemDataId) {
        if (entityType.equalsIgnoreCase("itemData")) {

            ItemData itemData = itemDataDao.findById(itemDataId);
            if (hasFormAccess(itemData.getEventCrf())) {
                return true;
            }
        } else if (entityType.equalsIgnoreCase("studyEvent"))
            return true;

        return false;
    }

    private List<DiscrepancyNoteBean> removeNoAccessNotes(List<DiscrepancyNoteBean> notes) {
        ItemDataDao itemDataDao = (ItemDataDao) SpringServletAccess.getApplicationContext(context).getBean("itemDataDao");
        List<DiscrepancyNoteBean> accessList = notes.stream().filter(note -> checkNoteAccess(itemDataDao, note.getEntityType(), note.getEntityId()))
                .collect(toList());
        return accessList;

    }

    private Map<String, String> makeDiscrepancyNoteTypesDecoder() {
        Map<String, String> decoder = new HashMap<String, String>();

        ResourceBundle reterm = ResourceBundleProvider.getTermsBundle();
        for (DiscrepancyNoteType type : DiscrepancyNoteType.list) {
            decoder.put(type.getName(), Integer.toString(type.getId()));
        }
        decoder.put(reterm.getString("Query_and_Failed_Validation_Check"), "1,3");

        return decoder;
    }

    private Map<String, String> makeResolutionStatusDecoder() {
        Map<String, String> decoder = new HashMap<String, String>();

        ResourceBundle reterm = ResourceBundleProvider.getTermsBundle();
        for (ResolutionStatus status : ResolutionStatus.list) {
            decoder.put(status.getName(), Integer.toString(status.getId()));
        }
        decoder.put(reterm.getString("New_and_Updated"), "1,2");
        return decoder;
    }

    private String getDateFormat() {
        Locale locale = LocaleResolver.getLocale(request);
        ResourceBundle resformat = ResourceBundleProvider.getFormatBundle(locale);
        return resformat.getString("date_format_string");
    }

    private Map<String, String> getFilters(HttpServletRequest request) {
        Map<String, String> filters = new HashMap<String, String>();
        String ids[] = {"discrepancyNoteBean.threadNumber", "studySubject.label", "siteId", "studySubject.labelExact", "discrepancyNoteBean.createdDate", "discrepancyNoteBean.updatedDate",
                "discrepancyNoteBean.description", "discrepancyNoteBean.user", "discrepancyNoteBean.disType", "discrepancyNoteBean.entityType",
                "discrepancyNoteBean.resolutionStatus", "age", "days",

                "eventName", "crfName", "entityName", "entityValue", "discrepancyNoteBean.description", "discrepancyNoteBean.user"};
        for (String s : ids) {
            String val = request.getParameter(s);
            if (val != null) {
                filters.put(s, val);
            }
        }
        return filters;
    }

    private List<Pair<String, String>> getSortOrder(HttpServletRequest request) {
        String ids[] = {"studySubject.label", "discrepancyNoteBean.createdDate", "days", "age", "discrepancyNoteBean.threadNumber"};

        List<Pair<String, String>> sortOrders = new ArrayList<Pair<String, String>>(4);
        for (String s : ids) {
            /*
             * The HTTP parameters for sorting are prefixed with 'sort'.
             */
            String orders[] = request.getParameterValues("sort." + s);
            if (orders != null) {
                for (String order : orders) {
                    if ("ASC".equals(order) || "DESC".equals(order)) {
                        sortOrders.add(new Pair<String, String>(s, order));
                        break;
                    }
                }
            }
        }
        return sortOrders;
    }

    private ArrayList<DiscrepancyNoteBean> populateRowsWithAttachedData(ArrayList<DiscrepancyNoteBean> noteRows) {
        Locale l = LocaleResolver.getLocale(request);
        resword = ResourceBundleProvider.getWordsBundle(l);
        resformat = ResourceBundleProvider.getFormatBundle(l);
        SimpleDateFormat sdf = new SimpleDateFormat(resformat.getString("date_format_string"), ResourceBundleProvider.getLocale());
        DiscrepancyNoteDAO dndao = new DiscrepancyNoteDAO(sm.getDataSource());
        StudySubjectDAO studySubjectDAO = new StudySubjectDAO(sm.getDataSource());
        StudyEventDAO sedao = new StudyEventDAO(sm.getDataSource());
        CRFVersionDAO cvdao = new CRFVersionDAO(sm.getDataSource());
        CRFDAO cdao = new CRFDAO(sm.getDataSource());
        StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(sm.getDataSource());
        EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());
        ItemDataDAO iddao = new ItemDataDAO(sm.getDataSource());
        ItemDAO idao = new ItemDAO(sm.getDataSource());
        ItemGroupMetadataDAO<String, ArrayList> igmdao = new ItemGroupMetadataDAO<String, ArrayList>(sm.getDataSource());
        ItemGroupDAO<String, ArrayList> igdao = new ItemGroupDAO<String, ArrayList>(sm.getDataSource());

        ArrayList<DiscrepancyNoteBean> allNotes = new ArrayList<DiscrepancyNoteBean>();

        for (int i = 0; i < noteRows.size(); i++) {
            DiscrepancyNoteBean dnb = noteRows.get(i);
            dnb.setCreatedDateString(dnb.getCreatedDate() == null ? "" : sdf.format(dnb.getCreatedDate()));
            if (dnb.getParentDnId() == 0) {
                ArrayList children = dndao.findAllByStudyAndParent(currentStudy, dnb.getId());
                children = children == null ? new ArrayList() : children;
                dnb.setNumChildren(children.size());
                dnb.setChildren(children);
                int lastDnId = dnb.getId();
                int lastChild = 0;

                for (int j = 0; j < children.size(); j++) {
                    DiscrepancyNoteBean child = (DiscrepancyNoteBean) children.get(j);
                    child.setCreatedDateString(child.getCreatedDate() == null ? "" : sdf.format(child.getCreatedDate()));
                    child.setUpdatedDateString(child.getCreatedDate() != null ? sdf.format(child.getCreatedDate()) : "");

                    if (child.getId() > lastDnId) {
                        lastDnId = child.getId();
                        lastChild = j;
                    }
                }
                if (children.size() > 0) {
                    DiscrepancyNoteBean lastdn = (DiscrepancyNoteBean) children.get(lastChild);
                    // dnb.setResStatus(ResolutionStatus.get(lastdn.getResolutionStatusId()));
                    /*
                     * The update date is the date created of the latest child
                     * note
                     */
                    dnb.setUpdatedDate(lastdn.getCreatedDate());
                    dnb.setUpdatedDateString(dnb.getUpdatedDate() != null ? sdf.format(dnb.getUpdatedDate()) : "");
                }
            }

            String entityType = dnb.getEntityType();

            if (dnb.getEntityId() > 0 && !entityType.equals("")) {
                AuditableEntityBean aeb = dndao.findEntity(dnb);
                if (entityType.equalsIgnoreCase("subject")) {
                    // allNotes.add(dnb);
                    SubjectBean sb = (SubjectBean) aeb;
                    StudySubjectBean ssb = studySubjectDAO.findBySubjectIdAndStudy(sb.getId(), currentStudy);
                    dnb.setStudySub(ssb);
                    dnb.setSubjectName(ssb.getLabel());
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
                    // allNotes.add(dnb);
                    StudySubjectBean ssb = (StudySubjectBean) aeb;
                    dnb.setStudySub(ssb);
                    dnb.setSubjectName(ssb.getLabel());
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
                    StudyEventBean se = (StudyEventBean) sedao.findByPK(dnb.getEntityId());
                    StudyEventDefinitionBean sedb = (StudyEventDefinitionBean) seddao.findByPK(se.getStudyEventDefinitionId());

                    EventCRFBean ecb = (EventCRFBean) aeb;
                    CRFVersionBean cvb = (CRFVersionBean) cvdao.findByPK(ecb.getCRFVersionId());
                    CRFBean cb = (CRFBean) cdao.findByPK(cvb.getCrfId());

                    dnb.setStageId(ecb.getStage().getId());
                    dnb.setEntityName(cb.getName() + " (" + cvb.getName() + ")");

                    StudySubjectBean ssub = (StudySubjectBean) studySubjectDAO.findByPK(ecb.getStudySubjectId());
                    dnb.setStudySub(ssub);
                    dnb.setSubjectName(ssub.getLabel());
                    if (se != null) {
                        if (se.getDateStarted() != null)
                            dnb.setEventStart(se.getDateStarted());
                        dnb.setEventName(se.getName());
                    }
                    dnb.setCrfName(cb.getName());

                    String crfStatus = resword.getString(ecb.getStage().getNameRaw());
                    if (crfStatus.equals("Invalid")) {
                        crfStatus = "";
                    } else if (crfStatus.equals("Data Entry Complete")) {
                        crfStatus = "Complete";
                    }
                    dnb.setCrfStatus(crfStatus);

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
                    dnb.setEvent(se);
                    dnb.setStudyEventDefinitionBean(sedb);
                    // }
                } else if (entityType.equalsIgnoreCase("studyEvent")) {
                    // allNotes.add(dnb);
                    StudyEventBean se = (StudyEventBean) sedao.findByPK(dnb.getEntityId());
                    StudyEventDefinitionBean sedb = (StudyEventDefinitionBean) seddao.findByPK(se.getStudyEventDefinitionId());
                    se.setName(sedb.getName());
                    dnb.setEntityName(dnb.getEntityName());
                    StudySubjectBean ssub = (StudySubjectBean) studySubjectDAO.findByPK(se.getStudySubjectId());
                    dnb.setStudySub(ssub);
                    dnb.setEventStart(se.getDateStarted());
                    dnb.setEventName(se.getName());
                    dnb.setSubjectName(ssub.getLabel());
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
                    dnb.setEvent(se);
                    dnb.setStudyEventDefinitionBean(sedb);
                } else if (entityType.equalsIgnoreCase("itemData")) {
                    ItemDataBean idb = (ItemDataBean) iddao.findByPK(dnb.getEntityId());
                    ItemBean ib = (ItemBean) idao.findByPK(idb.getItemId());

                    EventCRFBean ec = (EventCRFBean) ecdao.findByPK(idb.getEventCRFId());

                    CRFVersionBean cvb = (CRFVersionBean) cvdao.findByPK(ec.getCRFVersionId());
                    CRFBean cb = (CRFBean) cdao.findByPK(cvb.getCrfId());

                    ItemGroupMetadataBean itemGroupMetadataBean = (ItemGroupMetadataBean) igmdao.findByItemAndCrfVersion(ib.getId(), cvb.getId());
                    Boolean isRepeatForSure = itemGroupMetadataBean.isRepeatingGroup();
                    if (isRepeatForSure) {
                        ItemGroupBean ig = (ItemGroupBean) igdao.findByPK(itemGroupMetadataBean.getItemGroupId());
                        dnb.setItemDataOrdinal(idb.getOrdinal());
                        dnb.setItemGroupName(ig.getName());
                    }

                    // allNotes.add(dnb);
                    dnb.setStageId(ec.getStage().getId());
                    dnb.setEntityValue(idb.getValue());

                    StudyEventBean se = (StudyEventBean) sedao.findByPK(ec.getStudyEventId());

                    StudyEventDefinitionBean sedb = (StudyEventDefinitionBean) seddao.findByPK(se.getStudyEventDefinitionId());

                    se.setName(sedb.getName());

                    StudySubjectBean ssub = (StudySubjectBean) studySubjectDAO.findByPK(ec.getStudySubjectId());
                    dnb.setStudySub(ssub);
                    dnb.setSubjectName(ssub.getLabel());
                    dnb.setEventStart(se.getDateStarted());
                    dnb.setEventName(se.getName());
                    dnb.setCrfName(cb.getName());

                    dnb.setEventCrfWorkflowStatus(ec.getWorkflowStatus());
                    dnb.setEvent(se);
                    dnb.setStudyEventDefinitionBean(sedb);

                }
            }

            dnb.setStudy((Study) getStudyDao().findByPK(dnb.getStudyId()));
            if (dnb.getParentDnId() == 0 && dnb.getChildren().size() > 0) {
                ArrayList<DiscrepancyNoteBean> children = dnb.getChildren();
                int childrenSize = children.size();
                for (int j = 0; j < childrenSize; j++) {
                    DiscrepancyNoteBean child = children.get(j);
                    child.setSubjectName(dnb.getSubjectName());
                    child.setEventName(dnb.getEventName());
                    child.setCrfName(dnb.getCrfName());
                    child.setCrfStatus(dnb.getCrfStatus());
                    child.setEntityName(dnb.getEntityName());
                    child.setEntityValue(dnb.getEntityValue());
                    child.setStudySub(dnb.getStudySub());
                    child.setStudy(dnb.getStudy());
                }
            }
            allNotes.add(dnb);
        }
        return allNotes;
    }

    @Override
    protected void mayProceed() throws InsufficientPermissionException {
    }



    public ViewStudySubjectService getViewStudySubjectService() {
        return viewStudySubjectService= (ViewStudySubjectService) SpringServletAccess.getApplicationContext(context).getBean("viewStudySubjectService");
    }
    public PermissionService getPermissionService() {
        return permissionService= (PermissionService) SpringServletAccess.getApplicationContext(context).getBean("permissionService");
    }
    public ItemDataDao getItemDataDao() {
        return itemDataDao=(ItemDataDao) SpringServletAccess.getApplicationContext(context).getBean("itemDataDao");
    }

    public CrfDao getCrfDao() {
        return crfDao=(CrfDao) SpringServletAccess.getApplicationContext(context).getBean("crfDao");
    }

    public CrfVersionDao getCrfVersionDao() {
        return crfVersionDao=(CrfVersionDao) SpringServletAccess.getApplicationContext(context).getBean("crfVersionDao");
    }

    public StudyEventDao getStudyEventDao() {
        return studyEventDao=(StudyEventDao) SpringServletAccess.getApplicationContext(context).getBean("studyEventDaoDomain");
    }

    public ItemFormMetadataDao getItemFormMetadataDao() {
        return itemFormMetadataDao=(ItemFormMetadataDao) SpringServletAccess.getApplicationContext(context).getBean("itemFormMetadataDao");
    }

    public EventDefinitionCrfDao getEventDefinitionCrfDao() {
        return eventDefinitionCrfDao=(EventDefinitionCrfDao) SpringServletAccess.getApplicationContext(context).getBean("eventDefinitionCrfDao");
    }
    public EventDefinitionCrfPermissionTagDao getPermissionTagDao() {
        return permissionTagDao=(EventDefinitionCrfPermissionTagDao) SpringServletAccess.getApplicationContext(context).getBean("eventDefinitionCrfPermissionTagDao");
    }

    public ItemDao getItemDao() {
        return itemDao=(ItemDao) SpringServletAccess.getApplicationContext(context).getBean("itemDao");
    }
    public StudyEventDefinitionDao getStudyEventDefinitionDao() {
        return studyEventDefinitionDao= (StudyEventDefinitionDao) SpringServletAccess.getApplicationContext(context).getBean("studyEventDefDaoDomain");
    }

}