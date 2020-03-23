package org.akaza.openclinica.domain.enumsupport;

import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;

import java.util.HashMap;
import java.util.ResourceBundle;

/**
 * The SdvStatus enumeration.
 */
public enum EventDefinitionCrfStatusEnum {
    AVAILABLE, ARCHIVED, AUTO_ARCHIVED;

    public String getDisplayValue() {
        ResourceBundle resterm = ResourceBundleProvider.getTermsBundle();
        return resterm.getString(this.toString());
    }

    public static EventDefinitionCrfStatusEnum getByI18nDescription(String description) {
        HashMap<String, EventDefinitionCrfStatusEnum> mapObject = new HashMap<String, EventDefinitionCrfStatusEnum>();
        for (EventDefinitionCrfStatusEnum theEnum : EventDefinitionCrfStatusEnum.values()) {
            mapObject.put(theEnum.getDisplayValue(), theEnum);
        }
        return mapObject.get(description);
    }
}
