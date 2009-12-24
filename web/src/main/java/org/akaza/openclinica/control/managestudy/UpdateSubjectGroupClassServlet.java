/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import org.akaza.openclinica.bean.core.GroupClassType;
import org.akaza.openclinica.bean.core.NumericComparisonOperator;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.StudyGroupBean;
import org.akaza.openclinica.bean.managestudy.StudyGroupClassBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.managestudy.StudyGroupClassDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupDAO;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.util.ArrayList;
import java.util.Date;

/**
 * @author jxu
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class UpdateSubjectGroupClassServlet extends SecureController {
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        checkStudyLocked(Page.SUBJECT_GROUP_CLASS_LIST_SERVLET, respage.getString("current_study_locked"));
        if (ub.isSysAdmin()) {
            return;
        }
        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
            return;
        }
        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + "\n" + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.SUBJECT_GROUP_CLASS_LIST_SERVLET, resexception.getString("not_study_director"), "1");

    }

    @Override
    public void processRequest() throws Exception {
        String action = request.getParameter("action");
        FormProcessor fp = new FormProcessor(request);
        int classId = fp.getInt("id");

        if (classId == 0) {

            addPageMessage(respage.getString("please_choose_a_subject_group_class_to_edit"));
            forwardPage(Page.SUBJECT_GROUP_CLASS_LIST_SERVLET);
        } else {
            StudyGroupClassDAO sgcdao = new StudyGroupClassDAO(sm.getDataSource());
            StudyGroupDAO sgdao = new StudyGroupDAO(sm.getDataSource());

            if (!fp.isSubmitted()) {
                StudyGroupClassBean sgcb = (StudyGroupClassBean) sgcdao.findByPK(classId);

                ArrayList groups = sgdao.findAllByGroupClass(sgcb);
                request.setAttribute("groupTypes", GroupClassType.toArrayList());
                session.setAttribute("group", sgcb);
                session.setAttribute("studyGroups", groups);
                forwardPage(Page.UPDATE_SUBJECT_GROUP_CLASS);
            } else {
                if (action.equalsIgnoreCase("confirm")) {
                    confirmGroup();
                } else if (action.equalsIgnoreCase("submit")) {
                    submitGroup();
                } else {
                    addPageMessage(respage.getString("no_action_specified"));
                    forwardPage(Page.SUBJECT_GROUP_CLASS_LIST_SERVLET);
                }
            }

        }

    }

    /**
     * Validates the first section of study and save it into study bean
     *
     * @param request
     * @param response
     * @throws Exception
     */
    private void confirmGroup() throws Exception {
        Validator v = new Validator(request);
        FormProcessor fp = new FormProcessor(request);

        v.addValidation("name", Validator.NO_BLANKS);
        // v.addValidation("groupClassTypeId", Validator.IS_AN_INTEGER);
        v.addValidation("subjectAssignment", Validator.NO_BLANKS);

        v.addValidation("name", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 30);
        v.addValidation("subjectAssignment", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 30);

        ArrayList studyGroups = new ArrayList();
        for (int i = 0; i < 10; i++) {
            String name = fp.getString("studyGroup" + i);
            int studyGroupId = fp.getInt("studyGroupId" + i);
            String description = fp.getString("studyGroupDescription" + i);
            if (!StringUtil.isBlank(name)) {
                StudyGroupBean g = new StudyGroupBean();
                g.setName(name);
                g.setDescription(description);
                g.setId(studyGroupId);
                studyGroups.add(g);
                if (name.length() > 255) {
                    request.setAttribute("studyGroupError", resexception.getString("group_name_cannot_be_more_255"));
                    break;
                }
                if (description.length() > 1000) {
                    request.setAttribute("studyGroupError", resexception.getString("group_description_cannot_be_more_100"));
                    break;
                }
            }

        }

        errors = v.validate();
        if (fp.getInt("groupClassTypeId") == 0) {
            Validator.addError(errors, "groupClassTypeId", "Group Class Type is required.");
        }

        StudyGroupClassBean group = (StudyGroupClassBean) session.getAttribute("group");
        group.setName(fp.getString("name"));
        group.setGroupClassTypeId(fp.getInt("groupClassTypeId"));
        group.setSubjectAssignment(fp.getString("subjectAssignment"));

        session.setAttribute("group", group);
        session.setAttribute("studyGroups", studyGroups);

        if (errors.isEmpty()) {
            logger.info("no errors in the first section");
            group.setGroupClassTypeName(GroupClassType.get(group.getGroupClassTypeId()).getName());

            forwardPage(Page.UPDATE_SUBJECT_GROUP_CLASS_CONFIRM);

        } else {
            logger.info("has validation errors in the first section");
            request.setAttribute("formMessages", errors);
            request.setAttribute("groupTypes", GroupClassType.toArrayList());

            forwardPage(Page.UPDATE_SUBJECT_GROUP_CLASS);
        }

    }

    private void submitGroup() throws OpenClinicaException {
        StudyGroupClassBean group = (StudyGroupClassBean) session.getAttribute("group");
        ArrayList studyGroups = (ArrayList) session.getAttribute("studyGroups");
        ArrayList newStudyGroups = (ArrayList) session.getAttribute("newStudyGroups");
        StudyGroupClassDAO sgcdao = new StudyGroupClassDAO(sm.getDataSource());
        group.setUpdater(ub);
        group.setUpdatedDate(new Date());
        group = (StudyGroupClassBean) sgcdao.update(group);

        if (!group.isActive()) {
            addPageMessage(respage.getString("the_subject_group_class_no_updated_database"));
        } else {
            StudyGroupDAO sgdao = new StudyGroupDAO(sm.getDataSource());
            for (int i = 0; i < studyGroups.size(); i++) {
                StudyGroupBean sg = (StudyGroupBean) studyGroups.get(i);
                sg.setStudyGroupClassId(group.getId());
                if (sg.getId() == 0) {

                    sg.setOwner(ub);
                    sg.setStatus(Status.AVAILABLE);

                    sgdao.create(sg);
                } else {

                    sg.setUpdater(ub);
                    sg.setStatus(Status.AVAILABLE);

                    sgdao.update(sg);
                }

            }
            addPageMessage(respage.getString("the_subject_group_class_updated_succesfully"));
        }
        forwardPage(Page.SUBJECT_GROUP_CLASS_LIST_SERVLET);

    }

}
