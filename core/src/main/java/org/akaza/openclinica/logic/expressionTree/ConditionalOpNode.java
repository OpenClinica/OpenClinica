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
public class ConditionalOpNode extends ExpressionNode {
    Operator op; // The operator.
    ExpressionNode left; // The expression for its left operand.
    ExpressionNode right; // The expression for its right operand.

    ConditionalOpNode(Operator op, ExpressionNode left, ExpressionNode right) {
        // Construct a BinOpNode containing the specified data.
        assert op == Operator.OR || op == Operator.AND;
        assert left != null && right != null;
        this.op = op;
        this.left = left;
        this.right = right;
    }

    @Override
    String testCalculate() throws OpenClinicaSystemException {
        String l = String.valueOf(left.testValue());
        String r = String.valueOf(right.testValue());
        validate(l, r);
        return calc(l, r);
    }

    @Override
    String calculate() throws OpenClinicaSystemException {
        String l = String.valueOf(left.value());
        String r = String.valueOf(right.value());
        validate(l, r);
        return calc(l, r);
    }

    private String calc(String x, String y) throws OpenClinicaSystemException {

        switch (op) {
        case OR:
            return String.valueOf(Boolean.valueOf(x) || Boolean.valueOf(y));
        case AND:
            return String.valueOf(Boolean.valueOf(x) && Boolean.valueOf(y));
        default:
            throw new OpenClinicaSystemException(left.value() + " and " + right.value() + " cannot be calculated with the " + op.toString() + " operator");
        }
    }

    void validate(String l, String r) throws OpenClinicaSystemException {
        try {
            Boolean.valueOf(l);
            Boolean.valueOf(r);
        } catch (NumberFormatException e) {
            throw new OpenClinicaSystemException(l + " and " + r + " cannot be used with the " + op.toString() + " operator");
        }
    }

    @Override
    void printStackCommands() {
        // To evalute the expression on a stack machine, first do
        // whatever is necessary to evaluate the left operand, leaving
        // the answer on the stack. Then do the same thing for the
        // second operand. Then apply the operator (which means popping
        // the operands, applying the operator, and pushing the result).
        left.printStackCommands();
        right.printStackCommands();
        logger.info("  Operator " + op);
    }
}