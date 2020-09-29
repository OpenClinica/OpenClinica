/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.admin;

import core.org.akaza.openclinica.bean.admin.CRFBean;
import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import core.org.akaza.openclinica.bean.managestudy.StudyEventBean;
import core.org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.bean.submit.DisplayEventCRFBean;
import core.org.akaza.openclinica.bean.submit.EventCRFBean;
import core.org.akaza.openclinica.bean.submit.FormLayoutBean;
import core.org.akaza.openclinica.bean.submit.ItemDataBean;
import core.org.akaza.openclinica.core.LockInfo;
import core.org.akaza.openclinica.dao.hibernate.EventCrfDao;
import core.org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import core.org.akaza.openclinica.dao.submit.FormLayoutDAO;
import core.org.akaza.openclinica.domain.datamap.EventCrf;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.service.EventCRFService;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.view.Page;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;

/**
 * @author jxu
 *
 */
public class DeleteEventCRFServlet extends SecureController {

    public static String STUDY_SUB_ID = "ssId";
    public static String EVENT_CRF_ID = "eventCrfId";

    @Autowired
    FormLayoutDAO formLayoutDAO;
    @Autowired
    EventDefinitionCRFDAO eventDefinitionCRFDAO;
    @Autowired
    private EventCRFService eventCRFService;
    @Autowired
    EventCrfDao eventCrfDao;

    /**
     * 
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        if (!currentRole.getRole().equals(Role.MONITOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.LIST_STUDY_SUBJECTS, resexception.getString("not_admin"), "1");

    }

    @Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);

        int studySubId = fp.getInt(STUDY_SUB_ID, true);
        int eventCRFId = fp.getInt(EVENT_CRF_ID);

        String action = request.getParameter("action");

        request.setAttribute("errorData", null);
        String originatingPage = request.getParameter(ORIGINATING_PAGE);
        request.setAttribute(ORIGINATING_PAGE, originatingPage);


        if (eventCRFId == 0) {
            addPageMessage(respage.getString("please_choose_an_event_CRF_to_delete"));
            request.setAttribute("id", Integer.toString(studySubId));
            forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
        } else {

            EventCRFBean eventCRF = (EventCRFBean) eventCRFDAO.findByPK(eventCRFId);
            final EventCrf ec = eventCrfDao.findById(eventCRFId);

            if (hasFormAccess(ec) != true) {
                forwardPage(Page.NO_ACCESS);
                return;
            }
            StudySubjectBean studySub = (StudySubjectBean) studySubjectDAO.findByPK(studySubId);
            request.setAttribute("studySub", studySub);

            // construct info needed on view event crf page

            int formLayoutId = eventCRF.getFormLayoutId();
            CRFBean cb = crfDAO.findByLayoutId(formLayoutId);
            eventCRF.setCrf(cb);

            FormLayoutBean flb = (FormLayoutBean) formLayoutDAO.findByPK(formLayoutId);
            eventCRF.setFormLayout(flb);

            // then get the definition so we can call
            // DisplayEventCRFBean.setFlags
            int studyEventId = eventCRF.getStudyEventId();

            StudyEventBean event = (StudyEventBean) studyEventDAO.findByPK(studyEventId);

            int studyEventDefinitionId = studyEventDAO.getDefinitionIdFromStudyEventId(studyEventId);
            StudyEventDefinitionBean sed = (StudyEventDefinitionBean) studyEventDefinitionDAO.findByPK(studyEventDefinitionId);
            event.setStudyEventDefinition(sed);
            request.setAttribute("event", event);

            Study study = getStudyDao().findByPK(studySub.getStudyId());
            EventDefinitionCRFBean edc = eventDefinitionCRFDAO.findByStudyEventDefinitionIdAndCRFId(study, studyEventDefinitionId, cb.getId());

            DisplayEventCRFBean dec = new DisplayEventCRFBean();
            dec.setEventCRF(eventCRF);
            dec.setFlags(eventCRF, ub, currentRole, edc.isDoubleEntry());

            // find all item data
            ArrayList<ItemDataBean> itemData = itemDataDAO.findAllByEventCRFId(eventCRF.getId());
            request.setAttribute("items", itemData);
            if (getEventCrfLocker().isLocked(currentPublicStudy.getSchemaName()
                    + eventCRF.getStudyEventId() + eventCRF.getFormLayoutId(), ub.getId(), request.getSession().getId())) {
                LockInfo lockInfo = getEventCrfLocker().getLockOwner(currentPublicStudy.getSchemaName()
                        + eventCRF.getStudyEventId() + eventCRF.getFormLayoutId());
                if (lockInfo != null) {
                    UserAccountBean ubean = (UserAccountBean) userAccountDAO.findByPK(lockInfo.getUserId());
                    String errorData = "This form is currently unavailable for this action.\\n " +
                            "User " + ubean.getName() +" is currently entering data.\\n " +
                            resword.getString("CRF_perform_action") +"\\n";
                    request.setAttribute("errorData", errorData);
                }
                if ("confirm".equalsIgnoreCase(action)) {
                    request.setAttribute("id", Integer.toString(studySubId));
                    forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
                    return;
                } else {
                    request.setAttribute("displayEventCRF", dec);
                    forwardPage(Page.DELETE_EVENT_CRF);
                }
            }
            if ("confirm".equalsIgnoreCase(action)) {

                request.setAttribute("displayEventCRF", dec);
                request.setAttribute("iconInfoShown", true);

                forwardPage(Page.DELETE_EVENT_CRF);
            } else {
                logger.info("Delete/clearing event CRF - user confirmed.");

                eventCRFService.clearEventCrf(studySub, event, eventCRF, itemData, ub);

                String alertMessage = respage.getString("the_event_CRF") + cb.getName() + respage.getString("has_been_deleted_from_the_event")
                        + event.getStudyEventDefinition().getName() + ". " + respage.getString("has_been_deleted_from_the_event_cont");

                addPageMessage(alertMessage);
                request.setAttribute("id", Integer.toString(studySubId));
                forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
            }
        }
    }
}
