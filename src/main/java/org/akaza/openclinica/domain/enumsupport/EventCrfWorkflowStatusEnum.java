package org.akaza.openclinica.domain.enumsupport;

import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;

import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * The EventCrfWorkflowEnum enumeration.
 */
public enum EventCrfWorkflowStatusEnum {
    NOT_STARTED ,INITIAL_DATA_ENTRY, COMPLETED ,LOCKED;

    public String getDisplayValue() {
        ResourceBundle resterm = ResourceBundleProvider.getTermsBundle();
        return resterm.getString(this.toString().equals(COMPLETED.toString())? "DATA_ENTRY_COMPLETE".toLowerCase():this.toString().toLowerCase());
    }

    public String getEnglishDisplayValue() {
        ResourceBundle resterm = ResourceBundleProvider.getTermsBundle(Locale.ENGLISH);
        return resterm.getString(this.toString().equals(COMPLETED.toString())? "DATA_ENTRY_COMPLETE".toLowerCase():this.toString().toLowerCase());
    }

    public static EventCrfWorkflowStatusEnum getByI18nDescription(String description) {
        HashMap<String, EventCrfWorkflowStatusEnum> mapObject = new HashMap<String, EventCrfWorkflowStatusEnum>();
        for (EventCrfWorkflowStatusEnum theEnum : EventCrfWorkflowStatusEnum.values()) {
            mapObject.put(theEnum.getDisplayValue(), theEnum);
        }
        return mapObject.get(description);
    }

    public static EventCrfWorkflowStatusEnum getByEnglishDescription(String description) {
        HashMap<String, EventCrfWorkflowStatusEnum> mapObject = new HashMap<String, EventCrfWorkflowStatusEnum>();
        for (EventCrfWorkflowStatusEnum theEnum : EventCrfWorkflowStatusEnum.values()) {
            mapObject.put(theEnum.getEnglishDisplayValue(), theEnum);
        }
        mapObject.put("complete", EventCrfWorkflowStatusEnum.COMPLETED);
        return mapObject.get(description);
    }
}
