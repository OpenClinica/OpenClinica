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
 * @author ywang
 */
public class SCDItemDisplayInfo {
    private int rowDisplayStatus = SCDRowDisplayStatus.SHOW_UNCHANGABLE.getCode();  //0;
    /**
     * String pattern: -ItemID-, e.g. -11-12-
     */
    private String rowSCDShowIDStr = ""; 
    
    private int rowNum = 1;
    private int rowFirstColIndex = 0;
    
    private int scdShowStatus = SCDShowStatus.SHOW_UNCHANGABLE.getCode(); //0;
    //private SCDShowStatus scdShowStatus = SCDShowStatus.SHOW_UNCHANGABLE;
    
    
    public static DisplaySectionBean generateSCDDisplayInfo(DisplaySectionBean section, Boolean noValueComparison) {
        /* rowStatus -
         * SHOW_UNCHANGABLE: 0; row always display; 
         * SHOW_CHANGABLE 1: current display but changable; 
         * HIDE_CHANGABLE 2: current no display but changable;
         */
        /*
         * scdShowStatus - 
         * 0: always display; 
         * 1: display but changable; 
         * 2: no display but changable; 
         */
        List<DisplayItemWithGroupBean> allItems = section.getDisplayItemGroups();
        int rowStartIndex = 0, rowStatus = -1, rowIndex = 1;
        String ids = "";

        if(allItems==null || allItems.size()<1) {
            return section;
        }
        DisplayItemBean dib = allItems.get(0).getSingleItem();
        if(dib.getMetadata().getParentId()==0) {
            if(SCDItemDisplayInfo.isSCDItem(dib)) {
                int scdShowStatus0 = -1;
                if(noValueComparison) {
                    //scdShowStatus0 = dib.getNumDiscrepancyNotes() > 0 ? 0 : 
                        //dib.getData().getValue().length()>0 || dib.getIsSCDtoBeShown() ? 1 : 2;
                    scdShowStatus0 = dib.getNumDiscrepancyNotes() > 0 ? 
                        SCDShowStatus.SHOW_UNCHANGABLE.getCode() : 
                        dib.getData().getValue().length()>0 || dib.getIsSCDtoBeShown() ? 
                            SCDShowStatus.SHOW_CHANGABLE.getCode() : SCDShowStatus.HIDE_CHANGABLE.getCode();
                } else {
                    //scdShowStatus0 = dib.getNumDiscrepancyNotes() > 0 || dib.getScdData().getDbValue().length() > 0 ? 0 : 
                        //dib.getData().getValue().length()>0 || dib.getIsSCDtoBeShown() ? 1 : 2;
                    scdShowStatus0 = dib.getNumDiscrepancyNotes() > 0 || dib.getScdData().getDbValue().length() > 0 ? 
                        SCDShowStatus.SHOW_UNCHANGABLE.getCode() :  
                        dib.getData().getValue().length()>0 || dib.getIsSCDtoBeShown() ? 
                            SCDShowStatus.SHOW_CHANGABLE.getCode() : SCDShowStatus.HIDE_CHANGABLE.getCode();
                }
                SCDItemDisplayInfo dinfo0 = dib.getScdData().getScdDisplayInfo();
                dinfo0.setScdShowStatus(scdShowStatus0);
                //dinfo0.setScdShowStatus(SCDShowStatus.getByCode(scdShowStatus0));
                dinfo0.setRowNum(rowIndex);
                ids += scdShowStatus0 <= 1 ? dinfo0.getRowSCDShowIDStr()+"-"+dib.getMetadata().getItemId() : dinfo0.getRowSCDShowIDStr();
                rowStatus = rowStatus!=-1 && rowStatus <= scdShowStatus0 ? rowStatus : scdShowStatus0;
            } else {
                rowStatus = 0;
            }
            ArrayList<DisplayItemBean> childItems0 = dib.getChildren();
            for (int j = 0; j < childItems0.size(); ++j) {
                DisplayItemBean child = childItems0.get(j);
                if(SCDItemDisplayInfo.isSCDItem(child)) {
                    //int scdShowStatus = child.getNumDiscrepancyNotes() > 0 ? 0 : 
                        //child.getData().getValue().length()>0 || child.getIsSCDtoBeShown() ? 1 : 2;
                    int scdShowStatus = child.getNumDiscrepancyNotes() > 0 ? 
                        SCDShowStatus.SHOW_UNCHANGABLE.getCode() :  
                        child.getData().getValue().length()>0 || child.getIsSCDtoBeShown() ? 
                            SCDShowStatus.SHOW_CHANGABLE.getCode() : SCDShowStatus.HIDE_CHANGABLE.getCode();
                     child.getScdData().getScdDisplayInfo().setScdShowStatus(scdShowStatus);
                     //child.getScdData().getScdDisplayInfo().setScdShowStatus(SCDShowStatus.getByCode(scdShowStatus));
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
                    SCDItemDisplayInfo dinfo = prevDib.getScdData().getScdDisplayInfo();
                    //dinfo.setRowDisplayStatus(SCDRowDisplayStatus.getByCode(rowStatus));
                    dinfo.setRowDisplayStatus(rowStatus);
                    dinfo.setRowSCDShowIDStr(ids);
                    rowStartIndex = i;
                    rowStatus = -1;
                    ++rowIndex;
                }
                if(SCDItemDisplayInfo.isSCDItem(dib)) {
                    int scdShowStatus = -1;
                    if(noValueComparison) {
                        //scdShowStatus = dib.getNumDiscrepancyNotes() > 0 ? 0 : 
                            //dib.getData().getValue().length()>0 || dib.getIsSCDtoBeShown() ? 1 : 2;
                        scdShowStatus = dib.getNumDiscrepancyNotes() > 0 ? 
                            SCDShowStatus.SHOW_UNCHANGABLE.getCode() :  
                            dib.getData().getValue().length()>0 || dib.getIsSCDtoBeShown() ? 
                                SCDShowStatus.SHOW_CHANGABLE.getCode() : SCDShowStatus.HIDE_CHANGABLE.getCode();
                    } else {
                        //scdShowStatus = dib.getNumDiscrepancyNotes() > 0 || dib.getScdData().getDbValue().length() > 0 ? 0 : 
                            //dib.getData().getValue().length()>0 || dib.getIsSCDtoBeShown() ? 1 : 2;
                        scdShowStatus = dib.getNumDiscrepancyNotes() > 0 || dib.getScdData().getDbValue().length() > 0 ? 
                            SCDShowStatus.SHOW_UNCHANGABLE.getCode() : 
                            dib.getData().getValue().length()>0 || dib.getIsSCDtoBeShown() ? 
                                SCDShowStatus.SHOW_CHANGABLE.getCode() : SCDShowStatus.HIDE_CHANGABLE.getCode();
                    }
                    SCDItemDisplayInfo dinfo = dib.getScdData().getScdDisplayInfo();
                    dinfo.setScdShowStatus(scdShowStatus);
                    //dinfo.setScdShowStatus(SCDShowStatus.getByCode(scdShowStatus));
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
                    if(SCDItemDisplayInfo.isSCDItem(child)) {
                        //int scdShowStatus = child.getNumDiscrepancyNotes() > 0 ? 0 : 
                            //child.getData().getValue().length()>0 || child.getIsSCDtoBeShown() ? 1 : 2;
                        int scdShowStatus = child.getNumDiscrepancyNotes() > 0 ? 
                            SCDShowStatus.SHOW_UNCHANGABLE.getCode() : 
                            child.getData().getValue().length()>0 || child.getIsSCDtoBeShown() ? 
                                SCDShowStatus.SHOW_CHANGABLE.getCode() : SCDShowStatus.HIDE_CHANGABLE.getCode();
                         child.getScdData().getScdDisplayInfo().setScdShowStatus(scdShowStatus);
                         //child.getScdData().getScdDisplayInfo().setScdShowStatus(SCDShowStatus.getByCode(scdShowStatus));
                    }
                }
            }
        }
        if(rowStartIndex >= 0) {
            DisplayItemBean prevDib = allItems.get(rowStartIndex).getSingleItem();
            SCDItemDisplayInfo dinfo = prevDib.getScdData().getScdDisplayInfo();
            dinfo.setRowDisplayStatus(rowStatus);
            //dinfo.setRowDisplayStatus(SCDRowDisplayStatus.getByCode(rowStatus));
            dinfo.setRowSCDShowIDStr(ids+"-");
            dinfo.setRowFirstColIndex(rowStartIndex);
        }
        return section;
    }

    

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + rowDisplayStatus;
        result = prime * result + rowFirstColIndex;
        result = prime * result + rowNum;
        result = prime * result + (rowSCDShowIDStr == null ? 0 : rowSCDShowIDStr.hashCode());
        result = prime * result + scdShowStatus;
        return result;
    }



    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SCDItemDisplayInfo other = (SCDItemDisplayInfo) obj;
        if (rowDisplayStatus != other.rowDisplayStatus)
            return false;
        if (rowFirstColIndex != other.rowFirstColIndex)
            return false;
        if (rowNum != other.rowNum)
            return false;
        if (rowSCDShowIDStr == null) {
            if (other.rowSCDShowIDStr != null)
                return false;
        } else if (!rowSCDShowIDStr.equals(other.rowSCDShowIDStr))
            return false;
        if (scdShowStatus != other.scdShowStatus)
            return false;
        return true;
    }



    public static boolean isSCDItem(DisplayItemBean displayItemBean) {
        int scdId = displayItemBean.getScdData().getScdItemMetadataBean().getScdItemFormMetadataId();
        return scdId>0 && scdId == displayItemBean.getMetadata().getId() ? true : false;
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