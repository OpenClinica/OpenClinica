package org.akaza.openclinica.domain.rule.action;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.logic.rulerunner.ExecutionMode;
import org.akaza.openclinica.logic.rulerunner.RuleRunner.RuleRunnerMode;

/**
 * 
 * @author jnyayapathi
 *
 */
public class EventActionProcessor implements ActionProcessor {

	@Override
	public RuleActionBean execute(RuleRunnerMode ruleRunnerMode,
			ExecutionMode executionMode, RuleActionBean ruleAction,
			ItemDataBean itemDataBean, String itemData, StudyBean currentStudy,
			UserAccountBean ub, Object... arguments) {
		// TODO Auto-generated method stub
		return null;
	}

}
