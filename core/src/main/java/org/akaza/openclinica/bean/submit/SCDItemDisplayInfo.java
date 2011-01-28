/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 * copyright 2003-2011 Akaza Research
 */
package org.akaza.openclinica.bean.submit;

import java.util.ArrayList;
import java.util.List;

/**
 * For displaying simple conditional display items.
 *
 */
public class SCDItemDisplayInfo {
    /**
     * No status class for this so far, but follow the rule below:
     * 
     * 0: row always display; 
     * 1: current display but changable; 
     * 2: current no display but changable; 
     */
    private int rowDisplayStatus = 0;
    /**
     * String pattern: -ItemID-, e.g. -11-12-
     */
    private String rowSCDShowIDStr = ""; 
    
    private int rowNum = 1;
    private int rowFirstColIndex = 0;
    /**
     * No status class for this so far, but follow the rule below:
     * 
     * 0: always display; 
     * 1: display but changable; 
     * 2: no display but changable; 
     */
    private int scdShowStatus = 0;
    
    
    public static DisplaySectionBean generateSCDDisplayInfo(DisplaySectionBean section, Boolean noValueComparison) {
        List<DisplayItemWithGroupBean> allItems = section.getDisplayItemGroups();
        int rowStartIndex = 0, rowStatus = -1, rowIndex = 1;
        String ids = "";

        DisplayItemBean dib = allItems.get(0).getSingleItem();
        if(dib.getMetadata().getParentId()==0) {
            if(dib.getMetadata().isConditionalDisplayItem()) {
                int scdShowStatus0 = -1;
                if(noValueComparison) {
                    scdShowStatus0 = dib.getNumDiscrepancyNotes() > 0 ? 0 : 
                        dib.getData().getValue().length()>0 || dib.getIsSCDtoBeShown() ? 1 : 2;
                } else {
                    scdShowStatus0 = dib.getNumDiscrepancyNotes() > 0 || dib.getDbValue().length() > 0 ? 0 : 
                        dib.getData().getValue().length()>0 || dib.getIsSCDtoBeShown() ? 1 : 2;
                }
                SCDItemDisplayInfo dinfo0 = dib.getScdDisplayInfo();
                dinfo0.setScdShowStatus(scdShowStatus0);
                dinfo0.setRowNum(rowIndex);
                ids += scdShowStatus0 <= 1 ? dinfo0.getRowSCDShowIDStr()+"-"+dib.getMetadata().getItemId() : dinfo0.getRowSCDShowIDStr();
                rowStatus = rowStatus!=-1 && rowStatus <= scdShowStatus0 ? rowStatus : scdShowStatus0;
            } else {
                rowStatus = 0;
            }
            ArrayList<DisplayItemBean> childItems0 = dib.getChildren();
            for (int j = 0; j < childItems0.size(); ++j) {
                DisplayItemBean child = childItems0.get(j);
                if(child.getMetadata().isConditionalDisplayItem()) {
                    int scdShowStatus = child.getNumDiscrepancyNotes() > 0 ? 0 : 
                        child.getData().getValue().length()>0 || child.getIsSCDtoBeShown() ? 1 : 2;
                     child.getScdDisplayInfo().setScdShowStatus(scdShowStatus);
                }
            }
        }
        
        for (int i = 1; i < allItems.size(); ++i) {
            dib = allItems.get(i).getSingleItem();
            ItemFormMetadataBean ifmb = dib.getMetadata();
            if(ifmb.getParentId()==0) {
                int col = ifmb.getColumnNumber();
                if(col <= 1) {
                    DisplayItemBean prevDib = allItems.get(rowStartIndex).getSingleItem();
                    SCDItemDisplayInfo dinfo = prevDib.getScdDisplayInfo();
                    dinfo.setRowDisplayStatus(rowStatus);
                    dinfo.setRowSCDShowIDStr(ids);
                    rowStartIndex = i;
                    rowStatus = -1;
                    ++rowIndex;
                }
                if(ifmb.isConditionalDisplayItem()) {
                    int scdShowStatus = -1;
                    if(noValueComparison) {
                        scdShowStatus = dib.getNumDiscrepancyNotes() > 0 ? 0 : 
                            dib.getData().getValue().length()>0 || dib.getIsSCDtoBeShown() ? 1 : 2;
                    } else {
                        scdShowStatus = dib.getNumDiscrepancyNotes() > 0 || dib.getDbValue().length() > 0 ? 0 : 
                            dib.getData().getValue().length()>0 || dib.getIsSCDtoBeShown() ? 1 : 2;
                    }
                    SCDItemDisplayInfo dinfo = dib.getScdDisplayInfo();
                    dinfo.setScdShowStatus(scdShowStatus);
                    dinfo.setRowNum(rowIndex);
                    dinfo.setRowFirstColIndex(rowStartIndex);
                    ids += scdShowStatus <= 1 ? dinfo.getRowSCDShowIDStr()+"-"+ifmb.getItemId() : dinfo.getRowSCDShowIDStr();
                    rowStatus = rowStatus!=-1 && rowStatus <= scdShowStatus ? rowStatus : scdShowStatus;
                } else {
                    rowStatus = 0;
                }
    
                ArrayList childItems = dib.getChildren();
                for (int j = 0; j < childItems.size(); ++j) {
                    DisplayItemBean child = (DisplayItemBean)childItems.get(j);
                    if(child.getMetadata().isConditionalDisplayItem()) {
                        int scdShowStatus = child.getNumDiscrepancyNotes() > 0 ? 0 : 
                            child.getData().getValue().length()>0 || child.getIsSCDtoBeShown() ? 1 : 2;
                         child.getScdDisplayInfo().setScdShowStatus(scdShowStatus);
                    }
                }
            }
        }
        if(rowStartIndex >= 0) {
            DisplayItemBean prevDib = allItems.get(rowStartIndex).getSingleItem();
            SCDItemDisplayInfo dinfo = prevDib.getScdDisplayInfo();
            dinfo.setRowDisplayStatus(rowStatus);
            dinfo.setRowSCDShowIDStr(ids+"-");
            dinfo.setRowFirstColIndex(rowStartIndex);
        }
        return section;
    }

    
    
    public int getRowDisplayStatus() {
        return rowDisplayStatus;
    }
    public void setRowDisplayStatus(int rowDisplayStatus) {
        this.rowDisplayStatus = rowDisplayStatus;
    }
    public int getScdShowStatus() {
        return scdShowStatus;
    }
    public void setScdShowStatus(int scdShowStatus) {
        this.scdShowStatus = scdShowStatus;
    }
    public String getRowSCDShowIDStr() {
        return rowSCDShowIDStr;
    }
    public void setRowSCDShowIDStr(String rowSCDShowIDStr) {
        this.rowSCDShowIDStr = rowSCDShowIDStr;
    }
    public int getRowNum() {
        return rowNum;
    }
    public void setRowNum(int rowNum) {
        this.rowNum = rowNum;
    }
    public int getRowFirstColIndex() {
        return rowFirstColIndex;
    }
    public void setRowFirstColIndex(int rowFirstColIndex) {
        this.rowFirstColIndex = rowFirstColIndex;
    }
    
}