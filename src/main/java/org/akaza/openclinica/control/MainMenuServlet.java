/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control;

import core.org.akaza.openclinica.bean.core.Utils;
import core.org.akaza.openclinica.bean.service.StudyParameterValueBean;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.control.admin.EventStatusStatisticsTableFactory;
import org.akaza.openclinica.control.admin.SiteStatisticsTableFactory;
import org.akaza.openclinica.control.admin.StudyStatisticsTableFactory;
import org.akaza.openclinica.control.admin.StudySubjectStatusStatisticsTableFactory;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.submit.ListStudySubjectTableFactory;
import core.org.akaza.openclinica.dao.login.UserAccountDAO;
import core.org.akaza.openclinica.dao.managestudy.*;
import core.org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import core.org.akaza.openclinica.dao.submit.EventCRFDAO;
import core.org.akaza.openclinica.dao.submit.SubjectDAO;
import core.org.akaza.openclinica.dao.submit.SubjectGroupMapDAO;
import core.org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.view.Page;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import core.org.akaza.openclinica.web.table.sdv.SDVUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

/**
 * The main controller servlet for all the work behind study sites for
 * OpenClinica.
 * @author jxu
 */
public class MainMenuServlet extends SecureController {

    private final static String STUDY_ENV_UUID = "studyEnvUuid";

    Locale locale;
    private StudyEventDefinitionDAO studyEventDefinitionDAO;
    private SubjectDAO subjectDAO;
    @Autowired
    private StudySubjectDAO studySubjectDAO;
    @Autowired
    private StudyEventDAO studyEventDAO;
    @Autowired
    @Qualifier("studyDaoDomain")
    private StudyDao studyDAO;
    private StudyGroupClassDAO studyGroupClassDAO;
    private SubjectGroupMapDAO subjectGroupMapDAO;
    private StudyGroupDAO studyGroupDAO;
    private DiscrepancyNoteDAO discrepancyNoteDAO;
    @Autowired
    UserAccountDAO userAccountDAO;

    @Override
    public void mayProceed() throws InsufficientPermissionException {
        locale = LocaleResolver.getLocale(request);
    }

    @Override
    public void processRequest() throws Exception {

        FormProcessor fp = new FormProcessor(request);
        session.setAttribute(USER_BEAN_NAME, ub);
        request.setAttribute("iconInfoShown", true);
        request.setAttribute("closeInfoShowIcons", false);

        if (ub == null || ub.getId() == 0) {// in case database connection is
            // broken
            forwardPage(Page.MENU, false);
            return;
        }

        // a flag tells whether users are required to change pwd upon the first
        // time log in or pwd expired
        // update last visit date to current date

        ub.setLastVisitDate(new Date(System.currentTimeMillis()));
        // have to actually set the above to a timestamp? tbh
        userAccountDAO.update(ub);

        if (!currentRole.isActive()) {
            String paramStr = Utils.getParamsString(request.getParameterMap());
            request.setAttribute("prevPageParams", paramStr);
            forwardPage(Page.CHANGE_STUDY_SERVLET, false);
            return;
        }

        // Use study Id in JSPs
        if (currentStudy != null) {
            request.setAttribute("studyId", currentStudy.getStudyId());
            // Event Definition list and Group Class list for add suybject window.
            // request.setAttribute("allDefsArray", super.getEventDefinitionsByCurrentStudy());
            request.setAttribute("studyGroupClasses", super.getStudyGroupClassesByCurrentStudy());
        }

        logger.debug("is ub a ldapuser??" + ub.isLdapUser());

        if (currentStudy == null) {
            logger.error("CurrentStudy is null: forwarding to menu.jsp");
            forwardPage(Page.MENU);
            return;
        }

        ////Integer assignedDiscrepancies = getDiscrepancyNoteDAO().countAllItemDataByStudyAndUser(currentStudy, ub);
        //Integer assignedDiscrepancies = getDiscrepancyNoteDAO().getViewNotesCountWithFilter(" AND dn.assigned_user_id ="
        //  + ub.getId() + " AND (dn.resolution_status_id=1 OR dn.resolution_status_id=2 OR dn.resolution_status_id=3)", currentStudy);
        //Yufang code added by Jamuna, to optimize the query on MainMenu
        if (currentStudy != null) {
            int parentStudyId = currentStudy.isSite() ? currentStudy.getStudy().getStudyId() : currentStudy.getStudyId();
            StudyParameterValueDAO spvdao = new StudyParameterValueDAO(sm.getDataSource());
            StudyParameterValueBean parentSPV = spvdao.findByHandleAndStudy(parentStudyId, "subjectIdGeneration");
            currentStudy.setSubjectIdGeneration(parentSPV.getValue());
            String idSetting = parentSPV.getValue();
            if (idSetting.equals("auto editable") || idSetting.equals("auto non-editable")) {
                //Shaoyu Su
                //int nextLabel = this.getStudySubjectDAO().findTheGreatestLabel() + 1;
                //request.setAttribute("label", new Integer(nextLabel).toString());
                request.setAttribute("label", resword.getString("id_generated_Save_Add"));
                //@pgawade 27-June-2012 fix for issue 13477: set label to "ID will be generated on Save or Add" in case of auto generated subject id
                fp.addPresetValue("label", resword.getString("id_generated_Save_Add"));
            }
        }
        setPresetValues(fp.getPresetValues());

        if (currentRole.isInvestigator() || currentRole.isResearchAssistant() || currentRole.isResearchAssistant2()) {
            forwardPage(Page.LIST_STUDY_SUBJECTS_SERVLET);
            return;
        }
        if (currentRole.isMonitor()) {
            response.sendRedirect(request.getContextPath() + "/pages/viewAllSubjectSDVtmp?sdv_restore=true&studyId=" + currentStudy.getStudyId() + "&sdv_f_sdvStatus=Ready+to+verify+%2B+Changed+since+verified&sdv_s_4_eventDate=asc");
            return;
        } else if (currentRole.isCoordinator() || currentRole.isDirector()) {
            setupStudySiteStatisticsTable();
            setupSubjectEventStatusStatisticsTable();
            setupStudySubjectStatusStatisticsTable();
            if (!currentStudy.isSite()) {
                setupStudyStatisticsTable();
            }

        }

        logger.info("Current Role:" + currentRole.getRole().getName());

        forwardPage(Page.MENU);

    }

    private void setupStudySubjectStatusStatisticsTable() {

        StudySubjectStatusStatisticsTableFactory factory = new StudySubjectStatusStatisticsTableFactory();
        factory.setStudySubjectDao(studySubjectDAO);
        factory.setCurrentStudy(currentStudy);

        String studySubjectStatusStatistics = factory.createTable(request, response).render();
        request.setAttribute("studySubjectStatusStatistics", studySubjectStatusStatistics);
    }

    private void setupSubjectEventStatusStatisticsTable() {

        EventStatusStatisticsTableFactory factory = new EventStatusStatisticsTableFactory();
        factory.setStudySubjectDao(studySubjectDAO);
        factory.setStudyDao(studyDAO);
        factory.setCurrentStudy(currentStudy);
        factory.setStudyEventDao(studyEventDAO);
        String subjectEventStatusStatistics = factory.createTable(request, response).render();
        request.setAttribute("subjectEventStatusStatistics", subjectEventStatusStatistics);
    }

    private void setupStudySiteStatisticsTable() {

        SiteStatisticsTableFactory factory = new SiteStatisticsTableFactory();
        factory.setStudySubjectDao(studySubjectDAO);
        factory.setStudyDao(studyDAO);
        factory.setCurrentStudy(currentStudy);
        String studySiteStatistics = factory.createTable(request, response).render();
        request.setAttribute("studySiteStatistics", studySiteStatistics);

    }

    private void setupStudyStatisticsTable() {

        StudyStatisticsTableFactory factory = new StudyStatisticsTableFactory();
        factory.setStudySubjectDao(studySubjectDAO);
        factory.setStudyDao(studyDAO);
        factory.setCurrentStudy(currentPublicStudy);
        String studyStatistics = factory.createTable(request, response).render();
        request.setAttribute("studyStatistics", studyStatistics);

    }

}
