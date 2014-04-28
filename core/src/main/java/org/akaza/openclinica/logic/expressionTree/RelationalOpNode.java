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
public class RelationalOpNode extends ExpressionNode {
    Operator op; // The operator.
    ExpressionNode left; // The expression for its left operand.
    ExpressionNode right; // The expression for its right operand.

    RelationalOpNode(Operator op, ExpressionNode left, ExpressionNode right) {
        // Construct a BinOpNode containing the specified data.
        assert op == Operator.GREATER_THAN || op == Operator.GREATER_THAN_EQUAL || op == Operator.LESS_THAN || op == Operator.LESS_THAN_EQUAL;
        assert left != null && right != null;
        this.op = op;
        this.left = left;
        this.right = right;
    }

    @Override
    String testCalculate() throws OpenClinicaSystemException {
        double x, y;
        String l = String.valueOf(left.testValue());
        String r = String.valueOf(right.testValue());
        if(blankAgainstDateyyyyMMdd(l,r)) {
            return "blankAgainstDateyyyyMMdd";
        }
        validate(l, r, left.getNumber(), right.getNumber());
        if (ExpressionTreeHelper.isDateyyyyMMdd(l) && ExpressionTreeHelper.isDateyyyyMMdd(r)) {
            x = ExpressionTreeHelper.getDate(l).getTime();
            y = ExpressionTreeHelper.getDate(r).getTime();
        } else {
            x = Double.valueOf(l);
            y = Double.valueOf(r);
        }
        return calc(x, y);
    }

    @Override
    Object calculate() throws OpenClinicaSystemException {
        double x, y;
        String l = String.valueOf(left.value());
        String r = String.valueOf(right.value());
        if(blankAgainstDateyyyyMMdd(l,r)) {
            return "blankAgainstDateyyyyMMdd";
        }
        validate(l, r);
        if (ExpressionTreeHelper.isDateyyyyMMdd(l) && ExpressionTreeHelper.isDateyyyyMMdd(r)) {
            x = ExpressionTreeHelper.getDate(l).getTime();
            y = ExpressionTreeHelper.getDate(r).getTime();
        } else {
            x = Double.valueOf(l);
            y = Double.valueOf(r);
        }
        return calc(x, y);
    }

    private String calc(double x, double y) throws OpenClinicaSystemException {
        switch (op) {
        case GREATER_THAN:
            return String.valueOf(x > y);
        case GREATER_THAN_EQUAL:
            return String.valueOf(x >= y);
        case LESS_THAN:
            return String.valueOf(x < y);
        case LESS_THAN_EQUAL:
            return String.valueOf(x <= y);
        default:
            return null; // Bad operator!
        }
    }

    private boolean isDouble(String x, String y) {
        try {
            Double.valueOf(x);
            Double.valueOf(y);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    void validate(String l, String r) throws OpenClinicaSystemException {
        if (!(ExpressionTreeHelper.isDateyyyyMMdd(l) && ExpressionTreeHelper.isDateyyyyMMdd(r)) && !isDouble(l, r)) {
            //throw new OpenClinicaSystemException(l + " and " + r + " cannot be used with the " + op.toString() + " operator");
            throw new OpenClinicaSystemException("OCRERR_0001", new Object[] { l, r, op.toString() });
        }
    }

    void validate(String l, String r, String ltext, String rtext) throws OpenClinicaSystemException {
        if (!(ExpressionTreeHelper.isDateyyyyMMdd(l) && ExpressionTreeHelper.isDateyyyyMMdd(r)) && !isDouble(l, r)) {
            //throw new OpenClinicaSystemException(l + " and " + r + " cannot be used with the " + op.toString() + " operator");
            throw new OpenClinicaSystemException("OCRERR_0001", new Object[] { ltext, rtext, op.toString() });
        }
    }
    
    /*
     * Precondition: both l and r are not empty.
     * Return true only if one is dd-MMM-yyyy format, another is ExpressionTreeHelper.isDateyyyyMMdd
     */
    private boolean preValidateOnddMMMyyyyDashes(String l, String r) {
        if(ExpressionTreeHelper.isDateddMMMyyyyDashes(l)&&ExpressionTreeHelper.isDateyyyyMMdd(r)
            ||ExpressionTreeHelper.isDateddMMMyyyyDashes(r)&&ExpressionTreeHelper.isDateyyyyMMdd(l)) {
            return true;
        }
        return false;
    }
    
    private boolean blankAgainstDateyyyyMMdd(String l, String r) {
        return l.isEmpty() && ExpressionTreeHelper.isDateyyyyMMdd(r) 
                || r.isEmpty() && ExpressionTreeHelper.isDateyyyyMMdd(l);
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
