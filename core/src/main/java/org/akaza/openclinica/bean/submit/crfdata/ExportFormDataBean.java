/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2008 Akaza
 * Research
 *
 */

package org.akaza.openclinica.bean.submit.crfdata;

import org.akaza.openclinica.domain.datamap.EventCrf;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrf;
import org.akaza.openclinica.domain.datamap.FormLayout;

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
    private String status;
    private String formLayoutName;
    private String formName;
    private FormLayout formLayout;
    private EventCrf eventCrf;
    private EventDefinitionCrf eventDefinitionCrf;

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

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
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

}
