/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.dao.rule;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.rule.RuleSetAuditBean;
import org.akaza.openclinica.bean.rule.RuleSetBean;
import org.akaza.openclinica.bean.rule.RuleSetRuleAuditBean;
import org.akaza.openclinica.bean.rule.RuleSetRuleBean;
import org.akaza.openclinica.bean.submit.ItemGroupMetadataBean;
import org.akaza.openclinica.dao.core.EntityDAO;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.exception.OpenClinicaException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import javax.sql.DataSource;

public class RuleSetRuleAuditDAO extends EntityDAO {

    RuleSetDAO ruleSetDao;
    RuleSetRuleDAO ruleSetRuleDao;
    UserAccountDAO userAccountDao;

    public RuleSetRuleAuditDAO(DataSource ds) {
        super(ds);
        this.getCurrentPKName = "findCurrentPKValue";
    }

    @Override
    public int getCurrentPK() {
        int answer = 0;

        if (getCurrentPKName == null) {
            return answer;
        }

        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);

        ArrayList al = select(digester.getQuery(getCurrentPKName));

        if (al.size() > 0) {
            HashMap h = (HashMap) al.get(0);
            answer = ((Integer) h.get("key")).intValue();
        }

        return answer;
    }

    private RuleSetDAO getRuleSetDao() {
        return this.ruleSetDao != null ? this.ruleSetDao : new RuleSetDAO(ds);
    }

    private RuleSetRuleDAO getRuleSetRuleDao() {
        return this.ruleSetRuleDao != null ? this.ruleSetRuleDao : new RuleSetRuleDAO(ds);
    }

    private UserAccountDAO getUserAccountDao() {
        return this.userAccountDao != null ? this.userAccountDao : new UserAccountDAO(ds);
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_RULESETRULE_AUDIT;
    }

    public void setTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);
        this.setTypeExpected(3, TypeNames.DATE);// date_updated
        this.setTypeExpected(4, TypeNames.INT);
        this.setTypeExpected(5, TypeNames.INT);

    }

    public Object getEntityFromHashMap(HashMap hm) {
        RuleSetRuleAuditBean ruleSetRuleAudit = new RuleSetRuleAuditBean();
        ruleSetRuleAudit.setId((Integer) hm.get("rule_set_rule_audit_id"));
        int ruleSetRuleId = (Integer) hm.get("rule_set_rule_id");
        int userAccountId = (Integer) hm.get("updater_id");
        int statusId = (Integer) hm.get("status_id");
        Date dateUpdated = (Date) hm.get("date_updated");
        ruleSetRuleAudit.setDateUpdated(dateUpdated);
        ruleSetRuleAudit.setStatus(Status.get(statusId));
        ruleSetRuleAudit.setRuleSetRuleBean((RuleSetRuleBean) getRuleSetRuleDao().findByPK(ruleSetRuleId));
        ruleSetRuleAudit.setUpdater((UserAccountBean) getUserAccountDao().findByPK(userAccountId));

        return ruleSetRuleAudit;
    }

    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) throws OpenClinicaException {
        return new ArrayList();
    }

    public Collection findAll() throws OpenClinicaException {
        return new ArrayList();
    }

    public EntityBean findByPK(int id) throws OpenClinicaException {
        RuleSetRuleAuditBean ruleSetRuleAudit = null;

        this.setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), id);

        String sql = digester.getQuery("findByPK");
        ArrayList<?> alist = this.select(sql, variables);

        Iterator<?> it = alist.iterator();

        if (it.hasNext()) {
            ruleSetRuleAudit = (RuleSetRuleAuditBean) this.getEntityFromHashMap((HashMap<?, ?>) it.next());
        }
        return ruleSetRuleAudit;
    }

    public ArrayList<RuleSetRuleAuditBean> findAllByRuleSet(RuleSetBean ruleSet) {
        ArrayList<RuleSetRuleAuditBean> ruleSetRuleAuditBeans = new ArrayList<RuleSetRuleAuditBean>();

        this.setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), ruleSet.getId());

        String sql = digester.getQuery("findAllByRuleSet");
        ArrayList<?> alist = this.select(sql, variables);
        Iterator<?> it = alist.iterator();

        while (it.hasNext()) {
            RuleSetRuleAuditBean ruleSetRuleAudit = (RuleSetRuleAuditBean) this.getEntityFromHashMap((HashMap<?, ?>) it.next());
            ruleSetRuleAuditBeans.add(ruleSetRuleAudit);
        }
        return ruleSetRuleAuditBeans;
    }

    public EntityBean create(EntityBean eb, UserAccountBean ub) {
        // INSERT INTO rule_set_rule_audit (rule_set_rule_id, status_id,updater_id,date_updated) VALUES (?,?,?,?,?)
        RuleSetRuleBean ruleSetRuleBean = (RuleSetRuleBean) eb;
        RuleSetRuleAuditBean ruleSetRuleAudit = new RuleSetRuleAuditBean();
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(1, ruleSetRuleBean.getId());
        variables.put(2, ruleSetRuleBean.getStatus().getId());
        variables.put(3, ub.getId());

        this.execute(digester.getQuery("create"), variables);
        if (isQuerySuccessful()) {
            ruleSetRuleAudit.setRuleSetRuleBean(ruleSetRuleBean);
            ruleSetRuleAudit.setId(getCurrentPK());
            ruleSetRuleAudit.setStatus(ruleSetRuleBean.getStatus());
            ruleSetRuleAudit.setUpdater(ub);
        }
        return ruleSetRuleAudit;
    }

    public EntityBean create(EntityBean eb) throws OpenClinicaException {
        RuleSetRuleBean ruleSetRuleBean = (RuleSetRuleBean) eb;
        UserAccountBean userAccount = new UserAccountBean();
        userAccount.setId(ruleSetRuleBean.getUpdaterId());
        return create(eb, userAccount);
    }

    public EntityBean update(EntityBean eb) throws OpenClinicaException {
        return new ItemGroupMetadataBean(); // To change body of implemented

    }

    public Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase)
            throws OpenClinicaException {
        return new ArrayList<RuleSetAuditBean>();
    }

    public Collection findAllByPermission(Object objCurrentUser, int intActionType) throws OpenClinicaException {
        return new ArrayList<RuleSetAuditBean>();
    }

}
