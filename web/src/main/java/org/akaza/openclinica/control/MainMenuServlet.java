/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.control.admin.EventStatusStatisticsTableFactory;
import org.akaza.openclinica.control.admin.SiteStatisticsTableFactory;
import org.akaza.openclinica.control.admin.StudyStatisticsTableFactory;
import org.akaza.openclinica.control.admin.StudySubjectStatusStatisticsTableFactory;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.submit.ListStudySubjectTableFactory;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupClassDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.dao.submit.SubjectGroupMapDAO;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.SQLInitServlet;
import org.akaza.openclinica.web.table.sdv.SDVUtil;

/**
 *
 * The main controller servlet for all the work behind study sites for
 * OpenClinica.
 *
 * @author jxu
 *
 */
public class MainMenuServlet extends SecureController {

    //Shaoyu Su
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

    // < ResourceBundle respage;

    @Override
    public void mayProceed() throws InsufficientPermissionException {
        locale = LocaleResolver.getLocale(request);
        // < respage =
        // ResourceBundle.getBundle("org.akaza.openclinica.i18n.page_messages",locale);
    }

    @Override
    public void processRequest() throws Exception {

    	FormProcessor fp = new FormProcessor(request);
        ub.incNumVisitsToMainMenu();
        session.setAttribute(USER_BEAN_NAME, ub);
        request.setAttribute("iconInfoShown", true);
        request.setAttribute("closeInfoShowIcons", false);

        if (ub == null || ub.getId() == 0) {// in case database connection is
            // broken
            forwardPage(Page.MENU, false);
            return;
        }

        StudyDAO sdao = new StudyDAO(sm.getDataSource());
        ArrayList studies = null;

        long pwdExpireDay = new Long(SQLInitServlet.getField("passwd_expiration_time")).longValue();
        Date lastPwdChangeDate = ub.getPasswdTimestamp();

        // a flag tells whether users are required to change pwd upon the first
        // time log in or pwd expired
        int pwdChangeRequired = new Integer(SQLInitServlet.getField("change_passwd_required")).intValue();
        // update last visit date to current date
        UserAccountDAO udao = new UserAccountDAO(sm.getDataSource());
        UserAccountBean ub1 = (UserAccountBean) udao.findByPK(ub.getId());
        ub1.setLastVisitDate(new Date(System.currentTimeMillis()));
        // have to actually set the above to a timestamp? tbh
        ub1.setOwner(ub1);
        ub1.setUpdater(ub1);
        udao.update(ub1);

        // Use study Id in JSPs
        request.setAttribute("studyId", currentStudy.getId());
        // Event Definition list and Group Class list for add suybject window.
        request.setAttribute("allDefsArray", super.getEventDefinitionsByCurrentStudy());
        request.setAttribute("studyGroupClasses", super.getStudyGroupClassesByCurrentStudy());
    if (ub.isLdapUser()) {
            // "Forge" a password change date for LDAP user
            lastPwdChangeDate = new Date();
        }
System.out.println("is ub a ldapuser??"+ub.isLdapUser());


        //@pgawade 18-Sep-2012: fix for issue #14506 (https://issuetracker.openclinica.com/view.php?id=14506#c58197)
        if( (lastPwdChangeDate != null) || ((lastPwdChangeDate == null) && (pwdChangeRequired == 0))) {// not a new user
            if(lastPwdChangeDate != null){

	        	Calendar cal = Calendar.getInstance();
	            
	            // compute difference between current date and lastPwdChangeDate
	            long difference = Math.abs(cal.getTime().getTime() - lastPwdChangeDate.getTime());
	            long days = difference / (1000 * 60 * 60 * 24);
	            session.setAttribute("passwordExpired", "no");
	
	            if (!ub.isLdapUser() && pwdExpireDay > 0 && days >= pwdExpireDay) {// password expired, need to be changed
			System.out.println("here");
			studies = (ArrayList) sdao.findAllByUser(ub.getName());
	                request.setAttribute("studies", studies);
	                session.setAttribute("userBean1", ub);
	                addPageMessage(respage.getString("password_expired"));
	                // YW 06-25-2007 << add the feature that if password is expired,
	                // have to go through /ResetPassword page
	                session.setAttribute("passwordExpired", "yes");
	                if (pwdChangeRequired == 1) {
	                    request.setAttribute("mustChangePass", "yes");
	                    addPageMessage(respage.getString("your_password_has_expired_must_change"));
	                } else {
	                    request.setAttribute("mustChangePass", "no");
	                    addPageMessage(respage.getString("password_expired") + " " + respage.getString("if_you_do_not_want_change_leave_blank"));
	                }
	                forwardPage(Page.RESET_PASSWORD);
	                // YW >>
	            } 
            }
//            else {

                if (ub.getNumVisitsToMainMenu() <= 1) {
                    if (ub.getLastVisitDate() != null) {
                        addPageMessage(respage.getString("welcome") + " " + ub.getFirstName() + " " + ub.getLastName() + ". "
                            + respage.getString("last_logged") + " " + local_df.format(ub.getLastVisitDate()) + ". ");
                    } else {
                        addPageMessage(respage.getString("welcome") + " " + ub.getFirstName() + " " + ub.getLastName() + ". ");
                    }

                    if (currentStudy.getStatus().isLocked()) {
                        addPageMessage(respage.getString("current_study_locked"));
                    } else if (currentStudy.getStatus().isFrozen()) {
                        addPageMessage(respage.getString("current_study_frozen"));
                    }
                }

                ////Integer assignedDiscrepancies = getDiscrepancyNoteDAO().countAllItemDataByStudyAndUser(currentStudy, ub);
                //Integer assignedDiscrepancies = getDiscrepancyNoteDAO().getViewNotesCountWithFilter(" AND dn.assigned_user_id ="
                //  + ub.getId() + " AND (dn.resolution_status_id=1 OR dn.resolution_status_id=2 OR dn.resolution_status_id=3)", currentStudy);
                //Yufang code added by Jamuna, to optimize the query on MainMenu
                Integer assignedDiscrepancies = getDiscrepancyNoteDAO().getViewNotesCountWithFilter(ub.getId(), currentStudy.getId());
                request.setAttribute("assignedDiscrepancies", assignedDiscrepancies == null ? 0 : assignedDiscrepancies);

                int parentStudyId = currentStudy.getParentStudyId()>0?currentStudy.getParentStudyId():currentStudy.getId();
                StudyParameterValueDAO spvdao = new StudyParameterValueDAO(sm.getDataSource());
                StudyParameterValueBean parentSPV = spvdao.findByHandleAndStudy(parentStudyId, "subjectIdGeneration");
                currentStudy.getStudyParameterConfig().setSubjectIdGeneration(parentSPV.getValue());
                String idSetting = parentSPV.getValue();
                if (idSetting.equals("auto editable") || idSetting.equals("auto non-editable")) {
                    //Shaoyu Su
                    //int nextLabel = this.getStudySubjectDAO().findTheGreatestLabel() + 1;
                    //request.setAttribute("label", new Integer(nextLabel).toString());
                    request.setAttribute("label", resword.getString("id_generated_Save_Add"));
                    //@pgawade 27-June-2012 fix for issue 13477: set label to "ID will be generated on Save or Add" in case of auto generated subject id
                    fp.addPresetValue("label", resword.getString("id_generated_Save_Add"));
                }
                setPresetValues(fp.getPresetValues());

                if (currentRole.isInvestigator() || currentRole.isResearchAssistant() || currentRole.isResearchAssistant2()) {
                    setupListStudySubjectTable();
                }
                if (currentRole.isMonitor()) {
                    setupSubjectSDVTable();
                } else if (currentRole.isCoordinator() || currentRole.isDirector()) {
                    if (currentStudy.getStatus().isPending()) {
                        response.sendRedirect(request.getContextPath() + Page.MANAGE_STUDY_MODULE.getFileName());
                        return;
                    }
                    setupStudySiteStatisticsTable();
                    setupSubjectEventStatusStatisticsTable();
                    setupStudySubjectStatusStatisticsTable();
                    if (currentStudy.getParentStudyId() == 0) {
                        setupStudyStatisticsTable();
                    }

                }

                forwardPage(Page.MENU);
//            }

        } else {// a new user's first log in
            studies = (ArrayList) sdao.findAllByUser(ub.getName());
            request.setAttribute("studies", studies);
            session.setAttribute("userBean1", ub);
//            addPageMessage(respage.getString("welcome") + " " + ub.getFirstName() + " " + ub.getLastName() + ". " + respage.getString("password_set"));
//                + "<a href=\"UpdateProfile\">" + respage.getString("user_profile") + " </a>");
            
            if (pwdChangeRequired == 1) {
            } else {
                forwardPage(Page.MENU);
            }
        }

    }

    private void setupSubjectSDVTable() {

        request.setAttribute("studyId", currentStudy.getId());
        request.setAttribute("showMoreLink", "true");
        String sdvMatrix = getSDVUtil().renderEventCRFTableWithLimit(request, currentStudy.getId(), "");
        request.setAttribute("sdvMatrix", sdvMatrix);
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

    
    public StudyParameterValueDAO getStudyParameterValueDAO() {
     studyParameterValueDAO = this.studyParameterValueDAO == null ? new StudyParameterValueDAO(sm.getDataSource()) : studyParameterValueDAO;
		return studyParameterValueDAO;
	}

	public void setStudyParameterValueDAO(StudyParameterValueDAO studyParameterValueDAO) {
		studyParameterValueDAO = studyParameterValueDAO;
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

}
