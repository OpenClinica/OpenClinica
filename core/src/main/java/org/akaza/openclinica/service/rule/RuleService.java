/* 
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * OpenClinica is distributed under the
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.service.rule;

import org.akaza.openclinica.bean.oid.GenericOidGenerator;
import org.akaza.openclinica.bean.oid.OidGenerator;
import org.akaza.openclinica.bean.rule.RuleBean;
import org.akaza.openclinica.bean.rule.RuleSetBean;
import org.akaza.openclinica.dao.rule.RuleDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class RuleService {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    DataSource ds;
    private RuleDAO ruleDao;
    private OidGenerator oidGenerator;

    public RuleService(DataSource ds) {
        oidGenerator = new GenericOidGenerator();
        this.ds = ds;
    }

    public boolean enableRules(RuleSetBean ruleSet) {
        return true;
    }

    public boolean disableRules() {
        return true;

    }

    public RuleBean saveRule(RuleBean ruleBean) {
        return (RuleBean) getRuleDao().create(ruleBean);
    }

    public RuleBean updateRule(RuleBean ruleBean) {
        return (RuleBean) getRuleDao().update(ruleBean);
    }

    private RuleDAO getRuleDao() {
        ruleDao = this.ruleDao != null ? ruleDao : new RuleDAO(ds);
        return ruleDao;
    }

}
