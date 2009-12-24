/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.bean.service;

import org.akaza.openclinica.bean.core.AuditableEntityBean;

public class StudyParameterValueBean extends AuditableEntityBean {
    private int studyId;
    private String parameter;
    private String value;

    public StudyParameterValueBean() {
        studyId = 0;
        parameter = "";
        value = "";
    }

    /**
     * @return Returns the studyId.
     */
    public int getStudyId() {
        return studyId;
    }

    /**
     * @param studyId
     *            The studyId to set.
     */
    public void setStudyId(int studyId) {
        this.studyId = studyId;
    }

    /**
     * @return Returns the parameter.
     */
    public String getParameter() {
        return parameter;
    }

    /**
     * @param parameter
     *            The parameter to set.
     */
    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    /**
     * @return Returns the value.
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value
     *            The value to set.
     */
    public void setValue(String value) {
        this.value = value;
    }

}
