/*
 * Created on Sep 21, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.akaza.openclinica.control.admin;

import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.bean.managestudy.StudyEventBean;
import core.org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.bean.submit.SubjectBean;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import core.org.akaza.openclinica.dao.admin.AuditDAO;
import core.org.akaza.openclinica.dao.admin.CRFDAO;
import core.org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import core.org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import core.org.akaza.openclinica.dao.submit.CRFVersionDAO;
import core.org.akaza.openclinica.dao.submit.EventCRFDAO;
import core.org.akaza.openclinica.dao.submit.SubjectDAO;
import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.view.Page;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * @author thickerson
 * 
 * 
 */
public class AuditLogStudyServlet extends SecureController {

    Locale locale;
    private StudyEventDAO studyEventDAO;
    private EventCRFDAO eventCRFDAO;


    public static String getLink(int userId) {
        return "AuditLogStudy";
    }

    /*
     * (non-Javadoc) Assume that we get the user id automatically. We will jump
     * from the edit user page if the user is an admin, they can get to see the
     * users' log
     * 
     * @see org.akaza.openclinica.control.core.SecureController#processRequest()
     */

    /*
     * (non-Javadoc) redo this servlet to run the audits per study subject for
     * the study; need to add a studyId param and then use the
     * StudySubjectDAO.findAllByStudyOrderByLabel() method to grab a lot of
     * study subject beans and then return them much like in
     * ViewStudySubjectAuditLogServet.process()
     * 
     * currentStudy instead of studyId?
     */
    @Override
    protected void processRequest() throws Exception {

        studyEventDAO = (StudyEventDAO) SpringServletAccess.getApplicationContext(context).getBean("studyEventJDBCDao");
        eventCRFDAO = (EventCRFDAO) SpringServletAccess.getApplicationContext(context).getBean("eventCRFJDBCDao");
        StudySubjectDAO subdao = new StudySubjectDAO(sm.getDataSource());
        SubjectDAO sdao = new SubjectDAO(sm.getDataSource());
        AuditDAO adao = new AuditDAO(sm.getDataSource());

        FormProcessor fp = new FormProcessor(request);

        StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(sm.getDataSource());

        HashMap eventsHashMap = new HashMap();
        HashMap studySubjectAuditsHashMap = new HashMap();
        HashMap subjectHashMap = new HashMap();

        ArrayList studySubjects = subdao.findAllByStudyOrderByLabel(currentStudy);
        logger.info("found " + studySubjects.size() + " study subjects");
        request.setAttribute("studySubjects", studySubjects);

        for (int ss = 0; ss < studySubjects.size(); ss++) {
            ArrayList studySubjectAudits = new ArrayList();

            StudySubjectBean studySubject = (StudySubjectBean) studySubjects.get(ss);
            // request.setAttribute("studySub"+ss, studySubject);
            SubjectBean subject = (SubjectBean) sdao.findByPK(studySubject.getSubjectId());
            subjectHashMap.put(new Integer(studySubject.getId()), subject);
            // logger.info("just set a subject with a status of
            // "+subject.getStatus().getName());
            // request.setAttribute("subject"+ss, subject);
            Study study = (Study) getStudyDao().findByPK(studySubject.getStudyId());
            request.setAttribute("study", study);
            // hmm, repetitive work?

            // Show both study subject and subject audit events together
            studySubjectAudits.addAll(adao.findStudySubjectAuditEvents(studySubject.getId())); // Study
            // subject
            // value
            // changed
            studySubjectAudits.addAll(adao.findSubjectAuditEvents(subject.getId())); // Global
            // subject
            // value
            // changed

            studySubjectAuditsHashMap.put(new Integer(studySubject.getId()), studySubjectAudits);
            // request.setAttribute("studySubjectAudits"+ss,
            // studySubjectAudits);

            // Get the list of events
            ArrayList events = studyEventDAO.findAllByStudySubject(studySubject);
            for (int i = 0; i < events.size(); i++) {
                // Link study event definitions
                StudyEventBean studyEvent = (StudyEventBean) events.get(i);
                studyEvent.setStudyEventDefinition((StudyEventDefinitionBean) seddao.findByPK(studyEvent.getStudyEventDefinitionId()));

                // Link event CRFs
                studyEvent.setEventCRFs(eventCRFDAO.findAllByStudyEvent(studyEvent));
            }

            eventsHashMap.put(new Integer(studySubject.getId()), events);
            // request.setAttribute("events"+ss, events);
            // eventCRFAuditsHashMap.put(new Integer(studySubject.getId()),
            // eventCRFAudits);
            // request.setAttribute("eventCRFAudits"+ss, eventCRFAudits);
        }

        // request.setAttribute("eventCRFAudits", eventCRFAuditsHashMap);
        request.setAttribute("events", eventsHashMap);
        request.setAttribute("studySubjectAudits", studySubjectAuditsHashMap);
        request.setAttribute("study", currentStudy);
        request.setAttribute("subjects", subjectHashMap);

        logger.warn("*** found servlet, sending to page ***");
        String pattn = "";
        String pattern2 = "";
        pattn = ResourceBundleProvider.getFormatBundle().getString("date_format_string");
        pattern2 = ResourceBundleProvider.getFormatBundle().getString("date_time_format_string");
        request.setAttribute("dateFormatPattern", pattn);
        request.setAttribute("dateTimeFormatPattern", pattern2);
        forwardPage(Page.AUDIT_LOG_STUDY);

    }

    /*
     * (non-Javadoc) Since access to this servlet is admin-only, restricts user
     * to see logs of specific users only @author thickerson
     * 
     * @see org.akaza.openclinica.control.core.SecureController#mayProceed()
     */
    @Override
    protected void mayProceed() throws InsufficientPermissionException {

        // <locale = request.getLocale();
        // <//<
        // resexception=ResourceBundle.getBundle("core.org.akaza.openclinica.i18n.exceptions",locale);
        // <//< respage =
        // ResourceBundle.getBundle("core.org.akaza.openclinica.i18n.page_messages",locale);
        // <//< resword =
        // ResourceBundle.getBundle("core.org.akaza.openclinica.i18n.words",locale);

        if (ub.isSysAdmin()) {
            return;
        }

        Role r = currentRole.getRole();
        if (r.equals(Role.STUDYDIRECTOR) || r.equals(Role.COORDINATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_director"), "1");
    }

}
