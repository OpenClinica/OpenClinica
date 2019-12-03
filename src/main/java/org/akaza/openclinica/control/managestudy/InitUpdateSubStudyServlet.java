/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import core.org.akaza.openclinica.bean.admin.CRFBean;
import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.bean.core.Status;
import core.org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import core.org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import core.org.akaza.openclinica.bean.service.StudyParameterValueBean;
import core.org.akaza.openclinica.bean.service.StudyParamsConfig;
import core.org.akaza.openclinica.bean.submit.FormLayoutBean;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.datamap.StudyParameterValue;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
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
 * @version CVS: $Id: InitUpdateSubStudyServlet.java 9834 2007-09-05 22:28:31Z
 *          jxu $
 */
public class InitUpdateSubStudyServlet extends SecureController {
    EventDefinitionCrfTagService eventDefinitionCrfTagService = null;

    /**
     *
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        checkStudyLocked(Page.SITE_LIST_SERVLET, respage.getString("current_study_locked"));
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
        // baseUrl();
        String userName = request.getRemoteUser();
        String idString = request.getParameter("id");
        logger.info("study id:" + idString);
        if (StringUtil.isBlank(idString)) {
            addPageMessage(respage.getString("please_choose_a_study_to_edit"));
            forwardPage(Page.ERROR);
        } else {
            int studyId = Integer.valueOf(idString.trim()).intValue();
            Study study = (Study) getStudyDao().findByPK(studyId);

            checkRoleByUserAndStudy(ub, study);

            String parentStudyName = "";
            Study parent = new Study();
            if (study.isSite()) {
                parent = (Study) getStudyDao().findByPK(study.getStudy().getStudyId());
                parentStudyName = parent.getName();
                // at this time, this feature is only available for site
                createEventDefinitions(parent);
            }

            if(currentStudy.getStudyId() != study.getStudyId()){
                if(study.getStudyParameterValues() != null && study.getStudyParameterValues().size() > 0){
                    List<StudyParameterValue> spvList = currentStudy.getStudyParameterValues();
                    for(StudyParameterValue currentStudySpv : spvList){

                        if(currentStudySpv.getStudyParameterValueId() > 0 && currentStudySpv.getStudyParameter().isOverridable() &&
                                study.getIndividualStudyParameterValue(currentStudySpv.getStudyParameter().getHandle()) != null){
                            StudyParameterValue studySpv = study.getIndividualStudyParameterValue(currentStudySpv.getStudyParameter().getHandle());
                            if(studySpv.getValue().equals("enabled"))
                                baseUrl();
                            if(studySpv.getStudyParameterValueId() > 0)
                                currentStudySpv.setValue(studySpv.getValue());
                        }
                    }
                }
            }
            request.setAttribute("parentStudy", parent);
            session.setAttribute("parentName", parentStudyName);
            session.setAttribute("newStudy", study);
            request.setAttribute("facRecruitStatusMap", CreateStudyServlet.facRecruitStatusMap);
            request.setAttribute("statuses", Status.toStudyUpdateMembersList());

            FormProcessor fp = new FormProcessor(request);
            logger.info("start date:" + study.getDatePlannedEnd());
            if (study.getDatePlannedEnd() != null) {
                fp.addPresetValue(UpdateSubStudyServlet.INPUT_END_DATE, local_df.format(study.getDatePlannedEnd()));
            }
            if (study.getDatePlannedStart() != null) {
                fp.addPresetValue(UpdateSubStudyServlet.INPUT_START_DATE, local_df.format(study.getDatePlannedStart()));
            }
            if (study.getProtocolDateVerification() != null) {
                fp.addPresetValue(UpdateSubStudyServlet.INPUT_VER_DATE, local_df.format(study.getProtocolDateVerification()));
            }
            setPresetValues(fp.getPresetValues());

            forwardPage(Page.UPDATE_SUB_STUDY);
        }

    }

    private void createEventDefinitions(Study parentStudy) throws MalformedURLException {
        FormProcessor fp = new FormProcessor(request);
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(sm.getDataSource());

        int siteId = Integer.valueOf(request.getParameter("id").trim());
        ArrayList<StudyEventDefinitionBean> seds = new ArrayList<StudyEventDefinitionBean>();
        StudyEventDefinitionDAO sedDao = new StudyEventDefinitionDAO(sm.getDataSource(), getStudyDao());
        EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(sm.getDataSource());
        // CRFVersionDAO cvdao = new CRFVersionDAO(sm.getDataSource());
        FormLayoutDAO fldao = new FormLayoutDAO(sm.getDataSource());
        CRFDAO cdao = new CRFDAO(sm.getDataSource());
        seds = sedDao.findAllByStudy(parentStudy);
        int start = 0;
        for (StudyEventDefinitionBean sed : seds) {
            String participateFormStatus = spvdao.findByHandleAndStudy(sed.getStudyId(), "participantPortal").getValue();
            if (participateFormStatus.equals("enabled"))
                baseUrl();
            request.setAttribute("participateFormStatus", participateFormStatus);

            int defId = sed.getId();
            ArrayList<EventDefinitionCRFBean> edcs = (ArrayList<EventDefinitionCRFBean>) edcdao.findAllByDefinitionAndSiteIdAndParentStudyId(defId, siteId,
                    parentStudy.getStudyId());
            ArrayList<EventDefinitionCRFBean> defCrfs = new ArrayList<EventDefinitionCRFBean>();
            // sed.setCrfNum(edcs.size());
            for (EventDefinitionCRFBean edcBean : edcs) {
                CRFBean cBean = (CRFBean) cdao.findByPK(edcBean.getCrfId());
                String crfPath = sed.getOid() + "." + cBean.getOid();
                edcBean.setOffline(getEventDefinitionCrfTagService().getEventDefnCrfOfflineStatus(2, crfPath, true));

                int edcStatusId = edcBean.getStatus().getId();
                CRFBean crf = (CRFBean) cdao.findByPK(edcBean.getCrfId());
                int crfStatusId = crf.getStatusId();
                if (edcStatusId == 5 || edcStatusId == 7 || crfStatusId == 5 || crfStatusId == 7) {
                } else {
                    // ArrayList<CRFVersionBean> versions = (ArrayList<CRFVersionBean>)
                    // cvdao.findAllActiveByCRF(edcBean.getCrfId());
                    ArrayList<FormLayoutBean> versions = (ArrayList<FormLayoutBean>) fldao.findAllActiveByCRF(edcBean.getCrfId());
                    edcBean.setVersions(versions);
                    edcBean.setCrfName(crf.getName());

                    if (edcBean.getParentId() == 0)
                        edcBean.setSubmissionUrl("");

                    // CRFVersionBean defaultVersion = (CRFVersionBean) cvdao.findByPK(edcBean.getDefaultVersionId());
                    FormLayoutBean defaultVersion = (FormLayoutBean) fldao.findByPK(edcBean.getDefaultVersionId());
                    edcBean.setDefaultVersionName(defaultVersion.getName());
                    String sversionIds = edcBean.getSelectedVersionIds();

                    ArrayList<Integer> idList = new ArrayList<Integer>();
                    if (sversionIds.length() > 0) {
                        String[] ids = sversionIds.split("\\,");
                        for (String id : ids) {
                            idList.add(Integer.valueOf(id));
                        }
                    }
                    edcBean.setSelectedVersionIdList(idList);
                    defCrfs.add(edcBean);
                    ++start;
                }
            }
            logger.debug("definitionCrfs size=" + defCrfs.size() + " total size=" + edcs.size());
            sed.setCrfs(defCrfs);
            sed.setCrfNum(defCrfs.size());
        }
        // not sure if request is better, since not sure if there is another
        // process using this.
        session.setAttribute("definitions", seds);
        session.setAttribute("sdvOptions", this.setSDVOptions());
    }

    private ArrayList<String> setSDVOptions() {
        ArrayList<String> sdvOptions = new ArrayList<String>();
        sdvOptions.add(SourceDataVerification.AllREQUIRED.toString());
        sdvOptions.add(SourceDataVerification.PARTIALREQUIRED.toString());
        sdvOptions.add(SourceDataVerification.NOTREQUIRED.toString());
        sdvOptions.add(SourceDataVerification.NOTAPPLICABLE.toString());
        return sdvOptions;
    }

    @Override
    protected String getAdminServlet() {
        if (ub.isSysAdmin()) {
            return SecureController.ADMIN_SERVLET_CODE;
        } else {
            return "";
        }
    }

    public EventDefinitionCrfTagService getEventDefinitionCrfTagService() {
        eventDefinitionCrfTagService = this.eventDefinitionCrfTagService != null ? eventDefinitionCrfTagService
                : (EventDefinitionCrfTagService) SpringServletAccess.getApplicationContext(context).getBean("eventDefinitionCrfTagService");

        return eventDefinitionCrfTagService;
    }

}
