/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */

package org.akaza.openclinica.dao.rule;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.rule.RuleBean;
import org.akaza.openclinica.bean.rule.RuleSetBean;
import org.akaza.openclinica.bean.rule.RuleSetRuleBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;
import org.akaza.openclinica.exception.OpenClinicaException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.sql.DataSource;

/**
 * <p>
 * Manage RuleSets
 * 
 * 
 * @author Krikor Krumlian
 * 
 */
public class RuleSetRuleDAO extends AuditableEntityDAO {

    private RuleDAO ruleDao;
    private RuleSetDAO ruleSetDao;
    private RuleSetRuleAuditDAO ruleSetRuleAuditDao;

    private void setQueryNames() {
        this.findByPKAndStudyName = "findByPKAndStudy";
        this.getCurrentPKName = "getCurrentPK";
    }

    public RuleSetRuleDAO(DataSource ds) {
        super(ds);
        setQueryNames();
    }

    private RuleDAO getRuleDao() {
        return this.ruleDao != null ? this.ruleDao : new RuleDAO(ds);
    }

    private RuleSetDAO getRuleSetDao() {
        return this.ruleSetDao != null ? this.ruleSetDao : new RuleSetDAO(ds);
    }

    public RuleSetRuleDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
        setQueryNames();
    }

    private RuleSetRuleAuditDAO getRuleSetRuleAuditDao() {
        return this.ruleSetRuleAuditDao != null ? this.ruleSetRuleAuditDao : new RuleSetRuleAuditDAO(ds);
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_RULESET_RULE;
    }

    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT); // rule_set_rule_id
        this.setTypeExpected(2, TypeNames.INT);// rule_set_id
        this.setTypeExpected(3, TypeNames.INT);// rule_id
        this.setTypeExpected(4, TypeNames.INT);// owner_id
        this.setTypeExpected(5, TypeNames.DATE); // date_created
        this.setTypeExpected(6, TypeNames.DATE);// date_updated
        this.setTypeExpected(7, TypeNames.INT);// updater_id
        this.setTypeExpected(8, TypeNames.INT);// status_id

    }

    /**
     * Don't understand why I have to implement this if I don't need to. I understand the motive but with complex Object graphs it is not always CRUD.
     * 
     * @param eb
     * @return
     */
    public EntityBean update(EntityBean eb) throws OpenClinicaException {
        // TODO Auto-generated method stub
        return null;
    }

    public void removeByRuleSet(RuleSetBean eb) {

        RuleSetBean ruleSetBean = eb;
        HashMap<Integer, Integer> variables = new HashMap<Integer, Integer>();

        variables.put(new Integer(1), ruleSetBean.getUpdaterId());
        variables.put(new Integer(2), Status.DELETED.getId());
        variables.put(new Integer(3), ruleSetBean.getId());
        execute(digester.getQuery("updateStatusByRuleSet"), variables);

    }

    public void autoRemoveByRuleSet(RuleSetBean eb, UserAccountBean ub) {

        RuleSetBean ruleSetBean = eb;
        HashMap<Integer, Integer> variables = new HashMap<Integer, Integer>();

        variables.put(new Integer(1), ruleSetBean.getUpdaterId());
        variables.put(new Integer(2), Status.AUTO_DELETED.getId());
        variables.put(new Integer(3), ruleSetBean.getId());
        variables.put(new Integer(4), Status.AVAILABLE.getId());
        execute(digester.getQuery("updateStatusByRuleSetAuto"), variables);

    }

    public void autoRestoreByRuleSet(RuleSetBean eb, UserAccountBean ub) {

        RuleSetBean ruleSetBean = eb;
        HashMap<Integer, Integer> variables = new HashMap<Integer, Integer>();

        variables.put(new Integer(1), ub.getId());
        variables.put(new Integer(2), Status.AVAILABLE.getId());
        variables.put(new Integer(3), ruleSetBean.getId());
        variables.put(new Integer(4), Status.AUTO_DELETED.getId());
        execute(digester.getQuery("updateStatusByRuleSetAuto"), variables);

    }

    public void remove(RuleSetRuleBean eb, UserAccountBean ub) {

        RuleSetRuleBean ruleSetRuleBean = eb;
        HashMap<Integer, Integer> variables = new HashMap<Integer, Integer>();

        variables.put(new Integer(1), ub.getId());
        variables.put(new Integer(2), Status.DELETED.getId());
        variables.put(new Integer(3), ruleSetRuleBean.getId());
        execute(digester.getQuery("updateStatus"), variables);
        if (isQuerySuccessful()) {
            ruleSetRuleBean.setStatus(Status.DELETED);
            getRuleSetRuleAuditDao().create(ruleSetRuleBean, ub);
        }

    }

    public void restore(RuleSetRuleBean eb, UserAccountBean ub) {

        RuleSetRuleBean ruleSetRuleBean = eb;
        HashMap<Integer, Integer> variables = new HashMap<Integer, Integer>();

        variables.put(new Integer(1), ub.getId());
        variables.put(new Integer(2), Status.AVAILABLE.getId());
        variables.put(new Integer(3), ruleSetRuleBean.getId());
        execute(digester.getQuery("updateStatus"), variables);

        if (isQuerySuccessful()) {
            ruleSetRuleBean.setStatus(Status.AVAILABLE);
            getRuleSetRuleAuditDao().create(ruleSetRuleBean, ub);
        }

    }

    /*
     * I am going to attempt to use this create method as we use the saveOrUpdate method in Hibernate.
     */
    public EntityBean create(EntityBean eb) {
        RuleSetRuleBean ruleSetRuleBean = (RuleSetRuleBean) eb;
        RuleBean ruleBean = new RuleBean();
        ruleBean.setOid(ruleSetRuleBean.getOid());

        if (eb.getId() == 0) {
            HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
            HashMap<Integer, Object> nullVars = new HashMap<Integer, Object>();
            variables.put(new Integer(1), ruleSetRuleBean.getRuleSetBean().getId());
            variables.put(new Integer(2), getRuleDao().findByOid(ruleBean).getId());
            variables.put(new Integer(3), new Integer(ruleSetRuleBean.getOwnerId()));
            variables.put(new Integer(4), new Integer(Status.AVAILABLE.getId()));

            executeWithPK(digester.getQuery("create"), variables, nullVars);
            if (isQuerySuccessful()) {
                ruleSetRuleBean.setId(getLatestPK());
            }

        }
        // persist rules if exist
        // createRuleSetRules(ruleSetBean);

        return ruleSetRuleBean;
    }

    private void createRuleSetRules(RuleSetBean ruleSetBean) {
        if (ruleSetBean.getId() > 0) {

        }
    }

    public Object getEntityFromHashMap(HashMap hm) {
        RuleSetRuleBean ruleSetRuleBean = new RuleSetRuleBean();
        this.setEntityAuditInformation(ruleSetRuleBean, hm);

        ruleSetRuleBean.setId(((Integer) hm.get("rule_set_rule_id")).intValue());
        int ruleBeanId = ((Integer) hm.get("rule_id")).intValue();
        ruleSetRuleBean.setRuleBean((RuleBean) getRuleDao().findByPK(ruleBeanId));

        return ruleSetRuleBean;
    }

    public Object getEntityFromHashMap(HashMap hm, Boolean getRuleSet) {
        RuleSetRuleBean ruleSetRuleBean = new RuleSetRuleBean();
        this.setEntityAuditInformation(ruleSetRuleBean, hm);

        ruleSetRuleBean.setId(((Integer) hm.get("rule_set_rule_id")).intValue());
        int ruleSetBeanId = ((Integer) hm.get("rule_set_id")).intValue();
        if (getRuleSet) {
            ruleSetRuleBean.setRuleSetBean((RuleSetBean) getRuleSetDao().findByPK(ruleSetBeanId));
        }
        int ruleBeanId = ((Integer) hm.get("rule_id")).intValue();
        ruleSetRuleBean.setRuleBean((RuleBean) getRuleDao().findByPK(ruleBeanId));

        return ruleSetRuleBean;
    }

    public Collection findAll() {
        this.setTypesExpected();
        ArrayList alist = this.select(digester.getQuery("findAll"));
        ArrayList<RuleSetRuleBean> ruleSetBeans = new ArrayList<RuleSetRuleBean>();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            RuleSetRuleBean ruleSetRuleBean = (RuleSetRuleBean) this.getEntityFromHashMap((HashMap) it.next());
            ruleSetBeans.add(ruleSetRuleBean);
        }
        return ruleSetBeans;
    }

    public EntityBean findByPK(int ID) {
        RuleSetRuleBean ruleSetRuleBean = null;
        this.setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), new Integer(ID));

        String sql = digester.getQuery("findByPK");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            ruleSetRuleBean = (RuleSetRuleBean) this.getEntityFromHashMap((HashMap) it.next(), true);
        }
        return ruleSetRuleBean;
    }

    public ArrayList<RuleSetRuleBean> findByRuleSet(RuleSetBean ruleSet) {
        ArrayList<RuleSetRuleBean> ruleSetRuleBeans = new ArrayList<RuleSetRuleBean>();

        this.setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        Integer ruleSetId = Integer.valueOf(ruleSet.getId());
        variables.put(new Integer(1), ruleSetId);
        // variables.put(new Integer(2), new Integer(Status.AVAILABLE.getId()));

        String sql = digester.getQuery("findByRuleSetId");
        ArrayList<?> alist = this.select(sql, variables);
        Iterator<?> it = alist.iterator();

        while (it.hasNext()) {
            RuleSetRuleBean ruleSetRule = (RuleSetRuleBean) this.getEntityFromHashMap((HashMap<?, ?>) it.next());
            ruleSetRule.setRuleSetBean(ruleSet);
            ruleSetRuleBeans.add(ruleSetRule);
        }
        return ruleSetRuleBeans;
    }

    public ArrayList<RuleSetRuleBean> findByRuleSetAndRule(RuleSetBean ruleSet, RuleBean rule) {
        ArrayList<RuleSetRuleBean> ruleSetRuleBeans = new ArrayList<RuleSetRuleBean>();

        this.setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        Integer ruleSetId = Integer.valueOf(ruleSet.getId());
        Integer ruleId = Integer.valueOf(rule.getId());
        variables.put(new Integer(1), ruleSetId);
        variables.put(new Integer(2), new Integer(Status.AVAILABLE.getId()));
        variables.put(new Integer(3), ruleId);

        String sql = digester.getQuery("findByRuleSetIdAndRuleId");
        ArrayList<?> alist = this.select(sql, variables);
        Iterator<?> it = alist.iterator();

        while (it.hasNext()) {
            RuleSetRuleBean ruleSetRule = (RuleSetRuleBean) this.getEntityFromHashMap((HashMap<?, ?>) it.next());
            ruleSetRule.setRuleSetBean(ruleSet);
            ruleSetRuleBeans.add(ruleSetRule);
        }
        return ruleSetRuleBeans;
    }

    public RuleSetBean findByStudyEventDefinition(StudyEventDefinitionBean studyEventDefinition) {
        RuleSetBean ruleSetBean = null;
        this.setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        Integer studyEventDefinitionId = Integer.valueOf(studyEventDefinition.getId());
        variables.put(new Integer(1), studyEventDefinitionId);

        String sql = digester.getQuery("findByStudyEventDefinition");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            ruleSetBean = (RuleSetBean) this.getEntityFromHashMap((HashMap) it.next());
        }
        return ruleSetBean;
    }

    /*
     * Why should we even have these in here if they are not needed? TODO: refactor super class to remove dependency.
     */
    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        ArrayList al = new ArrayList();

        return al;
    }

    /*
     * Why should we even have these in here if they are not needed? TODO: refactor super class to remove dependency.
     */
    public Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        ArrayList al = new ArrayList();

        return al;
    }

    /*
     * Why should we even have these in here if they are not needed? TODO: refactor super class to remove dependency.
     */
    public Collection findAllByPermission(Object objCurrentUser, int intActionType) {
        ArrayList al = new ArrayList();

        return al;
    }

}