package core.org.akaza.openclinica.bean.rule.action;

import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.StudyBean;

public interface ActionProcessor {

    public void execute(RuleActionBean ruleAction, int itemDataBeanId, String itemData, StudyBean currentStudy, UserAccountBean ub, Object... arguments);
}
