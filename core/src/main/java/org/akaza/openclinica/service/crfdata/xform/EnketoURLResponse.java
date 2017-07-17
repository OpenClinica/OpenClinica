package org.akaza.openclinica.service.crfdata.xform;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EnketoURLResponse {
    private String url = null;
    private String preview_url = null;
    private String edit_url = null;
    private String offline_url = null;
    private String iframe_url = null;
    private String code = null;
    private boolean complete_button = true;
    private String edit_iframe_url = null;

    private String single_fieldsubmission_iframe_url = null;

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

    public String getIframe_url() {
        return iframe_url;
    }

    public void setIframe_url(String iframe_url) {
        this.iframe_url = iframe_url;
    }

    public String getSingle_fieldsubmission_iframe_url() {
        return single_fieldsubmission_iframe_url;
    }

    public void setSingle_fieldsubmission_iframe_url(String single_fieldsubmission_iframe_url) {
        this.single_fieldsubmission_iframe_url = single_fieldsubmission_iframe_url;
    }

    public boolean isComplete_button() {
        return complete_button;
    }

    public void setComplete_button(boolean complete_button) {
        this.complete_button = complete_button;
    }

    public String getEdit_iframe_url() {
        return edit_iframe_url;
    }

    public void setEdit_iframe_url(String edit_iframe_url) {
        this.edit_iframe_url = edit_iframe_url;
    }

}
