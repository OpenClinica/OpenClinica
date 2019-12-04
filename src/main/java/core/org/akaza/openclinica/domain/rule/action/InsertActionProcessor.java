package core.org.akaza.openclinica.domain.rule.action;

import core.org.akaza.openclinica.bean.core.Status;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.submit.ItemDataBean;
import core.org.akaza.openclinica.dao.hibernate.RuleActionRunLogDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.rule.RuleSetBean;
import core.org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import core.org.akaza.openclinica.logic.rulerunner.ExecutionMode;
import core.org.akaza.openclinica.logic.rulerunner.RuleRunner.RuleRunnerMode;
import core.org.akaza.openclinica.service.crfdata.DynamicsMetadataService;

import javax.sql.DataSource;

public class InsertActionProcessor implements ActionProcessor {

    DataSource ds;
    DynamicsMetadataService itemMetadataService;
    RuleActionRunLogDao ruleActionRunLogDao;
    RuleSetBean ruleSet;
    RuleSetRuleBean ruleSetRule;

    public InsertActionProcessor(DataSource ds, DynamicsMetadataService itemMetadataService, RuleActionRunLogDao ruleActionRunLogDao, RuleSetBean ruleSet,
            RuleSetRuleBean ruleSetRule) {
        this.itemMetadataService = itemMetadataService;
        this.ruleSet = ruleSet;
        this.ruleSetRule = ruleSetRule;
        this.ruleActionRunLogDao = ruleActionRunLogDao;
        this.ds = ds;
    }

    public RuleActionBean execute(RuleRunnerMode ruleRunnerMode, ExecutionMode executionMode, RuleActionBean ruleAction, ItemDataBean itemDataBean,
                                  String itemData, Study currentStudy, UserAccountBean ub, Object... arguments) {

        switch (executionMode) {
        case DRY_RUN: {
            if (ruleRunnerMode == RuleRunnerMode.DATA_ENTRY) {
                return null;
            } else {
                dryRun(ruleAction, itemDataBean, itemData, currentStudy, ub);
            }
        }
        case SAVE: {
            if (ruleRunnerMode == RuleRunnerMode.DATA_ENTRY) {
                save(ruleAction, itemDataBean, itemData, currentStudy, ub);
            } else if(ruleRunnerMode == RuleRunnerMode.IMPORT_DATA) {
                saveWithStatusUpdated(ruleAction, itemDataBean, itemData, currentStudy, ub);
            } else {
                save(ruleAction, itemDataBean, itemData, currentStudy, ub);
            }
        }
        default:
            return null;
        }
    }

    private RuleActionBean saveWithStatusUpdated(RuleActionBean ruleAction, ItemDataBean itemDataBean, String itemData, Study currentStudy, UserAccountBean ub) {
        itemDataBean.setStatus(Status.UNAVAILABLE);
        getItemMetadataService().insert(itemDataBean, ((InsertActionBean) ruleAction).getProperties(), ub, ruleSet,null);
        ruleActionRunLogSaveOrUpdate(ruleAction, itemDataBean, itemData, currentStudy, ub);
        return null;
    }

    private RuleActionBean save(RuleActionBean ruleAction, ItemDataBean itemDataBean, String itemData, Study currentStudy, UserAccountBean ub) {
        getItemMetadataService().insert(itemDataBean.getId(), ((InsertActionBean) ruleAction).getProperties(), ub, ruleSet,null);
        ruleActionRunLogSaveOrUpdate(ruleAction, itemDataBean, itemData, currentStudy, ub);
        return null;
    }

    private void ruleActionRunLogSaveOrUpdate(RuleActionBean ruleAction, ItemDataBean itemDataBean, String itemData, Study currentStudy, UserAccountBean ub) {
        RuleActionRunLogBean ruleActionRunLog =
                new RuleActionRunLogBean(ruleAction.getActionType(), itemDataBean, itemDataBean.getValue(), ruleSetRule.getRuleBean().getOid());
        if (ruleActionRunLogDao.findCountByRuleActionRunLogBean(ruleActionRunLog) > 0) {
        } else {
            ruleActionRunLogDao.saveOrUpdate(ruleActionRunLog);
        }
    }

    private RuleActionBean saveAndReturnMessage(RuleActionBean ruleAction, ItemDataBean itemDataBean, String itemData, Study currentStudy,
            UserAccountBean ub) {
        //
        return ruleAction;
    }

    private RuleActionBean dryRun(RuleActionBean ruleAction, ItemDataBean itemDataBean, String itemData, Study currentStudy, UserAccountBean ub) {
        return ruleAction;
    }

    private DynamicsMetadataService getItemMetadataService() {
        return itemMetadataService;
    }

}
