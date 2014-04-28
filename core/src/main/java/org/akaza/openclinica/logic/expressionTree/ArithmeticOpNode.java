/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.logic.expressionTree;

import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.joda.time.DateMidnight;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * @author Krikor Krumlian
 * 
 */
public class ArithmeticOpNode extends ExpressionNode {
    Operator op;
    ExpressionNode left;
    ExpressionNode right;

    ArithmeticOpNode(Operator op, ExpressionNode left, ExpressionNode right) {
        // Construct a BinOpNode containing the specified data.
        assert op == Operator.PLUS || op == Operator.MINUS || op == Operator.MULTIPLY || op == Operator.DIVIDE;
        assert left != null && right != null;
        this.op = op;
        this.left = left;
        this.right = right;
    }

    @Override
    String testCalculate() throws OpenClinicaSystemException {

        String l = left.testValue();
        String r = right.testValue();
        validate(l, r, left.getNumber(), right.getNumber());
        String t = calculateDate(l, r);
        if (t != null) {
            return t;
        }

        double x = Double.valueOf(l);
        double y = Double.valueOf(r);
        return calc(x, y);
    }

    @Override
    Object calculate() throws OpenClinicaSystemException {

        String l = (String) left.value();
        String r = (String) right.value();
        validate(l, r);
        String t = calculateDate(l, r);
        if (t != null) {
            return t;
        }

        double x = Double.valueOf(l);
        double y = Double.valueOf(r);
        return calc(x, y);

    }

    private String calculateDate(String leftValue, String rightValue) {
        if (ExpressionTreeHelper.isDateyyyyMMdd(leftValue) && isDouble(rightValue)) {
            logger.debug("We determined that leftValue was a date and rightValue a Double");
            return calculateGenericDate(leftValue, rightValue);
        }
        if (isDouble(leftValue) && ExpressionTreeHelper.isDateyyyyMMdd(rightValue)) {
            logger.debug("We determined that rightValue was a date and leftValue a Double");
            return calculateGenericDate(rightValue, leftValue);
        }
        if (ExpressionTreeHelper.isDateyyyyMMdd(leftValue) && ExpressionTreeHelper.isDateyyyyMMdd(rightValue)) {
            logger.debug("We determined that rightValue was a date and leftValue a Date");
            return calculateDaysBetween(rightValue, leftValue);
        }
        return null;
    }

    private String calculateDaysBetween(String value1, String value2) {
        DateMidnight dm1 = new DateMidnight(ExpressionTreeHelper.getDate(value1).getTime());
        DateMidnight dm2 = new DateMidnight(ExpressionTreeHelper.getDate(value2).getTime());
        switch (op) {
        case MINUS: {
            Days d = Days.daysBetween(dm1, dm2);
            int days = d.getDays();
            return String.valueOf(Math.abs(days));
        }
        default:
            return null; // Bad operator!
        }
    }

    private String calculateGenericDate(String value1, String value2) {
        DateMidnight dm = new DateMidnight(ExpressionTreeHelper.getDate(value1).getTime());
        DateTimeFormatter fmt = ISODateTimeFormat.date();
        switch (op) {
        case PLUS: {
            dm = dm.plusDays(Double.valueOf(value2).intValue());
            return fmt.print(dm);
        }
        case MINUS: {
            dm = dm.minusDays(Double.valueOf(value2).intValue());
            return fmt.print(dm);
        }
        default:
            return null; // Bad operator!
        }
    }

    private String calc(double x, double y) throws OpenClinicaSystemException {
        switch (op) {
        case PLUS:
            return String.valueOf(x + y);
        case MINUS:
            return String.valueOf(x - y);
        case MULTIPLY:
            return String.valueOf(x * y);
        case DIVIDE:
            return String.valueOf(x / y);
        default:
            return null; // Bad operator!
        }
    }

    void validate(String l, String r) throws OpenClinicaSystemException {

        String t = calculateDate(l, r);
        if (t == null) {
            try {
                Double.valueOf(l);
                Double.valueOf(r);
            } catch (NumberFormatException e) {
                throw new OpenClinicaSystemException("OCRERR_0001", new Object[] { l, r, op.toString() });
            }
        }
    }

    void validate(String l, String r, String ltext, String rtext) throws OpenClinicaSystemException {

        String t = calculateDate(l, r);
        if (t == null) {
            try {
                Double.valueOf(l);
                Double.valueOf(r);
            } catch (NumberFormatException e) {
                throw new OpenClinicaSystemException("OCRERR_0001", new Object[] { ltext, rtext, op.toString() });
            }
        }
    }

    boolean isInteger(String value) {
        try {
            Integer.valueOf(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    boolean isDouble(String value) {
        try {
            Double.valueOf(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    void printStackCommands() {
        left.printStackCommands();
        right.printStackCommands();
        logger.info("  Operator " + op);
    }
}
