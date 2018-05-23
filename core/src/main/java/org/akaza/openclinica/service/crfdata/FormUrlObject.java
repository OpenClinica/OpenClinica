package org.akaza.openclinica.service.crfdata;

public class FormUrlObject {
    private String formUrl;
    private boolean lockOn;

    public FormUrlObject(String formUrl, boolean lockOn) {
        this.formUrl = formUrl;
        this.lockOn = lockOn;
    }

    public String getFormUrl() {
        return formUrl;
    }

    public void setFormUrl(String formUrl) {
        this.formUrl = formUrl;
    }

    public boolean isLockOn() {
        return lockOn;
    }

    public void setLockOn(boolean lockOn) {
        this.lockOn = lockOn;
    }
}
