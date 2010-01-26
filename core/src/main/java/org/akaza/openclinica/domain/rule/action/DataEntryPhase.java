package org.akaza.openclinica.domain.rule.action;

import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.enumsupport.CodedEnum;

import java.util.HashMap;

public enum DataEntryPhase implements CodedEnum {

    ADMIN_EDITING(1, "Administrative Editing"), INITIAL_DATA_ENTRY(2, "Initial Data Entry"), DOUBLE_DATA_ENTRY(3, "Double Data Entry"), IMPORT(4, "Import");

    private int code;
    private String description;

    DataEntryPhase(int code) {
        this(code, null);
    }

    DataEntryPhase(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static Status getByName(String name) {
        return Status.valueOf(Status.class, name);
    }

    public static DataEntryPhase getByCode(Integer code) {
        HashMap<Integer, DataEntryPhase> enumObjects = new HashMap<Integer, DataEntryPhase>();
        for (DataEntryPhase theEnum : DataEntryPhase.values()) {
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
