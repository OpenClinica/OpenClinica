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
import core.org.akaza.openclinica.dao.admin.CRFDAO;
import core.org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import core.org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import core.org.akaza.openclinica.dao.submit.CRFVersionDAO;
import core.org.akaza.openclinica.dao.submit.EventCRFDAO;
import core.org.akaza.openclinica.dao.submit.ItemDataDAO;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.service.EventCRFService;
import core.org.akaza.openclinica.service.JdbcService;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.view.Page;

import java.util.ArrayList;

/**
 * @author jxu
 * 
 * Processes request of 'restore an event CRF from a event'
 */
public class RestoreEventCRFServlet extends SecureController {
    private StudyEventDAO studyEventDAO;
    private EventCRFDAO eventCRFDAO;
    private EventCRFService eventCRFService;
    private CRFDAO crfDAO;
    private CRFVersionDAO crfVersionDAO;
    private StudyEventDefinitionDAO studyEventDefinitionDAO;
    private EventDefinitionCRFDAO eventDefinitionCRFDAO;
    private ItemDataDAO itemDataDAO;
    private StudySubjectDAO studySubjectDAO;

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
        studyEventDAO = JdbcService.getStudyEventDao(context);
        eventCRFDAO = JdbcService.getEventCrfDao(context);
        crfDAO = JdbcService.getCrfDao(context);
        crfVersionDAO = JdbcService.getCRFVersionDao(context);
        studyEventDefinitionDAO = JdbcService.getStudyEventDefinitionDao(context);
        eventDefinitionCRFDAO = JdbcService.getEventDefinitionCRFDao(context);
        itemDataDAO = JdbcService.getItemDataDao(context);
        studySubjectDAO = JdbcService.getStudySubjectDao(context);
        eventCRFService = (EventCRFService) SpringServletAccess.getApplicationContext(context).getBean("EventCRFService");
        int eventCRFId = fp.getInt("eventCrfId");// eventCRFId
        int studySubId = fp.getInt("studySubId");// studySubjectId
        checkStudyLocked("ViewStudySubject?id" + studySubId, respage.getString("current_study_locked"));

        if (eventCRFId == 0) {
            addPageMessage(respage.getString("please_choose_an_event_CRF_to_restore"));
            request.setAttribute("id", Integer.toString(studySubId));
            forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
        } else {
            EventCRFBean eventCRF = (EventCRFBean) eventCRFDAO.findByPK(eventCRFId);

            StudySubjectBean studySubject = (StudySubjectBean) studySubjectDAO.findByPK(studySubId);
            // Study event cannot be restored if the subject has been removed.
            Status subjectStatus = studySubject.getStatus();
            if ("removed".equalsIgnoreCase(subjectStatus.getName()) || "auto-removed".equalsIgnoreCase(subjectStatus.getName())) {
                addPageMessage(resword.getString("event_CRF") + resterm.getString("could_not_be") + resterm.getString("restored") + "."
                    + respage.getString("study_subject_has_been_deleted"));
                request.setAttribute("id", Integer.toString(studySubId));
                forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
            }
            request.setAttribute("studySub", studySubject);

            int crfVersionId = eventCRF.getCRFVersionId();
            CRFBean cb = crfDAO.findByVersionId(crfVersionId);
            eventCRF.setCrf(cb);

            CRFVersionBean cvb = (CRFVersionBean) crfVersionDAO.findByPK(crfVersionId);
            eventCRF.setCrfVersion(cvb);

            // then get the definition so we can call DisplayEventCRFBean.setFlags
            int studyEventId = eventCRF.getStudyEventId();

            StudyEventBean studyEvent = (StudyEventBean) studyEventDAO.findByPK(studyEventId);

            int studyEventDefinitionId = studyEventDAO.getDefinitionIdFromStudyEventId(studyEventId);

            StudyEventDefinitionBean sed = (StudyEventDefinitionBean) studyEventDefinitionDAO.findByPK(studyEventDefinitionId);
            studyEvent.setStudyEventDefinition(sed);
            request.setAttribute("event", studyEvent);

            Study study = getStudyDao().findByPK(studySubject.getStudyId());
            EventDefinitionCRFBean edc = eventDefinitionCRFDAO.findByStudyEventDefinitionIdAndCRFId(study, studyEventDefinitionId, cb.getId());

            DisplayEventCRFBean dec = new DisplayEventCRFBean();
            dec.setEventCRF(eventCRF);
            dec.setFlags(eventCRF, ub, currentRole, edc.isDoubleEntry());

            // find all item data
            ArrayList itemData = itemDataDAO.findAllByEventCRFId(eventCRF.getId());

            request.setAttribute("items", itemData);

            String action = request.getParameter("action");
            if ("confirm".equalsIgnoreCase(action)) {
                request.setAttribute("displayEventCRF", dec);

                forwardPage(Page.RESTORE_EVENT_CRF);
            } else {
                logger.info("Restoring event CRF - user confirmed.");

                eventCRFService.restoreEventCrf(studySubject, studyEvent, eventCRF, ub);

                request.setAttribute("id", Integer.toString(studySubId));
                forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
            }
        }
    }

}
