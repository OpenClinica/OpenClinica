package org.akaza.openclinica.domain;

import org.akaza.openclinica.domain.enumsupport.CodedEnum;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;

import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

/*
 * @Author Krikor Krumlian
 */
public enum Status implements CodedEnum {

    INVALID(0, "invalid"),
    AVAILABLE(1, "available"),
    UNAVAILABLE(2, "unavailable"),
    PRIVATE(3, "private"),
    PENDING(4, "pending"),
    DELETED(5, "removed"),
    LOCKED(6, "locked"),
    AUTO_DELETED(7, "auto-removed"),
    SIGNED(8, "signed"),
    FROZEN(9, "frozen"),
    SOURCE_DATA_VERIFICATION(10, "source_data_verification"),
    RESET(11, "reset"),
    ARCHIVED(12, "archived");

    private int code;
    private String description;

    Status() {
    }

    Status(int code) {
        this(code, null);
    }

    Status(int code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public String toString() {
        ResourceBundle resterm = ResourceBundleProvider.getTermsBundle();
        return resterm.getString(getDescription());
    }

    public static Status getByName(String name) {
        return Status.valueOf(Status.class, name);
    }

    public static Status getByCode(Integer code) {
        HashMap<Integer, Status> enumObjects = new HashMap<Integer, Status>();
        for (Status theEnum : Status.values()) {
            enumObjects.put(theEnum.getCode(), theEnum);
        }
        return enumObjects.get(Integer.valueOf(code));
    }
    
    public static Status getByI18nDescription(String i18nDescription, Locale locale) {
        for (Status theEnum : Status.values()) {
            if(i18nDescription.equals(theEnum.getI18nDescription(locale))) {
                return theEnum;
            }
        }
        return null;
    }
    
    public String getI18nDescription(Locale locale) {
        if (!"".equals(this.description)) {
            ResourceBundle resterm = ResourceBundleProvider.getTermsBundle(locale);
            String des = resterm.getString(this.description);
            if(des != null) {
                return des.trim();
            }  else {
                return "";
            }
        } else {
            return this.description;
        }
    }

    /**
     * A wrapper for name() method to be used in JSPs
     */
    public String getName() {
        return this.name();
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

}
