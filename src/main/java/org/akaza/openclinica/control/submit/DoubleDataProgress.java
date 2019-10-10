package org.akaza.openclinica.control.submit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class tracks the progress of a user when viewing CRF sections during
 * double data entry. When the user views the section for the first time, any
 * defauts field values are shown on the form, and the visit is recorded and
 * stored in a session attribute. Default values are not displayed on the form
 * thereafter, to prevent default values from being displayed, for instance,
 * when the form is redisplayed with error messages.
 *
 * @see SectionVisit
 * @author Bruce W. Perry Date: Nov 30, 2007
 */
public class DoubleDataProgress implements EnterDataProgress {
    // the synchronizedList is accessed from a session attribute
    private List<SectionVisit> synchronizedList;
    // The eventCRFId represents the uniqueness of this set of visits to a form
    private int eventCRFId;

    public DoubleDataProgress() {
        synchronizedList = Collections.synchronizedList(new ArrayList<SectionVisit>());
        eventCRFId = 0;
    }

    public DoubleDataProgress(int numberOfSections, int eventCRFId) {
        this();
        this.eventCRFId = eventCRFId;
        // numberOfSections is one by default, as every legitimate form
        // has at least one section
        numberOfSections = numberOfSections < 1 ? 1 : numberOfSections;
        List<SectionVisit> sectionVisits = new ArrayList<SectionVisit>();
        SectionVisit sectionVisit;
        for (int i = 1; i <= numberOfSections; i++) {
            sectionVisit = new SectionVisit();
            sectionVisit.setSectionNumber(i);
            sectionVisit.setEventCRFId(eventCRFId);
            sectionVisits.add(sectionVisit);
        }
        synchronizedList = Collections.synchronizedList(sectionVisits);
    }

    public boolean getSectionVisited(int sectionNumber, int eventCRFId) {
        for (SectionVisit secVisit : synchronizedList) {
            if (secVisit.getEventCRFId() == eventCRFId && secVisit.getSectionNumber() == sectionNumber)
                return secVisit.isVisitedOnce();
        }
        return false;
    }

    public void setSectionVisited(int eventCRFId, int sectionNumber, boolean hasVisited) {
        for (SectionVisit secVisit : synchronizedList) {
            if (secVisit.getEventCRFId() == eventCRFId && secVisit.getSectionNumber() == sectionNumber) {
                secVisit.setVisitedOnce(hasVisited);
            }
        }
    }

    public int getEventCRFId() {
        return eventCRFId;
    }

    public void setEventCRFId(int eventCRFId) {
        this.eventCRFId = eventCRFId;
    }
}
