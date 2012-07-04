package org.akaza.openclinica.dao.hibernate;

import static java.util.Arrays.asList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.akaza.openclinica.domain.technicaladmin.ConfigurationBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PasswordRequirementsDao {
	private final Logger logger = LoggerFactory.getLogger(getClass().getName());
	private ConfigurationDao configurationDao;

    private List<String> 
		boolConfigKeys = asList(
				"pwd.chars.case.lower",
				"pwd.chars.case.upper",
				"pwd.chars.digits",
				"pwd.chars.specials",
				"pwd.allow.reuse"),
		intConfigKeys  = asList(
				"pwd.chars.min",
				"pwd.chars.max",
				"pwd.history.size");
	
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
		setValue("pwd.chars.case.lower", hasLower);
	}

	public void setHasUpper(boolean hasUpper) {
		setValue("pwd.chars.case.upper", hasUpper);
	}

	public void setHasDigits(boolean hasDigits) {
		setValue("pwd.chars.digits", hasDigits);
	}

	public void setHasSpecials(boolean hasSpecials) {
		setValue("pwd.chars.specials", hasSpecials);
	}

	public void setAllowReuse(boolean allowReuse) {
        setValue("pwd.allow.reuse", allowReuse);
	}

	public void setMinLength(int minLen) {
        setValue("pwd.chars.min", minLen);
	}

	public void setMaxLength(int maxLen) {
        setValue("pwd.chars.max", maxLen);
	}

	public void setHistorySize(int size) {
		setValue("pwd.history.size", size);
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
