package org.akaza.openclinica.service.crfdata.xform;

public class EnketoURLRequest {
    private String server_url = null;
    private String form_id = null;

    public EnketoURLRequest(String server_url, String form_id) {
        this.server_url = server_url;
        this.form_id = form_id;
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
}
