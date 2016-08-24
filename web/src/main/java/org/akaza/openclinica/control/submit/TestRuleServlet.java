/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research
 */
package org.akaza.openclinica.control.submit;

import org.akaza.openclinica.bean.core.ItemDataType;
import org.akaza.openclinica.bean.core.NullValue;
import org.akaza.openclinica.bean.core.ResponseType;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.rule.XmlSchemaValidationHelper;
import org.akaza.openclinica.bean.submit.DisplayItemBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.ResponseSetBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.hibernate.RuleSetDao;
import org.akaza.openclinica.dao.hibernate.RuleSetRuleDao;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.akaza.openclinica.domain.rule.action.DiscrepancyNoteActionBean;
import org.akaza.openclinica.domain.rule.action.RuleActionBean;
import org.akaza.openclinica.domain.rule.expression.Context;
import org.akaza.openclinica.domain.rule.expression.ExpressionBean;
import org.akaza.openclinica.domain.rule.expression.ExpressionObjectWrapper;
import org.akaza.openclinica.domain.rule.expression.ExpressionProcessor;
import org.akaza.openclinica.domain.rule.expression.ExpressionProcessorFactory;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.service.rule.RulesPostImportContainerService;
import org.akaza.openclinica.service.rule.expression.ExpressionService;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Verify the Rule import , show records that have Errors as well as records that will be saved.
 *
 * @author Krikor krumlian
 */
public class TestRuleServlet extends SecureController {

    private static final long serialVersionUID = 9116068126651934226L;
    protected final Logger log = LoggerFactory.getLogger(TestRuleServlet.class);

    Locale locale;
    XmlSchemaValidationHelper schemaValidator = new XmlSchemaValidationHelper();
    RuleSetRuleDao ruleSetRuleDao;
    RuleSetDao ruleSetDao;
    ItemDAO itemDAO;
    ItemFormMetadataDAO itemFormMetadataDAO;
    RulesPostImportContainerService rulesPostImportContainerService;
    private ExpressionService expressionService;
    private final String TARGET = "target";
    private final String RULE = "rule";
    private final String RULE_SET_RULE_ID = "ruleSetRuleId";
    private final String RULE_SET_ID = "ruleSetId";

    void putDummyActionInSession() {
        ArrayList<RuleActionBean> actions = new ArrayList<RuleActionBean>();
        DiscrepancyNoteActionBean discNoteAction = new DiscrepancyNoteActionBean();
        discNoteAction.setExpressionEvaluatesTo(true);
        discNoteAction.setMessage("TEST DISCREPANCY");
        actions.add(discNoteAction);
        session.setAttribute("testRuleActions", actions);
    }

    void populteFormFields(FormProcessor fp) {

        String targetForm = fp.getString(TARGET).trim().replaceAll("(\n|\t|\r)", "");
        String testRulesTarget = (String) session.getAttribute("testRulesTarget");
        if (testRulesTarget != null) {
            // above added to avoid NPEs, tbh #6012
            String targetSess = testRulesTarget.trim().replaceAll("(\n|\t|\r)", "");
            if (!targetForm.equals(targetSess)) {
                putDummyActionInSession();
                session.removeAttribute("testRulesTarget");
            }
        }
        String textFields[] = { TARGET, RULE, RULE_SET_RULE_ID };
        fp.setCurrentStringValuesAsPreset(textFields);
        HashMap presetValues = fp.getPresetValues();
        setPresetValues(presetValues);

    }

    @Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        String action = request.getParameter("action");
        Validator v = new Validator(request);

        if (StringUtil.isBlank(action)) {
            request.setAttribute("result", resword.getString("test_rule_default_result"));
            Integer ruleSetRuleId = fp.getInt("ruleSetRuleId");

            if (ruleSetRuleId != 0) { // If testing an existing ruleSetRule
                RuleSetRuleBean rsr = getRuleSetRuleDao().findById(ruleSetRuleId);
                rsr.getActions().size();
                HashMap presetValues = new HashMap();
                presetValues.put(TARGET, rsr.getRuleSetBean().getTarget().getValue());
                presetValues.put(RULE, rsr.getRuleBean().getExpression().getValue());
                presetValues.put(RULE_SET_RULE_ID, String.valueOf(ruleSetRuleId));
                fp.setPresetValues(presetValues);
                setPresetValues(presetValues);
                session.setAttribute("testRuleActions", rsr.getActions());
                session.setAttribute("testRulesTarget", rsr.getRuleSetBean().getTarget().getValue());
                 request.setAttribute("ruleSetRuleId", ruleSetRuleId);
                request.setAttribute("ruleSetId", rsr.getRuleSetBean().getId());
                ItemBean item = getExpressionService().getItemBeanFromExpression(rsr.getRuleSetBean().getTarget().getValue());
                if(item!=null)
                {
                	request.setAttribute("itemName", item.getName());
                	request.setAttribute("itemDefinition", item.getDescription());
                }
                else
                	{
                	StudyEventDefinitionBean studyEventDef= getExpressionService().getStudyEventDefinitionFromExpressionForEvents(rsr.getRuleSetBean().getTarget().getValue(),this.currentStudy);
                	request.setAttribute("itemName", studyEventDef.getName());
                	request.setAttribute("itemDefinition", studyEventDef.getDescription());
                	}
                
                request.setAttribute("ruleSetRuleAvailable", true);

            } else { // free form testing
                putDummyActionInSession();
            }
            session.removeAttribute("testValues");
            request.setAttribute("action", "validate");
            forwardPage(Page.TEST_RULES);

        } else if (action.equals("validate")) {

            HashMap<String, String> result = validate(v);

            if (result.get("ruleValidation").equals("rule_valid")) {
                addPageMessage(resword.getString("test_rules_message_valid"));
            } else {
                addPageMessage(resword.getString("test_rules_message_invalid"));
            }
            request.setAttribute("ruleValidation", result.get("ruleValidation"));
            request.setAttribute("validate", "on");
            request.setAttribute("ruleEvaluatesTo", resword.getString("test_rules_validate_message"));
            request.setAttribute("ruleValidationFailMessage", result.get("ruleValidationFailMessage"));
            request.setAttribute("action", result.get("ruleValidation").equals("rule_valid") ? "test" : "validate");
            result.remove("result");
            result.remove("ruleValidation");
            result.remove("ruleEvaluatesTo");
            result.remove("ruleValidationFailMessage");
            populateTooltip(result);
            session.setAttribute("testValues", result);
            populteFormFields(fp);

            forwardPage(Page.TEST_RULES);

        } else if (action.equals("test")) {

            HashMap<String, String> result = validate(v);
            HashMap errors = v.validate();

            if (!errors.isEmpty()) {
                setInputMessages(errors);
                if (result.get("ruleValidation").equals("rule_valid")) {
                    addPageMessage(resword.getString("test_rules_message_valid"));
                } else {
                    addPageMessage(resword.getString("test_rules_message_invalid"));
                }
                request.setAttribute("ruleValidation", result.get("ruleValidation"));
                request.setAttribute("validate", "on");
                request.setAttribute("ruleEvaluatesTo",
                        resword.getString("test_rules_rule_fail_invalid_data_type") + " " + resword.getString("test_rules_rule_fail_invalid_data_type_desc"));
                request.setAttribute("ruleValidationFailMessage", result.get("ruleValidationFailMessage"));
                request.setAttribute("action", "test");

            } else {

                if (result.get("ruleValidation").equals("rule_valid")) {
                    addPageMessage(resword.getString("test_rules_message_valid"));
                } else {
                    addPageMessage(resword.getString("test_rules_message_invalid"));
                }
                request.setAttribute("action", result.get("ruleValidation").equals("rule_valid") ? "test" : "validate");
                request.setAttribute("ruleValidation", result.get("ruleValidation"));
                request.setAttribute("ruleEvaluatesTo", result.get("ruleEvaluatesTo"));
                request.setAttribute("ruleValidationFailMessage", result.get("ruleValidationFailMessage"));
            }

            if (result.get("ruleValidation").equals("rule_invalid")) {
                session.setAttribute("testValues", new HashMap<String, String>());
            } else {
                session.setAttribute("testValues", result);
            }

            result.remove("result");
            result.remove("ruleValidation");
            result.remove("ruleEvaluatesTo");
            result.remove("ruleValidationFailMessage");
            populateTooltip(result);

            populteFormFields(fp);

            forwardPage(Page.TEST_RULES);
        }
    }

    private void itemDataTypeToValidatorId(String key, ItemBean item, Validator v) {
        switch (item.getItemDataTypeId()) {
        case 6:
            v.addValidation(key, Validator.IS_AN_INTEGER);
            break;
        case 7:
            v.addValidation(key, Validator.IS_A_NUMBER);
            break;
        case 9:
            v.addValidation(key, Validator.IS_A_DATE);
            break;

        default:
            break;
        }
    }

    private void itemDataTypeToValidatorIdMultiSelect(String key, ItemBean item, Validator v, ResponseSetBean responseSet) {
        v.addValidation(key, Validator.IN_RESPONSE_SET_COMMA_SEPERATED, responseSet);
    }

    private void populateTooltip(HashMap<String, String> testVariablesAndValues) {

        if (testVariablesAndValues != null) {
            for (Map.Entry<String, String> entry : testVariablesAndValues.entrySet()) {
                ItemBean item = getExpressionService().getItemBeanFromExpression(entry.getKey());
                if(item!=null){
                DisplayItemBean dib = new DisplayItemBean();
                dib.setItem(item);
                request.setAttribute(entry.getKey() + "-tooltip", item.getName() + ": " + ItemDataType.get(item.getItemDataTypeId()).getName());
                request.setAttribute(entry.getKey() + "-dib", dib);
                }
                else{
                	StudyEventDefinitionBean sed = getExpressionService().getStudyEventDefinitionFromExpressionForEvents(entry.getKey(), currentStudy);
                	if(entry.getKey().contains(ExpressionService.STARTDATE))
                	{
                		request.setAttribute(entry.getKey() + "-tooltip", sed.getName() + ": " + "date");
                		request.setAttribute("studyEventProperty", new Integer(9));
                	}
                	else if(entry.getKey().contains(ExpressionService.STATUS))
                	{
                		request.setAttribute(entry.getKey() + "-tooltip", sed.getName() + ": " + "status");
                		request.setAttribute("studyEventProperty", new Integer(5));
                	}
                }
                if ((item!=null &&item.getItemDataTypeId() == 9)||(item==null&&entry.getKey().contains(ExpressionService.STARTDATE))) {//so enter this in case if the rules are event action based or if item has date type
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat(resformat.getString("date_format_string"),this.locale);
                        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
                        if(!entry.getValue().isEmpty()) {
                            java.util.Date date = sdf2.parse(entry.getValue());
                            entry.setValue(sdf.format(date));
                        }
                    } catch (Exception e) {
                        logger.error(e.toString());
                        // TODO: handle exception
                    }
                }
            }
        }

    }

    private HashMap<String, String> validate(Validator v) throws ParseException {

        FormProcessor fp = new FormProcessor(request);

        String targetString = fp.getString("target");
        String ruleString = fp.getString("rule");
        ruleString = ruleString.trim().replaceAll("(\n|\t|\r)", " ");
        targetString = targetString.trim().replaceAll("(\n|\t|\r)", "");

        HashMap<String, String> p =
            session.getAttribute("testValues") != null ? (HashMap<String, String>) session.getAttribute("testValues") : new HashMap<String, String>();

        if (p != null) {
            for (Map.Entry<String, String> entry : p.entrySet()) {
                entry.setValue(fp.getString(entry.getKey()));
              if(entry.getKey().startsWith(ExpressionService.STUDY_EVENT_OID_START_KEY)&&(entry.getKey().endsWith(ExpressionService.STATUS)||entry.getKey().endsWith(ExpressionService.STARTDATE)))
              {
            	  StudyEventDefinitionBean sed = getExpressionService().getStudyEventDefinitionFromExpressionForEvents(entry.getKey(), currentStudy);
            	  if(entry.getKey().endsWith(ExpressionService.STATUS)){
            		  //TODO add the logic for status
            	  }
            	  else if(entry.getKey().endsWith(ExpressionService.STARTDATE)){
            		  try {
                          v.addValidation(entry.getKey(), Validator.IS_A_DATE);
                          SimpleDateFormat sdf = new SimpleDateFormat(resformat.getString("date_format_string"),this.locale);
                          SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
                          if(!entry.getValue().isEmpty()) {
                              java.util.Date date = sdf.parse(entry.getValue());
                              entry.setValue(sdf2.format(date));
                          }
                      } catch (Exception e) {
                          logger.error(e.toString());
                          // TODO: handle exception
                      }
                             	  }
            		  
              }
              else{
                ItemBean item = getExpressionService().getItemBeanFromExpression(entry.getKey());
                List<ItemFormMetadataBean> itemFormMetadataBeans = getItemFormMetadataDAO().findAllByItemId(item.getId());
                ItemFormMetadataBean itemFormMetadataBean = itemFormMetadataBeans.size() > 0 ? itemFormMetadataBeans.get(0) : null;
                if (!entry.getValue().equals("") && NullValue.getByName(entry.getValue()) == NullValue.INVALID) {
                    if (itemFormMetadataBean != null) {
                        if (itemFormMetadataBean.getResponseSet().getResponseType() == ResponseType.SELECTMULTI
                            || itemFormMetadataBean.getResponseSet().getResponseType() == ResponseType.CHECKBOX) {
                            v.addValidation(entry.getKey(), Validator.IN_RESPONSE_SET_COMMA_SEPERATED, itemFormMetadataBean.getResponseSet());
                        }
                        if (itemFormMetadataBean.getResponseSet().getResponseType() == ResponseType.SELECT
                            || itemFormMetadataBean.getResponseSet().getResponseType() == ResponseType.RADIO) {
                            v.addValidation(entry.getKey(), Validator.IN_RESPONSE_SET_SINGLE_VALUE, itemFormMetadataBean.getResponseSet());
                        } else {
                            itemDataTypeToValidatorId(entry.getKey(), item, v);
                        }
                    }
                }

                if (item.getItemDataTypeId() == 9) {
                    try {
                    	SimpleDateFormat sdf = new SimpleDateFormat(resformat.getString("date_format_string"), this.locale);
                    	SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
                        if(!entry.getValue().isEmpty()) {
                            java.util.Date date = sdf.parse(entry.getValue());
                            entry.setValue(sdf2.format(date));
                        }
                    } catch (Exception e) {
                        // TODO: handle exception
                    	logger.error(e.toString());
                    }
                }
            }
            }
        }

        List<RuleActionBean> actions =
            session.getAttribute("testRuleActions") != null ? (List<RuleActionBean>) session.getAttribute("testRuleActions") : new ArrayList<RuleActionBean>();

        if (actions != null) {
            for (int i = 0; i < actions.size(); i++) {
                actions.get(i).setExpressionEvaluatesTo(fp.getBoolean("actions" + i));
            }
        }

        // Check Target if not valid report and return
        try {
            getExpressionService().ruleSetExpressionChecker(targetString);
        } catch (OpenClinicaSystemException e) {
            HashMap<String, String> result = new HashMap<String, String>();
            MessageFormat mf = new MessageFormat("");
            mf.applyPattern(respage.getString(e.getErrorCode()));
            Object[] arguments = e.getErrorParams();
            result.put("ruleValidation", "target_invalid");
            result.put("ruleValidationFailMessage", e.getErrorCode() + " : " + mf.format(arguments));
            result.put("ruleEvaluatesTo", "");
            request.setAttribute("targetFail", "on");
            return result;

        }

        // Auto update itemName & itemDefinition based on target
        ItemBean item = getExpressionService().getItemBeanFromExpression(targetString);
        StudyEventDefinitionBean sed = null ; 
        if (item != null) {
            request.setAttribute("itemName", item.getName());
            request.setAttribute("itemDefinition", item.getDescription());
        }
        else{
        	sed = getExpressionService().getStudyEventDefinitionFromExpressionForEvents(targetString, currentStudy);
        	if(sed!=null)
        	{
        		request.setAttribute("itemName",sed.getName());
        		request.setAttribute("itemDefinition",sed.getDescription());
        	}
        }
        
        	
        RuleSetBean ruleSet = new RuleSetBean();
        ExpressionBean target = new ExpressionBean();
        target.setContext(Context.OC_RULES_V1);
        target.setValue(targetString);
        ruleSet.setTarget(target);

        RuleSetBean persistentRuleSet = getRuleSetDao().findByExpressionAndStudy(ruleSet,currentStudy.getId());

        if (persistentRuleSet != null) {
if(item!=null)
        	request.setAttribute("ruleSetId", item.getId());
else
		request.setAttribute("ruleSetId", sed.getId());

        }

        ExpressionBean rule = new ExpressionBean();
        rule.setContext(Context.OC_RULES_V1);
        rule.setValue(ruleString);

        ExpressionObjectWrapper eow = new ExpressionObjectWrapper(sm.getDataSource(), currentStudy, rule, ruleSet);
        ExpressionProcessor ep = ExpressionProcessorFactory.createExpressionProcessor(eow);
        ep.setRespage(respage);

        // Run expression with populated HashMap
        DateTime start = new DateTime();
        HashMap<String, String> result = ep.testEvaluateExpression(p);
        DateTime end = new DateTime();
        Duration dur = new Duration(start, end);
        PeriodFormatter yearsAndMonths =
            new PeriodFormatterBuilder().printZeroAlways().appendSecondsWithMillis().appendSuffix(" second", " seconds").toFormatter();
        yearsAndMonths.print(dur.toPeriod());

        // Run expression with empty HashMap to check rule validity, because
        // using illegal test values will cause invalidity
        HashMap<String, String> k = new HashMap<String, String>();
        HashMap<String, String> theResult = ep.testEvaluateExpression(k);
        if (theResult.get("ruleValidation").equals("rule_valid") && result.get("ruleValidation").equals("rule_invalid")) {
            result.put("ruleValidation", "rule_valid");
            result.put("ruleEvaluatesTo", resword.getString("test_rules_rule_fail") + " " + result.get("ruleValidationFailMessage"));
            result.remove("ruleValidationFailMessage");

        }
        // Put on screen
        request.setAttribute("duration", yearsAndMonths.print(dur.toPeriod()));
        return result;

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

    private RuleSetRuleDao getRuleSetRuleDao() {
        ruleSetRuleDao =
            this.ruleSetRuleDao != null ? ruleSetRuleDao : (RuleSetRuleDao) SpringServletAccess.getApplicationContext(context).getBean("ruleSetRuleDao");
        return ruleSetRuleDao;
    }

    private RuleSetDao getRuleSetDao() {
        ruleSetDao = this.ruleSetDao != null ? ruleSetDao : (RuleSetDao) SpringServletAccess.getApplicationContext(context).getBean("ruleSetDao");
        return ruleSetDao;
    }

    private ItemDAO getItemDAO() {
        itemDAO = this.itemDAO != null ? itemDAO : new ItemDAO(sm.getDataSource());
        return itemDAO;
    }

    private ItemFormMetadataDAO getItemFormMetadataDAO() {
        itemFormMetadataDAO = this.itemFormMetadataDAO != null ? itemFormMetadataDAO : new ItemFormMetadataDAO(sm.getDataSource());
        return itemFormMetadataDAO;
    }

    private RulesPostImportContainerService getRulesPostImportContainerService() {
        rulesPostImportContainerService =
            this.rulesPostImportContainerService != null ? rulesPostImportContainerService : (RulesPostImportContainerService) SpringServletAccess
                    .getApplicationContext(context).getBean("rulesPostImportContainerService");
        rulesPostImportContainerService.setCurrentStudy(currentStudy);
        rulesPostImportContainerService.setRespage(respage);
        rulesPostImportContainerService.setUserAccount(ub);
        return rulesPostImportContainerService;
    }

    private ExpressionService getExpressionService() {
        expressionService =
            this.expressionService != null ? expressionService : new ExpressionService(
                    new ExpressionObjectWrapper(sm.getDataSource(), currentStudy, (ExpressionBean) null, (RuleSetBean) null));
        expressionService.setExpressionWrapper(new ExpressionObjectWrapper(sm.getDataSource(), currentStudy, (ExpressionBean) null, (RuleSetBean)null));

        return expressionService;
    }

}
