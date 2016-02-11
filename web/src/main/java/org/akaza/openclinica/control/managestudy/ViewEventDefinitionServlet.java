/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.domain.datamap.CrfBean;
import org.akaza.openclinica.service.pmanage.Authorization;
import org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

/**
 * View the details of a study event definition
 *
 * @author jxu
 *
 */
public class ViewEventDefinitionServlet extends SecureController {
    /**
     * Checks whether the user has the correct privilege
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }
        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + " " + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.LIST_DEFINITION_SERVLET, resexception.getString("not_director"), "1");

    }

    @Override
    public void processRequest() throws Exception {

        StudyEventDefinitionDAO sdao = new StudyEventDefinitionDAO(sm.getDataSource());
        StudyDAO studyDao = new StudyDAO(sm.getDataSource());
        FormProcessor fp = new FormProcessor(request);
        int defId = fp.getInt("id", true);

        if (defId == 0) {
            addPageMessage(respage.getString("please_choose_a_definition_to_view"));
            forwardPage(Page.LIST_DEFINITION_SERVLET);
        } else {
            // definition id
            StudyEventDefinitionBean sed = (StudyEventDefinitionBean) sdao.findByPK(defId);

            if (currentStudy.getId() != sed.getStudyId()) {
                addPageMessage(respage.getString("no_have_correct_privilege_current_study")
                        + " " + respage.getString("change_active_study_or_contact"));
                forwardPage(Page.MENU_SERVLET);
                return;
            }
            
            checkRoleByUserAndStudy(ub, sed.getStudyId(), 0);

            EventDefinitionCRFDAO edao = new EventDefinitionCRFDAO(sm.getDataSource());
            ArrayList eventDefinitionCRFs = (ArrayList) edao.findAllByDefinition(this.currentStudy, defId);

            CRFVersionDAO cvdao = new CRFVersionDAO(sm.getDataSource());
            CRFDAO cdao = new CRFDAO(sm.getDataSource());

            for (int i = 0; i < eventDefinitionCRFs.size(); i++) {
                EventDefinitionCRFBean edc = (EventDefinitionCRFBean) eventDefinitionCRFs.get(i);
                ArrayList versions = (ArrayList) cvdao.findAllByCRF(edc.getCrfId());
                edc.setVersions(versions);
                CRFBean crf = (CRFBean) cdao.findByPK(edc.getCrfId());
                // edc.setCrfLabel(crf.getLabel());
                edc.setCrfName(crf.getName());
                // to show/hide edit action on jsp page
                if (crf.getStatus().equals(Status.AVAILABLE)) {
                    edc.setOwner(crf.getOwner());
                }

                CRFVersionBean defaultVersion = (CRFVersionBean) cvdao.findByPK(edc.getDefaultVersionId());
                edc.setDefaultVersionName(defaultVersion.getName());
  
                CRFBean cBean = (CRFBean) cdao.findByPK(edc.getCrfId());                
                String crfPath=sed.getOid()+"."+cBean.getOid();
                edc.setOffline(getEventDefnCrfOfflineStatus(2,crfPath,true));
            }
            
            StudyParameterValueDAO spvdao = new StudyParameterValueDAO(sm.getDataSource());    
            String participateFormStatus = spvdao.findByHandleAndStudy(sed.getStudyId(), "participantPortal").getValue();       
            request.setAttribute("participateFormStatus",participateFormStatus );       
            if (participateFormStatus.equals("enabled")) baseUrl();

            request.setAttribute("participateFormStatus",participateFormStatus );

            
            
            request.setAttribute("definition", sed);
            request.setAttribute("eventDefinitionCRFs", eventDefinitionCRFs);
            request.setAttribute("defSize", new Integer(eventDefinitionCRFs.size()));
            // request.setAttribute("eventDefinitionCRFs", new
            // ArrayList(tm.values()));
            forwardPage(Page.VIEW_EVENT_DEFINITION);
        }

    }
}
