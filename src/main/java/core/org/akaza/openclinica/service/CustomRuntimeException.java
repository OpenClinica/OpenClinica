package core.org.akaza.openclinica.service;

import java.util.ArrayList;
import java.util.List;

import core.org.akaza.openclinica.service.crfdata.ErrorObj;

public class CustomRuntimeException extends RuntimeException {
    List<ErrorObj> errList;

    public CustomRuntimeException(String message) {
        super(message);
        errList = new ArrayList<>();
    }

    public CustomRuntimeException(String message, List<ErrorObj> errList) {
        super(message);
        this.errList = errList;
    }

    public boolean hasErrors() {
        return ! errList.isEmpty();
    }

    public List<ErrorObj> getErrList() {
        return errList;
    }

    public void setErrList(List<ErrorObj> errList) {
        this.errList = errList;
    }

    public void addError(ErrorObj errorObj){
        errList.add(errorObj);
    }

}
