package org.akaza.openclinica.control.managestudy;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import core.org.akaza.openclinica.bean.admin.CRFBean;
import core.org.akaza.openclinica.bean.core.DataEntryStage;
import core.org.akaza.openclinica.bean.login.StudyUserRoleBean;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import core.org.akaza.openclinica.bean.managestudy.StudyEventBean;
import core.org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.bean.submit.CRFVersionBean;
import core.org.akaza.openclinica.bean.submit.EventCRFBean;
import core.org.akaza.openclinica.bean.submit.SubjectBean;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.Status;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.domain.enumsupport.EventCrfWorkflowStatusEnum;
import org.jmesa.view.html.HtmlBuilder;

public class EventCrfLayerBuilder {

    HtmlBuilder html;
    SubjectBean subject;
    Integer rowCount;
    List<StudyEventBean> studyEvents;
    DataEntryStage eventCrfStatus;
    EventCRFBean eventCrfBean = null;
    StudySubjectBean studySubject;
    Study currentStudy;
    StudyUserRoleBean currentRole;
    UserAccountBean currentUser;
    EventDefinitionCRFBean eventDefinitionCrf;
    CRFBean crf;
    StudyEventDefinitionBean studyEventDefinition;
    private ResourceBundle reswords = ResourceBundleProvider.getWordsBundle();
    private ResourceBundle restexts = ResourceBundleProvider.getTextsBundle();
    String contextPath;
    StudyDao studyDao;

    public EventCrfLayerBuilder(SubjectBean subject, Integer rowCount, List<StudyEventBean> studyEvents, DataEntryStage eventCrfStatus,
                                EventCRFBean eventCrfBean, StudySubjectBean studySubject, Study currentStudy, StudyUserRoleBean currentRole, UserAccountBean currentUser,
                                EventDefinitionCRFBean eventDefinitionCrf, CRFBean crf, StudyEventDefinitionBean studyEventDefinition, String contextPath , StudyDao studyDao) {
        super();
        this.html = new HtmlBuilder();
        this.subject = subject;
        this.rowCount = rowCount;
        this.studyEvents = studyEvents;
        this.eventCrfStatus = eventCrfStatus;
        this.eventCrfBean = eventCrfBean;
        this.studySubject = studySubject;
        this.currentStudy = currentStudy;
        this.currentRole = currentRole;
        this.currentUser = currentUser;
        this.eventDefinitionCrf = eventDefinitionCrf;
        this.crf = crf;
        this.studyEventDefinition = studyEventDefinition;
        this.contextPath = contextPath;
        this.studyDao=studyDao;
    }

    StudyEventBean getStudyEvent() {
        return studyEvents.size() < 1 ? null : studyEvents.get(0);
    }

    String buid() {
        if (eventCrfBean==null){
            eventCrfBean= new EventCRFBean(EventCrfWorkflowStatusEnum.NOT_STARTED);
        }

        buildLock();
        buildEvent();
        clickToEnterData();
        buildEnd();
        return html.toString();
    }

    void buildLock() {

        String studySubjectLabel = studySubject.getLabel();

        html.table(0).border("0").cellpadding("0").cellspacing("0").close();
        html.tr(0).valign("top").close().td(0).close();
        // Lock Div
        html.div().id("Lock_" + studySubjectLabel + "_" + crf.getId() + "_" + rowCount)
                .style("position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;").close();
        if ((eventCrfBean.getRemoved()!=null && eventCrfBean.getRemoved())) {
            lockLinkBuilder(html, studySubjectLabel, rowCount, crf, "images/CRF_status_icon_Invalid_collapse.gif", "images/CRF_status_icon_Invalid.gif");
        } else if (eventCrfBean.getWorkflowStatus() == EventCrfWorkflowStatusEnum.COMPLETED) {
            lockLinkBuilder(html, studySubjectLabel, rowCount, crf, "images/CRF_status_icon_Complete_collapse.gif", "images/CRF_status_icon_Complete.gif");
        } else if (eventCrfBean.getWorkflowStatus() == EventCrfWorkflowStatusEnum.NOT_STARTED) {
            lockLinkBuilder(html, studySubjectLabel, rowCount, crf, "images/CRF_status_icon_Scheduled.gif", "images/CRF_status_icon_Scheduled.gif");
        } else if (eventCrfBean.getWorkflowStatus() == EventCrfWorkflowStatusEnum.INITIAL_DATA_ENTRY) {
            lockLinkBuilder(html, studySubjectLabel, rowCount, crf, "images/CRF_status_icon_Started.gif", "images/CRF_status_icon_Started.gif");
        }
        html.divEnd();

    }

    void buildEvent() {

        String studySubjectLabel = studySubject.getLabel();
        String tableHeaderRowLeftStyleClass = "table_header_row_left";
        String subjectText = reswords.getString("subject");
        String crfText = reswords.getString("CRF");

        // Event Div
        html.div().id("Event_" + studySubjectLabel + "_" + crf.getId() + "_" + rowCount)
                .style("position: absolute; visibility: hidden; z-index: 3;width:180px; top: 0px;").close();
        html.div().styleClass("box_T").close().div().styleClass("box_L").close().div().styleClass("box_R").close().div().styleClass("box_B").close().div()
                .styleClass("box_TL").close().div().styleClass("box_TR").close().div().styleClass("box_BL").close().div().styleClass("box_BR").close();

        html.div().styleClass("tablebox_center").close();
        html.div().styleClass("ViewSubjectsPopup").style("color:#5b5b5b").close();

        html.table(0).border("0").cellpadding("0").cellspacing("0").close();
        html.tr(0).valign("top").close();

        html.td(0).styleClass(tableHeaderRowLeftStyleClass).close();
        html.append(subjectText).append(": ").append(studySubjectLabel).br();
        html.append(crfText).append(": ").append(crf.getName()).br();

        html.append("Status").append(": ").append(eventCrfStatus.getName()).br();
        html.tdEnd();
        html.td(0).styleClass(tableHeaderRowLeftStyleClass).align("right").close();
        if ((eventCrfBean.getRemoved()!=null && eventCrfBean.getRemoved())) {
            linkBuilder(html, studySubjectLabel, rowCount, crf, "images/CRF_status_icon_Invalid.gif");
        } else if (eventCrfBean.getWorkflowStatus() == EventCrfWorkflowStatusEnum.COMPLETED) {
            linkBuilder(html, studySubjectLabel, rowCount, crf, "images/CRF_status_icon_Complete.gif");
        } else if (eventCrfBean.getWorkflowStatus() == EventCrfWorkflowStatusEnum.NOT_STARTED) {
            linkBuilder(html, studySubjectLabel, rowCount, crf, "images/CRF_status_icon_Scheduled.gif");
        } else if (eventCrfBean.getWorkflowStatus() == EventCrfWorkflowStatusEnum.INITIAL_DATA_ENTRY) {
            linkBuilder(html, studySubjectLabel, rowCount, crf, "images/CRF_status_icon_Started.gif");
        }
        html.tdEnd().trEnd(0);
        // html.table(0).border("0").cellpadding("0").cellspacing("0").close();

    }

    void clickToEnterData() {

        String click_to_enter_data = restexts.getString("click_to_enter_data");
        String to_use_another_version_click = restexts.getString("to_use_another_version");
        String in_order_to_enter_data_create_event = restexts.getString("in_order_to_enter_data_create_e");
        String click_for_more_options = restexts.getString("click_for_more_options");

        String table_cell_left = "table_cell_left";

        String studySubjectLabel = studySubject.getLabel();
        html.tr(0).id("Menu_off_" + studySubjectLabel + "_" + crf.getId() + "_" + rowCount).style("display: all").close();
        html.td(0).styleClass("table_cell_left").colspan("2").close();
            if (eventCrfBean.getWorkflowStatus()== EventCrfWorkflowStatusEnum.NOT_STARTED) {
                if (getStudyEvent() != null) {
                html.append("<i>" + click_to_enter_data).br();
                html.append(to_use_another_version_click + "</i>");
            } else {
                html.append("<i>" + in_order_to_enter_data_create_event + "</i>");
            }
        } else if (getStudyEvent() == null && eventCrfBean.getWorkflowStatus()== EventCrfWorkflowStatusEnum.NOT_STARTED) {
            html.append("<i>" + click_to_enter_data).br();
            html.append(to_use_another_version_click + "</i>");
        } else {
            html.append("<i>" + click_for_more_options + "</i>");
        }
        html.tdEnd().trEnd(0);

        html.tr(0).id("Menu_on_" + studySubjectLabel + "_" + crf.getId() + "_" + rowCount).style("display: none").close();
        html.td(0).colspan("2").close();
        html.table(0).border("0").cellpadding("0").cellspacing("0").close();

        Study subjectStudy=studyDao.findByPK(studySubject.getStudyId());


        if ((eventCrfBean.getRemoved()!=null && eventCrfBean.getRemoved())) {

            if (!hiddenCrf()) {
                html.tr(0).valign("top").close();
                html.td(0).styleClass(table_cell_left).close();
                viewSectionDataEntry(html, eventCrfBean, eventDefinitionCrf, getStudyEvent());
                html.nbsp().nbsp();
                viewSectionDataEntry(html, eventCrfBean, reswords.getString("view"), eventDefinitionCrf, getStudyEvent());
                html.tdEnd().trEnd(0);
            }
            if (studySubject.getStatus() != core.org.akaza.openclinica.bean.core.Status.DELETED && studySubject.getStatus() != core.org.akaza.openclinica.bean.core.Status.AUTO_DELETED
                    && !currentRole.isMonitor()) {
                html.tr(0).valign("top").close();
                html.td(0).styleClass(table_cell_left).close();
                restoreEventCrf(html, eventCrfBean, studySubject);
                html.nbsp().nbsp();
                restoreEventCrf(html, eventCrfBean, studySubject, reswords.getString("restore"));
                html.tdEnd().trEnd(0);
            }


        } else if (eventCrfBean.getWorkflowStatus() == EventCrfWorkflowStatusEnum.COMPLETED) {
            if (!hiddenCrf()) {
                html.tr(0).valign("top").close();
                html.td(0).styleClass(table_cell_left).close();
                viewEventCrfContentLink(html, studySubject, eventCrfBean, getStudyEvent());
                html.nbsp().nbsp();
                viewSectionDataEntry(html, eventCrfBean, reswords.getString("view"), eventDefinitionCrf, getStudyEvent());
                html.tdEnd().trEnd(0);
            }

            // if (currentStudy.getStatus() == Status.AVAILABLE && (currentRole.isDirector() ||
            // currentUser.isSysAdmin())) {
            if (!currentRole.isMonitor() && subjectStudy.getStatus() == Status.AVAILABLE) {
                if (!hiddenCrf()) {
                    html.tr(0).valign("top").close();
                    html.td(0).styleClass(table_cell_left).close();
                    initialDataEntryLink(html, eventCrfBean == null ? new EventCRFBean() : eventCrfBean, studySubject, eventDefinitionCrf, getStudyEvent());
                    html.nbsp().nbsp();
                    initialDataEntryLink(html, eventCrfBean == null ? new EventCRFBean() : eventCrfBean, studySubject, eventDefinitionCrf, getStudyEvent(),
                            reswords.getString("edit"));
                    html.nbsp().nbsp();
                    html.tdEnd().trEnd(0);
                }
                html.tr(0).valign("top").close();
                html.td(0).styleClass(table_cell_left).close();
                removeEventCrf(html, eventCrfBean, studySubject);
                html.nbsp().nbsp();
                removeEventCrf(html, eventCrfBean, studySubject, reswords.getString("remove"));
                html.tdEnd().trEnd(0);
            }
            // Delete the crf should be allowed for all user types and all roles except Monitor(https://jira.openclinica.com/browse/OC-8798)
            if (subjectStudy.getStatus() == Status.AVAILABLE && !currentRole.isMonitor()) {
                html.tr(0).valign("top").close();
                html.td(0).styleClass(table_cell_left).close();
                deleteEventCrf(html, eventCrfBean, studySubject);
                html.nbsp().nbsp();
                deleteEventCrf(html, eventCrfBean, studySubject, reswords.getString("delete"));
                html.tdEnd().trEnd(0);
            }
            if (subjectStudy.getStatus() == Status.AVAILABLE && !currentRole.isMonitor()) {
                html.tr(0).valign("top").close();
                html.td(0).styleClass(table_cell_left).close();
                reassignEventCrf(html, eventDefinitionCrf, eventCrfBean, crf, studySubject);
                html.nbsp().nbsp();
                reassignEventCrf(html, eventDefinitionCrf, eventCrfBean, crf, studySubject, reswords.getString("reassign"));
                html.tdEnd().trEnd(0);
            }

        } else if (eventCrfBean.getWorkflowStatus() == EventCrfWorkflowStatusEnum.NOT_STARTED) {
            if (!hiddenCrf()) {
                html.tr(0).valign("top").close();
                html.td(0).styleClass(table_cell_left).close();
                viewSectionDataEntryParameterized(html, eventCrfBean, eventDefinitionCrf, getStudyEvent());
                html.nbsp().nbsp();
                viewSectionDataEntryParameterized(html, eventCrfBean, eventDefinitionCrf, reswords.getString("view"), getStudyEvent());
                html.tdEnd().trEnd(0);

                if (getStudyEvent() != null && !currentRole.isMonitor() && subjectStudy.getStatus() == Status.AVAILABLE) {
                    html.tr(0).valign("top").close();
                    html.td(0).styleClass(table_cell_left).close();
                    initialDataEntryLink(html, eventCrfBean == null ? new EventCRFBean() : eventCrfBean, studySubject, eventDefinitionCrf, getStudyEvent());
                    html.nbsp().nbsp();
                    initialDataEntryLink(html, eventCrfBean == null ? new EventCRFBean() : eventCrfBean, studySubject, eventDefinitionCrf, getStudyEvent(),
                            reswords.getString("edit"));
                    html.tdEnd().trEnd(0);
                }
            }

        } else if (eventCrfBean.getWorkflowStatus() == EventCrfWorkflowStatusEnum.INITIAL_DATA_ENTRY) {
            if (!hiddenCrf()) {
                html.tr(0).valign("top").close();
                html.td(0).styleClass(table_cell_left).close();
                viewSectionDataEntry(html, eventCrfBean, eventDefinitionCrf, getStudyEvent());
                html.nbsp().nbsp();
                viewSectionDataEntry(html, eventCrfBean, reswords.getString("view"), eventDefinitionCrf, getStudyEvent());
                html.tdEnd().trEnd(0);
            }
            if (!currentRole.isMonitor() && subjectStudy.getStatus() == Status.AVAILABLE) {
                if (!hiddenCrf()) {
                    html.tr(0).valign("top").close();
                    html.td(0).styleClass(table_cell_left).close();
                    initialDataEntryLink(html, eventCrfBean == null ? new EventCRFBean() : eventCrfBean, studySubject, eventDefinitionCrf, getStudyEvent());
                    html.nbsp().nbsp();
                    initialDataEntryLink(html, eventCrfBean == null ? new EventCRFBean() : eventCrfBean, studySubject, eventDefinitionCrf, getStudyEvent(),
                            reswords.getString("edit"));
                    html.tdEnd().trEnd(0);
                }
            }
            if (subjectStudy.getStatus() == Status.AVAILABLE && !currentRole.isMonitor()) {
                html.tr(0).valign("top").close();
                html.td(0).styleClass(table_cell_left).close();
                removeEventCrf(html, eventCrfBean, studySubject);
                html.nbsp().nbsp();
                removeEventCrf(html, eventCrfBean, studySubject, reswords.getString("remove"));
                html.tdEnd().trEnd(0);
            }
            // Delete the crf should be allowed for all user types and all roles except Monitor(https://jira.openclinica.com/browse/OC-8798)
            if (subjectStudy.getStatus() == Status.AVAILABLE && !currentRole.isMonitor()) {
                html.tr(0).valign("top").close();
                html.td(0).styleClass(table_cell_left).close();
                deleteEventCrf(html, eventCrfBean, studySubject);
                html.nbsp().nbsp();
                deleteEventCrf(html, eventCrfBean, studySubject, reswords.getString("delete"));
                html.tdEnd().trEnd(0);
            }
            if (subjectStudy.getStatus() == Status.AVAILABLE && !currentRole.isMonitor()) {
                html.tr(0).valign("top").close();
                html.td(0).styleClass(table_cell_left).close();
                reassignEventCrf(html, eventDefinitionCrf, eventCrfBean, crf, studySubject);
                html.nbsp().nbsp();
                reassignEventCrf(html, eventDefinitionCrf, eventCrfBean, crf, studySubject, reswords.getString("reassign"));
                html.tdEnd().trEnd(0);
            }
        }
        html.tableEnd(0);
        html.tdEnd().trEnd(0).tableEnd(0);
        html.divEnd().divEnd().divEnd().divEnd().divEnd().divEnd().divEnd().divEnd().divEnd().divEnd().divEnd();

    }

    private StudyEventBean getStudyEventForThisEventCRF() {
        List<StudyEventBean> ses = this.studyEvents;
        for (StudyEventBean studyEvent : ses) {
            if (studyEvent.getId() == eventCrfBean.getStudyEventId())
                studyEvent.setStudyEventDefinition(this.studyEventDefinition);
            return studyEvent;
        }
        return null;
    }

    private String getCRFVersionOID() {

        for (CRFVersionBean crfV : (ArrayList<CRFVersionBean>) this.crf.getVersions()) {
            if (crfV.getId() == eventCrfBean.getCRFVersionId()) {
                return crfV.getOid();
            }
        }
        return null;
    }

    void buildEnd() {

        String studySubjectLabel = studySubject.getLabel();
        if ((eventCrfBean.getRemoved()!=null && eventCrfBean.getRemoved())) {
            iconLinkBuilder(html, studySubjectLabel, rowCount, crf, "images/CRF_status_icon_Invalid_expand.gif", "images/CRF_status_icon_Invalid.gif");
        } else if (eventCrfBean.getWorkflowStatus() == EventCrfWorkflowStatusEnum.COMPLETED) {
            iconLinkBuilder(html, studySubjectLabel, rowCount, crf, "images/CRF_status_icon_Complete_expand.gif", "images/CRF_status_icon_Complete.gif");
        } else if (eventCrfBean.getWorkflowStatus() == EventCrfWorkflowStatusEnum.NOT_STARTED) {
            iconLinkBuilder(html, studySubjectLabel, rowCount, crf, "images/CRF_status_icon_Scheduled.gif", "images/CRF_status_icon_Scheduled.gif");
        } else if (eventCrfBean.getWorkflowStatus() == EventCrfWorkflowStatusEnum.INITIAL_DATA_ENTRY) {
            iconLinkBuilder(html, studySubjectLabel, rowCount, crf, "images/CRF_status_icon_Started.gif", "images/CRF_status_icon_Started.gif");
        }
    }

    private void viewEventCrfContentLink(HtmlBuilder builder, StudySubjectBean studySubject, EventCRFBean eventCrf, StudyEventBean studyEvent) {
        String href = "ViewEventCRFContent?id=" + studySubject.getId() + "&eventCrfId=" + eventCrf.getId() + "&eventId=" + studyEvent.getId();
        builder.a().append(" class=\"accessCheck\"  ").href(href).close();
        builder.append("<span border=\"0\" align=\"left\" class=\"icon icon-search\"/>");
        builder.aEnd();
    }

    private void viewEventCrfContentLinkPrint(HtmlBuilder builder, StudySubjectBean studySubject, EventCRFBean eventCrf, StudyEventBean studyEvent) {
        String href = "ViewEventCRFContent?id=" + studySubject.getId() + "&eventCrfId=" + eventCrf.getId() + "&eventId=" + studyEvent.getId();
        builder.a().append(" class=\"accessCheck\"  ").href(href).close();
        builder.append("<span border=\"0\" align=\"left\" class=\"icon icon-print\"/>");
        builder.aEnd();
    }

    private void viewEventCrfContentLink(HtmlBuilder builder, StudySubjectBean studySubject, EventCRFBean eventCrf, StudyEventBean studyEvent, String link) {
        String href = "ViewEventCRFContent?id=" + studySubject.getId() + "&eventCrfId=" + eventCrf.getId() + "&eventId=" + studyEvent.getId();
        builder.a().append(" class=\"accessCheck\"  ").href(href).close();
        builder.append(link);
        builder.aEnd();
    }

    private void viewSectionDataEntry(HtmlBuilder builder, EventCRFBean eventCrf, String link, EventDefinitionCRFBean eventDefinitionCrf,
            StudyEventBean studyEvent) {
        int formLayoutId = 0;
        int eventCrfId = 0;
        int studyEventId = 0;
        if (eventCrf == null || eventCrf.getId() == 0) {
            formLayoutId = eventDefinitionCrf.getDefaultVersionId();
        } else {
            formLayoutId = eventCrf.getFormLayoutId();
            eventCrfId = eventCrf.getId();
        }
        if (studyEvent != null) {
            studyEventId = studyEvent.getId();
        }

        String href = "EnketoFormServlet?formLayoutId=" + formLayoutId + "&studyEventId=" + studyEventId + "&eventCrfId=" + eventCrfId + "&originatingPage="
                + "ListEventsForSubjects%3Fmodule=submit%26defId=" + studyEventDefinition.getId() + "&mode=view";
        builder.a().append(" class=\"accessCheck\"  ").href(href).close();
        builder.append(link);
        builder.aEnd();
    }

    private void viewSectionDataEntry(HtmlBuilder builder, EventCRFBean eventCrf, EventDefinitionCRFBean eventDefinitionCrf, StudyEventBean studyEvent) {
        int formLayoutId = 0;
        int eventCrfId = 0;
        int studyEventId = 0;
        if (eventCrf == null || eventCrf.getId() == 0) {
            formLayoutId = eventDefinitionCrf.getDefaultVersionId();
        } else {
            formLayoutId = eventCrf.getFormLayoutId();
            eventCrfId = eventCrf.getId();
        }
        if (studyEvent != null) {
            studyEventId = studyEvent.getId();
        }

        String href = "EnketoFormServlet?formLayoutId=" + formLayoutId + "&studyEventId=" + studyEventId + "&eventCrfId=" + eventCrfId + "&originatingPage="
                + "ListEventsForSubjects%3Fmodule=submit%26defId=" + studyEventDefinition.getId() + "&mode=view";
        builder.a().append(" class=\"accessCheck\"  ").href(href).close();
        builder.append("<span border=\"0\" align=\"left\" class=\"icon icon-search\"/>");
        builder.aEnd();
    }

    private void viewSectionDataEntryParameterized(HtmlBuilder builder, EventCRFBean eventCrf, EventDefinitionCRFBean eventDefinitionCrf, String link,
            StudyEventBean studyEvent) {
        int formLayoutId = 0;
        int eventCrfId = 0;
        int studyEventId = 0;
        if (eventCrf == null || eventCrf.getId() == 0) {
            formLayoutId = eventDefinitionCrf.getDefaultVersionId();
        } else {
            formLayoutId = eventCrf.getFormLayoutId();
            eventCrfId = eventCrf.getId();
        }
        if (studyEvent != null) {
            studyEventId = studyEvent.getId();
        }

        String href = "EnketoFormServlet?formLayoutId=" + formLayoutId + "&studyEventId=" + studyEventId + "&eventCrfId=" + eventCrfId + "&originatingPage="
                + "ListEventsForSubjects%3Fmodule=submit%26defId=" + studyEventDefinition.getId() + "&mode=view";
        builder.a().append(" class=\"accessCheck\"  ").href(href).close();
        builder.append(link);
        builder.aEnd();
    }

    private void viewSectionDataEntryParameterized(HtmlBuilder builder, EventCRFBean eventCrf, EventDefinitionCRFBean eventDefinitionCrf,
            StudyEventBean studyEvent) {
        int formLayoutId = 0;
        int eventCrfId = 0;
        int studyEventId = 0;
        if (eventCrf == null || eventCrf.getId() == 0) {
            formLayoutId = eventDefinitionCrf.getDefaultVersionId();
        } else {
            formLayoutId = eventCrf.getFormLayoutId();
            eventCrfId = eventCrf.getId();
        }
        if (studyEvent != null) {
            studyEventId = studyEvent.getId();
        }

        String href = "EnketoFormServlet?formLayoutId=" + formLayoutId + "&studyEventId=" + studyEventId + "&eventCrfId=" + eventCrfId + "&originatingPage="
                + "ListEventsForSubjects%3Fmodule=submit%26defId=" + studyEventDefinition.getId() + "&mode=view";
        builder.a().append(" class=\"accessCheck\"  ").href(href).close();
        builder.append("<span border=\"0\" align=\"left\" class=\"icon icon-search\"/>");
        builder.aEnd();
    }

    private void printDataEntry(HtmlBuilder builder, EventCRFBean eventCrf) {
        // String href = "javascript:openPrintWindow('/rest/clinicaldata/html/print/" +
        // this.currentStudy.getOid()+"/"+this.studySubject.getOid()+"/"+this.getStudyEvent().getStudyEventDefinition().getOid()+"["+this.getStudyEvent().getSampleOrdinal()+"]"+this.eventCrfBean.getCrfVersion().getOid()
        // + "')";
        String href = this.contextPath + "/rest/clinicaldata/html/print/" + this.currentStudy.getOc_oid() + "/" + this.studySubject.getOid() + "/"
                + this.getStudyEventForThisEventCRF().getStudyEventDefinition().getOid() + "[" + this.getStudyEventForThisEventCRF().getSampleOrdinal() + "]/"
                + getCRFVersionOID();

        builder.a().href(href).close();
        builder.append("<span border=\"0\" align=\"left\" class=\"icon icon-print\"/>");
        builder.aEnd();
    }

    private void printDataEntry(HtmlBuilder builder, EventCRFBean eventCrf, String link) {
        // String href = "javascript:openDocWindow('PrintDataEntry?eventCrfId=" + eventCrf.getId() + "')";
        String href = this.contextPath + "" + "/rest/clinicaldata/html/print/" + this.currentStudy.getOc_oid() + "/" + this.studySubject.getOid() + "/"
                + this.getStudyEventForThisEventCRF().getStudyEventDefinition().getOid() + "%5b" + this.getStudyEventForThisEventCRF().getSampleOrdinal()
                + "%5d/" + getCRFVersionOID();

        builder.a().href(href).close();
        builder.append(link);
        builder.aEnd();
    }

    private void printCrf(HtmlBuilder builder, EventDefinitionCRFBean eventDefinitionCrf) {
        // String href = "javascript:processPrintCRFRequest('rest/metadata/html/print/*/*/" +
        // eventDefinitionCrf.getDefaultVersionName() + "')";
        int sampleOrdinal = 1;
        if (getStudyEvent() != null) {
            sampleOrdinal = getStudyEvent().getSampleOrdinal();// this covers the events
        }
        String href = this.contextPath + "" + "/rest/clinicaldata/html/print/" + this.currentStudy.getOc_oid() + "/" + this.studySubject.getOid() + "/"
                + this.studyEventDefinition.getOid() + "%5b" + sampleOrdinal + "%5d/" + this.eventDefinitionCrf.getDefaultCRF().getOid();

        builder.a().href(href).close();
        builder.append("<span border=\"0\" align=\"left\" class=\"icon icon-print\"/>");
        builder.aEnd();
    }

    private void printCrf(HtmlBuilder builder, EventDefinitionCRFBean eventDefinitionCrf, String link) {
        // String href = "javascript:processPrintCRFRequest('rest/metadata/html/print/*/*/" +
        // eventDefinitionCrf.getDefaultVersionName() + "')";
        int sampleOrdinal = 1;
        if (getStudyEvent() != null) {
            sampleOrdinal = getStudyEvent().getSampleOrdinal();// this covers the events
        }
        String href = this.contextPath + "" + "/rest/clinicaldata/html/print/" + this.currentStudy.getOc_oid() + "/" + this.studySubject.getOid() + "/"
                + this.studyEventDefinition.getOid() + "%5b" + sampleOrdinal + "%5d/" + this.eventDefinitionCrf.getDefaultCRF().getOid();

        builder.a().href(href).close();
        builder.append(link);
        builder.aEnd();
    }

    private void administrativeEditing(HtmlBuilder builder, EventCRFBean eventCrf) {
        String href = "AdministrativeEditing?eventCRFId=" + eventCrf.getId() + "&exitTo=ListStudySubjects";
        builder.a().href(href).close();
        builder.img().src("images/bt_Edit.gif").border("0").align("left").close();
        builder.aEnd();
    }

    private void administrativeEditing(HtmlBuilder builder, EventCRFBean eventCrf, String link) {
        String href = "AdministrativeEditing?eventCRFId=" + eventCrf.getId() + "&exitTo=ListStudySubjects";
        builder.a().href(href).close();
        builder.append(link);
        builder.aEnd();
    }

    private void removeEventCrf(HtmlBuilder builder, EventCRFBean eventCrf, StudySubjectBean studySubject) {
        String href = "RemoveEventCRF?action=confirm&eventCrfId=" + eventCrf.getId() + "&studySubId=" + studySubject.getId();
        builder.a().append(" class=\"accessCheck\"  ").href(href).close();
        builder.append("<span border=\"0\" align=\"left\" class=\"icon icon-cancel\"/>");
        builder.aEnd();
    }

    private void removeEventCrf(HtmlBuilder builder, EventCRFBean eventCrf, StudySubjectBean studySubject, String link) {
        String href = "RemoveEventCRF?action=confirm&eventCrfId=" + eventCrf.getId() + "&studySubId=" + studySubject.getId();
        builder.a().append(" class=\"accessCheck\"  ").href(href).close();
        builder.append(link);
        builder.aEnd();
    }

    private void reassignEventCrf(HtmlBuilder builder, EventDefinitionCRFBean eventDefinitionCrf, EventCRFBean eventCrf, CRFBean crf, StudySubjectBean studySubject) {
        reassignEventCrf(builder, eventDefinitionCrf, eventCrf, crf, studySubject, "<span border=\"0\" align=\"left\" class=\"icon icon-icon-reassign3\"/>");
    }

    private void reassignEventCrf(HtmlBuilder builder, EventDefinitionCRFBean eventDefinitionCrf, EventCRFBean eventCrf, CRFBean crf, StudySubjectBean studySubject, String link) {
        int formLayoutId = 0;
        if (eventCrf == null || eventCrf.getId() == 0) {
            formLayoutId = eventDefinitionCrf.getDefaultVersionId();
        } else {
            formLayoutId = eventCrf.getFormLayoutId();
        }
        String href = "pages/managestudy/chooseCRFVersion?crfId=" + crf.getId() + "&crfName=" + crf.getName() + "&formLayoutId=" + Integer.toString(formLayoutId) + "&formLayoutName=" + Integer.toString(formLayoutId) + "&studySubjectLabel=" + studySubject.getLabel() + "&studySubjectId=" + studySubject.getId() + "&eventCrfId=" + eventCrf.getId() + "&eventDefinitionCRFId=" + eventDefinitionCrf.getId() + "&originatingPage=ListStudySubjects";
        builder.a().append(" class=\"accessCheck\"  ").href(href).close();
        builder.append(link);
        builder.aEnd();
    }

    private void restoreEventCrf(HtmlBuilder builder, EventCRFBean eventCrf, StudySubjectBean studySubject) {
        String href = "RestoreEventCRF?action=confirm&eventCrfId=" + eventCrf.getId() + "&studySubId=" + studySubject.getId();
        builder.a().append(" class=\"accessCheck\"  ").href(href).close();
        builder.append("<span border=\"0\" align=\"left\" class=\"icon icon-ccw\"/>");
        builder.aEnd();
    }

    private void restoreEventCrf(HtmlBuilder builder, EventCRFBean eventCrf, StudySubjectBean studySubject, String link) {
        String href = "RestoreEventCRF?action=confirm&eventCrfId=" + eventCrf.getId() + "&studySubId=" + studySubject.getId();
        builder.a().append(" class=\"accessCheck\"  ").href(href).close();
        builder.append(link);
        builder.aEnd();
    }

    private void deleteEventCrf(HtmlBuilder builder, EventCRFBean eventCrf, StudySubjectBean studySubject) {
        String href = "DeleteEventCRF?action=confirm&ssId=" + studySubject.getId() + "&eventCrfId=" + eventCrf.getId();
        builder.a().append(" class=\"accessCheck\"  ").href(href).close();
        builder.append("<span border=\"0\" align=\"left\" class=\"icon icon-trash red\"/>");
        builder.aEnd();
    }

    private void deleteEventCrf(HtmlBuilder builder, EventCRFBean eventCrf, StudySubjectBean studySubject, String link) {
        String href = "DeleteEventCRF?action=confirm&ssId=" + studySubject.getId() + "&eventCrfId=" + eventCrf.getId();
        builder.a().append(" class=\"accessCheck\"  ").href(href).close();
        builder.append(link);
        builder.aEnd();
    }

    private void initialDataEntryLink(HtmlBuilder builder, EventCRFBean eventCrf, StudySubjectBean studySubject, EventDefinitionCRFBean eventDefinitionCrf,
            StudyEventBean studyEvent) {
        int formLayoutId = 0;
        if (eventCrf == null || eventCrf.getId() == 0) {
            formLayoutId = eventDefinitionCrf.getDefaultVersionId();
        } else {
            formLayoutId = eventCrf.getFormLayoutId();
        }

        String href = "EnketoFormServlet?formLayoutId=" + formLayoutId + "&studyEventId=" + studyEvent.getId() + "&eventCrfId=" + eventCrf.getId()
                + "&originatingPage=" + "ListEventsForSubjects%3Fmodule=submit%26defId=" + studyEventDefinition.getId() + "&mode=edit";
        builder.a().append(" class=\"accessCheck\"  ").href(href).close();
        builder.append("<span border=\"0\" align=\"left\" class=\"icon icon-pencil\"/>");
        builder.aEnd();
    }

    private void initialDataEntryLink(HtmlBuilder builder, EventCRFBean eventCrf, StudySubjectBean studySubject, EventDefinitionCRFBean eventDefinitionCrf,
            StudyEventBean studyEvent, String link) {
        int formLayoutId = 0;
        if (eventCrf == null || eventCrf.getId() == 0) {
            formLayoutId = eventDefinitionCrf.getDefaultVersionId();
        } else {
            formLayoutId = eventCrf.getFormLayoutId();
        }
        String href = "EnketoFormServlet?formLayoutId=" + formLayoutId + "&studyEventId=" + studyEvent.getId() + "&eventCrfId=" + eventCrf.getId()
                + "&originatingPage=" + "ListEventsForSubjects%3Fmodule=submit%26defId=" + studyEventDefinition.getId() + "&mode=edit";
        builder.a().append(" class=\"accessCheck\"  ").href(href).close();
        builder.append(link);
        builder.aEnd();
    }

    private void initialDataEntryParameterizedLink(HtmlBuilder builder, EventCRFBean eventCrf) {
        String href = "InitialDataEntry?eventCRFId=" + eventCrf.getId() + "&exitTo=ListStudySubjects";
        builder.a().append(" class=\"accessCheck\"  ").href(href).close();
        builder.append("<span border=\"0\" align=\"left\" class=\"icon icon-pencil\"/>");
        builder.aEnd();
    }

    private void initialDataEntryParameterizedLink(HtmlBuilder builder, EventCRFBean eventCrf, String link) {
        String href = "InitialDataEntry?eventCRFId=" + eventCrf.getId() + "&exitTo=ListStudySubjects";
        builder.a().append(" class=\"accessCheck\"  ").href(href).close();
        builder.append(link);
        builder.aEnd();
    }

    private void doubleDataEntryLink(HtmlBuilder builder, EventCRFBean eventCrf) {
        String href = "DoubleDataEntry?eventCRFId= " + eventCrf.getId() + "&exitTo=ListStudySubjects";
        builder.a().href(href).close();
        builder.append("<span border=\"0\" align=\"left\" class=\"icon icon-pencil\"/>");
        builder.aEnd();
    }

    private void doubleDataEntryLink(HtmlBuilder builder, EventCRFBean eventCrf, String link) {
        String href = "DoubleDataEntry?eventCRFId= " + eventCrf.getId() + "&exitTo=ListStudySubjects";
        builder.a().href(href).close();
        builder.append(link);
        builder.aEnd();
    }

    private void linkBuilder(HtmlBuilder builder, String studySubjectLabel, Integer rowCount, CRFBean crf, String icon) {
        studySubjectLabel = studySubjectLabel.replaceAll("'", "\\\\'");
        String href1 = "javascript:leftnavExpand('Menu_on_" + studySubjectLabel + "_" + crf.getId() + "_" + rowCount + "'); ";
        String href2 = "javascript:leftnavExpand('Menu_off_" + studySubjectLabel + "_" + crf.getId() + "_" + rowCount + "'); ";
        String onClick1 = "layersShowOrHide('hidden','Lock_all'); ";
        String onClick2 = "layersShowOrHide('hidden','Event_" + studySubjectLabel + "_" + crf.getId() + "_" + rowCount + "'); ";
        String onClick3 = "layersShowOrHide('hidden','Lock_" + studySubjectLabel + "_" + crf.getId() + "_" + rowCount + "'); ";
        String onClick4 = "javascript:setImage('CRFicon_" + studySubjectLabel + "_" + crf.getId() + "_" + rowCount + "','" + icon + "'); ";
        builder.a().href(href1 + href2);
        builder.onclick(onClick1 + onClick2 + onClick3 + onClick4);
        builder.close().append("X").aEnd();

    }

    private void lockLinkBuilder(HtmlBuilder builder, String studySubjectLabel, Integer rowCount, CRFBean crf, String collapsedIcon, String icon) {
        studySubjectLabel = studySubjectLabel.replaceAll("'", "\\\\'");
        String href1 = "javascript:leftnavExpand('Menu_on_" + studySubjectLabel + "_" + crf.getId() + "_" + rowCount + "'); ";
        String href2 = "javascript:leftnavExpand('Menu_off_" + studySubjectLabel + "_" + crf.getId() + "_" + rowCount + "'); ";
        String onmouseover = "layersShowOrHide('visible','Event_" + studySubjectLabel + "_" + crf.getId() + "_" + rowCount + "'); ";
        onmouseover += "javascript:setImage('CRFicon_" + studySubjectLabel + "_" + crf.getId() + "_" + rowCount + "','" + collapsedIcon + "');";
        String onClick1 = "layersShowOrHide('hidden','Lock_all'); ";
        String onClick2 = "layersShowOrHide('hidden','Event_" + studySubjectLabel + "_" + crf.getId() + "_" + rowCount + "'); ";
        String onClick3 = "layersShowOrHide('hidden','Lock_" + studySubjectLabel + "_" + crf.getId() + "_" + rowCount + "'); ";
        String onClick4 = "javascript:setImage('CRFicon_" + studySubjectLabel + "_" + crf.getId() + "_" + rowCount + "','" + icon + "'); ";
        builder.a().href(href1 + href2);
        builder.onclick(onmouseover + onClick1 + onClick2 + onClick3 + onClick4);
        builder.close();
        builder.img().src("images/spacer.gif").border("0").append("height=\"30\"").width("144").close().aEnd();

    }

    private void iconLinkBuilder(HtmlBuilder builder, String studySubjectLabel, Integer rowCount, CRFBean crf, String expandedIcon, String icon) {
        studySubjectLabel = studySubjectLabel.replaceAll("'", "\\\\'");
        String href1 = "javascript:leftnavExpand('Menu_on_" + studySubjectLabel + "_" + crf.getId() + "_" + rowCount + "'); ";
        String href2 = "javascript:leftnavExpand('Menu_off_" + studySubjectLabel + "_" + crf.getId() + "_" + rowCount + "'); ";
        String onmouseover = "moveObject('Event_" + studySubjectLabel + "_" + crf.getId() + "_" + rowCount + "', event); ";
        onmouseover += "setImage('CRFicon" + studySubjectLabel + "_" + crf.getId() + "_" + rowCount + "','" + expandedIcon + "');";
        String onmouseout = "layersShowOrHide('hidden','Event_" + studySubjectLabel + "_" + crf.getId() + "_" + rowCount + "'); ";
        onmouseout += "setImage('CRFicon_" + studySubjectLabel + "_" + crf.getId() + "_" + rowCount + "','" + icon + "');";
        String onClick1 = "layersShowOrHide('visible','Lock_all'); ";
        String onClick2 = "LockObject('Lock_" + studySubjectLabel + "_" + crf.getId() + "_" + rowCount + "',event); ";
        builder.a().href(href1 + href2);
        builder.onclick(onmouseover + onClick1 + onClick2);
        builder.close();

    }

    private boolean hiddenCrf() {
        if (currentStudy.isSite() && eventDefinitionCrf.isHideCrf()) {
            return true;
        }

        return false;
    }
}
