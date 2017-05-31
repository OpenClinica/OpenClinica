package org.akaza.openclinica.web.pform.formlist;

public abstract class FormDecorator implements Form {
    protected Form form;

    public FormDecorator(Form form) {
        this.form = form;
    }

    public String decorate() throws Exception {
        return form.decorate();
    }
}