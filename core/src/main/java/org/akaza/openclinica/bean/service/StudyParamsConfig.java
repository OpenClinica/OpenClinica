/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.bean.service;

import java.io.Serializable;

public class StudyParamsConfig implements Serializable{
    private StudyParameterValueBean value;
    private StudyParameter parameter;

    public StudyParamsConfig() {
        value = new StudyParameterValueBean();
        parameter = new StudyParameter();
    }

    /**
     * @return Returns the parameter.
     */
    public StudyParameter getParameter() {
        return parameter;
    }

    /**
     * @param parameter
     *            The parameter to set.
     */
    public void setParameter(StudyParameter parameter) {
        this.parameter = parameter;
    }

    /**
     * @return Returns the value.
     */
    public StudyParameterValueBean getValue() {
        return value;
    }

    /**
     * @param value
     *            The value to set.
     */
    public void setValue(StudyParameterValueBean value) {
        this.value = value;
    }

}
