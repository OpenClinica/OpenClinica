package core.org.akaza.openclinica.domain.rule.action;

import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.submit.ItemDataBean;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.logic.rulerunner.ExecutionMode;
import core.org.akaza.openclinica.logic.rulerunner.RuleRunner.RuleRunnerMode;

/**
 * 
 * @author jnyayapathi
 *
 */
public class EventActionProcessor implements ActionProcessor {

	@Override
	public RuleActionBean execute(RuleRunnerMode ruleRunnerMode,
			ExecutionMode executionMode, RuleActionBean ruleAction,
			ItemDataBean itemDataBean, String itemData, Study currentStudy,
			UserAccountBean ub, Object... arguments) {
		// TODO Auto-generated method stub
		return null;
	}

}
