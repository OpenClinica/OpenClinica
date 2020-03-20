/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import core.org.akaza.openclinica.bean.admin.CRFBean;
import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.bean.core.Status;
import core.org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import core.org.akaza.openclinica.bean.managestudy.StudyEventBean;
import core.org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.bean.submit.CRFVersionBean;
import core.org.akaza.openclinica.bean.submit.DisplayEventCRFBean;
import core.org.akaza.openclinica.bean.submit.EventCRFBean;
import core.org.akaza.openclinica.bean.submit.ItemDataBean;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import core.org.akaza.openclinica.core.EmailEngine;
import core.org.akaza.openclinica.dao.admin.CRFDAO;
import core.org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import core.org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import core.org.akaza.openclinica.dao.submit.CRFVersionDAO;
import core.org.akaza.openclinica.dao.submit.EventCRFDAO;
import core.org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.view.Page;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;

/**
 * @author jxu
 * 
 * Processes request of 'restore an event CRF from a event'
 */
public class RestoreEventCRFServlet extends SecureController {

    /**
     * 
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }

        if (!currentRole.getRole().equals(Role.MONITOR) ){
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_study_director"), "1");

    }

    @Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        int eventCRFId = fp.getInt("eventCrfId");// eventCRFId
        int studySubId = fp.getInt("studySubId");// studySubjectId
        checkStudyLocked("ViewStudySubject?id" + studySubId, respage.getString("current_study_locked"));
        StudyEventDAO sedao = new StudyEventDAO(sm.getDataSource());
        StudySubjectDAO subdao = new StudySubjectDAO(sm.getDataSource());
        EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());

        if (eventCRFId == 0) {
            addPageMessage(respage.getString("please_choose_an_event_CRF_to_restore"));
            request.setAttribute("id", new Integer(studySubId).toString());
            forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
        } else {
            EventCRFBean eventCRF = (EventCRFBean) ecdao.findByPK(eventCRFId);

            StudySubjectBean studySub = (StudySubjectBean) subdao.findByPK(studySubId);
            // YW 11-07-2007, an event CRF could not be restored if its study
            // subject has been removed
            Status s = studySub.getStatus();
            if ("removed".equalsIgnoreCase(s.getName()) || "auto-removed".equalsIgnoreCase(s.getName())) {
                addPageMessage(resword.getString("event_CRF") + resterm.getString("could_not_be") + resterm.getString("restored") + "."
                    + respage.getString("study_subject_has_been_deleted"));
                request.setAttribute("id", new Integer(studySubId).toString());
                forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
            }
            // YW
            request.setAttribute("studySub", studySub);

            // construct info needed on view event crf page
            CRFDAO cdao = new CRFDAO(sm.getDataSource());
            CRFVersionDAO cvdao = new CRFVersionDAO(sm.getDataSource());

            int crfVersionId = eventCRF.getCRFVersionId();
            CRFBean cb = cdao.findByVersionId(crfVersionId);
            eventCRF.setCrf(cb);

            CRFVersionBean cvb = (CRFVersionBean) cvdao.findByPK(crfVersionId);
            eventCRF.setCrfVersion(cvb);

            // then get the definition so we can call
            // DisplayEventCRFBean.setFlags
            int studyEventId = eventCRF.getStudyEventId();

            StudyEventBean event = (StudyEventBean) sedao.findByPK(studyEventId);

            int studyEventDefinitionId = sedao.getDefinitionIdFromStudyEventId(studyEventId);
            StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(sm.getDataSource());
            StudyEventDefinitionBean sed = (StudyEventDefinitionBean) seddao.findByPK(studyEventDefinitionId);
            event.setStudyEventDefinition(sed);
            request.setAttribute("event", event);

            EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(sm.getDataSource());

            Study study = (Study) getStudyDao().findByPK(studySub.getStudyId());
            EventDefinitionCRFBean edc = edcdao.findByStudyEventDefinitionIdAndCRFId(study, studyEventDefinitionId, cb.getId());

            DisplayEventCRFBean dec = new DisplayEventCRFBean();
            dec.setEventCRF(eventCRF);
            dec.setFlags(eventCRF, ub, currentRole, edc.isDoubleEntry());

            // find all item data
            ItemDataDAO iddao = new ItemDataDAO(sm.getDataSource());

            ArrayList itemData = iddao.findAllByEventCRFId(eventCRF.getId());

            request.setAttribute("items", itemData);

            String action = request.getParameter("action");
            if ("confirm".equalsIgnoreCase(action)) {
                request.setAttribute("displayEventCRF", dec);

                forwardPage(Page.RESTORE_EVENT_CRF);
            } else {
                logger.info("submit to restore the event CRF from study");

                eventCRF.setRemoved(Boolean.FALSE);
                eventCRF.setUpdater(ub);
                eventCRF.setUpdatedDate(new Date());
                ecdao.update(eventCRF);

                // restore all the item data
                for (int a = 0; a < itemData.size(); a++) {
                    ItemDataBean item = (ItemDataBean) itemData.get(a);
                    if (item.getStatus().equals(Status.AUTO_DELETED)) {
                        item.setStatus(Status.AVAILABLE);
                        item.setUpdater(ub);
                        item.setUpdatedDate(new Date());
                        iddao.update(item);
                    }
                }
                /* OC-8797
                    Do not send email notification when data is removed
                    String emailBody =
                        respage.getString("the_event_CRF") + cb.getName() + " " + respage.getString("has_been_restored_to_the_event") + " "
                            + event.getStudyEventDefinition().getName() + ".";

                    addPageMessage(emailBody);
                    sendEmail(emailBody);
                */
                request.setAttribute("id", new Integer(studySubId).toString());
                forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
            }
        }
    }

    /**
     * Send email to director and administrator
     *
     */
    private void sendEmail(String emailBody) throws Exception {

        logger.info("Sending email...");
        sendEmail(ub.getEmail().trim(), respage.getString("restore_event_CRF_to_event"), emailBody, false);
        // to admin
        sendEmail(EmailEngine.getAdminEmail(), respage.getString("restore_event_CRF_to_event"), emailBody, false);
        logger.info("Sending email done..");
    }

}
