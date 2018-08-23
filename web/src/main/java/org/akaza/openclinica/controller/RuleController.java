package org.akaza.openclinica.controller;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.extract.odm.AdminDataReportBean;
import org.akaza.openclinica.bean.extract.odm.FullReportBean;
import org.akaza.openclinica.bean.extract.odm.MetaDataReportBean;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.odmbeans.ODMBean;
import org.akaza.openclinica.dao.core.CoreResources;
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
import org.akaza.openclinica.domain.rule.action.EventActionBean;
import org.akaza.openclinica.domain.rule.action.EventPropertyBean;
import org.akaza.openclinica.domain.rule.action.HideActionBean;
import org.akaza.openclinica.domain.rule.action.InsertActionBean;
import org.akaza.openclinica.domain.rule.action.NotificationActionBean;
import org.akaza.openclinica.domain.rule.action.PropertyBean;
import org.akaza.openclinica.domain.rule.action.ShowActionBean;
import org.akaza.openclinica.domain.rule.expression.Context;
import org.akaza.openclinica.domain.rule.expression.ExpressionBean;
import org.akaza.openclinica.domain.rule.expression.ExpressionObjectWrapper;
import org.akaza.openclinica.domain.rule.expression.ExpressionProcessor;
import org.akaza.openclinica.domain.rule.expression.ExpressionProcessorFactory;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.logic.odmExport.AdminDataCollector;
import org.akaza.openclinica.logic.odmExport.MetaDataCollector;
import org.akaza.openclinica.service.rule.RuleSetServiceInterface;
import org.akaza.openclinica.service.rule.RulesPostImportContainerService;
import org.apache.commons.dbcp2.BasicDataSource;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.openclinica.ns.response.v31.MessagesType;
import org.openclinica.ns.response.v31.Response;
import org.openclinica.ns.rules.v31.DiscrepancyNoteActionType;
import org.openclinica.ns.rules.v31.EmailActionType;
import org.openclinica.ns.rules.v31.EventActionType;
import org.openclinica.ns.rules.v31.EventDestinationType;
import org.openclinica.ns.rules.v31.HideActionType;
import org.openclinica.ns.rules.v31.InsertActionType;
import org.openclinica.ns.rules.v31.NotificationActionType;
import org.openclinica.ns.rules.v31.PropertyType;
import org.openclinica.ns.rules.v31.RuleAssignmentType;
import org.openclinica.ns.rules.v31.RuleDefType;
import org.openclinica.ns.rules.v31.RuleRefType;
import org.openclinica.ns.rules.v31.RunOnScheduleType;
import org.openclinica.ns.rules.v31.ShowActionType;
import org.openclinica.ns.rules.v31.TargetType;
import org.openclinica.ns.rules_test.v31.ParameterType;
import org.openclinica.ns.rules_test.v31.RulesTestMessagesType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

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
    @Autowired
    CoreResources coreResources;

    private final static String DUPLICATE_MESSAGE = "DUPLICATE";

    private RulesPostImportContainer mapRulesToRulesPostImportContainer(org.openclinica.ns.rules.v31.Rules rules) {
        RulesPostImportContainer rpic = new RulesPostImportContainer();

        for (RuleAssignmentType rat : rules.getRuleAssignment()) {
            TargetType targetType = rat.getTarget();
            ExpressionBean targetBean = new ExpressionBean(Context.OC_RULES_V1, targetType.getValue());
            RunOnScheduleType scheduleType = rules.getRuleAssignment().get(0).getRunOnSchedule();
            RuleSetBean ruleSetBean = new RuleSetBean();
            ruleSetBean.setOriginalTarget(targetBean);
            if (scheduleType != null) {
                if (!scheduleType.getTime().equals("")) {
                    ruleSetBean.setRunTime(scheduleType.getTime());
                }
            }

            for (RuleRefType rrt : rat.getRuleRef()) {
                RuleSetRuleBean ruleSetRuleBean = new RuleSetRuleBean();
                ruleSetRuleBean.setOid(rrt.getOID());

                for (DiscrepancyNoteActionType discrepancyNoteActionType : rrt.getDiscrepancyNoteAction()) {
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
                for (EmailActionType emailActionType : rrt.getEmailAction()) {
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
                for (ShowActionType showActionType : rrt.getShowAction()) {
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
                for (HideActionType hideActionType : rrt.getHideAction()) {
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
                for (InsertActionType insertActionType : rrt.getInsertAction()) {
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
                        action.addProperty(property);
                    }
                    ruleSetRuleBean.addAction(action);
                }

                for (EventActionType eventActionType : rrt.getEventAction()) {
                    EventActionBean action = new EventActionBean();
                    action.setExpressionEvaluatesTo(Boolean.valueOf(eventActionType.getIfExpressionEvaluates()));
                    action.setOc_oid_reference(eventActionType.getOID());
                    action.getRuleActionRun().setNot_started(eventActionType.getRunOnStatus().isNotScheduled());
                    action.getRuleActionRun().setScheduled(eventActionType.getRunOnStatus().isScheduled());
                    action.getRuleActionRun().setData_entry_started(eventActionType.getRunOnStatus().isDataEntryStarted());
                    action.getRuleActionRun().setComplete(eventActionType.getRunOnStatus().isCompleted());
                    action.getRuleActionRun().setSkipped(eventActionType.getRunOnStatus().isSkipped());
                    action.getRuleActionRun().setStopped(eventActionType.getRunOnStatus().isStopped());
                    for (EventDestinationType eventDestinationType : eventActionType.getEventDestination()) {
                        EventPropertyBean property = new EventPropertyBean();
                        property.setProperty(eventDestinationType.getProperty());
                        ExpressionBean expressionBean = new ExpressionBean(Context.OC_RULES_V1, eventDestinationType.getValueExpression().getValue());
                        property.setValueExpression(expressionBean);
                        action.addProperty(property);
                    }
                    ruleSetRuleBean.addAction(action);
                }

                for (NotificationActionType notificationActionType : rrt.getNotificationAction()) {
                    NotificationActionBean action = new NotificationActionBean();
                    action.setExpressionEvaluatesTo(Boolean.valueOf(notificationActionType.getIfExpressionEvaluates()));
                    action.setTo(notificationActionType.getTo());
                    action.setSubject(notificationActionType.getSubject());
                    action.setMessage(notificationActionType.getMessage());
                    ruleSetRuleBean.addAction(action);
                }

                ruleSetBean.addRuleSetRule(ruleSetRuleBean);
            }
            rpic.addRuleSet(ruleSetBean);
        }

        for (RuleDefType rdt : rules.getRuleDef()) {
            RuleBean ruleBean = new RuleBean();
            ExpressionBean ruleExpressionBean = new ExpressionBean(Context.OC_RULES_V1, rdt.getExpression().getValue());
            ruleBean.setExpression(ruleExpressionBean);
            ruleBean.setDescription(rdt.getDescription());
            ruleBean.setName(rdt.getName());
            ruleBean.setOid(rdt.getOID());
            rpic.addRuleDef(ruleBean);
        }

        return rpic;
    }

    @RequestMapping(value = "/studies/{study}/metadata", method = RequestMethod.GET)
    public ModelAndView studyMetadata(Model model, HttpSession session, @PathVariable("study") String studyOid, HttpServletResponse response) throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        StudyBean currentStudy = (StudyBean) session.getAttribute("study");
        UserAccountBean userAccount = (UserAccountBean) session.getAttribute("userBean");

        UserAccountDAO userAccountDao = new UserAccountDAO(dataSource);
        userAccount = (UserAccountBean) userAccountDao.findByUserName("root");

        StudyDAO studyDao = new StudyDAO(dataSource);
        currentStudy = studyDao.findByOid(studyOid);

        MetaDataCollector mdc = new MetaDataCollector(dataSource, currentStudy, getRuleSetRuleDao(),null);
        AdminDataCollector adc = new AdminDataCollector(dataSource, currentStudy);
        // RulesDataCollector rdc = new RulesDataCollector(sm.getDataSource(), currentStudy,getRuleSetRuleDao());
        MetaDataCollector.setTextLength(200);

        ODMBean odmb = mdc.getODMBean();
        odmb.setSchemaLocation("http://www.cdisc.org/ns/odm/v1.3 OpenClinica-ODM1-3-0-OC3-0.xsd");
        ArrayList<String> xmlnsList = new ArrayList<String>();
        xmlnsList.add("xmlns=\"http://www.cdisc.org/ns/odm/v1.3\"");
        // xmlnsList.add("xmlns:OpenClinica=\"http://www.openclinica.org/ns/openclinica_odm/v1.3\"");
        xmlnsList.add("xmlns:OpenClinica=\"http://www.openclinica.org/ns/odm_ext_v130/v3.1\"");
        xmlnsList.add("xmlns:OpenClinicaRules=\"http://www.openclinica.org/ns/rules/v3.1\"");
        odmb.setXmlnsList(xmlnsList);
        odmb.setODMVersion("oc1.3");
        mdc.setODMBean(odmb);
        adc.setOdmbean(odmb);
        // rdc.setOdmbean(odmb);

        mdc.collectFileData();
        MetaDataReportBean metaReport = new MetaDataReportBean(mdc.getOdmStudyMap(), coreResources);
        metaReport.setODMVersion("oc1.3");
        metaReport.setOdmBean(mdc.getODMBean());
        metaReport.createChunkedOdmXml(Boolean.FALSE);

        adc.collectFileData();
        AdminDataReportBean adminReport = new AdminDataReportBean(adc.getOdmAdminDataMap());
        adminReport.setODMVersion("oc1.3");
        adminReport.setOdmBean(mdc.getODMBean());
        adminReport.createChunkedOdmXml(Boolean.FALSE);

        // rdc.collectFileData();
        // RulesDataReportBean rulesReport = new RulesDataReportBean(rdc.getOdmRulesDataMap(),getCoreResources());
        // rulesReport.setODMVersion("oc1.3");
        // rulesReport.setOdmBean(mdc.getODMBean());
        // rulesReport.createChunkedOdmXml(Boolean.FALSE);

        FullReportBean report = new FullReportBean();
        report.setAdminDataMap(adc.getOdmAdminDataMap());
        report.setOdmStudyMap(mdc.getOdmStudyMap());
        report.setCoreResources(coreResources);
        // report.setRulesDataMap(rdc.getOdmRulesDataMap());
        report.setOdmBean(mdc.getODMBean());
        report.setODMVersion("oc1.3");
        report.createStudyMetaOdmXml(Boolean.FALSE);

        response.setContentType("application/xml");
        PrintWriter out = response.getWriter();
        out.print(report.getXmlOutput().toString().trim());

        return null;
    }

    private StudyUserRoleBean getRole(UserAccountBean userAccount, StudyBean study) throws Exception {
        StudyUserRoleBean role = new StudyUserRoleBean();
        if (study == null || userAccount == null || study.getId() == 0) {
            throw new Exception();
        }
        if (userAccount.getId() > 0 && study.getId() > 0 && !study.getStatus().getName().equals("removed")) {
            role = userAccount.getRoleByStudy(study.getId());
            if (study.getParentStudyId() > 0) {
                StudyUserRoleBean roleInParent = userAccount.getRoleByStudy(study.getParentStudyId());
                role.setRole(Role.max(role.getRole(), roleInParent.getRole()));
            }
        } else {
            throw new Exception();
        }
        return role;
    }

    private void mayProceed(UserAccountBean userAccount, StudyBean study) throws Exception {
        Role r = getRole(userAccount, study).getRole();
        if (r.equals(Role.STUDYDIRECTOR) || r.equals(Role.COORDINATOR)) {
            return;
        } else {
            throw new Exception("Insufficient Permission");
        }
    }

    @RequestMapping(value = "/studies/{study}/connect", method = RequestMethod.POST)
    public @ResponseBody org.openclinica.ns.response.v31.Response create(@RequestBody org.openclinica.ns.response.v31.Response responeType, Model model,
            HttpSession session, @PathVariable("study") String studyOid) throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        StudyDAO studyDao = new StudyDAO(dataSource);
        StudyBean currentStudy = studyDao.findByOid(studyOid);

        UserAccountBean userAccount = getUserAccount();
        mayProceed(userAccount, currentStudy);

        Response response = new Response();
        response.setValid(Boolean.TRUE);
        org.openclinica.ns.response.v31.MessagesType theMessageType = new MessagesType();
        theMessageType.setMessage("Hello");
        response.getMessages().add(theMessageType);
        logger.debug("RPIC READY");
        return response;
        // model.addAttribute("response", response);
    }

    @RequestMapping(value = "/studies/{study}/validateRule", method = RequestMethod.POST)
    public @ResponseBody Response create(@RequestBody org.openclinica.ns.rules.v31.Rules rules, Model model, HttpSession session,
            @PathVariable("study") String studyOid) throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        RulesPostImportContainer rpic = mapRulesToRulesPostImportContainer(rules);
        StudyDAO studyDao = new StudyDAO(dataSource);
        StudyBean currentStudy = studyDao.findByOid(studyOid);

        UserAccountBean userAccount = getUserAccount();
        mayProceed(userAccount, currentStudy);

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
        return response;
    }

    @RequestMapping(value = "/studies/{study}/validateAndSaveRule", method = RequestMethod.POST)
    public @ResponseBody Response validateAndSave(@RequestBody org.openclinica.ns.rules.v31.Rules rules, Model model, HttpSession session,
            @PathVariable("study") String studyOid, @RequestParam("ignoreDuplicates") Boolean ignoreDuplicates) throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        RulesPostImportContainer rpic = mapRulesToRulesPostImportContainer(rules);
        StudyDAO studyDao = new StudyDAO(dataSource);
        StudyBean currentStudy = studyDao.findByOid(studyOid);

        UserAccountBean userAccount = getUserAccount();
        mayProceed(userAccount, currentStudy);

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
        } else if ((rpic.getDuplicateRuleDefs().size() > 0) && !ignoreDuplicates) {
            response.setValid(Boolean.FALSE);
            for (AuditableBeanWrapper<RuleBean> beanWrapper : rpic.getDuplicateRuleDefs()) {
                org.openclinica.ns.response.v31.MessagesType messageType = new MessagesType();
                messageType.setMessage(DUPLICATE_MESSAGE);
                response.getMessages().add(messageType);
            }
        } else {
            getRuleSetService().saveImportFromDesigner(rpic);
        }
        logger.debug("RPIC READY");
        return response;
    }

    @RequestMapping(value = "/studies/{study}/validateAndTestRule", method = RequestMethod.POST)
    public @ResponseBody org.openclinica.ns.rules_test.v31.RulesTest create(@RequestBody org.openclinica.ns.rules_test.v31.RulesTest ruleTest, Model model,
            HttpSession session, @PathVariable("study") String studyOid) throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        RulesPostImportContainer rpic = mapRulesToRulesPostImportContainer(ruleTest.getRules());
        StudyDAO studyDao = new StudyDAO(dataSource);
        StudyBean currentStudy = studyDao.findByOid(studyOid);

        UserAccountBean userAccount = getUserAccount();
        mayProceed(userAccount, currentStudy);

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

        HashMap<String, String> p = new HashMap<String, String>();
        for (ParameterType parameterType : ruleTest.getParameters()) {
            p.put(parameterType.getKey(), parameterType.getValue());
        }
        ExpressionObjectWrapper eow = new ExpressionObjectWrapper(dataSource, currentStudy, rpic.getRuleDefs().get(0).getExpression(),
                rpic.getRuleSets().get(0));
        ExpressionProcessor ep = ExpressionProcessorFactory.createExpressionProcessor(eow);

        // Run expression with populated HashMap
        DateTime start = new DateTime();
        HashMap<String, String> result = ep.testEvaluateExpression(p);
        DateTime end = new DateTime();
        Duration dur = new Duration(start, end);
        PeriodFormatter yearsAndMonths = new PeriodFormatterBuilder().printZeroAlways().appendSecondsWithMillis().appendSuffix(" second", " seconds")
                .toFormatter();
        yearsAndMonths.print(dur.toPeriod());

        // Run expression with empty HashMap to check rule validity, because
        // using illegal test values will cause invalidity
        HashMap<String, String> k = new HashMap<String, String>();
        HashMap<String, String> theResult = ep.testEvaluateExpression(k);
        ruleTest.getParameters().clear();
        for (Map.Entry<String, String> entry : result.entrySet()) {
            ParameterType parameterType = new ParameterType();
            parameterType.setKey(entry.getKey());
            parameterType.setValue(entry.getValue());
            ruleTest.getParameters().add(parameterType);
        }
        // if (theResult.get("ruleValidation").equals("rule_valid") &&
        // result.get("ruleValidation").equals("rule_invalid")) {
        // result.put("ruleValidation", "rule_valid");
        // result.put("ruleEvaluatesTo", resword.getString("test_rules_rule_fail") + " " +
        // result.get("ruleValidationFailMessage"));
        // result.remove("ruleValidationFailMessage");
        // }
        // Put on screen
        // request.setAttribute("duration", yearsAndMonths.print(dur.toPeriod()));
        RulesTestMessagesType messageType = new RulesTestMessagesType();
        messageType.setKey("duration");
        messageType.setValue(yearsAndMonths.print(dur.toPeriod()));
        ruleTest.getRulesTestMessages().add(messageType);

        return ruleTest;
    }

    private UserAccountBean getUserAccount() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = null;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        UserAccountDAO userAccountDao = new UserAccountDAO(dataSource);
        return (UserAccountBean) userAccountDao.findByUserName(username);
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
