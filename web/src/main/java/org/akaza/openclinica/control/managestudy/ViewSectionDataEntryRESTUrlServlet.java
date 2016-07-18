/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import org.akaza.openclinica.bean.core.ResolutionStatus;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.core.Utils;
import org.akaza.openclinica.bean.managestudy.*;
import org.akaza.openclinica.bean.submit.*;
import org.akaza.openclinica.control.form.FormDiscrepancyNotes;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.submit.AddNewSubjectServlet;
import org.akaza.openclinica.control.submit.TableOfContentsServlet;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.managestudy.*;
import org.akaza.openclinica.dao.submit.*;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.DiscrepancyNoteThread;
import org.akaza.openclinica.service.DiscrepancyNoteUtil;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author jxu
 *         <p/>
 *         View a CRF version section data entry
 */
public class ViewSectionDataEntryRESTUrlServlet extends ViewSectionDataEntryServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(ViewSectionDataEntryServlet.class);

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        FormProcessor fp = new FormProcessor(request);
        StudyBean currentStudy = (StudyBean) request.getSession().getAttribute("study");
        EventCRFBean ecb = (EventCRFBean) request.getAttribute(INPUT_EVENT_CRF);

        SectionBean sb = (SectionBean) request.getAttribute(SECTION_BEAN);
        boolean isSubmitted = false;
        EventDefinitionCRFBean edcb = (EventDefinitionCRFBean) request.getAttribute(EVENT_DEF_CRF_BEAN);
        if (!fp.getString("exitTo").equals("")) {
            request.setAttribute("exitTo",request.getContextPath()+ "/"+fp.getString("exitTo"));

        }
        int crfVersionId = fp.getInt("crfVersionId", true);

        Integer sectionId = (Integer) request.getAttribute("sectionId");
        if(sectionId==null || sectionId == 0)
        {
        	sectionId = new Integer(1);
        }
        int eventCRFId = fp.getInt(EVENT_CRF_ID, true);
        int studySubjectId = fp.getInt("studySubjectId", true);
        String action = fp.getString("action");
        HttpSession session = request.getSession();
        String fromResolvingNotes = fp.getString("fromResolvingNotes", true);
        if (StringUtil.isBlank(fromResolvingNotes)) {
            session.removeAttribute(ViewNotesServlet.WIN_LOCATION);
            session.removeAttribute(ViewNotesServlet.NOTES_TABLE);
        }

        request.setAttribute("studySubjectId", studySubjectId + "");// Added for
        // Mantis
        // Issue
        // 2268
        request.setAttribute("crfListPage", fp.getString("crfListPage"));// Added
        // for
        // Mantis
        // Issue
        // 2268
        request.setAttribute("eventId", fp.getString("eventId"));// Added for
        // Mantis
        // Issue
        // 2268
        // YW <<
        int sedId = fp.getInt("sedId");
        request.setAttribute("sedId", sedId + "");
        int crfId = fp.getInt("crfId");
        // BWP>> ... try to get crfId from crfVersionId
        if (crfId == 0 && crfVersionId > 0) {
            CRFVersionDAO crfVDao = new CRFVersionDAO(getDataSource());
            CRFVersionBean crvVBean = (CRFVersionBean) crfVDao.findByPK(crfVersionId);
            if (crvVBean != null) {
                crfId = crvVBean.getCrfId();
            }
        }

        // YW >>
        // int eventDefinitionCRFId = fp.getInt("eventDefinitionCRFId");
        Integer eventDefinitionCRFId = (Integer) (request.getAttribute("eventDefinitionCRFId"));

        EventDefinitionCRFDAO eventCrfDao = new EventDefinitionCRFDAO(getDataSource());
        edcb = (EventDefinitionCRFBean) eventCrfDao.findByPK(eventDefinitionCRFId);
        if (eventCRFId == 0 && edcb.getStudyId() != currentStudy.getParentStudyId() && edcb.getStudyId() != currentStudy.getId()) {
            addPageMessage(respage.getString("no_have_correct_privilege_current_study") + " " + respage.getString("change_study_contact_sysadmin"), request);
            throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_director"), "1");
        }

        if (crfId == 0 && eventDefinitionCRFId > 0) {
            // try to get crfId from eventDefinitionCRFId
            // edcb = (EventDefinitionCRFBean)
            // eventCrfDao.findByPK(eventDefinitionCRFId);
            if (edcb != null) {
                crfId = edcb.getCrfId();
            }
        }
        request.setAttribute("crfId", crfId + "");
        request.setAttribute("eventDefinitionCRFId", eventDefinitionCRFId + "");
        String printVersion = fp.getString("print");
        // BWP>> this has to be removed for CRFs that do not display an
        // interviewdate
        // for a particular event
        session.removeAttribute("presetValues");

        EventCRFDAO ecdao = new EventCRFDAO(getDataSource());
        SectionDAO sdao = new SectionDAO(getDataSource());
        String age = "";
        if (sectionId == 0 && crfVersionId == 0 && eventCRFId == 0) {
            addPageMessage(respage.getString("please_choose_a_CRF_to_view"), request);
            // forwardPage(Page.SUBMIT_DATA_SERVLET);
            forwardPage(Page.LIST_STUDY_SUBJECTS_SERVLET, request, response);
            // >> changed tbh, 06/2009
            return;
        }
        if (studySubjectId > 0) {
            StudySubjectDAO ssdao = new StudySubjectDAO(getDataSource());
            StudySubjectBean sub = (StudySubjectBean) ssdao.findByPK(studySubjectId);
            request.setAttribute("studySubject", sub);
        }

        if (eventCRFId > 0) {
            // for event crf, the input crfVersionId from url =0
            ecb = (EventCRFBean) ecdao.findByPK(eventCRFId);

            StudyEventDAO sedao = new StudyEventDAO(getDataSource());
            StudyEventBean event = (StudyEventBean) sedao.findByPK(ecb.getStudyEventId());
            // System.out.println("event.getSubjectEventStatus()" +
            // event.getSubjectEventStatus().getName());
            if (event.getSubjectEventStatus().equals(SubjectEventStatus.LOCKED)) {
                request.setAttribute("isLocked", "yes");
                // System.out.println("this event crf is locked");
            }
            // @pgawade 28-Aug-2012 Reverted the change no. 2 in
            // https://issuetracker.openclinica.com/view.php?id=12343#c56722
            else {
                request.setAttribute("isLocked", "no");
            }

            if (studySubjectId <= 0) {

                studySubjectId = event.getStudySubjectId();
                request.setAttribute("studySubjectId", studySubjectId + "");

            }
            // Get the status/number of item discrepancy notes
            DiscrepancyNoteDAO dndao = new DiscrepancyNoteDAO(getDataSource());
            ArrayList<DiscrepancyNoteBean> allNotes = new ArrayList<DiscrepancyNoteBean>();
            List<DiscrepancyNoteBean> eventCrfNotes = new ArrayList<DiscrepancyNoteBean>();
            List<DiscrepancyNoteThread> noteThreads = new ArrayList<DiscrepancyNoteThread>();

            // if (eventCRFId > 0) {
            // this method finds only parent notes
            allNotes = dndao.findAllTopNotesByEventCRF(eventCRFId);
            // add interviewer.jsp related notes to this Collection
            eventCrfNotes = dndao.findOnlyParentEventCRFDNotesFromEventCRF(ecb);
            if (!eventCrfNotes.isEmpty()) {
                allNotes.addAll(eventCrfNotes);
                // make sure a necessary request attribute "hasNameNote" is set
                // properly
                this.setAttributeForInterviewerDNotes(eventCrfNotes, request);
            }
            // }
            // Create disc note threads out of the various notes
            DiscrepancyNoteUtil dNoteUtil = new DiscrepancyNoteUtil();
            noteThreads = dNoteUtil.createThreadsOfParents(allNotes, getDataSource(), currentStudy, null, -1, true);
            // variables that provide values for the CRF discrepancy note header
            int updatedNum = 0;
            int openNum = 0;
            int closedNum = 0;
            int resolvedNum = 0;
            int notAppNum = 0;
            DiscrepancyNoteBean tempBean;
            for (DiscrepancyNoteThread dnThread : noteThreads) {
                /*
                 * 3014: do not count parent beans, only the last child disc
                 * note of the thread.
                 */
                tempBean = dnThread.getLinkedNoteList().getLast();
                if (tempBean != null) {
                    if (ResolutionStatus.UPDATED.equals(tempBean.getResStatus())) {
                        updatedNum++;
                    } else if (ResolutionStatus.OPEN.equals(tempBean.getResStatus())) {
                        openNum++;
                    } else if (ResolutionStatus.CLOSED.equals(tempBean.getResStatus())) {
                        // if (dn.getParentDnId() > 0){
                        closedNum++;
                        // }
                    } else if (ResolutionStatus.RESOLVED.equals(tempBean.getResStatus())) {
                        // if (dn.getParentDnId() > 0){
                        resolvedNum++;
                        // }
                    } else if (ResolutionStatus.NOT_APPLICABLE.equals(tempBean.getResStatus())) {
                        notAppNum++;
                    }
                }

            }
            request.setAttribute("updatedNum", updatedNum + "");
            request.setAttribute("openNum", openNum + "");
            request.setAttribute("closedNum", closedNum + "");
            request.setAttribute("resolvedNum", resolvedNum + "");
            request.setAttribute("notAppNum", notAppNum + "");

            DisplayTableOfContentsBean displayBean = TableOfContentsServlet.getDisplayBean(ecb, getDataSource(), currentStudy);
            // Make sure that the interviewDate in the eventCRF is properly
            // formatted
            // for viewSectionDataEntry.jsp --> interviewer.jsp
            // int studyEventId = (Integer)request.getAttribute("studyEvent");
            // SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
            //
            Date tmpDate = displayBean.getEventCRF().getDateInterviewed();
            String formattedInterviewerDate;
            try {
                DateFormat local_df = new SimpleDateFormat(resformat.getString("date_format_string"), ResourceBundleProvider.getLocale());
                formattedInterviewerDate = local_df.format(tmpDate);
            } catch (Exception e) {
                formattedInterviewerDate = "";
            }
            HashMap presetVals = (HashMap) session.getAttribute("presetValues");
            if (presetVals == null) {
                presetVals = new HashMap();
                session.setAttribute("presetValues", presetVals);
            }
            presetVals.put("interviewDate", formattedInterviewerDate);
            request.setAttribute("toc", displayBean);

            ArrayList sections = displayBean.getSections();

            request.setAttribute("sectionNum", sections.size() + "");
            if (!sections.isEmpty()) {
                if (sectionId == 0) {
                    SectionBean firstSec = (SectionBean) sections.get(0);
                    sectionId = firstSec.getId();
                }
            } else {
                addPageMessage(respage.getString("there_are_no_sections_ins_this_CRF"), request);
                // forwardPage(Page.SUBMIT_DATA_SERVLET);
                forwardPage(Page.LIST_STUDY_SUBJECTS_SERVLET, request, response);
                // >> changed tbh, 06/2009
                return;
            }
        } else if (crfVersionId > 0) {// for viewing blank CRF
            DisplayTableOfContentsBean displayBean = ViewTableOfContentServlet.getDisplayBean(getDataSource(), crfVersionId);
            request.setAttribute("toc", displayBean);
            ArrayList sections = displayBean.getSections();

            request.setAttribute("sectionNum", sections.size() + "");
            if (!sections.isEmpty()) {
                if (sectionId == 0) {
                    SectionBean firstSec = (SectionBean) sections.get(0);
                    sectionId = firstSec.getId();
                }
            } else {
                addPageMessage(respage.getString("there_are_no_sections_ins_this_CRF_version"), request);
                if (eventCRFId == 0) {
                    forwardPage(Page.CRF_LIST_SERVLET, request, response);
                } else {
                    // forwardPage(Page.SUBMIT_DATA_SERVLET);
                    forwardPage(Page.LIST_STUDY_SUBJECTS_SERVLET, request, response);
                    // >> changed tbh, 06/2009
                }
                return;
            }

        }

        sb = (SectionBean) sdao.findByPK(sectionId);
        if (eventCRFId == 0) {
            ecb = new EventCRFBean();
            ecb.setCRFVersionId(sb.getCRFVersionId());
            if (currentStudy.getParentStudyId() > 0) {
                // this is a site,find parent
                StudyDAO studydao = new StudyDAO(getDataSource());
                StudyBean parentStudy = (StudyBean) studydao.findByPK(currentStudy.getParentStudyId());
                request.setAttribute("studyTitle", parentStudy.getName());
                request.setAttribute("siteTitle", currentStudy.getName());
            } else {
                request.setAttribute("studyTitle", currentStudy.getName());
            }

        } else {
            ecb = (EventCRFBean) ecdao.findByPK(eventCRFId);

            request.setAttribute(INPUT_EVENT_CRF, ecb);
            request.setAttribute(SECTION_BEAN, sb);

            // This is the StudySubjectBean
            StudySubjectDAO ssdao = new StudySubjectDAO(getDataSource());
            StudySubjectBean sub = (StudySubjectBean) ssdao.findByPK(ecb.getStudySubjectId());
            // This is the SubjectBean
            SubjectDAO subjectDao = new SubjectDAO(getDataSource());
            int subjectId = sub.getSubjectId();
            int studyId = sub.getStudyId();
            SubjectBean subject = (SubjectBean) subjectDao.findByPK(subjectId);
            // BWP 01/08 >> check for a null currentStudy
            // Let us process the age
            if (currentStudy.getStudyParameterConfig().getCollectDob().equals("1")) {
                StudyEventDAO sedao = new StudyEventDAO(getDataSource());
                StudyEventBean se = (StudyEventBean) sedao.findByPK(ecb.getStudyEventId());
                StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(getDataSource());
                StudyEventDefinitionBean sed = (StudyEventDefinitionBean) seddao.findByPK(se.getStudyEventDefinitionId());
                se.setStudyEventDefinition(sed);
                request.setAttribute("studyEvent", se);

                // YW 11-16-2007 enrollment-date is used for computing age
                age = Utils.getInstacne().processAge(sub.getEnrollmentDate(), subject.getDateOfBirth());
            }
            // Get the study then the parent study
            StudyDAO studydao = new StudyDAO(getDataSource());
            StudyBean study = (StudyBean) studydao.findByPK(studyId);

            if (study.getParentStudyId() > 0) {
                // this is a site,find parent
                StudyBean parentStudy = (StudyBean) studydao.findByPK(study.getParentStudyId());
                request.setAttribute("studyTitle", parentStudy.getName());
                request.setAttribute("siteTitle", study.getName());
            } else {
                request.setAttribute("studyTitle", study.getName());
            }

            request.setAttribute("studySubject", sub);
            request.setAttribute("subject", subject);
            request.setAttribute("age", age);

        }
        // FormBeanUtil formUtil = new FormBeanUtil();
        // DisplaySectionBean newDisplayBean = new DisplaySectionBean();

        boolean hasItemGroup = false;
        // we will look into db to see if any repeating items for this CRF
        // section
        ItemGroupDAO igdao = new ItemGroupDAO(getDataSource());
        List<ItemGroupBean> itemGroups = igdao.findLegitGroupBySectionId(sectionId);
        if (!itemGroups.isEmpty()) {
            hasItemGroup = true;

        }

        // if the List of DisplayFormGroups is empty, then the servlet defers to
        // the prior method
        // of generating a DisplaySectionBean for the application

        DisplaySectionBean dsb;
        // want to get displayBean with grouped and ungrouped items
        request.setAttribute(EVENT_DEF_CRF_BEAN, edcb);
        request.setAttribute(INPUT_EVENT_CRF, ecb);
        request.setAttribute(SECTION_BEAN, sb);
        dsb = super.getDisplayBean(hasItemGroup, false, request, isSubmitted);

        FormDiscrepancyNotes discNotes = (FormDiscrepancyNotes) session.getAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME);
        if (discNotes == null) {
            discNotes = new FormDiscrepancyNotes();
            session.setAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME, discNotes);
        }

        /*
         * if (hasItemGroup) { //
         * dsb.setDisplayItemGroups(newDisplayBean.getDisplayItemGroups());
         * request.setAttribute("new_table", true); }
         */
        // If the Horizontal type table will be used, then set the
        // DisplaySectionBean's
        // DisplayFormGroups List to the ones we have just generated
        // @pgawade 30-May-2012 Fix for issue 13963 - added an extra parameter
        // 'isSubmitted' to method createItemWithGroups
        List<DisplayItemWithGroupBean> displayItemWithGroups = super.createItemWithGroups(dsb, hasItemGroup, eventDefinitionCRFId, request, isSubmitted);
        dsb.setDisplayItemGroups(displayItemWithGroups);

        super.populateNotesWithDBNoteCounts(discNotes, dsb, request);

        if (fp.getString("fromViewNotes") != null && "1".equals(fp.getString("fromViewNotes"))) {
            request.setAttribute("fromViewNotes", fp.getString("fromViewNotes"));
        } else {
            session.removeAttribute("viewNotesURL");
        }

        if ("saveNotes".equalsIgnoreCase(action)) {
            LOGGER.info("33333how many group rows:" + dsb.getDisplayItemGroups().size());

            // let's save notes for the blank items
            DiscrepancyNoteDAO dndao = new DiscrepancyNoteDAO(getDataSource());
            discNotes = (FormDiscrepancyNotes) session.getAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME);

            for (int i = 0; i < dsb.getDisplayItemGroups().size(); i++) {
                DisplayItemWithGroupBean diwb = dsb.getDisplayItemGroups().get(i);

                if (diwb.isInGroup()) {
                    List<DisplayItemGroupBean> dgbs = diwb.getItemGroups();
                    LOGGER.info("dgbs size: " + dgbs.size());
                    for (int j = 0; j < dgbs.size(); j++) {
                        DisplayItemGroupBean displayGroup = dgbs.get(j);
                        List<DisplayItemBean> items = displayGroup.getItems();
                        LOGGER.info("item size: " + items.size());
                        for (DisplayItemBean displayItem : items) {
                            String inputName = getGroupItemInputName(displayGroup, j, displayItem);
                            LOGGER.info("inputName:" + inputName);
                            LOGGER.info("item data id:" + displayItem.getData().getId());
                            AddNewSubjectServlet.saveFieldNotes(inputName, discNotes, dndao, displayItem.getData().getId(), "itemData", currentStudy);

                        }
                    }

                } else {
                    DisplayItemBean dib = diwb.getSingleItem();
                    // TODO work on this line

                    String inputName = getInputName(dib);
                    AddNewSubjectServlet.saveFieldNotes(inputName, discNotes, dndao, dib.getData().getId(), DiscrepancyNoteBean.ITEM_DATA, currentStudy);

                    ArrayList childItems = dib.getChildren();
                    for (int j = 0; j < childItems.size(); j++) {
                        DisplayItemBean child = (DisplayItemBean) childItems.get(j);
                        inputName = getInputName(child);
                        AddNewSubjectServlet.saveFieldNotes(inputName, discNotes, dndao, dib.getData().getId(), DiscrepancyNoteBean.ITEM_DATA, currentStudy);

                    }
                }
            }

            addPageMessage("Discrepancy notes are saved successfully.", request);
            request.setAttribute("id", studySubjectId + "");
            forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET, request, response);
            // response.sendRedirect(response.encodeRedirectURL(
            // "ViewStudySubject?id=" + studySubjectId));
            return;
        } else {
            request.setAttribute(BEAN_DISPLAY, dsb);
            request.setAttribute(BEAN_ANNOTATIONS, ecb.getAnnotations());
            request.setAttribute("sec", sb);
            request.setAttribute("EventCRFBean", ecb);

            int tabNum = 1;
            if ("".equalsIgnoreCase(fp.getString("tabId"))) {
                tabNum = 1;
            } else {
                tabNum = fp.getInt("tabId");
            }
            request.setAttribute("tabId", new Integer(tabNum).toString());

            // 2808: Signal interviewer.jsp that the containing page is
            // viewSectionData,
            // for the purpose of suppressing discrepancy note icons for the
            // interview date and name fields
            request.setAttribute(ENCLOSING_PAGE, "viewSectionData");

            if ("yes".equalsIgnoreCase(printVersion)) {
                forwardPage(Page.VIEW_SECTION_DATA_ENTRY_PRINT, request, response);
            } else {
                forwardPage(Page.VIEW_SECTION_DATA_ENTRY, request, response);
            }
        }
    }

    /**
     * Current User may access a requested event CRF in the current user's
     * studies
     * 
     * @author ywang 10-18-2007
     * @param request
     *            TODO
     */

    private void setAttributeForInterviewerDNotes(List<DiscrepancyNoteBean> eventCrfNotes, HttpServletRequest request) {
        for (DiscrepancyNoteBean dnBean : eventCrfNotes) {

            if (INTERVIEWER_NAME.equalsIgnoreCase(dnBean.getColumn())) {
                request.setAttribute("hasNameNote", "yes");
                request.setAttribute(INTERVIEWER_NAME_NOTE, dnBean);

            }
            if (DATE_INTERVIEWED.equalsIgnoreCase(dnBean.getColumn())) {
                request.setAttribute("hasDateNote", "yes");
                request.setAttribute(INTERVIEWER_DATE_NOTE, dnBean);
            }
        }
    }

}
