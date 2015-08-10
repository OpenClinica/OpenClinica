package org.akaza.openclinica.domain.rule.action;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.logic.rulerunner.ExecutionMode;
import org.akaza.openclinica.logic.rulerunner.RuleRunner.RuleRunnerMode;
import org.akaza.openclinica.service.crfdata.DynamicsMetadataService;

import javax.sql.DataSource;

public class HideActionProcessor implements ActionProcessor {

    DataSource ds;
    DynamicsMetadataService dynamicsMetadataService;
    RuleSetBean ruleSet;

    public HideActionProcessor(DataSource ds, DynamicsMetadataService dynamicsMetadataService, RuleSetBean ruleSet) {
        this.dynamicsMetadataService = dynamicsMetadataService;
        this.ds = ds;
        this.ruleSet = ruleSet;
    }

    public RuleActionBean execute(RuleRunnerMode ruleRunnerMode, ExecutionMode executionMode, RuleActionBean ruleAction, ItemDataBean itemDataBean,
            String itemData, StudyBean currentStudy, UserAccountBean ub, Object... arguments) {

        switch (executionMode) {
        case DRY_RUN: {
            if (ruleRunnerMode == RuleRunnerMode.DATA_ENTRY || ruleRunnerMode == RuleRunnerMode.RUN_ON_SCHEDULE) {
                return null;
            } else {
                dryRun(ruleAction, itemDataBean, itemData, currentStudy, ub);
            }
        }
        case SAVE: {
            if (ruleRunnerMode == RuleRunnerMode.DATA_ENTRY) {
                return saveAndReturnMessage(ruleAction, itemDataBean, itemData, currentStudy, ub);
            }else if (ruleRunnerMode == RuleRunnerMode.RUN_ON_SCHEDULE) {
                    return null;
            } else {
                return save(ruleAction, itemDataBean, itemData, currentStudy, ub);
            }
        }
        default:
            return null;
        }
    }

    private RuleActionBean save(RuleActionBean ruleAction, ItemDataBean itemDataBean, String itemData, StudyBean currentStudy, UserAccountBean ub) {
        getDynamicsMetadataService().hideNew(itemDataBean.getId(), ((HideActionBean) ruleAction).getProperties(), ub, ruleSet);
        return ruleAction;
    }

    private RuleActionBean saveAndReturnMessage(RuleActionBean ruleAction, ItemDataBean itemDataBean, String itemData, StudyBean currentStudy,
            UserAccountBean ub) {
        getDynamicsMetadataService().hideNew(itemDataBean.getId(), ((HideActionBean) ruleAction).getProperties(), ub, ruleSet);
        return ruleAction;
    }

    private RuleActionBean dryRun(RuleActionBean ruleAction, ItemDataBean itemDataBean, String itemData, StudyBean currentStudy, UserAccountBean ub) {
        return ruleAction;
    }

    private DynamicsMetadataService getDynamicsMetadataService() {
        return dynamicsMetadataService;
    }

}
