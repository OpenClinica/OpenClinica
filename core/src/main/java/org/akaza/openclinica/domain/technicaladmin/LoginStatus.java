package org.akaza.openclinica.domain.technicaladmin;

import org.akaza.openclinica.domain.enumsupport.CodedEnum;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;

import java.util.HashMap;
import java.util.ResourceBundle;

/*
 * Use this enum as login status holder
 * @author Krikor Krumlian
 *
 */

public enum LoginStatus implements CodedEnum {

    SUCCESSFUL_LOGIN(1, "successful_login"), FAILED_LOGIN(2, "failed_login"), FAILED_LOGIN_LOCKED(3, "failed_login_locked"), SUCCESSFUL_LOGOUT(4,
            "successful_logout"),ACCESS_CODE_VIEWED(5,"access_code_viewed");

    private int code;
    private String description;

    LoginStatus(int code) {
        this(code, null);
    }

    LoginStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public String toString() {
        ResourceBundle resterm = ResourceBundleProvider.getTermsBundle();
        return resterm.getString(getDescription());
    }

    public static LoginStatus getByName(String name) {
        return LoginStatus.valueOf(LoginStatus.class, name);
    }

    public static LoginStatus getByCode(Integer code) {
        HashMap<Integer, LoginStatus> enumObjects = new HashMap<Integer, LoginStatus>();
        for (LoginStatus theEnum : LoginStatus.values()) {
            enumObjects.put(theEnum.getCode(), theEnum);
        }
        return enumObjects.get(Integer.valueOf(code));
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

}
