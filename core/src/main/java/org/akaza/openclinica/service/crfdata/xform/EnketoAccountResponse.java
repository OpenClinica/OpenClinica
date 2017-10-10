package org.akaza.openclinica.service.crfdata.xform;

public class EnketoAccountResponse {

    private String server_url = null;
    private String api_key = null;
    private int code = 0;

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

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

}
