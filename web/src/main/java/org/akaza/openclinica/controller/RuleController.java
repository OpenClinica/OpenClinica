package org.akaza.openclinica.controller;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.hibernate.RuleSetRuleDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.domain.rule.AuditableBeanWrapper;
import org.akaza.openclinica.domain.rule.RuleBean;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.akaza.openclinica.domain.rule.RulesPostImportContainer;
import org.akaza.openclinica.domain.rule.action.DiscrepancyNoteActionBean;
import org.akaza.openclinica.domain.rule.action.EmailActionBean;
import org.akaza.openclinica.domain.rule.action.HideActionBean;
import org.akaza.openclinica.domain.rule.action.InsertActionBean;
import org.akaza.openclinica.domain.rule.action.PropertyBean;
import org.akaza.openclinica.domain.rule.action.ShowActionBean;
import org.akaza.openclinica.domain.rule.expression.Context;
import org.akaza.openclinica.domain.rule.expression.ExpressionBean;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.rule.RuleSetServiceInterface;
import org.akaza.openclinica.service.rule.RulesPostImportContainerService;
import org.apache.commons.dbcp.BasicDataSource;
import org.openclinica.ns.response.v31.MessagesType;
import org.openclinica.ns.response.v31.Response;
import org.openclinica.ns.rules.v31.DiscrepancyNoteActionType;
import org.openclinica.ns.rules.v31.EmailActionType;
import org.openclinica.ns.rules.v31.HideActionType;
import org.openclinica.ns.rules.v31.InsertActionType;
import org.openclinica.ns.rules.v31.PropertyType;
import org.openclinica.ns.rules.v31.ShowActionType;
import org.openclinica.ns.rules.v31.TargetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping(value = "/rule")
public class RuleController {

    @Autowired
    @Qualifier("dataSource")
    private BasicDataSource dataSource;
    private RuleSetRuleDao ruleSetRuleDao;
    private RuleSetServiceInterface ruleSetService;
    private RulesPostImportContainerService rulesPostImportContainerService;
    private MessageSource messageSource;
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private RulesPostImportContainer mapRulesToRulesPostImportContainer(org.openclinica.ns.rules.v31.Rules rules) {
        RulesPostImportContainer rpic = new RulesPostImportContainer();
        TargetType targetType = rules.getRuleAssignment().get(0).getTarget();
        ExpressionBean targetBean = new ExpressionBean(Context.OC_RULES_V1, targetType.getValue());
        RuleSetBean ruleSetBean = new RuleSetBean();
        ruleSetBean.setOriginalTarget(targetBean);

        // Creating rule definition & populating
        RuleBean ruleBean = new RuleBean();
        ExpressionBean ruleExpressionBean = new ExpressionBean(Context.OC_RULES_V1, rules.getRuleDef().get(0).getExpression().getValue());
        ruleBean.setExpression(ruleExpressionBean);
        ruleBean.setDescription(rules.getRuleDef().get(0).getDescription());
        ruleBean.setName(rules.getRuleDef().get(0).getName());
        ruleBean.setOid(rules.getRuleDef().get(0).getOID());

        RuleSetRuleBean ruleSetRuleBean = new RuleSetRuleBean();
        ruleSetRuleBean.setOid(rules.getRuleAssignment().get(0).getRuleRef().get(0).getOID());

        for (DiscrepancyNoteActionType discrepancyNoteActionType : rules.getRuleAssignment().get(0).getRuleRef().get(0).getDiscrepancyNoteAction()) {
            DiscrepancyNoteActionBean action = new DiscrepancyNoteActionBean();
            action.setMessage(discrepancyNoteActionType.getMessage());
            action.setExpressionEvaluatesTo(Boolean.valueOf(discrepancyNoteActionType.getIfExpressionEvaluates()));
            action.getRuleActionRun().setInitialDataEntry(discrepancyNoteActionType.getRun().isInitialDataEntry());
            action.getRuleActionRun().setDoubleDataEntry(discrepancyNoteActionType.getRun().isDoubleDataEntry());
            action.getRuleActionRun().setAdministrativeDataEntry(discrepancyNoteActionType.getRun().isAdministrativeDataEntry());
            action.getRuleActionRun().setImportDataEntry(discrepancyNoteActionType.getRun().isImportDataEntry());
            action.getRuleActionRun().setBatch(discrepancyNoteActionType.getRun().isBatch());
            ruleSetRuleBean.addAction(action);
        }
        for (EmailActionType emailActionType : rules.getRuleAssignment().get(0).getRuleRef().get(0).getEmailAction()) {
            EmailActionBean action = new EmailActionBean();
            action.setMessage(emailActionType.getMessage());
            action.setTo(emailActionType.getTo());
            action.setExpressionEvaluatesTo(Boolean.valueOf(emailActionType.getIfExpressionEvaluates()));
            action.getRuleActionRun().setInitialDataEntry(emailActionType.getRun().isInitialDataEntry());
            action.getRuleActionRun().setDoubleDataEntry(emailActionType.getRun().isDoubleDataEntry());
            action.getRuleActionRun().setAdministrativeDataEntry(emailActionType.getRun().isAdministrativeDataEntry());
            action.getRuleActionRun().setImportDataEntry(emailActionType.getRun().isImportDataEntry());
            action.getRuleActionRun().setBatch(emailActionType.getRun().isBatch());
            ruleSetRuleBean.addAction(action);
        }
        for (ShowActionType showActionType : rules.getRuleAssignment().get(0).getRuleRef().get(0).getShowAction()) {
            ShowActionBean action = new ShowActionBean();
            action.setMessage(showActionType.getMessage());
            action.setExpressionEvaluatesTo(Boolean.valueOf(showActionType.getIfExpressionEvaluates()));
            action.getRuleActionRun().setInitialDataEntry(showActionType.getRun().isInitialDataEntry());
            action.getRuleActionRun().setDoubleDataEntry(showActionType.getRun().isDoubleDataEntry());
            action.getRuleActionRun().setAdministrativeDataEntry(showActionType.getRun().isAdministrativeDataEntry());
            action.getRuleActionRun().setImportDataEntry(showActionType.getRun().isImportDataEntry());
            action.getRuleActionRun().setBatch(showActionType.getRun().isBatch());
            for (PropertyType propertyType : showActionType.getDestinationProperty()) {
                PropertyBean property = new PropertyBean();
                property.setOid(propertyType.getOID());
                action.addProperty(property);
            }
            ruleSetRuleBean.addAction(action);
        }
        for (HideActionType hideActionType : rules.getRuleAssignment().get(0).getRuleRef().get(0).getHideAction()) {
            HideActionBean action = new HideActionBean();
            action.setMessage(hideActionType.getMessage());
            action.setExpressionEvaluatesTo(Boolean.valueOf(hideActionType.getIfExpressionEvaluates()));
            action.getRuleActionRun().setInitialDataEntry(hideActionType.getRun().isInitialDataEntry());
            action.getRuleActionRun().setDoubleDataEntry(hideActionType.getRun().isDoubleDataEntry());
            action.getRuleActionRun().setAdministrativeDataEntry(hideActionType.getRun().isAdministrativeDataEntry());
            action.getRuleActionRun().setImportDataEntry(hideActionType.getRun().isImportDataEntry());
            action.getRuleActionRun().setBatch(hideActionType.getRun().isBatch());
            for (PropertyType propertyType : hideActionType.getDestinationProperty()) {
                PropertyBean property = new PropertyBean();
                property.setOid(propertyType.getOID());
                action.addProperty(property);
            }
            ruleSetRuleBean.addAction(action);
        }
        for (InsertActionType insertActionType : rules.getRuleAssignment().get(0).getRuleRef().get(0).getInsertAction()) {
            InsertActionBean action = new InsertActionBean();
            action.setExpressionEvaluatesTo(Boolean.valueOf(insertActionType.getIfExpressionEvaluates()));
            action.getRuleActionRun().setInitialDataEntry(insertActionType.getRun().isInitialDataEntry());
            action.getRuleActionRun().setDoubleDataEntry(insertActionType.getRun().isDoubleDataEntry());
            action.getRuleActionRun().setAdministrativeDataEntry(insertActionType.getRun().isAdministrativeDataEntry());
            action.getRuleActionRun().setImportDataEntry(insertActionType.getRun().isImportDataEntry());
            action.getRuleActionRun().setBatch(insertActionType.getRun().isBatch());
            ruleSetRuleBean.addAction(action);
            for (PropertyType propertyType : insertActionType.getDestinationProperty()) {
                PropertyBean property = new PropertyBean();
                property.setOid(propertyType.getOID());
                property.setValue(propertyType.getValue());
                ExpressionBean expressionBean = new ExpressionBean(Context.OC_RULES_V1, propertyType.getValueExpression().getValue());
                property.setValueExpression(expressionBean);
            }
            ruleSetRuleBean.addAction(action);
        }

        ruleSetBean.addRuleSetRule(ruleSetRuleBean);
        rpic.addRuleSet(ruleSetBean);
        rpic.addRuleDef(ruleBean);

        return rpic;
    }

    @RequestMapping(value = "/studies/{study}/validateRule", method = RequestMethod.POST)
    public void create(@RequestBody org.openclinica.ns.rules.v31.Rules rules, Model model, HttpSession session, @PathVariable("study") String studyOid) {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        RulesPostImportContainer rpic = mapRulesToRulesPostImportContainer(rules);
        StudyBean currentStudy = (StudyBean) session.getAttribute("study");
        UserAccountBean userAccount = (UserAccountBean) session.getAttribute("userBean");

        UserAccountDAO userAccountDao = new UserAccountDAO(dataSource);
        userAccount = (UserAccountBean) userAccountDao.findByUserName("root");

        StudyDAO studyDao = new StudyDAO(dataSource);
        currentStudy = studyDao.findByOid(studyOid);

        getRulePostImportContainerService(currentStudy, userAccount);
        rpic = getRulePostImportContainerService(currentStudy, userAccount).validateRuleDefs(rpic);
        rpic = getRulePostImportContainerService(currentStudy, userAccount).validateRuleSetDefs(rpic);
        Response response = new Response();
        response.setValid(Boolean.TRUE);
        if (rpic.getInValidRuleDefs().size() > 0 || rpic.getInValidRuleSetDefs().size() > 0) {
            response.setValid(Boolean.FALSE);
            for (AuditableBeanWrapper<RuleBean> beanWrapper : rpic.getInValidRuleDefs()) {
                for (String error : beanWrapper.getImportErrors()) {
                    org.openclinica.ns.response.v31.MessagesType messageType = new MessagesType();
                    messageType.setMessage(error);
                    response.getMessages().add(messageType);
                }
            }
            for (AuditableBeanWrapper<RuleSetBean> beanWrapper : rpic.getInValidRuleSetDefs()) {
                for (String error : beanWrapper.getImportErrors()) {
                    org.openclinica.ns.response.v31.MessagesType messageType = new MessagesType();
                    messageType.setMessage(error);
                    response.getMessages().add(messageType);
                }
            }
        }
        logger.debug("RPIC READY");
        model.addAttribute("response", response);
    }

    @RequestMapping(value = "/studies/{study}/validateAndSaveRule", method = RequestMethod.POST)
    public void validateAndSave(@RequestBody org.openclinica.ns.rules.v31.Rules rules, Model model, HttpSession session, @PathVariable("study") String studyOid) {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        RulesPostImportContainer rpic = mapRulesToRulesPostImportContainer(rules);
        StudyBean currentStudy = (StudyBean) session.getAttribute("study");
        UserAccountBean userAccount = (UserAccountBean) session.getAttribute("userBean");

        UserAccountDAO userAccountDao = new UserAccountDAO(dataSource);
        userAccount = (UserAccountBean) userAccountDao.findByUserName("root");

        StudyDAO studyDao = new StudyDAO(dataSource);
        currentStudy = studyDao.findByOid(studyOid);

        getRulePostImportContainerService(currentStudy, userAccount);
        rpic = getRulePostImportContainerService(currentStudy, userAccount).validateRuleDefs(rpic);
        rpic = getRulePostImportContainerService(currentStudy, userAccount).validateRuleSetDefs(rpic);
        Response response = new Response();
        response.setValid(Boolean.TRUE);
        if (rpic.getInValidRuleDefs().size() > 0 || rpic.getInValidRuleSetDefs().size() > 0) {
            response.setValid(Boolean.FALSE);
            for (AuditableBeanWrapper<RuleBean> beanWrapper : rpic.getInValidRuleDefs()) {
                for (String error : beanWrapper.getImportErrors()) {
                    org.openclinica.ns.response.v31.MessagesType messageType = new MessagesType();
                    messageType.setMessage(error);
                    response.getMessages().add(messageType);
                }
            }
            for (AuditableBeanWrapper<RuleSetBean> beanWrapper : rpic.getInValidRuleSetDefs()) {
                for (String error : beanWrapper.getImportErrors()) {
                    org.openclinica.ns.response.v31.MessagesType messageType = new MessagesType();
                    messageType.setMessage(error);
                    response.getMessages().add(messageType);
                }
            }
        } else {
            getRuleSetService().saveImport(rpic);
        }
        logger.debug("RPIC READY");
        model.addAttribute("response", response);
    }

    public static boolean isAjaxRequest(String requestedWith) {
        return requestedWith != null ? "XMLHttpRequest".equals(requestedWith) : false;
    }

    public static boolean isAjaxUploadRequest(HttpServletRequest request) {
        return request.getParameter("ajaxUpload") != null;
    }

    public RuleSetServiceInterface getRuleSetService() {
        return ruleSetService;
    }

    @Autowired
    public void setRuleSetService(RuleSetServiceInterface ruleSetService) {
        this.ruleSetService = ruleSetService;
    }

    public RuleSetRuleDao getRuleSetRuleDao() {
        return ruleSetRuleDao;
    }

    @Autowired
    public void setRuleSetRuleDao(RuleSetRuleDao ruleSetRuleDao) {
        this.ruleSetRuleDao = ruleSetRuleDao;
    }

    public RulesPostImportContainerService getRulesPostImportContainerService() {
        return rulesPostImportContainerService;
    }

    // TODO: fix locale
    public RulesPostImportContainerService getRulePostImportContainerService(StudyBean currentStudy, UserAccountBean userAccount) {
        Locale l = new Locale("en_US");
        this.rulesPostImportContainerService.setCurrentStudy(currentStudy);
        this.rulesPostImportContainerService.setRespage(ResourceBundleProvider.getPageMessagesBundle(l));
        this.rulesPostImportContainerService.setUserAccount(userAccount);
        return rulesPostImportContainerService;
    }

    @Autowired
    public void setRulesPostImportContainerService(RulesPostImportContainerService rulesPostImportContainerService) {
        this.rulesPostImportContainerService = rulesPostImportContainerService;
    }

    public MessageSource getMessageSource() {
        return messageSource;
    }

    @Autowired
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

}
