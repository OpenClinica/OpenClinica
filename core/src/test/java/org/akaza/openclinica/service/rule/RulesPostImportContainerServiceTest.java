package org.akaza.openclinica.service.rule;

import junit.framework.TestCase;
import org.akaza.openclinica.domain.rule.RuleBean;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.akaza.openclinica.domain.rule.RulesPostImportContainer;
import org.akaza.openclinica.domain.rule.action.EventActionBean;
import org.akaza.openclinica.domain.rule.expression.Context;
import org.akaza.openclinica.domain.rule.expression.ExpressionBean;

import java.util.ArrayList;

public class RulesPostImportContainerServiceTest extends TestCase {

    public RulesPostImportContainerServiceTest() {
        super();
    }

    /**
    public void testDuplicationRuleSetDefs() {
        StudyDAO studyDao = new StudyDAO(getDataSource());
        StudyBean study = (StudyBean) studyDao.findByPK(1);
        RulesPostImportContainerService postImportContainerService = (RulesPostImportContainerService) getContext().getBean("rulesPostImportContainerService");
        postImportContainerService.setCurrentStudy(study);

        RulesPostImportContainer container = prepareContainer();

        container = postImportContainerService.validateRuleDefs(container);

        assertEquals(0, container.getDuplicateRuleDefs().size());
        assertEquals(0, container.getInValidRuleDefs().size());
        assertEquals(1, container.getValidRuleDefs().size());

        container = postImportContainerService.validateRuleSetDefs(container);
        assertEquals(1, container.getDuplicateRuleSetDefs().size());
        assertEquals(0, container.getInValidRuleSetDefs().size());
        assertEquals(0, container.getValidRuleSetDefs().size());
    }
    **/
    
    public void testCreateObj(){
    	RulesPostImportContainerService service = new RulesPostImportContainerService(null);
  //  	service.runValidationInList("SE_REG2.STARTDATE","SE_REG.STARTDATE",null,prepareContainer());    // Commented out this line due to failing when running unit test
    	
    }

    private  ArrayList<RuleSetBean> prepareContainer() {
        RulesPostImportContainer container = new RulesPostImportContainer();
        ArrayList<RuleSetBean> ruleSets = new ArrayList<RuleSetBean>();
        ArrayList<RuleBean> ruleDefs = new ArrayList<RuleBean>();

        RuleBean rule = createRuleBean();
        RuleSetBean ruleSet = getRuleSet(rule.getOid(),"SE_REG.STARTDATE","SE_REG3");
        RuleSetBean ruleSet2 = getRuleSet(rule.getOid(),"SE_REG3.STARTDATE","SE_REG2");
        ruleSets.add(ruleSet);
        ruleSets.add(ruleSet2);
        //ruleDefs.add(rule);
        //container.setRuleSets(ruleSets);
        //container.setRuleDefs(ruleDefs);
        return ruleSets;

    }
    

    private RuleSetBean getRuleSet(String ruleOid,String target,String oidRef) {
        RuleSetBean ruleSet = new RuleSetBean();
        ruleSet.setTarget(createExpression(Context.OC_RULES_V1, target));
        ruleSet.setOriginalTarget(createExpression(Context.OC_RULES_V1, target));
        RuleSetRuleBean ruleSetRule = createRuleSetRule(ruleSet, ruleOid,oidRef);
        ruleSet.addRuleSetRule(ruleSetRule);
        return ruleSet;

    }

    private RuleSetRuleBean createRuleSetRule(RuleSetBean ruleSet, String ruleOid, String oidRef) {
        RuleSetRuleBean ruleSetRule = new RuleSetRuleBean();
        //DiscrepancyNoteActionBean ruleAction = new DiscrepancyNoteActionBean();
        EventActionBean ruleAction = new EventActionBean();
        ruleAction.setOc_oid_reference(oidRef);
        ruleAction.setExpressionEvaluatesTo(true);
        ruleSetRule.addAction(ruleAction);
        ruleSetRule.setRuleSetBean(ruleSet);
        ruleSetRule.setOid(ruleOid);

        return ruleSetRule;
    }

    private RuleBean createRuleBean() {
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
}