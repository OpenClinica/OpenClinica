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
public class UnaryMinusNode extends ExpressionNode {
    ExpressionNode operand; // The operand to which the unary minus applies.

    UnaryMinusNode(ExpressionNode operand) {
        // Construct a UnaryMinusNode with the specified operand.
        assert operand != null;
        this.operand = operand;
    }

    @Override
    String testCalculate() throws OpenClinicaSystemException {
        return (String) calculate();
    }

    @Override
    Object calculate() {
        // The value is the negative of the value of the operand.
        String theOperand = (String) operand.value();
        validate(theOperand);
        double neg = Double.valueOf(theOperand);
        return String.valueOf(-neg);
    }

    void validate(String theOperand) throws OpenClinicaSystemException {
        try {
            Double.valueOf(theOperand);
        } catch (NumberFormatException e) {
            throw new OpenClinicaSystemException("OCRERR_0015", new Object[] { theOperand });
        }
    }

    @Override
    void printStackCommands() {
        // To evaluate this expression on a stack machine, first do
        // whatever is necessary to evaluate the operand, leaving the
        // operand on the stack. Then apply the unary minus (which means
        // popping the operand, negating it, and pushing the result).
        operand.printStackCommands();
        logger.info("  Unary minus");
    }
}