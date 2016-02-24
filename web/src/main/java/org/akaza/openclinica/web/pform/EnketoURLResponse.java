package org.akaza.openclinica.web.pform;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EnketoURLResponse {
    private String url = null;
    private String preview_url = null;
    private String edit_url = null;
    private String offline_url = null;
    private String offline_iframe_url;
    private String code = null;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCode() {
        return code;
    }

    public String getPreview_url() {
        return preview_url;
    }

    public void setPreview_url(String preview_url) {
        this.preview_url = preview_url;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getEdit_url() {
        return edit_url;
    }

    public void setEdit_url(String edit_url) {
        this.edit_url = edit_url;
    }

    public String getOffline_url() {
        return offline_url;
    }

    public void setOffline_url(String offline_url) {
        this.offline_url = offline_url;
    }

    public String getOffline_iframe_url() {
        return offline_iframe_url;
    }

    public void setOffline_iframe_url(String offline_iframe_url) {
        this.offline_iframe_url = offline_iframe_url;
    }
}
