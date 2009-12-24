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

public class ItemGroupDefBean extends ElementDefBean {
    private String preSASDatasetName;
    private String comment;
    private List<ElementRefBean> itemRefs;

    public ItemGroupDefBean() {
        itemRefs = new ArrayList<ElementRefBean>();
    }

    public void setPreSASDatasetName(String sasname) {
        this.preSASDatasetName = sasname;
    }

    public String getPreSASDatasetName() {
        return this.preSASDatasetName;
    }

    public void setItemRefs(List<ElementRefBean> itemRefs) {
        this.itemRefs = itemRefs;
    }

    public List<ElementRefBean> getItemRefs() {
        return this.itemRefs;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return this.comment;
    }
}