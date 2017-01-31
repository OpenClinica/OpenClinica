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

public class FormDefBean extends ElementDefBean {
    private List<ElementRefBean> itemGroupRefs;
    private List<ElementRefBean> formLayoutRefs;

    // openclinica extension
    private FormDetailsBean formDetails;

    public FormDefBean() {
        itemGroupRefs = new ArrayList<ElementRefBean>();
        formDetails = new FormDetailsBean();
    }

    public void setItemGroupRefs(List<ElementRefBean> igRefs) {
        this.itemGroupRefs = igRefs;
    }

    public List<ElementRefBean> getItemGroupRefs() {
        return this.itemGroupRefs;
    }

    public FormDetailsBean getFormDetails() {
        return formDetails;
    }

    public void setFormDetails(FormDetailsBean formDetails) {
        this.formDetails = formDetails;
    }

    public List<ElementRefBean> getFormLayoutRefs() {
        return formLayoutRefs;
    }

    public void setFormLayoutRefs(List<ElementRefBean> formLayoutRefs) {
        this.formLayoutRefs = formLayoutRefs;
    }

}