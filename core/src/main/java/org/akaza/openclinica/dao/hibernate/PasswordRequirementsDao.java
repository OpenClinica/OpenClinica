package org.akaza.openclinica.dao.hibernate;

import static java.util.Arrays.asList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.akaza.openclinica.domain.technicaladmin.ConfigurationBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PasswordRequirementsDao {
	public static final String
		PWD_HISTORY_SIZE = "pwd.history.size",
	    PWD_ALLOW_REUSE = "pwd.allow.reuse",
	    PWD_CHARS_MIN = "pwd.chars.min",
	    PWD_CHARS_MAX = "pwd.chars.max",
	    PWD_CHARS_SPECIALS = "pwd.chars.specials",
	    PWD_CHARS_DIGITS = "pwd.chars.digits",
	    PWD_CHARS_CASE_UPPER = "pwd.chars.case.upper",
	    PWD_CHARS_CASE_LOWER = "pwd.chars.case.lower",

	    SPECIALS = "!@#$%&*()";

	private final Logger logger = LoggerFactory.getLogger(getClass().getName());
	private ConfigurationDao configurationDao;

    private List<String> 
		boolConfigKeys = asList(
				PWD_CHARS_CASE_LOWER,
				PWD_CHARS_CASE_UPPER,
				PWD_CHARS_DIGITS,
				PWD_CHARS_SPECIALS,
				PWD_ALLOW_REUSE),
		intConfigKeys  = asList(
				PWD_CHARS_MIN,
				PWD_CHARS_MAX,
				PWD_HISTORY_SIZE);
	
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

	public void setAllowReuse(boolean allowReuse) {
        setValue(PWD_ALLOW_REUSE, allowReuse);
	}

	public void setMinLength(int minLen) {
        setValue(PWD_CHARS_MIN, minLen);
	}

	public void setMaxLength(int maxLen) {
        setValue(PWD_CHARS_MAX, maxLen);
	}

	/**
	 * How many old passwords the user cannot reuse 
	 */
	public void setHistorySize(int size) {
		setValue(PWD_HISTORY_SIZE, size);
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
	public boolean allowReuse() {
		return getBoolProperty(PWD_ALLOW_REUSE);
	}
	/**
	 * How many old passwords the user cannot reuse 
	 */
	public int historySize() {
		return getIntProperty(PWD_HISTORY_SIZE);
	}
	public int minLength() {
		return getIntProperty(PWD_CHARS_MIN);
	}
	public int maxLength() {
		return getIntProperty(PWD_CHARS_MAX);
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
