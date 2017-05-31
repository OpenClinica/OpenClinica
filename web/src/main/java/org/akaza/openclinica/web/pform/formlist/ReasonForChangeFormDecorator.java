package org.akaza.openclinica.web.pform.formlist;

import org.akaza.openclinica.domain.xform.XformParserHelper;

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

    @Override
    public String decorate(XformParserHelper xformParserHelper) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }
}