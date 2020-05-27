/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import java.net.MalformedURLException;
import java.util.ArrayList;

import core.org.akaza.openclinica.bean.admin.CRFBean;
import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import core.org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import core.org.akaza.openclinica.bean.submit.FormLayoutBean;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import core.org.akaza.openclinica.core.form.StringUtil;
import core.org.akaza.openclinica.dao.admin.CRFDAO;
import core.org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import core.org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import core.org.akaza.openclinica.dao.submit.FormLayoutDAO;
import core.org.akaza.openclinica.domain.SourceDataVerification;
import core.org.akaza.openclinica.service.managestudy.EventDefinitionCrfTagService;
import org.akaza.openclinica.view.Page;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author jxu
 * 
 *         TODO To change the template for this generated type comment go to Window -
 *         Preferences - Java - Code Style - Code Templates
 */
public class ViewSiteServlet extends SecureController {

    EventDefinitionCrfTagService eventDefinitionCrfTagService = null;

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
        int siteId = request.getParameter("id") == null ? 0 : Integer.valueOf(request.getParameter("id"));
        if (currentStudy.getStudyId() == siteId) {
            return;
        }
        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + " " + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_director"), "1");

    }

    @Override
    public void processRequest() throws Exception {

        String idString = "";
        if (request.getAttribute("siteId") == null) {
            idString = request.getParameter("id");
        } else {
            idString = request.getAttribute("siteId").toString();
        }
        logger.info("site id:" + idString);
        if (StringUtil.isBlank(idString)) {
            addPageMessage(respage.getString("please_choose_a_site_to_edit"));
            forwardPage(Page.MENU_SERVLET);
        } else {
            int siteId = Integer.valueOf(idString.trim()).intValue();
            Study study = (Study) getStudyDao().findByPK(siteId);

            checkRoleByUserAndStudy(ub, study);

            String parentStudyName = "";
            if (study.isSite())
                parentStudyName = study.getStudy().getName();
            request.setAttribute("parentName", parentStudyName);
            request.setAttribute("siteToView", study);
            request.setAttribute("idToSort", request.getAttribute("idToSort"));
            viewSiteEventDefinitions(study);

            forwardPage(Page.VIEW_SITE);
        }
    }

    private void viewSiteEventDefinitions(Study siteToView) throws MalformedURLException {
        int siteId = siteToView.getStudyId();
        ArrayList<StudyEventDefinitionBean> seds = new ArrayList<StudyEventDefinitionBean>();
        StudyEventDefinitionDAO sedDao = new StudyEventDefinitionDAO(sm.getDataSource(), getStudyDao());
        EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(sm.getDataSource());
        FormLayoutDAO fldao = new FormLayoutDAO(sm.getDataSource());
        CRFDAO cdao = new CRFDAO(sm.getDataSource());
        seds = sedDao.findAllByStudy(siteToView);
        int start = 0;
        for (StudyEventDefinitionBean sed : seds) {
            StudyParameterValueDAO spvdao = new StudyParameterValueDAO(sm.getDataSource());
            String participateFormStatus = spvdao.findByHandleAndStudy(sed.getStudyId(), "participantPortal").getValue();
            request.setAttribute("participateFormStatus", participateFormStatus);
            if (participateFormStatus.equals("enabled"))
                baseUrl();

            request.setAttribute("participateFormStatus", participateFormStatus);

            int defId = sed.getId();
            ArrayList<EventDefinitionCRFBean> edcs = (ArrayList<EventDefinitionCRFBean>) edcdao.findAllByDefinitionAndSiteIdAndParentStudyId(defId, siteId,
                    siteToView.checkAndGetParentStudyId());
            ArrayList<EventDefinitionCRFBean> defCrfs = new ArrayList<EventDefinitionCRFBean>();
            for (EventDefinitionCRFBean edcBean : edcs) {
                CRFBean cBean = (CRFBean) cdao.findByPK(edcBean.getCrfId());
                String crfPath = sed.getOid() + "." + cBean.getOid();
                edcBean.setOffline(getEventDefinitionCrfTagService().getEventDefnCrfOfflineStatus(2, crfPath, true));

                int edcStatusId = edcBean.getStatus().getId();
                CRFBean crf = (CRFBean) cdao.findByPK(edcBean.getCrfId());
                int crfStatusId = crf.getStatusId();
                ArrayList<FormLayoutBean> versions = (ArrayList<FormLayoutBean>) fldao.findAllActiveByCRF(edcBean.getCrfId());
                edcBean.setVersions(versions);
                edcBean.setCrfName(crf.getName());
                FormLayoutBean defaultVersion = (FormLayoutBean) fldao.findByPK(edcBean.getDefaultVersionId());
                edcBean.setDefaultVersionName(defaultVersion.getName());
                String sversionIds = edcBean.getSelectedVersionIds();
                ArrayList<Integer> idList = new ArrayList<Integer>();
                String idNames = "";
                if (sversionIds.length() > 0) {
                    String[] ids = sversionIds.split("\\,");
                    for (String id : ids) {
                        idList.add(Integer.valueOf(id));
                        for (FormLayoutBean v : versions) {
                            if (v.getId() == Integer.valueOf(id)) {
                                idNames += v.getName() + ",";
                                break;
                            }
                        }
                    }
                    idNames = idNames.substring(0, idNames.length() - 1);
                }
                if (edcBean.getParentId() < 1) {
                    edcBean.setSubmissionUrl("");
                }
                edcBean.setSelectedVersionIdList(idList);
                edcBean.setSelectedVersionNames(idNames);
                defCrfs.add(edcBean);
                ++start;
            }
            sed.setCrfs(defCrfs);
            sed.setCrfNum(defCrfs.size());
        }

        request.setAttribute("definitions", seds);
        ArrayList<String> sdvOptions = new ArrayList<String>();
        sdvOptions.add(SourceDataVerification.AllREQUIRED.toString());
        sdvOptions.add(SourceDataVerification.PARTIALREQUIRED.toString());
        sdvOptions.add(SourceDataVerification.NOTREQUIRED.toString());
        sdvOptions.add(SourceDataVerification.NOTAPPLICABLE.toString());
        request.setAttribute("sdvOptions", sdvOptions);

    }

    public EventDefinitionCrfTagService getEventDefinitionCrfTagService() {
        eventDefinitionCrfTagService = this.eventDefinitionCrfTagService != null ? eventDefinitionCrfTagService
                : (EventDefinitionCrfTagService) SpringServletAccess.getApplicationContext(context).getBean("eventDefinitionCrfTagService");

        return eventDefinitionCrfTagService;
    }

}
