/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 *
 * Created on Sep 22, 2005
 */
package org.akaza.openclinica.control.managestudy;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.DiscrepancyNoteType;
import org.akaza.openclinica.bean.core.ResolutionStatus;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.FormLayoutBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.submit.CreateDiscrepancyNoteServlet;
import org.akaza.openclinica.control.submit.EnketoFormServlet;
import org.akaza.openclinica.control.submit.EnterDataForStudyEventServlet;
import org.akaza.openclinica.control.submit.TableOfContentsServlet;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.FormLayoutDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.service.DiscrepancyNoteUtil;
import org.akaza.openclinica.service.crfdata.EnketoUrlService;
import org.akaza.openclinica.service.crfdata.xform.PFormCacheSubjectContextEntry;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InconsistentStateException;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.pform.PFormCache;

/**
 * @author ssachs
 *
 *         TODO To change the template for this generated type comment go to Window -
 *         Preferences - Java - Code Style - Code Templates
 */
public class ResolveDiscrepancyServlet extends SecureController {

    private static final String INPUT_NOTE_ID = "noteId";
    private static final String CAN_ADMIN_EDIT = "canAdminEdit";
    private static final String EVENT_CRF_ID = "ecId";
    private static final String STUDY_SUB_ID = "studySubjectId";

    private static final String RESOLVING_NOTE = "resolving_note";
    private static final String RETURN_FROM_PROCESS_REQUEST = "returnFromProcess";
    private static final String FLAVOR = "-query";

    public Page getPageForForwarding(DiscrepancyNoteBean note, boolean isCompleted) {
        String entityType = note.getEntityType().toLowerCase();
        request.setAttribute("fromResolvingNotes", "yes");

        if ("subject".equalsIgnoreCase(entityType)) {
            if (ub.isSysAdmin() || ub.isTechAdmin()) {
                return Page.UPDATE_SUBJECT_SERVLET;
            } else {
                return Page.VIEW_STUDY_SUBJECT_SERVLET;
            }
        } else if ("studysub".equalsIgnoreCase(entityType)) {
            if (ub.isSysAdmin() || ub.isTechAdmin()) {
                return Page.UPDATE_STUDY_SUBJECT_SERVLET;
            } else {
                return Page.VIEW_STUDY_SUBJECT_SERVLET;
            }
        } else if ("studyevent".equalsIgnoreCase(entityType)) {
            if (ub.isSysAdmin() || ub.isTechAdmin()) {
                return Page.UPDATE_STUDY_EVENT_SERVLET;
            } else {
                return Page.ENTER_DATA_FOR_STUDY_EVENT_SERVLET;
            }
        } else if ("itemdata".equalsIgnoreCase(entityType) || "eventcrf".equalsIgnoreCase(entityType)) {
            if (currentRole.getRole().equals(Role.MONITOR) || !isCompleted) {
                return Page.ENKETO_FORM_SERVLET;
            } else {
                return Page.ENKETO_FORM_SERVLET;
            }
        }
        return null;
    }

    public boolean prepareRequestForResolution(HttpServletRequest request, DataSource ds, StudyBean currentStudy, DiscrepancyNoteBean note, boolean isCompleted)
            throws Exception {
        String entityType = note.getEntityType().toLowerCase();
        int id = note.getEntityId();
        if ("subject".equalsIgnoreCase(entityType)) {
            StudySubjectDAO ssdao = new StudySubjectDAO(ds);
            StudySubjectBean ssb = ssdao.findBySubjectIdAndStudy(id, currentStudy);

            request.setAttribute("action", "show");
            request.setAttribute("id", String.valueOf(note.getEntityId()));
            request.setAttribute("studySubId", String.valueOf(ssb.getId()));
        } else if ("studysub".equalsIgnoreCase(entityType)) {
            request.setAttribute("action", "show");
            request.setAttribute("id", String.valueOf(note.getEntityId()));
        } else if ("eventcrf".equalsIgnoreCase(entityType)) {
            request.setAttribute("editInterview", "1");

            EventCRFDAO ecdao = new EventCRFDAO(ds);
            EventCRFBean ecb = (EventCRFBean) ecdao.findByPK(id);
            request.setAttribute(TableOfContentsServlet.INPUT_EVENT_CRF_BEAN, ecb);
            // If the request is passed along to ViewSectionDataEntryServlet,
            // that code needs
            // an event crf id; the (ecb.getId()+"") is necessary because
            // FormProcessor throws
            // a ClassCastException without the casting to a String
            request.setAttribute(ViewSectionDataEntryServlet.EVENT_CRF_ID, ecb.getId() + "");
        } else if ("studyevent".equalsIgnoreCase(entityType)) {
            StudyEventDAO sedao = new StudyEventDAO(ds);
            StudyEventBean seb = (StudyEventBean) sedao.findByPK(id);
            request.setAttribute(EnterDataForStudyEventServlet.INPUT_EVENT_ID, String.valueOf(id));
            request.setAttribute(UpdateStudyEventServlet.EVENT_ID, String.valueOf(id));
            request.setAttribute(UpdateStudyEventServlet.STUDY_SUBJECT_ID, String.valueOf(seb.getStudySubjectId()));
        }

        // this is for item data
        else if ("itemdata".equalsIgnoreCase(entityType)) {
            ItemDataDAO iddao = new ItemDataDAO(ds);
            ItemDataBean idb = (ItemDataBean) iddao.findByPK(id);

            EventCRFDAO ecdao = new EventCRFDAO(ds);
            EventCRFBean ecb = (EventCRFBean) ecdao.findByPK(idb.getEventCRFId());

            FormLayoutDAO fldao = new FormLayoutDAO(ds);
            FormLayoutBean formLayout = (FormLayoutBean) fldao.findByPK(ecb.getFormLayoutId());

            StudyEventDAO sedao = new StudyEventDAO(ds);

            StudySubjectDAO ssdao = new StudySubjectDAO(sm.getDataSource());
            StudySubjectBean ssb = (StudySubjectBean) ssdao.findByPK(ecb.getStudySubjectId());

            ItemFormMetadataDAO ifmdao = new ItemFormMetadataDAO(ds);
            ItemFormMetadataBean ifmb = ifmdao.findByItemIdAndCRFVersionId(idb.getItemId(), ecb.getFormLayoutId());

            EnketoUrlService enketoUrlService = (EnketoUrlService) SpringServletAccess.getApplicationContext(context).getBean("enketoUrlService");
            StudyEventBean seb = (StudyEventBean) sedao.findByPK(ecb.getStudyEventId());

            // Cache the subject context for use during xform submission
            PFormCache cache = PFormCache.getInstance(context);
            PFormCacheSubjectContextEntry subjectContext = new PFormCacheSubjectContextEntry();
            subjectContext.setStudySubjectOid(ssb.getOid());
            subjectContext.setStudyEventDefinitionId(seb.getStudyEventDefinitionId());
            subjectContext.setOrdinal(seb.getSampleOrdinal());
            subjectContext.setFormLayoutOid(formLayout.getOid());
            subjectContext.setUserAccountId(ub.getId());
            String contextHash = cache.putSubjectContext(subjectContext);

            String formUrl = null;
            if (ecb.getId() > 0) {
                formUrl = enketoUrlService.getEditUrl(contextHash, subjectContext, currentStudy.getOid(), null, null, FLAVOR);
            } else {
                formUrl = enketoUrlService.getInitialDataEntryUrl(contextHash, subjectContext, currentStudy.getOid(), FLAVOR);
            }

            request.setAttribute(EnketoFormServlet.FORM_URL, formUrl);
        }

        return true;

    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.control.core.SecureController#processRequest()
     */
    @Override
    protected void processRequest() throws Exception {

        FormProcessor fp = new FormProcessor(request);
        int noteId = fp.getInt(INPUT_NOTE_ID);
        String module = (String) session.getAttribute("module");

        StudySubjectDAO studySubjectDAO = new StudySubjectDAO(sm.getDataSource());

        DiscrepancyNoteDAO dndao = new DiscrepancyNoteDAO(sm.getDataSource());
        dndao.setFetchMapping(true);

        // check that the note exists
        DiscrepancyNoteBean discrepancyNoteBean = (DiscrepancyNoteBean) dndao.findByPK(noteId);

        if (!discrepancyNoteBean.isActive()) {
            throw new InconsistentStateException(Page.MANAGE_STUDY_SERVLET, resexception.getString("you_are_trying_resolve_discrepancy_not_exist"));
        }

        // check that the note has not already been closed
        ArrayList children = dndao.findAllByParent(discrepancyNoteBean);
        discrepancyNoteBean.setChildren(children);
        // This logic has been reverted, issue-7459
        // if (parentNoteIsClosed(discrepancyNoteBean)) {
        // throw new InconsistentStateException(Page.VIEW_DISCREPANCY_NOTES_IN_STUDY_SERVLET, respage
        // .getString("the_discrepancy_choose_has_been_closed_resolved_create_new"));
        // }

        // all clear, send the user to the resolved screen
        String entityType = discrepancyNoteBean.getEntityType().toLowerCase();
        discrepancyNoteBean.setResStatus(ResolutionStatus.get(discrepancyNoteBean.getResolutionStatusId()));
        discrepancyNoteBean.setDisType(DiscrepancyNoteType.get(discrepancyNoteBean.getDiscrepancyNoteTypeId()));
        // BWP 03/17/2009 3166: if it's not an ItemData type note, redirect
        // Monitors to View Subject or
        // View Study Events <<
        if (currentRole.getRole().equals(Role.MONITOR) && !"itemdata".equalsIgnoreCase(entityType) && !"eventcrf".equalsIgnoreCase(entityType)) {
            redirectMonitor(module, discrepancyNoteBean);
            return;
        }
        // >>
        // If Study is Frozen or Locked
        if (currentStudy.getStatus().isFrozen() && !"itemdata".equalsIgnoreCase(entityType) && !"eventcrf".equalsIgnoreCase(entityType)) {
            redirectMonitor(module, discrepancyNoteBean);
            return;
        }

        boolean toView = false;
        boolean isCompleted = false;
        if ("itemdata".equalsIgnoreCase(entityType)) {
            ItemDataDAO iddao = new ItemDataDAO(sm.getDataSource());
            ItemDataBean idb = (ItemDataBean) iddao.findByPK(discrepancyNoteBean.getEntityId());

            EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());

            EventCRFBean ecb = (EventCRFBean) ecdao.findByPK(idb.getEventCRFId());
            StudySubjectBean studySubjectBean = (StudySubjectBean) studySubjectDAO.findByPK(ecb.getStudySubjectId());

            discrepancyNoteBean.setSubjectId(studySubjectBean.getId());
            discrepancyNoteBean.setItemId(idb.getItemId());

            if (ecb.getStatus().equals(Status.UNAVAILABLE)) {
                isCompleted = true;
            }

            toView = true;// we want to go to view note page if the note is
            // for item data
        }
        // logger.info("set up pop up url: " + createNoteURL);
        // System.out.println("set up pop up url: " + createNoteURL);
        boolean goNext = prepareRequestForResolution(request, sm.getDataSource(), currentStudy, discrepancyNoteBean, isCompleted);

        Page p = getPageForForwarding(discrepancyNoteBean, isCompleted);

        // logger.info("found page for forwarding: " + p.getFileName());
        if (p == null) {
            throw new InconsistentStateException(Page.VIEW_DISCREPANCY_NOTES_IN_STUDY_SERVLET,
                    resexception.getString("the_discrepancy_note_triying_resolve_has_invalid_type"));
        } else {
            if (p.getFileName().contains("?")) {
                if (!p.getFileName().contains("fromViewNotes=1")) {
                    p.setFileName(p.getFileName() + "&fromViewNotes=1");
                }
            } else {
                p.setFileName(p.getFileName() + "?fromViewNotes=1");
            }
            String createNoteURL = CreateDiscrepancyNoteServlet.getAddChildURL(discrepancyNoteBean, ResolutionStatus.CLOSED, true);
            setPopUpURL(createNoteURL);
        }

        if (!goNext) {
            setPopUpURL("");
            addPageMessage(respage.getString("you_may_not_perform_admin_edit_on_CRF_not_completed_by_user"));
            p = Page.VIEW_DISCREPANCY_NOTES_IN_STUDY_SERVLET;

        }

        forwardPage(p);
    }

    /**
     * Determines if a discrepancy note is closed or not. The note is closed if
     * it has status closed, or any of its children have closed status.
     *
     * @param note
     *            The discrepancy note. The children should already be set.
     * @return <code>true</code> if the note is closed, <code>false</code>
     *         otherwise.
     */
    public static boolean noteIsClosed(DiscrepancyNoteBean note) {
        if (note.getResolutionStatusId() == ResolutionStatus.CLOSED.getId()) {
            return true;
        }

        ArrayList children = note.getChildren();
        for (int i = 0; i < children.size(); i++) {
            DiscrepancyNoteBean child = (DiscrepancyNoteBean) children.get(i);
            if (child.getResolutionStatusId() == ResolutionStatus.CLOSED.getId()) {
                return true;
            }
        }

        return false;
    }

    public static boolean parentNoteIsClosed(DiscrepancyNoteBean parentNote) {
        if (parentNote.getResolutionStatusId() == ResolutionStatus.CLOSED.getId()) {
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.control.core.SecureController#mayProceed()
     */
    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        String module = (String) session.getAttribute("module");
        /*
         * BWP: This caused a problem with page refreshing (the subjectId was
         * lost); so I had to comment it out if(subjectId != null){
         * session.removeAttribute("subjectId"); }
         */
        if (module != null) {
            session.removeAttribute("module");
        }

        // BWP 11/03/2008 3029: redirect monitor user to ViewStudySubject if
        // they click "add note to thread" link>>
        // if (currentRole.getRole().equals(Role.MONITOR)) {
        // addPageMessage(respage.getString("no_have_permission_to_resolve_discrepancy"));
        //
        // RequestDispatcher dispatcher =
        // request.getRequestDispatcher("/ViewStudySubject?id=" + subjectId +
        // "&module=" + module);
        // try {
        // dispatcher.forward(request, response);
        // } catch (ServletException e) {
        // e.printStackTrace();
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
        // return;
        // }
        // tbh 02/2009: now removed, to allow for the JsonQuery workflow and allow a
        // Monitor to Resolve a JsonQuery.
        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)
                || currentRole.getRole().equals(Role.INVESTIGATOR) || currentRole.getRole().equals(Role.RESEARCHASSISTANT)
                || currentRole.getRole().equals(Role.RESEARCHASSISTANT2) || currentRole.getRole().equals(Role.MONITOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_permission_to_resolve_discrepancy") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_study_director_or_study_coordinator"), "1");
    }

    /**
     * Redirect the request to another page if the user is a Monitor type and
     * the discrepancy note is a type other than item data or event crf.
     *
     * @param module
     *            A String like "managestudy" or "admin"
     * @param discrepancyNoteBean
     */
    private void redirectMonitor(String module, DiscrepancyNoteBean discrepancyNoteBean) {

        if (discrepancyNoteBean != null) {

            String createNoteURL = "";
            // This String will determine whether the type is other than
            // itemdata.
            String entityType = discrepancyNoteBean.getEntityType().toLowerCase();
            // The id of the subject, study subject, or study event
            int entityId = discrepancyNoteBean.getEntityId();
            RequestDispatcher dispatcher = null;
            DiscrepancyNoteUtil discNoteUtil = new DiscrepancyNoteUtil();

            if (entityType != null && !"".equalsIgnoreCase(entityType) && !"itemdata".equalsIgnoreCase(entityType)
                    && !"eventcrf".equalsIgnoreCase(entityType)) {
                // redirect to View Study Subject
                // addPageMessage(resword.getString("monitors_do_not_have_permission_to_resolve_discrepancy_notes"));
                if ("studySub".equalsIgnoreCase(entityType)) {
                    dispatcher = request.getRequestDispatcher("/ViewStudySubject?id=" + entityId + "&module=" + module);
                    discrepancyNoteBean.setSubjectId(entityId);
                } else if ("subject".equalsIgnoreCase(entityType)) {

                    int studySubId = discNoteUtil.getStudySubjectIdForDiscNote(discrepancyNoteBean, sm.getDataSource(), currentStudy.getId());

                    dispatcher = request.getRequestDispatcher("/ViewStudySubject?id=" + studySubId + "&module=" + module);
                    discrepancyNoteBean.setSubjectId(studySubId);
                } else if ("studyevent".equalsIgnoreCase(entityType)) {
                    dispatcher = request.getRequestDispatcher("/EnterDataForStudyEvent?eventId=" + entityId);

                }

                // This code creates the URL for a popup window, which the
                // processing Servlet will initiate.
                // 'true' parameter means that ViewDiscrepancyNote is the
                // handling Servlet.
                createNoteURL = CreateDiscrepancyNoteServlet.getAddChildURL(discrepancyNoteBean, ResolutionStatus.CLOSED, true);
                request.setAttribute(POP_UP_URL, createNoteURL);

                try {
                    if (dispatcher != null) {
                        dispatcher.forward(request, response);
                    }
                } catch (ServletException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
