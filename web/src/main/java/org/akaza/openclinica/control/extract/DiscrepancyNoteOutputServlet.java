package org.akaza.openclinica.control.extract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.AuditableEntityBean;
import org.akaza.openclinica.bean.extract.DownloadDiscrepancyNote;
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
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.dao.managestudy.ListNotesFilter;
import org.akaza.openclinica.dao.managestudy.ListNotesSort;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.DiscrepancyNoteThread;
import org.akaza.openclinica.service.DiscrepancyNoteUtil;
import org.akaza.openclinica.web.InsufficientPermissionException;

/**
 * A servlet that sends via HTTP a file containing Discrepancy-Note related data.
 * @author Bruce W. Perry
 * @see ChooseDownloadFormat
 * @see org.akaza.openclinica.bean.extract.DownloadDiscrepancyNote
 */
public class DiscrepancyNoteOutputServlet extends SecureController {
    // These are the headers that must appear in the HTTP response, when sending a
    // file back to the user
    public static String CONTENT_DISPOSITION_HEADER = "Content-Disposition";
    public static String CONTENT_DISPOSITION_VALUE = "attachment; filename=";

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
        /*int resolutionStatus = 0;
        try {
            resolutionStatus = Integer.parseInt(request.getParameter("resolutionStatus"));
        } catch(NumberFormatException nfe){
            //Show all DN's
            resolutionStatus=-1;
        }*/
        // possibly for a later implementation: int definitionId = fp.getInt("defId");
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
        StudyBean studyBean = (StudyBean) session.getAttribute("study");

        Set<Integer> resolutionStatusIds = (HashSet) session.getAttribute("resolutionStatus");

        // It will also change any resolution status IDs among parents of children that have a different
        // id value (last boolean parameter; 'true' to perform the latter task)
        // In this case we want to include all the discrepancy notes, despite the res status or
        // type filtering, because we don't want to filter out parents, thus leaving out a child note
        // that might match the desired res status
        ListNotesFilter listNotesFilter = new ListNotesFilter();
        StudySubjectBean studySubject = new StudySubjectBean();
        if(subjectId>0) {
            studySubject = new StudySubjectDAO(sm.getDataSource()).findBySubjectIdAndStudy(subjectId, studyBean);
            listNotesFilter.addFilter("studySubject.label", studySubject.getLabel());
        }
        if(discNoteType >= 1 && discNoteType <= 5) {
            listNotesFilter.addFilter("discrepancyNoteBean.disType", discNoteType);
        }
        if(resolutionStatusIds != null && !resolutionStatusIds.isEmpty()) {
            for(Integer statusId : resolutionStatusIds) {
                listNotesFilter.addFilter("discrepancyNoteBean.resolutionStatus", statusId);
            }
        }
        ListNotesSort listNotesSort = new ListNotesSort();//getListSubjectSort(limit);
        listNotesSort.addSort("","");
        ArrayList<DiscrepancyNoteBean> allDiscNotes = new DiscrepancyNoteDAO(sm.getDataSource()).getNotesWithFilterAndSort(studyBean, listNotesFilter, listNotesSort);
        allDiscNotes = populateRowsWithAttachedData(allDiscNotes);
        // Now we have to package all the discrepancy notes in DiscrepancyNoteThread objects
        // Do the filtering for type or status here
        DiscrepancyNoteUtil discNoteUtil = new DiscrepancyNoteUtil();

        List<DiscrepancyNoteThread> discrepancyNoteThreads =
            discNoteUtil.createThreads(allDiscNotes, sm.getDataSource(), studyBean, resolutionStatusIds, discNoteType);

        if ("csv".equalsIgnoreCase(format)) {
            /*response.setContentLength(
              downLoader.getListContentLength(allDiscNotes,DownloadDiscrepancyNote.CSV));*/
            //3014: this has been changed to only show the parent of the thread; then changed back again!
            int contentLen = downLoader.getThreadListContentLength(discrepancyNoteThreads);
            response.setContentLength(contentLen);

            /*downLoader.downLoadDiscBeans(allDiscNotes,
              DownloadDiscrepancyNote.CSV,response.getOutputStream(), null);*/
            downLoader.downLoadThreadedDiscBeans(discrepancyNoteThreads, DownloadDiscrepancyNote.CSV, response.getOutputStream(), null);
        } else {
            response.setHeader("Pragma", "public");
            /*downLoader.downLoadDiscBeans(allDiscNotes,
              DownloadDiscrepancyNote.PDF,
              response.getOutputStream(), studyIdentifier);*/
            downLoader.downLoadThreadedDiscBeans(discrepancyNoteThreads, DownloadDiscrepancyNote.PDF, response.getOutputStream(), studyIdentifier);
        }

    }

    private ArrayList<DiscrepancyNoteBean> populateRowsWithAttachedData(ArrayList<DiscrepancyNoteBean> noteRows) {
        Locale l = request.getLocale();
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
        ArrayList<DiscrepancyNoteBean> allNotes = new ArrayList<DiscrepancyNoteBean>();

        for (int i = 0; i < noteRows.size(); i++) {
            DiscrepancyNoteBean dnb = noteRows.get(i);
            dnb.setCreatedDateString(dnb.getCreatedDate()==null?"":sdf.format(dnb.getCreatedDate()));
            if (dnb.getParentDnId() == 0) {
                ArrayList children = dndao.findAllByStudyAndParent(currentStudy, dnb.getId());
                children = children == null? new ArrayList():children;
                dnb.setNumChildren(children.size());
                dnb.setChildren(children);
                int lastDnId = dnb.getId();
                int lastChild = 0;

                for (int j = 0; j < children.size(); j++) {
                    DiscrepancyNoteBean child = (DiscrepancyNoteBean) children.get(j);
                    child.setCreatedDateString(child.getCreatedDate()==null?"":sdf.format(child.getCreatedDate()));
                    child.setUpdatedDateString(child.getCreatedDate()!=null?sdf.format(child.getCreatedDate()):"");

                    if (child.getId() > lastDnId) {
                        lastDnId = child.getId();
                        lastChild = j;
                    }
                }
                if(children.size()>0) {
                    DiscrepancyNoteBean lastdn = (DiscrepancyNoteBean) children.get(lastChild);
                    //dnb.setResStatus(ResolutionStatus.get(lastdn.getResolutionStatusId()));
                    /*
                     * The update date is the date created of the latest child
                     * note
                     */
                    dnb.setUpdatedDate(lastdn.getCreatedDate());
                    dnb.setUpdatedDateString(dnb.getUpdatedDate()!=null?sdf.format(dnb.getUpdatedDate()):"");
                }
            }

            String entityType = dnb.getEntityType();

            if (dnb.getEntityId() > 0 && !entityType.equals("")) {
                AuditableEntityBean aeb = dndao.findEntity(dnb);
                dnb.setEntityName(aeb.getName());
                if (entityType.equalsIgnoreCase("subject")) {
                    allNotes.add(dnb);
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
                    allNotes.add(dnb);
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

                    EventCRFBean ecb = (EventCRFBean) aeb;
                    CRFVersionBean cvb = (CRFVersionBean) cvdao.findByPK(ecb.getCRFVersionId());
                    CRFBean cb = (CRFBean) cdao.findByPK(cvb.getCrfId());

                    dnb.setStageId(ecb.getStage().getId());
                    dnb.setEntityName(cb.getName() + " (" + cvb.getName() + ")");

                    StudySubjectBean ssub = (StudySubjectBean) studySubjectDAO.findByPK(ecb.getStudySubjectId());
                    dnb.setStudySub(ssub);
                    dnb.setSubjectName(ssub.getLabel());
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
                    StudyEventBean se = (StudyEventBean) sedao.findByPK(dnb.getEntityId());
                    StudyEventDefinitionBean sedb = (StudyEventDefinitionBean) seddao.findByPK(se.getStudyEventDefinitionId());
                    se.setName(sedb.getName());
                    dnb.setEntityName(sedb.getName());
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
                } else if (entityType.equalsIgnoreCase("itemData")) {
                    ItemDataBean idb = (ItemDataBean) iddao.findByPK(dnb.getEntityId());
                    ItemBean ib = (ItemBean) idao.findByPK(idb.getItemId());

                    EventCRFBean ec = (EventCRFBean) ecdao.findByPK(idb.getEventCRFId());

                    CRFVersionBean cvb = (CRFVersionBean) cvdao.findByPK(ec.getCRFVersionId());
                    CRFBean cb = (CRFBean) cdao.findByPK(cvb.getCrfId());

                    allNotes.add(dnb);
                    dnb.setStageId(ec.getStage().getId());
                    dnb.setEntityName(ib.getName());
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
                }
            }

            if(dnb.getParentDnId()==0 && dnb.getChildren().size()>0) {
                ArrayList<DiscrepancyNoteBean> children = dnb.getChildren();
                int childrenSize = children.size();
                for (int j = 0; j < childrenSize; j++) {
                    DiscrepancyNoteBean child = children.get(j);
                    child.setSubjectName(dnb.getSubjectName());
                    child.setEventName(dnb.getEventName());
                    child.setCrfName(dnb.getCrfName());
                    child.setEntityName(dnb.getEntityName());
                    child.setEntityValue(dnb.getEntityValue());
                }
            }
        }
        return allNotes;
    }

    @Override
    protected void mayProceed() throws InsufficientPermissionException {

    }
}
