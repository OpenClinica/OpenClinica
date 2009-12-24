/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.logic.expressionTree;

import org.akaza.openclinica.domain.rule.expression.ExpressionObjectWrapper;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Krikor Krumlian
 * 
 */
public class OpenClinicaExpressionParser {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    TextIO textIO;
    ExpressionObjectWrapper expressionWrapper;
    private final String ERROR_MESSAGE = "Extra Data in Expression.";

    public OpenClinicaExpressionParser() {
        textIO = new TextIO();
    }

    public OpenClinicaExpressionParser(ExpressionObjectWrapper expressionWrapper) {
        textIO = new TextIO();
        this.expressionWrapper = expressionWrapper;
    }

    public void parseExpression(String expression) throws OpenClinicaSystemException {
        getTextIO().fillBuffer(expression);
        getTextIO().skipBlanks();
        ExpressionNode exp = expressionTree();
        if (getTextIO().peek() != '\n')
            throw new OpenClinicaSystemException(ERROR_MESSAGE);
        exp.printStackCommands();
    }

    public String parseAndEvaluateExpression(String expression) throws OpenClinicaSystemException {
        getTextIO().fillBuffer(expression);
        getTextIO().skipBlanks();
        ExpressionNode exp = expressionTree();
        if (getTextIO().peek() != '\n')
            throw new OpenClinicaSystemException(ERROR_MESSAGE);
        return exp.value();
    }

    public String parseAndTestEvaluateExpression(String expression) throws OpenClinicaSystemException {
        getTextIO().fillBuffer(expression);
        getTextIO().skipBlanks();
        ExpressionNode exp = expressionTree();
        if (getTextIO().peek() != '\n')
            throw new OpenClinicaSystemException(ERROR_MESSAGE);
        return exp.testValue();

    }

    /**
     * Reads an expression from the current line of input and builds an
     * expression tree that represents the expression.
     * 
     * @return an ExpNode which is a pointer to the root node of the expression
     *         tree
     * @throws OpenClinicaSystemException
     *             if a syntax error is found in the input
     */
    private ExpressionNode expressionTree() throws OpenClinicaSystemException {
        textIO.skipBlanks();
        boolean negative; // True if there is a leading minus sign.
        negative = false;
        if (textIO.peek() == '-') {
            textIO.getAnyChar();
            negative = true;
        }
        ExpressionNode exp; // The expression tree for the expression.
        exp = termTree3(); // Start with the first term.
        if (negative)
            exp = new UnaryMinusNode(exp);
        textIO.skipBlanks();

        while (textIO.peek() == 'o' && textIO.peek(3).matches("or ") || textIO.peek() == 'a' && textIO.peek(4).matches("and ")) {
            // Read the next term and combine it with the
            // previous terms into a bigger expression tree.
            // char op = textIO.getAnyChar();
            String op = textIO.peek() == 'o' ? textIO.getAnyString(3) : textIO.getAnyString(4);
            logger.info("Operator" + op);
            ExpressionNode nextTerm = termTree3();
            exp = ExpressionNodeFactory.getExpNode(Operator.getByDescription(op), exp, nextTerm);
            textIO.skipBlanks();
        }

        return exp;
    } // end expressionTree()

    private ExpressionNode termTree3() throws OpenClinicaSystemException {
        textIO.skipBlanks();
        ExpressionNode term; // The expression tree representing the term.
        term = termTree2();
        textIO.skipBlanks();

        while (textIO.peek() == 'e' && textIO.peek(3).matches("eq ") || textIO.peek() == 'n' && textIO.peek(3).matches("ne ") || textIO.peek() == 'c'
            && textIO.peek(3).matches("ct ") || textIO.peek() == 'g' && textIO.peek(3).matches("gt ") || textIO.peek() == 'g' && textIO.peek(4).matches("gte ")
            || textIO.peek() == 'l' && textIO.peek(3).matches("lt ") || textIO.peek() == 'l' && textIO.peek(4).matches("lte ")) {
            // Read the next term and combine it with the
            // previous terms into a bigger expression tree.
            // char op = textIO.getAnyChar();
            String op = textIO.peek(4).matches("gte ") || textIO.peek(4).matches("lte ") ? textIO.getAnyString(4) : textIO.getAnyString(3);
            ExpressionNode nextTerm = termTree2();
            term = ExpressionNodeFactory.getExpNode(Operator.getByDescription(String.valueOf(op)), term, nextTerm);
            // term = new BooleanOpNode(Operator.getByDescription(op), term,
            // nextTerm);
            textIO.skipBlanks();
        }

        return term;
    } // end termValue()

    private ExpressionNode termTree2() throws OpenClinicaSystemException {
        textIO.skipBlanks();
        ExpressionNode term; // The expression tree representing the term.
        term = termTree();
        textIO.skipBlanks();

        while (textIO.peek() == '+' || textIO.peek() == '-') {
            // Read the next term and combine it with the
            // previous terms into a bigger expression tree.
            char op = textIO.getAnyChar();
            ExpressionNode nextTerm = termTree();
            term = ExpressionNodeFactory.getExpNode(Operator.getByDescription(String.valueOf(op)), term, nextTerm);
            // term = new
            // BinOpNode(Operator.getByDescription(String.valueOf(op)), term,
            // nextTerm);
            textIO.skipBlanks();
        }
        return term;
    } // end termValue()

    /**
     * Reads a term from the current line of input and builds an expression tree
     * that represents the expression.
     * 
     * @return an ExpNode which is a pointer to the root node of the expression
     *         tree
     * @throws OpenClinicaSystemException
     *             if a syntax error is found in the input
     */
    private ExpressionNode termTree() throws OpenClinicaSystemException {
        textIO.skipBlanks();
        ExpressionNode term; // The expression tree representing the term.
        term = factorTree();
        textIO.skipBlanks();

        while (textIO.peek() == '*' || textIO.peek() == '/') {
            // Read the next factor, and combine it with the
            // previous factors into a bigger expression tree.
            char op = textIO.getAnyChar();
            ExpressionNode nextFactor = factorTree();
            term = ExpressionNodeFactory.getExpNode(Operator.getByDescription(String.valueOf(op)), term, nextFactor);
            // term = new
            // BinOpNode(Operator.getByDescription(String.valueOf(op)), term,
            // nextFactor);
            textIO.skipBlanks();
        }
        return term;
    } // end termValue()

    /**
     * Reads a factor from the current line of input and builds an expression
     * tree that represents the expression.
     * 
     * @return an ExpNode which is a pointer to the root node of the expression
     *         tree
     * @throws OpenClinicaSystemException
     *             if a syntax error is found in the input
     */
    private ExpressionNode factorTree() throws OpenClinicaSystemException {
        textIO.skipBlanks();
        char ch = textIO.peek();
        logger.info("TheChar is : " + ch);
        if (Character.isDigit(ch)) {
            String dateOrNum = textIO.getDate();
            if (dateOrNum == null) {
                dateOrNum = String.valueOf(textIO.getDouble());

            }
            logger.info("TheNum is : " + dateOrNum);
            return new ConstantNode(dateOrNum);
        } else if (ch == '(') {
            // The factor is an expression in parentheses.
            // Return a tree representing that expression.
            textIO.getAnyChar(); // Read the "("
            ExpressionNode exp = expressionTree();
            textIO.skipBlanks();
            if (textIO.peek() != ')')
                throw new OpenClinicaSystemException("Missing right parenthesis.");
            textIO.getAnyChar(); // Read the ")"
            return exp;
        } else if (String.valueOf(ch).matches("\\w+")) {
            String k = textIO.getWord();
            logger.info("TheWord 1 is : " + k);
            return new OpenClinicaVariableNode(k, expressionWrapper);
        } else if (String.valueOf(ch).matches("\"")) {
            String k = textIO.getDoubleQuoteWord();
            logger.info("TheWord 2 is : " + k);
            return new ConstantNode(k);
        } else if (ch == '\n')
            throw new OpenClinicaSystemException("End-of-line encountered in the middle of an expression.");
        else if (ch == ')')
            throw new OpenClinicaSystemException("Extra right parenthesis.");
        else if (ch == '+' || ch == '-' || ch == '*' || ch == '/')
            throw new OpenClinicaSystemException("Misplaced operator.");
        else
            throw new OpenClinicaSystemException("Unexpected character \"" + ch + "\" encountered.");
    } // end factorTree()

    /**
     * @return the textIO
     */
    public TextIO getTextIO() {
        return textIO;
    }

    /**
     * @param textIO
     *            the textIO to set
     */
    public void setTextIO(TextIO textIO) {
        this.textIO = textIO;
    }

    /*
     * public static void main(String[] args) {
     * 
     * SimpleParser4 smp4 = new SimpleParser4(); // textIO.putln("\n\nEnter an
     * expression, or press return to end."); // textIO.put("\n? ");
     * smp4.getTextIO().fillBuffer(" ((2 +2 * 4+2 *2 gte 15) or false or false
     * or 3+4 * 2 lt 10) and yellow eq \"yellow\" "); //
     * smp4.getTextIO().fillBuffer(" \"yellow\" eq yellow ");
     * smp4.getTextIO().skipBlanks();
     * 
     * try { ExpNode exp = smp4.expressionTree(); logger.info("\nValue 1 is " +
     * exp.value()); logger.info("\nOrder of postfix evaluation is:\n");
     * exp.printStackCommands(); } catch (OpenClinicaSystemException e) {
     * logger.info("\n*** Error in input: " + e.getMessage()); logger.info("***
     * Discarding input: " + smp4.getTextIO().getln()); } } // end main()
     */

}
