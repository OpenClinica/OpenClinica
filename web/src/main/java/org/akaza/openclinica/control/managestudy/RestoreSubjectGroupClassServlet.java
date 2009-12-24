/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.StudyGroupBean;
import org.akaza.openclinica.bean.managestudy.StudyGroupClassBean;
import org.akaza.openclinica.bean.submit.SubjectGroupMapBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.managestudy.StudyGroupClassDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupDAO;
import org.akaza.openclinica.dao.submit.SubjectGroupMapDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.util.ArrayList;

/**
 * @author jxu
 *
 * Restores a removed subject group class
 */
public class RestoreSubjectGroupClassServlet extends SecureController {
    /**
     *
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        checkStudyLocked(Page.SUBJECT_GROUP_CLASS_LIST_SERVLET, respage.getString("current_study_locked"));
        if (ub.isSysAdmin()) {
            return;
        }

        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.SUBJECT_GROUP_CLASS_LIST_SERVLET, resexception.getString("not_study_director"), "1");

    }

    @Override
    public void processRequest() throws Exception {
        String action = request.getParameter("action");
        FormProcessor fp = new FormProcessor(request);
        int classId = fp.getInt("id");

        if (classId == 0) {

            addPageMessage(respage.getString("please_choose_a_subject_group_class_to_restore"));
            forwardPage(Page.SUBJECT_GROUP_CLASS_LIST_SERVLET);
        } else {
            StudyGroupClassDAO sgcdao = new StudyGroupClassDAO(sm.getDataSource());
            StudyGroupDAO sgdao = new StudyGroupDAO(sm.getDataSource());
            SubjectGroupMapDAO sgmdao = new SubjectGroupMapDAO(sm.getDataSource());

            if (action.equalsIgnoreCase("confirm")) {
                StudyGroupClassBean sgcb = (StudyGroupClassBean) sgcdao.findByPK(classId);
                if (sgcb.getStatus().equals(Status.AVAILABLE)) {
                    addPageMessage(respage.getString("this_subject_group_class_is_available_cannot_restore"));
                    forwardPage(Page.SUBJECT_GROUP_CLASS_LIST_SERVLET);
                    return;
                }

                ArrayList groups = sgdao.findAllByGroupClass(sgcb);

                for (int i = 0; i < groups.size(); i++) {
                    StudyGroupBean sg = (StudyGroupBean) groups.get(i);
                    ArrayList subjectMaps = sgmdao.findAllByStudyGroupClassAndGroup(sgcb.getId(), sg.getId());
                    sg.setSubjectMaps(subjectMaps);

                }

                session.setAttribute("group", sgcb);
                session.setAttribute("studyGroups", groups);
                forwardPage(Page.RESTORE_SUBJECT_GROUP_CLASS);

            } else if (action.equalsIgnoreCase("submit")) {
                StudyGroupClassBean group = (StudyGroupClassBean) session.getAttribute("group");
                group.setStatus(Status.AVAILABLE);
                group.setUpdater(ub);
                sgcdao.update(group);

                ArrayList subjectMaps = sgmdao.findAllByStudyGroupClassId(group.getId());
                for (int i = 0; i < subjectMaps.size(); i++) {
                    SubjectGroupMapBean sgmb = (SubjectGroupMapBean) subjectMaps.get(i);
                    if (sgmb.getStatus().equals(Status.AUTO_DELETED)) {
                        sgmb.setStatus(Status.AVAILABLE);
                        sgmb.setUpdater(ub);
                        sgmdao.update(sgmb);
                    }
                }
                addPageMessage(respage.getString("this_subject_group_class_was_restored_succesfully"));
                forwardPage(Page.SUBJECT_GROUP_CLASS_LIST_SERVLET);
            } else {
                addPageMessage(respage.getString("no_action_specified"));
                forwardPage(Page.SUBJECT_GROUP_CLASS_LIST_SERVLET);
            }

        }
    }

}
