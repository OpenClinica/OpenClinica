/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.bean.submit;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;

import java.util.ArrayList;

/**
 * @author ssachs
 */
public class DisplayTableOfContentsBean {
    private boolean active; // AND's together all the active bits of all the
    // beans that were explicitly set, except for
    // sections
    private String actionServlet; // the servlet to execute the action which
    // the user can perform on the items in the
    // event crf
    // set in setEventCRF

    /**
     * The String we should use to call TableOfContentsServlet in order to
     * display an overview of this.eventCRF. ie "TableOfContents?ecid=" +
     * this.eventCRF.id + "&action=" + this.action is a GET query which will
     * display the overview for this event CRF.
     */
    private String action;

    // metadata
    private StudyEventDefinitionBean studyEventDefinition;
    private EventDefinitionCRFBean eventDefinitionCRF;
    private CRFBean crf;
    private CRFVersionBean crfVersion;

    // data
    private StudySubjectBean studySubject;
    private StudyEventBean studyEvent;
    private EventCRFBean eventCRF;
    private ArrayList sections; // this really should be metadata, but the

    // sections are populated with the number of
    // items completed, so there's some data in them

    // each element is a SectionBean

    public DisplayTableOfContentsBean() {
        active = false;
        actionServlet = "";

        studyEventDefinition = new StudyEventDefinitionBean();
        eventDefinitionCRF = new EventDefinitionCRFBean();
        crf = new CRFBean();
        crfVersion = new CRFVersionBean();

        // data
        studySubject = new StudySubjectBean();
        studyEvent = new StudyEventBean();
        eventCRF = new EventCRFBean();
    }

    /**
     * @return Returns the crf.
     */
    public CRFBean getCrf() {
        return crf;
    }

    /**
     * @param crf
     *            The crf to set.
     */
    public void setCrf(CRFBean crf) {
        this.crf = crf;
        active = active && crf.isActive();
    }

    /**
     * @return Returns the crfVersion.
     */
    public CRFVersionBean getCrfVersion() {
        return crfVersion;
    }

    /**
     * @param crfVersion
     *            The crfVersion to set.
     */
    public void setCrfVersion(CRFVersionBean crfVersion) {
        this.crfVersion = crfVersion;
        active = active && crfVersion.isActive();
    }

    /**
     * @return Returns the eventCRF.
     */
    public EventCRFBean getEventCRF() {
        return eventCRF;
    }

    /**
     * @param eventCRF
     *            The eventCRF to set.
     */
    public void setEventCRF(EventCRFBean eventCRF) {
        this.eventCRF = eventCRF;
        active = active && eventCRF.isActive();

        if (eventCRF.getStage().equals(DataEntryStage.UNCOMPLETED)) {
            actionServlet = "InitialDataEntry";
        } else if (eventCRF.getStage().equals(DataEntryStage.INITIAL_DATA_ENTRY)) {
            actionServlet = "InitialDataEntry";
        } else if (eventCRF.getStage().equals(DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE)) {
            actionServlet = "DoubleDataEntry";
        } else if (eventCRF.getStage().equals(DataEntryStage.DOUBLE_DATA_ENTRY)) {
            actionServlet = "DoubleDataEntry";
        } else if (eventCRF.getStage().equals(DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE)) {
            actionServlet = "AdministrativeEditing";
        }
    }

    /**
     * @return Returns the eventDefinitionCRF.
     */
    public EventDefinitionCRFBean getEventDefinitionCRF() {
        return eventDefinitionCRF;
    }

    /**
     * @param eventDefinitionCRF
     *            The eventDefinitionCRF to set.
     */
    public void setEventDefinitionCRF(EventDefinitionCRFBean eventDefinitionCRF) {
        this.eventDefinitionCRF = eventDefinitionCRF;
        active = active && eventDefinitionCRF.isActive();
    }

    /**
     * @return Returns the sections.
     */
    public ArrayList getSections() {
        return sections;
    }

    /**
     * @param sections
     *            The sections to set.
     */
    public void setSections(ArrayList sections) {
        this.sections = sections;
    }

    /**
     * @return Returns the studyEvent.
     */
    public StudyEventBean getStudyEvent() {
        return studyEvent;
    }

    /**
     * @param studyEvent
     *            The studyEvent to set.
     */
    public void setStudyEvent(StudyEventBean studyEvent) {
        this.studyEvent = studyEvent;
        active = active && studyEvent.isActive();
    }

    /**
     * @return Returns the studyEventDefinition.
     */
    public StudyEventDefinitionBean getStudyEventDefinition() {
        return studyEventDefinition;
    }

    /**
     * @param studyEventDefinition
     *            The studyEventDefinition to set.
     */
    public void setStudyEventDefinition(StudyEventDefinitionBean studyEventDefinition) {
        this.studyEventDefinition = studyEventDefinition;
        active = active && studyEventDefinition.isActive();
    }

    /**
     * @return Returns the studySubject.
     */
    public StudySubjectBean getStudySubject() {
        return studySubject;
    }

    /**
     * @param studySubject
     *            The studySubject to set.
     */
    public void setStudySubject(StudySubjectBean studySubject) {
        this.studySubject = studySubject;
        active = active && studySubject.isActive();
    }

    /**
     * @return Returns the active.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @return Returns the actionServlet.
     */
    public String getActionServlet() {
        return actionServlet;
    }

    /**
     * @return Returns the action.
     */
    public String getAction() {
        return action;
    }

    /**
     * @param action
     *            The action to set.
     */
    public void setAction(String action) {
        this.action = action;
    }
}