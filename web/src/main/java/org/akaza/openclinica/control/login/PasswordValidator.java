package org.akaza.openclinica.control.login;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.dao.hibernate.PasswordRequirementsDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;

/**
 *
 * @author Leonel Gayard
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 *
 */
public class PasswordValidator {
    private static boolean hasLowerCaseChars(String str) {
    	int len = str.length();
    	for (int i = 0; i < len; i++) {
    		if (Character.isLowerCase(str.charAt(i))) return true;
    	}
    	return false;
    }
    private static boolean hasUpperCaseChars(String str) {
    	int len = str.length();
    	for (int i = 0; i < len; i++) {
    		if (Character.isUpperCase(str.charAt(i))) return true;
    	}
    	return false;
    }
    private static boolean hasDigits(String str) {
    	int len = str.length();
    	for (int i = 0; i < len; i++) {
    		if (Character.isDigit(str.charAt(i))) return true;
    	}
    	return false;
    }
    private static boolean hasSpecialChars(String str) {
    	int len = str.length();
    	for (int i = 0; i < len; i++) {
    		if (PasswordRequirementsDao.SPECIALS.indexOf(str.charAt(i)) >= 0)
    			return true;
    	}
    	return false;
    }

    /**
     * Validates whether a new password meets the requirements set by the
     * administrator
     * @param passwordRequirementsDao
     * @param newPassword
     * @return list of strings with validation errors; empty list if password
     *  meets all validation requirements
     */

    public static List<String> validatePassword(
    		PasswordRequirementsDao passwordRequirementsDao,
    		UserAccountDAO userDao,
    		int userId,
    		String newPassword,
    		String newHash,
    		ResourceBundle resexception) {
    	ArrayList<String> errors = new ArrayList<String>();

    	UserAccountBean userBean = (UserAccountBean) userDao.findByPK(userId);
        if (userBean.getPasswd().equals(newHash)) {
            errors.add(resexception.getString("pwd_cannot_reuse"));
        }

    	int
    		minLen = passwordRequirementsDao.minLength(),
    		maxLen = passwordRequirementsDao.maxLength();

    	if (newPassword.length() == 0) {
    	    return new ArrayList<String>();
    	}

    	if (minLen > 0 && newPassword.length() < minLen) {
    		errors.add(resexception.getString("pwd_too_short") + " " + minLen + " " + resexception.getString("chars"));
    	}

    	if (maxLen > 0 && newPassword.length() > maxLen) {
    		errors.add(resexception.getString("pwd_too_long") + " " + maxLen + " " + resexception.getString("chars" ));
    	}
    	if (passwordRequirementsDao.hasLower() && !hasLowerCaseChars(newPassword)) {
    		errors.add(resexception.getString("pwd_needs_lower_case"));
    	}
    	if (passwordRequirementsDao.hasUpper() && !hasUpperCaseChars(newPassword)) {
    		errors.add(resexception.getString("pwd_needs_upper_case"));
    	}
    	if (passwordRequirementsDao.hasDigits() && !hasDigits(newPassword)) {
    		errors.add(resexception.getString("pwd_needs_digits"));
    	}
    	if (passwordRequirementsDao.hasSpecials() && !hasSpecialChars(newPassword)) {
    		errors.add(resexception.getString("pwd_needs_special_chars"));
    	}
    	return errors;
    }
}
