/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */

package org.akaza.openclinica.dao.rule;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.rule.RuleSetBean;
import org.akaza.openclinica.bean.rule.expression.Context;
import org.akaza.openclinica.bean.rule.expression.ExpressionBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.service.rule.expression.ExpressionService;

import javax.sql.DataSource;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * <p>
 * Manage RuleSets
 * 
 * 
 * @author Krikor Krumlian
 * 
 */
public class RuleSetDAO extends AuditableEntityDAO {

    private EventCRFDAO eventCrfDao;
    private StudyEventDefinitionDAO studyEventDefinitionDAO;
    private RuleDAO ruleDao;
    private ExpressionDAO expressionDao;
    private CRFDAO crfDao;
    private CRFVersionDAO crfVersionDao;
    private ExpressionService expressionService;
    private RuleSetRuleDAO ruleSetRuleDao;
    private RuleSetAuditDAO ruleSetAuditDao;

    private void setQueryNames() {
        this.findByPKAndStudyName = "findByPKAndStudy";
        this.getCurrentPKName = "getCurrentPK";
    }

    public RuleSetDAO(DataSource ds) {
        super(ds);
        setQueryNames();
    }

    private StudyEventDefinitionDAO getStudyEventDefinitionDao() {
        return this.studyEventDefinitionDAO != null ? this.studyEventDefinitionDAO : new StudyEventDefinitionDAO(ds);
    }

    private CRFDAO getCrfDao() {
        return this.crfDao != null ? this.crfDao : new CRFDAO(ds);
    }

    private CRFVersionDAO getCrfVersionDao() {
        return this.crfVersionDao != null ? this.crfVersionDao : new CRFVersionDAO(ds);
    }

    private EventCRFDAO getEventCrfDao() {
        return this.eventCrfDao != null ? this.eventCrfDao : new EventCRFDAO(ds);
    }

    private RuleDAO getRuleDao() {
        return this.ruleDao != null ? this.ruleDao : new RuleDAO(ds);
    }

    private RuleSetAuditDAO getRuleSetAuditDao() {
        return this.ruleSetAuditDao != null ? this.ruleSetAuditDao : new RuleSetAuditDAO(ds);
    }

    private ExpressionDAO getExpressionDao() {
        return this.expressionDao != null ? this.expressionDao : new ExpressionDAO(ds);
    }

    private ExpressionService getExpressionService() {
        return this.expressionService != null ? this.expressionService : new ExpressionService(ds);
    }

    private RuleSetRuleDAO getRuleSetRuleDao() {
        return this.ruleSetRuleDao != null ? this.ruleSetRuleDao : new RuleSetRuleDAO(ds);
    }

    public RuleSetDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
        setQueryNames();
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_RULESET;
    }

    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT); // ruleset_id
        this.setTypeExpected(2, TypeNames.INT);// expression_id
        this.setTypeExpected(3, TypeNames.INT);// study_event_definition_id
        this.setTypeExpected(4, TypeNames.INT);// crf_id
        this.setTypeExpected(5, TypeNames.INT);// crf_version_id
        this.setTypeExpected(6, TypeNames.INT);// study_id
        this.setTypeExpected(7, TypeNames.INT);// owner_id
        this.setTypeExpected(8, TypeNames.DATE); // date_created
        this.setTypeExpected(9, TypeNames.DATE);// date_updated
        this.setTypeExpected(10, TypeNames.INT);// updater_id
        this.setTypeExpected(11, TypeNames.INT);// status_id

    }

    public EntityBean update(EntityBean eb) {
        RuleSetBean ruleSetBean = (RuleSetBean) eb;

        ruleSetBean.setActive(false);

        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        HashMap nullVars = new HashMap();

        this.execute(digester.getQuery("update"), variables, nullVars);

        if (isQuerySuccessful()) {
            ruleSetBean.setActive(true);
        }

        return ruleSetBean;
    }

    public EntityBean remove(RuleSetBean ruleSetBean, UserAccountBean ub) {
        ruleSetBean.setActive(false);

        HashMap<Integer, Integer> variables = new HashMap<Integer, Integer>();

        variables.put(new Integer(1), new Integer(ub.getId()));
        variables.put(new Integer(2), new Integer(Status.DELETED.getId()));
        variables.put(new Integer(3), new Integer(ruleSetBean.getId()));

        this.execute(digester.getQuery("removeOrRestore"), variables);

        if (isQuerySuccessful()) {
            ruleSetBean.setActive(true);
            getRuleSetRuleDao().autoRemoveByRuleSet(ruleSetBean, ub);
            ruleSetBean.setStatus(Status.DELETED);
            getRuleSetAuditDao().create(ruleSetBean, ub);

        }

        return ruleSetBean;
    }

    public EntityBean restore(RuleSetBean ruleSetBean, UserAccountBean ub) {
        ruleSetBean.setActive(false);

        HashMap<Integer, Integer> variables = new HashMap<Integer, Integer>();

        variables.put(new Integer(1), new Integer(ub.getId()));
        variables.put(new Integer(2), new Integer(Status.AVAILABLE.getId()));
        variables.put(new Integer(3), new Integer(ruleSetBean.getId()));

        this.execute(digester.getQuery("removeOrRestore"), variables);

        if (isQuerySuccessful()) {
            ruleSetBean.setActive(true);
            getRuleSetRuleDao().autoRestoreByRuleSet(ruleSetBean, ub);
            ruleSetBean.setStatus(Status.AVAILABLE);
            getRuleSetAuditDao().create(ruleSetBean, ub);
        }

        return ruleSetBean;
    }

    /*
     * I am going to attempt to use this create method as we use the saveOrUpdate method in Hibernate.
     */
    public EntityBean create(EntityBean eb) {
        RuleSetBean ruleSetBean = (RuleSetBean) eb;
        if (eb.getId() == 0) {
            HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
            HashMap<Integer, Object> nullVars = new HashMap<Integer, Object>();
            variables.put(new Integer(1), getExpressionDao().create(ruleSetBean.getTarget()).getId());
            variables.put(new Integer(2), new Integer(ruleSetBean.getStudyEventDefinition().getId()));
            if (ruleSetBean.getCrf() == null) {
                nullVars.put(new Integer(3), new Integer(Types.INTEGER));
                variables.put(new Integer(3), null);
        } else {
                variables.put(new Integer(3), new Integer(ruleSetBean.getCrf().getId()));
            }
            if (ruleSetBean.getCrfVersion() == null) {
                nullVars.put(new Integer(4), new Integer(Types.INTEGER));
                variables.put(new Integer(4), null);
            } else {
                variables.put(new Integer(4), new Integer(ruleSetBean.getCrfVersion().getId()));
            }
            variables.put(new Integer(5), new Integer(ruleSetBean.getStudy().getId()));
            variables.put(new Integer(6), new Integer(ruleSetBean.getOwnerId()));
            variables.put(new Integer(7), new Integer(Status.AVAILABLE.getId()));

            executeWithPK(digester.getQuery("create"), variables, nullVars);
            if (isQuerySuccessful()) {
                ruleSetBean.setId(getLatestPK());
            }

        }
        return ruleSetBean;
    }

    public Object getEntityFromHashMap(HashMap hm) {
        RuleSetBean ruleSetBean = new RuleSetBean();
        this.setEntityAuditInformation(ruleSetBean, hm);

        ruleSetBean.setId(((Integer) hm.get("rule_set_id")).intValue());
        int expressionId = ((Integer) hm.get("rule_expression_id")).intValue();
        ExpressionBean expression = (ExpressionBean) getExpressionDao().findByPK(expressionId);
        ruleSetBean.setTarget(expression);
        ruleSetBean.setOriginalTarget(expression);
        ruleSetBean.setItemGroup(getExpressionService().getItemGroupExpression(ruleSetBean.getTarget().getValue()));
        ruleSetBean.setItem(getExpressionService().getItemExpression(ruleSetBean.getTarget().getValue(), ruleSetBean.getItemGroup()));
        int studyEventDefenitionId = ((Integer) hm.get("study_event_definition_id")).intValue();
        ruleSetBean.setStudyEventDefinition((StudyEventDefinitionBean) getStudyEventDefinitionDao().findByPK(studyEventDefenitionId));
        int crfId = ((Integer) hm.get("crf_id")).intValue();
        ruleSetBean.setCrf((CRFBean) getCrfDao().findByPK(crfId));
        if ((Integer) hm.get("crf_version_id") != 0) {
            int crfVersionId = ((Integer) hm.get("crf_version_id")).intValue();
            ruleSetBean.setCrfVersion((CRFVersionBean) getCrfVersionDao().findByPK(crfVersionId));
        } else {
            ruleSetBean.setCrfVersion(null);
        }

        return ruleSetBean;
    }

    public RuleSetBean findByExpression(RuleSetBean ruleSetBean) {
        RuleSetBean ruleSetBeanInDb = new RuleSetBean();
        Context c = ruleSetBean.getTarget().getContext() == null ? Context.OC_RULES_V1 : ruleSetBean.getTarget().getContext();
        this.setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), c.getCode());
        variables.put(new Integer(2), ruleSetBean.getTarget().getValue());

        String sql = digester.getQuery("findByExpression");
        ArrayList<?> alist = this.select(sql, variables);
        Iterator<?> it = alist.iterator();

        if (it.hasNext()) {
            ruleSetBeanInDb = (RuleSetBean) this.getEntityFromHashMap((HashMap<?, ?>) it.next());
        }
        if (alist.isEmpty()) {
            ruleSetBeanInDb = null;
        }
        return ruleSetBeanInDb;
    }

    private int getStudyId(StudyBean currentStudy) {
        return currentStudy.getParentStudyId() != 0 ? currentStudy.getParentStudyId() : currentStudy.getId();
    }

    public ArrayList<RuleSetBean> findByCrf(CRFBean crfBean, StudyBean currentStudy) {
        ArrayList<RuleSetBean> ruleSetBeans = new ArrayList<RuleSetBean>();

        this.setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), crfBean.getId());
        variables.put(new Integer(2), getStudyId(currentStudy));

        String sql = digester.getQuery("findByCrfId");
        ArrayList<?> alist = this.select(sql, variables);
        Iterator<?> it = alist.iterator();

        while (it.hasNext()) {
            RuleSetBean ruleSet = (RuleSetBean) this.getEntityFromHashMap((HashMap<?, ?>) it.next());
            ruleSetBeans.add(ruleSet);
        }
        return ruleSetBeans;
    }

    public ArrayList<RuleSetBean> findByCrfVersionStudyAndStudyEventDefinition(CRFVersionBean crfVersionBean, StudyBean currentStudy,
            StudyEventDefinitionBean sed) {
        ArrayList<RuleSetBean> ruleSetBeans = new ArrayList<RuleSetBean>();

        this.setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), crfVersionBean.getId());
        variables.put(new Integer(2), getStudyId(currentStudy));
        variables.put(new Integer(3), sed.getId());

        String sql = digester.getQuery("findByCrfVersionStudyAndStudyEventDefinition");
        ArrayList<?> alist = this.select(sql, variables);
        Iterator<?> it = alist.iterator();

        while (it.hasNext()) {
            RuleSetBean ruleSet = (RuleSetBean) this.getEntityFromHashMap((HashMap<?, ?>) it.next());
            ruleSetBeans.add(ruleSet);
        }
        return ruleSetBeans;
    }

    public ArrayList<RuleSetBean> findByCrfVersionOrCrfAndStudyAndStudyEventDefinition(CRFVersionBean crfVersion, CRFBean crfBean, StudyBean currentStudy,
            StudyEventDefinitionBean sed) {
        ArrayList<RuleSetBean> ruleSetBeans = new ArrayList<RuleSetBean>();

        this.setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), getStudyId(currentStudy));
        variables.put(new Integer(2), sed.getId());
        variables.put(new Integer(3), crfVersion.getId());
        variables.put(new Integer(4), crfBean.getId());
        variables.put(new Integer(5), crfBean.getId());

        String sql = digester.getQuery("findByCrfVersionOrCrfStudyAndStudyEventDefinition");
        ArrayList<?> alist = this.select(sql, variables);
        Iterator<?> it = alist.iterator();

        while (it.hasNext()) {
            RuleSetBean ruleSet = (RuleSetBean) this.getEntityFromHashMap((HashMap<?, ?>) it.next());
            ruleSetBeans.add(ruleSet);
        }
        return ruleSetBeans;
    }

    public ArrayList<RuleSetBean> findByCrfStudyAndStudyEventDefinition(CRFBean crfBean, StudyBean currentStudy, StudyEventDefinitionBean sed) {
        ArrayList<RuleSetBean> ruleSetBeans = new ArrayList<RuleSetBean>();

        this.setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), crfBean.getId());
        variables.put(new Integer(2), getStudyId(currentStudy));
        variables.put(new Integer(3), sed.getId());

        String sql = digester.getQuery("findByCrfStudyAndStudyEventDefinition");
        ArrayList<?> alist = this.select(sql, variables);
        Iterator<?> it = alist.iterator();

        while (it.hasNext()) {
            RuleSetBean ruleSet = (RuleSetBean) this.getEntityFromHashMap((HashMap<?, ?>) it.next());
            ruleSetBeans.add(ruleSet);
        }
        return ruleSetBeans;
    }

    @Override
    public ArrayList<RuleSetBean> findAllByStudy(StudyBean currentStudy) {
        ArrayList<RuleSetBean> ruleSetBeans = new ArrayList<RuleSetBean>();

        this.setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), getStudyId(currentStudy));

        String sql = digester.getQuery("findAllByStudy");
        ArrayList<?> alist = this.select(sql, variables);
        Iterator<?> it = alist.iterator();

        while (it.hasNext()) {
            RuleSetBean ruleSet = (RuleSetBean) this.getEntityFromHashMap((HashMap<?, ?>) it.next());
            ruleSetBeans.add(ruleSet);
        }
        return ruleSetBeans;
    }

    public Collection findAll() {
        this.setTypesExpected();
        ArrayList alist = this.select(digester.getQuery("findAll"));
        ArrayList<RuleSetBean> ruleSetBeans = new ArrayList<RuleSetBean>();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            RuleSetBean ruleSet = (RuleSetBean) this.getEntityFromHashMap((HashMap) it.next());
            ruleSetBeans.add(ruleSet);
        }
        return ruleSetBeans;
    }

    public EntityBean findByPK(int ID) {
        RuleSetBean ruleSetBean = null;
        this.setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), new Integer(ID));

        String sql = digester.getQuery("findByPK");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            ruleSetBean = (RuleSetBean) this.getEntityFromHashMap((HashMap) it.next());
        }
        return ruleSetBean;
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