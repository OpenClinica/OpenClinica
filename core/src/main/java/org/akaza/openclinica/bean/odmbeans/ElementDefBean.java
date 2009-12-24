/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2005 Akaza
 * Research
 *
 */

package org.akaza.openclinica.bean.odmbeans;

/**
 * 
 * @author ywang (May, 2008)
 * 
 */

public class ElementDefBean extends ElementOIDBean {
    private String name;
    private String repeating;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setRepeating(String yesNo) {
        this.repeating = yesNo;
    }

    public String getRepeating() {
        return this.repeating;
    }
}