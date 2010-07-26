/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.logic.expressionTree;

import org.akaza.openclinica.exception.OpenClinicaSystemException;

/**
 * @author Krikor Krumlian
 * 
 */
class ExpressionNodeFactory {

    static ExpressionNode getExpNode(Operator op, ExpressionNode node1, ExpressionNode node2) {
        if (op == Operator.PLUS || op == Operator.MINUS || op == Operator.MULTIPLY || op == Operator.DIVIDE) {
            return new ArithmeticOpNode(op, node1, node2);
        } else if (op == Operator.GREATER_THAN || op == Operator.GREATER_THAN_EQUAL || op == Operator.LESS_THAN || op == Operator.LESS_THAN_EQUAL) {
            return new RelationalOpNode(op, node1, node2);
        } else if (op == Operator.EQUAL || op == Operator.NOT_EQUAL || op == Operator.CONTAINS) {
            return new EqualityOpNode(op, node1, node2);
        } else if (op == Operator.OR || op == Operator.AND) {
            return new ConditionalOpNode(op, node1, node2);
        } else {
            throw new OpenClinicaSystemException("OCRERR_0003");
        }

    }
}