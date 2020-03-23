package org.akaza.openclinica.domain.enumsupport;

import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;

import java.util.HashMap;
import java.util.ResourceBundle;

/**
 * The SdvStatus enumeration.
 */
public enum StudyEventDefinitionStatusEnum {
    AVAILABLE, ARCHIVED;

    public String getDisplayValue() {
        ResourceBundle resterm = ResourceBundleProvider.getTermsBundle();
        return resterm.getString(this.toString());
    }

    public static StudyEventDefinitionStatusEnum getByI18nDescription(String description) {
        HashMap<String, StudyEventDefinitionStatusEnum> mapObject = new HashMap<String, StudyEventDefinitionStatusEnum>();
        for (StudyEventDefinitionStatusEnum theEnum : StudyEventDefinitionStatusEnum.values()) {
            mapObject.put(theEnum.getDisplayValue(), theEnum);
        }
        return mapObject.get(description);
    }
}
