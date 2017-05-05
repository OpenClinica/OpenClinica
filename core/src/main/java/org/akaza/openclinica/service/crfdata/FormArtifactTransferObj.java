package org.akaza.openclinica.service.crfdata;

import java.util.List;

import org.akaza.openclinica.domain.xform.XformContainer;
import org.akaza.openclinica.service.dto.Form;

public class FormArtifactTransferObj {
    private Form form;
    private XformContainer container;
    private List<ErrorObj> err;

    public FormArtifactTransferObj() {
        super();
        // TODO Auto-generated constructor stub
    }

    public FormArtifactTransferObj(Form form, List<ErrorObj> err, XformContainer container) {
        this.form = form;
        this.container = container;
        this.err = err;
    }

    public Form getForm() {
        return form;
    }

    public void setForm(Form form) {
        this.form = form;
    }

    public XformContainer getContainer() {
        return container;
    }

    public void setContainer(XformContainer container) {
        this.container = container;
    }

    public List<ErrorObj> getErr() {
        return err;
    }

    public void setErr(List<ErrorObj> err) {
        this.err = err;
    }

}