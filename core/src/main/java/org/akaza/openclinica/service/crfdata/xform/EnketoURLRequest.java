package org.akaza.openclinica.service.crfdata.xform;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EnketoURLRequest {
    private String server_url = null;
    private String form_id = null;
    private String load_warning = null;
    @JsonProperty("ecid")
    private String ecId = null;
    public String getLoad_warning() {
        return load_warning;
    }
    private String jini = null;
    private String pid = null;
    @JsonProperty("parent_window_origin")
    private String parentWindowOrigin = null;

    public void setLoad_warning(String load_warning) {
        this.load_warning = load_warning;
    }

    public EnketoURLRequest(String server_url, String ecId, String form_id, String load_warning, String jini, String pid, String parentWindowOrigin) {
        this.server_url = server_url;
        this.form_id = form_id;
        this.load_warning = load_warning;
        this.ecId = ecId;
        this.jini = jini;
        this.pid = pid;
        this.parentWindowOrigin = parentWindowOrigin;
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

    public String getEcId() {
        return ecId;
    }

    public void setEcId(String ecId) {
        this.ecId = ecId;
    }

    public String getJini() {
        return jini;
    }

    public void setJini(String jini) {
        this.jini = jini;
    }
    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getParentWindowOrigin() {
        return parentWindowOrigin;
    }

    public void setParentWindowOrigin(String parentWindowOrigin) {
        this.parentWindowOrigin = parentWindowOrigin;
    }
}
