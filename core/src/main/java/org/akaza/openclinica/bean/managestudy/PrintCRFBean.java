package org.akaza.openclinica.bean.managestudy;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.DisplaySectionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;

import java.util.List;

/**
 * @author: Shamim
 * Date: Nov 23, 2009
 * Time: 8:41:41 AM
 */
public class PrintCRFBean {
    private StudyEventBean studyEventBean;
    private CRFBean crfBean;
    private CRFVersionBean crfVersionBean;
    private EventCRFBean eventCrfBean;
    private DisplaySectionBean displaySectionBean;
    private List displaySectionBeans;
    private List allSections;
    private boolean grouped;

    public boolean isGrouped() {
        return grouped;
    }

    public void setGrouped(boolean grouped) {
        this.grouped = grouped;
    }

    public StudyEventBean getStudyEventBean() {
        return studyEventBean;
    }

    public void setStudyEventBean(StudyEventBean studyEventBean) {
        this.studyEventBean = studyEventBean;
    }

    public CRFBean getCrfBean() {
        return crfBean;
    }

    public void setCrfBean(CRFBean crfBean) {
        this.crfBean = crfBean;
    }

    public CRFVersionBean getCrfVersionBean() {
        return crfVersionBean;
    }

    public void setCrfVersionBean(CRFVersionBean crfVersionBean) {
        this.crfVersionBean = crfVersionBean;
    }

    public EventCRFBean getEventCrfBean() {
        return eventCrfBean;
    }

    public void setEventCrfBean(EventCRFBean eventCrfBean) {
        this.eventCrfBean = eventCrfBean;
    }

    public DisplaySectionBean getDisplaySectionBean() {
        return displaySectionBean;
    }

    public void setDisplaySectionBean(DisplaySectionBean displaySectionBean) {
        this.displaySectionBean = displaySectionBean;
    }

    public List getDisplaySectionBeans() {
        return displaySectionBeans;
    }

    public void setDisplaySectionBeans(List displaySectionBeans) {
        this.displaySectionBeans = displaySectionBeans;
    }

    public List getAllSections() {
        return allSections;
    }

    public void setAllSections(List allSections) {
        this.allSections = allSections;
    }
}
