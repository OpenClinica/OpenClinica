package core.org.akaza.openclinica.service;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class OCMessageDTO implements Serializable {
    @NotNull
    private String receiverPhone;

    @NotNull
    private String message;

    private String subdomain;

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public String getSubdomain() {
        return subdomain;
    }

    public void setSubdomain(String subdomain) {
        this.subdomain = subdomain;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
