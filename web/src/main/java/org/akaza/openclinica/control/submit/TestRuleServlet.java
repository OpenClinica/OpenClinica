/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research
 */
package org.akaza.openclinica.control.submit;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.rule.FileUploadHelper;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.bean.rule.XmlSchemaValidationHelper;
import org.akaza.openclinica.domain.rule.expression.Context;
import org.akaza.openclinica.domain.rule.expression.ExpressionBean;
import org.akaza.openclinica.domain.rule.expression.ExpressionObjectWrapper;
import org.akaza.openclinica.domain.rule.expression.ExpressionProcessor;
import org.akaza.openclinica.domain.rule.expression.ExpressionProcessorFactory;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.service.rule.RuleSetServiceInterface;
import org.akaza.openclinica.service.rule.RulesPostImportContainerService;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

/**
 * Verify the Rule import , show records that have Errors as well as records
 * that will be saved.
 * 
 * @author Krikor krumlian
 */
public class TestRuleServlet extends SecureController {

    private static final long serialVersionUID = 9116068126651934226L;
    protected final Logger log = LoggerFactory.getLogger(TestRuleServlet.class);

    Locale locale;
    FileUploadHelper uploadHelper = new FileUploadHelper();
    XmlSchemaValidationHelper schemaValidator = new XmlSchemaValidationHelper();
    RuleSetServiceInterface ruleSetService;
    RulesPostImportContainerService rulesPostImportContainerService;

    @Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        String action = request.getParameter("action");

        if (StringUtil.isBlank(action)) {
            request.setAttribute("result", resword.getString("test_rule_default_result"));
            forwardPage(Page.TEST_RULES);
        } else {

            String targetString = fp.getString("target");
            String ruleString = fp.getString("rule");
            RuleSetBean ruleSet = new RuleSetBean();
            ExpressionBean target = new ExpressionBean();
            target.setContext(Context.OC_RULES_V1);
            target.setValue(targetString);
            ruleSet.setTarget(target);

            ExpressionBean rule = new ExpressionBean();
            rule.setContext(Context.OC_RULES_V1);
            rule.setValue(ruleString);

            ExpressionObjectWrapper eow = new ExpressionObjectWrapper(sm.getDataSource(), currentStudy, rule, ruleSet);
            ExpressionProcessor ep = ExpressionProcessorFactory.createExpressionProcessor(eow);
            String result = ep.testEvaluateExpression();

            request.setAttribute("result", result);
            session.setAttribute("target", targetString);
            session.setAttribute("rule", ruleString);

            forwardPage(Page.TEST_RULES);
        }
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
        locale = request.getLocale();
        if (ub.isSysAdmin()) {
            return;
        }
        Role r = currentRole.getRole();
        if (r.equals(Role.STUDYDIRECTOR) || r.equals(Role.COORDINATOR) || r.equals(Role.INVESTIGATOR) || r.equals(Role.RESEARCHASSISTANT)) {
            return;
        }
        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("may_not_submit_data"), "1");
    }

    private RuleSetServiceInterface getRuleSetService() {
        ruleSetService =
            this.ruleSetService != null ? ruleSetService : (RuleSetServiceInterface) SpringServletAccess.getApplicationContext(context).getBean(
                    "ruleSetService");
        // TODO: Add getRequestURLMinusServletPath(),getContextPath()
        return ruleSetService;
    }

}