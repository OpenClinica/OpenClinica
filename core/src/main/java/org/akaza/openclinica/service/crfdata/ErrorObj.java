package org.akaza.openclinica.service.crfdata;

public class ErrorObj {
    private String code;
    private String message;
    private String timeStamp;

    public ErrorObj() {
        super();
    }

    public ErrorObj(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public ErrorObj(String code, String message, String timeStamp) {
        this.code = code;
        this.message = message;
        this.timeStamp = timeStamp;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
    @Override
    public String toString() {
        return "ErrorObj{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", timeStamp='" + timeStamp + '\'' +
                '}';
    }
}
