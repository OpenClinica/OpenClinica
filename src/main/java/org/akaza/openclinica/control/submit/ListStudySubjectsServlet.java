/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2009 Akaza Research
 */
package org.akaza.openclinica.control.submit;

import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormDiscrepancyNotes;
import org.akaza.openclinica.control.form.FormProcessor;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.hibernate.*;
import core.org.akaza.openclinica.dao.managestudy.*;
import core.org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import core.org.akaza.openclinica.dao.submit.*;
import core.org.akaza.openclinica.domain.datamap.StudyParameterValue;
import core.org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.service.Component;
import core.org.akaza.openclinica.service.PermissionService;
import org.akaza.openclinica.service.UserService;
import org.akaza.openclinica.service.ViewStudySubjectService;
import org.akaza.openclinica.view.Page;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

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
    private EventCRFDAO eventCRFDAO;
    private EventDefinitionCRFDAO eventDefintionCRFDAO;
    private StudyGroupDAO studyGroupDAO;
    private StudyParameterValueDAO studyParameterValueDAO;
    private UserService userService;
    private ViewStudySubjectService viewStudySubjectService;
    private PermissionService permissionService;
    private ItemDao itemDao;
    private ItemDataDao itemDataDao;
    private ItemFormMetadataDao itemFormMetadataDao;
    private ResponseSetDao responseSetDao;
    private EventCrfDao eventCrfDao;
    private StudyEventDao studyEventDao;
    private CrfDao crfDao;
    private CrfVersionDao crfVersionDao;
    private EventDefinitionCrfDao eventDefinitionCrfDao;
    private StudyEventDefinitionDao studyEventDefinitionHibDao;
    private EventDefinitionCrfPermissionTagDao permissionTagDao;

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
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("" +
                "may_not_submit_data"), "1");
    }

    @Override
    protected void processRequest() throws Exception {
        WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        FormProcessor fp = new FormProcessor(request);
        boolean showMoreLink;
        if(currentStudy !=null && currentStudy.getStudyId() > 0){
            session.setAttribute("study", currentStudy);
        }
        if(currentStudy.isSite())
            currentStudy.setSubjectIdGeneration(currentStudy.getStudy().getSubjectIdGeneration());

        String addNewSubjectOverlay = fp.getRequest().getParameter("addNewSubject");
        if (addNewSubjectOverlay != null){
            if (addNewSubjectOverlay.equals("true")){
                request.setAttribute("showOverlay", true);
            }
        }

        if(fp.getString("showMoreLink").equals("")){
            showMoreLink = true;
        }else {
            showMoreLink = Boolean.parseBoolean(fp.getString("showMoreLink"));
        }
        logger.info("CurrentStudy:" + currentPublicStudy.getSchemaName());
//        logger.info("StudyParameterConfig:" + currentPublicStudy.getStudyParameterConfig().toString());
        String idSetting ;
        if(currentStudy.isSite())
            idSetting = currentStudy.getStudy().getSubjectIdGeneration();
        else
            idSetting = currentStudy.getSubjectIdGeneration();
        logger.info("idSetting:" + idSetting);
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

        request.setAttribute("siteSubStringMark", CoreResources.getField("insight.report.replica.substring"));

        request.setAttribute("closeInfoShowIcons", true);
        if (fp.getString("navBar").equals("yes") && fp.getString("findSubjects_f_studySubject.label").trim().length() > 0) {
            List<StudySubjectBean> studySubjectList = getStudySubjectDAO().findAllSubjectsByLabelAndStudy(fp.getString("findSubjects_f_studySubject.label"), currentStudy);
            if (studySubjectList.size() == 1 ) {
                request.setAttribute("id", new Integer(studySubjectList.get(0).getId()).toString());
                forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
            } else {
                createTable(showMoreLink);
            }
        } else {
            createTable(showMoreLink);
        }

    }

    private void createTable(boolean showMoreLink) {

        ListStudySubjectTableFactory factory = new ListStudySubjectTableFactory(showMoreLink);
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
        factory.setUserService(getUserService());
        factory.setRequest(request);
        factory.setViewStudySubjectService(getViewStudySubjectService());
        factory.setPermissionService(getPermissionService());
        factory.setItemDao(getItemDao());
        factory.setItemDataDao(getItemDataDao());
        factory.setCrfDao(getCrfDao());
        factory.setCrfVersionDao(getCrfVersionDao());
        factory.setStudyEventDao(getStudyEventDao());
        factory.setEventCrfDao(getEventCrfDao());
        factory.setEventDefinitionCrfDao(getEventDefinitionCrfDao());
        factory.setItemFormMetadataDao(getItemFormMetadataDao());
        factory.setPermissionTagDao(getPermissionTagDao());
        factory.setStudyEventDefinitionHibDao(getStudyEventDefinitionHibDao());

        List<Component> components = getViewStudySubjectService().getPageComponents(ListStudySubjectTableFactory.PAGE_NAME);
        if (components != null) {
            for (Component component : components) {
                if (component.getColumns() != null) {
                    List<String> permissionTags = permissionService.getPermissionTagsList(request);
                    request.getSession().setAttribute("userPermissionTags", permissionTags);
                    break;
                }
            }
        }

        String findSubjectsHtml = factory.createTable(request, response).render();

        request.setAttribute("findSubjectsHtml", findSubjectsHtml);
        // A. Hamid.
        // For event definitions and group class list in the add subject popup
        // request.setAttribute("allDefsArray", super.getEventDefinitionsByCurrentStudy());
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

    public UserService getUserService() {
        return userService= (UserService) SpringServletAccess.getApplicationContext(context).getBean("userService");
    }

    public ViewStudySubjectService getViewStudySubjectService() {
        return viewStudySubjectService= (ViewStudySubjectService) SpringServletAccess.getApplicationContext(context).getBean("viewStudySubjectService");
    }

    public ItemDao getItemDao() {
        return itemDao=(ItemDao) SpringServletAccess.getApplicationContext(context).getBean("itemDao");
    }

    public ItemDataDao getItemDataDao() {
        return itemDataDao=(ItemDataDao) SpringServletAccess.getApplicationContext(context).getBean("itemDataDao");
    }

    public CrfDao getCrfDao() {
        return crfDao=(CrfDao) SpringServletAccess.getApplicationContext(context).getBean("crfDao");
    }

    public CrfVersionDao getCrfVersionDao() {
        return crfVersionDao=(CrfVersionDao) SpringServletAccess.getApplicationContext(context).getBean("crfVersionDao");
    }

    public StudyEventDao getStudyEventDao() {
        return studyEventDao=(StudyEventDao) SpringServletAccess.getApplicationContext(context).getBean("studyEventDaoDomain");
    }

    public EventCrfDao getEventCrfDao() {
        return eventCrfDao=(EventCrfDao) SpringServletAccess.getApplicationContext(context).getBean("eventCrfDao");
    }

    public ItemFormMetadataDao getItemFormMetadataDao() {
        return itemFormMetadataDao=(ItemFormMetadataDao) SpringServletAccess.getApplicationContext(context).getBean("itemFormMetadataDao");
    }

    public EventDefinitionCrfDao getEventDefinitionCrfDao() {
        return eventDefinitionCrfDao=(EventDefinitionCrfDao) SpringServletAccess.getApplicationContext(context).getBean("eventDefinitionCrfDao");
    }

    public EventDefinitionCrfPermissionTagDao getPermissionTagDao() {
        return permissionTagDao=(EventDefinitionCrfPermissionTagDao) SpringServletAccess.getApplicationContext(context).getBean("eventDefinitionCrfPermissionTagDao");
    }

    public PermissionService getPermissionService() {
        return permissionService= (PermissionService) SpringServletAccess.getApplicationContext(context).getBean("permissionService");
    }

    public StudyEventDefinitionDao getStudyEventDefinitionHibDao() {
        return studyEventDefinitionHibDao= (StudyEventDefinitionDao) SpringServletAccess.getApplicationContext(context).getBean("studyEventDefDaoDomain");

    }

    public StudyDao getStudyDao() {
        return (StudyDao) SpringServletAccess.getApplicationContext(context).getBean("studyDaoDomain");
    }
}
