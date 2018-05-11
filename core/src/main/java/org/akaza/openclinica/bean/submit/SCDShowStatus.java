/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 * copyright 2003-2011 Akaza Research
 */
package org.akaza.openclinica.bean.submit;

import java.util.HashMap;

/**
 * Control displaying of a simple conditional display item at the front end <br/>
 * SHOW_UNCHANGABLE: 0: always display; 
 * SHOW_CHANGABLE: 1: display but changable; 
 * HIDE_CHANGABLE: 2: no display but changable;   
 */
public enum SCDShowStatus {
    SHOW_UNCHANGABLE(0), SHOW_CHANGABLE(1), HIDE_CHANGABLE(2);
    
    private int code;
    
    SCDShowStatus() {
        this.code = 0;
    }
    
    SCDShowStatus(int code) {
        this.code = code;
    }
    
    @Override
    public String toString() {
        return name().toString();
    }
    
    public static SCDShowStatus getByCode(Integer code) {
        HashMap<Integer, SCDShowStatus> enumObjects = new HashMap<Integer, SCDShowStatus>();
        for (SCDShowStatus theEnum : SCDShowStatus.values()) {
            enumObjects.put(theEnum.getCode(), theEnum);
        }
        return enumObjects.get(Integer.valueOf(code));
    }
    
    public int getCode() {
        return code;
    }
}
