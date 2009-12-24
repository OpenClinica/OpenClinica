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
import org.akaza.openclinica.dao.rule.RuleSetDAO;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.service.rule.RuleSetServiceInterface;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

/**
 * @author Krikor Krumlian
 *
 */
public class RestoreRuleSetServlet extends SecureController {

    private static final long serialVersionUID = 1L;
    RuleSetDAO ruleSetDao;
    RuleSetServiceInterface ruleSetService;

    private static String RULESET_ID = "ruleSetId";
    private static String RULESET = "ruleSet";
    private static String ACTION = "action";

    @Override
    public void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }

        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.LIST_DEFINITION_SERVLET, resexception.getString("not_study_director"), "1");

    }

    @Override
    public void processRequest() throws Exception {
        String ruleSetId = request.getParameter(RULESET_ID);
        String action = request.getParameter(ACTION);
        if (ruleSetId == null) {
            addPageMessage(respage.getString("please_choose_a_CRF_to_view"));
            forwardPage(Page.CRF_LIST);
        } else {
            RuleSetBean ruleSetBean = null;
            // ruleSetBean = getRuleSetService().getRuleSetById(currentStudy, ruleSetId, null);
            ruleSetBean = getRuleSetService().getRuleSetById(currentStudy, ruleSetId);
            if (action.equals("confirm")) {
                request.setAttribute(RULESET, ruleSetBean);
                forwardPage(Page.RESTORE_RULE_SET);
            } else {
                // getRuleSetDao().restore(ruleSetBean, ub);
                // getRuleSetService().restore(ruleSetBean, ub);
                getRuleSetService().updateRuleSet(ruleSetBean, ub, Status.AVAILABLE);
                forwardPage(Page.LIST_RULE_SETS_SERVLET);
            }
        }
    }

    private RuleSetDAO getRuleSetDao() {
        ruleSetDao = this.ruleSetDao != null ? ruleSetDao : new RuleSetDAO(sm.getDataSource());
        return ruleSetDao;
    }

    private RuleSetServiceInterface getRuleSetService() {
        ruleSetService =
            this.ruleSetService != null ? ruleSetService : (RuleSetServiceInterface) SpringServletAccess.getApplicationContext(context).getBean("ruleSetService");
        // TODO: Add getRequestURLMinusServletPath(),getContextPath()
        return ruleSetService;
    }

}
