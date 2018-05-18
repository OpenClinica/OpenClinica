package org.akaza.openclinica.service.crfdata.xform;

public class EnketoURLRequest {
    private String server_url = null;
    private String form_id = null;
    private String load_warning = null;

    public String getLoad_warning() {
        return load_warning;
    }

    public void setLoad_warning(String load_warning) {
        this.load_warning = load_warning;
    }

    public EnketoURLRequest(String server_url, String form_id, String load_warning) {
        this.server_url = server_url;
        this.form_id = form_id;
        this.load_warning = load_warning;

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
