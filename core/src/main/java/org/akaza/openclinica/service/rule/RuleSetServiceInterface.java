package org.akaza.openclinica.service.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.dao.hibernate.RuleDao;
import org.akaza.openclinica.dao.hibernate.RuleSetAuditDao;
import org.akaza.openclinica.dao.hibernate.RuleSetDao;
import org.akaza.openclinica.dao.hibernate.RuleSetRuleDao;
import org.akaza.openclinica.dao.hibernate.ViewRuleAssignmentFilter;
import org.akaza.openclinica.dao.hibernate.ViewRuleAssignmentSort;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.rule.RuleBean;
import org.akaza.openclinica.domain.rule.RuleBulkExecuteContainer;
import org.akaza.openclinica.domain.rule.RuleBulkExecuteContainerTwo;
import org.akaza.openclinica.domain.rule.RuleSetBasedViewContainer;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.akaza.openclinica.domain.rule.RulesPostImportContainer;
import org.akaza.openclinica.domain.rule.action.RuleActionRunBean.Phase;
import org.akaza.openclinica.logic.rulerunner.ExecutionMode;
import org.akaza.openclinica.logic.rulerunner.ImportDataRuleRunnerContainer;
import org.akaza.openclinica.logic.rulerunner.MessageContainer;
import org.springframework.mail.javamail.JavaMailSenderImpl;

public interface RuleSetServiceInterface {

    /**
     * Now I know why ORM are pretty cool. Takes care of saving the whole object graph.
     *
     * @param ruleSetBean
     * @return
     *
     *         public RuleSetBean saveRuleSet(RuleSetBean ruleSetBean) { RuleSetBean persistentRuleSetBean = (RuleSetBean)
     *         getRuleSetDao().saveOrUpdate(ruleSetBean); // Save RuleSetRules for (RuleSetRuleBean ruleSetRule : persistentRuleSetBean.getRuleSetRules()) {
     *         ruleSetRule.setRuleSetBean(persistentRuleSetBean); getRuleSetRuleDao().saveOrUpdate(ruleSetRule); // Save Actions for (RuleActionBean action :
     *         ruleSetRule.getActions()) { action.setRuleSetRule(ruleSetRule); getRuleActionDao().saveOrUpdate(action); } } return persistentRuleSetBean; }
     */

    public abstract RuleSetBean saveRuleSet(RuleSetBean ruleSetBean);

    public abstract void saveImportFromDesigner(RulesPostImportContainer rulesContainer);

    public abstract void saveImport(RulesPostImportContainer rulesContainer);

    public void saveImport(RuleSetRuleBean ruleSetRule);

    public abstract RuleSetBean updateRuleSet(RuleSetBean ruleSetBean, UserAccountBean user, Status status);

    public abstract void loadRuleSetRuleWithPersistentRules(RuleSetBean ruleSetBean);

    public abstract RuleSetBean replaceRuleSet(RuleSetBean ruleSetBean);

    public abstract HashMap<RuleBulkExecuteContainer, HashMap<RuleBulkExecuteContainerTwo, Set<String>>> runRulesInBulk(String crfId,
            ExecutionMode executionMode, StudyBean currentStudy, UserAccountBean ub);

    public abstract HashMap<RuleBulkExecuteContainer, HashMap<RuleBulkExecuteContainerTwo, Set<String>>> runRulesInBulk(String ruleSetRuleId,
            String crfVersionId, ExecutionMode executionMode, StudyBean currentStudy, UserAccountBean ub);

    public abstract List<RuleSetBasedViewContainer> runRulesInBulk(List<RuleSetBean> ruleSets, Boolean dryRun, StudyBean currentStudy, UserAccountBean ub, boolean jobTrigger);

    public abstract MessageContainer runRulesInDataEntry(List<RuleSetBean> ruleSets, Boolean dryRun, StudyBean currentStudy, UserAccountBean ub,
            HashMap<String, String> variableAndValue, Phase phase,EventCRFBean ecb, HttpServletRequest request);

    /**
     * @param containers
     * @param study
     * @param ub
     * @param executionMode
     * @return RuleActionBean summary with key as groupOrdinalPLusItemOid.
     */
    public abstract HashMap<String, ArrayList<String>> runRulesInImportData(List<ImportDataRuleRunnerContainer> containers, StudyBean study, UserAccountBean ub, ExecutionMode executionMode);

    public abstract List<RuleSetBean> getRuleSetsByCrfStudyAndStudyEventDefinition(StudyBean study, StudyEventDefinitionBean sed, CRFVersionBean crfVersion);

    /*
     * Used to Manage RuleSets ,Hence will return all RuleSets whether removed or not
     */
    public abstract List<RuleSetBean> getRuleSetsByStudy(StudyBean study);

    public int getCountWithFilter(ViewRuleAssignmentFilter viewRuleAssignmentFilter);

    public int getCountByStudy(StudyBean study);

    public List<RuleSetRuleBean> getWithFilterAndSort(ViewRuleAssignmentFilter viewRuleAssignmentFilter, ViewRuleAssignmentSort viewRuleAssignmentSort,
            int rowStart, int rowEnd);

    // . TODO: why are we including study but not using it in query
    public abstract RuleSetBean getRuleSetById(StudyBean study, String id);

    public abstract List<RuleSetRuleBean> getRuleSetById(StudyBean study, String id, RuleBean ruleBean);

    public abstract List<RuleSetBean> getRuleSetsByCrfAndStudy(CRFBean crfBean, StudyBean study);

    public abstract List<RuleSetBean> filterByStatusEqualsAvailableOnlyRuleSetRules(List<RuleSetBean> ruleSets);

    public abstract List<RuleSetBean> filterByStatusEqualsAvailable(List<RuleSetBean> ruleSets);

    public abstract RuleSetBean filterByRules(RuleSetBean ruleSet, Integer ruleBeanId);

    public RuleSetBean getObjects(RuleSetBean ruleSetBean);

    /**
     * Use in DataEntry Rule Execution Scenarios
     *
     * A RuleSet has a Target with Value which is provided by rule Creator. value might be :
     * SE_TESTINGF[ALL].F_AGEN_8_V204.IG_AGEN_DOSETABLE6[ALL].I_AGEN_DOSEDATE64 OR SE_TESTINGF[1].F_AGEN_8_V204.IG_AGEN_DOSETABLE6[ALL].I_AGEN_DOSEDATE64 OR
     * SE_TESTINGF.F_AGEN_8_V204.IG_AGEN_DOSETABLE6[ALL].I_AGEN_DOSEDATE64 in which case it would need to be transformed to
     * SE_TESTINGF[x].F_AGEN_8_V204.IG_AGEN_DOSETABLE6[ALL].I_AGEN_DOSEDATE64 where x is the studyEventId.
     *
     * @param ruleSets
     * @param studyEvent
     * @param crfVersion
     *            TODO
     * @param studyEventDefinition
     *            TODO
     * @return
     */
    public abstract List<RuleSetBean> filterRuleSetsByStudyEventOrdinal(List<RuleSetBean> ruleSets, StudyEventBean studyEvent, CRFVersionBean crfVersion,
            StudyEventDefinitionBean studyEventDefinition);

    @SuppressWarnings("unchecked")
    public abstract List<RuleSetBean> filterRuleSetsByStudyEventOrdinal(List<RuleSetBean> ruleSets, String crfVersionId);

    /**
     * Iterate over ruleSet.getExpressions(). Given the following expression SE_TESTINGF[studyEventId].F_AGEN_8_V204.IG_AGEN_DOSETABLE6[X].I_AGEN_DOSEDATE64 X
     * could be : ALL , "" , Number if ALL or "" then iterate over all group ordinals if they exist and add. if Number just add the number
     *
     * @param ruleSets
     * @param grouped
     * @return
     */
    public abstract List<RuleSetBean> solidifyGroupOrdinalsUsingFormProperties(List<RuleSetBean> ruleSets, HashMap<String, Integer> grouped);

    public abstract List<RuleSetBean> filterRuleSetsBySectionAndGroupOrdinal(List<RuleSetBean> ruleSets, HashMap<String, Integer> grouped);

    /**
     * Iterate over rulesets and remove those which are currently hidden.
     * @param allItems
     */
    public abstract List<RuleSetBean> filterRuleSetsByHiddenItems(List<RuleSetBean> ruleSets, EventCRFBean eventCrf, CRFVersionBean crfVersion, List<ItemBean> itemBeansWithSCDShown);

    /**
     * Iterate over ruleSet.getExpressions(). Given the following expression SE_TESTINGF[studyEventId].F_AGEN_8_V204.IG_AGEN_DOSETABLE6[X].I_AGEN_DOSEDATE64 X
     * could be : ALL , "" , Number case 1 : if "" then iterate over itemDatas if they exist add. case 2 : if Number just add the number
     *
     * @param ruleSets
     * @param grouped
     * @return
     */
    public abstract List<RuleSetBean> filterRuleSetsByGroupOrdinal(List<RuleSetBean> ruleSets);

    public abstract List<String> getGroupOrdinalPlusItemOids(List<RuleSetBean> ruleSets);

    public abstract RuleSetBean replaceCrfOidInTargetExpression(RuleSetBean ruleSetBean, String replacementCrfOid);

    public String getContextPath();

    public void setContextPath(String contextPath);

    public void setRequestURLMinusServletPath(String requestURLMinusServletPath);

    public abstract String getRequestURLMinusServletPath();

    /**
     * @return the ruleSetDao
     */
    public abstract RuleSetDao getRuleSetDao();

    /**
     * @param ruleSetDao
     *            the ruleSetDao to set
     */
    public abstract void setRuleSetDao(RuleSetDao ruleSetDao);

    /**
     * @param ruleSetRuleDao
     *            the ruleSetRuleDao to set
     */
    public abstract void setRuleSetRuleDao(RuleSetRuleDao ruleSetRuleDao);

    /**
     * @return the ruleSetRuleDao
     */
    public abstract RuleSetRuleDao getRuleSetRuleDao();

    /**
     * @return the ruleDao
     */
    public abstract RuleDao getRuleDao();

    /**
     * @param ruleDao
     *            the ruleDao to set
     */
    public abstract void setRuleDao(RuleDao ruleDao);

    public RuleSetAuditDao getRuleSetAuditDao();

    public void setRuleSetAuditDao(RuleSetAuditDao ruleSetAuditDao);

    public JavaMailSenderImpl getMailSender();

    public void setMailSender(JavaMailSenderImpl mailSender);

    /**
     * Return true if there is at least one rule should be run for a phase.
     *
     * @param ruleSets
     * @param phase
     * @return
     */
    public boolean shouldRunRulesForRuleSets(List<RuleSetBean> ruleSets, Phase phase);

}