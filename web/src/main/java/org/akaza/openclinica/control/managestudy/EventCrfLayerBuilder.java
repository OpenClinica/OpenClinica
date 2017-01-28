package org.akaza.openclinica.control.managestudy;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.jmesa.view.html.HtmlBuilder;

public class EventCrfLayerBuilder {

    HtmlBuilder html;
    SubjectBean subject;
    Integer rowCount;
    List<StudyEventBean> studyEvents;
    DataEntryStage eventCrfStatus;
    EventCRFBean eventCrfBean;
    StudySubjectBean studySubject;
    StudyBean currentStudy;
    StudyUserRoleBean currentRole;
    UserAccountBean currentUser;
    EventDefinitionCRFBean eventDefinitionCrf;
    CRFBean crf;
    StudyEventDefinitionBean studyEventDefinition;
    private ResourceBundle reswords = ResourceBundleProvider.getWordsBundle();
    private ResourceBundle restexts = ResourceBundleProvider.getTextsBundle();
    String contextPath;

    public EventCrfLayerBuilder(SubjectBean subject, Integer rowCount, List<StudyEventBean> studyEvents, DataEntryStage eventCrfStatus,
            EventCRFBean eventCrfBean, StudySubjectBean studySubject, StudyBean currentStudy, StudyUserRoleBean currentRole, UserAccountBean currentUser,
            EventDefinitionCRFBean eventDefinitionCrf, CRFBean crf, StudyEventDefinitionBean studyEventDefinition, String contextPath) {
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
    }

    StudyEventBean getStudyEvent() {
        return studyEvents.size() < 1 ? null : studyEvents.get(0);
    }

    String buid() {
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
        if (eventCrfStatus == DataEntryStage.ADMINISTRATIVE_EDITING) {
            lockLinkBuilder(html, studySubjectLabel, rowCount, crf, "images/CRF_status_icon_Complete_collapse.gif", "images/CRF_status_icon_Complete.gif");
        } else if (eventCrfStatus == DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE) {
            lockLinkBuilder(html, studySubjectLabel, rowCount, crf, "images/CRF_status_icon_InitialDEcomplete_collapse.gif",
                    "images/CRF_status_icon_InitialDEcomplete.gif");
        } else if (eventCrfStatus == DataEntryStage.DOUBLE_DATA_ENTRY) {
            lockLinkBuilder(html, studySubjectLabel, rowCount, crf, "images/CRF_status_icon_DDE_collapse.gif", "images/CRF_status_icon_DDE.gif");
        } else if (eventCrfStatus == DataEntryStage.LOCKED) {
            // Do Nothing
        } else if (eventCrfStatus == DataEntryStage.UNCOMPLETED) {
            lockLinkBuilder(html, studySubjectLabel, rowCount, crf, "images/CRF_status_icon_Scheduled.gif", "images/CRF_status_icon_Scheduled.gif");
        } else if (eventCrfStatus == DataEntryStage.INVALID) {
            lockLinkBuilder(html, studySubjectLabel, rowCount, crf, "images/CRF_status_icon_Invalid_collapse.gif", "images/CRF_status_icon_Invalid.gif");
        } else {
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
        if (eventCrfStatus == DataEntryStage.ADMINISTRATIVE_EDITING) {
            linkBuilder(html, studySubjectLabel, rowCount, crf, "images/CRF_status_icon_Complete.gif");
        } else if (eventCrfStatus == DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE) {
            linkBuilder(html, studySubjectLabel, rowCount, crf, "images/CRF_status_icon_InitialDEcomplete.gif");
        } else if (eventCrfStatus == DataEntryStage.DOUBLE_DATA_ENTRY) {
            linkBuilder(html, studySubjectLabel, rowCount, crf, "images/CRF_status_icon_DDE.gif");
        } else if (eventCrfStatus == DataEntryStage.UNCOMPLETED) {
            linkBuilder(html, studySubjectLabel, rowCount, crf, "images/CRF_status_icon_Scheduled.gif");
        } else if (eventCrfStatus == DataEntryStage.LOCKED) {
            // Do Nothing
        } else if (eventCrfStatus == DataEntryStage.INVALID) {
            linkBuilder(html, studySubjectLabel, rowCount, crf, "images/CRF_status_icon_Invalid.gif");
        } else {
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
        if (eventCrfStatus == DataEntryStage.UNCOMPLETED) {
            if (getStudyEvent() != null) {
                html.append("<i>" + click_to_enter_data).br();
                html.append(to_use_another_version_click + "</i>");
            } else {
                html.append("<i>" + in_order_to_enter_data_create_event + "</i>");
            }
        } else if (getStudyEvent() == null && eventCrfStatus == DataEntryStage.UNCOMPLETED) {
            html.append("<i>" + click_to_enter_data).br();
            html.append(to_use_another_version_click + "</i>");
        } else {
            html.append("<i>" + click_for_more_options + "</i>");
        }
        html.tdEnd().trEnd(0);

        html.tr(0).id("Menu_on_" + studySubjectLabel + "_" + crf.getId() + "_" + rowCount).style("display: none").close();
        html.td(0).colspan("2").close();
        html.table(0).border("0").cellpadding("0").cellspacing("0").close();
        if (eventCrfStatus == DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE || eventCrfStatus == DataEntryStage.ADMINISTRATIVE_EDITING) {

            if (!hiddenCrf()) {
                html.tr(0).valign("top").close();
                html.td(0).styleClass(table_cell_left).close();
                viewEventCrfContentLink(html, studySubject, eventCrfBean, getStudyEvent());
                html.nbsp().nbsp();
                viewSectionDataEntry(html, eventCrfBean, reswords.getString("view"), eventDefinitionCrf);
                html.tdEnd().trEnd(0);
                html.tr(0).valign("top").close();
                html.td(0).styleClass(table_cell_left).close();
                printDataEntry(html, eventCrfBean);
                html.nbsp().nbsp();
                printDataEntry(html, eventCrfBean, reswords.getString("print"));
                html.tdEnd().trEnd(0);
            }

            // if (currentStudy.getStatus() == Status.AVAILABLE && (currentRole.isDirector() ||
            // currentUser.isSysAdmin())) {
            if (!currentRole.isMonitor() && currentStudy.getStatus() == Status.AVAILABLE) {
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
                if (currentRole.isDirector() || currentUser.isSysAdmin()) {
                    html.tr(0).valign("top").close();
                    html.td(0).styleClass(table_cell_left).close();
                    removeEventCrf(html, eventCrfBean, studySubject);
                    html.nbsp().nbsp();
                    removeEventCrf(html, eventCrfBean, studySubject, reswords.getString("remove"));
                    html.tdEnd().trEnd(0);
                }
            }
            if (currentStudy.getStatus() == Status.AVAILABLE && currentUser.isSysAdmin()) {
                html.tr(0).valign("top").close();
                html.td(0).styleClass(table_cell_left).close();
                deleteEventCrf(html, eventCrfBean, studySubject);
                html.nbsp().nbsp();
                deleteEventCrf(html, eventCrfBean, studySubject, reswords.getString("delete"));
                html.tdEnd().trEnd(0);
            }
        } else if (eventCrfStatus == DataEntryStage.LOCKED) {
            if (!hiddenCrf()) {
                html.tr(0).valign("top").close();
                html.td(0).styleClass(table_cell_left).close();
                viewEventCrfContentLink(html, studySubject, eventCrfBean, getStudyEvent());
                html.nbsp().nbsp();
                viewSectionDataEntry(html, eventCrfBean, reswords.getString("view"), eventDefinitionCrf);
                html.tdEnd().trEnd(0);
                html.tr(0).valign("top").close();
                html.td(0).styleClass(table_cell_left).close();
                viewEventCrfContentLinkPrint(html, studySubject, eventCrfBean, getStudyEvent());
                html.nbsp().nbsp();
                viewEventCrfContentLink(html, studySubject, eventCrfBean, getStudyEvent(), reswords.getString("print"));
                html.tdEnd().trEnd(0);
            }
            if (currentStudy.getStatus() == Status.AVAILABLE && (currentRole.isDirector() || currentUser.isSysAdmin())) {
                html.tr(0).valign("top").close();
                html.td(0).styleClass(table_cell_left).close();
                removeEventCrf(html, eventCrfBean, studySubject);
                html.nbsp().nbsp();
                removeEventCrf(html, eventCrfBean, studySubject, reswords.getString("remove"));
                html.tdEnd().trEnd(0);
            }
        } else if (eventCrfStatus == DataEntryStage.INITIAL_DATA_ENTRY || eventCrfStatus == DataEntryStage.UNCOMPLETED) {
            if (getStudyEvent() != null && !currentRole.isMonitor() && currentStudy.getStatus() == Status.AVAILABLE) {
                if (!hiddenCrf()) {
                    html.tr(0).valign("top").close();
                    html.td(0).styleClass(table_cell_left).close();
                    initialDataEntryLink(html, eventCrfBean == null ? new EventCRFBean() : eventCrfBean, studySubject, eventDefinitionCrf, getStudyEvent());
                    html.nbsp().nbsp();
                    initialDataEntryLink(html, eventCrfBean == null ? new EventCRFBean() : eventCrfBean, studySubject, eventDefinitionCrf, getStudyEvent(),
                            reswords.getString("enter_data"));
                    html.tdEnd().trEnd(0);
                }
            }

            if (!hiddenCrf()) {
                html.tr(0).valign("top").close();
                html.td(0).styleClass(table_cell_left).close();
                viewSectionDataEntryParameterized(html, eventDefinitionCrf);
                html.nbsp().nbsp();
                viewSectionDataEntryParameterized(html, eventDefinitionCrf, reswords.getString("view"));
                html.tdEnd().trEnd(0);
                html.tr(0).valign("top").close();
                html.td(0).styleClass(table_cell_left).close();
                if (eventCrfBean == null)
                    printCrf(html, eventDefinitionCrf);
                else
                    printDataEntry(html, eventCrfBean);
                html.nbsp().nbsp();
                if (eventCrfBean == null)
                    printCrf(html, eventDefinitionCrf, reswords.getString("print"));
                else
                    printDataEntry(html, eventCrfBean, reswords.getString("print"));
                html.tdEnd().trEnd(0);
            }
        } else if (eventCrfStatus == DataEntryStage.INVALID) {
            if (!hiddenCrf()) {
                html.tr(0).valign("top").close();
                html.td(0).styleClass(table_cell_left).close();
                viewSectionDataEntry(html, eventCrfBean, eventDefinitionCrf);
                html.nbsp().nbsp();
                viewSectionDataEntry(html, eventCrfBean, reswords.getString("view"), eventDefinitionCrf);
                html.tdEnd().trEnd(0);
                html.tr(0).valign("top").close();
                html.td(0).styleClass(table_cell_left).close();
                printDataEntry(html, eventCrfBean);
                html.nbsp().nbsp();
                printDataEntry(html, eventCrfBean, reswords.getString("print"));
                html.tdEnd().trEnd(0);
            }
            if (studySubject.getStatus() != Status.DELETED && studySubject.getStatus() != Status.AUTO_DELETED
                    && (currentRole.isDirector() || currentUser.isSysAdmin())) {
                html.tr(0).valign("top").close();
                html.td(0).styleClass(table_cell_left).close();
                restoreEventCrf(html, eventCrfBean, studySubject);
                html.nbsp().nbsp();
                restoreEventCrf(html, eventCrfBean, studySubject, "Restore");
                html.tdEnd().trEnd(0);
            }
        } else {
            if (!currentRole.isMonitor() && currentStudy.getStatus() == Status.AVAILABLE) {
                if (eventCrfStatus == DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE || eventCrfStatus == DataEntryStage.DOUBLE_DATA_ENTRY) {
                    if (!hiddenCrf()) {
                        html.tr(0).valign("top").close();
                        html.td(0).styleClass(table_cell_left).close();
                        doubleDataEntryLink(html, eventCrfBean);
                        html.nbsp().nbsp();
                        doubleDataEntryLink(html, eventCrfBean, reswords.getString("enter_data"));
                        html.tdEnd().trEnd(0);
                    }
                } else {
                    if (!hiddenCrf()) {
                        html.tr(0).valign("top").close();
                        html.td(0).styleClass(table_cell_left).close();
                        initialDataEntryParameterizedLink(html, eventCrfBean);
                        html.nbsp().nbsp();
                        initialDataEntryParameterizedLink(html, eventCrfBean, reswords.getString("enter_data"));
                        html.tdEnd().trEnd(0);
                    }
                }
            }
            if (!hiddenCrf()) {
                html.tr(0).valign("top").close();
                html.td(0).styleClass(table_cell_left).close();
                viewSectionDataEntry(html, eventCrfBean, eventDefinitionCrf);
                html.nbsp().nbsp();
                viewSectionDataEntry(html, eventCrfBean, reswords.getString("view"), eventDefinitionCrf);
                html.tdEnd().trEnd(0);
                html.tr(0).valign("top").close();
                html.td(0).styleClass(table_cell_left).close();
                printDataEntry(html, eventCrfBean);
                html.nbsp().nbsp();
                printDataEntry(html, eventCrfBean, reswords.getString("print"));
                html.tdEnd().trEnd(0);
            }
            if (currentStudy.getStatus() == Status.AVAILABLE && (currentRole.isDirector() || currentUser.isSysAdmin())) {
                html.tr(0).valign("top").close();
                html.td(0).styleClass(table_cell_left).close();
                removeEventCrf(html, eventCrfBean, studySubject);
                html.nbsp().nbsp();
                removeEventCrf(html, eventCrfBean, studySubject, reswords.getString("remove"));
                html.tdEnd().trEnd(0);
            }
            if (currentStudy.getStatus() == Status.AVAILABLE && currentUser.isSysAdmin()) {
                html.tr(0).valign("top").close();
                html.td(0).styleClass(table_cell_left).close();
                deleteEventCrf(html, eventCrfBean, studySubject);
                html.nbsp().nbsp();
                deleteEventCrf(html, eventCrfBean, studySubject, reswords.getString("delete"));
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
        if (eventCrfStatus == DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE || eventCrfStatus == DataEntryStage.ADMINISTRATIVE_EDITING) {
            iconLinkBuilder(html, studySubjectLabel, rowCount, crf, "images/CRF_status_icon_Complete_expand.gif", "images/CRF_status_icon_Complete.gif");
        } else if (eventCrfStatus == DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE) {
            iconLinkBuilder(html, studySubjectLabel, rowCount, crf, "images/CRF_status_icon_InitialDEcomplete_expand.gif",
                    "images/CRF_status_icon_InitialDEcomplete.gif");
        } else if (eventCrfStatus == DataEntryStage.LOCKED) {
            // Do Nothing
        } else if (eventCrfStatus == DataEntryStage.UNCOMPLETED) {
            iconLinkBuilder(html, studySubjectLabel, rowCount, crf, "images/CRF_status_icon_Scheduled.gif", "images/CRF_status_icon_Scheduled.gif");
        } else if (eventCrfStatus == DataEntryStage.INVALID) {
            iconLinkBuilder(html, studySubjectLabel, rowCount, crf, "images/CRF_status_icon_Invalid_expand.gif", "images/CRF_status_icon_Invalid.gif");
        } else {
            iconLinkBuilder(html, studySubjectLabel, rowCount, crf, "images/CRF_status_icon_Started.gif", "images/CRF_status_icon_Started.gif");
        }
    }

    private void viewEventCrfContentLink(HtmlBuilder builder, StudySubjectBean studySubject, EventCRFBean eventCrf, StudyEventBean studyEvent) {
        String href = "ViewEventCRFContent?id=" + studySubject.getId() + "&ecId=" + eventCrf.getId() + "&eventId=" + studyEvent.getId();
        builder.a().href(href).close();
        builder.img().src("images/bt_View.gif").border("0").align("left").close();
        builder.aEnd();
    }

    private void viewEventCrfContentLinkPrint(HtmlBuilder builder, StudySubjectBean studySubject, EventCRFBean eventCrf, StudyEventBean studyEvent) {
        String href = "ViewEventCRFContent?id=" + studySubject.getId() + "&ecId=" + eventCrf.getId() + "&eventId=" + studyEvent.getId();
        builder.a().href(href).close();
        builder.img().src("images/bt_Print.gif").border("0").align("left").close();
        builder.aEnd();
    }

    private void viewEventCrfContentLink(HtmlBuilder builder, StudySubjectBean studySubject, EventCRFBean eventCrf, StudyEventBean studyEvent, String link) {
        String href = "ViewEventCRFContent?id=" + studySubject.getId() + "&ecId=" + eventCrf.getId() + "&eventId=" + studyEvent.getId();
        builder.a().href(href).close();
        builder.append(link);
        builder.aEnd();
    }

    private void viewSectionDataEntry(HtmlBuilder builder, EventCRFBean eventCrf, String link, EventDefinitionCRFBean eventDefinitionCrf) {
        String href = "ViewSectionDataEntry?eventDefinitionCRFId=" + eventDefinitionCrf.getId() + "&ecId=" + eventCrf.getId() + "&tabId=1"
                + "&exitTo=ListStudySubjects";
        builder.a().href(href).close();
        builder.append(link);
        builder.aEnd();
    }

    private void viewSectionDataEntry(HtmlBuilder builder, EventCRFBean eventCrf, EventDefinitionCRFBean eventDefinitionCrf) {
        String href = "ViewSectionDataEntry?eventDefinitionCRFId=" + eventDefinitionCrf.getId() + "&ecId=" + eventCrf.getId() + "&tabId=1"
                + "&exitTo=ListStudySubjects";
        builder.a().href(href).close();
        builder.img().src("images/bt_View.gif").border("0").align("left").close();
        builder.aEnd();
    }

    private void viewSectionDataEntryParameterized(HtmlBuilder builder, EventDefinitionCRFBean eventDefinitionCrf, String link) {
        String href = "ViewSectionDataEntry?eventDefinitionCRFId=" + eventDefinitionCrf.getId() + "&crfVersionId=" + eventDefinitionCrf.getDefaultVersionId()
                + "&tabId=1" + "&exitTo=ListStudySubjects";
        builder.a().href(href).close();
        builder.append(link);
        builder.aEnd();
    }

    private void viewSectionDataEntryParameterized(HtmlBuilder builder, EventDefinitionCRFBean eventDefinitionCrf) {
        String href = "ViewSectionDataEntry?eventDefinitionCRFId=" + eventDefinitionCrf.getId() + "&crfVersionId=" + eventDefinitionCrf.getDefaultVersionId()
                + "&tabId=1" + "&exitTo=ListStudySubjects";
        builder.a().href(href).close();
        builder.img().src("images/bt_View.gif").border("0").align("left").close();
        builder.aEnd();
    }

    private void printDataEntry(HtmlBuilder builder, EventCRFBean eventCrf) {
        // String href = "javascript:openPrintWindow('/rest/clinicaldata/html/print/" +
        // this.currentStudy.getOid()+"/"+this.studySubject.getOid()+"/"+this.getStudyEvent().getStudyEventDefinition().getOid()+"["+this.getStudyEvent().getSampleOrdinal()+"]"+this.eventCrfBean.getCrfVersion().getOid()
        // + "')";
        String href = this.contextPath + "/rest/clinicaldata/html/print/" + this.currentStudy.getOid() + "/" + this.studySubject.getOid() + "/"
                + this.getStudyEventForThisEventCRF().getStudyEventDefinition().getOid() + "[" + this.getStudyEventForThisEventCRF().getSampleOrdinal() + "]/"
                + getCRFVersionOID();

        builder.a().href(href).close();
        builder.img().src("images/bt_Print.gif").border("0").align("left").close();
        builder.aEnd();
    }

    private void printDataEntry(HtmlBuilder builder, EventCRFBean eventCrf, String link) {
        // String href = "javascript:openDocWindow('PrintDataEntry?ecId=" + eventCrf.getId() + "')";
        String href = this.contextPath + "" + "/rest/clinicaldata/html/print/" + this.currentStudy.getOid() + "/" + this.studySubject.getOid() + "/"
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
        String href = this.contextPath + "" + "/rest/clinicaldata/html/print/" + this.currentStudy.getOid() + "/" + this.studySubject.getOid() + "/"
                + this.studyEventDefinition.getOid() + "%5b" + sampleOrdinal + "%5d/" + this.eventDefinitionCrf.getDefaultCRF().getOid();

        builder.a().href(href).close();
        builder.img().src("images/bt_Print.gif").border("0").align("left").close();
        builder.aEnd();
    }

    private void printCrf(HtmlBuilder builder, EventDefinitionCRFBean eventDefinitionCrf, String link) {
        // String href = "javascript:processPrintCRFRequest('rest/metadata/html/print/*/*/" +
        // eventDefinitionCrf.getDefaultVersionName() + "')";
        int sampleOrdinal = 1;
        if (getStudyEvent() != null) {
            sampleOrdinal = getStudyEvent().getSampleOrdinal();// this covers the events
        }
        String href = this.contextPath + "" + "/rest/clinicaldata/html/print/" + this.currentStudy.getOid() + "/" + this.studySubject.getOid() + "/"
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
        String href = "RemoveEventCRF?action=confirm&id=" + eventCrf.getId() + "&studySubId=" + studySubject.getId();
        builder.a().href(href).close();
        builder.img().src("images/bt_Remove.gif").border("0").align("left").close();
        builder.aEnd();
    }

    private void removeEventCrf(HtmlBuilder builder, EventCRFBean eventCrf, StudySubjectBean studySubject, String link) {
        String href = "RemoveEventCRF?action=confirm&id=" + eventCrf.getId() + "&studySubId=" + studySubject.getId();
        builder.a().href(href).close();
        builder.append(link);
        builder.aEnd();
    }

    private void restoreEventCrf(HtmlBuilder builder, EventCRFBean eventCrf, StudySubjectBean studySubject) {
        String href = "RestoreEventCRF?action=confirm&id=" + eventCrf.getId() + "&studySubId=" + studySubject.getId();
        builder.a().href(href).close();
        builder.img().src("images/bt_Restore.gif").border("0").align("left").close();
        builder.aEnd();
    }

    private void restoreEventCrf(HtmlBuilder builder, EventCRFBean eventCrf, StudySubjectBean studySubject, String link) {
        String href = "RestoreEventCRF?action=confirm&id=" + eventCrf.getId() + "&studySubId=" + studySubject.getId();
        builder.a().href(href).close();
        builder.append(link);
        builder.aEnd();
    }

    private void deleteEventCrf(HtmlBuilder builder, EventCRFBean eventCrf, StudySubjectBean studySubject) {
        String href = "DeleteEventCRF?action=confirm&ssId=" + studySubject.getId() + "&ecId=" + eventCrf.getId();
        builder.a().href(href).close();
        builder.img().src("images/bt_Delete.gif").border("0").align("left").close();
        builder.aEnd();
    }

    private void deleteEventCrf(HtmlBuilder builder, EventCRFBean eventCrf, StudySubjectBean studySubject, String link) {
        String href = "DeleteEventCRF?action=confirm&ssId=" + studySubject.getId() + "&ecId=" + eventCrf.getId();
        builder.a().href(href).close();
        builder.append(link);
        builder.aEnd();
    }

    private void initialDataEntryLink(HtmlBuilder builder, EventCRFBean eventCrf, StudySubjectBean studySubject, EventDefinitionCRFBean eventDefinitionCrf,
            StudyEventBean studyEvent) {
        String href = "EnketoFormServlet?formLayoutId=" + eventDefinitionCrf.getDefaultVersionId() + "&studyEventId=" + studyEvent.getId() + "&eventCrfId="
                + eventCrf.getId() + "&originatingPage=" + "ListEventsForSubjects%3Fmodule=submit%26defId=" + studyEventDefinition.getId();
        builder.a().href(href).close();
        builder.img().src("images/bt_Edit.gif").border("0").align("left").close();
        builder.aEnd();
    }

    private void initialDataEntryLink(HtmlBuilder builder, EventCRFBean eventCrf, StudySubjectBean studySubject, EventDefinitionCRFBean eventDefinitionCrf,
            StudyEventBean studyEvent, String link) {
        String href = "EnketoFormServlet?formLayoutId=" + eventDefinitionCrf.getDefaultVersionId() + "&studyEventId=" + studyEvent.getId() + "&eventCrfId="
                + eventCrf.getId() + "&originatingPage=" + "ListEventsForSubjects%3Fmodule=submit%26defId=" + studyEventDefinition.getId();
        builder.a().href(href).close();
        builder.append(link);
        builder.aEnd();
    }

    private void initialDataEntryParameterizedLink(HtmlBuilder builder, EventCRFBean eventCrf) {
        String href = "InitialDataEntry?eventCRFId=" + eventCrf.getId() + "&exitTo=ListStudySubjects";
        builder.a().href(href).close();
        builder.img().src("images/bt_Edit.gif").border("0").align("left").close();
        builder.aEnd();
    }

    private void initialDataEntryParameterizedLink(HtmlBuilder builder, EventCRFBean eventCrf, String link) {
        String href = "InitialDataEntry?eventCRFId=" + eventCrf.getId() + "&exitTo=ListStudySubjects";
        builder.a().href(href).close();
        builder.append(link);
        builder.aEnd();
    }

    private void doubleDataEntryLink(HtmlBuilder builder, EventCRFBean eventCrf) {
        String href = "DoubleDataEntry?eventCRFId= " + eventCrf.getId() + "&exitTo=ListStudySubjects";
        builder.a().href(href).close();
        builder.img().src("images/bt_Edit.gif").border("0").align("left").close();
        builder.aEnd();
    }

    private void doubleDataEntryLink(HtmlBuilder builder, EventCRFBean eventCrf, String link) {
        String href = "DoubleDataEntry?eventCRFId= " + eventCrf.getId() + "&exitTo=ListStudySubjects";
        builder.a().href(href).close();
        builder.append(link);
        builder.aEnd();
    }

    private void linkBuilder(HtmlBuilder builder, String studySubjectLabel, Integer rowCount, CRFBean crf, String icon) {
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
        String href1 = "javascript:leftnavExpand('Menu_on_" + studySubjectLabel + "_" + crf.getId() + "_" + rowCount + "'); ";
        String href2 = "javascript:leftnavExpand('Menu_off_" + studySubjectLabel + "_" + crf.getId() + "_" + rowCount + "'); ";
        String onmouseover = "layersShowOrHide('visible','Event_" + studySubjectLabel + "_" + crf.getId() + "_" + rowCount + "'); ";
        onmouseover += "javascript:setImage('CRFicon_" + studySubjectLabel + "_" + crf.getId() + "_" + rowCount + "','" + collapsedIcon + "');";
        String onClick1 = "layersShowOrHide('hidden','Lock_all'); ";
        String onClick2 = "layersShowOrHide('hidden','Event_" + studySubjectLabel + "_" + crf.getId() + "_" + rowCount + "'); ";
        String onClick3 = "layersShowOrHide('hidden','Lock_" + studySubjectLabel + "_" + crf.getId() + "_" + rowCount + "'); ";
        String onClick4 = "javascript:setImage('CRFicon_" + studySubjectLabel + "_" + crf.getId() + "_" + rowCount + "','" + icon + "'); ";
        builder.a().href(href1 + href2);
        builder.onmouseover(onmouseover);
        builder.onclick(onClick1 + onClick2 + onClick3 + onClick4);
        builder.close();
        builder.img().src("images/spacer.gif").border("0").append("height=\"30\"").width("144").close().aEnd();

    }

    private void iconLinkBuilder(HtmlBuilder builder, String studySubjectLabel, Integer rowCount, CRFBean crf, String expandedIcon, String icon) {
        String href1 = "javascript:leftnavExpand('Menu_on_" + studySubjectLabel + "_" + crf.getId() + "_" + rowCount + "'); ";
        String href2 = "javascript:leftnavExpand('Menu_off_" + studySubjectLabel + "_" + crf.getId() + "_" + rowCount + "'); ";
        String onmouseover = "moveObject('Event_" + studySubjectLabel + "_" + crf.getId() + "_" + rowCount + "', event); ";
        onmouseover += "setImage('CRFicon" + studySubjectLabel + "_" + crf.getId() + "_" + rowCount + "','" + expandedIcon + "');";
        String onmouseout = "layersShowOrHide('hidden','Event_" + studySubjectLabel + "_" + crf.getId() + "_" + rowCount + "'); ";
        onmouseout += "setImage('CRFicon_" + studySubjectLabel + "_" + crf.getId() + "_" + rowCount + "','" + icon + "');";
        String onClick1 = "layersShowOrHide('visible','Lock_all'); ";
        String onClick2 = "LockObject('Lock_" + studySubjectLabel + "_" + crf.getId() + "_" + rowCount + "',event); ";
        builder.a().href(href1 + href2);
        builder.onmouseover(onmouseover);
        builder.onmouseout(onmouseout);
        builder.onclick(onClick1 + onClick2);
        builder.close();

    }

    private boolean hiddenCrf() {
        if (currentStudy.getParentStudyId() > 0 && eventDefinitionCrf.isHideCrf()) {
            return true;
        }

        return false;
    }
}
