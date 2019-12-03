/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import core.org.akaza.openclinica.bean.admin.DisplayStudyBean;
import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.bean.core.Status;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.bean.submit.SubjectBean;
import core.org.akaza.openclinica.bean.submit.SubjectGroupMapBean;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import core.org.akaza.openclinica.core.form.StringUtil;
import core.org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import core.org.akaza.openclinica.dao.submit.SubjectDAO;
import core.org.akaza.openclinica.dao.submit.SubjectGroupMapDAO;
import org.akaza.openclinica.view.Page;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author jxu
 *
 * Assigns a study subject to another study
 */
public class ReassignStudySubjectServlet extends SecureController {
    /**
     *
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        checkStudyLocked(Page.LIST_STUDY_SUBJECTS, respage.getString("current_study_locked"));
        checkStudyFrozen(Page.LIST_STUDY_SUBJECTS, respage.getString("current_study_frozen"));
        if (ub.isSysAdmin()) {
            return;
        }

        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_study_director"), "1");

    }

    @Override
    public void processRequest() throws Exception {
        String action = request.getParameter("action");
        StudySubjectDAO ssdao = new StudySubjectDAO(sm.getDataSource());
        SubjectDAO subdao = new SubjectDAO(sm.getDataSource());
        FormProcessor fp = new FormProcessor(request);

        int studySubId = fp.getInt("id");
        if (studySubId == 0) {
            addPageMessage(respage.getString("please_choose_a_subject_to_reassign"));
            forwardPage(Page.LIST_STUDY_SUBJECTS_SERVLET);
            return;
        } else {
            StudySubjectBean studySub = (StudySubjectBean) ssdao.findByPK(studySubId);
            int subjectId = studySub.getSubjectId();
            request.setAttribute("studySub", studySub);
            SubjectBean subject = (SubjectBean) subdao.findByPK(subjectId);
            request.setAttribute("subject", subject);

            SubjectGroupMapDAO sgmdao = new SubjectGroupMapDAO(sm.getDataSource());
            ArrayList groupMaps = (ArrayList) sgmdao.findAllByStudySubject(studySubId);

            if (StringUtil.isBlank(action)) {
                ArrayList studies = null;
                DisplayStudyBean displayStudy = new DisplayStudyBean();
                Study study = (Study) getStudyDao().findByPK(studySub.getStudyId());
                if (study.isSite()) {// current in site
                    studies = (ArrayList) getStudyDao().findAllByParent(study.getStudy().getStudyId());
                    Study parent = (Study) getStudyDao().findByPK(study.getStudy().getStudyId());
                    displayStudy.setParent(parent);
                    // studies.add(parent);
                    displayStudy.setChildren(studies);
                } else {
                    studies = (ArrayList) getStudyDao().findAllByParent(study.getStudyId());
                    displayStudy.setParent(study);
                    displayStudy.setChildren(studies);
                    // studies.add(study);
                }
                // request.setAttribute("studies", studies);
                request.setAttribute("displayStudy", displayStudy);
                forwardPage(Page.REASSIGN_STUDY_SUBJECT);
            } else {
                int studyId = fp.getInt("studyId");
                if (studyId == 0) {
                    addPageMessage(respage.getString("please_choose_a_study_site_to_reassign_the_subject"));
                    forwardPage(Page.REASSIGN_STUDY_SUBJECT);
                    return;
                }
                Study st = (Study) getStudyDao().findByPK(studyId);
                if ("confirm".equalsIgnoreCase(action)) {
                    StudySubjectBean sub1 = (StudySubjectBean) ssdao.findAnotherBySameLabel(studySub.getLabel(), studyId, studySub.getId());
                    if (sub1.getId() > 0) {
                        addPageMessage(respage.getString("the_study_subject_ID_used_by_another_in_study_site"));
                        forwardPage(Page.REASSIGN_STUDY_SUBJECT);
                        return;
                    }
                    // YW << comment out this message
                    // if (groupMaps.size() > 0) {
                    // addPageMessage("Warning: This subject has Group data
                    // assoicated with current study,"
                    // + "the group data will be lost if it is reassigned to
                    // another study.");
                    // }
                    // YW >>

                    request.setAttribute("newStudy", st);
                    forwardPage(Page.REASSIGN_STUDY_SUBJECT_CONFIRM);
                } else {

                    int subjectCount = getSubjectCount(st);

                    st.setSubjectCount(subjectCount+1);
                    getStudyDao().update(st);

                    logger.info("submit to reassign the subject");
                    studySub.setUpdatedDate(new Date());
                    studySub.setUpdater(ub);
                    studySub.setStudyId(studyId);
                    ssdao.update(studySub);

                    for (int i = 0; i < groupMaps.size(); i++) {
                        SubjectGroupMapBean sgm = (SubjectGroupMapBean) groupMaps.get(i);
                        sgm.setUpdatedDate(new Date());
                        sgm.setUpdater(ub);
                        sgm.setStatus(Status.DELETED);
                        sgmdao.update(sgm);
                    }
                    MessageFormat mf = new MessageFormat("");
                    mf.applyPattern(respage.getString("subject_reassigned"));
                    Object[] arguments = { studySub.getLabel(), st.getName() };
                    addPageMessage(mf.format(arguments));
                    forwardPage(Page.LIST_STUDY_SUBJECTS_SERVLET);

                }

            }
        }
    }

}
