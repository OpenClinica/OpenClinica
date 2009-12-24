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
import org.akaza.openclinica.bean.rule.XmlSchemaValidationHelper;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.service.rule.RuleSetServiceInterface;
import org.akaza.openclinica.service.rule.RulesPostImportContainerService;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.domain.EntityBeanTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Verify the Rule import , show records that have Errors as well as records
 * that will be saved.
 * 
 * @author Krikor krumlian
 */
public class ViewRuleAssignmentServlet extends SecureController {

    private static final long serialVersionUID = 9116068126651934226L;
    protected final Logger log = LoggerFactory.getLogger(ViewRuleAssignmentServlet.class);

    Locale locale;
    FileUploadHelper uploadHelper = new FileUploadHelper();
    XmlSchemaValidationHelper schemaValidator = new XmlSchemaValidationHelper();
    RuleSetServiceInterface ruleSetService;
    RulesPostImportContainerService rulesPostImportContainerService;

    @Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);

        List<RuleSetBean> ruleSets = getRuleSetService().getRuleSetsByStudy(currentStudy);
        ruleSets = getRuleSetService().filterByStatusEqualsAvailableOnlyRuleSetRules(ruleSets);

        EntityBeanTable table = fp.getWebEntityBeanTable();
        ArrayList allRows = ViewRuleAssignmentRow.generateRowsFromBeans((ArrayList) ruleSets);

        String[] columns =
            { resword.getString("rule_study_event_definition"), resword.getString("CRF_name"), resword.getString("rule_group_label"),
                resword.getString("rule_item_name"), resword.getString("rule_rules"), resword.getString("rule_action_type"), resword.getString("actions") };

        table.setColumns(new ArrayList(Arrays.asList(columns)));
        table.hideColumnLink(4);
        table.hideColumnLink(5);
        table.hideColumnLink(6);
        table.setQuery("ViewRuleAssignment", new HashMap());
        // table.addLink(resword.getString("rule_import_rule"), "ImportRule");
        table.addLink(resword.getString("test_rule_title"), "TestRule");
        table.setRows(allRows);
        table.computeDisplay();

        request.setAttribute("table", table);

        if (request.getParameter("read") != null && request.getParameter("read").equals("true")) {
            request.setAttribute("readOnly", true);
        }

        resetPanel();
        panel.setStudyInfoShown(false);
        panel.setOrderedData(true);
        panel.setSubmitDataModule(false);
        panel.setExtractData(false);
        panel.setCreateDataset(false);

        if (ruleSets.size() > 0) {
            setToPanel("CRFs", new Integer(ruleSets.size()).toString());
        }

        setToPanel(resword.getString("create_CRF"), respage.getString("br_create_new_CRF_entering"));
        setToPanel(resword.getString("create_CRF_version"), respage.getString("br_create_new_CRF_uploading"));
        setToPanel(resword.getString("revise_CRF_version"), respage.getString("br_if_you_owner_CRF_version"));
        setToPanel(resword.getString("CRF_spreadsheet_template"), respage.getString("br_download_blank_CRF_spreadsheet_from"));
        setToPanel(resword.getString("example_CRF_br_spreadsheets"), respage.getString("br_download_example_CRF_instructions_from"));
        forwardPage(Page.VIEW_RULE_SETS);
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