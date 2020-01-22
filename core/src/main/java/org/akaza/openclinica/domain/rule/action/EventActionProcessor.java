/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
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
