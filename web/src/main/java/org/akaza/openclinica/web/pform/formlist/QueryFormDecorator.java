package org.akaza.openclinica.web.pform.formlist;

import javax.inject.Singleton;

import org.akaza.openclinica.domain.xform.XformParserHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class QueryFormDecorator extends FormDecorator {
    public static final String QUERY = "-query";
    public static final String COMMENT = "_comment";

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public QueryFormDecorator(Form form) {
        super(form);
    }

    @Override
    public String decorate(XformParserHelper xformParserHelper) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

}
