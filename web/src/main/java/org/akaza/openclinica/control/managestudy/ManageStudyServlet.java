/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Generates the index page of manage study module
 *
 * @author ssachs
 */
public class ManageStudyServlet extends SecureController {

    Locale locale;
    public final List<String> INSTRUCTIONS = new ArrayList<String>();

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.control.core.SecureController#processRequest()
     */
    @Override
    protected void processRequest() throws Exception {
        // for the sake of testing a prototype; 2560
        if (!INSTRUCTIONS.isEmpty()) {
            INSTRUCTIONS.clear();
        }
        INSTRUCTIONS.add(restext.getString("director_coordinator_privileges_manage"));
        INSTRUCTIONS.add(restext.getString("side_tables_shows_last_modified"));
        request.setAttribute("instructions", INSTRUCTIONS);
        // show icon keys on the sidebar, and display the instructions and
        // alert messages fields
        request.setAttribute("showIcons", true);
        request.setAttribute("openIcons", true);
        request.setAttribute("openAlerts", true);
        request.setAttribute("openInstructions", true);

        // find last 5 modifed sites
        StudyDAO sdao = new StudyDAO(sm.getDataSource());
        // ArrayList sites = (ArrayList)
        // sdao.findAllByParentAndLimit(currentStudy.getId(),true);
        ArrayList allSites = (ArrayList) sdao.findAllByParent(currentStudy.getId());
        ArrayList sites = new ArrayList();
        for (int i = 0; i < allSites.size(); i++) {
            sites.add(allSites.get(i));
            if (i == 5) {
                break;
            }
        }
        request.setAttribute("sites", sites);
        request.setAttribute("sitesCount", new Integer(sites.size()));
        request.setAttribute("allSitesCount", new Integer(allSites.size()));
        // BWP 3057: add study name to JSP
        if (currentStudy != null) {
            request.setAttribute("studyIdentifier", currentStudy.getIdentifier());
        }

        StudyEventDefinitionDAO edao = new StudyEventDefinitionDAO(sm.getDataSource());
        ArrayList seds = (ArrayList) edao.findAllByStudyAndLimit(currentStudy.getId());
        ArrayList allSeds = edao.findAllByStudy(currentStudy);
        request.setAttribute("seds", seds);
        request.setAttribute("sedsCount", new Integer(seds.size()));
        request.setAttribute("allSedsCount", new Integer(allSeds.size()));

        UserAccountDAO udao = new UserAccountDAO(sm.getDataSource());
        ArrayList users = udao.findAllUsersByStudyIdAndLimit(currentStudy.getId(), true);
        ArrayList allUsers = udao.findAllUsersByStudy(currentStudy.getId());
        request.setAttribute("users", users);
        request.setAttribute("usersCount", new Integer(users.size()));
        request.setAttribute("allUsersCount", new Integer(allUsers.size()));

        StudySubjectDAO ssdao = new StudySubjectDAO(sm.getDataSource());
        // ArrayList subjects = (ArrayList)
        // ssdao.findAllByStudyIdAndLimit(currentStudy.getId(),true);
        ArrayList allSubjects = ssdao.findAllByStudyId(currentStudy.getId());
        ArrayList subjects = new ArrayList();
        for (int i = 0; i < allSubjects.size(); i++) {
            subjects.add(allSubjects.get(i));
            if (i == 5) {
                break;
            }
        }
        request.setAttribute("subs", subjects);
        request.setAttribute("subsCount", new Integer(subjects.size()));
        request.setAttribute("allSubsCount", new Integer(allSubjects.size()));

        // added tbh, 9-21-2005
        // AuditEventDAO aedao = new AuditEventDAO(sm.getDataSource());
        // ArrayList audits = (ArrayList)
        // aedao.findAllByStudyIdAndLimit(currentStudy.getId());
        // request.setAttribute("audits", audits);
        resetPanel();

        if (allSubjects.size() > 0) {
            setToPanel("Subjects", new Integer(allSubjects.size()).toString());
        }
        if (allUsers.size() > 0) {
            setToPanel("Users", new Integer(allUsers.size()).toString());
        }
        if (allSites.size() > 0) {
            setToPanel("Sites", new Integer(allSites.size()).toString());
        }
        if (allSeds.size() > 0) {
            setToPanel("Event Definitions", new Integer(allSeds.size()).toString());
        }
        String proto = request.getParameter("proto");
        if (proto == null || "".equalsIgnoreCase(proto)) {
            forwardPage(Page.MANAGE_STUDY);
        } else {
            forwardPage(Page.MANAGE_STUDY_BODY);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.control.core.SecureController#mayProceed()
     */
    /**
     * Checks whether the user has the correct privilege
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        locale = LocaleResolver.getLocale(request);
        // < restext =
        // ResourceBundle.getBundle("org.akaza.openclinica.i18n.notes",locale);
        // < respage =
        // ResourceBundle.getBundle("org.akaza.openclinica.i18n.page_messages",locale);
        // <
        // resexception=ResourceBundle.getBundle("org.akaza.openclinica.i18n.exceptions",locale);

        if (ub.isSysAdmin()) {
            return;
        }

        Role r = currentRole.getRole();
        if (r.equals(Role.STUDYDIRECTOR) || r.equals(Role.COORDINATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, restext.getString("not_study_director"), "1");// TODO
    }

}
