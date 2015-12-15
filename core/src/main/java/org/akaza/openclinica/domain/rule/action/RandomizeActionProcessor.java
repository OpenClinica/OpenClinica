package org.akaza.openclinica.domain.rule.action;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.dao.hibernate.RuleActionRunLogDao;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.akaza.openclinica.logic.rulerunner.ExecutionMode;
import org.akaza.openclinica.logic.rulerunner.RuleRunner.RuleRunnerMode;
import org.akaza.openclinica.service.crfdata.DynamicsMetadataService;

import javax.sql.DataSource;

public class RandomizeActionProcessor implements ActionProcessor {

    DataSource ds;
    DynamicsMetadataService itemMetadataService;
    RuleActionRunLogDao ruleActionRunLogDao;
    RuleSetBean ruleSet;
    RuleSetRuleBean ruleSetRule;

    public RandomizeActionProcessor(DataSource ds, DynamicsMetadataService itemMetadataService, RuleActionRunLogDao ruleActionRunLogDao, RuleSetBean ruleSet,
            RuleSetRuleBean ruleSetRule) {
        this.itemMetadataService = itemMetadataService;
        this.ruleSet = ruleSet;
        this.ruleSetRule = ruleSetRule;
        this.ruleActionRunLogDao = ruleActionRunLogDao;
        this.ds = ds;
    }

    public RuleActionBean execute(RuleRunnerMode ruleRunnerMode, ExecutionMode executionMode, RuleActionBean ruleAction, ItemDataBean itemDataBean,
            String itemData, StudyBean currentStudy, UserAccountBean ub, Object... arguments) {

        switch (executionMode) {
        case DRY_RUN: {
            if (ruleRunnerMode == RuleRunnerMode.DATA_ENTRY) {
                return null;
            } else {
                return ruleAction;
            }
        }
        case SAVE: {
            if (ruleRunnerMode == RuleRunnerMode.IMPORT_DATA) {
                return saveWithStatusUpdated(ruleAction, itemDataBean, itemData, currentStudy, ub);
            } else {
                return save(ruleAction, itemDataBean, itemData, currentStudy, ub);
            }
        }
        default:
            return ruleAction;
        }
    }

    private RuleActionBean saveWithStatusUpdated(RuleActionBean ruleAction, ItemDataBean itemDataBean, String itemData, StudyBean currentStudy, UserAccountBean ub) {
        itemDataBean.setStatus(Status.UNAVAILABLE);
        getItemMetadataService().insert(itemDataBean, ((RandomizeActionBean) ruleAction).getProperties(), ub, ruleSet,((RandomizeActionBean) ruleAction).getStratificationFactors());
 //       ruleActionRunLogSaveOrUpdate(ruleAction, itemDataBean, itemData, currentStudy, ub);
        return ruleAction;
    }

    private RuleActionBean save(RuleActionBean ruleAction, ItemDataBean itemDataBean, String itemData, StudyBean currentStudy, UserAccountBean ub) {
        
        getItemMetadataService().insert(itemDataBean.getId(), ((RandomizeActionBean) ruleAction).getProperties(), ub, ruleSet ,((RandomizeActionBean) ruleAction).getStratificationFactors());
  //      ruleActionRunLogSaveOrUpdate(ruleAction, itemDataBean, itemData, currentStudy, ub);
        return ruleAction;
    }

    private void ruleActionRunLogSaveOrUpdate(RuleActionBean ruleAction, ItemDataBean itemDataBean, String itemData, StudyBean currentStudy, UserAccountBean ub) {
        RuleActionRunLogBean ruleActionRunLog =
                new RuleActionRunLogBean(ruleAction.getActionType(), itemDataBean, itemDataBean.getValue(), ruleSetRule.getRuleBean().getOid());
        if (ruleActionRunLogDao.findCountByRuleActionRunLogBean(ruleActionRunLog) > 0) {
        } else {
            ruleActionRunLogDao.saveOrUpdate(ruleActionRunLog);
        }
    }

    private RuleActionBean saveAndReturnMessage(RuleActionBean ruleAction, ItemDataBean itemDataBean, String itemData, StudyBean currentStudy,
            UserAccountBean ub) {
        //
        return ruleAction;
    }


    private DynamicsMetadataService getItemMetadataService() {
        return itemMetadataService;
    }

}
