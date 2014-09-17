/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2011 Akaza Research
 */
package org.akaza.openclinica.service.crfdata;

import org.akaza.openclinica.bean.submit.SCDItemDisplayInfo;
import org.akaza.openclinica.domain.crfdata.SCDItemMetadataBean;

import java.util.ArrayList;


public class SCDData {

    private SCDItemDisplayInfo scdDisplayInfo = new SCDItemDisplayInfo(); 
    /**
     * held by a control displayItemBean
     */
    private ArrayList<SCDItemMetadataBean> scdSetsForControl = new ArrayList<SCDItemMetadataBean>();
    /**
     * held by a scd item
     */
    private SCDItemMetadataBean scdItemMetadataBean = new SCDItemMetadataBean();
    /**
     * Records ItemDataBean value stored in database only, may not always available. 
     */
    private String dbValue = "";
    
    
    
    public SCDData() {
        scdDisplayInfo = new SCDItemDisplayInfo();
        dbValue = "";
        scdItemMetadataBean = new SCDItemMetadataBean();
        scdSetsForControl = new ArrayList<SCDItemMetadataBean>();
    }
    
    public ArrayList<SCDItemMetadataBean> getScdSetsForControl() {
        return scdSetsForControl;
    }
    
    public void setScdSetsForControl(ArrayList<SCDItemMetadataBean> scdSetsForControl) {
        this.scdSetsForControl = scdSetsForControl;
    }
    
    public SCDItemMetadataBean getScdItemMetadataBean() {
        return scdItemMetadataBean;
    }
    
    public void setScdItemMetadataBean(SCDItemMetadataBean scdItemMetadataBean) {
        this.scdItemMetadataBean = scdItemMetadataBean;
    }
    
    public SCDItemDisplayInfo getScdDisplayInfo() {
        return scdDisplayInfo;
    }
    
    public void setScdDisplayInfo(SCDItemDisplayInfo scdDisplayInfo) {
        this.scdDisplayInfo = scdDisplayInfo;
    }
    
    public String getDbValue() {
        return dbValue;
    }
    
    public void setDbValue(String dbValue) {
        this.dbValue = dbValue;
    }
}