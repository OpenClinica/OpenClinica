/*
 *
 */
package org.akaza.openclinica.control.techadmin;

//
// import java.util.ArrayList;

import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.util.ArrayList;

/**
 * @author thickerson
 *
 *
 */
public class TechAdminServlet extends SecureController {

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.control.core.SecureController#processRequest()
     */
    @Override
    protected void processRequest() throws Exception {
        // find last 5 modifed studies
        StudyDAO sdao = new StudyDAO(sm.getDataSource());
        // ArrayList studies = (ArrayList) sdao.findAllByLimit(true);
        // request.setAttribute("studies", studies);
        ArrayList allStudies = (ArrayList) sdao.findAll();
        // request.setAttribute("allStudyNumber", new
        // Integer(allStudies.size()));

        UserAccountDAO udao = new UserAccountDAO(sm.getDataSource());
        // ArrayList users = (ArrayList) udao.findAllByLimit(true);
        // request.setAttribute("users", users);
        ArrayList allUsers = (ArrayList) udao.findAll();
        // request.setAttribute("allUserNumber", new Integer(allUsers.size()));

        SubjectDAO subdao = new SubjectDAO(sm.getDataSource());
        // ArrayList subjects = (ArrayList) subdao.findAllByLimit(true);
        // request.setAttribute("subjects", subjects);
        ArrayList allSubjects = (ArrayList) subdao.findAll();
        // request.setAttribute("allSubjectNumber", new
        // Integer(allSubjects.size()));

        CRFDAO cdao = new CRFDAO(sm.getDataSource());
        // ArrayList crfs = (ArrayList) cdao.findAllByLimit(true);
        // request.setAttribute("crfs", subjects);
        ArrayList allCrfs = (ArrayList) cdao.findAll();
        // request.setAttribute("allCrfNumber", new Integer(allCrfs.size()));

        resetPanel();

        panel.setStudyInfoShown(false);
        panel.setOrderedData(true);
        setToPanel(resword.getString("in_the_application"), "");
        if (allSubjects.size() > 0) {
            setToPanel(resword.getString("subjects"), new Integer(allSubjects.size()).toString());
        }
        if (allUsers.size() > 0) {
            setToPanel(resword.getString("users"), new Integer(allUsers.size()).toString());
        }
        if (allStudies.size() > 0) {
            setToPanel(resword.getString("studies"), new Integer(allStudies.size()).toString());
        }
        if (allCrfs.size() > 0) {
            setToPanel(resword.getString("CRFs"), new Integer(allCrfs.size()).toString());
        }
        forwardPage(Page.TECH_ADMIN_SYSTEM);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.control.core.SecureController#mayProceed()
     */
    @Override
    protected void mayProceed() throws InsufficientPermissionException {

        if (!ub.isTechAdmin()) {
            throw new InsufficientPermissionException(Page.MENU, resexception.getString("you_may_not_perform_technical_admin_functions"), "1");
        }

        return;
    }

    @Override
    protected String getAdminServlet() {
        return SecureController.ADMIN_SERVLET_CODE;
    }
}
