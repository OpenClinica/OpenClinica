/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2008 Akaza
 * Research
 *
 */

package org.akaza.openclinica.bean.submit.crfdata;


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
}
