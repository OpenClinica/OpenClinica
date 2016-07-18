/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.extract.DatasetBean;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyGroupBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.SubjectGroupMapBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.extract.DatasetDAO;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.*;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.SubjectGroupMapDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Restores a removed site and all its data, including users. roles, study
 * groups, definitions, events and items
 * 
 * @author jxu
 * 
 */
public class RestoreSiteServlet extends SecureController {
    /**
     *
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        checkStudyLocked(Page.SITE_LIST_SERVLET, respage.getString("current_study_locked"));
        if (ub.isSysAdmin()) {
            return;
        }

        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.SITE_LIST_SERVLET, resexception.getString("not_study_director"), "1");

    }

    @Override
    public void processRequest() throws Exception {

        StudyDAO sdao = new StudyDAO(sm.getDataSource());
        String idString = request.getParameter("id");
        logger.info("site id:" + idString);

        int siteId = Integer.valueOf(idString.trim()).intValue();
        StudyBean study = (StudyBean) sdao.findByPK(siteId);

        // find all user and roles
        UserAccountDAO udao = new UserAccountDAO(sm.getDataSource());
        ArrayList userRoles = udao.findAllByStudyId(siteId);

        // find all subjects
        StudySubjectDAO ssdao = new StudySubjectDAO(sm.getDataSource());
        ArrayList subjects = ssdao.findAllByStudy(study);

        // find all events
        StudyEventDefinitionDAO sefdao = new StudyEventDefinitionDAO(sm.getDataSource());
        ArrayList definitions = sefdao.findAllByStudy(study);

        String action = request.getParameter("action");
        if (StringUtil.isBlank(idString)) {
            addPageMessage(respage.getString("please_choose_a_site_to_restore"));
            forwardPage(Page.SITE_LIST_SERVLET);
        } else {
            if ("confirm".equalsIgnoreCase(action)) {
                // site can be restored when its parent study is not "removed"
                // -- YW -6-21-2007
                StudyBean parentstudy = (StudyBean) sdao.findByPK(study.getParentStudyId());
                if (!"removed".equals(parentstudy.getStatus().getName())) {
                    request.setAttribute("siteToRestore", study);

                    request.setAttribute("userRolesToRestore", userRoles);

                    request.setAttribute("subjectsToRestore", subjects);

                    // request.setAttribute("definitionsToRestore",
                    // definitions);
                } else {
                    MessageFormat mf = new MessageFormat("");
                    mf.applyPattern(respage.getString("choosen_site_cannot_restored"));
                    Object[] arguments = { study.getName(), parentstudy.getName() };
                    addPageMessage(mf.format(arguments));
                    forwardPage(Page.STUDY_LIST_SERVLET);
                }
                forwardPage(Page.RESTORE_SITE);
            } else {
                logger.info("submit to restore the site");
                // change all statuses to unavailable
                StudyDAO studao = new StudyDAO(sm.getDataSource());
                study.setStatus(study.getOldStatus());
                study.setUpdater(ub);
                study.setUpdatedDate(new Date());
                studao.update(study);

                // restore all users and roles
                for (int i = 0; i < userRoles.size(); i++) {
                    StudyUserRoleBean role = (StudyUserRoleBean) userRoles.get(i);
                    if (role.getStatus().equals(Status.AUTO_DELETED)) {
                        role.setStatus(Status.AVAILABLE);
                        role.setUpdater(ub);
                        role.setUpdatedDate(new Date());
                        // YW << So study_user_role table status_id field can be
                        // updated
                        udao.updateStudyUserRole(role, role.getUserName());
                    }
                    // YW 06-18-2007 >>
                }

                // YW 06-19-2007 << Meanwhile update current active study
                // attribute of session if restored study is current active
                // study
                if (study.getId() == currentStudy.getId()) {
                    currentStudy.setStatus(Status.AVAILABLE);

                    StudyUserRoleBean r = (new UserAccountDAO(sm.getDataSource())).findRoleByUserNameAndStudyId(ub.getName(), currentStudy.getId());
                    StudyUserRoleBean rInParent =
                        (new UserAccountDAO(sm.getDataSource())).findRoleByUserNameAndStudyId(ub.getName(), currentStudy.getParentStudyId());
                    // according to logic in SecureController.java: inherited
                    // role from parent study, pick the higher role
                    currentRole.setRole(Role.max(r.getRole(), rInParent.getRole()));
                }
                // YW >>

                // restore all study_group
                StudyGroupDAO sgdao = new StudyGroupDAO(sm.getDataSource());
                SubjectGroupMapDAO sgmdao = new SubjectGroupMapDAO(sm.getDataSource());
                ArrayList groups = sgdao.findAllByStudy(study);
                for (int i = 0; i < groups.size(); i++) {
                    StudyGroupBean group = (StudyGroupBean) groups.get(i);
                    if (group.getStatus().equals(Status.AUTO_DELETED)) {
                        group.setStatus(Status.AVAILABLE);
                        group.setUpdater(ub);
                        group.setUpdatedDate(new Date());
                        sgdao.update(group);
                        // all subject_group_map
                        ArrayList subjectGroupMaps = sgmdao.findAllByStudyGroupId(group.getId());
                        for (int j = 0; j < subjectGroupMaps.size(); j++) {
                            SubjectGroupMapBean sgMap = (SubjectGroupMapBean) subjectGroupMaps.get(j);
                            if (sgMap.getStatus().equals(Status.AUTO_DELETED)) {
                                sgMap.setStatus(Status.AVAILABLE);
                                sgMap.setUpdater(ub);
                                sgMap.setUpdatedDate(new Date());
                                sgmdao.update(sgMap);
                            }
                        }
                    }
                }

                // restore all events with subjects
                EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(sm.getDataSource());
                StudyEventDAO sedao = new StudyEventDAO(sm.getDataSource());
                for (int i = 0; i < subjects.size(); i++) {
                    StudySubjectBean subject = (StudySubjectBean) subjects.get(i);
                    if (subject.getStatus().equals(Status.AUTO_DELETED)) {
                        subject.setStatus(Status.AVAILABLE);
                        subject.setUpdater(ub);
                        subject.setUpdatedDate(new Date());
                        ssdao.update(subject);

                        ArrayList events = sedao.findAllByStudySubject(subject);
                        EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());

                        for (int j = 0; j < events.size(); j++) {
                            StudyEventBean event = (StudyEventBean) events.get(j);
                            if (event.getStatus().equals(Status.AUTO_DELETED)) {
                                event.setStatus(Status.AVAILABLE);
                                event.setUpdater(ub);
                                event.setUpdatedDate(new Date());
                                sedao.update(event);

                                ArrayList eventCRFs = ecdao.findAllByStudyEvent(event);

                                ItemDataDAO iddao = new ItemDataDAO(sm.getDataSource());
                                for (int k = 0; k < eventCRFs.size(); k++) {
                                    // YW << fix broken page for storing site
                                    EventCRFBean eventCRF = (EventCRFBean) eventCRFs.get(k);
                                    // >> YW
                                    if (eventCRF.getStatus().equals(Status.AUTO_DELETED)) {
                                        eventCRF.setStatus(eventCRF.getOldStatus());
                                        eventCRF.setUpdater(ub);
                                        eventCRF.setUpdatedDate(new Date());
                                        ecdao.update(eventCRF);

                                        ArrayList itemDatas = iddao.findAllByEventCRFId(eventCRF.getId());
                                        for (int a = 0; a < itemDatas.size(); a++) {
                                            ItemDataBean item = (ItemDataBean) itemDatas.get(a);
                                            if (item.getStatus().equals(Status.AUTO_DELETED)) {
                                                item.setStatus(item.getOldStatus());
                                                item.setUpdater(ub);
                                                item.setUpdatedDate(new Date());
                                                iddao.update(item);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }// for subjects

                DatasetDAO datadao = new DatasetDAO(sm.getDataSource());
                ArrayList dataset = datadao.findAllByStudyId(study.getId());
                for (int i = 0; i < dataset.size(); i++) {
                    DatasetBean data = (DatasetBean) dataset.get(i);
                    data.setStatus(Status.AVAILABLE);
                    data.setUpdater(ub);
                    data.setUpdatedDate(new Date());
                    datadao.update(data);
                }

                addPageMessage(respage.getString("this_site_has_been_restored_succesfully"));
                String fromListSite = (String) session.getAttribute("fromListSite");
                if (fromListSite != null && fromListSite.equals("yes") && currentRole.getRole().equals(Role.STUDYDIRECTOR)) {
                    session.removeAttribute("fromListSite");
                    forwardPage(Page.SITE_LIST_SERVLET);
                } else {
                    session.removeAttribute("fromListSite");
                    if (currentRole.getRole().equals(Role.ADMIN)) {
                        forwardPage(Page.STUDY_LIST_SERVLET);
                    } else {
                        forwardPage(Page.SITE_LIST_SERVLET);
                    }
                }

            }

        }
    }

}
