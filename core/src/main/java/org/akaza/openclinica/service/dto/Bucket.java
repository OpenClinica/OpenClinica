package org.akaza.openclinica.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by krikorkrumlian on 6/7/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Bucket extends AbstractAuditingEntity {

    @JsonProperty("forms")
    ArrayList<Form> forms;

    public ArrayList<Form> getForms() {
        return forms;
    }

    public void setForms(ArrayList<Form> forms) {
        this.forms = forms;
    }
}
