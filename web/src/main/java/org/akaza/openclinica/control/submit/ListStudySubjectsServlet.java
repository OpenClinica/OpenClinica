/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2009 Akaza Research
 */
package org.akaza.openclinica.control.submit;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormDiscrepancyNotes;
import org.akaza.openclinica.control.form.FormProcessor;
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

/**
 * Servlet for creating a table.
 *
 * @author Krikor Krumlian
 */
public class ListStudySubjectsServlet extends SecureController {

    // Shaoyu Su
    private static final long serialVersionUID = 1L;
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
    private boolean showMoreLink;
    private StudyParameterValueDAO studyParameterValueDAO;
    Locale locale;

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.control.core.SecureController#mayProceed()
     */
    @Override
    protected void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);

        if (ub.isSysAdmin()) {
            return;
        }

        if (SubmitDataServlet.mayViewData(ub, currentRole)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("may_not_submit_data"), "1");
    }

    @Override
    protected void processRequest() throws Exception {
        getCrfLocker().unlockAllForUser(ub.getId());
        FormProcessor fp = new FormProcessor(request);
        if(fp.getString("showMoreLink").equals("")){
            showMoreLink = true;
        }else {
            showMoreLink = Boolean.parseBoolean(fp.getString("showMoreLink"));
        }
        String idSetting = currentStudy.getStudyParameterConfig().getSubjectIdGeneration();
        // set up auto study subject id
        if (idSetting.equals("auto editable") || idSetting.equals("auto non-editable")) {
            //Shaoyu Su
            // int nextLabel = getStudySubjectDAO().findTheGreatestLabel() + 1;
            // request.setAttribute("label", new Integer(nextLabel).toString());
            request.setAttribute("label", resword.getString("id_generated_Save_Add"));
            fp.addPresetValue("label", resword.getString("id_generated_Save_Add"));
        }

        if (fp.getRequest().getParameter("subjectOverlay") == null){
            Date today = new Date(System.currentTimeMillis());
            String todayFormatted = local_df.format(today);
            if (request.getAttribute(PRESET_VALUES) != null) {
                fp.setPresetValues((HashMap)request.getAttribute(PRESET_VALUES));
            }
            fp.addPresetValue(AddNewSubjectServlet.INPUT_ENROLLMENT_DATE, todayFormatted);
            fp.addPresetValue(AddNewSubjectServlet.INPUT_EVENT_START_DATE, todayFormatted);
            setPresetValues(fp.getPresetValues());
        }

        ub.incNumVisitsToMainMenu();

        request.setAttribute("closeInfoShowIcons", true);
        if (fp.getString("navBar").equals("yes") && fp.getString("findSubjects_f_studySubject.label").trim().length() > 0) {
            StudySubjectBean studySubject = getStudySubjectDAO().findByLabelAndStudy(fp.getString("findSubjects_f_studySubject.label"), currentStudy);
            if (studySubject.getId() > 0) {
                request.setAttribute("id", new Integer(studySubject.getId()).toString());
                forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
            } else {
                createTable();
            }
        } else {
            createTable();
        }

    }

    private void createTable() {

        ListStudySubjectTableFactory factory = new ListStudySubjectTableFactory(showMoreLink);
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
        // A. Hamid.
        // For event definitions and group class list in the add subject popup
        request.setAttribute("allDefsArray", super.getEventDefinitionsByCurrentStudy());
        request.setAttribute("studyGroupClasses", super.getStudyGroupClassesByCurrentStudy());
        FormDiscrepancyNotes discNotes = new FormDiscrepancyNotes();
        session.setAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME, discNotes);

        forwardPage(Page.LIST_STUDY_SUBJECTS);

    }

    @Override
    protected String getAdminServlet() {
        return SecureController.ADMIN_SERVLET_CODE;
    }
    
    public StudyParameterValueDAO getStudyParameterValueDAO() {
        studyParameterValueDAO = this.studyParameterValueDAO == null ? new StudyParameterValueDAO(sm.getDataSource()) : studyParameterValueDAO;
		return studyParameterValueDAO;
	}

	public void setStudyParameterValueDAO(StudyParameterValueDAO studyParameterValueDAO) {
		this.studyParameterValueDAO = studyParameterValueDAO;
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

}
