package org.akaza.openclinica.core;

import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.encoding.PasswordEncoder;

public class OpenClinicaPasswordEncoder implements PasswordEncoder {

    PasswordEncoder currentPasswordEncoder;
    PasswordEncoder oldPasswordEncoder;

    public OpenClinicaPasswordEncoder() {
    }

    public String encodePassword(String rawPass, Object salt) throws DataAccessException {
        return currentPasswordEncoder.encodePassword(rawPass, salt);
    }

    public boolean isPasswordValid(String encPass, String rawPass, Object salt) throws DataAccessException {

        boolean result = false;
        if (currentPasswordEncoder.isPasswordValid(encPass, rawPass, salt) || oldPasswordEncoder.isPasswordValid(encPass, rawPass, salt)) {
            result = true;
        }
        return result;
    }

    public PasswordEncoder getCurrentPasswordEncoder() {
        return currentPasswordEncoder;
    }

    public void setCurrentPasswordEncoder(PasswordEncoder currentPasswordEncoder) {
        this.currentPasswordEncoder = currentPasswordEncoder;
    }

    public PasswordEncoder getOldPasswordEncoder() {
        return oldPasswordEncoder;
    }

    public void setOldPasswordEncoder(PasswordEncoder oldPasswordEncoder) {
        this.oldPasswordEncoder = oldPasswordEncoder;
    }

}
