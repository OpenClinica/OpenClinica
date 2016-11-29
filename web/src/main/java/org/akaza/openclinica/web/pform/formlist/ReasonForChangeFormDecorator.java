package org.akaza.openclinica.web.pform.formlist;

public class ReasonForChangeFormDecorator extends FormDecorator {

    public ReasonForChangeFormDecorator(Form form) {
        super(form);
        // TODO Auto-generated constructor stub
    }

    @Override
    public String decorate() throws Exception {
        return applyReasonForChange(form.decorate());
    }

    private String applyReasonForChange(String xform) {
        System.out.println("Applying Reason For Change on XForm ");
        return xform;
    }
}