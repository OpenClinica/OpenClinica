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

public class StudyEventDefBean extends ElementDefBean {
    private String type;
    private List<ElementRefBean> formRefs;
    //OpenClinica extension
    private EventDefinitionDetailsBean eventDefinitionDetais;
    
    public StudyEventDefBean() {
        formRefs = new ArrayList<ElementRefBean>();
        this.eventDefinitionDetais = new EventDefinitionDetailsBean();
    }

    public void setType(String type) {
        this.type = type;
    }
    
    public String getType() {
        return this.type;
    }
    
    public void setFormRefs(List<ElementRefBean> formRefs) {
        this.formRefs = formRefs;
    }

    public List<ElementRefBean> getFormRefs() {
        return this.formRefs;
    }

    public EventDefinitionDetailsBean getEventDefinitionDetais() {
        return eventDefinitionDetais;
    }

    public void setEventDefinitionDetais(EventDefinitionDetailsBean eventDefinitionDetais) {
        this.eventDefinitionDetais = eventDefinitionDetais;
    }
}