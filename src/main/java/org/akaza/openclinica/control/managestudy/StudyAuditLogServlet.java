/*
 * Created on Sep 21, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.akaza.openclinica.control.managestudy;

import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import core.org.akaza.openclinica.dao.admin.AuditDAO;
import core.org.akaza.openclinica.dao.admin.CRFDAO;
import core.org.akaza.openclinica.dao.login.UserAccountDAO;
import core.org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import core.org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import core.org.akaza.openclinica.dao.submit.CRFVersionDAO;
import core.org.akaza.openclinica.dao.submit.EventCRFDAO;
import core.org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.view.Page;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Locale;

/**
 * @author thickerson
 * 
 * 
 */
public class StudyAuditLogServlet extends SecureController {

    @Autowired
    private StudySubjectDAO studySubjectDAO;
    @Autowired
    private SubjectDAO subjectDAO;
    @Autowired
    UserAccountDAO userAccountDAO;

    public static String getLink(int userId) {
        return "AuditLogStudy";
    }

    /*
     * (non-Javadoc) Assume that we get the user id automatically. We will jump
     * from the edit user page if the user is an admin, they can get to see the
     * users' log
     * @see org.akaza.openclinica.control.core.SecureController#processRequest()
     */

    /*
     * (non-Javadoc) redo this servlet to run the audits per study subject for
     * the study; need to add a studyId param and then use the
     * StudySubjectDAO.findAllByStudyOrderByLabel() method to grab a lot of
     * study subject beans and then return them much like in
     * ViewStudySubjectAuditLogServet.process() currentStudy instead of studyId?
     */
    @Override
    protected void processRequest() throws Exception {

        FormProcessor fp = new FormProcessor(request);

        StudyAuditLogTableFactory factory = new StudyAuditLogTableFactory();
        factory.setSubjectDao(subjectDAO);
        factory.setStudySubjectDao(studySubjectDAO);
        factory.setUserAccountDao(userAccountDAO);
        factory.setCurrentStudy(currentStudy);

        String auditLogsHtml = factory.createTable(request, response).render();
        request.setAttribute("auditLogsHtml", auditLogsHtml);

        forwardPage(Page.AUDIT_LOGS_STUDY);

    }

    /*
     * (non-Javadoc) Since access to this servlet is admin-only, restricts user
     * to see logs of specific users only @author thickerson
     * @see org.akaza.openclinica.control.core.SecureController#mayProceed()
     */
    @Override
    protected void mayProceed() throws InsufficientPermissionException {

        if (ub.isSysAdmin()) {
            return;
        }

        Role r = currentRole.getRole();
        if (r.equals(Role.STUDYDIRECTOR) || r.equals(Role.COORDINATOR) || r.equals(Role.MONITOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_director"), "1");
    }

}
