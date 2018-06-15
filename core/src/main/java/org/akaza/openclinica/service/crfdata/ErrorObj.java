package org.akaza.openclinica.service.crfdata;

public class ErrorObj {
    private String code;
    private String message;

    public ErrorObj() {
        super();
    }

    public ErrorObj(String code, String message) {
        super();
        this.code = code;
        this.message = message;
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

    @Override
    public String toString() {
        return "ErrorObj{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
