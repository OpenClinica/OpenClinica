package core.org.akaza.openclinica.bean.rule.action;

import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.domain.datamap.Study;

public interface ActionProcessor {

    public void execute(RuleActionBean ruleAction, int itemDataBeanId, String itemData, Study currentStudy, UserAccountBean ub, Object... arguments);
}
