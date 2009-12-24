package org.akaza.openclinica.service;

import java.util.LinkedList;

import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;

/**
 * This class represents a Discrepancy Note thread or sequence of notes that are
 * conceptually linked together on an Event CRF.
 */
public class DiscrepancyNoteThread {
    private LinkedList<DiscrepancyNoteBean> linkedNoteList;
    private String latestResolutionStatus;
    private int studyId;

    public DiscrepancyNoteThread() {
        linkedNoteList = new LinkedList<DiscrepancyNoteBean>();
        studyId=0;
        latestResolutionStatus="";
    }

    public DiscrepancyNoteThread(LinkedList<DiscrepancyNoteBean> linkedNoteList, int studyId) {
        this.linkedNoteList = linkedNoteList;
        this.studyId = studyId;
    }

    public String getLatestResolutionStatus() {
        return latestResolutionStatus;
    }

    public void setLatestResolutionStatus(String latestResolutionStatus) {
        this.latestResolutionStatus = latestResolutionStatus;
    }

    public LinkedList<DiscrepancyNoteBean> getLinkedNoteList() {
        return linkedNoteList;
    }

    public void setLinkedNoteList(LinkedList<DiscrepancyNoteBean> linkedNoteList) {
        this.linkedNoteList = linkedNoteList;
    }

    public int getStudyId() {
        return studyId;
    }

    public void setStudyId(int studyId) {
        this.studyId = studyId;
    }
}
