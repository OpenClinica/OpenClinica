package org.akaza.openclinica.dao.rule;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.hibernate.RuleDao;
import org.akaza.openclinica.domain.rule.RuleBean;
import org.akaza.openclinica.domain.rule.expression.Context;
import org.akaza.openclinica.domain.rule.expression.ExpressionBean;
import org.akaza.openclinica.templates.HibernateOcDbTestCase;
import org.junit.Ignore;

@Ignore
public class RuleDaoTest extends HibernateOcDbTestCase {
    private static RuleDao ruleDao;

    public RuleDaoTest() {
        super();
       
    }

    @Override
    public void setUp() throws Exception{
        super.setUp();
        ruleDao = (RuleDao) getContext().getBean("ruleDao");
        
    }
    public void testFindByOidString() {
      //  RuleDao ruleDao = (RuleDao) getContext().getBean("ruleDao");
        String oid = "RULE_1";
        RuleBean persistentRuleBean = ruleDao.findByOid(oid, 1);

        assertNotNull("RuleSet is null", persistentRuleBean);
        assertEquals("The id of the retrieved RuleSet should be 1", new Integer(-1), persistentRuleBean.getId());
    }

    public void testFindById() {
      //  RuleDao ruleDao = (RuleDao) getContext().getBean("ruleDao");
        RuleBean ruleBean = null;
        ruleBean = ruleDao.findById(-1);

        // Test Rule
        assertNotNull("RuleSet is null", ruleBean);
        assertEquals("The id of the retrieved RuleSet should be 1", new Integer(-1), ruleBean.getId());

    }

    public void testFindByIdEmptyResultSet() {
      //  RuleDao ruleDao = (RuleDao) getContext().getBean("ruleDao");
        RuleBean ruleBean2 = null;
        ruleBean2 = ruleDao.findById(-3);

        // Test Rule
        assertNull("RuleSet is null", ruleBean2);
    }

    public void testFindByOid() {
       // RuleDao ruleDao = (RuleDao) getContext().getBean("ruleDao");
        RuleBean ruleBean = new RuleBean();
        ruleBean.setOid("RULE_1");
        StudyBean studyBean = new StudyBean();
        studyBean.setId(1);
        ruleBean.setStudy(studyBean);
        RuleBean persistentRuleBean = ruleDao.findByOid(ruleBean);

        assertNotNull("RuleSet is null", persistentRuleBean);
        assertEquals("The id of the retrieved RuleSet should be 1", new Integer(-1), persistentRuleBean.getId());
    }

    public void testSaveOrUpdate() {
     //   RuleDao ruleDao = (RuleDao) getContext().getBean("ruleDao");
        RuleBean ruleBean = createRuleBeanStub();
        RuleBean persistentRuleBean = ruleDao.saveOrUpdate(ruleBean);

        RuleBean ruleBean2 = createRuleBeanStub();
        RuleBean persistentRuleBean2 = ruleDao.saveOrUpdate(ruleBean2);

        assertNotNull("Persistent id is null", persistentRuleBean.getId());
        assertNotNull("Persistent id is null", persistentRuleBean2.getId());
    }

    private RuleBean createRuleBeanStub() {
        RuleBean ruleBean = new RuleBean();
        ruleBean.setName("TEST");
        ruleBean.setOid("BOY");
        ruleBean.setDescription("Yellow");
        ruleBean.setExpression(createExpression(Context.OC_RULES_V1,
                "SE_ED1NONRE.F_AGEN.IG_AGEN_UNGROUPED[1].I_AGEN_PERIODSTART eq \"07/01/2008\" and I_CONC_CON_MED_NAME eq \"Tylenol\""));
        return ruleBean;
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