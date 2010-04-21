package org.akaza.openclinica.domain.rule.action;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.dao.hibernate.RuleActionRunLogDao;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.akaza.openclinica.logic.rulerunner.ExecutionMode;
import org.akaza.openclinica.logic.rulerunner.RuleRunner.RuleRunnerMode;
import org.akaza.openclinica.service.managestudy.DiscrepancyNoteService;

import javax.sql.DataSource;

public class DiscrepancyNoteActionProcessor implements ActionProcessor {

    DataSource ds;
    DiscrepancyNoteService discrepancyNoteService;
    RuleActionRunLogDao ruleActionRunLogDao;
    RuleSetRuleBean ruleSetRule;

    public DiscrepancyNoteActionProcessor(DataSource ds, RuleActionRunLogDao ruleActionRunLogDao, RuleSetRuleBean ruleSetRule) {
        this.ds = ds;
        this.ruleActionRunLogDao = ruleActionRunLogDao;
        this.ruleSetRule = ruleSetRule;
    }

    public RuleActionBean execute(RuleRunnerMode ruleRunnerMode, ExecutionMode executionMode, RuleActionBean ruleAction, ItemDataBean itemDataBean,
            String itemData, StudyBean currentStudy, UserAccountBean ub, Object... arguments) {
        switch (executionMode) {
        case DRY_RUN: {
            return dryRun(ruleAction, itemDataBean, itemData, currentStudy, ub);
        }

        case SAVE: {
            return save(ruleAction, itemDataBean, itemData, currentStudy, ub);
        }
        default:
            return null;
        }
    }

    private RuleActionBean save(RuleActionBean ruleAction, ItemDataBean itemDataBean, String itemData, StudyBean currentStudy, UserAccountBean ub) {
        getDiscrepancyNoteService().saveFieldNotes(ruleAction.getCuratedMessage(), itemDataBean.getId(), itemData, currentStudy, ub);
        RuleActionRunLogBean ruleActionRunLog =
            new RuleActionRunLogBean(ruleAction.getActionType(), itemDataBean, itemDataBean.getValue(), ruleSetRule.getRuleBean().getOid());
        ruleActionRunLogDao.saveOrUpdate(ruleActionRunLog);
        return null;
    }

    private RuleActionBean dryRun(RuleActionBean ruleAction, ItemDataBean itemDataBean, String itemData, StudyBean currentStudy, UserAccountBean ub) {
        return ruleAction;
    }

    public void execute(String message, int itemDataBeanId, String itemData, StudyBean currentStudy, UserAccountBean ub, Object... arguments) {
        getDiscrepancyNoteService().saveFieldNotes(message, itemDataBeanId, itemData, currentStudy, ub);
    }

    private DiscrepancyNoteService getDiscrepancyNoteService() {
        discrepancyNoteService = this.discrepancyNoteService != null ? discrepancyNoteService : new DiscrepancyNoteService(ds);
        return discrepancyNoteService;
    }

}
