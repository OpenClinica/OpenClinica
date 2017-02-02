package org.akaza.openclinica.web.pform.formlist;

import org.akaza.openclinica.domain.xform.XformParserHelper;

public interface Form {
    String decorate() throws Exception;

    String decorate(XformParserHelper xformParserHelper) throws Exception;

}
