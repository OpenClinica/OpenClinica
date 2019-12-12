/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.login;

import core.org.akaza.openclinica.bean.core.CustomRole;
import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.bean.core.Status;
import core.org.akaza.openclinica.bean.login.StudyUserRoleBean;
import core.org.akaza.openclinica.bean.service.StudyParameterValueBean;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
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
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.login.UserAccountDAO;
import core.org.akaza.openclinica.dao.managestudy.*;
import core.org.akaza.openclinica.dao.service.StudyConfigService;
import core.org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import core.org.akaza.openclinica.dao.submit.EventCRFDAO;
import core.org.akaza.openclinica.dao.submit.SubjectDAO;
import core.org.akaza.openclinica.dao.submit.SubjectGroupMapDAO;
import core.org.akaza.openclinica.i18n.core.LocaleResolver;
import core.org.akaza.openclinica.service.StudyBuildService;
import core.org.akaza.openclinica.service.StudyEnvironmentRoleDTO;
import core.org.akaza.openclinica.service.randomize.ModuleProcessor;
import org.akaza.openclinica.view.Page;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import core.org.akaza.openclinica.web.table.sdv.SDVUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.util.*;
import java.util.stream.Collectors;

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
//    private StudyDAO studyDAO;
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
        // ResourceBundle.getBundle("core.org.akaza.openclinica.i18n.notes",locale);

    }


    @Override
    public void processRequest() throws Exception {

        String action = request.getParameter("action");// action sent by user
        request.setAttribute("requestSchema", "public");
        UserAccountDAO udao = new UserAccountDAO(sm.getDataSource(), getStudyDao());
        Map<Integer, Study> allStudies = getStudyDao().findAll().stream().collect(Collectors.toMap(s->s.getStudyId(),s-> s));
        ArrayList<StudyUserRoleBean> studies = udao.findStudyByUser(ub.getName(), new ArrayList<Study>( allStudies.values()));
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
        for (int i = 0; i < studies.size(); i++) {
            StudyUserRoleBean sr = (StudyUserRoleBean) studies.get(i);
            Study study = allStudies.get(sr.getStudyId());
            if (study != null && study.getStatus().equals(Status.PENDING)) {
                sr.setStatus(Status.get(study.getStatus().getCode()));
            }
            if (study.isPublished() == false)
                continue;
            validStudies.add(sr);
        }


        if (StringUtils.isEmpty(action)) {
            request.setAttribute("studies", validStudies);

            forwardPage(Page.CHANGE_STUDY);
        } else {

            validateChangeStudy(studies, new ArrayList<Study> (allStudies.values()));
            logger.info("submit");
            changeStudy(customRole);
            return;
        }

    }

    private void validateChangeStudy(List<StudyUserRoleBean> studies, List<Study> studyList) throws Exception {
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

                    String studyEnvUuid = StringUtils.isNotEmpty(studyInfoObject.getStudy().getStudyEnvUuid()) ?
                            studyInfoObject.getStudy().getStudyEnvUuid()
                            : studyInfoObject.getStudy().getStudyEnvSiteUuid();
                    request.setAttribute("studyEnvUuid", studyEnvUuid);
                    request.setAttribute("currentStudy", currentStudy);
                    return;

                }
            }
            addPageMessage(restext.getString("no_study_selected"));

            forwardPage(Page.CHANGE_STUDY);
        }
    }

    private StudyInfoObject getProtocolInfo(int studyId, List<Study>studyList) {
        for (Study study: studyList) {
            if (study.getStudyId() == studyId) {
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
        
        String oldStudySchema = null;
        if (!currentStudy.isSite()) {
            oldStudySchema =currentStudy.getSchemaName();
        } else {
            oldStudySchema = currentStudy.getStudy().getSchemaName();
        }
        
        Study newPublicStudy = getStudyDao().findByStudyEnvUuid(studyEnvUuid);
        request.setAttribute("changeStudySchema", newStudySchema);
        request.setAttribute("requestSchema",newStudySchema);
        Study newStudy = getStudyDao().findByStudyEnvUuid(studyEnvUuid);

        request.setAttribute("changeStudySchema", null);

        if (currentStudy != null) {
            request.setAttribute("requestSchema", "public");
            String idSetting = newStudy.getSubjectIdGeneration();
            if (idSetting.equals("auto editable") || idSetting.equals("auto non-editable")) {
                request.setAttribute("changeStudySchema", newStudySchema);
                int nextLabel = this.getStudySubjectDAO().findTheGreatestLabel() + 1;
                request.setAttribute("label", new Integer(nextLabel).toString());
                request.setAttribute("changeStudySchema", null);
            }
            request.setAttribute("requestSchema", newStudySchema); //schema we are changing to.
            StudyConfigService scs = new StudyConfigService(sm.getDataSource());
//            if (newStudy.isSite())
//                scs.setParametersForSite(newStudy);
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
            ub.setActiveStudyId(newPublicStudy.getStudyId());
            ub.setUpdater(ub);
            ub.setUpdatedDate(new java.util.Date());
            udao.update(ub);

            String accessToken = (String) request.getSession().getAttribute("accessToken");
            session.setAttribute("study", newStudy);
            getStudyBuildService().processModule(accessToken, newPublicStudy, ModuleProcessor.Modules.PARTICIPATE);
            request.setAttribute("changeStudySchema", newStudySchema);
            currentStudy = newStudy;

            Study userRoleStudy = getStudyBuildService().getPublicStudy(currentRole.getStudyId());
            if (userRoleStudy.isSite()) {
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
            // update baseUserRole value when switch study/site(OC-10770)
            StudyEnvironmentRoleDTO envRole = getBaseRoleName();
            session.setAttribute("customUserRole", envRole.getDynamicRoleName());
            session.setAttribute("baseUserRole", envRole.getBaseRoleName());
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
        request.setAttribute("enrollmentCapped", isEnrollmentCapped());

        if (currentRole.isInvestigator() || currentRole.isResearchAssistant()|| currentRole.isResearchAssistant2()) {
            response.sendRedirect(request.getContextPath() + Page.LIST_STUDY_SUBJECTS_SERVLET.getFileName());
            return;
        }
        if (currentRole.isMonitor()) {
            response.sendRedirect(request.getContextPath() + "/pages/viewAllSubjectSDVtmp?sdv_restore=true&studyId=" + currentStudy.getStudyId() + "&studyJustChanged=yes");
            return;
        } else if (currentRole.isCoordinator() || currentRole.isDirector()) {
            setupStudySiteStatisticsTable();
            setupSubjectEventStatusStatisticsTable();
            setupStudySubjectStatusStatisticsTable();
            if (currentStudy.getStudy() ==null || currentStudy.getStudy().getStudyId() == 0) {
                setupStudyStatisticsTable();
            }

        }

        forwardPage(Page.MENU);

    }

    private StudyEnvironmentRoleDTO getBaseRoleName() {
        List<StudyEnvironmentRoleDTO> roles = (List<StudyEnvironmentRoleDTO>) session.getAttribute("allUserRoles");
        StudyEnvironmentRoleDTO role;

        if (currentStudy.getStudyEnvSiteUuid() != null && !currentStudy.getStudyEnvSiteUuid().equals("")) {
            // Active study is a site level study
            // Active study is a site level hence it's parent is the study

            // Look for a site based role
            role = roles.stream()
                    .filter(s -> s.getSiteUuid() != null && s.getSiteUuid().equals(currentStudy.getStudyEnvSiteUuid()))
                    .findAny()
                    .orElse(null);

            if (role == null) {
                // The user inherit a study level role
                Study parent = currentStudy.getStudy();
                role = roles.stream()
                        .filter(s -> s.getStudyEnvironmentUuid() != null && s.getStudyEnvironmentUuid().equals(parent.getStudyEnvUuid()))
                        .findAny()
                        .orElse(null);
            }
        } else {
            // Active study is a study not a site
            role = roles.stream()
                    .filter(s -> s.getStudyEnvironmentUuid() != null && s.getStudyEnvironmentUuid().equals(currentStudy.getStudyEnvUuid()))
                    .findAny()
                    .orElse(null);
        }
        return role;
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
        String studySubjectStatusStatistics = factory.createTable(request, response).render();
        request.setAttribute("studySubjectStatusStatistics", studySubjectStatusStatistics);
    }

    private void setupSubjectEventStatusStatisticsTable() {

        EventStatusStatisticsTableFactory factory = new EventStatusStatisticsTableFactory();
        factory.setStudySubjectDao(getStudySubjectDAO());
        factory.setCurrentStudy(currentStudy);
        factory.setStudyEventDao(getStudyEventDAO());
        factory.setStudyDao(getStudyDao());
        String subjectEventStatusStatistics = factory.createTable(request, response).render();
        request.setAttribute("subjectEventStatusStatistics", subjectEventStatusStatistics);
    }

    private void setupStudySiteStatisticsTable() {

        SiteStatisticsTableFactory factory = new SiteStatisticsTableFactory();
        factory.setStudySubjectDao(getStudySubjectDAO());
        factory.setCurrentStudy(currentStudy);
        factory.setStudyDao(getStudyDao());
        String studySiteStatistics = factory.createTable(request, response).render();
        request.setAttribute("studySiteStatistics", studySiteStatistics);

    }

    private void setupStudyStatisticsTable() {

        StudyStatisticsTableFactory factory = new StudyStatisticsTableFactory();
        factory.setStudySubjectDao(getStudySubjectDAO());
        factory.setCurrentStudy(currentStudy);
        factory.setStudyDao(getStudyDao());
        String studyStatistics = factory.createTable(request, response).render();
        request.setAttribute("studyStatistics", studyStatistics);

    }

    private void setupListStudySubjectTable() {

        ListStudySubjectTableFactory factory = new ListStudySubjectTableFactory(true);
        factory.setStudyEventDefinitionDao(getStudyEventDefinitionDao());
        factory.setSubjectDAO(getSubjectDAO());
        factory.setStudySubjectDAO(getStudySubjectDAO());
        factory.setStudyEventDAO(getStudyEventDAO());
        factory.setStudyDAO(getStudyDao());
        factory.setStudyBean(currentStudy);
        factory.setStudyGroupClassDAO(getStudyGroupClassDAO());
        factory.setSubjectGroupMapDAO(getSubjectGroupMapDAO());
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

    public StudyDao getStudyDao() {
        return (StudyDao) SpringServletAccess.getApplicationContext(context).getBean("studyDaoDomain");
    }
}
