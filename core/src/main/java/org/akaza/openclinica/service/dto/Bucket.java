package org.akaza.openclinica.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

/**
 * Created by krikorkrumlian on 6/7/17.
 */
@JsonIgnoreProperties
public class Bucket {

    @JsonProperty("forms")
    ArrayList<Form> forms;

    public ArrayList<Form> getForms() {
        return forms;
    }

    public void setForms(ArrayList<Form> forms) {
        this.forms = forms;
    }
}
