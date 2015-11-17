package org.akaza.openclinica.domain.rule.action;

import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.enumsupport.CodedEnum;

import java.util.HashMap;

/*
 * Use this enum as operator holder
 * @author Krikor Krumlian
 *
 */

public enum ActionType implements CodedEnum {

    FILE_DISCREPANCY_NOTE(1, "DiscrepancyNoteAction"), EMAIL(2, "EmailAction"), SHOW(3, "ShowAction"), INSERT(4, "InsertAction"), HIDE(5, "HideAction"),EVENT(6,"EventAction"),NOTIFICATION(7,"NotificationAction"),RANDOMIZE(8,"RandomizeAction");


    private int code;
    private String description;

    ActionType(int code) {
        this(code, null);
    }

    ActionType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static Status getByName(String name) {
        return Status.valueOf(Status.class, name);
    }

    public static ActionType getByCode(Integer code) {
        HashMap<Integer, ActionType> enumObjects = new HashMap<Integer, ActionType>();
        for (ActionType theEnum : ActionType.values()) {
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

    public static ActionType getByDescription(String description) {
        for (ActionType theEnum : ActionType.values()) {
            if(theEnum.getDescription().equals(description)) {
                return theEnum;
            }
        }
        return null;
    }
}
