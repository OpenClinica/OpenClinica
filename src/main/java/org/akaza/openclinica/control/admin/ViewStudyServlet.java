/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.admin;

import core.org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.submit.SubmitDataServlet;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.login.UserAccountDAO;
import core.org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import core.org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import core.org.akaza.openclinica.dao.service.StudyConfigService;
import core.org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import core.org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import core.org.akaza.openclinica.service.pmanage.RandomizationRegistrar;
import core.org.akaza.openclinica.service.pmanage.SeRandomizationDTO;
import org.akaza.openclinica.view.Page;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * @author jxu
 *
 * Processes the reuqest of 'view study details'
 */
public class ViewStudyServlet extends SecureController {

    /**
     * Checks whether the user has the correct privilege
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }

        if (SubmitDataServlet.mayViewData(ub, currentRole)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_admin"), "1");

    }

    @Override
    public void processRequest() throws Exception {

        FormProcessor fp = new FormProcessor(request);
        int studyId = fp.getInt("id");
        Study study = (Study) getStudyDao().findByPK(studyId);

        if (studyId == 0) {
            addPageMessage(respage.getString("please_choose_a_study_to_view"));
            forwardPage(Page.ERROR);
        } else {
            if (currentStudy.getStudyId() != studyId && currentStudy.getStudy().getStudyId() != studyId) {
                checkRoleByUserAndStudy(ub, study);
            }

            String viewFullRecords = fp.getString("viewFull");

            StudyParameterValueDAO spvdao = new StudyParameterValueDAO(sm.getDataSource());
            String randomizationStatusInOC = spvdao.findByHandleAndStudy(study.getStudyId(), "randomization").getValue();
            String participantStatusInOC = spvdao.findByHandleAndStudy(study.getStudyId(), "participantPortal").getValue();
            if(participantStatusInOC=="") participantStatusInOC="disabled";
            if(randomizationStatusInOC=="") randomizationStatusInOC="disabled";

            RandomizationRegistrar randomizationRegistrar = new RandomizationRegistrar();
            SeRandomizationDTO seRandomizationDTO = randomizationRegistrar.getCachedRandomizationDTOObject(study.getOc_oid(), false);

            if (seRandomizationDTO!=null && seRandomizationDTO.getStatus().equalsIgnoreCase("ACTIVE") && randomizationStatusInOC.equalsIgnoreCase("enabled")){
                study.setRandomization("enabled");
            } else {
                study.setRandomization("disabled");
            };

             ParticipantPortalRegistrar  participantPortalRegistrar = new ParticipantPortalRegistrar();
             String pStatus = participantPortalRegistrar.getCachedRegistrationStatus(study.getOc_oid(), session);
             study.setParticipantPortal("enabled");

            request.setAttribute("studyToView", study);
            if ("yes".equalsIgnoreCase(viewFullRecords)) {
                UserAccountDAO udao = new UserAccountDAO(sm.getDataSource());
                StudySubjectDAO ssdao = new StudySubjectDAO(sm.getDataSource());
                ArrayList sites = new ArrayList();
                ArrayList userRoles = new ArrayList();
                ArrayList subjects = new ArrayList();
                if (this.currentStudy.isSite() && this.currentRole.getRole().getId() > 3) {
                    sites.add(this.currentStudy);
                    request.setAttribute("requestSchema", "public");
                    userRoles = udao.findAllUsersByStudy(currentPublicStudy.getStudyId());
                    request.setAttribute("requestSchema", currentPublicStudy.getSchemaName());
                    subjects = ssdao.findAllByStudy(currentStudy);
                } else {
                    sites = (ArrayList) getStudyDao().findAllByParent(studyId);
                    Study publicStudy = getStudyDao().findPublicStudy(study.getOc_oid());
                    request.setAttribute("requestSchema", "public");
                    userRoles = udao.findAllUsersByStudy(publicStudy.getStudyId());
                    request.setAttribute("requestSchema", publicStudy.getSchemaName());
                    subjects = ssdao.findAllByStudy(study);
                }
              // find all subjects in the study, include ones in sites
                StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(sm.getDataSource(), getStudyDao());
                EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(sm.getDataSource());

                // find all events in the study, include ones in sites
                ArrayList definitions = seddao.findAllByStudy(study);

                for (int i = 0; i < definitions.size(); i++) {
                    StudyEventDefinitionBean def = (StudyEventDefinitionBean) definitions.get(i);
                    ArrayList crfs = (ArrayList) edcdao.findAllActiveParentsByEventDefinitionId(def.getId());
                    def.setCrfNum(crfs.size());

                }
                String moduleManager = CoreResources.getField("moduleManager");
                request.setAttribute("moduleManager", moduleManager);

                String portalURL = CoreResources.getField("portalURL");
                request.setAttribute("portalURL", portalURL);

                request.setAttribute("config", study);

                request.setAttribute("sitesToView", sites);
                request.setAttribute("siteNum", sites.size() + "");

                request.setAttribute("userRolesToView", userRoles);
  //              request.setAttribute("customRoles", customRoles);


                request.setAttribute("userNum", userRoles.size() + "");

                // request.setAttribute("subjectsToView", displayStudySubs);
                // request.setAttribute("subjectNum", subjects.size() + "");

                request.setAttribute("definitionsToView", definitions);
                request.setAttribute("defNum", definitions.size() + "");
                forwardPage(Page.VIEW_FULL_STUDY);

            } else {
                forwardPage(Page.VIEW_STUDY);
            }
        }
    }

    @Override
    protected String getAdminServlet() {
        if (ub.isSysAdmin()) {
            return SecureController.ADMIN_SERVLET_CODE;
        } else {
            return "";
        }
    }

}
