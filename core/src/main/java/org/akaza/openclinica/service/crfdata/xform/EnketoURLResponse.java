package org.akaza.openclinica.service.crfdata.xform;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EnketoURLResponse {
    private String url = null;
    private String code = null;
    private boolean complete_button = true;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isComplete_button() {
        return complete_button;
    }

    public void setComplete_button(boolean complete_button) {
        this.complete_button = complete_button;
    }

}
