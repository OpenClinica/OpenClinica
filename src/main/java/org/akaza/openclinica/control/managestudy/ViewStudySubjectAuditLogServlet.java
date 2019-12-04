/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2007 Akaza Research
 */

package org.akaza.openclinica.control.managestudy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import core.org.akaza.openclinica.bean.admin.AuditBean;
import core.org.akaza.openclinica.bean.admin.CRFBean;
import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.bean.core.Status;
import core.org.akaza.openclinica.bean.core.Utils;
import core.org.akaza.openclinica.bean.managestudy.*;
import core.org.akaza.openclinica.bean.submit.EventCRFBean;
import core.org.akaza.openclinica.bean.submit.FormLayoutBean;
import core.org.akaza.openclinica.bean.submit.ItemDataBean;
import core.org.akaza.openclinica.bean.submit.SubjectBean;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.submit.SubmitDataServlet;
import core.org.akaza.openclinica.dao.admin.AuditDAO;
import core.org.akaza.openclinica.dao.admin.CRFDAO;
import core.org.akaza.openclinica.dao.hibernate.EventDefinitionCrfPermissionTagDao;
import core.org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import core.org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import core.org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import core.org.akaza.openclinica.dao.submit.EventCRFDAO;
import core.org.akaza.openclinica.dao.submit.FormLayoutDAO;
import core.org.akaza.openclinica.dao.submit.ItemDataDAO;
import core.org.akaza.openclinica.dao.submit.SubjectDAO;
import core.org.akaza.openclinica.domain.datamap.EventDefinitionCrfPermissionTag;
import org.akaza.openclinica.view.Page;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author jsampson
 *
 */

public class ViewStudySubjectAuditLogServlet extends SecureController {

    /**
     * Checks whether the user has the right permission to proceed function
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {

        // if (SubmitDataServlet.mayViewData(ub, currentRole)) {
        // return;
        // }
        // if (ub.isSysAdmin()) {
        // return;
        // }
        // Role r = currentRole.getRole();
        // if (r.equals(Role.STUDYDIRECTOR) || r.equals(Role.COORDINATOR)) {
        // return;
        // }
        // addPageMessage(respage.getString("no_have_correct_privilege_current_study") +
        // respage.getString("change_study_contact_sysadmin"));
        // throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("may_not_submit_data"),
        // "1");
        if (ub.isSysAdmin()) {
            return;
        }

        if (SubmitDataServlet.mayViewData(ub, currentRole)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + " " + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.LIST_STUDY_SUBJECTS, resexception.getString("not_study_director"), "1");

    }

    @Override
    public void processRequest() throws Exception {
        EventDefinitionCrfPermissionTagDao eventDefinitionCrfPermissionTagDao = (EventDefinitionCrfPermissionTagDao) SpringServletAccess.getApplicationContext(context).getBean("eventDefinitionCrfPermissionTagDao");
        StudySubjectDAO subdao = new StudySubjectDAO(sm.getDataSource());
        SubjectDAO sdao = new SubjectDAO(sm.getDataSource());
        AuditDAO adao = new AuditDAO(sm.getDataSource());

        FormProcessor fp = new FormProcessor(request);

        StudyEventDAO sedao = new StudyEventDAO(sm.getDataSource());
        StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(sm.getDataSource());
        EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(sm.getDataSource());
        EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());
        CRFDAO cdao = new CRFDAO(sm.getDataSource());
        FormLayoutDAO fldao = new FormLayoutDAO(sm.getDataSource());

        ArrayList studySubjectAudits = new ArrayList();
        ArrayList<AuditBean> eventCRFAudits = new ArrayList();
        ArrayList studyEventAudits = new ArrayList();
        ArrayList allDeletedEventCRFs = new ArrayList();
        String attachedFilePath = Utils.getAttachedFilePath(currentStudy);

        int studySubId = fp.getInt("id", true);// studySubjectId
        request.setAttribute("id", studySubId);

        if (studySubId == 0) {
            addPageMessage(respage.getString("please_choose_a_subject_to_view"));
            forwardPage(Page.LIST_STUDY_SUBJECTS);
        } else {
            StudySubjectBean studySubject = (StudySubjectBean) subdao.findByPK(studySubId);
            Study study = (Study) getStudyDao().findByPK(studySubject.getStudyId());
            // Check if this StudySubject would be accessed from the Current Study
            if (studySubject.getStudyId() != currentStudy.getStudyId()) {
                if (currentStudy.isSite()) {
                    addPageMessage(respage.getString("no_have_correct_privilege_current_study") + " " + respage.getString("change_active_study_or_contact"));
                    forwardPage(Page.MENU_SERVLET);
                    return;
                } else {
                    // The SubjectStudy is not belong to currentstudy and current study is not a site.
                    Collection sites = getStudyDao().findOlnySiteIdsByStudy(currentStudy);
                    if (!sites.contains(study.getStudyId())) {
                        addPageMessage(
                                respage.getString("no_have_correct_privilege_current_study") + " " + respage.getString("change_active_study_or_contact"));
                        forwardPage(Page.MENU_SERVLET);
                        return;
                    }
                }
            }

            request.setAttribute("studySub", studySubject);
            SubjectBean subject = (SubjectBean) sdao.findByPK(studySubject.getSubjectId());

            StudyParameterValueDAO spvdao = new StudyParameterValueDAO(sm.getDataSource());
            study.setCollectDob(spvdao.findByHandleAndStudy(study.getStudyId(), "collectDob").getValue());
            String collectdob = "used";
            if (study.getCollectDob().equals("2")) {
                collectdob = "yearOnly";
            } else if (study.getCollectDob().equals("3")) {
                collectdob = "notUsed";
            } else if (study.getCollectDob().equals("1")) {
                collectdob = "used";
            }

            request.setAttribute("collectdob", collectdob);
            request.setAttribute("subject", subject);

            request.setAttribute("study", study);

            /* Show both study subject and subject audit events together */
            // Study subject value changed
            Collection studySubjectAuditEvents = adao.findStudySubjectAuditEvents(studySubject.getId());
            // Text values will be shown on the page for the corresponding
            // integer values.

            Role role = currentRole.getRole();

            for (Iterator iterator = studySubjectAuditEvents.iterator(); iterator.hasNext(); ) {
                AuditBean auditBean = (AuditBean) iterator.next();
                if (auditBean.getAuditEventTypeId() == 3) {
                    auditBean.setOldValue(Status.get(Integer.parseInt(auditBean.getOldValue())).getName());
                    auditBean.setNewValue(Status.get(Integer.parseInt(auditBean.getNewValue())).getName());
                }

                if (getAuditLogEventTypes().contains(auditBean.getAuditEventTypeId())) {

                    if ((role.equals(Role.RESEARCHASSISTANT)
                            && role.getDescription().equals("Clinical Research Coordinator"))
                            || (role.equals(Role.INVESTIGATOR)
                            && role.getDescription().equals("Investigator"))) {
                        auditBean.setOldValue(getCrytoConverter().convertToEntityAttribute(auditBean.getOldValue()));
                        auditBean.setNewValue(getCrytoConverter().convertToEntityAttribute(auditBean.getNewValue()));
                    } else {
                        auditBean.setOldValue("<Masked>");
                        auditBean.setNewValue("<Masked>");
                    }
                }
            }
            studySubjectAudits.addAll(studySubjectAuditEvents);

            // Global subject value changed
            studySubjectAudits.addAll(adao.findSubjectAuditEvents(subject.getId()));

            studySubjectAudits.addAll(adao.findStudySubjectGroupAssignmentAuditEvents(studySubject.getId()));
            request.setAttribute("studySubjectAudits", studySubjectAudits);

            // Get the list of events
            ArrayList events = sedao.findAllByStudySubject(studySubject);
            for (int i = 0; i < events.size(); i++) {
                // Link study event definitions
                StudyEventBean studyEvent = (StudyEventBean) events.get(i);
                StudyEventDefinitionBean sedBean = (StudyEventDefinitionBean) seddao.findByPK(studyEvent.getStudyEventDefinitionId());
                studyEvent.setStudyEventDefinition(sedBean);

                // Link event CRFs
                studyEvent.setEventCRFs(ecdao.findAllByStudyEvent(studyEvent));

                // Find deleted Event CRFs
                List deletedEventCRFs = adao.findDeletedEventCRFsFromAuditEventByEventCRFStatus(studyEvent.getId());
                allDeletedEventCRFs.addAll(deletedEventCRFs);
                logger.info("deletedEventCRFs size[" + deletedEventCRFs.size() + "]");
            }
            
            List<String> tagIds = getPermissionTagsList().size()!=0 ?getPermissionTagsList():new ArrayList<>();

            for (int i = 0; i < events.size(); i++) {
                StudyEventBean studyEvent = (StudyEventBean) events.get(i);
                studyEventAudits.addAll(adao.findStudyEventAuditEvents(studyEvent.getId()));

                ArrayList eventCRFs = studyEvent.getEventCRFs();
                for (int j = 0; j < eventCRFs.size(); j++) {
                    // Link CRF and CRF Versions
                    EventCRFBean eventCRF = (EventCRFBean) eventCRFs.get(j);
                    eventCRF.setFormLayout((FormLayoutBean) fldao.findByPK(eventCRF.getFormLayoutId()));
                    CRFBean crf = cdao.findByLayoutId(eventCRF.getFormLayoutId());
                    StudyEventDefinitionBean sed = (StudyEventDefinitionBean) seddao.findByPK(studyEvent.getStudyEventDefinitionId());
                    eventCRF.setCrf(crf);
                    // Get the event crf audits

                    List<AuditBean> abs = (List<AuditBean>) adao.findEventCRFAuditEventsWithItemDataType(eventCRF.getId());
                    for (AuditBean ab : abs) {
                        if (ab.getAuditTable().equalsIgnoreCase("item_data")) {
                            EventDefinitionCRFBean edc = edcdao.findByStudyEventDefinitionIdAndCRFId(study, sed.getId(), crf.getId());
                            List<EventDefinitionCrfPermissionTag> edcPTagIds = eventDefinitionCrfPermissionTagDao.findByEdcIdTagId(edc.getId(), edc.getParentId(), tagIds);
                            if (edcPTagIds.size() != 0) {
                                ab.setOldValue("<Masked>");
                                ab.setNewValue("<Masked>");
                            }
                        }
                    }

                    eventCRFAudits.addAll(abs);
                    logger.info("eventCRFAudits size [" + eventCRFAudits.size() + "] eventCRF id [" + eventCRF.getId() + "]");
                }
            }
            ItemDataDAO itemDataDao = new ItemDataDAO(sm.getDataSource());
            for (Object o : eventCRFAudits) {
                AuditBean ab = (AuditBean) o;
                if (ab.getAuditTable().equalsIgnoreCase("item_data")) {
                    ItemDataBean idBean = (ItemDataBean) itemDataDao.findByPK(ab.getEntityId());
                    ab.setOrdinal(idBean.getOrdinal());
                }
            }
            request.setAttribute("events", events);
            request.setAttribute("eventCRFAudits", eventCRFAudits);
            request.setAttribute("studyEventAudits", studyEventAudits);
            request.setAttribute("allDeletedEventCRFs", allDeletedEventCRFs);
            request.setAttribute("attachedFilePath", attachedFilePath);

            forwardPage(Page.VIEW_STUDY_SUBJECT_AUDIT);

        }
    }

    @Override
    protected String getAdminServlet() {
        if (ub.isSysAdmin()) {
            return SecureController.ADMIN_SERVLET_CODE;
        } else {
            return "";
        }
    }

    private List<Integer> getAuditLogEventTypes() {
        List<Integer> auditLogEventTypes = new ArrayList<>();
        auditLogEventTypes.add(43);
        auditLogEventTypes.add(44);
        auditLogEventTypes.add(46);
        auditLogEventTypes.add(47);
        auditLogEventTypes.add(49);
        auditLogEventTypes.add(50);
        auditLogEventTypes.add(52);
        auditLogEventTypes.add(53);
        auditLogEventTypes.add(55);
        auditLogEventTypes.add(56);
        return auditLogEventTypes;
    }
}
