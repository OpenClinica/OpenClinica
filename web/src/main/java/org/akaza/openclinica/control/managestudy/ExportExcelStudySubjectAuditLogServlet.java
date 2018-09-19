/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2007 Akaza Research
 */

package org.akaza.openclinica.control.managestudy;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.akaza.openclinica.bean.admin.AuditBean;
import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.admin.DeletedEventCRFBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.Utils;
import org.akaza.openclinica.bean.managestudy.*;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.FormLayoutBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.submit.SubmitDataServlet;
import org.akaza.openclinica.dao.admin.AuditDAO;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.hibernate.EventDefinitionCrfPermissionTagDao;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.FormLayoutDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrfPermissionTag;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 * @author jsampson
 * @author akung
 */

@SuppressWarnings("serial")
public class ExportExcelStudySubjectAuditLogServlet extends SecureController {

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

        StudyEventDAO sedao = new StudyEventDAO(sm.getDataSource());
        StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(sm.getDataSource());
        EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(sm.getDataSource());
        EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());
        StudyDAO studydao = new StudyDAO(sm.getDataSource());
        CRFDAO cdao = new CRFDAO(sm.getDataSource());
        FormLayoutDAO fldao = new FormLayoutDAO(sm.getDataSource());
        StudySubjectBean studySubject = null;
        SubjectBean subject = null;
        ArrayList events = null;
        ArrayList studySubjectAudits = new ArrayList();
        ArrayList <AuditBean>eventCRFAudits = new ArrayList();
        ArrayList studyEventAudits = new ArrayList();
        ArrayList allDeletedEventCRFs = new ArrayList();
        ArrayList allEventCRFs = new ArrayList();
        ArrayList allEventCRFItems = new ArrayList();
        String attachedFilePath = Utils.getAttachedFilePath(currentStudy);

        FormProcessor fp = new FormProcessor(request);

        int studySubId = fp.getInt("id", true);

        if (studySubId == 0) {
            addPageMessage(respage.getString("please_choose_a_subject_to_view"));
            forwardPage(Page.LIST_STUDY_SUBJECTS);
        } else {
            studySubject = (StudySubjectBean) subdao.findByPK(studySubId);
            StudyBean study = (StudyBean) studydao.findByPK(studySubject.getStudyId());
            // Check if this StudySubject would be accessed from the Current Study
            if (studySubject.getStudyId() != currentStudy.getId()) {
                if (currentStudy.getParentStudyId() > 0) {
                    addPageMessage(respage.getString("no_have_correct_privilege_current_study") + " " + respage.getString("change_active_study_or_contact"));
                    forwardPage(Page.MENU_SERVLET);
                    return;
                } else {
                    // The SubjectStudy is not belong to currentstudy and current study is not a site.
                    Collection sites = studydao.findOlnySiteIdsByStudy(currentStudy);
                    if (!sites.contains(study.getId())) {
                        addPageMessage(
                                respage.getString("no_have_correct_privilege_current_study") + " " + respage.getString("change_active_study_or_contact"));
                        forwardPage(Page.MENU_SERVLET);
                        return;
                    }
                }
            }

            subject = (SubjectBean) sdao.findByPK(studySubject.getSubjectId());

            /* Show both study subject and subject audit events together */
            // Study subject value changed
            Collection studySubjectAuditEvents = adao.findStudySubjectAuditEvents(studySubject.getId());
            // Text values will be shown on the page for the corresponding
            // integer values.
            for (Iterator iterator = studySubjectAuditEvents.iterator(); iterator.hasNext();) {
                AuditBean auditBean = (AuditBean) iterator.next();
                if (auditBean.getAuditEventTypeId() == 3) {
                    auditBean.setOldValue(Status.get(Integer.parseInt(auditBean.getOldValue())).getName());
                    auditBean.setNewValue(Status.get(Integer.parseInt(auditBean.getNewValue())).getName());
                }
            }
            studySubjectAudits.addAll(studySubjectAuditEvents);

            // Global subject value changed
            studySubjectAudits.addAll(adao.findSubjectAuditEvents(subject.getId()));

            studySubjectAudits.addAll(adao.findStudySubjectGroupAssignmentAuditEvents(studySubject.getId()));

            // Get the list of events
            events = sedao.findAllByStudySubject(studySubject);
            for (int i = 0; i < events.size(); i++) {
                // Link study event definitions
                StudyEventBean studyEvent = (StudyEventBean) events.get(i);
                studyEvent.setStudyEventDefinition((StudyEventDefinitionBean) seddao.findByPK(studyEvent.getStudyEventDefinitionId()));

                // Link event CRFs
                studyEvent.setEventCRFs(ecdao.findAllByStudyEvent(studyEvent));

                // Find deleted Event CRFs
                List deletedEventCRFs = adao.findDeletedEventCRFsFromAuditEvent(studyEvent.getId());
                allDeletedEventCRFs.addAll(deletedEventCRFs);
                List eventCRFs = (List) adao.findAllEventCRFAuditEvents(studyEvent.getId());
                allEventCRFs.addAll(eventCRFs);
                List eventCRFItems = (List) adao.findAllEventCRFAuditEventsWithItemDataType(studyEvent.getId());
                allEventCRFItems.addAll(eventCRFItems);
                logger.info("deletedEventCRFs size[" + deletedEventCRFs.size() + "]");
                logger.info("allEventCRFItems size[" + allEventCRFItems.size() + "]");
            }

            for (int i = 0; i < events.size(); i++) {
                StudyEventBean studyEvent = (StudyEventBean) events.get(i);
                studyEventAudits.addAll(adao.findStudyEventAuditEvents(studyEvent.getId()));

                ArrayList eventCRFs = studyEvent.getEventCRFs();
                for (int j = 0; j < eventCRFs.size(); j++) {
                    // Link CRF and CRF Versions
                    EventCRFBean eventCRF = (EventCRFBean) eventCRFs.get(j);
                    eventCRF.setFormLayout((FormLayoutBean) fldao.findByPK(eventCRF.getFormLayoutId()));
                    // Get the event crf audits
                    CRFBean crf =cdao.findByLayoutId(eventCRF.getFormLayoutId());
                    StudyEventDefinitionBean sed = (StudyEventDefinitionBean)seddao.findByPK(studyEvent.getStudyEventDefinitionId());
                    eventCRF.setCrf(crf);

                    List<String> tagIds = getPermissionTagsList().size()!=0 ?getPermissionTagsList():new ArrayList<>();

                    List < AuditBean> abs= (List<AuditBean>) adao.findEventCRFAuditEventsWithItemDataType(eventCRF.getId());
                    for (AuditBean ab : abs) {
                        if (ab.getAuditTable().equalsIgnoreCase("item_data")) {
                            EventDefinitionCRFBean edc = edcdao.findByStudyEventDefinitionIdAndCRFId(study, sed.getId(), crf.getId());
                            List <EventDefinitionCrfPermissionTag> edcPTagIds= eventDefinitionCrfPermissionTagDao.findByEdcIdTagId(edc.getId(), edc.getParentId(),tagIds);

                            if(edcPTagIds.size()!=0){
                                ab.setOldValue("<Masked>");
                                ab.setNewValue("<Masked>");                            }
                        }
                        ab.setStudyEventId(studyEvent.getId());
                        ab.setEventCrfVersionId(eventCRF.getFormLayoutId());
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

        }

        try {

            WritableFont headerFormat = new WritableFont(WritableFont.ARIAL, 8, WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.BLUE2);
            WritableCellFormat cellFormat = new WritableCellFormat();
            cellFormat.setFont(headerFormat);

            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=export.xls");

            WorkbookSettings wbSettings = new WorkbookSettings();
            wbSettings.setLocale(new Locale("en", "EN"));
            WritableWorkbook workbook = Workbook.createWorkbook(response.getOutputStream(), wbSettings);

            int row = 0;

            // Subject Information
            workbook.createSheet("Subject Information", 0);
            WritableSheet excelSheet = workbook.getSheet(0);
            // Subject Summary
            String[] excelRow = new String[] { "study_subject_ID", "created_by", "status" };
            for (int i = 0; i < excelRow.length; i++) {
                Label label = new Label(i, row, ResourceBundleProvider.getResWord(excelRow[i]), cellFormat);
                excelSheet.addCell(label);
            }
            row++;

            excelRow = new String[] { studySubject.getLabel(), studySubject.getOwner().getName(), studySubject.getStatus().getName() };
            for (int i = 0; i < excelRow.length; i++) {
                Label label = new Label(i, row, ResourceBundleProvider.getResWord(excelRow[i]), cellFormat);
                excelSheet.addCell(label);
            }
            row++;
            row++;

            // Subject Audit Events
            excelRow = new String[] { "audit_event", "date_time_of_server", "user", "value_type", "old", "new" };
            for (int i = 0; i < excelRow.length; i++) {
                Label label = new Label(i, row, ResourceBundleProvider.getResWord(excelRow[i]), cellFormat);
                excelSheet.addCell(label);
            }
            row++;

            for (int j = 0; j < studySubjectAudits.size(); j++) {
                AuditBean audit = (AuditBean) studySubjectAudits.get(j);

                excelRow = new String[] { audit.getAuditEventTypeName(), dateTimeFormat(audit.getAuditDate()), audit.getUserName(), audit.getEntityName(),
                        audit.getOldValue(), audit.getNewValue() };
                for (int i = 0; i < excelRow.length; i++) {
                    Label label = new Label(i, row, ResourceBundleProvider.getResWord(excelRow[i].replace(" ", "_").toLowerCase()), cellFormat);
                    excelSheet.addCell(label);
                }
                row++;
            }
            row++;

            // Study Events
            excelRow = new String[] { "study_events", "location", "date", "occurrence_number" };
            for (int i = 0; i < excelRow.length; i++) {
                Label label = new Label(i, row, ResourceBundleProvider.getResWord(excelRow[i]), cellFormat);
                excelSheet.addCell(label);
            }
            row++;

            for (int j = 0; j < events.size(); j++) {
                StudyEventBean event = (StudyEventBean) events.get(j);
                if (event.getStartTimeFlag()) {
                    excelRow = new String[] { event.getStudyEventDefinition().getName(), event.getLocation(), dateTimeFormat(event.getDateStarted()),
                            Integer.toString(event.getSampleOrdinal()) };
                } else {
                    excelRow = new String[] { event.getStudyEventDefinition().getName(), event.getLocation(), dateFormat(event.getDateStarted()),
                            Integer.toString(event.getSampleOrdinal()) };
                }
                for (int i = 0; i < excelRow.length; i++) {
                    Label label = new Label(i, row, ResourceBundleProvider.getResWord(excelRow[i]), cellFormat);
                    excelSheet.addCell(label);
                }
                row++;
            }
            autoSizeColumns(excelSheet);

            int sheet = 0;

            // Study Event Summary Looper
            for (int eventCount = 0; eventCount < events.size(); eventCount++) {
                row = 0;
                sheet++;
                StudyEventBean event = (StudyEventBean) events.get(eventCount);
                workbook.createSheet(event.getStudyEventDefinition().getName().replace("/", ".") + "_" + event.getSampleOrdinal(), sheet);
                excelSheet = workbook.getSheet(sheet);

                Label label = null;

                // Header
                label = new Label(0, row, ResourceBundleProvider.getResWord("name"), cellFormat);
                excelSheet.addCell(label);
                label = new Label(1, row, event.getStudyEventDefinition().getName(), cellFormat);
                excelSheet.addCell(label);
                row++;
                label = new Label(0, row, "Location");
                excelSheet.addCell(label);
                label = new Label(1, row, event.getLocation());
                excelSheet.addCell(label);
                row++;
                label = new Label(0, row, "Start Date");
                excelSheet.addCell(label);
                if (event.getStartTimeFlag()) {
                    label = new Label(1, row, dateTimeFormat(event.getDateStarted()));
                } else {
                    label = new Label(1, row, dateFormat(event.getDateStarted()));
                }
                excelSheet.addCell(label);
                row++;
                label = new Label(0, row, "Status");
                excelSheet.addCell(label);
                label = new Label(1, row, event.getSubjectEventStatus().getName());
                excelSheet.addCell(label);
                row++;
                label = new Label(0, row, ResourceBundleProvider.getResWord("occurrence_number"));
                excelSheet.addCell(label);
                label = new Label(1, row, Integer.toString(event.getSampleOrdinal()));
                excelSheet.addCell(label);
                row++;
                row++;
                // End Header

                // Audit for Deleted Event CRFs
                excelRow = new String[] { "name", "version", "deleted_by", "delete_date" };
                for (int i = 0; i < excelRow.length; i++) {
                    label = new Label(i, row, ResourceBundleProvider.getResWord(excelRow[i]), cellFormat);
                    excelSheet.addCell(label);
                }
                row++;

                for (int j = 0; j < allDeletedEventCRFs.size(); j++) {
                    DeletedEventCRFBean deletedEventCRF = (DeletedEventCRFBean) allDeletedEventCRFs.get(j);
                    if (deletedEventCRF.getStudyEventId() == event.getId()) {
                        excelRow = new String[] { deletedEventCRF.getCrfName(), deletedEventCRF.getFormLayout(), deletedEventCRF.getDeletedBy(),
                                dateFormat(deletedEventCRF.getDeletedDate()) };
                        for (int i = 0; i < excelRow.length; i++) {
                            label = new Label(i, row, ResourceBundleProvider.getResWord(excelRow[i]), cellFormat);
                            excelSheet.addCell(label);
                        }
                        row++;
                    }
                }
                row++;
                row++;

                // Audit Events for Study Event
                excelRow = new String[] { "audit_event", "date_time_of_server", "user", "value_type", "old", "new", "details" };
                for (int i = 0; i < excelRow.length; i++) {
                    label = new Label(i, row, ResourceBundleProvider.getResWord(excelRow[i]), cellFormat);
                    excelSheet.addCell(label);
                }
                row++;

                for (int j = 0; j < studyEventAudits.size(); j++) {
                    AuditBean studyEvent = (AuditBean) studyEventAudits.get(j);
                    if (studyEvent.getEntityId() == event.getId()) {
                        String getOld = studyEvent.getOldValue();
                        String oldValue = "";
                        if (getOld.equals("0"))
                            oldValue = "invalid";
                        else if (getOld.equals("1"))
                            oldValue = "scheduled";
                        else if (getOld.equals("2"))
                            oldValue = "not_scheduled";
                        else if (getOld.equals("3"))
                            oldValue = "data_entry_started";
                        else if (getOld.equals("4"))
                            oldValue = "completed";
                        else if (getOld.equals("5"))
                            oldValue = "stopped";
                        else if (getOld.equals("6"))
                            oldValue = "skipped";
                        else if (getOld.equals("7"))
                            oldValue = "locked";
                        else if (getOld.equals("8"))
                            oldValue = "signed";
                        else
                            oldValue = studyEvent.getOldValue();

                        String getNew = studyEvent.getNewValue();
                        String newValue = "";
                        if (getNew.equals("0"))
                            newValue = "invalid";
                        else if (getNew.equals("1"))
                            newValue = "scheduled";
                        else if (getNew.equals("2"))
                            newValue = "not_scheduled";
                        else if (getNew.equals("3"))
                            newValue = "data_entry_started";
                        else if (getNew.equals("4"))
                            newValue = "completed";
                        else if (getNew.equals("5"))
                            newValue = "removed";
                        else if (getNew.equals("6"))
                            newValue = "skipped";
                        else if (getNew.equals("7"))
                            newValue = "locked";
                        else if (getNew.equals("8"))
                            newValue = "signed";
                        else if (getNew.equals("9"))
                            newValue = "forzen";
                        else
                            newValue = studyEvent.getNewValue();

                        excelRow = new String[] { studyEvent.getAuditEventTypeName(), dateTimeFormat(studyEvent.getAuditDate()), studyEvent.getUserName(),
                                studyEvent.getEntityName() + "(" + studyEvent.getOrdinal() + ")", oldValue, newValue, studyEvent.getDetails() };
                        for (int i = 0; i < excelRow.length; i++) {
                            label = new Label(i, row, ResourceBundleProvider.getResWord(excelRow[i]), cellFormat);
                            excelSheet.addCell(label);
                        }
                        row++;
                    }
                }
                row++;
                row++;

                // Event CRFs Audit Events
                for (int j = 0; j < allEventCRFs.size(); j++) {
                    AuditBean auditBean = (AuditBean) allEventCRFs.get(j);
                    EventCRFBean eventCrf = (EventCRFBean) ecdao.findByPK(auditBean.getEventCRFId());
                    FormLayoutBean formLayout = (FormLayoutBean) fldao.findByPK(eventCrf.getFormLayoutId());
                    if (auditBean.getStudyEventId() == event.getId()) {

                        // Audit Events for Study Event
                        excelRow = new String[] { "name", "version", "date_interviewed", "interviewer_name", "owner" };

                        for (int i = 0; i < excelRow.length; i++) {
                            label = new Label(i, row, ResourceBundleProvider.getResWord(excelRow[i]), cellFormat);
                            excelSheet.addCell(label);
                        }
                        row++;

                        excelRow = new String[] { auditBean.getCrfName(), formLayout.getName(), dateFormat(eventCrf.getDateInterviewed()),
                                eventCrf.getInterviewerName(), eventCrf.getOwner().getName() };
                        for (int i = 0; i < excelRow.length; i++) {
                            label = new Label(i, row, ResourceBundleProvider.getResWord(excelRow[i]), cellFormat);
                            excelSheet.addCell(label);
                        }
                        row++;
                        row++;

                        excelRow = new String[] { "audit_event", "date_time_of_server", "user", "value_type", "old", "new" };
                        for (int i = 0; i < excelRow.length; i++) {
                            label = new Label(i, row, ResourceBundleProvider.getResWord(excelRow[i]), cellFormat);
                            excelSheet.addCell(label);
                        }
                        row++;
                        row++;
                        for (int k = 0; k < eventCRFAudits.size(); k++) {
                            row--;
                            AuditBean eventCrfAudit = (AuditBean) eventCRFAudits.get(k);
                            if (eventCrfAudit.getStudyEventId() == event.getId() && eventCrfAudit.getEventCrfVersionId() == auditBean.getEventCrfVersionId()) {
                                String oldValue = "";
                                String newValue = "";
                                if (eventCrfAudit.getAuditEventTypeId() == 12 || eventCrfAudit.getEntityName().equals("Status")) {
                                    String getOld = eventCrfAudit.getOldValue();
                                    if (getOld.equals("0"))
                                        oldValue = "invalid";
                                    else if (getOld.equals("1"))
                                        oldValue = "available";
                                    else if (getOld.equals("2"))
                                        oldValue = "unavailable";
                                    else if (getOld.equals("3"))
                                        oldValue = "private";
                                    else if (getOld.equals("4"))
                                        oldValue = "pending";
                                    else if (getOld.equals("5"))
                                        oldValue = "removed";
                                    else if (getOld.equals("6"))
                                        oldValue = "locked";
                                    else if (getOld.equals("7"))
                                        oldValue = "auto-removed";
                                    else {
                                        oldValue = getOld;
                                    }
                                } else if (eventCrfAudit.getAuditEventTypeId() == 32) {
                                    String getOld = eventCrfAudit.getOldValue();
                                    if (getOld.equals("0"))
                                        oldValue = "FALSE";
                                    else if (getOld.equals("1"))
                                        oldValue = "TRUE";
                                    else {
                                        oldValue = getOld;
                                    }
                                } else {
                                    oldValue = eventCrfAudit.getOldValue();
                                }

                                if (eventCrfAudit.getAuditEventTypeId() == 12 || eventCrfAudit.getEntityName().equals("Status")) {
                                    String getNew = eventCrfAudit.getNewValue();
                                    if (getNew.equals("0"))
                                        newValue = "invalid";
                                    else if (getNew.equals("1"))
                                        newValue = "available";
                                    else if (getNew.equals("2"))
                                        newValue = "unavailable";
                                    else if (getNew.equals("3"))
                                        newValue = "private";
                                    else if (getNew.equals("4"))
                                        newValue = "pending";
                                    else if (getNew.equals("5"))
                                        newValue = "removed";
                                    else if (getNew.equals("6"))
                                        newValue = "locked";
                                    else if (getNew.equals("7"))
                                        newValue = "auto-removed";
                                    else {
                                        newValue = getNew;
                                    }
                                } else if (eventCrfAudit.getAuditEventTypeId() == 32) {
                                    String getNew = eventCrfAudit.getNewValue();
                                    if (getNew.equals("0"))
                                        newValue = "FALSE";
                                    else if (getNew.equals("1"))
                                        newValue = "TRUE";
                                    else {
                                        newValue = getNew;
                                    }

                                } else {
                                    newValue = eventCrfAudit.getNewValue();
                                }
                                String ordinal = "";
                                if (eventCrfAudit.getOrdinal() != 0) {
                                    ordinal = "(" + eventCrfAudit.getOrdinal() + ")";
                                } else if (eventCrfAudit.getOrdinal() == 0 && eventCrfAudit.getItemDataRepeatKey() != 0) {
                                    ordinal = "(" + eventCrfAudit.getItemDataRepeatKey() + ")";
                                }

                                excelRow = new String[] { eventCrfAudit.getAuditEventTypeName(), dateTimeFormat(eventCrfAudit.getAuditDate()),
                                        eventCrfAudit.getUserName(), eventCrfAudit.getEntityName() + ordinal, oldValue, newValue };
                                for (int i = 0; i < excelRow.length; i++) {
                                    label = new Label(i, row, ResourceBundleProvider.getResWord(excelRow[i]), cellFormat);
                                    excelSheet.addCell(label);
                                }
                                row++;
                                row++;
                            }

                            row++;

                        }
                        row++;

                    }
                    autoSizeColumns(excelSheet);
                }
            }

            workbook.write();
            workbook.close();
            session.setAttribute("subject", null);
            session.setAttribute("studySub", null);
            session.setAttribute("studyEventAudits", null);
            session.setAttribute("studySubjectAudits", null);
            session.setAttribute("events", null);
            session.setAttribute("eventCRFAudits", null);
            session.setAttribute("allDeletedEventCRFs", null);
        } catch (Exception e) {
            throw e;
        } finally {
            // proposed move session attributes here

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

    private String dateFormat(Date date) {
        if (date == null) {
            return "";
        } else {
            SimpleDateFormat dteFormat = new SimpleDateFormat(ResourceBundleProvider.getFormatBundle().getString("date_format_string"));
            return dteFormat.format(date);
        }
    }

    private String dateTimeFormat(Date date) {
        if (date == null) {
            return "";
        } else {
            SimpleDateFormat dtetmeFormat = new SimpleDateFormat(ResourceBundleProvider.getFormatBundle().getString("date_time_format_string"));
            return dtetmeFormat.format(date);
        }
    }

    private void autoSizeColumns(WritableSheet sheet) {
        for (int x = 0; x < 6; x++) {
            CellView cell = sheet.getColumnView(x);
            cell.setAutosize(true);
            sheet.setColumnView(x, cell);
        }
    }

}
