package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.technicaladmin.ConfigurationBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class PasswordRequirementsDao {
	public static final String
	    PWD_CHARS_MIN = "pwd.chars.min",
	    PWD_CHARS_MAX = "pwd.chars.max",
	    PWD_CHARS_SPECIALS = "pwd.chars.specials",
	    PWD_CHARS_DIGITS = "pwd.chars.digits",
	    PWD_CHARS_CASE_UPPER = "pwd.chars.case.upper",
	    PWD_CHARS_CASE_LOWER = "pwd.chars.case.lower",
	    PWD_CHANGE_REQUIRED = "pwd.change.required",
	    PWD_EXPIRATION_DAYS = "pwd.expiration.days",

	    SPECIALS = "!@#$%&*()";

	private final Logger logger = LoggerFactory.getLogger(getClass().getName());
	private final ConfigurationDao configurationDao;

    private final List<String>
		boolConfigKeys = asList(
				PWD_CHARS_CASE_LOWER,
				PWD_CHARS_CASE_UPPER,
				PWD_CHARS_DIGITS,
				PWD_CHARS_SPECIALS),
		intConfigKeys  = asList(
				PWD_CHARS_MIN,
				PWD_CHARS_MAX,
				PWD_EXPIRATION_DAYS,
				PWD_CHANGE_REQUIRED); // PWD_CHANGE_REQUIRED is in the 'int' list for\backwards compatibility reasons

	public PasswordRequirementsDao(ConfigurationDao configurationDao) {
		this.configurationDao = configurationDao;
	}

	public Map<String,Object> configs() {
		HashMap<String,Object> map = new HashMap<String,Object>();

		List<ConfigurationBean> beans = this.configurationDao.findAll();
		for (ConfigurationBean bean: beans) {
			String key = bean.getKey(), value = bean.getValue();
			if (boolConfigKeys.contains(key)) {
				map.put(key, Boolean.valueOf(value));

			} else if (intConfigKeys.contains(key)) {
				try {
					map.put(key, Integer.valueOf(value));
				} catch (NumberFormatException ex) {
					logger.warn("Invalid configuration key: " + key + "."
							+ " Should be an integer, but is: " + value, ex);
				}
			}
		}
		return map;
	}

	public List<String> boolConfigKeys() {
		return boolConfigKeys;
	}

	public List<String> intConfigKeys() {
		return intConfigKeys;
	}

	public void setHasLower(boolean hasLower) {
		setValue(PWD_CHARS_CASE_LOWER, hasLower);
	}

	public void setHasUpper(boolean hasUpper) {
		setValue(PWD_CHARS_CASE_UPPER, hasUpper);
	}

	public void setHasDigits(boolean hasDigits) {
		setValue(PWD_CHARS_DIGITS, hasDigits);
	}

	public void setHasSpecials(boolean hasSpecials) {
		setValue(PWD_CHARS_SPECIALS, hasSpecials);
	}

	public void setMinLength(int minLen) {
        setValue(PWD_CHARS_MIN, minLen);
	}

	public void setMaxLength(int maxLen) {
        setValue(PWD_CHARS_MAX, maxLen);
	}
	public void setExpirationDays(int expirationDays) {
	    setValue(PWD_EXPIRATION_DAYS, expirationDays);
	}

	public void setChangeRequired(int changeRequired) {
        setValue(PWD_CHANGE_REQUIRED, changeRequired);
    }
	public boolean hasLower() {
		return getBoolProperty(PWD_CHARS_CASE_LOWER);
	}
	public boolean hasUpper() {
		return getBoolProperty(PWD_CHARS_CASE_UPPER);
	}
	public boolean hasDigits() {
		return getBoolProperty(PWD_CHARS_DIGITS);
	}
	public boolean hasSpecials() {
		return getBoolProperty(PWD_CHARS_SPECIALS);
	}
	public boolean changeRequired() {
	    return getBoolProperty(PWD_CHANGE_REQUIRED);
	}
	public int minLength() {
		return getIntProperty(PWD_CHARS_MIN);
	}
	public int maxLength() {
		return getIntProperty(PWD_CHARS_MAX);
	}
	public int expirationDays() {
	    return getIntProperty(PWD_EXPIRATION_DAYS);
	}

	private int getIntProperty(String key) {
		ConfigurationBean bean = this.configurationDao.findByKey(key);
		return Integer.parseInt(bean.getValue());
	}

	private boolean getBoolProperty(String key) {
		ConfigurationBean bean = this.configurationDao.findByKey(key);
		return Boolean.parseBoolean(bean.getValue());
	}

	private void setValue(String key, boolean value) {
		ConfigurationBean bean = this.configurationDao.findByKey(key);
		bean.setValue(Boolean.toString(value));
		this.configurationDao.saveOrUpdate(bean);
	}

	private void setValue(String key, int value) {
		ConfigurationBean bean = this.configurationDao.findByKey(key);
		bean.setValue(Integer.toString(value));
		this.configurationDao.saveOrUpdate(bean);
	}
}
