package org.akaza.openclinica.logic.rulerunner;

import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.enumsupport.CodedEnum;

import java.util.HashMap;

public enum ExecutionMode implements CodedEnum {

    DRY_RUN(1, "Dry Run"), SAVE(2, "Save");

    private int code;
    private String description;

    ExecutionMode(int code) {
        this(code, null);
    }

    ExecutionMode(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static Status getByName(String name) {
        return Status.valueOf(Status.class, name);
    }

    public static ExecutionMode getByCode(Integer code) {
        HashMap<Integer, ExecutionMode> enumObjects = new HashMap<Integer, ExecutionMode>();
        for (ExecutionMode theEnum : ExecutionMode.values()) {
            enumObjects.put(theEnum.getCode(), theEnum);
        }
        return enumObjects.get(Integer.valueOf(code));
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

}
