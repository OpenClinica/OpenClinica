/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.view;

import java.io.Serializable;

/**
 * @author ssachs
 */
public class StudyInfoPanelLine implements Serializable{
    protected String title;
    protected String info;
    protected boolean colon;
    protected boolean lastCRF;// for submit module only
    protected boolean current; // indicate whether it is the current object,

    // need to be highlighted

    /**
     * @param title
     * @param info
     */
    public StudyInfoPanelLine(String title, String info) {
        this.title = title;
        this.info = info;
    }

    public StudyInfoPanelLine(String title, String info, boolean colon) {
        this.title = title;
        this.info = info;
        this.colon = colon;
    }

    public StudyInfoPanelLine(String title, String info, boolean colon, boolean lastCRF) {
        this.title = title;
        this.info = info;
        this.colon = colon;
        this.lastCRF = lastCRF;
    }

    public StudyInfoPanelLine(String title, String info, boolean colon, boolean lastCRF, boolean current) {
        this.title = title;
        this.info = info;
        this.colon = colon;
        this.lastCRF = lastCRF;
        this.current = current;
    }

    /**
     * @return Returns the info.
     */
    public String getInfo() {
        return info;
    }

    /**
     * @param info
     *            The info to set.
     */
    public void setInfo(String info) {
        this.info = info;
    }

    /**
     * @return Returns the title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     *            The title to set.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return Returns the colon.
     */
    public boolean isColon() {
        return colon;
    }

    /**
     * @param colon
     *            The colon to set.
     */
    public void setColon(boolean colon) {
        this.colon = colon;
    }

    /**
     * @return Returns the lastCRF.
     */
    public boolean isLastCRF() {
        return lastCRF;
    }

    /**
     * @param lastCRF
     *            The lastCRF to set.
     */
    public void setLastCRF(boolean lastCRF) {
        this.lastCRF = lastCRF;
    }

    /**
     * @return Returns the current.
     */
    public boolean isCurrent() {
        return current;
    }

    /**
     * @param current
     *            The current to set.
     */
    public void setCurrent(boolean current) {
        this.current = current;
    }
}
