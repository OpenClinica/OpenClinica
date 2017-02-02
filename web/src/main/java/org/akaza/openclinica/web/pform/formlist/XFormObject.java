package org.akaza.openclinica.web.pform.formlist;

import org.akaza.openclinica.domain.xform.XformParserHelper;

public class XFormObject implements Form {
    private String xform = null;

    @Override
    public String decorate() {
        // TODO Auto-generated method stub
        return xform;
    }

    public String getXform() {
        return xform;
    }

    public void setXform(String xform) {
        this.xform = xform;
    }

    @Override
    public String decorate(XformParserHelper xformParserHelper) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

}
