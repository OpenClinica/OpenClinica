/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.bean.managestudy;

import org.akaza.openclinica.bean.core.AuditableEntityBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;

/**
 * @author jxu
 *
 */
public class DisplayEventDefinitionCRFBean extends AuditableEntityBean {
    private EventDefinitionCRFBean edc;
    private EventCRFBean eventCRF;
    private String enketoURL;
    private boolean completedEventCRFs;

    /**
     * @return Returns the completedEventCRFs.
     */
    public boolean isCompletedEventCRFs() {
        return completedEventCRFs;
    }

    /**
     * @param completedEventCRFs
     *            The completedEventCRFs to set.
     */
    public void setCompletedEventCRFs(boolean completedEventCRFs) {
        this.completedEventCRFs = completedEventCRFs;
    }

    /**
     * @return Returns the edc.
     */
    public EventDefinitionCRFBean getEdc() {
        return edc;
    }

    /**
     * @param edc
     *            The edc to set.
     */
    public void setEdc(EventDefinitionCRFBean edc) {
        this.edc = edc;
    }

    /**
     * @return Returns the eventCRF.
     */
    public EventCRFBean getEventCRF() {
        return eventCRF;
    }

    /**
     * @param eventCRF
     *            The eventCRF to set.
     */
    public void setEventCRF(EventCRFBean eventCRF) {
        this.eventCRF = eventCRF;
    }

    public String getEnketoURL() {
        return enketoURL;
    }

    public void setEnketoURL(String enketoURL) {
        this.enketoURL = enketoURL;
    }
    
    
}
