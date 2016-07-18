/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.domain.rule.expression;

import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.logic.expressionTree.ExpressionNode;
import org.akaza.openclinica.logic.expressionTree.OpenClinicaExpressionParser;
import org.akaza.openclinica.service.rule.expression.ExpressionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * @author Krikor Krumlian
 * 
 */
public class OpenClinicaV1ExpressionProcessor implements ExpressionProcessor {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    ExpressionBean e;
    Pattern[] pattern;
    ExpressionNode node;
    OpenClinicaExpressionParser oep;
    DataSource ds;
    ExpressionService expressionService;
    ExpressionObjectWrapper expressionWrapper;
    ResourceBundle respage;

    public OpenClinicaV1ExpressionProcessor(ExpressionObjectWrapper expressionWrapper) {
        this.expressionWrapper = expressionWrapper;
        this.e = expressionWrapper.getExpressionBean();

    }

    public String isRuleAssignmentExpressionValid() {
        try {
            oep = new OpenClinicaExpressionParser(expressionWrapper);
            oep.parseAndTestEvaluateExpression(e.getValue());

            expressionService = new ExpressionService(expressionWrapper);
            if (expressionService.ruleSetExpressionChecker(e.getValue())) {
                return null;
            } else {
                MessageFormat mf = new MessageFormat("");
                String errorCode = "OCRERR_0024";
                mf.applyPattern(respage.getString(errorCode));
                Object[] arguments = {};
                return errorCode + " : " + mf.format(arguments);
            }
        } catch (OpenClinicaSystemException e) {
            MessageFormat mf = new MessageFormat("");
            mf.applyPattern(respage.getString(e.getErrorCode()));
            Object[] arguments = e.getErrorParams();
            return e.getErrorCode() + " : " + mf.format(arguments);
        }
    }

    public String isRuleExpressionValid() {
        try {
            oep = new OpenClinicaExpressionParser(expressionWrapper);
            String result = oep.parseAndTestEvaluateExpression(e.getValue());
            logger.debug("Test Result : " + result);
            return null;
        } catch (OpenClinicaSystemException e) {
            MessageFormat mf = new MessageFormat("");
            mf.applyPattern(respage.getString(e.getErrorCode()));
            Object[] arguments = e.getErrorParams();
            return e.getErrorCode() + " : " + mf.format(arguments);

        }
    }

    public String testEvaluateExpression() {
        try {
            oep = new OpenClinicaExpressionParser(expressionWrapper);
            String result = oep.parseAndTestEvaluateExpression(e.getValue());
            logger.debug("Test Result : " + result);
            return result;
        } catch (OpenClinicaSystemException e) {
            MessageFormat mf = new MessageFormat("");
            mf.applyPattern(respage.getString(e.getErrorCode()));
            Object[] arguments = e.getErrorParams();
            return "Fail - " + e.getErrorCode() + " : " + mf.format(arguments);
        }
    }

    public HashMap<String, String> testEvaluateExpression(HashMap<String, String> testValues) {
        try {
            oep = new OpenClinicaExpressionParser(expressionWrapper);
            HashMap<String, String> resultAndTestValues = oep.parseAndTestEvaluateExpression(e.getValue(), testValues);
            String returnedResult = resultAndTestValues.get("result");
            logger.debug("Test Result : " + returnedResult);
            resultAndTestValues.put("ruleValidation", "rule_valid");
            resultAndTestValues.put("ruleEvaluatesTo", returnedResult);

            return resultAndTestValues;
        } catch (OpenClinicaSystemException e) {
            MessageFormat mf = new MessageFormat("");
            mf.applyPattern(respage.getString(e.getErrorCode()));
            Object[] arguments = e.getErrorParams();
            testValues.put("ruleValidation", "rule_invalid");
            testValues.put("ruleValidationFailMessage", e.getErrorCode() + " : " + mf.format(arguments));
            testValues.put("ruleEvaluatesTo", "");
            return testValues;
        }
    }

    public boolean process() {
        return false;
    }

    public void setExpression(ExpressionBean e) {
        this.e = e;
    }

    public void setRespage(ResourceBundle respage) {
        this.respage = respage;
    }

}
