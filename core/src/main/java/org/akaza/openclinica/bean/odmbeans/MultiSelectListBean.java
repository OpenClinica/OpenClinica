/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2010 Akaza
 * Research
 *
 */

package org.akaza.openclinica.bean.odmbeans;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author ywang (March, 2010)
 *
 */
public class MultiSelectListBean extends ElementOIDBean {
    private String name;
    private String dataType;
    private String actualDataType;
    private List<MultiSelectListItemBean> multiSelectListItems = new ArrayList<MultiSelectListItemBean> ();
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDataType() {
        return dataType;
    }
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
    public String getActualDataType() {
        return actualDataType;
    }
    public void setActualDataType(String actualDataType) {
        this.actualDataType = actualDataType;
    }
    public List<MultiSelectListItemBean> getMultiSelectListItems() {
        return multiSelectListItems;
    }
    public void setMultiSelectListItems(List<MultiSelectListItemBean> multiSelectListItems) {
        this.multiSelectListItems = multiSelectListItems;
    }
}