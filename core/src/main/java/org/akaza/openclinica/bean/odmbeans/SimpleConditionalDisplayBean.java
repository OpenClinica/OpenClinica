/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 
 *
 */

package org.akaza.openclinica.bean.odmbeans;



public class SimpleConditionalDisplayBean {
    private String controlItemName;
    private String optionValue;
    private String message;
    public String getControlItemName() {
        return controlItemName;
    }
    public void setControlItemName(String controlItemName) {
        this.controlItemName = controlItemName;
    }
    public String getOptionValue() {
        return optionValue;
    }
    public void setOptionValue(String optionValue) {
        this.optionValue = optionValue;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}