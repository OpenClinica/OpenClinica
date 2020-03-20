package org.akaza.openclinica.domain.enumsupport;

import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;

import java.util.HashMap;
import java.util.ResourceBundle;

/**
 * The SdvStatus enumeration.
 */
public enum StudyStatusEnum {
    AVAILABLE, DESIGN, FROZEN,LOCKED;

    public String getDisplayValue() {
        ResourceBundle resterm = ResourceBundleProvider.getTermsBundle();
        return resterm.getString(this.toString());
    }

    public static StudyStatusEnum getByI18nDescription(String description) {
        HashMap<String, StudyStatusEnum> mapObject = new HashMap<String, StudyStatusEnum>();
        for (StudyStatusEnum theEnum : StudyStatusEnum.values()) {
            mapObject.put(theEnum.getDisplayValue(), theEnum);
        }
        return mapObject.get(description);
    }
}
