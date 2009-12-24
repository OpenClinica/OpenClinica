/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import org.akaza.openclinica.bean.core.ResolutionStatus;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.core.Utils;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.DisplayItemBean;
import org.akaza.openclinica.bean.submit.DisplayItemGroupBean;
import org.akaza.openclinica.bean.submit.DisplayItemWithGroupBean;
import org.akaza.openclinica.bean.submit.DisplaySectionBean;
import org.akaza.openclinica.bean.submit.DisplayTableOfContentsBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.bean.submit.SectionBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.control.form.DiscrepancyValidator;
import org.akaza.openclinica.control.form.FormDiscrepancyNotes;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.submit.AddNewSubjectServlet;
import org.akaza.openclinica.control.submit.DataEntryServlet;
import org.akaza.openclinica.control.submit.SubmitDataServlet;
import org.akaza.openclinica.control.submit.TableOfContentsServlet;
import org.akaza.openclinica.core.SessionManager;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemGroupDAO;
import org.akaza.openclinica.dao.submit.SectionDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.DiscrepancyNoteThread;
import org.akaza.openclinica.service.DiscrepancyNoteUtil;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author jxu <p/> View a CRF version section data entry
 */
public class ViewSectionDataEntryServlet extends DataEntryServlet {

    Locale locale;
    public static String EVENT_CRF_ID = "ecId";
    public static String ENCLOSING_PAGE = "enclosingPage";

    /**
     * Checks whether the user has the correct privilege
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        mayAccess();

        locale = request.getLocale();
        // <
        // resexception=ResourceBundle.getBundle(
        // "org.akaza.openclinica.i18n.exceptions",locale);
        // < respage =
        // ResourceBundle.getBundle("org.akaza.openclinica.i18n.page_messages",
        // locale);
        // < resword =
        // ResourceBundle.getBundle("org.akaza.openclinica.i18n.words",locale);

        if (ub.isSysAdmin()) {
            return;
        }
        if (SubmitDataServlet.mayViewData(ub, currentRole)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + " " + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_director"), "1");

    }

    // BWP 01/08 >>For out of container testing...
    public void setRequest(HttpServletRequest _request) {
        this.request = _request;
    }

    public void setResponse(HttpServletResponse _response) {
        this.response = _response;
    }

    public void setSessionManager(SessionManager manager) {
        this.sm = manager;
    }

    public void setSession(HttpSession session) {
        this.session = session;
    }

    public void setUserAccountBean(UserAccountBean userBean) {
        this.ub = userBean;
    }

    public Map getErrors() {
        return errors;
    }

    public void setStudy(StudyBean studyBean) {
        this.currentStudy = studyBean;
    }

    public void initializeMembers(Locale locale) {
        resadmin = ResourceBundleProvider.getAdminBundle(locale);
        resaudit = ResourceBundleProvider.getAuditEventsBundle(locale);
        resexception = ResourceBundleProvider.getExceptionsBundle(locale);
        resformat = ResourceBundleProvider.getFormatBundle(locale);
        restext = ResourceBundleProvider.getTextsBundle(locale);
        resterm = ResourceBundleProvider.getTermsBundle(locale);
        resword = ResourceBundleProvider.getWordsBundle(locale);
        respage = ResourceBundleProvider.getPageMessagesBundle(locale);
        resworkflow = ResourceBundleProvider.getWorkflowBundle(locale);

        local_df = new SimpleDateFormat(resformat.getString("date_format_string"));
    }

    // BWP 01/08>>
    @Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        if (!fp.getString("exitTo").equals("")) {
            request.setAttribute("exitTo", fp.getString("exitTo"));
        }
        int crfVersionId = fp.getInt("crfVersionId", true);
        int sectionId = fp.getInt("sectionId");
        int eventCRFId = fp.getInt(EVENT_CRF_ID, true);
        int studySubjectId = fp.getInt("studySubjectId", true);
        String action = fp.getString("action");

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
            CRFVersionDAO crfVDao = new CRFVersionDAO(sm.getDataSource());
            CRFVersionBean crvVBean = (CRFVersionBean) crfVDao.findByPK(crfVersionId);
            if (crvVBean != null) {
                crfId = crvVBean.getCrfId();
            }
        }

        // YW >>
        int eventDefinitionCRFId = fp.getInt("eventDefinitionCRFId");
        EventDefinitionCRFDAO eventCrfDao = new EventDefinitionCRFDAO(sm.getDataSource());
        edcb = (EventDefinitionCRFBean) eventCrfDao.findByPK(eventDefinitionCRFId);
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

        EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());
        SectionDAO sdao = new SectionDAO(sm.getDataSource());
        String age = "";
        if (sectionId == 0 && crfVersionId == 0 && eventCRFId == 0) {
            addPageMessage(respage.getString("please_choose_a_CRF_to_view"));
            // forwardPage(Page.SUBMIT_DATA_SERVLET);
            forwardPage(Page.LIST_STUDY_SUBJECTS_SERVLET);
            // >> changed tbh, 06/2009
            return;
        }
        if (studySubjectId > 0) {
            StudySubjectDAO ssdao = new StudySubjectDAO(sm.getDataSource());
            StudySubjectBean sub = (StudySubjectBean) ssdao.findByPK(studySubjectId);
            request.setAttribute("studySubject", sub);
        }

        if (eventCRFId > 0) {
            // for event crf, the input crfVersionId from url =0
            super.ecb = (EventCRFBean) ecdao.findByPK(eventCRFId);

            StudyEventDAO sedao = new StudyEventDAO(sm.getDataSource());
            StudyEventBean event = (StudyEventBean) sedao.findByPK(super.ecb.getStudyEventId());
            // System.out.println("event.getSubjectEventStatus()" +
            // event.getSubjectEventStatus().getName());
            if (event.getSubjectEventStatus().equals(SubjectEventStatus.LOCKED)) {
                request.setAttribute("isLocked", "yes");
                // System.out.println("this event crf is locked");
            } else {
                request.setAttribute("isLocked", "no");
            }

            if (studySubjectId <= 0) {

                studySubjectId = event.getStudySubjectId();
                request.setAttribute("studySubjectId", studySubjectId + "");

            }
            // Get the status/number of item discrepancy notes
            DiscrepancyNoteDAO dndao = new DiscrepancyNoteDAO(sm.getDataSource());
            ArrayList<DiscrepancyNoteBean> allNotes = new ArrayList<DiscrepancyNoteBean>();
            List<DiscrepancyNoteBean> eventCrfNotes = new ArrayList<DiscrepancyNoteBean>();
            List<DiscrepancyNoteThread> noteThreads = new ArrayList<DiscrepancyNoteThread>();

            // if (eventCRFId > 0) {
            // this method finds only parent notes
            allNotes = dndao.findAllTopNotesByEventCRF(eventCRFId);
            // add interviewer.jsp related notes to this Collection
            eventCrfNotes = dndao.findOnlyParentEventCRFDNotesFromEventCRF(super.ecb);
            if (!eventCrfNotes.isEmpty()) {
                allNotes.addAll(eventCrfNotes);
                // make sure a necessary request attribute "hasNameNote" is set
                // properly
                this.setAttributeForInterviewerDNotes(eventCrfNotes);
            }
            // }
            // Create disc note threads out of the various notes
            DiscrepancyNoteUtil dNoteUtil = new DiscrepancyNoteUtil();
            noteThreads = dNoteUtil.createThreadsOfParents(allNotes, sm.getDataSource(), currentStudy, null, -1, true);
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

            DisplayTableOfContentsBean displayBean = TableOfContentsServlet.getDisplayBean(super.ecb, sm.getDataSource(), currentStudy);
            // Make sure that the interviewDate in the eventCRF is properly
            // formatted
            // for viewSectionDataEntry.jsp --> interviewer.jsp
            // int studyEventId = (Integer)request.getAttribute("studyEvent");
            // SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
            //
            Date tmpDate = displayBean.getEventCRF().getDateInterviewed();
            String formattedInterviewerDate;
            try {
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
                addPageMessage(respage.getString("there_are_no_sections_ins_this_CRF"));
                // forwardPage(Page.SUBMIT_DATA_SERVLET);
                forwardPage(Page.LIST_STUDY_SUBJECTS_SERVLET);
                // >> changed tbh, 06/2009
                return;
            }
        } else if (crfVersionId > 0) {// for viewing blank CRF
            DisplayTableOfContentsBean displayBean = ViewTableOfContentServlet.getDisplayBean(sm.getDataSource(), crfVersionId);
            request.setAttribute("toc", displayBean);
            ArrayList sections = displayBean.getSections();

            request.setAttribute("sectionNum", sections.size() + "");
            if (!sections.isEmpty()) {
                if (sectionId == 0) {
                    SectionBean firstSec = (SectionBean) sections.get(0);
                    sectionId = firstSec.getId();
                }
            } else {
                addPageMessage(respage.getString("there_are_no_sections_ins_this_CRF_version"));
                if (eventCRFId == 0) {
                    forwardPage(Page.CRF_LIST_SERVLET);
                } else {
                    // forwardPage(Page.SUBMIT_DATA_SERVLET);
                    forwardPage(Page.LIST_STUDY_SUBJECTS_SERVLET);
                    // >> changed tbh, 06/2009
                }
                return;
            }

        }

        super.sb = (SectionBean) sdao.findByPK(sectionId);
        if (eventCRFId == 0) {
            super.ecb = new EventCRFBean();
            super.ecb.setCRFVersionId(sb.getCRFVersionId());
        } else {
            super.ecb = (EventCRFBean) ecdao.findByPK(eventCRFId);

            // This is the StudySubjectBean
            StudySubjectDAO ssdao = new StudySubjectDAO(sm.getDataSource());
            StudySubjectBean sub = (StudySubjectBean) ssdao.findByPK(super.ecb.getStudySubjectId());
            // This is the SubjectBean
            SubjectDAO subjectDao = new SubjectDAO(sm.getDataSource());
            int subjectId = sub.getSubjectId();
            int studyId = sub.getStudyId();
            SubjectBean subject = (SubjectBean) subjectDao.findByPK(subjectId);
            // BWP 01/08 >> check for a null currentStudy
            // Let us process the age
            if (currentStudy.getStudyParameterConfig().getCollectDob().equals("1")) {
                StudyEventDAO sedao = new StudyEventDAO(sm.getDataSource());
                StudyEventBean se = (StudyEventBean) sedao.findByPK(super.ecb.getStudyEventId());
                StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(sm.getDataSource());
                StudyEventDefinitionBean sed = (StudyEventDefinitionBean) seddao.findByPK(se.getStudyEventDefinitionId());
                se.setStudyEventDefinition(sed);
                request.setAttribute("studyEvent", se);

                // YW 11-16-2007 enrollment-date is used for computing age
                age = Utils.getInstacne().processAge(sub.getEnrollmentDate(), subject.getDateOfBirth());
            }
            // Get the study then the parent study
            StudyDAO studydao = new StudyDAO(sm.getDataSource());
            StudyBean study = (StudyBean) studydao.findByPK(studyId);

            if (study.getParentStudyId() > 0) {
                // this is a site,find parent
                StudyBean parentStudy = (StudyBean) studydao.findByPK(study.getParentStudyId());
                request.setAttribute("studyTitle", parentStudy.getName() + " - " + study.getName());
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
        ItemGroupDAO igdao = new ItemGroupDAO(sm.getDataSource());
        List<ItemGroupBean> itemGroups = igdao.findLegitGroupBySectionId(sectionId);
        if (!itemGroups.isEmpty()) {
            hasItemGroup = true;
            /*
             * newDisplayBean = formUtil.
             * createDisplaySectionBWithFormGroups(sectionId,crfVersionId, sm );
             */
        }

        // if the List of DisplayFormGroups is empty, then the servlet defers to
        // the prior method
        // of generating a DisplaySectionBean for the application

        DisplaySectionBean dsb;
        // want to get displayBean with grouped and ungrouped items
        dsb = super.getDisplayBean(hasItemGroup, false);

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
        List<DisplayItemWithGroupBean> displayItemWithGroups = super.createItemWithGroups(dsb, hasItemGroup, eventDefinitionCRFId);
        dsb.setDisplayItemGroups(displayItemWithGroups);

        super.populateNotesWithDBNoteCounts(discNotes, dsb);

        if ("saveNotes".equalsIgnoreCase(action)) {
            logger.info("33333how many group rows:" + dsb.getDisplayItemGroups().size());

            // let's save notes for the blank items
            dndao = new DiscrepancyNoteDAO(sm.getDataSource());
            discNotes = (FormDiscrepancyNotes) session.getAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME);

            for (int i = 0; i < dsb.getDisplayItemGroups().size(); i++) {
                DisplayItemWithGroupBean diwb = dsb.getDisplayItemGroups().get(i);

                if (diwb.isInGroup()) {
                    List<DisplayItemGroupBean> dgbs = diwb.getItemGroups();
                    logger.info("dgbs size: " + dgbs.size());
                    for (int j = 0; j < dgbs.size(); j++) {
                        DisplayItemGroupBean displayGroup = dgbs.get(j);
                        List<DisplayItemBean> items = displayGroup.getItems();
                        logger.info("item size: " + items.size());
                        for (DisplayItemBean displayItem : items) {
                            String inputName = getGroupItemInputName(displayGroup, j, displayItem);
                            logger.info("inputName:" + inputName);
                            logger.info("item data id:" + displayItem.getData().getId());
                            AddNewSubjectServlet.saveFieldNotes(inputName, discNotes, dndao, displayItem.getData().getId(), "itemData", currentStudy);

                        }
                    }

                } else {
                    DisplayItemBean dib = diwb.getSingleItem();
                    // TODO work on this line

                    String inputName = getInputName(dib);
                    AddNewSubjectServlet.saveFieldNotes(inputName, discNotes, dndao, dib.getData().getId(), "ItemData", currentStudy);

                    ArrayList childItems = dib.getChildren();
                    for (int j = 0; j < childItems.size(); j++) {
                        DisplayItemBean child = (DisplayItemBean) childItems.get(j);
                        inputName = getInputName(child);
                        AddNewSubjectServlet.saveFieldNotes(inputName, discNotes, dndao, dib.getData().getId(), "ItemData", currentStudy);

                    }
                }
            }

            addPageMessage("Discrepancy notes are saved successfully.");
            request.setAttribute("id", studySubjectId + "");
            forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
            // response.sendRedirect(response.encodeRedirectURL(
            // "ViewStudySubject?id=" + studySubjectId));
            return;
        } else {
            request.setAttribute(BEAN_DISPLAY, dsb);
            request.setAttribute(BEAN_ANNOTATIONS, ecb.getAnnotations());
            request.setAttribute("sec", sb);
            request.setAttribute("EventCRFBean", super.ecb);

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
                forwardPage(Page.VIEW_SECTION_DATA_ENTRY_PRINT);
            } else {
                forwardPage(Page.VIEW_SECTION_DATA_ENTRY);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.akaza.openclinica.control.submit.DataEntryServlet#getBlankItemStatus
     * ()
     */
    @Override
    protected Status getBlankItemStatus() {
        return Status.AVAILABLE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.akaza.openclinica.control.submit.DataEntryServlet#getNonBlankItemStatus
     * ()
     */
    @Override
    protected Status getNonBlankItemStatus() {
        return edcb.isDoubleEntry() ? Status.PENDING : Status.UNAVAILABLE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.akaza.openclinica.control.submit.DataEntryServlet#getEventCRFAnnotations
     * ()
     */
    @Override
    protected String getEventCRFAnnotations() {
        return ecb.getAnnotations();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.akaza.openclinica.control.submit.DataEntryServlet#setEventCRFAnnotations
     * (java.lang.String)
     */
    @Override
    protected void setEventCRFAnnotations(String annotations) {
        ecb.setAnnotations(annotations);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.akaza.openclinica.control.submit.DataEntryServlet#getJSPPage()
     */
    @Override
    protected Page getJSPPage() {
        return Page.VIEW_SECTION_DATA_ENTRY;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.akaza.openclinica.control.submit.DataEntryServlet#getServletPage()
     */
    @Override
    protected Page getServletPage() {
        return Page.VIEW_SECTION_DATA_ENTRY_SERVLET;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.akaza.openclinica.control.submit.DataEntryServlet#
     * validateInputOnFirstRound()
     */
    @Override
    protected boolean validateInputOnFirstRound() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.akaza.openclinica.control.submit.DataEntryServlet#validateDisplayItemBean
     * (org.akaza.openclinica.core.form.Validator,
     * org.akaza.openclinica.bean.submit.DisplayItemBean)
     */
    @Override
    protected DisplayItemBean validateDisplayItemBean(DiscrepancyValidator v, DisplayItemBean dib, String inputName) {
        ItemBean ib = dib.getItem();
        org.akaza.openclinica.bean.core.ResponseType rt = dib.getMetadata().getResponseSet().getResponseType();

        // note that this step sets us up both for
        // displaying the data on the form again, in the event of an error
        // and sending the data to the database, in the event of no error
        dib = loadFormValue(dib);

        // types TEL and ED are not supported yet
        if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.TEXT) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.TEXTAREA)
            || rt.equals(org.akaza.openclinica.bean.core.ResponseType.CALCULATION) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.GROUP_CALCULATION)) {
            dib = validateDisplayItemBeanText(v, dib, inputName);
        } else if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.RADIO) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.SELECT)) {
            dib = validateDisplayItemBeanSingleCV(v, dib, inputName);
        } else if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.CHECKBOX) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.SELECTMULTI)) {
            dib = validateDisplayItemBeanMultipleCV(v, dib, inputName);
        }

        return dib;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.akaza.openclinica.control.submit.DataEntryServlet#
     * validateDisplayItemGroupBean()
     */
    @Override
    protected List<DisplayItemGroupBean> validateDisplayItemGroupBean(DiscrepancyValidator v, DisplayItemGroupBean digb, List<DisplayItemGroupBean> digbs,
            List<DisplayItemGroupBean> formGroups) {

        return formGroups;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.akaza.openclinica.control.submit.DataEntryServlet#loadDBValues()
     */
    @Override
    protected boolean shouldLoadDBValues(DisplayItemBean dib) {
        return true;
    }

    /**
     * Current User may access a requested event CRF in the current user's
     * studies
     * 
     * @author ywang 10-18-2007
     */
    public void mayAccess() throws InsufficientPermissionException {
        FormProcessor fp = new FormProcessor(request);
        EventCRFDAO edao = new EventCRFDAO(sm.getDataSource());
        int eventCRFId = fp.getInt("ecId", true);

        if (eventCRFId > 0) {
            if (!entityIncluded(eventCRFId, ub.getName(), edao, sm.getDataSource())) {
                addPageMessage(respage.getString("required_event_CRF_belong"));
                throw new InsufficientPermissionException(Page.MENU, resexception.getString("entity_not_belong_studies"), "1");
            }
        }
    }

    private void setAttributeForInterviewerDNotes(List<DiscrepancyNoteBean> eventCrfNotes) {
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

    private Date formatInterviewDate(Date datetoBeFormatted) {
        Date tmpDate = new Date();
        if (datetoBeFormatted == null) {
            return null;
        }
        String tmpStr = datetoBeFormatted.toString().replaceAll("-", "/");
        try {
            tmpDate = new SimpleDateFormat("MM/dd/yyyy").parse(tmpStr);
        } catch (java.text.ParseException pe) {
            return datetoBeFormatted;
        }
        return tmpDate;
    }

    @Override
    protected boolean shouldRunRules() {
        return false;
    }

    @Override
    protected boolean isAdministrativeEditing() {
        return false;
    }

    @Override
    protected boolean isAdminForcedReasonForChange() {
        return false;
    }
}
