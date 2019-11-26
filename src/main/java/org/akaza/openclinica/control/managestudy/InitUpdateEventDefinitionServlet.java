/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import core.org.akaza.openclinica.bean.admin.CRFBean;
import core.org.akaza.openclinica.bean.core.NullValue;
import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.bean.core.Status;
import core.org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import core.org.akaza.openclinica.bean.managestudy.StudyEventBean;
import core.org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import core.org.akaza.openclinica.bean.submit.FormLayoutBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import core.org.akaza.openclinica.core.form.StringUtil;
import core.org.akaza.openclinica.dao.admin.CRFDAO;
import core.org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import core.org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import core.org.akaza.openclinica.dao.submit.FormLayoutDAO;
import core.org.akaza.openclinica.domain.SourceDataVerification;
import core.org.akaza.openclinica.service.managestudy.EventDefinitionCrfTagService;
import org.akaza.openclinica.view.Page;
import core.org.akaza.openclinica.web.InsufficientPermissionException;

/**
 * Prepares to update study event definition
 *
 * @author jxu
 *
 */
public class InitUpdateEventDefinitionServlet extends SecureController {
    EventDefinitionCrfTagService eventDefinitionCrfTagService = null;

    /**
     * Checks whether the user has the correct privilege
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        checkStudyLocked(Page.LIST_DEFINITION_SERVLET, respage.getString("current_study_locked"));
        if (ub.isSysAdmin()) {
            return;
        }

        StudyEventDAO sdao = new StudyEventDAO(sm.getDataSource());
        // get current studyid
        int studyId = currentStudy.getStudyId();

        if (ub.hasRoleInStudy(studyId)) {
            Role r = ub.getRoleByStudy(studyId).getRole();
            if (r.equals(Role.STUDYDIRECTOR) || r.equals(Role.COORDINATOR)) {
                return;
            } else {
                addPageMessage(
                        respage.getString("no_have_permission_to_update_study_event_definition") + respage.getString("please_contact_sysadmin_questions"));
                throw new InsufficientPermissionException(Page.LIST_DEFINITION_SERVLET, resexception.getString("not_study_director"), "1");

            }
        }

        // To Do: the following code doesn't apply to admin for now
        String idString = request.getParameter("id");
        int defId = Integer.valueOf(idString.trim()).intValue();
        logger.info("defId" + defId);
        ArrayList events = (ArrayList) sdao.findAllByDefinition(defId);
        if (events != null && events.size() > 0) {
            logger.info("has events");
            for (int i = 0; i < events.size(); i++) {
                StudyEventBean sb = (StudyEventBean) events.get(i);
                if (!sb.getStatus().equals(Status.DELETED) && !sb.getStatus().equals(Status.AUTO_DELETED)) {
                    logger.info("found one event");
                    addPageMessage(respage.getString("sorry_but_at_this_time_may_not_modufy_SED"));
                    throw new InsufficientPermissionException(Page.LIST_DEFINITION_SERVLET, resexception.getString("not_unpopulated"), "1");
                }
            }
        }

    }

    @Override
    public void processRequest() throws Exception {

        StudyEventDefinitionDAO sdao = new StudyEventDefinitionDAO(sm.getDataSource());
        String idString = request.getParameter("id");
        logger.info("definition id: " + idString);
        if (StringUtil.isBlank(idString)) {
            addPageMessage(respage.getString("please_choose_a_definition_to_edit"));
            forwardPage(Page.LIST_DEFINITION_SERVLET);
        } else {
            // definition id
            int defId = Integer.valueOf(idString.trim()).intValue();
            StudyEventDefinitionBean sed = (StudyEventDefinitionBean) sdao.findByPK(defId);
            StudyParameterValueDAO spvdao = new StudyParameterValueDAO(sm.getDataSource());
            String participateFormStatus = spvdao.findByHandleAndStudy(sed.getStudyId(), "participantPortal").getValue();
            if (participateFormStatus.equals("enabled"))
                baseUrl();
            request.setAttribute("participateFormStatus", participateFormStatus);

            if (currentStudy.getStudyId() != sed.getStudyId()) {
                addPageMessage(respage.getString("no_have_correct_privilege_current_study") + " " + respage.getString("change_active_study_or_contact"));
                forwardPage(Page.MENU_SERVLET);
                return;
            }

            EventDefinitionCRFDAO edao = new EventDefinitionCRFDAO(sm.getDataSource());
            ArrayList eventDefinitionCRFs = (ArrayList) edao.findAllParentsByDefinition(defId);

            FormLayoutDAO fldao = new FormLayoutDAO(sm.getDataSource());
            CRFDAO cdao = new CRFDAO(sm.getDataSource());
            ArrayList newEventDefinitionCRFs = new ArrayList();
            for (int i = 0; i < eventDefinitionCRFs.size(); i++) {
                EventDefinitionCRFBean edc = (EventDefinitionCRFBean) eventDefinitionCRFs.get(i);
                ArrayList versions = (ArrayList) fldao.findAllActiveByCRF(edc.getCrfId());
                edc.setVersions(versions);
                CRFBean crf = (CRFBean) cdao.findByPK(edc.getCrfId());
                edc.setCrfName(crf.getName());
                edc.setCrf(crf);
                edc.setParticipantForm(edc.isParticipantForm());
                // TO DO: use a better way on JSP page,eg.function tag
                edc.setNullFlags(processNullValues(edc));
                FormLayoutBean formLayout = (FormLayoutBean) fldao.findByPK(edc.getDefaultVersionId());
                edc.setDefaultVersionName(formLayout.getName());

                String crfPath = sed.getOid() + "." + edc.getCrf().getOid();
                edc.setOffline(getEventDefinitionCrfTagService().getEventDefnCrfOfflineStatus(2, crfPath, true));
                newEventDefinitionCRFs.add(edc);
            }

            session.setAttribute("definition", sed);
            session.setAttribute("eventDefinitionCRFs", newEventDefinitionCRFs);
            // changed above to new list because static, in-place updating is
            // updating all EDCs, tbh 102007

            ArrayList<String> sdvOptions = new ArrayList<String>();
            sdvOptions.add(SourceDataVerification.AllREQUIRED.toString());
            sdvOptions.add(SourceDataVerification.PARTIALREQUIRED.toString());
            sdvOptions.add(SourceDataVerification.NOTREQUIRED.toString());
            sdvOptions.add(SourceDataVerification.NOTAPPLICABLE.toString());
            request.setAttribute("sdvOptions", sdvOptions);

            forwardPage(Page.UPDATE_EVENT_DEFINITION1);
        }

    }

    private HashMap processNullValues(EventDefinitionCRFBean edc) {
        HashMap flags = new LinkedHashMap();
        String s = "";// edc.getNullValues();
        for (int j = 0; j < edc.getNullValuesList().size(); j++) {
            NullValue nv1 = (NullValue) edc.getNullValuesList().get(j);
            s = s + nv1.getName().toUpperCase() + ",";
        }
        // String s = edc.getNullValuesList().toString();
        logger.info("********:" + s);
        if (s != null) {
            for (int i = 1; i <= NullValue.toArrayList().size(); i++) {
                String nv = NullValue.get(i).getName().toUpperCase();
                // if (s.indexOf(nv) >= 0) {
                // indexOf won't save us
                // because NA and NASK will come back both positive, for example
                // rather, we need a regexp here
                Pattern p = Pattern.compile(nv + "\\W");
                // find our word with a non-word character after it (,)
                Matcher m = p.matcher(s);
                if (m.find()) {
                    flags.put(nv, "1");
                    logger.info("********1:" + nv + " found at " + m.start() + ", " + m.end());
                } else {
                    flags.put(nv, "0");
                    logger.info("********0:" + nv);
                }

            }
        }

        return flags;
    }

    public EventDefinitionCrfTagService getEventDefinitionCrfTagService() {
        eventDefinitionCrfTagService = this.eventDefinitionCrfTagService != null ? eventDefinitionCrfTagService
                : (EventDefinitionCrfTagService) SpringServletAccess.getApplicationContext(context).getBean("eventDefinitionCrfTagService");

        return eventDefinitionCrfTagService;
    }

}
