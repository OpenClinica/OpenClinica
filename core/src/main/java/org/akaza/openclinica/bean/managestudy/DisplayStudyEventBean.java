/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.bean.managestudy;

import org.akaza.openclinica.bean.core.AuditableEntityBean;
import org.akaza.openclinica.bean.submit.DisplayEventCRFBean;

import java.util.ArrayList;

/**
 * @author jxu
 */
public class DisplayStudyEventBean extends AuditableEntityBean {

    private StudyEventBean studyEvent;
    private ArrayList<DisplayEventCRFBean> displayEventCRFs;
    private ArrayList<DisplayEventDefinitionCRFBean> uncompletedCRFs;
    private ArrayList<DisplayEventCRFBean> allEventCRFs; //includes uncompleted and completed, order by ordinal
    private StudySubjectBean studySubject; // added on 07/26/06
    private int maximumSampleOrdinal; //added on 04/09/08

    /**
     *
     */
    public DisplayStudyEventBean() {
        displayEventCRFs = new ArrayList<DisplayEventCRFBean>();
        uncompletedCRFs = new ArrayList<DisplayEventDefinitionCRFBean>();
        allEventCRFs = new ArrayList<DisplayEventCRFBean>();

    }

    /**
     * @return Returns the allEventCRFs.
     */
    public ArrayList<DisplayEventCRFBean> getAllEventCRFs() {
        return allEventCRFs;
    }

    /**
     * @param allEventCRFs The allEventCRFs to set.
     */
    public void setAllEventCRFs(ArrayList<DisplayEventCRFBean> allEventCRFs) {
        this.allEventCRFs = allEventCRFs;
    }

    /**
     * @return Returns the displayEventCRFs.
     */
    public ArrayList<DisplayEventCRFBean> getDisplayEventCRFs() {
        return displayEventCRFs;
    }

    /**
     * @param displayEventCRFs The displayEventCRFs to set.
     */
    public void setDisplayEventCRFs(ArrayList<DisplayEventCRFBean> displayEventCRFs) {
        this.displayEventCRFs = displayEventCRFs;
    }

    /**
     * @return Returns the studyEvent.
     */
    public StudyEventBean getStudyEvent() {
        return studyEvent;
    }

    /**
     * @param studyEvent The studyEvent to set.
     */
    public void setStudyEvent(StudyEventBean studyEvent) {
        this.studyEvent = studyEvent;
    }

    /**
     * @return Returns the uncompletedCRFs.
     */
    public ArrayList<DisplayEventDefinitionCRFBean> getUncompletedCRFs() {
        return uncompletedCRFs;
    }

    /**
     * @param uncompletedCRFs The uncompletedCRFs to set.
     */
    public void setUncompletedCRFs(ArrayList<DisplayEventDefinitionCRFBean> uncompletedCRFs) {
        this.uncompletedCRFs = uncompletedCRFs;
    }

    /**
     * @return Returns the studySubject.
     */
    public StudySubjectBean getStudySubject() {
        return studySubject;
    }

    /**
     * @param studySubject The studySubject to set.
     */
    public void setStudySubject(StudySubjectBean studySubject) {
        this.studySubject = studySubject;
    }

    public int getMaximumSampleOrdinal() {
        return maximumSampleOrdinal;
    }

    public void setMaximumSampleOrdinal(int maximumSampleOrdinal) {
        this.maximumSampleOrdinal = maximumSampleOrdinal;
    }
}
