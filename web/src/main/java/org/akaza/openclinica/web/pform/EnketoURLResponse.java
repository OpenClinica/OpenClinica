package org.akaza.openclinica.web.pform;

public class EnketoURLResponse {
    private String url = null;
    private String preview_url = null;
    private String edit_url = null;
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

}
