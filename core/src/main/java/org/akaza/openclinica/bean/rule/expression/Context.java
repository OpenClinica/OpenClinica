/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.bean.rule.expression;

import java.util.HashMap;

/*
 * @Author Krikor Krumlian
 */
public enum Context {

    OC_RULES_V1(1, "OpenClinica rules v1.0");

    private int code;

    /*
     * Default Constructor
     */
    Context() {

    }

    Context(int code) {
        this(code, null);
    }

    Context(int code, String longName) {
        this.code = code;
    }

    public Context getByContextName(String name) {
        HashMap<String, Context> operators = new HashMap<String, Context>();
        for (Context operator : Context.values()) {
            operators.put(operator.name(), operator);
        }
        return operators.get(name);
    }

    public static Context getByName(String name) {
        HashMap<String, Context> operators = new HashMap<String, Context>();
        for (Context operator : Context.values()) {
            operators.put(operator.name(), operator);
        }
        return operators.get(name);
    }

    public static Context getByCode(int code) {
        HashMap<Integer, Context> operators = new HashMap<Integer, Context>();
        for (Context operator : Context.values()) {
            operators.put(operator.getCode(), operator);
        }
        return operators.get(Integer.valueOf(code));
    }

    public Integer getCode() {
        return code;
    }

}
