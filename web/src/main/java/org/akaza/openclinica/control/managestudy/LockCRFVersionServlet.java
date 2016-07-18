/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.control.admin.RemoveCRFVersionServlet;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.util.ArrayList;

public class LockCRFVersionServlet extends SecureController {
    /**
    *
    */
   @Override
   public void mayProceed() throws InsufficientPermissionException {
       if (ub.isSysAdmin()) {
           return;
       }

       if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
           return;
       }

       addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
       throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_study_director"), "1");

   }
   
   @Override
   public void processRequest() throws Exception {
       FormProcessor fp = new FormProcessor(request);
       
       int crfVersionId = fp.getInt("id",true);
       String action = fp.getString("action");
       
       // checks which module the requests are from
       String module = fp.getString(MODULE);
       request.setAttribute(MODULE, module);
       
       if(crfVersionId ==0) {
           addPageMessage(respage.getString("choose_valid_crf_version"));
           forwardPage(Page.CRF_LIST_SERVLET);
           return;
       }
       
       CRFVersionDAO cvdao = new CRFVersionDAO(sm.getDataSource());
       CRFDAO cdao = new CRFDAO (sm.getDataSource());
       
       CRFVersionBean version = (CRFVersionBean)cvdao.findByPK(crfVersionId);
       //System.out.println("crf version found:" + version.getName());
       CRFBean crf = (CRFBean)cdao.findByPK(version.getCrfId());

       if (!ub.isSysAdmin() && (version.getOwnerId() != ub.getId())) {
           addPageMessage(respage.getString("no_have_correct_privilege_current_study")
                   + " " + respage.getString("change_active_study_or_contact"));
           forwardPage(Page.MENU_SERVLET);
           return;
       }
      
       EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());
       ArrayList eventCRFs = ecdao.findAllStudySubjectByCRFVersion(crfVersionId);
       
       if (StringUtil.isBlank(action)) {
           request.setAttribute("crfVersionToLock", version);
           request.setAttribute("crf", crf);
           request.setAttribute("eventSubjectsUsingVersion", eventCRFs);
           forwardPage(Page.CONFIRM_LOCKING_CRF_VERSION);
           
       } else if ("confirm".equalsIgnoreCase(action)) {
           version.setStatus(Status.LOCKED);
           version.setUpdater(ub);
           cvdao.update(version);

           ArrayList versionList = (ArrayList)cvdao.findAllByCRF(version.getCrfId());
           if(versionList.size() > 0){
               EventDefinitionCRFDAO edCRFDao = new EventDefinitionCRFDAO(sm.getDataSource());
               ArrayList edcList = (ArrayList)edCRFDao.findAllByCRF(version.getCrfId());
               for(int i = 0; i < edcList.size(); i++){
                   EventDefinitionCRFBean edcBean = (EventDefinitionCRFBean)edcList.get(i);
                    // @pgawade 18-May-2011 #5414 - Changes for setting the correct
                    // default crf version Id to event
                    // when existing default version is locked
                    // RemoveCRFVersionServlet.updateEventDef(edcBean, edCRFDao,
                    // versionList);
                    RemoveCRFVersionServlet.updateEventDef(edcBean, edCRFDao, versionList, crfVersionId);
               }
           }

           addPageMessage(respage.getString("crf_version_archived_successfully"));
           forwardPage(Page.CRF_LIST_SERVLET);
       }
   }

}
