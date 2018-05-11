/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 * copyright 2003-2011 Akaza Research
 */
package org.akaza.openclinica.bean.submit;

import java.util.HashMap;

/**
 * Control displaying of a row with simple conditional display item at the front end <br/>
 * SHOW_UNCHANGABLE: 0; row always display; 
 * SHOW_CHANGABLE 1: current display but changable; 
 * HIDE_CHANGABLE 2: current no display but changable; <br/>  
 */
public enum SCDRowDisplayStatus {
    SHOW_UNCHANGABLE(0), SHOW_CHANGABLE(1), HIDE_CHANGABLE(2);
    
    private int code;
    
    SCDRowDisplayStatus() {
        this.code = 0;
    }
    
    SCDRowDisplayStatus(int code) {
        this.code = code;
    }
    
    @Override
    public String toString() {
        return name().toString();
    }
    
    public static SCDRowDisplayStatus getByCode(Integer code) {
        HashMap<Integer, SCDRowDisplayStatus> enumObjects = new HashMap<Integer, SCDRowDisplayStatus>();
        for (SCDRowDisplayStatus theEnum : SCDRowDisplayStatus.values()) {
            enumObjects.put(theEnum.getCode(), theEnum);
        }
        return enumObjects.get(Integer.valueOf(code));
    }
    
    public int getCode() {
        return code;
    }
}
