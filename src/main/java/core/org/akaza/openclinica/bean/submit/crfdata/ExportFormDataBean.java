/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2008 Akaza
 * Research
 *
 */

package core.org.akaza.openclinica.bean.submit.crfdata;

import core.org.akaza.openclinica.domain.datamap.EventCrf;
import core.org.akaza.openclinica.domain.datamap.EventDefinitionCrf;
import core.org.akaza.openclinica.domain.datamap.FormLayout;
import org.akaza.openclinica.domain.enumsupport.EventCrfWorkflowStatusEnum;

/**
 * OpenClinica form attributes have been included in addition to ODM FormData
 * attributes
 * 
 * @author ywang (Nov, 2008)
 */

public class ExportFormDataBean extends FormDataBean {
    private String crfVersion;
    private String interviewerName;
    private String interviewDate;
    private EventCrfWorkflowStatusEnum workflowStatus;
    private String formLayoutName;
    private String formName;
    private FormLayout formLayout;
    private EventCrf eventCrf;
    private EventDefinitionCrf eventDefinitionCrf;
    private Boolean removed;
    private Boolean archived;
    private Boolean locked;

    public ExportFormDataBean() {
        super();
    }

    public void setCrfVersion(String crfVersion) {
        this.crfVersion = crfVersion;
    }

    public String getCrfVersion() {
        return this.crfVersion;
    }

    public void setInterviewerName(String interviewerName) {
        this.interviewerName = interviewerName;
    }

    public String getInterviewerName() {
        return this.interviewerName;
    }

    public void setInterviewDate(String interviewDate) {
        this.interviewDate = interviewDate;
    }

    public String getInterviewDate() {
        return this.interviewDate;
    }

    public String getFormLayoutName() {
        return formLayoutName;
    }

    public void setFormLayoutName(String formLayoutName) {
        this.formLayoutName = formLayoutName;
    }

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public FormLayout getFormLayout() {
        return formLayout;
    }

    public void setFormLayout(FormLayout formLayout) {
        this.formLayout = formLayout;
    }

    public EventCrf getEventCrf() {
        return eventCrf;
    }

    public void setEventCrf(EventCrf eventCrf) {
        this.eventCrf = eventCrf;
    }

    public EventDefinitionCrf getEventDefinitionCrf() {
        return eventDefinitionCrf;
    }

    public void setEventDefinitionCrf(EventDefinitionCrf eventDefinitionCrf) {
        this.eventDefinitionCrf = eventDefinitionCrf;
    }

    public EventCrfWorkflowStatusEnum getWorkflowStatus() {
        return workflowStatus;
    }

    public void setWorkflowStatus(EventCrfWorkflowStatusEnum workflowStatus) {
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
