/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research
 */
package org.akaza.openclinica.control.submit;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.rule.XmlSchemaValidationHelper;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.domain.rule.RuleBulkExecuteContainer;
import org.akaza.openclinica.domain.rule.RuleBulkExecuteContainerTwo;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.logic.rulerunner.ExecutionMode;
import org.akaza.openclinica.service.rule.RuleSetServiceInterface;
import org.akaza.openclinica.service.rule.RulesPostImportContainerService;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

/**
 * Run Rules Using this Servlet
 *
 * @author Krikor krumlian
 */
public class RunRuleServlet extends SecureController {
    private static final long serialVersionUID = 9116068126651934226L;
    protected final Logger log = LoggerFactory.getLogger(RunRuleServlet.class);

    Locale locale;
    XmlSchemaValidationHelper schemaValidator = new XmlSchemaValidationHelper();
    RuleSetServiceInterface ruleSetService;
    RulesPostImportContainerService rulesPostImportContainerService;

    @Override
    public void processRequest() throws Exception {
        String action = request.getParameter("action");
        String crfId = request.getParameter("crfId");
        String ruleSetRuleId = request.getParameter("ruleSetRuleId");
        String versionId = request.getParameter("versionId");

        if (StringUtil.isBlank(action)) {
            // TODO : if someone tampers with URL catch here and forwar to correct place
            forwardPage(Page.MENU_SERVLET);
        }

        //Boolean dryRun = action == null || "dryRun".equalsIgnoreCase(action) ? true : false;
        ExecutionMode executionMode = action == null || "dryRun".equalsIgnoreCase(action) ? ExecutionMode.DRY_RUN : ExecutionMode.SAVE;
        String submitLinkParams = "";

        HashMap<RuleBulkExecuteContainer, HashMap<RuleBulkExecuteContainerTwo, Set<String>>> result = null;
        if (ruleSetRuleId != null && versionId != null) {
            submitLinkParams = "ruleSetRuleId=" + ruleSetRuleId + "&versionId=" + versionId + "&action=no";
            result = getRuleSetService().runRulesInBulk(ruleSetRuleId, versionId, executionMode, currentStudy, ub);
        } else {
            submitLinkParams = "crfId=" + crfId + "&action=no";
            result = getRuleSetService().runRulesInBulk(crfId, executionMode, currentStudy, ub);
        }

        request.setAttribute("result", result);
        request.setAttribute("submitLinkParams", submitLinkParams);
        if (executionMode == ExecutionMode.SAVE) {
            forwardPage(Page.LIST_RULE_SETS_SERVLET);
        } else {
            forwardPage(Page.VIEW_EXECUTED_RULES_FROM_CRF);
        }
    }

    private RuleSetServiceInterface getRuleSetService() {
        ruleSetService =
            this.ruleSetService != null ? ruleSetService : (RuleSetServiceInterface) SpringServletAccess.getApplicationContext(context).getBean(
                    "ruleSetService");
        ruleSetService.setMailSender((JavaMailSenderImpl) SpringServletAccess.getApplicationContext(context).getBean("mailSender"));
        ruleSetService.setContextPath(getContextPath());
        ruleSetService.setRequestURLMinusServletPath(getRequestURLMinusServletPath());
        return ruleSetService;
    }

    @Override
    protected String getAdminServlet() {
        if (ub.isSysAdmin()) {
            return SecureController.ADMIN_SERVLET_CODE;
        } else {
            return "";
        }
    }

    @Override
    public void mayProceed() throws InsufficientPermissionException {
        locale = LocaleResolver.getLocale(request);
        if (ub.isSysAdmin()) {
            return;
        }
        Role r = currentRole.getRole();
        if (r.equals(Role.STUDYDIRECTOR) || r.equals(Role.COORDINATOR)) {
            return;
        }
        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("may_not_submit_data"), "1");
    }
}