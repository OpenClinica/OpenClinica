package org.akaza.openclinica.domain.enumsupport;

import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;

import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * The SdvStatus enumeration.
 */
public enum SdvStatus {
    NOT_VERIFIED, VERIFIED, CHANGED_SINCE_VERIFIED;

    public String getDisplayValue() {
        ResourceBundle resterm = ResourceBundleProvider.getTermsBundle();
        return resterm.getString(this.toString());
    }

    public String getDisplayValueForNonSdvPage() {
        ResourceBundle resterm = ResourceBundleProvider.getTermsBundle();
        if(this == SdvStatus.NOT_VERIFIED)
            return resterm.getString(this.toString()+"_non_sdv_page");
        return resterm.getString(this.toString());
    }

    public String getEnglishDisplayValue() {
        ResourceBundle resterm = ResourceBundleProvider.getTermsBundle(Locale.ENGLISH);
        return resterm.getString(this.toString());
    }

    public static SdvStatus getByI18nDescription(String description) {
        HashMap<String, SdvStatus> sdvObjects = new HashMap<String, SdvStatus>();
        for (SdvStatus theEnum : SdvStatus.values()) {
            sdvObjects.put(theEnum.getDisplayValue(), theEnum);
        }
        return sdvObjects.get(description);
    }

    public static SdvStatus getBySdvStatusString(String sdvStatusString) {
        HashMap<String, SdvStatus> sdvObjects = new HashMap<String, SdvStatus>();
        for (SdvStatus theEnum : SdvStatus.values()) {
            sdvObjects.put(theEnum.toString(), theEnum);
        }
        return sdvObjects.get(sdvStatusString);
    }
}
