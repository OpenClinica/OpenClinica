/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2005 Akaza
 * Research
 *
 */

package org.akaza.openclinica.bean.odmbeans;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author ywang (May, 2008)
 * 
 */

public class CodeListBean extends ElementOIDBean {
    private String name;
    private String dataType;
    private String preSASFormatName;
    private List<CodeListItemBean> codeListItems;
    
    public CodeListBean() {
        this.codeListItems = new ArrayList<CodeListItemBean>();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setDataType(String datatype) {
        this.dataType = datatype;
    }

    public String getDataType() {
        return this.dataType;
    }

    public void setPreSASFormatName(String sasname) {
        this.preSASFormatName = sasname;
    }

    public String getPreSASFormatName() {
        return this.preSASFormatName;
    }

    public void setCodeListItems(List<CodeListItemBean> codeListItems) {
        this.codeListItems = codeListItems;
    }

    public List<CodeListItemBean> getCodeListItems() {
        return this.codeListItems;
    }
}