/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2010-2011 Akaza Research

 * Development of this web service or portions thereof has been funded
 * by Federal Funds from the National Cancer Institute, 
 * National Institutes of Health, under Contract No. HHSN261200800001E.
 * In addition to the GNU LGPL license, this code is also available
 * from NCI CBIIT repositories under the terms of the caBIG Software License. 
 * For details see: https://cabig.nci.nih.gov/adopt/caBIGModelLicense
 */
package org.akaza.openclinica.ws.logic;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.extract.DatasetBean;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudyGroupClassBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.SubjectGroupMapBean;
import org.akaza.openclinica.dao.extract.DatasetDAO;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupClassDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.SubjectGroupMapDAO;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Date;

public class CreateStudyService {

    private final String CONNECTOR_NAMESPACE_V1 = "http://clinicalconnector.nci.nih.gov";

    public CreateStudyService() {

    }

    public StudyBean generateStudyBean(UserAccountBean user, Node study) throws Exception {
        StudyBean studyBean = new StudyBean();
        studyBean.setOwner(user);
        DomParsingService xmlService = new DomParsingService();
        // studyBean.setIdentifier(xmlService.getElementValue(study, CONNECTOR_NAMESPACE_V1, "identifier", "extension"));
        studyBean.setStatus(Status.AVAILABLE);// coordinatingCenterStudyStatusCode?
        studyBean.setName(xmlService.getElementValue(study, this.CONNECTOR_NAMESPACE_V1, "publicTitle", "value"));
        studyBean.setOfficialTitle(xmlService.getElementValue(study, this.CONNECTOR_NAMESPACE_V1, "officialTitle", "value"));
        // set secondary identifier below, in getStudyIdentifier
        studyBean.setProtocolDescription(xmlService.getElementValue(study, this.CONNECTOR_NAMESPACE_V1, "scientificDescription", "value"));
        studyBean.setPhase(xmlService.getElementValue(study, this.CONNECTOR_NAMESPACE_V1, "phaseCode", "code"));
        // tbh 7619
        studyBean.setPurpose(xmlService.getElementValue(study, this.CONNECTOR_NAMESPACE_V1, "primaryPurposeCode", "code"));
        // going back to this one
        studyBean.setSummary(xmlService.getElementValue(study, this.CONNECTOR_NAMESPACE_V1, "publicDescription", "value"));
        // if there is a null value, set to null instead of the value code
        if (studyBean.getSummary().length() <= 2) {
            // assumes two-character code
            studyBean.setSummary("");
        }
        studyBean.setSecondaryIdentifier(xmlService.getElementValue(study, this.CONNECTOR_NAMESPACE_V1, "publicTitle", "value"));
        int enrollment = xmlService.getTargetAccrualNumberRange(study);// xmlService.getElementValue(study, this.CONNECTOR_NAMESPACE_V1, xmlLine, attrName)
        studyBean.setExpectedTotalEnrollment(enrollment);
        // System.out.println("found enrollment " + enrollment);
        studyBean = xmlService.getStudyInvestigator(studyBean, study);
        studyBean = xmlService.getStudyCenter(studyBean, study);
        studyBean = xmlService.getSponsorName(studyBean, study);
        studyBean = xmlService.getStudyIdentifier(studyBean, study);
        // ArrayList<StudyBean> sites = xmlService.getSites(studyBean, study);
        return studyBean;
    }

    public ArrayList<StudyBean> generateSites(UserAccountBean user, StudyBean parent, Node study) throws Exception {
        DomParsingService xmlService = new DomParsingService();
        // above dry?
        ArrayList<StudyBean> sites = xmlService.getSites(parent, study);
        for (StudyBean site : sites) {
            site.setOwner(user);
            site.setStatus(Status.AVAILABLE);
        }
        return sites;
    }

    public StudyBean changeStatus(Status oldStatus, Status newStatus,
            StudyBean studyBean,
            UserAccountBean userAccount, // StudyDAO studyDao,
            UserAccountDAO userAccountDao, StudySubjectDAO studySubjectDao, StudyGroupDAO studyGroupDao, StudyGroupClassDAO studyGroupClassDao,
            SubjectGroupMapDAO subjectGroupMapDao, EventDefinitionCRFDAO eventDefinitionCrfDao, StudyEventDefinitionDAO studyEventDefinitionDao,
            StudyEventDAO studyEventDao, EventCRFDAO eventCrfDao, ItemDataDAO itemDataDao, DatasetDAO datasetDao) {
        // ArrayList<StudyBean> sites = (ArrayList) studyDao.findAllByParent(studyBean.getId());
        // YW 09-27-2007 << restore auto-removed sites
        // for (int i = 0; i < sites.size(); i++) {
        // StudyBean site = (StudyBean) sites.get(i);
        // if (site.getStatus() == oldStatus) {
        // site.setStatus(site.getOldStatus());
        // site.setUpdater(userAccount);
        // site.setUpdatedDate(new Date(System.currentTimeMillis()));
        // studyDao.update(site);
        // }
        // }

        // restore all users and roles
        ArrayList<StudyUserRoleBean> userRoles = userAccountDao.findAllByStudyId(studyBean.getId());
        for (int i = 0; i < userRoles.size(); i++) {
            StudyUserRoleBean role = (StudyUserRoleBean) userRoles.get(i);
            if (role.getStatus().equals(oldStatus)) {
                role.setStatus(newStatus);
                role.setUpdater(userAccount);
                role.setUpdatedDate(new Date(System.currentTimeMillis()));
                userAccountDao.updateStudyUserRole(role, role.getUserName());
            }
        }

        // YW << Meanwhile update current active study if restored study
        // is current active study
        // if (study.getId() == currentStudy.getId()) {
        // currentStudy.setStatus(newStatus);
        //
        // StudyUserRoleBean r = (new UserAccountDAO(sm.getDataSource())).findRoleByUserNameAndStudyId(ub.getName(), currentStudy.getId());
        // currentRole.setRole(r.getRole());
        // }
        // when an active site's parent study has been restored, this
        // active site will be restored as well if it was auto-removed
        // else if (currentStudy.getParentStudyId() == study.getId() && currentStudy.getStatus() == newStatus) {
        // currentStudy.setStatus(newStatus);
        //
        // StudyUserRoleBean r = (new UserAccountDAO(sm.getDataSource())).findRoleByUserNameAndStudyId(ub.getName(), currentStudy.getId());
        // StudyUserRoleBean rInParent = (new UserAccountDAO(sm.getDataSource())).findRoleByUserNameAndStudyId(ub.getName(), currentStudy.getParentStudyId());
        // // according to logic in SecureController.java: inherited
        // // role from parent study, pick the higher role
        // currentRole.setRole(Role.get(Role.max(r.getRole(), rInParent.getRole()).getId()));
        // }
        // YW 06-18-2007 >>
        ArrayList subjects = studySubjectDao.findAllByStudy(studyBean);
        // restore all subjects
        for (int i = 0; i < subjects.size(); i++) {
            StudySubjectBean subject = (StudySubjectBean) subjects.get(i);
            if (subject.getStatus().equals(oldStatus)) {
                subject.setStatus(newStatus);
                subject.setUpdater(userAccount);
                subject.setUpdatedDate(new Date(System.currentTimeMillis()));
                studySubjectDao.update(subject);
            }
        }

        // restore all study_group
        // StudyGroupDAO sgdao = new StudyGroupDAO(sm.getDataSource());
        // StudyGroupClassDAO sgcdao = new StudyGroupClassDAO(sm.getDataSource());
        // SubjectGroupMapDAO sgmdao = new SubjectGroupMapDAO(sm.getDataSource());
        ArrayList groups = studyGroupClassDao.findAllByStudy(studyBean);
        for (int i = 0; i < groups.size(); i++) {
            StudyGroupClassBean group = (StudyGroupClassBean) groups.get(i);
            if (group.getStatus().equals(oldStatus)) {
                group.setStatus(newStatus);
                group.setUpdater(userAccount);
                group.setUpdatedDate(new Date(System.currentTimeMillis()));
                studyGroupClassDao.update(group);
                // all subject_group_map
                ArrayList subjectGroupMaps = subjectGroupMapDao.findAllByStudyGroupClassId(group.getId());
                for (int j = 0; j < subjectGroupMaps.size(); j++) {
                    SubjectGroupMapBean sgMap = (SubjectGroupMapBean) subjectGroupMaps.get(j);
                    if (sgMap.getStatus().equals(oldStatus)) {
                        sgMap.setStatus(newStatus);
                        sgMap.setUpdater(userAccount);
                        sgMap.setUpdatedDate(new Date(System.currentTimeMillis()));
                        subjectGroupMapDao.update(sgMap);
                    }
                }
            }
        }

        // restore all event definitions and event
        // EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(sm.getDataSource());
        // StudyEventDAO sedao = new StudyEventDAO(sm.getDataSource());

        ArrayList definitions = studyEventDefinitionDao.findAllByStudy(studyBean);
        for (int i = 0; i < definitions.size(); i++) {
            StudyEventDefinitionBean definition = (StudyEventDefinitionBean) definitions.get(i);
            if (definition.getStatus().equals(oldStatus)) {
                definition.setStatus(newStatus);
                definition.setUpdater(userAccount);
                definition.setUpdatedDate(new Date());
                studyEventDefinitionDao.update(definition);
                ArrayList edcs = (ArrayList) eventDefinitionCrfDao.findAllByDefinition(definition.getId());
                for (int j = 0; j < edcs.size(); j++) {
                    EventDefinitionCRFBean edc = (EventDefinitionCRFBean) edcs.get(j);
                    if (edc.getStatus().equals(oldStatus)) {
                        edc.setStatus(newStatus);
                        edc.setUpdater(userAccount);
                        edc.setUpdatedDate(new Date());
                        eventDefinitionCrfDao.update(edc);
                    }
                }

                ArrayList events = (ArrayList) studyEventDao.findAllByDefinition(definition.getId());
                // EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());

                for (int j = 0; j < events.size(); j++) {
                    StudyEventBean event = (StudyEventBean) events.get(j);
                    if (event.getStatus().equals(oldStatus)) {
                        event.setStatus(newStatus);
                        event.setUpdater(userAccount);
                        event.setUpdatedDate(new Date());
                        studyEventDao.update(event);

                        ArrayList eventCRFs = eventCrfDao.findAllByStudyEvent(event);

                        for (int k = 0; k < eventCRFs.size(); k++) {
                            EventCRFBean eventCRF = (EventCRFBean) eventCRFs.get(k);
                            if (eventCRF.getStatus().equals(oldStatus)) {
                                eventCRF.setStatus(eventCRF.getOldStatus());
                                eventCRF.setUpdater(userAccount);
                                eventCRF.setUpdatedDate(new Date());
                                eventCrfDao.update(eventCRF);

                                ArrayList itemDatas = itemDataDao.findAllByEventCRFId(eventCRF.getId());
                                for (int a = 0; a < itemDatas.size(); a++) {
                                    ItemDataBean item = (ItemDataBean) itemDatas.get(a);
                                    if (!item.getStatus().equals(oldStatus)) {
                                        item.setStatus(newStatus);
                                        item.setUpdater(userAccount);
                                        item.setUpdatedDate(new Date());
                                        itemDataDao.update(item);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }// for definitions

        // DatasetDAO datadao = new DatasetDAO(sm.getDataSource());
        ArrayList dataset = datasetDao.findAllByStudyId(studyBean.getId());
        for (int i = 0; i < dataset.size(); i++) {
            DatasetBean data = (DatasetBean) dataset.get(i);
            if (data.getStatus().equals(oldStatus)) {
                data.setStatus(newStatus);
                data.setUpdater(userAccount);
                data.setUpdatedDate(new Date());
                datasetDao.update(data);
            }
        }
        return studyBean;
    }
}
