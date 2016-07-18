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
import org.akaza.openclinica.bean.submit.ItemGroupMetadataBean;
import org.akaza.openclinica.dao.core.EntityDAO;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.exception.OpenClinicaException;

import javax.sql.DataSource;
import java.util.*;

public class RuleSetAuditDAO extends EntityDAO {

    RuleSetDAO ruleSetDao;
    UserAccountDAO userAccountDao;

    public RuleSetAuditDAO(DataSource ds) {
        super(ds);
        this.getCurrentPKName = "findCurrentPKValue";
        // this.getNextPKName = "getNextPK";
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

    private UserAccountDAO getUserAccountDao() {
        return this.userAccountDao != null ? this.userAccountDao : new UserAccountDAO(ds);
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_RULESET_AUDIT;
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
        RuleSetAuditBean ruleSetAudit = new RuleSetAuditBean();
        ruleSetAudit.setId((Integer) hm.get("rule_set_audit_id"));
        int ruleSetId = (Integer) hm.get("rule_set_id");
        int userAccountId = (Integer) hm.get("updater_id");
        int statusId = (Integer) hm.get("status_id");
        Date dateUpdated = (Date) hm.get("date_updated");
        ruleSetAudit.setDateUpdated(dateUpdated);
        ruleSetAudit.setStatus(Status.get(statusId));
        ruleSetAudit.setRuleSetBean((RuleSetBean) getRuleSetDao().findByPK(ruleSetId));
        ruleSetAudit.setUpdater((UserAccountBean) getUserAccountDao().findByPK(userAccountId));

        return ruleSetAudit;
    }

    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) throws OpenClinicaException {
        return new ArrayList();
    }

    public Collection findAll() throws OpenClinicaException {
        return new ArrayList();
    }

    public EntityBean findByPK(int id) throws OpenClinicaException {
        RuleSetAuditBean ruleSetAudit = null;

        this.setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), id);

        String sql = digester.getQuery("findByPK");
        ArrayList<?> alist = this.select(sql, variables);

        Iterator<?> it = alist.iterator();

        if (it.hasNext()) {
            ruleSetAudit = (RuleSetAuditBean) this.getEntityFromHashMap((HashMap<?, ?>) it.next());
        }
        return ruleSetAudit;
    }

    public ArrayList<RuleSetAuditBean> findAllByRuleSet(RuleSetBean ruleSet) {
        ArrayList<RuleSetAuditBean> ruleSetAuditBeans = new ArrayList<RuleSetAuditBean>();

        this.setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), ruleSet.getId());

        String sql = digester.getQuery("findAllByRuleSet");
        ArrayList<?> alist = this.select(sql, variables);
        Iterator<?> it = alist.iterator();

        while (it.hasNext()) {
            RuleSetAuditBean ruleSetAudit = (RuleSetAuditBean) this.getEntityFromHashMap((HashMap<?, ?>) it.next());
            ruleSetAuditBeans.add(ruleSetAudit);
        }
        return ruleSetAuditBeans;
    }

    public EntityBean create(EntityBean eb, UserAccountBean ub) {
        // INSERT INTO rule_set_audit (rule_set_audit_id,rule_set_id, status_id,updater_id,date_updated) VALUES (?,?,?,?,?)
        RuleSetBean ruleSetBean = (RuleSetBean) eb;
        RuleSetAuditBean ruleSetAudit = new RuleSetAuditBean();
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(1, ruleSetBean.getId());
        variables.put(2, ruleSetBean.getStatus().getId());
        variables.put(3, ub.getId());

        this.execute(digester.getQuery("create"), variables);
        if (isQuerySuccessful()) {
            ruleSetAudit.setRuleSetBean(ruleSetBean);
            ruleSetAudit.setId(getCurrentPK());
            ruleSetAudit.setStatus(ruleSetBean.getStatus());
            ruleSetAudit.setUpdater(ub);
        }
        return ruleSetAudit;
    }

    public EntityBean create(EntityBean eb) throws OpenClinicaException {
        RuleSetBean ruleSetBean = (RuleSetBean) eb;
        UserAccountBean userAccount = new UserAccountBean();
        userAccount.setId(ruleSetBean.getUpdaterId());
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
