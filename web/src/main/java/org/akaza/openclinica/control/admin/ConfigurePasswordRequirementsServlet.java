/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 * copyright 2003-2009 Akaza Research
 */
package org.akaza.openclinica.control.admin;

import java.util.HashMap;

import javax.servlet.ServletException;

import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.dao.hibernate.ConfigurationDao;
import org.akaza.openclinica.dao.hibernate.PasswordRequirementsDao;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Krikor Krumlian
 */
public class ConfigurePasswordRequirementsServlet extends SecureController {
	final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private static final long serialVersionUID = 2729725318725545575L;

    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        if (!ub.isSysAdmin()) {
            addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
            throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("you_may_not_perform_administrative_functions"), "1");
        }
        return;
    }
    
    @Override
    public void init() throws ServletException {
    	super.init();
    	ConfigurationDao configurationDao = SpringServletAccess
    			.getApplicationContext(context)
    			.getBean(ConfigurationDao.class);
    	this.passwordRequirementsDao = new PasswordRequirementsDao(configurationDao);
    }

    private PasswordRequirementsDao passwordRequirementsDao;

    @Override
    protected void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);

        if (!fp.isSubmitted()) {
        	setPresetValues(new HashMap<String,Object>(passwordRequirementsDao.configs()));
            forwardPage(Page.CONFIGURATION_PASSWORD_REQUIREMENTS);

        } else {
        	Validator v = new Validator(request);
        	for (String key: passwordRequirementsDao.intConfigKeys()) {
        		v.addValidation(key, Validator.IS_AN_INTEGER);
        	}

        	HashMap<?,?> errors = v.validate();
        	if (errors.isEmpty()) {
				this.passwordRequirementsDao.setHasLower(   Boolean.valueOf(fp.getString("pwd.chars.case.lower")));
				this.passwordRequirementsDao.setHasUpper(   Boolean.valueOf(fp.getString("pwd.chars.case.upper")));
				this.passwordRequirementsDao.setHasDigits(  Boolean.valueOf(fp.getString("pwd.chars.digits")));
				this.passwordRequirementsDao.setHasSpecials(Boolean.valueOf(fp.getString("pwd.chars.specials")));
				this.passwordRequirementsDao.setAllowReuse( Boolean.valueOf(fp.getString("pwd.allow.reuse")));

				this.passwordRequirementsDao.setMinLength(fp.getInt("pwd.chars.min"));
				this.passwordRequirementsDao.setMaxLength(fp.getInt("pwd.chars.max"));
				this.passwordRequirementsDao.setHistorySize(fp.getInt("pwd.history.size"));

				addPageMessage(respage.getString("password_req_changes_have_been_saved"));
				forwardPage(Page.LIST_USER_ACCOUNTS_SERVLET);
        	} else {
        		setPresetValues(submittedValues(fp));
        		setInputMessages(errors);
				forwardPage(Page.CONFIGURATION_PASSWORD_REQUIREMENTS);
        	}
        }
    }

    private HashMap<String,Object> submittedValues(FormProcessor fp) {
    	HashMap<String,Object> values = new HashMap<String,Object>();
    	for (String key: passwordRequirementsDao.boolConfigKeys()) {
    		String val = fp.getString(key);
    		if (val != null) {
    			values.put(key, Boolean.valueOf(val));
    		}
    	}
    	for (String key: passwordRequirementsDao.intConfigKeys()) {
    		String val = fp.getString(key);
    		if (val != null) {
    			values.put(key, val);
    		}
    	}
    	return values;
    }
    
    @Override
    protected String getAdminServlet() {
        return SecureController.ADMIN_SERVLET_CODE;
    }
}
