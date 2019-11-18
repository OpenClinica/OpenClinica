package org.akaza.openclinica.control.submit;

/**
 * Created by IntelliJ IDEA. User: bruceperry Date: Nov 30, 2007 This class is
 * designed to determine whether a user has viewed a certain section, to
 * determine, among other reasons, whether default values should be displayed.
 */
public class SectionVisit {
    private int eventCRFId;
    private boolean visitedOnce;
    private int sectionNumber;

    public SectionVisit() {
        visitedOnce = false;
        sectionNumber = 1;
    }

    public int getEventCRFId() {
        return eventCRFId;
    }

    public void setEventCRFId(int eventCRFId) {
        this.eventCRFId = eventCRFId;
    }

    public boolean isVisitedOnce() {
        return visitedOnce;
    }

    public void setVisitedOnce(boolean visitedOnce) {
        this.visitedOnce = visitedOnce;
    }

    public int getSectionNumber() {
        return sectionNumber;
    }

    public void setSectionNumber(int sectionNumber) {
        this.sectionNumber = sectionNumber;
    }
}
