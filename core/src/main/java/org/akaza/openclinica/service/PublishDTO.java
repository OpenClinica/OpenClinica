/**
 * 
 */
package org.akaza.openclinica.service;

import org.cdisc.ns.odm.v130.ODM;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author joekeremian
 *
 */
@JsonIgnoreProperties
public class PublishDTO {
    private Page page;
    @JsonProperty("ODM")
    private ODM odm;

    public ODM getOdm() {
        return odm;
    }

    public void setOdm(ODM odm) {
        this.odm = odm;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

}
