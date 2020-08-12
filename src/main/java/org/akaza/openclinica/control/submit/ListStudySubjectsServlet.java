/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2009 Akaza Research
 */
package org.akaza.openclinica.control.submit;

import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.dao.admin.AuditEventDAO;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormDiscrepancyNotes;
import org.akaza.openclinica.control.form.FormProcessor;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.hibernate.*;
import core.org.akaza.openclinica.dao.managestudy.*;
import core.org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import core.org.akaza.openclinica.dao.submit.*;
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
    @Autowired
    private StudySubjectDAO studySubjectDAO;
    @Autowired
    private SubjectDAO subjectDAO;
    @Autowired
    private StudyEventDAO studyEventDAO;
    @Autowired
    private EventCRFDAO eventCRFDAO;
    @Autowired
    private StudyEventDefinitionDAO studyEventDefinitionDAO;
    @Autowired
    private StudyGroupClassDAO studyGroupClassDAO;
    @Autowired
    private SubjectGroupMapDAO subjectGroupMapDAO;
    @Autowired
    private EventDefinitionCRFDAO eventDefinitionCRFDAO;
    @Autowired
    private StudyGroupDAO studyGroupDAO;
    @Autowired
    private StudyParameterValueDAO studyParameterValueDAO;
    @Autowired
    private ViewStudySubjectService viewStudySubjectService;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private ItemDao itemDao;
    @Autowired
    private ItemDataDao itemDataDao;
    @Autowired
    private ItemFormMetadataDao itemFormMetadataDao;
    @Autowired
    private EventCrfDao eventCrfDao;
    @Autowired
    private StudyEventDao studyEventDao;
    @Autowired
    private CrfDao crfDao;
    @Autowired
    private CrfVersionDao crfVersionDao;
    @Autowired
    private EventDefinitionCrfDao eventDefinitionCrfDao;
    @Autowired
    private StudyEventDefinitionDao studyEventDefinitionDao;
    @Autowired
    private EventDefinitionCrfPermissionTagDao permissionTagDao;
    @Autowired
    private StudySubjectDao studySubjectDao;

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

        if (SubmitDataUtil.mayViewData(ub, currentRole)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("" +
                "may_not_submit_data"), "1");
    }

    @Override
    protected void processRequest() throws Exception {
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
            List<StudySubjectBean> studySubjectList = studySubjectDAO.findAllSubjectsByLabelAndStudy(fp.getString("findSubjects_f_studySubject.label"), currentStudy);
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
        factory.setStudyEventDefinitionDao(studyEventDefinitionDAO);
        factory.setSubjectDAO(subjectDAO);
        factory.setStudySubjectDAO(studySubjectDAO);
        factory.setStudyEventDAO(studyEventDAO);
        factory.setStudyDAO(getStudyDao());
        factory.setStudyBean(currentStudy);
        factory.setStudyGroupClassDAO(studyGroupClassDAO);
        factory.setSubjectGroupMapDAO(subjectGroupMapDAO);
        factory.setCurrentRole(currentRole);
        factory.setCurrentUser(ub);
        factory.setEventCRFDAO(eventCRFDAO);
        factory.setEventDefintionCRFDAO(eventDefinitionCRFDAO);
        factory.setStudyGroupDAO(studyGroupDAO);
        factory.setStudyParameterValueDAO(studyParameterValueDAO);
        factory.setUserService(getUserService());
        factory.setRequest(request);
        factory.setViewStudySubjectService(viewStudySubjectService);
        factory.setPermissionService(getPermissionService());
        factory.setItemDao(itemDao);
        factory.setItemDataDao(itemDataDao);
        factory.setCrfDao(crfDao);
        factory.setCrfVersionDao(crfVersionDao);
        factory.setStudyEventDao(studyEventDao);
        factory.setEventCrfDao(eventCrfDao);
        factory.setEventDefinitionCrfDao(eventDefinitionCrfDao);
        factory.setItemFormMetadataDao(itemFormMetadataDao);
        factory.setPermissionTagDao(permissionTagDao);
        factory.setStudySubjectDao(studySubjectDao);
        factory.setStudyEventDefinitionHibDao(studyEventDefinitionDao);

        List<Component> components = viewStudySubjectService.getPageComponents(ListStudySubjectTableFactory.PAGE_NAME);
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

}
