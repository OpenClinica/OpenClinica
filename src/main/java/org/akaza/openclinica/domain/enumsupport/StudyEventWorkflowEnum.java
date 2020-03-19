package org.akaza.openclinica.domain.enumsupport;

import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

/**
 * The StudyEventWorkflowEnum enumeration.
 */
public enum StudyEventWorkflowEnum {
    NOT_SCHEDULED, SCHEDULED, DATA_ENTRY_STARTED, COMPLETED,STOPPED,SKIPPED,SIGNED;
    String displayValue;

    public String getDisplayValue() {
        ResourceBundle resterm = ResourceBundleProvider.getTermsBundle();
        return resterm.getString(this.toString().toLowerCase());
    }

    public static StudyEventWorkflowEnum getByI18nDescription(String description) {
        HashMap<String, StudyEventWorkflowEnum> mapObject = new HashMap<String, StudyEventWorkflowEnum>();
        for (StudyEventWorkflowEnum theEnum : StudyEventWorkflowEnum.values()) {
            mapObject.put(theEnum.getDisplayValue(), theEnum);
        }
        return mapObject.get(description);
    }


}
