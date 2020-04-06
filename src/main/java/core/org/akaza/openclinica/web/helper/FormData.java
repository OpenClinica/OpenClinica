package core.org.akaza.openclinica.web.helper;

import java.util.Objects;

public class FormData {
    private String formOid;

    public FormData(String formOid) {
        this.formOid = formOid;
    }

    public String getFormOid() {
        return formOid;
    }

    public void setFormOid(String formOid) {
        this.formOid = formOid;
    }


}
