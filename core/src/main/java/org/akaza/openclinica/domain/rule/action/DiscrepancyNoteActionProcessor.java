package org.akaza.openclinica.domain.rule.action;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.logic.rulerunner.ExecutionMode;
import org.akaza.openclinica.logic.rulerunner.RuleRunner.RuleRunnerMode;
import org.akaza.openclinica.service.managestudy.DiscrepancyNoteService;

import javax.sql.DataSource;

public class DiscrepancyNoteActionProcessor implements ActionProcessor {

    DataSource ds;
    DiscrepancyNoteService discrepancyNoteService;

    public DiscrepancyNoteActionProcessor(DataSource ds) {
        this.ds = ds;
    }

    public RuleActionBean execute(RuleRunnerMode ruleRunnerMode, ExecutionMode executionMode, RuleActionBean ruleAction, int itemDataBeanId, String itemData,
            StudyBean currentStudy, UserAccountBean ub, Object... arguments) {
        switch (executionMode) {
        case DRY_RUN: {
            return dryRun(ruleAction, itemDataBeanId, itemData, currentStudy, ub);
        }

        case SAVE: {
            return save(ruleAction, itemDataBeanId, itemData, currentStudy, ub);
        }
        default:
            return null;
        }
    }

    private RuleActionBean save(RuleActionBean ruleAction, int itemDataBeanId, String itemData, StudyBean currentStudy, UserAccountBean ub) {
        getDiscrepancyNoteService().saveFieldNotes(ruleAction.getCuratedMessage(), itemDataBeanId, itemData, currentStudy, ub);
        return null;
    }

    private RuleActionBean saveAndReturnMessage(RuleActionBean ruleAction, int itemDataBeanId, String itemData, StudyBean currentStudy, UserAccountBean ub) {
        getDiscrepancyNoteService().saveFieldNotes(ruleAction.getCuratedMessage(), itemDataBeanId, itemData, currentStudy, ub);
        return ruleAction;
    }

    private RuleActionBean dryRun(RuleActionBean ruleAction, int itemDataBeanId, String itemData, StudyBean currentStudy, UserAccountBean ub) {
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
