package org.akaza.openclinica.dao.rule;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.hibernate.RuleDao;
import org.akaza.openclinica.dao.hibernate.RuleSetDao;
import org.akaza.openclinica.domain.rule.RuleBean;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.akaza.openclinica.domain.rule.action.ActionType;
import org.akaza.openclinica.domain.rule.action.DiscrepancyNoteActionBean;
import org.akaza.openclinica.domain.rule.expression.Context;
import org.akaza.openclinica.domain.rule.expression.ExpressionBean;
import org.akaza.openclinica.templates.HibernateOcDbTestCase;
import org.hibernate.HibernateException;
import org.junit.Ignore;

import java.util.List;

@Ignore
public class RuleSetDaoTest extends HibernateOcDbTestCase {
    private static RuleSetDao ruleSetDao;
    private static RuleDao ruleDao;
    public RuleSetDaoTest() {
        super();
       
    }
    
    @Override
    public void setUp() throws Exception{
        super.setUp();
        ruleSetDao = (RuleSetDao) getContext().getBean("ruleSetDao");
        ruleDao = (RuleDao) getContext().getBean("ruleDao");
    }


    public void testFindById() {
 //       RuleSetDao ruleSetDao = (RuleSetDao) getContext().getBean("ruleSetDao");
        RuleSetBean ruleSet = null;
        ruleSet = ruleSetDao.findById(-1);

        // Test RuleSet
        assertNotNull("RuleSet is null", ruleSet);
        assertEquals("The id of the retrieved RuleSet should be 1", new Integer(-1), ruleSet.getId());
        assertNotNull("The Expression is null", ruleSet.getTarget());
        assertNotNull("The Context is null", ruleSet.getTarget().getContext());
        assertEquals("The context should be 1", new Integer(1), ruleSet.getTarget().getContext().getCode());

        // Test RuleSetRules
        assertEquals("The size of the RuleSetRules is not 1", new Integer(1), Integer.valueOf(ruleSet.getRuleSetRules().size()));

        // Test RuleActions in RuleSetRules
        assertEquals("The ActionType should be FILE_DISCREPANCY_NOTE", ActionType.FILE_DISCREPANCY_NOTE, ruleSet.getRuleSetRules().get(0).getActions().get(0)
                .getActionType());
        assertEquals("The type of the Action should be DiscrepancyNoteAction", "org.akaza.openclinica.domain.rule.action.DiscrepancyNoteActionBean", ruleSet
                .getRuleSetRules().get(0).getActions().get(0).getClass().getName());
        assertEquals("The size of the RuleSetRules is not 2", new Integer(2), Integer.valueOf(ruleSet.getRuleSetRules().get(0).getActions().size()));
    }


    public void testSaveOrUpdate() {
        // RuleSetDao ruleSetDao = (RuleSetDao) getContext().getBean("ruleSetDao");
      //   RuleDao ruleDao = (RuleDao) getContext().getBean("ruleDao");
         RuleBean persistantRuleBean = ruleDao.findById(1);
         RuleSetBean ruleSetBean = createStubRuleSetBean(persistantRuleBean);
         ruleSetBean = ruleSetDao.saveOrUpdate(ruleSetBean);
              assertNotNull("Persistant id is null", ruleSetBean.getId());
     }

    public void testFindByCrfEmptyResultSet() {
        CRFBean crfBean = new CRFBean();
        crfBean.setId(4);
        StudyBean studyBean = new StudyBean();
        studyBean.setId(1);
       // RuleSetDao ruleSetDao = (RuleSetDao) getContext().getBean("ruleSetDao");

        crfBean.setId(4);
        List<RuleSetBean> persistentRuleSets = ruleSetDao.findByCrf(crfBean, studyBean);
        assertNotNull("The returned ruleSet was null", persistentRuleSets);
        assertEquals("The List size of ruleset objects should be 0 ", persistentRuleSets.size(), 0);

    }

    public void testFindByExpression() {
        RuleSetBean ruleSet = createStubRuleSetBean();
     //   RuleSetDao ruleSetDao = (RuleSetDao) getContext().getBean("ruleSetDao");
        RuleSetBean persistentRuleSet = ruleSetDao.findByExpression(ruleSet);
        assertNotNull("The returned ruleSet was null", persistentRuleSet);
        assertEquals("The id of returned object should be -1 ", persistentRuleSet.getId(), new Integer(-1));

    }

    private RuleSetBean createStubRuleSetBean(RuleBean ruleBean) {
        RuleSetBean ruleSet = new RuleSetBean();
        ruleSet.setOriginalTarget(createExpression(Context.OC_RULES_V1, "SE_ED2REPEA.F_CONC_V20.IG_CONC_CONCOMITANTMEDICATIONS.I_CONC_CON_MED_N"));
        ruleSet.setStudyId(1);
        RuleSetRuleBean ruleSetRule = createRuleSetRule(ruleSet, ruleBean);
        ruleSet.addRuleSetRule(ruleSetRule);
        return ruleSet;
    }

    private RuleSetBean createStubRuleSetBean() {
        RuleSetBean ruleSet = new RuleSetBean();
        ruleSet.setTarget(createExpression(Context.OC_RULES_V1, "SE_ED2REPEA.F_CONC_V20.IG_CONC_CONCOMITANTMEDICATIONS.I_CONC_CON_MED_NAME"));
        RuleSetRuleBean ruleSetRule = createRuleSetRule(ruleSet, null);
        ruleSet.addRuleSetRule(ruleSetRule);
        return ruleSet;
    }

    private RuleSetRuleBean createRuleSetRule(RuleSetBean ruleSet, RuleBean ruleBean) {
        RuleSetRuleBean ruleSetRule = new RuleSetRuleBean();
        DiscrepancyNoteActionBean ruleAction = new DiscrepancyNoteActionBean();
        ruleAction.setMessage("HELLO WORLD");
        ruleAction.setExpressionEvaluatesTo(true);
        ruleSetRule.addAction(ruleAction);
        ruleSetRule.setRuleSetBean(ruleSet);
        ruleSetRule.setRuleBean(ruleBean);

        return ruleSetRule;
    }


    private ExpressionBean createExpression(Context context, String value) {
        ExpressionBean expression = new ExpressionBean();
        expression.setContext(context);
        expression.setValue(value);
        return expression;
    }
    public void tearDown(){
        super.tearDown();
    }

}