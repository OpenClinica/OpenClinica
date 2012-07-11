package org.akaza.openclinica.control.login;

import java.util.ArrayList;
import java.util.Set;

import org.akaza.openclinica.dao.hibernate.PasswordRequirementsDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;

public class PasswordValidator {
    private boolean hasLowerCaseChars(String str) {
    	int len = str.length();
    	for (int i = 0; i < len; i++) { 
    		if (Character.isLowerCase(str.charAt(i))) return true;
    	}
    	return false;
    }
    private boolean hasUpperCaseChars(String str) {
    	int len = str.length();
    	for (int i = 0; i < len; i++) { 
    		if (Character.isUpperCase(str.charAt(i))) return true;
    	}
    	return false;
    }
    private boolean hasDigits(String str) {
    	int len = str.length();
    	for (int i = 0; i < len; i++) { 
    		if (Character.isDigit(str.charAt(i))) return true;
    	}
    	return false;
    }
    private boolean hasSpecialChars(String str) {
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
    public ArrayList<String> validatePassword(
    		PasswordRequirementsDao passwordRequirementsDao,
    		UserAccountDAO userDao,
    		int userId,
    		String newPassword,
    		String newHash) {
    	ArrayList<String> errors = new ArrayList<String>();

    	if (!passwordRequirementsDao.allowReuse()) {
    		int historySize = passwordRequirementsDao.historySize();
    		Set<String> oldHashes = userDao.findOldPasswordHashes(userId, historySize);

    		if (oldHashes.contains(newHash)) {
    			errors.add("pwd_cannot_reuse");
    		}
    	}

    	int
    		minLen = passwordRequirementsDao.minLength(),
    		maxLen = passwordRequirementsDao.maxLength();
    	if (minLen >= 0 && newPassword.length() < minLen) {
    		errors.add("pwd_too_short");
    	}

    	if (maxLen >= 0 && newPassword.length() > maxLen) {
    		errors.add("pwd_too_long");
    	}
    	if (passwordRequirementsDao.hasLower() && !hasLowerCaseChars(newPassword)) {
    		errors.add("pwd_needs_lower_case");
    	}
    	if (passwordRequirementsDao.hasUpper() && !hasUpperCaseChars(newPassword)) {
    		errors.add("pwd_needs_upper_case");
    	}
    	if (passwordRequirementsDao.hasDigits() && !hasDigits(newPassword)) {
    		errors.add("pwd_needs_digits");
    	}
    	if (passwordRequirementsDao.hasSpecials() && !hasSpecialChars(newPassword)) {
    		errors.add("pwd_needs_special_chars");
    	}
    	return errors;
    }
}
