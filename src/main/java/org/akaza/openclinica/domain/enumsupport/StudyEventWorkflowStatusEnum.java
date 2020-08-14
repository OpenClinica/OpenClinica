package org.akaza.openclinica.domain.enumsupport;

import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;

import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * The StudyEventWorkflowEnum enumeration.
 */
public enum StudyEventWorkflowStatusEnum {
    NOT_SCHEDULED, SCHEDULED, DATA_ENTRY_STARTED, COMPLETED, STOPPED, SKIPPED;
    String displayValue;

    public String getDisplayValue() {
        ResourceBundle resterm = ResourceBundleProvider.getTermsBundle();
        return resterm.getString(this.toString().toLowerCase());
    }

    public String getEnglishDisplayValue() {
        ResourceBundle resterm = ResourceBundleProvider.getTermsBundle(Locale.ENGLISH);
        return resterm.getString(this.toString().toLowerCase());
    }

    public static StudyEventWorkflowStatusEnum getByI18nDescription(String description) {
        HashMap<String, StudyEventWorkflowStatusEnum> mapObject = new HashMap<String, StudyEventWorkflowStatusEnum>();
        for (StudyEventWorkflowStatusEnum theEnum : StudyEventWorkflowStatusEnum.values()) {
            mapObject.put(theEnum.getDisplayValue(), theEnum);
        }
        return mapObject.get(description);
    }

    public static StudyEventWorkflowStatusEnum getByEnglishDescription(String description) {
        HashMap<String, StudyEventWorkflowStatusEnum> mapObject = new HashMap<String, StudyEventWorkflowStatusEnum>();
        for (StudyEventWorkflowStatusEnum theEnum : StudyEventWorkflowStatusEnum.values()) {
            mapObject.put(theEnum.getEnglishDisplayValue(), theEnum);
        }
        return mapObject.get(description);
    }

}
