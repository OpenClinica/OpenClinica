/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.view;

/**
 * @author jxu
 *
 */
public class StudyInfoPanelLineSubmit extends StudyInfoPanelLine {
    private int eventId;
    private int eventCRFId;

    public StudyInfoPanelLineSubmit(String title, String info, boolean colon, boolean lastCRF, int eventId, int eventCRFId) {
        super(title, info, colon, lastCRF);
        this.eventId = eventId;
        this.eventCRFId = eventCRFId;

    }

    /**
     * @return Returns the eventCRFId.
     */
    public int getEventCRFId() {
        return eventCRFId;
    }

    /**
     * @param eventCRFId
     *            The eventCRFId to set.
     */
    public void setEventCRFId(int eventCRFId) {
        this.eventCRFId = eventCRFId;
    }

    /**
     * @return Returns the eventId.
     */
    public int getEventId() {
        return eventId;
    }

    /**
     * @param eventId
     *            The eventId to set.
     */
    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

}
