package org.akaza.openclinica.domain.enumsupport;

import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;

import java.util.HashMap;
import java.util.ResourceBundle;

/**
 * The EventCrfWorkflowEnum enumeration.
 */
public enum EventCrfWorkflowEnum {
    NOT_STARTED ,INITIAL_DATA_ENTRY, COMPLETED;

    public String getDisplayValue() {
        ResourceBundle resterm = ResourceBundleProvider.getTermsBundle();
        return resterm.getString(this.toString().toLowerCase());
    }

    public static EventCrfWorkflowEnum getByI18nDescription(String description) {
        HashMap<String, EventCrfWorkflowEnum> mapObject = new HashMap<String, EventCrfWorkflowEnum>();
        for (EventCrfWorkflowEnum theEnum : EventCrfWorkflowEnum.values()) {
            mapObject.put(theEnum.getDisplayValue(), theEnum);
        }
        return mapObject.get(description);
    }
}
