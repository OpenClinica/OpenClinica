/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.bean.rule.expression;

import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.logic.expressionTree.ExpressionNode;
import org.akaza.openclinica.logic.expressionTree.OpenClinicaExpressionParser;
import org.akaza.openclinica.service.rule.expression.ExpressionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
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
            expressionService = null;// new ExpressionService(expressionWrapper);
            if (expressionService.ruleSetExpressionChecker(e.getValue())) {
                return null;
            } else {
                return "Expression Syntax InValid";
            }
        } catch (OpenClinicaSystemException e) {
            return e.getMessage();
        }
    }

    public String isRuleExpressionValid() {
        try {
            oep = null; // new OpenClinicaExpressionParser(expressionWrapper);
            String result = oep.parseAndTestEvaluateExpression(e.getValue());
            logger.info("Test Result : " + result);
            return null;
        } catch (OpenClinicaSystemException e) {
            return e.getMessage();
        }
    }

    public String testEvaluateExpression() {
        try {
            oep = null; // new OpenClinicaExpressionParser(expressionWrapper);
            String result = oep.parseAndTestEvaluateExpression(e.getValue());
            logger.info("Test Result : " + result);
            return "Pass : " + result;
        } catch (OpenClinicaSystemException e) {
            return "Fail : " + e.getMessage();
        }
    }

    public HashMap<String, String> testEvaluateExpression(HashMap<String, String> testValues) {
        try {
            oep = null; // new OpenClinicaExpressionParser(expressionWrapper);
            HashMap<String, String> resultAndTestValues = oep.parseAndTestEvaluateExpression(e.getValue(), testValues);
            String returnedResult = resultAndTestValues.get("result");
            logger.info("Test Result : " + returnedResult);
            resultAndTestValues.put("result", "Pass : " + returnedResult);

            return resultAndTestValues;
        } catch (OpenClinicaSystemException e) {
            testValues.put("result", "Fail : " + e.getMessage());
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
