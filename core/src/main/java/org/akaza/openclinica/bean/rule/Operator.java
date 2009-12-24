package org.akaza.openclinica.bean.rule;

import java.util.HashMap;

/*
 * Use this enum as operator holder
 * @author Krikor Krumlian
 *
 */

public enum Operator {

    EQUAL(1), NOTEQUAL(2), GREATER(3), GREATERorEQUAL(4), LESS(5), LESSorEQUAL(6);

    private int code;

    Operator(int code) {
        this(code, null);
    }

    Operator(int code, String longName) {
        this.code = code;
    }

    public static Operator getByName(String name) {
        HashMap<String, Operator> operators = new HashMap<String, Operator>();
        for (Operator operator : Operator.values()) {
            operators.put(operator.name(), operator);
        }
        return operators.get(name);
    }

    public static Operator getByCode(int code) {
        HashMap<Integer, Operator> operators = new HashMap<Integer, Operator>();
        for (Operator operator : Operator.values()) {
            operators.put(operator.getCode(), operator);
        }
        return operators.get(Integer.valueOf(code));
    }

    public Integer getCode() {
        return code;
    }

}
