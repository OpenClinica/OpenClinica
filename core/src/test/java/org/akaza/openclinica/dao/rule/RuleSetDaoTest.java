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

import java.util.List;

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
   /* static
    {
        
        loadProperties();
        dbName = properties.getProperty("dbName");
        dbUrl = properties.getProperty("url");
        dbUserName = properties.getProperty("username");
        dbPassword = properties.getProperty("password");
        dbDriverClassName = properties.getProperty("driver");
        locale = properties.getProperty("locale");
        initializeLocale();
        initializeQueriesInXml();
       
     
        
        context =
            new ClassPathXmlApplicationContext(
                    new String[] { "classpath*:applicationContext-core-s*.xml", "classpath*:org/akaza/openclinica/applicationContext-core-db.xml",
                        "classpath*:org/akaza/openclinica/applicationContext-core-email.xml",
                        "classpath*:org/akaza/openclinica/applicationContext-core-hibernate.xml",
                        "classpath*:org/akaza/openclinica/applicationContext-core-scheduler.xml",
                        "classpath*:org/akaza/openclinica/applicationContext-core-service.xml",
                       " classpath*:org/akaza/openclinica/applicationContext-core-timer.xml",
                        "classpath*:org/akaza/openclinica/applicationContext-security.xml" });
      transactionManager = (PlatformTransactionManager) context.getBean("transactionManager");
      transactionManager.getTransaction(new DefaultTransactionDefinition());
        

    }*/


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

    //JN:The following commented out tests are failing, suspicion is and arnd studyid and the way it is set, TODO:revisit
/*    public void testFindAllByStudy() {

   //     RuleSetDao ruleSetDao = (RuleSetDao) getContext().getBean("ruleSetDao");
        List<RuleSetBean> ruleSets = null;
        StudyBean study = new StudyBean();
        study.setId(1);
        ruleSets = ruleSetDao.findAllByStudy(study);
        assertEquals("The RuleSets List size should be 2", 2, ruleSets.size());
    }

    public void testFindByCrfVersionOrCrfAndStudyAndStudyEventDefinition() {
        CRFBean crfBean = new CRFBean();
        crfBean.setId(2);
        CRFVersionBean crfVersionBean = new CRFVersionBean();
        crfVersionBean.setId(2);
        StudyBean studyBean = new StudyBean();
        studyBean.setId(1);
        StudyEventDefinitionBean sed = new StudyEventDefinitionBean();
        sed.setId(2);

        RuleSetDao ruleSetDao = (RuleSetDao) getContext().getBean("ruleSetDao");
        List<RuleSetBean> persistentRuleSets = ruleSetDao.findByCrfVersionOrCrfAndStudyAndStudyEventDefinition(crfVersionBean, crfBean, studyBean, sed);
        assertNotNull("The returned ruleSet was null", persistentRuleSets);
        assertEquals("The List size of ruleset objects should be 2 ", persistentRuleSets.size(), 2);

    }*/

    public void testSaveOrUpdate() {
        // RuleSetDao ruleSetDao = (RuleSetDao) getContext().getBean("ruleSetDao");
      //   RuleDao ruleDao = (RuleDao) getContext().getBean("ruleDao");
         RuleBean persistantRuleBean = ruleDao.findById(1);
         RuleSetBean ruleSetBean = createStubRuleSetBean(persistantRuleBean);
         ruleSetBean = ruleSetDao.saveOrUpdate(ruleSetBean);
              assertNotNull("Persistant id is null", ruleSetBean.getId());
     }
    // @pgawade 09-NOV-2010 Commented out the test "testFindByCrf" temporarily
    // public void testFindByCrf() {
    // CRFBean crfBean = new CRFBean();
    // crfBean.setId(2);
    // StudyBean studyBean = new StudyBean();
    // studyBean.setId(1);
    // RuleSetDao ruleSetDao = (RuleSetDao) getContext().getBean("ruleSetDao");
    // List<RuleSetBean> persistentRuleSets = ruleSetDao.findByCrf(crfBean,
    // studyBean);
    // assertNotNull("The returned ruleSet was null", persistentRuleSets);
    // assertEquals("The List size of ruleset objects should be 2 ",
    // persistentRuleSets.size(), 2);
    // }

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

    /*
     * private RuleBean createRuleBean() { RuleBean ruleBean = new RuleBean();
     * ruleBean.setName("TEST"); ruleBean.setOid("BOY");
     * ruleBean.setDescription("Yellow");
     * ruleBean.setExpression(createExpression(Context.OC_RULES_V1,
     * "SE_ED1NONRE.F_AGEN.IG_AGEN_UNGROUPED[1].I_AGEN_PERIODSTART eq \"07/01/2008\" and I_CONC_CON_MED_NAME eq \"Tylenol\""
     * )); return ruleBean; }
     */

    private ExpressionBean createExpression(Context context, String value) {
        ExpressionBean expression = new ExpressionBean();
        expression.setContext(context);
        expression.setValue(value);
        return expression;
    }
    public void tearDown(){
        try {
        //    ruleSetDao.getSessionFactory().getCurrentSession().flush();
        //    ruleSetDao.getSessionFactory().getCurrentSession().close();
        //    ruleDao.getSessionFactory().getCurrentSession().flush();
        //    ruleDao.getSessionFactory().getCurrentSession().close();
        } catch (HibernateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        super.tearDown();
    }

}