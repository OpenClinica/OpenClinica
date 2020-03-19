/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2008 Akaza
 * Research
 *
 */

package core.org.akaza.openclinica.bean.submit.crfdata;

import java.util.ArrayList;

import core.org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.domain.enumsupport.StudyEventWorkflowStatusEnum;

/**
 * OpenClinica event attributes have been included in addition to ODM
 * StudyEventData attributes
 * 
 * @author ywang (Nov, 2008)
 */

public class ExportStudyEventDataBean extends StudyEventDataBean {
    private String location;
    private String startDate;
    private String endDate;
    private String status;
    private Integer ageAtEvent;
    private String eventName;
    private StudyEvent studyEvent;
    private ArrayList<ExportFormDataBean> exportFormData;
    private StudyEventWorkflowStatusEnum workflowStatus;
    private Boolean removed;
    private Boolean archived;
    private Boolean locked;

    public ExportStudyEventDataBean() {
        super();
        exportFormData = new ArrayList<ExportFormDataBean>();
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocation() {
        return this.location;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getStartDate() {
        return this.startDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getEndDate() {
        return this.endDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }

    public ArrayList<ExportFormDataBean> getExportFormData() {
        return exportFormData;
    }

    public void setExportFormData(ArrayList<ExportFormDataBean> formData) {
        this.exportFormData = formData;
    }

    public void setAgeAtEvent(Integer ageAtEvent) {
        this.ageAtEvent = ageAtEvent;
    }

    public Integer getAgeAtEvent() {
        return this.ageAtEvent;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public StudyEvent getStudyEvent() {
        return studyEvent;
    }

    public void setStudyEvent(StudyEvent studyEvent) {
        this.studyEvent = studyEvent;
    }

    public StudyEventWorkflowStatusEnum getWorkflowStatus() {
        return workflowStatus;
    }

    public void setWorkflowStatus(StudyEventWorkflowStatusEnum workflowStatus) {
        this.workflowStatus = workflowStatus;
    }

    public Boolean getRemoved() {
        return removed;
    }

    public void setRemoved(Boolean removed) {
        this.removed = removed;
    }

    public Boolean getArchived() {
        return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }
}
