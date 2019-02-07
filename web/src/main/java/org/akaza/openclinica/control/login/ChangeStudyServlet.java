/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.login;

import org.akaza.openclinica.bean.core.CustomRole;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.admin.EventStatusStatisticsTableFactory;
import org.akaza.openclinica.control.admin.SiteStatisticsTableFactory;
import org.akaza.openclinica.control.admin.StudyStatisticsTableFactory;
import org.akaza.openclinica.control.admin.StudySubjectStatusStatisticsTableFactory;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.control.submit.ListStudySubjectTableFactory;
import org.akaza.openclinica.controller.helper.StudyInfoObject;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.*;
import org.akaza.openclinica.dao.service.StudyConfigService;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.dao.submit.SubjectGroupMapDAO;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.service.StudyBuildService;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.table.sdv.SDVUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * @author jxu
 *
 * Processes the request of changing current study
 */
public class ChangeStudyServlet extends SecureController {
    /**
     * Checks whether the user has the correct privilege
     */

    Locale locale;
    private StudyEventDefinitionDAO studyEventDefinitionDAO;
    private SubjectDAO subjectDAO;
    private StudySubjectDAO studySubjectDAO;
    private StudyEventDAO studyEventDAO;
    private StudyGroupClassDAO studyGroupClassDAO;
    private SubjectGroupMapDAO subjectGroupMapDAO;
    private StudyDAO studyDAO;
    private EventCRFDAO eventCRFDAO;
    private EventDefinitionCRFDAO eventDefintionCRFDAO;
    private StudyGroupDAO studyGroupDAO;
    private DiscrepancyNoteDAO discrepancyNoteDAO;
    private StudyParameterValueDAO studyParameterValueDAO;
    private StudyBuildService studyBuildService;

    // < ResourceBundlerestext;

    @Override
    public void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);
        // < restext =
        // ResourceBundle.getBundle("org.akaza.openclinica.i18n.notes",locale);

    }


    @Override
    public void processRequest() throws Exception {

        String action = request.getParameter("action");// action sent by user
        request.setAttribute("requestSchema", "public");
        UserAccountDAO udao = new UserAccountDAO(sm.getDataSource());
        StudyDAO sdao = new StudyDAO(sm.getDataSource());

        ArrayList<StudyUserRoleBean> studies = udao.findStudyByUser(ub.getName(), (ArrayList) sdao.findAll());
        CustomRole customRole = new CustomRole();

        populateCustomUserRoles(customRole, ub.getName());
        request.setAttribute("siteRoleMap", customRole.siteRoleMap);
        request.setAttribute("studyRoleMap", customRole.studyRoleMap);
        if(request.getAttribute("label")!=null) {
            String label = (String) request.getAttribute("label");
            if(label.length()>0) {
                request.setAttribute("label", label);
            }
        }

        ArrayList<StudyUserRoleBean> validStudies = new ArrayList<>();
        ArrayList<StudyBean> studyList = new ArrayList<>();
        for (int i = 0; i < studies.size(); i++) {
            StudyUserRoleBean sr = (StudyUserRoleBean) studies.get(i);
            StudyBean study = (StudyBean) sdao.findByPK(sr.getStudyId());
            if (study != null && study.getStatus().equals(Status.PENDING)) {
                sr.setStatus(study.getStatus());
            }
            if (study.isPublished() == false)
                continue;
            studyList.add(study);
            validStudies.add(sr);
        }


        if (StringUtils.isEmpty(action)) {
            request.setAttribute("studies", validStudies);

            forwardPage(Page.CHANGE_STUDY);
        } else {

            validateChangeStudy(studies, studyList);
            logger.info("submit");
            changeStudy(customRole);
            return;
        }

    }

    private void validateChangeStudy(List<StudyUserRoleBean> studies, List<StudyBean> studyList) throws Exception {
        Validator v = new Validator(request);
        FormProcessor fp = new FormProcessor(request);
        v.addValidation("studyId", Validator.IS_AN_INTEGER);

        errors = v.validate();

        if (!errors.isEmpty()) {
            request.setAttribute("studies", studies);
            forwardPage(Page.CHANGE_STUDY);
        } else {
            int studyId = fp.getInt("studyId");
            logger.info("new study id:" + studyId);
            for (int i = 0; i < studies.size(); i++) {
                StudyUserRoleBean studyWithRole = (StudyUserRoleBean) studies.get(i);
                if (studyWithRole.getStudyId() == studyId) {
                    request.setAttribute("studyId", new Integer(studyId));
                    session.setAttribute("studyWithRole", studyWithRole);
                    StudyInfoObject studyInfoObject = getProtocolInfo(studyId, studyList);
                    if (studyInfoObject == null)
                        break;
                    if (StringUtils.isNotEmpty(studyInfoObject.getSchema())) {
                        request.setAttribute("changeStudySchema", studyInfoObject.getSchema());
                    } else // should this be DEFAULT_TENANT_ID from CoreResources?
                        request.setAttribute("changeStudySchema", "public");

                    String studyEnvUuid = StringUtils.isNotEmpty(studyInfoObject.getStudyBean().getStudyEnvUuid()) ?
                            studyInfoObject.getStudyBean().getStudyEnvUuid()
                            : studyInfoObject.getStudyBean().getStudyEnvSiteUuid();
                    request.setAttribute("studyEnvUuid", studyEnvUuid);
                    request.setAttribute("currentStudy", currentStudy);
                    return;

                }
            }
            addPageMessage(restext.getString("no_study_selected"));

            forwardPage(Page.CHANGE_STUDY);
        }
    }

    private StudyInfoObject getProtocolInfo(int studyId, List<StudyBean>studyList) {
        for (StudyBean study: studyList) {
            if (study.getId() == studyId) {
                StudyInfoObject studyInfoObject = new StudyInfoObject(study.getSchemaName(), study);
                return studyInfoObject;
            }
        }
        return null;
    }
    private void changeStudy(CustomRole customRole) throws Exception {
        Validator v = new Validator(request);
        FormProcessor fp = new FormProcessor(request);
        String newStudySchema = fp.getString("changeStudySchema", true);
        request.setAttribute("requestSchema", "public");
        request.setAttribute("changeStudySchema", null);
        String studyEnvUuid = fp.getString("studyEnvUuid", true);
        String prevStudyEnvUuid = currentStudy != null ? currentStudy.getStudyEnvUuid() : null;
        
        StudyDAO sdao = new StudyDAO(sm.getDataSource());

        String oldStudySchema = null;
        if (currentStudy.getParentStudyId() <= 0) {
            oldStudySchema = sdao.findByStudyEnvUuid(currentStudy.getStudyEnvUuid()).getSchemaName();
        } else {
            oldStudySchema = sdao.findByStudyEnvUuid(currentStudy.getStudyEnvSiteUuid()).getSchemaName();
        }
        
        StudyBean newPublicStudy = sdao.findByStudyEnvUuid(studyEnvUuid);
        request.setAttribute("changeStudySchema", newStudySchema);
        StudyBean newStudy = sdao.findByStudyEnvUuid(studyEnvUuid); 

        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(sm.getDataSource());
        ArrayList studyParameters = spvdao.findParamConfigByStudy(newStudy); 
        newStudy.setStudyParameters(studyParameters);
        request.setAttribute("changeStudySchema", null);

        if (currentStudy != null) { 
            int parentStudyId = currentStudy.getParentStudyId() > 0 ? currentStudy.getParentStudyId() : currentStudy.getId();
            request.setAttribute("requestSchema", oldStudySchema); 
            StudyParameterValueBean parentSPV = spvdao.findByHandleAndStudy(parentStudyId, "subjectIdGeneration");
            newStudy.getStudyParameterConfig().setSubjectIdGeneration(parentSPV.getValue());
            request.setAttribute("requestSchema", "public");
            String idSetting = newStudy.getStudyParameterConfig().getSubjectIdGeneration();
            if (idSetting.equals("auto editable") || idSetting.equals("auto non-editable")) {
                request.setAttribute("changeStudySchema", newStudySchema);
                int nextLabel = this.getStudySubjectDAO().findTheGreatestLabel() + 1;
                request.setAttribute("label", new Integer(nextLabel).toString());
                request.setAttribute("changeStudySchema", null);
            }
            request.setAttribute("requestSchema", newStudySchema); //schema we are changing to.
            StudyConfigService scs = new StudyConfigService(sm.getDataSource());
            if (newStudy.getParentStudyId() <= 0) {// top study
                scs.setParametersForStudy(newStudy); 
            } else {
                if (newStudy.getParentStudyId() > 0) {
                    newStudy.setParentStudyName((sdao.findByPK(newStudy.getParentStudyId())).getName());
                }
                scs.setParametersForSite(newStudy);

            }
        }
        request.setAttribute("requestSchema", "public");
        if (newStudy.getStatus().equals(Status.DELETED) || newStudy.getStatus().equals(Status.AUTO_DELETED)) {
            session.removeAttribute("studyWithRole");
            addPageMessage(restext.getString("study_choosed_removed_restore_first"));
        } else {
            session.setAttribute("publicStudy", newPublicStudy);
            currentPublicStudy = newPublicStudy;
            // change user's active study id
            UserAccountDAO udao = new UserAccountDAO(sm.getDataSource());
            ub.setActiveStudyId(newPublicStudy.getId());
            ub.setUpdater(ub);
            ub.setUpdatedDate(new java.util.Date());
            udao.update(ub);
            getStudyBuildService().updateParticipateModuleStatusInOC(request,newPublicStudy.getOid());
            request.setAttribute("changeStudySchema", newStudySchema);
            StudyDAO sdaoStudy = new StudyDAO(sm.getDataSource());
            StudyBean study = sdaoStudy.findByStudyEnvUuid(studyEnvUuid);
            study.setParentStudyName(newStudy.getParentStudyName());
            study.setStudyParameterConfig(newStudy.getStudyParameterConfig());
            session.setAttribute("study", study);
            currentStudy = study;

            StudyBean userRoleStudy = CoreResources.getPublicStudy(currentRole.getStudyId(), sm.getDataSource());
            if (userRoleStudy.getParentStudyId() > 0) {
                /*
                 * The Role decription will be set depending on whether the user
                 * logged in at study lever or site level. issue-2422
                 */
                List roles = Role.toArrayList();
                for (Iterator it = roles.iterator(); it.hasNext();) {
                    Role role = (Role) it.next();
                    switch (role.getId()) {
                    case 2:
                        role.setDescription("site_Study_Coordinator");
                        break;
                    case 3:
                        role.setDescription("site_Study_Director");
                        break;
                    case 4:
                        role.setDescription("site_investigator");
                        break;
                    case 5:
                        role.setDescription("site_Data_Entry_Person");
                        break;
                    case 6:
                        role.setDescription("site_monitor");
                        break;
                    case 7:
                        role.setDescription("site_Data_Entry_Person2");
                        break;
                    case 8:
                        role.setDescription("site_Data_Entry_Participant");
                        break;
                    default:
                        // logger.info("No role matched when setting role description");
                    }
                }
            } else {
                /*
                 * If the current study is a site, we will change the role
                 * description. issue-2422
                 */
                List roles = Role.toArrayList();
                for (Iterator it = roles.iterator(); it.hasNext();) {
                    Role role = (Role) it.next();
                    switch (role.getId()) {
                    case 2:
                        role.setDescription("Study_Coordinator");
                        break;
                    case 3:
                        role.setDescription("Study_Director");
                        break;
                    case 4:
                        role.setDescription("investigator");
                        break;
                    case 5:
                        role.setDescription("Data_Entry_Person");
                        break;
                    case 6:
                        role.setDescription("monitor");
                        break;
                    default:
                        // logger.info("No role matched when setting role description");
                    }
                }
            }

            currentRole = (StudyUserRoleBean) session.getAttribute("studyWithRole");
            session.setAttribute("userRole", currentRole);
            if (currentPublicStudy.getParentStudyId() == 0)
                session.setAttribute("customUserRole", customRole.studyRoleMap.get(currentPublicStudy.getId()));
            else
                session.setAttribute("customUserRole", customRole.siteRoleMap.get(currentPublicStudy.getId()));
            session.removeAttribute("studyWithRole");
            addPageMessage(restext.getString("current_study_changed_succesfully"));
        }
        // YW 2-18-2008, if study has been really changed <<
        if (StringUtils.equals(prevStudyEnvUuid, studyEnvUuid) != true)  {
            session.removeAttribute("eventsForCreateDataset");
            session.setAttribute("tableFacadeRestore", "false");
        }
        request.setAttribute("studyJustChanged", "yes");
        // YW >>

        //Integer assignedDiscrepancies = getDiscrepancyNoteDAO().countAllItemDataByStudyAndUser(currentStudy, ub);
        Integer assignedDiscrepancies = getDiscrepancyNoteDAO().getViewNotesCountWithFilter(" AND dn.assigned_user_id ="
                + ub.getId() + " AND (dn.resolution_status_id=1 OR dn.resolution_status_id=2 OR dn.resolution_status_id=3)", currentStudy);
        request.setAttribute("assignedDiscrepancies", assignedDiscrepancies == null ? 0 : assignedDiscrepancies);
        request.setAttribute("enrollmentCapped", isEnrollmentCapped());

        if (currentRole.isInvestigator() || currentRole.isResearchAssistant()|| currentRole.isResearchAssistant2()) {
            response.sendRedirect(request.getContextPath() + Page.LIST_STUDY_SUBJECTS_SERVLET.getFileName());
            return;
        }
        if (currentRole.isMonitor()) {
            response.sendRedirect(request.getContextPath() + "/pages/viewAllSubjectSDVtmp?sdv_restore=true&studyId=" + currentStudy.getId() + "&studyJustChanged=yes");
        } else if (currentRole.isCoordinator() || currentRole.isDirector()) {
            setupStudySiteStatisticsTable();
            setupSubjectEventStatusStatisticsTable();
            setupStudySubjectStatusStatisticsTable();
            if (currentStudy.getParentStudyId() == 0) {
                setupStudyStatisticsTable();
            }

        }

        forwardPage(Page.MENU);

    }

    private void setupSubjectSDVTable() {

     //   request.setAttribute("studyId", currentStudy.getId());
     //   String sdvMatrix = getSDVUtil().renderEventCRFTableWithLimit(request, currentStudy.getId(), "");
     //   request.setAttribute("sdvMatrix", sdvMatrix);
    }

    private void setupStudySubjectStatusStatisticsTable() {

        StudySubjectStatusStatisticsTableFactory factory = new StudySubjectStatusStatisticsTableFactory();
        factory.setStudySubjectDao(getStudySubjectDAO());
        factory.setCurrentStudy(currentStudy);
        factory.setStudyDao(getStudyDAO());
        String studySubjectStatusStatistics = factory.createTable(request, response).render();
        request.setAttribute("studySubjectStatusStatistics", studySubjectStatusStatistics);
    }

    private void setupSubjectEventStatusStatisticsTable() {

        EventStatusStatisticsTableFactory factory = new EventStatusStatisticsTableFactory();
        factory.setStudySubjectDao(getStudySubjectDAO());
        factory.setCurrentStudy(currentStudy);
        factory.setStudyEventDao(getStudyEventDAO());
        factory.setStudyDao(getStudyDAO());
        String subjectEventStatusStatistics = factory.createTable(request, response).render();
        request.setAttribute("subjectEventStatusStatistics", subjectEventStatusStatistics);
    }

    private void setupStudySiteStatisticsTable() {

        SiteStatisticsTableFactory factory = new SiteStatisticsTableFactory();
        factory.setStudySubjectDao(getStudySubjectDAO());
        factory.setCurrentStudy(currentStudy);
        factory.setStudyDao(getStudyDAO());
        String studySiteStatistics = factory.createTable(request, response).render();
        request.setAttribute("studySiteStatistics", studySiteStatistics);

    }

    private void setupStudyStatisticsTable() {

        StudyStatisticsTableFactory factory = new StudyStatisticsTableFactory();
        factory.setStudySubjectDao(getStudySubjectDAO());
        factory.setCurrentStudy(currentStudy);
        factory.setStudyDao(getStudyDAO());
        String studyStatistics = factory.createTable(request, response).render();
        request.setAttribute("studyStatistics", studyStatistics);

    }

    private void setupListStudySubjectTable() {

        ListStudySubjectTableFactory factory = new ListStudySubjectTableFactory(true);
        factory.setStudyEventDefinitionDao(getStudyEventDefinitionDao());
        factory.setSubjectDAO(getSubjectDAO());
        factory.setStudySubjectDAO(getStudySubjectDAO());
        factory.setStudyEventDAO(getStudyEventDAO());
        factory.setStudyBean(currentStudy);
        factory.setStudyGroupClassDAO(getStudyGroupClassDAO());
        factory.setSubjectGroupMapDAO(getSubjectGroupMapDAO());
        factory.setStudyDAO(getStudyDAO());
        factory.setCurrentRole(currentRole);
        factory.setCurrentUser(ub);
        factory.setEventCRFDAO(getEventCRFDAO());
        factory.setEventDefintionCRFDAO(getEventDefinitionCRFDAO());
        factory.setStudyGroupDAO(getStudyGroupDAO());
        factory.setStudyParameterValueDAO(getStudyParameterValueDAO());
        String findSubjectsHtml = factory.createTable(request, response).render();
        request.setAttribute("findSubjectsHtml", findSubjectsHtml);
    }

    public StudyEventDefinitionDAO getStudyEventDefinitionDao() {
        studyEventDefinitionDAO = studyEventDefinitionDAO == null ? new StudyEventDefinitionDAO(sm.getDataSource()) : studyEventDefinitionDAO;
        return studyEventDefinitionDAO;
    }

    public SubjectDAO getSubjectDAO() {
        subjectDAO = this.subjectDAO == null ? new SubjectDAO(sm.getDataSource()) : subjectDAO;
        return subjectDAO;
    }

    public StudySubjectDAO getStudySubjectDAO() {
        studySubjectDAO = this.studySubjectDAO == null ? new StudySubjectDAO(sm.getDataSource()) : studySubjectDAO;
        return studySubjectDAO;
    }

    public StudyGroupClassDAO getStudyGroupClassDAO() {
        studyGroupClassDAO = this.studyGroupClassDAO == null ? new StudyGroupClassDAO(sm.getDataSource()) : studyGroupClassDAO;
        return studyGroupClassDAO;
    }

    public SubjectGroupMapDAO getSubjectGroupMapDAO() {
        subjectGroupMapDAO = this.subjectGroupMapDAO == null ? new SubjectGroupMapDAO(sm.getDataSource()) : subjectGroupMapDAO;
        return subjectGroupMapDAO;
    }

    public StudyEventDAO getStudyEventDAO() {
        studyEventDAO = this.studyEventDAO == null ? new StudyEventDAO(sm.getDataSource()) : studyEventDAO;
        return studyEventDAO;
    }

    public StudyDAO getStudyDAO() {
        studyDAO = this.studyDAO == null ? new StudyDAO(sm.getDataSource()) : studyDAO;
        return studyDAO;
    }

    public EventCRFDAO getEventCRFDAO() {
        eventCRFDAO = this.eventCRFDAO == null ? new EventCRFDAO(sm.getDataSource()) : eventCRFDAO;
        return eventCRFDAO;
    }

    public EventDefinitionCRFDAO getEventDefinitionCRFDAO() {
        eventDefintionCRFDAO = this.eventDefintionCRFDAO == null ? new EventDefinitionCRFDAO(sm.getDataSource()) : eventDefintionCRFDAO;
        return eventDefintionCRFDAO;
    }

    public StudyGroupDAO getStudyGroupDAO() {
        studyGroupDAO = this.studyGroupDAO == null ? new StudyGroupDAO(sm.getDataSource()) : studyGroupDAO;
        return studyGroupDAO;
    }

    public DiscrepancyNoteDAO getDiscrepancyNoteDAO() {
        discrepancyNoteDAO = this.discrepancyNoteDAO == null ? new DiscrepancyNoteDAO(sm.getDataSource()) : discrepancyNoteDAO;
        return discrepancyNoteDAO;
    }

    public SDVUtil getSDVUtil() {
        return (SDVUtil) SpringServletAccess.getApplicationContext(context).getBean("sdvUtil");
    }

	public StudyParameterValueDAO getStudyParameterValueDAO() {
	     studyParameterValueDAO = this.studyParameterValueDAO == null ? new StudyParameterValueDAO(sm.getDataSource()) : studyParameterValueDAO;
		return studyParameterValueDAO;
	}

	public void setStudyParameterValueDAO(StudyParameterValueDAO studyParameterValueDAO) {
		this.studyParameterValueDAO = studyParameterValueDAO;
	}

    public StudyBuildService getStudyBuildService() {
        WebApplicationContext ctx =
                WebApplicationContextUtils
                        .getWebApplicationContext(context);
        return ctx.getBean("studyBuildService", StudyBuildService.class);
    }

}
