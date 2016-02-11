/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.view.Page;

import java.util.ArrayList;

/**
 * View the details of a study event definition
 *
 * @author jxu
 *
 */
public class ViewEventDefinitionReadOnlyServlet extends ViewEventDefinitionServlet {

    public static String EVENT_ID = "id";
    public static String EVENT_OID = "Oid";

    @Override
    public void processRequest() throws Exception {

        StudyEventDefinitionDAO sdao = new StudyEventDefinitionDAO(sm.getDataSource());
        FormProcessor fp = new FormProcessor(request);
        int defId = fp.getInt(EVENT_ID, true);
        String eventOid = fp.getString(EVENT_OID);

        if (defId == 0 && eventOid == null) {
            addPageMessage(respage.getString("please_choose_a_definition_to_view"));
            forwardPage(Page.LIST_DEFINITION_SERVLET);
            return;
        }

        // definition id
        StudyEventDefinitionBean sed = defId > 0 ? (StudyEventDefinitionBean) sdao.findByPK(defId) : (StudyEventDefinitionBean) sdao.findByOid(eventOid);

        EventDefinitionCRFDAO edao = new EventDefinitionCRFDAO(sm.getDataSource());
        ArrayList eventDefinitionCRFs = (ArrayList) edao.findAllByDefinition(this.currentStudy, sed.getId());

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

            CRFBean cBean = (CRFBean) cdao.findByPK(edc.getCrfId());                
            String crfPath=sed.getOid()+"."+cBean.getOid();
            edc.setOffline(getEventDefnCrfOfflineStatus(2,crfPath,true));

            CRFVersionBean defaultVersion = (CRFVersionBean) cvdao.findByPK(edc.getDefaultVersionId());
            edc.setDefaultVersionName(defaultVersion.getName());
        }
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(sm.getDataSource());    
        String participateFormStatus = spvdao.findByHandleAndStudy(sed.getStudyId(), "participantPortal").getValue();
    
        request.setAttribute("participateFormStatus",participateFormStatus );

        request.setAttribute("definition", sed);
        request.setAttribute("eventDefinitionCRFs", eventDefinitionCRFs);
        request.setAttribute("defSize", new Integer(eventDefinitionCRFs.size()));
        // request.setAttribute("eventDefinitionCRFs", new
        // ArrayList(tm.values()));
        if (defId > 0) {
            forwardPage(Page.VIEW_EVENT_DEFINITION_READONLY);
        } else {
            forwardPage(Page.VIEW_EVENT_DEFINITION_NOSIDEBAR);
        }
    }

}
