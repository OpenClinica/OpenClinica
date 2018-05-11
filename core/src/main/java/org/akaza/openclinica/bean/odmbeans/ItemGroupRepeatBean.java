/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 
 *
 */

package org.akaza.openclinica.bean.odmbeans;



/**
 *
 * @author ywang (Augest, 2010)
 *
 */

public class ItemGroupRepeatBean {
    //attributes
    private Integer repeatNumber;
    private Integer repeatMax;
    
    public Integer getRepeatNumber() {
        return repeatNumber;
    }
    public void setRepeatNumber(Integer repeatNumber) {
        this.repeatNumber = repeatNumber;
    }
    public Integer getRepeatMax() {
        return repeatMax;
    }
    public void setRepeatMax(Integer repeatMax) {
        this.repeatMax = repeatMax;
    }
}