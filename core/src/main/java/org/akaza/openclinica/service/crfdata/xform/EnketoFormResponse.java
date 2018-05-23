package org.akaza.openclinica.service.crfdata.xform;

public class EnketoFormResponse {
    private EnketoURLResponse enketoUrlResponse;
    private boolean lockOn;

    public EnketoFormResponse(EnketoURLResponse enketoUrlResponse, boolean lockOn) {
        this.enketoUrlResponse = enketoUrlResponse;
        this.lockOn = lockOn;
    }

    public EnketoURLResponse getEnketoUrlResponse() {
        return enketoUrlResponse;
    }

    public void setEnketoUrlResponse(EnketoURLResponse enketoUrlResponse) {
        this.enketoUrlResponse = enketoUrlResponse;
    }

    public boolean isLockOn() {
        return lockOn;
    }

    public void setLockOn(boolean lockOn) {
        this.lockOn = lockOn;
    }
}
