package org.akaza.openclinica.controller.helper;

import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.service.StudyConfigService;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.view.StudyInfoPanel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 *This class has been created from the existing SecureController to implement the
 * set-up code for the existing view-related JSPs (sidebars, etc.).
 */
public class SetUpStudyRole {
/*
    @Autowired
    @Qualifier("dataSource")*/
    private DataSource dataSource;

    public static final String STUDY_INFO_PANEL = "panel";

    public SetUpStudyRole(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setUp(HttpSession httpSession, UserAccountBean userAccountBean){

        StudyUserRoleBean currentRole = new StudyUserRoleBean();
        StudyBean currentStudy = new StudyBean();
        StudyInfoPanel panel = new StudyInfoPanel();

        StudyDAO sdao = new StudyDAO(dataSource);

        if (userAccountBean.getId() > 0 && userAccountBean.getActiveStudyId() > 0) {
            StudyParameterValueDAO spvdao = new StudyParameterValueDAO(dataSource);
            currentStudy = (StudyBean) sdao.findByPK(userAccountBean.getActiveStudyId());

            ArrayList studyParameters = spvdao.findParamConfigByStudy(currentStudy);

            currentStudy.setStudyParameters(studyParameters);

            StudyConfigService scs = new StudyConfigService(dataSource);
            if (currentStudy.getParentStudyId() <= 0) {// top study
                scs.setParametersForStudy(currentStudy);

            } else {
                // YW <<
                currentStudy.setParentStudyName(((StudyBean) sdao.findByPK(currentStudy.getParentStudyId())).getName());
                // YW >>
                scs.setParametersForSite(currentStudy);
            }

            // set up the panel here, tbh
            panel.reset();
            /*
            * panel.setData("Study", currentStudy.getName());
            * panel.setData("Summary", currentStudy.getSummary());
            * panel.setData("Start Date",
            * sdf.format(currentStudy.getDatePlannedStart()));
            * panel.setData("End Date",
            * sdf.format(currentStudy.getDatePlannedEnd()));
            * panel.setData("Principal Investigator",
            * currentStudy.getPrincipalInvestigator());
            */
            httpSession.setAttribute(STUDY_INFO_PANEL, panel);
        } else {
            currentStudy = new StudyBean();
        }
        httpSession.setAttribute("study", currentStudy);
        // YW 06-20-2007<< set site's parentstudy name when site is
        // restored
        if (currentStudy.getParentStudyId() > 0) {
            currentStudy.setParentStudyName(((StudyBean) sdao.findByPK(currentStudy.getParentStudyId())).getName());
        }
        // YW >>

        if (currentRole.getId() <= 0) {
            // if (ub.getId() > 0 && currentStudy.getId() > 0) {
            // if current study has been "removed", current role will be
            // kept as "invalid" -- YW 06-21-2007
            if (userAccountBean.getId() > 0 && currentStudy.getId() > 0 && !currentStudy.getStatus().getName().equals("removed")) {
                currentRole = userAccountBean.getRoleByStudy(currentStudy.getId());
                if (currentStudy.getParentStudyId() > 0) {
                    // Checking if currentStudy has been removed or not will
                    // ge good enough -- YW 10-17-2007
                    StudyUserRoleBean roleInParent = userAccountBean.getRoleByStudy(currentStudy.getParentStudyId());
                    // inherited role from parent study, pick the higher
                    // role
                    currentRole.setRole(Role.max(currentRole.getRole(), roleInParent.getRole()));
                }
                // logger.info("currentRole:" + currentRole.getRoleName());
            } else {
                currentRole = new StudyUserRoleBean();
            }
            httpSession.setAttribute("userRole", currentRole);
        }
        // YW << For the case that current role is not "invalid" but current
        // active study has been removed.
        else if (currentRole.getId() > 0 &&
                (currentStudy.getStatus().equals(Status.DELETED) ||
                        currentStudy.getStatus().equals(Status.AUTO_DELETED))) {
            currentRole.setRole(Role.INVALID);
            currentRole.setStatus(Status.DELETED);
            httpSession.setAttribute("userRole", currentRole);
        }


        StudyBean userRoleStudy = CoreResources.getPublicStudy(currentRole.getStudyId(), dataSource);

        if (userRoleStudy.getParentStudyId() > 0) {
            /*The Role decription will be set depending on whether the user logged in at
       study lever or site level. issue-2422*/
            List roles = Role.toArrayList();
            for (Iterator it = roles.iterator(); it.hasNext();) {
                Role role = (Role) it.next();
                switch (role.getId()) {
                    case 2:
                        role.setDescription("site_Study_Coordinator");
                        break;
                    case 3:
                        role.setDescription("site_Study_Director");
                        break;
                    case 4:
                        role.setDescription("site_investigator");
                        break;
                    case 5:
                        role.setDescription("site_Data_Entry_Person");
                        break;
                    case 6:
                        role.setDescription("site_monitor");
                        break;
                    case 7:
                        role.setDescription("site_Data_Entry_Person2");
                        break;
                    case 8:
                        role.setDescription("site_Data_Entry_Participant");
                        break;
                    default:
                        // logger.info("No role matched when setting role description");
                }
            }
        } else {
            /*If the current study is a site, we will change the role description. issue-2422*/
            List roles = Role.toArrayList();
            for (Iterator it = roles.iterator(); it.hasNext();) {
                Role role = (Role) it.next();
                switch (role.getId()) {
                    case 2:
                        role.setDescription("Study_Coordinator");
                        break;
                    case 3:
                        role.setDescription("Study_Director");
                        break;
                    case 4:
                        role.setDescription("investigator");
                        break;
                    case 5:
                        role.setDescription("Data_Entry_Person");
                        break;
                    case 6:
                        role.setDescription("monitor");
                        break;
                    default:
                        // logger.info("No role matched when setting role description");
                }
            }
        }


    }
}
