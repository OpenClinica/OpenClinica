/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.logic.expressionTree;

import java.util.TimeZone;

import org.akaza.openclinica.bean.core.ItemDataType;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.domain.rule.expression.ExpressionObjectWrapper;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.service.rule.expression.ExpressionService;
import org.joda.time.DateMidnight;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * @author Krikor Krumlian
 * 
 */
public class OpenClinicaVariableNode extends ExpressionNode {
    String number;
    ExpressionService expressionService;
    ExpressionObjectWrapper expressionWrapper;
    private final String STARTDATE =".STARTDATE";
    private final String STATUS =".STATUS";
    private final String REPEATING = ".*\\[(END|ALL|[1-9]\\d*)\\]$";
    private final String REPEATING_NOALL = ".*\\[(END|[1-9]\\d*)\\]$";

    OpenClinicaVariableNode(String val) {
        number = val;
        // validate();
    }

    OpenClinicaVariableNode(String val, ExpressionObjectWrapper expressionWrapper) {
        this.expressionWrapper = expressionWrapper;
        number = val;
        // validate();
    }

    OpenClinicaVariableNode(String val, ExpressionObjectWrapper expressionWrapper, OpenClinicaExpressionParser parser) {
        setExpressionParser(parser);
        this.expressionWrapper = expressionWrapper;
        number = val;
        // validate();
    }

    @Override
    String getNumber() {
        return number;

    }

    /**
     * 
     * getTestValues() returns a hashMap of user defined values
     * getResponseTestValues() is empty and will be filled with variables being processed
     * @param var the default test value
     * @return the Value
     */
    private String theTest(String var) {
        if (getTestValues() == null) {
            return var;
        } else if (getTestValues().get(number) == null) {
            getTestValues().put(number, var);
            getResponseTestValues().put(number, var);
            return var;
        } else {
            getResponseTestValues().put(number, getTestValues().get(number));
            return getTestValues().get(number);
        }

    }

    @Override
    String testCalculate() throws OpenClinicaSystemException {

    	validate();
        String variableValue = testCalculateVariable();
    	boolean isEventStatusParamExist = number.endsWith(STATUS);
    	boolean isEventStartDateParamExist = number.endsWith(STARTDATE) ;
    	boolean isEventStartDateAndStatusParamExist = (number.endsWith(STARTDATE) ||number.endsWith(STATUS));

        if (variableValue != null) {
            return variableValue;
        }
        ItemBean item = getExpressionService().getItemBeanFromExpression(number);
        String testString = "test";
        String testInt = "1";
        String testBoolean = "true";
        String testDate = "2008-01-01";
        String testPDate = "";
        
                
        if (item != null) {
            ItemDataType itemDataType = ItemDataType.get(item.getItemDataTypeId());
            switch (itemDataType.getId()) {
            case 1: {
                return theTest(testBoolean);
            }
            case 2: {
                return theTest(testBoolean);
            }
            case 3: {
                return theTest(testString);
            }
            case 4: {
                return theTest(testString);
            }
            case 5: {
                return theTest(testString);
            }
            case 6: {
                return theTest(testInt);
            }
            case 7: {
                return theTest(testInt);
            }
            case 8: {
                return theTest(testString);
            }
            case 9: {
                return theTest(testDate);
            }
            case 10: {
                return theTest(testPDate);
            }
            case 11: {
                return theTest(testString + ".txt");
            }
            default:
                throw new OpenClinicaSystemException("OCRERR_0011");
            }
        } else if(isEventStartDateAndStatusParamExist) {
           if(isEventStatusParamExist) return theTest(testString);
           else if(isEventStartDateParamExist) return theTest(testDate);
           else return null;
        } else {
            throw new OpenClinicaSystemException("OCRERR_0012", new String[] { number });
        } 
        
    }

    @Override
    Object calculate() throws OpenClinicaSystemException {
        // The value of the node is the number that it contains.
        // return number;
        validate();
        String variableValue = calculateVariable();
        if (variableValue != null) {
            return variableValue;
        }
        return getExpressionService().evaluateExpression(number);
    }

    void validate() throws OpenClinicaSystemException {
        // logger.info(number + " : " +
        // getExpressionService().checkSyntax(number));
        if (calculateVariable() != null) {

        
        // logger.info("e" + expressionWrapper.getRuleSet());
        }else if((expressionWrapper.getRuleSet() != null) && (getExpressionService().checkIfExpressionIsForScheduling(expressionWrapper.getRuleSet().getTarget().getValue())) && !number.endsWith(STARTDATE) && !number.endsWith(STATUS))  {
          System.out.println("the Target value is  " + expressionWrapper.getRuleSet().getTarget().getValue());        	
          System.out.println("the number is  " + number );        	
            throw new OpenClinicaSystemException("OCRERR_0046", new Object[] { number });
        
        }else if (!getExpressionService().ruleExpressionChecker(number)) {
            logger.info("Go down");
            throw new OpenClinicaSystemException("OCRERR_0013", new Object[] { number });
        }
        if (number.endsWith(STARTDATE) ||number.endsWith(STATUS)) 
        	validateEvent();
    }
    
    void validateEvent() throws OpenClinicaSystemException {
        StudyEventDefinitionBean studyEventDefinition = getExpressionService().getStudyEventDefinitionFromExpressionForEventScheduling(number);
        String studyEventOID = number.split("\\.")[0];
        String expressionContext = expressionWrapper.getExpressionContext();
        //Verify expression refers to a valid event.
        if (studyEventDefinition == null) throw new OpenClinicaSystemException("OCRERR_0034", new String[] { number });
        //Verify expression doesn't use repeating event notation if event is not repeating
        if (!studyEventDefinition.isRepeating() && studyEventOID.matches(REPEATING))
        	throw new OpenClinicaSystemException("OCRERR_0039", new String[] { number });
        //Verify only Target expressions use the 'ALL' repeating notation.
        if (expressionContext != null && !expressionContext.equals(ExpressionObjectWrapper.CONTEXT_TARGET) ) {
            if (studyEventOID.matches(REPEATING) && !studyEventOID.matches(REPEATING_NOALL)) 
                throw new OpenClinicaSystemException("OCRERR_0040", new String[] { number });
        }
    }


    private String calculateVariable() {
        if (number.equals("_CURRENT_DATE")) {
        	String ssZoneId= getExpressionService().getSSZoneId();
            String serverZoneId =TimeZone.getDefault().getID();
            return getExpressionService().getSSDate(ssZoneId,serverZoneId).get("ssDate");
        }
        return null;
    }

    private String testCalculateVariable() {
        if (number.equals("_CURRENT_DATE")) {
        	String ssZoneId= getExpressionService().getSSZoneId();
            String serverZoneId =TimeZone.getDefault().getID();
            return getExpressionService().getSSDate(ssZoneId,serverZoneId).get("ssDate");
        }
        return null;
    }

    @Override
    void printStackCommands() {
        // On a stack machine, just push the number onto the stack.
        logger.info("  Push " + number);
    }

    private ExpressionService getExpressionService() {
        expressionService = this.expressionService != null ? expressionService : new ExpressionService(expressionWrapper);
        return expressionService;
    }

}