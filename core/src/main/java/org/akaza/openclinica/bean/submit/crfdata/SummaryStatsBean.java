/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.bean.submit.crfdata;

public class SummaryStatsBean {

    private int eventCrfCount;
    private int skippedCrfCount;
    private int studySubjectCount;
    private int discNoteCount;

    public int getEventCrfCount() {
        return eventCrfCount;
    }

    public int getSkippedCrfCount() {
        return skippedCrfCount;
    }

    public void setSkippedCrfCount(int skippedCrfCount) {
        this.skippedCrfCount = skippedCrfCount;
    }

    public void setEventCrfCount(int eventCrfCount) {
        this.eventCrfCount = eventCrfCount;
    }

    public int getStudySubjectCount() {
        return studySubjectCount;
    }

    public void setStudySubjectCount(int studySubjectCount) {
        this.studySubjectCount = studySubjectCount;
    }

    public int getDiscNoteCount() {
        return discNoteCount;
    }

    public void setDiscNoteCount(int discNoteCount) {
        this.discNoteCount = discNoteCount;
    }

}
