package org.akaza.openclinica.domain;

import org.akaza.openclinica.domain.enumsupport.CodedEnum;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;

import java.util.HashMap;
import java.util.ResourceBundle;

/**
 * An enum that represents an Event definition's  requirement for SourceDataVerification.
 */
public enum SourceDataVerification implements CodedEnum {

    AllREQUIRED(1, "100percent_required"), PARTIALREQUIRED(2, "partial_required"), NOTREQUIRED(3, "not_required"), NOTAPPLICABLE(4, "not_applicable");

    private int code;
    private String description;

    SourceDataVerification() {
    }

    SourceDataVerification(int code) {
        this(code, null);
    }

    SourceDataVerification(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        ResourceBundle resterm = ResourceBundleProvider.getTermsBundle();
        return resterm.getString(getDescription());
    }

    public static SourceDataVerification getByName(String name) {
        return SourceDataVerification.valueOf(SourceDataVerification.class, name);
    }

    public static SourceDataVerification getByCode(Integer code) {
        HashMap<Integer, SourceDataVerification> enumObjects = new HashMap<Integer, SourceDataVerification>();
        for (SourceDataVerification theEnum : SourceDataVerification.values()) {
            enumObjects.put(theEnum.getCode(), theEnum);
        }
        return enumObjects.get(Integer.valueOf(code));
    }

    public static SourceDataVerification getByDescription(String description) {
        HashMap<String, SourceDataVerification> sdvObjects = new HashMap<String, SourceDataVerification>();
        for (SourceDataVerification theEnum : SourceDataVerification.values()) {
            sdvObjects.put(theEnum.getDescription(), theEnum);
        }
        return sdvObjects.get(description);
    }

    public static SourceDataVerification getByI18nDescription(String description) {
        HashMap<String, SourceDataVerification> sdvObjects = new HashMap<String, SourceDataVerification>();
        for (SourceDataVerification theEnum : SourceDataVerification.values()) {
            sdvObjects.put(theEnum.toString(), theEnum);
        }
        return sdvObjects.get(description);
    }

    /**
     * A wrapper for name() method to be used in JSPs
     * @return A String, the name of the requirement.
     */
    public String getName() {
        return this.name();
    }
}
