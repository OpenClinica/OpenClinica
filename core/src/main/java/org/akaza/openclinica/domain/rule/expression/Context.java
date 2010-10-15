/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.domain.rule.expression;

import org.akaza.openclinica.domain.enumsupport.CodedEnum;

import java.util.HashMap;

/*
 * @Author Krikor Krumlian
 */
public enum Context implements CodedEnum {

    OC_RULES_V1(1, "OpenClinica rules v1.0");

    private int code;
    private String description;

    // private static final ResourceBundle resterm = ResourceBundleProvider.getTermsBundle();

    /*
     * Default Constructor
     */
    Context() {

    }

    Context(int code) {
        this(code, null);
    }

    Context(int code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public String toString() {
        return this.name();
    }

    public static Context getByName(String name) {
        return Context.valueOf(Context.class, name);
    }

    /*
     * public Context getByContextName(String name) { HashMap<String, Context> operators = new HashMap<String, Context>(); for (Context operator :
     * Context.values()) { operators.put(operator.name(), operator); } return operators.get(name); }
     */

    public static Context getByCode(Integer code) {
        HashMap<Integer, Context> operators = new HashMap<Integer, Context>();
        for (Context operator : Context.values()) {
            operators.put(operator.getCode(), operator);
        }
        return operators.get(Integer.valueOf(code));
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

}
