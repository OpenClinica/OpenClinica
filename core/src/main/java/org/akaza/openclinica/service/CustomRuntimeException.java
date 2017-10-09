package org.akaza.openclinica.service;

import java.util.List;

import org.akaza.openclinica.service.crfdata.ErrorObj;

public class CustomRuntimeException extends RuntimeException {
    List<ErrorObj> errList;

    public CustomRuntimeException(String message, List<ErrorObj> errList) {
        super(message);
        this.errList = errList;
    }

    public List<ErrorObj> getErrList() {
        return errList;
    }

    public void setErrList(List<ErrorObj> errList) {
        this.errList = errList;
    }

}
