/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.util.ArrayList;
import java.util.Locale;

/**
 * @author ssachs
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class AdminSystemServlet extends SecureController {

    Locale locale;

    // < ResourceBundleresword,resexception;
    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.control.core.SecureController#processRequest()
     */
    @Override
    protected void processRequest() throws Exception {

        // find last 5 modifed studies
        StudyDAO sdao = new StudyDAO(sm.getDataSource());
        ArrayList studies = (ArrayList) sdao.findAllByLimit(true);
        request.setAttribute("studies", studies);
        ArrayList allStudies = (ArrayList) sdao.findAll();
        request.setAttribute("allStudyNumber", new Integer(allStudies.size()));

        UserAccountDAO udao = new UserAccountDAO(sm.getDataSource());
        ArrayList users = (ArrayList) udao.findAllByLimit(true);
        request.setAttribute("users", users);
        ArrayList allUsers = (ArrayList) udao.findAll();
        request.setAttribute("allUserNumber", new Integer(allUsers.size()));

        SubjectDAO subdao = new SubjectDAO(sm.getDataSource());
        ArrayList subjects = (ArrayList) subdao.findAllByLimit(true);
        request.setAttribute("subjects", subjects);
        ArrayList allSubjects = (ArrayList) subdao.findAll();
        request.setAttribute("allSubjectNumber", new Integer(allSubjects.size()));

        CRFDAO cdao = new CRFDAO(sm.getDataSource());
        ArrayList crfs = (ArrayList) cdao.findAllByLimit(true);
        request.setAttribute("crfs", crfs);
        ArrayList allCrfs = (ArrayList) cdao.findAll();
        request.setAttribute("allCrfNumber", new Integer(allCrfs.size()));

        resetPanel();
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

        panel.setStudyInfoShown(false);
        forwardPage(Page.ADMIN_SYSTEM);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.control.core.SecureController#mayProceed()
     */
    @Override
    protected void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);
        // < resword =
        // ResourceBundle.getBundle("org.akaza.openclinica.i18n.words",locale);
        // <
        // resexception=ResourceBundle.getBundle("org.akaza.openclinica.i18n.exceptions",locale);

        if (!ub.isSysAdmin()) {
            throw new InsufficientPermissionException(Page.MENU, "You may not perform administrative functions", "1");
        }

        return;
    }

    @Override
    protected String getAdminServlet() {
        return SecureController.ADMIN_SERVLET_CODE;
    }
}
