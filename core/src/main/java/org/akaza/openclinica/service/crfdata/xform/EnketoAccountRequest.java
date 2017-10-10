package org.akaza.openclinica.service.crfdata.xform;

public class EnketoAccountRequest {
    private String server_url = null;
    private String api_key = null;

    public EnketoAccountRequest(String server_url, String api_key) {
        this.server_url = server_url;
        this.api_key = api_key;
    }

    public String getServer_url() {
        return server_url;
    }

    public void setServer_url(String server_url) {
        this.server_url = server_url;
    }

    public String getApi_key() {
        return api_key;
    }

    public void setApi_key(String api_key) {
        this.api_key = api_key;
    }

}
