/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.logic.expressionTree;

import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Krikor Krumlian
 * 
 */
public abstract class ExpressionNode {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    String value() throws OpenClinicaSystemException {
        return calculate();
    }

    /*
     * Use this method to test the expression mainly Data types plugging test
     * data wherever necessary. This will not only validate the syntax but also
     * test the validity of the expression itself.
     * 
     */
    String testValue() throws OpenClinicaSystemException {
        return testCalculate();
    }

    abstract String calculate() throws OpenClinicaSystemException;

    abstract String testCalculate() throws OpenClinicaSystemException;

    abstract void printStackCommands();
}