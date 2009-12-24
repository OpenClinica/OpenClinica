/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.logic.expressionTree;

import java.util.HashMap;

/**
 * @author Krikor Krumlian
 * 
 */
public enum Operator {
    EQUAL(1, "eq"), NOT_EQUAL(2, "ne"), OR(3, "or"), AND(4, "and"), GREATER_THAN(5, "gt"), GREATER_THAN_EQUAL(6, "gte"), LESS_THAN(7, "lt"), LESS_THAN_EQUAL(8,
            "lte"), PLUS(9, "+"), MINUS(10, "-"), MULTIPLY(11, "*"), POWER(12, "^"), DIVIDE(13, "/"), CONTAINS(14, "ct");

    private int code;
    private String description;

    Operator(int code) {
        this(code, null);
    }

    Operator(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static Operator getByDescription(String description) {
        HashMap<String, Operator> operators = new HashMap<String, Operator>();
        for (Operator operator : Operator.values()) {
            operators.put(operator.getDescription(), operator);
        }
        return operators.get(description.trim());
    }

    public String getDescription() {
        return description;
    }
}