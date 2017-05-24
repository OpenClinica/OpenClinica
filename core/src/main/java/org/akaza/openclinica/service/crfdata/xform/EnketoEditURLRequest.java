package org.akaza.openclinica.service.crfdata.xform;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class EnketoEditURLRequest {

    private String server_url = null;
    private String form_id = null;
    private String instance_id = null;
    private String return_url = null;
    private String instance = null;
    private String complete_button = null;
    @JsonProperty("instance_attachments")
    private InstanceAttachment instanceAttachments;
    @JsonIgnore
    private Map<String, String> additionalProperties = new HashMap<String, String>();

    @JsonProperty("instance_attachments")
    public InstanceAttachment getInstanceAttachments() {
        return instanceAttachments;
    }

    @JsonProperty("instance_attachments")
    public void setInstanceAttachments(InstanceAttachment instanceAttachments) {
        this.instanceAttachments = instanceAttachments;
    }

    @JsonAnyGetter
    public Map<String, String> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, String value) {
        this.additionalProperties.put(name, value);
    }

    public EnketoEditURLRequest(String server_url, String form_id, String instance_id, String return_url, String instance, String complete_button,
            InstanceAttachment instanceAttachments) {
        this.server_url = server_url;
        this.form_id = form_id;
        this.instance_id = instance_id;
        this.return_url = return_url;
        this.instance = instance;
        this.complete_button = complete_button;
        this.instanceAttachments = instanceAttachments;
    }

    public String getServer_url() {
        return server_url;
    }

    public void setServer_url(String server_url) {
        this.server_url = server_url;
    }

    public String getForm_id() {
        return form_id;
    }

    public void setForm_id(String form_id) {
        this.form_id = form_id;
    }

    public String getInstance_id() {
        return instance_id;
    }

    public void setInstance_id(String instance_id) {
        this.instance_id = instance_id;
    }

    public String getReturn_url() {
        return return_url;
    }

    public void setReturn_url(String return_url) {
        this.return_url = return_url;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getComplete_button() {
        return complete_button;
    }

    public void setComplete_button(String complete_button) {
        this.complete_button = complete_button;
    }

}
