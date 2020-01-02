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
import core.org.akaza.openclinica.bean.submit.CRFVersionBean;
import core.org.akaza.openclinica.bean.submit.EventCRFBean;
import core.org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import core.org.akaza.openclinica.core.EmailEngine;
import core.org.akaza.openclinica.core.form.StringUtil;
import core.org.akaza.openclinica.dao.admin.CRFDAO;
import core.org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import core.org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import core.org.akaza.openclinica.dao.submit.CRFVersionDAO;
import core.org.akaza.openclinica.dao.submit.EventCRFDAO;
import core.org.akaza.openclinica.dao.submit.ItemDataDAO;
import core.org.akaza.openclinica.service.managestudy.EventDefinitionCrfTagService;
import org.akaza.openclinica.view.Page;
import core.org.akaza.openclinica.web.InsufficientPermissionException;

import java.util.ArrayList;
import java.util.Date;

/**
 * @author jxu
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class RemoveEventDefinitionServlet extends SecureController {
    EventDefinitionCrfTagService eventDefinitionCrfTagService = null;

    /**
     *
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        checkStudyLocked(Page.LIST_DEFINITION_SERVLET, respage.getString("current_study_locked"));
        if (ub.isSysAdmin()) {
            return;
        }

        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.LIST_DEFINITION_SERVLET, resexception.getString("not_study_director"), "1");

    }

    @Override
    public void processRequest() throws Exception {
        String idString = request.getParameter("id");

        int defId = Integer.valueOf(idString.trim()).intValue();
        StudyEventDefinitionDAO sdao = new StudyEventDefinitionDAO(sm.getDataSource());
        StudyEventDefinitionBean sed = (StudyEventDefinitionBean) sdao.findByPK(defId);

//        checkRoleByUserAndStudy(ub.getName(), sed.getStudyId(), 0);
        if (currentStudy.getStudyId() != sed.getStudyId()) {
            addPageMessage(respage.getString("no_have_correct_privilege_current_study")
                    + " " + respage.getString("change_active_study_or_contact"));
            forwardPage(Page.MENU_SERVLET);
            return;
        }
        
        
        // find all CRFs
        EventDefinitionCRFDAO edao = new EventDefinitionCRFDAO(sm.getDataSource());
        ArrayList eventDefinitionCRFs = (ArrayList) edao.findAllByDefinition(defId);

        CRFVersionDAO cvdao = new CRFVersionDAO(sm.getDataSource());
        CRFDAO cdao = new CRFDAO(sm.getDataSource());
        for (int i = 0; i < eventDefinitionCRFs.size(); i++) {
            EventDefinitionCRFBean edc = (EventDefinitionCRFBean) eventDefinitionCRFs.get(i);
            ArrayList versions = (ArrayList) cvdao.findAllByCRF(edc.getCrfId());
            edc.setVersions(versions);
            CRFBean crf = (CRFBean) cdao.findByPK(edc.getCrfId());
            edc.setCrfName(crf.getName());
            CRFVersionBean defaultVersion = (CRFVersionBean) cvdao.findByPK(edc.getDefaultVersionId());
            edc.setDefaultVersionName(defaultVersion.getName());

            CRFBean cBean = (CRFBean) cdao.findByPK(edc.getCrfId());                
            String crfPath=sed.getOid()+"."+cBean.getOid();
            edc.setOffline(getEventDefinitionCrfTagService().getEventDefnCrfOfflineStatus(2,crfPath,true));

        }

        // finds all events
        StudyEventDAO sedao = new StudyEventDAO(sm.getDataSource());
        ArrayList events = (ArrayList) sedao.findAllByDefinition(sed.getId());

        String action = request.getParameter("action");
        if (StringUtil.isBlank(idString)) {
            addPageMessage(respage.getString("please_choose_a_SED_to_remove"));
            forwardPage(Page.LIST_DEFINITION_SERVLET);
        } else {
            if ("confirm".equalsIgnoreCase(action)) {
                if (!sed.getStatus().equals(Status.AVAILABLE)) {
                    addPageMessage(respage.getString("this_SED_is_not_available_for_this_study")
                        + respage.getString("please_contact_sysadmin_for_more_information"));
                    forwardPage(Page.LIST_DEFINITION_SERVLET);
                    return;
                }
                StudyParameterValueDAO spvdao = new StudyParameterValueDAO(sm.getDataSource());    
                String participateFormStatus = spvdao.findByHandleAndStudy(sed.getStudyId(), "participantPortal").getValue();
                if (participateFormStatus.equals("enabled")) baseUrl();
            
                request.setAttribute("participateFormStatus",participateFormStatus );

                
                
                request.setAttribute("definitionToRemove", sed);
                request.setAttribute("eventDefinitionCRFs", eventDefinitionCRFs);
                request.setAttribute("events", events);
                forwardPage(Page.REMOVE_DEFINITION);
            } else {
                logger.info("submit to remove the definition");
                // remove definition
                sed.setStatus(Status.DELETED);
                sed.setUpdater(ub);
                sed.setUpdatedDate(new Date());
                sdao.update(sed);

                // remove all crfs
                for (int j = 0; j < eventDefinitionCRFs.size(); j++) {
                    EventDefinitionCRFBean edc = (EventDefinitionCRFBean) eventDefinitionCRFs.get(j);
                    if (!edc.getStatus().equals(Status.DELETED)) {
                        edc.setStatus(Status.AUTO_DELETED);
                        edc.setUpdater(ub);
                        edc.setUpdatedDate(new Date());
                        edao.update(edc);
                    }
                }
                // remove all events

                EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());

                for (int j = 0; j < events.size(); j++) {
                    StudyEventBean event = (StudyEventBean) events.get(j);
                    if (!event.getStatus().equals(Status.DELETED)) {
                        event.setStatus(Status.AUTO_DELETED);
                        event.setUpdater(ub);
                        event.setUpdatedDate(new Date());
                        sedao.update(event);

                        ArrayList eventCRFs = ecdao.findAllByStudyEvent(event);
                        // remove all the item data
                        ItemDataDAO iddao = new ItemDataDAO(sm.getDataSource());
                        for (int k = 0; k < eventCRFs.size(); k++) {
                            EventCRFBean eventCRF = (EventCRFBean) eventCRFs.get(k);
                            if (!eventCRF.getStatus().equals(Status.DELETED)) {
                                eventCRF.setStatus(Status.AUTO_DELETED);
                                eventCRF.setUpdater(ub);
                                eventCRF.setUpdatedDate(new Date());
                                ecdao.update(eventCRF);

                                ArrayList itemDatas = iddao.findAllByEventCRFId(eventCRF.getId());
                                for (int a = 0; a < itemDatas.size(); a++) {
                                    ItemDataBean item = (ItemDataBean) itemDatas.get(a);
                                    if (!item.getStatus().equals(Status.DELETED)) {
                                        item.setStatus(Status.AUTO_DELETED);
                                        item.setUpdater(ub);
                                        item.setUpdatedDate(new Date());
                                        iddao.update(item);
                                    }
                                }
                            }
                        }
                    }
                }
                String emailBody =
                    respage.getString("the_SED") + sed.getName() + " " + respage.getString("has_been_removed_from_the_study") + currentStudy.getName() + ".";

                addPageMessage(emailBody);
//                sendEmail(emailBody);
                forwardPage(Page.LIST_DEFINITION_SERVLET);
            }

        }

    }

    /**
     * Send email to director and administrator
     *
     * @param request
     * @param response
     */
    private void sendEmail(String emailBody) throws Exception {

        logger.info("Sending email...");
        // to study director
        boolean messageSent = sendEmail(ub.getEmail().trim(), respage.getString("remove_SED"), emailBody, false);
        // to admin
        if (messageSent) {
            sendEmail(EmailEngine.getAdminEmail(), respage.getString("remove_SED"), emailBody, false);
        }
        logger.info("Sending email done..");
    }
    
    public EventDefinitionCrfTagService getEventDefinitionCrfTagService() {
        eventDefinitionCrfTagService=
         this.eventDefinitionCrfTagService != null ? eventDefinitionCrfTagService : (EventDefinitionCrfTagService) SpringServletAccess.getApplicationContext(context).getBean("eventDefinitionCrfTagService");

         return eventDefinitionCrfTagService;
     }

}
