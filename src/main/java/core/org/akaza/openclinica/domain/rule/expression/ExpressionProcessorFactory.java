/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package core.org.akaza.openclinica.domain.rule.expression;

import core.org.akaza.openclinica.exception.OpenClinicaSystemException;

/**
 * @author Krikor Krumlian
 * 
 */
public class ExpressionProcessorFactory {

    public static ExpressionProcessor createExpressionProcessor(ExpressionObjectWrapper expressionWrapper) {

        ExpressionProcessor ep = null;

        switch (expressionWrapper.getExpressionBean().getContext()) {
        case OC_RULES_V1: {
            ep = new OpenClinicaV1ExpressionProcessor(expressionWrapper);
            break;
        }
        default:
            throw new OpenClinicaSystemException("Context : " + expressionWrapper.getExpressionBean().getContext() + " not Valid");
        }

        return ep;

    }

}
