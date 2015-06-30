/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.control.submit;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.domain.rule.RuleSetBasedViewContainer;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.service.rule.RuleSetServiceInterface;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Krikor Krumlian
 */
public class RunRuleSetServlet extends SecureController {

    private static String RULESET_ID = "ruleSetId";
    private static String RULE_ID = "ruleId";
    private static String RULESET = "ruleSet";
    private static String RULESET_RESULT = "ruleSetResult";
    private RuleSetServiceInterface ruleSetService;

    /**
     * 
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }
        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_study_director"), "1");

    }

    @Override
    public void processRequest() throws Exception {

        String ruleSetId = request.getParameter(RULESET_ID);
        String ruleId = request.getParameter(RULE_ID);
        String dryRun = request.getParameter("dryRun");

        RuleSetBean ruleSetBean = getRuleSetBean(ruleSetId, ruleId);
        if (ruleSetBean != null) {
            List<RuleSetBean> ruleSets = new ArrayList<RuleSetBean>();
            ruleSets.add(ruleSetBean);
            if (dryRun != null && dryRun.equals("no")) {
                List<RuleSetBasedViewContainer> resultOfRunningRules = getRuleSetService().runRulesInBulk(ruleSets, false, currentStudy, ub ,false);
                addPageMessage(respage.getString("actions_successfully_taken"));
                forwardPage(Page.LIST_RULE_SETS_SERVLET);

            } else {
                List<RuleSetBasedViewContainer> resultOfRunningRules = getRuleSetService().runRulesInBulk(ruleSets, true, currentStudy, ub,false);
                request.setAttribute(RULESET, ruleSetBean);
                request.setAttribute(RULESET_RESULT, resultOfRunningRules);
                if (resultOfRunningRules.size() > 0) {
                    addPageMessage(resword.getString("view_executed_rules_affected_subjects"));
                } else {
                    addPageMessage(resword.getString("view_executed_rules_no_affected_subjects"));
                }

                forwardPage(Page.VIEW_EXECUTED_RULES);

            }

        } else {
            addPageMessage("RuleSet not found");
            forwardPage(Page.LIST_RULE_SETS_SERVLET);
        }
    }

    private RuleSetBean getRuleSetBean(String ruleSetId, String ruleId) {
        RuleSetBean ruleSetBean = null;
        if (ruleId != null && ruleSetId != null && ruleId.length() > 0 && ruleSetId.length() > 0) {
            ruleSetBean = getRuleSetService().getRuleSetById(currentStudy, ruleSetId);
            ruleSetBean = ruleSetService.filterByRules(ruleSetBean, Integer.valueOf(ruleId));
        } else if (ruleSetId != null && ruleSetId.length() > 0) {
            // getRuleSetService().getRuleSetById(currentStudy, ruleSetId);
            // ruleSetBean = getRuleSetService().getRuleSetById(currentStudy, ruleSetId, null);
            ruleSetBean = getRuleSetService().getRuleSetById(currentStudy, ruleSetId);
        }
        return ruleSetBean;
    }

    @Override
    protected String getAdminServlet() {
        if (ub.isSysAdmin()) {
            return SecureController.ADMIN_SERVLET_CODE;
        } else {
            return "";
        }
    }

    private RuleSetServiceInterface getRuleSetService() {
        ruleSetService =
            this.ruleSetService != null ? ruleSetService : (RuleSetServiceInterface) SpringServletAccess.getApplicationContext(context).getBean(
                    "ruleSetService");
        ruleSetService.setContextPath(getContextPath());
        ruleSetService.setMailSender((JavaMailSenderImpl) SpringServletAccess.getApplicationContext(context).getBean("mailSender"));
        ruleSetService.setRequestURLMinusServletPath(getRequestURLMinusServletPath());
        return ruleSetService;
    }

}
