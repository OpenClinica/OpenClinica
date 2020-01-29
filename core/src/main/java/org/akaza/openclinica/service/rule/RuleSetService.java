/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
/*
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: https://libreclinica.org/license
 *
 * LibreClinica is distributed under the
 * Copyright 2003-2008 Akaza Research
 */
package org.akaza.openclinica.service.rule;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.hibernate.DynamicsItemFormMetadataDao;
import org.akaza.openclinica.dao.hibernate.RuleActionRunLogDao;
import org.akaza.openclinica.dao.hibernate.RuleDao;
import org.akaza.openclinica.dao.hibernate.RuleSetAuditDao;
import org.akaza.openclinica.dao.hibernate.RuleSetDao;
import org.akaza.openclinica.dao.hibernate.RuleSetRuleDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDefinitionDao;
import org.akaza.openclinica.dao.hibernate.ViewRuleAssignmentFilter;
import org.akaza.openclinica.dao.hibernate.ViewRuleAssignmentSort;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.rule.action.RuleActionDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.crfdata.DynamicsItemFormMetadataBean;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.domain.datamap.StudyEventDefinition;
import org.akaza.openclinica.domain.rule.AuditableBeanWrapper;
import org.akaza.openclinica.domain.rule.RuleBean;
import org.akaza.openclinica.domain.rule.RuleBulkExecuteContainer;
import org.akaza.openclinica.domain.rule.RuleBulkExecuteContainerTwo;
import org.akaza.openclinica.domain.rule.RuleSetAuditBean;
import org.akaza.openclinica.domain.rule.RuleSetBasedViewContainer;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean.RuleSetRuleBeanImportStatus;
import org.akaza.openclinica.domain.rule.RulesPostImportContainer;
import org.akaza.openclinica.domain.rule.action.RuleActionBean;
import org.akaza.openclinica.domain.rule.action.RuleActionRunBean.Phase;
import org.akaza.openclinica.domain.rule.expression.ExpressionBean;
import org.akaza.openclinica.logic.rulerunner.BeanPropertyRuleRunner;
import org.akaza.openclinica.logic.rulerunner.CrfBulkRuleRunner;
import org.akaza.openclinica.logic.rulerunner.DataEntryRuleRunner;
import org.akaza.openclinica.logic.rulerunner.ExecutionMode;
import org.akaza.openclinica.logic.rulerunner.ImportDataRuleRunner;
import org.akaza.openclinica.logic.rulerunner.ImportDataRuleRunnerContainer;
import org.akaza.openclinica.logic.rulerunner.MessageContainer;
import org.akaza.openclinica.logic.rulerunner.RuleSetBulkRuleRunner;
import org.akaza.openclinica.patterns.ocobserver.StudyEventChangeDetails;
import org.akaza.openclinica.service.BulkEmailSenderService;
import org.akaza.openclinica.service.crfdata.BeanPropertyService;
import org.akaza.openclinica.service.crfdata.DynamicsMetadataService;
import org.akaza.openclinica.service.rule.expression.ExpressionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author krikor
 *
 */
public class RuleSetService implements RuleSetServiceInterface {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private DataSource dataSource;
    private RuleSetDao ruleSetDao;
    private RuleSetAuditDao ruleSetAuditDao;
    private RuleDao ruleDao;
    private RuleSetRuleDao ruleSetRuleDao;
    private JavaMailSenderImpl mailSender;
    // private RuleSetRuleDAO ruleSetRuleDao;
    private BulkEmailSenderService bulkEmailSenderService;

    // Jdbc based DAOs
    private StudyDAO studyDao;
    private StudyEventDefinitionDAO studyEventDefinitionDao;
    private StudySubjectDAO studySubjecdao;
    private CRFDAO crfDao;
    private CRFVersionDAO crfVersionDao;

    private RuleActionDAO ruleActionDao;
    private StudyEventDAO studyEventDao;
    private ItemDAO itemDao;
    private ItemDataDAO itemDataDao;
    private ItemFormMetadataDAO itemFormMetadataDao;
    private DynamicsItemFormMetadataDao dynamicsItemFormMetadataDao;
    private ExpressionService expressionService;
    private String requestURLMinusServletPath;
    private String contextPath;
    private DynamicsMetadataService dynamicsMetadataService;
    private RuleActionRunLogDao ruleActionRunLogDao;
    private BeanPropertyService beanPropertyService;
    //hibernate based daos
    private StudyEventDao studyEventDomainDao;
    private StudyEventDefinitionDao studyEventDefDomainDao;
    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.service.rule.RuleSetServiceInterface#saveRuleSet(org.akaza.openclinica.domain.rule.RuleSetBean)
     */

    public RuleSetBean saveRuleSet(RuleSetBean ruleSetBean) {
        RuleSetBean persistentRuleSetBean = getRuleSetDao().saveOrUpdate(ruleSetBean);
        return persistentRuleSetBean;
    }

 public BeanPropertyService getBeanPropertyService() {
		return beanPropertyService;
	}

	public void setBeanPropertyService(BeanPropertyService beanPropertyService) {
		this.beanPropertyService = beanPropertyService;
	}

@Transactional
    public void saveImportFromDesigner(RulesPostImportContainer rulesContainer) {
    	HashMap<String,RuleBean> ruleBeans = new HashMap<String, RuleBean>();
        for (AuditableBeanWrapper<RuleBean> ruleBeanWrapper : rulesContainer.getValidRuleDefs()) {
            RuleBean r = getRuleDao().saveOrUpdate(ruleBeanWrapper.getAuditableBean());
            ruleBeans.put(r.getOid(), r);
        }
        for (AuditableBeanWrapper<RuleBean> ruleBeanWrapper : rulesContainer.getDuplicateRuleDefs()) {
        	RuleBean r = getRuleDao().saveOrUpdate(ruleBeanWrapper.getAuditableBean());
            ruleBeans.put(r.getOid(), r);
        }

        for (AuditableBeanWrapper<RuleSetBean> ruleBeanWrapper : rulesContainer.getValidRuleSetDefs()) {
            loadRuleSetRuleWithPersistentRules(ruleBeanWrapper.getAuditableBean());
            saveRuleSet(ruleBeanWrapper.getAuditableBean());
        }

        for (AuditableBeanWrapper<RuleSetBean> ruleBeanWrapper : rulesContainer.getDuplicateRuleSetDefs()) {
            loadRuleSetRuleWithPersistentRulesWithHashMap(ruleBeanWrapper.getAuditableBean(),ruleBeans);
            replaceRuleSet(ruleBeanWrapper.getAuditableBean());
        }
    }

    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.service.rule.RuleSetServiceInterface#saveImport(org.akaza.openclinica.domain.rule.RulesPostImportContainer)
     */

    public void saveImport(RulesPostImportContainer rulesContainer) {
        for (AuditableBeanWrapper<RuleBean> ruleBeanWrapper : rulesContainer.getValidRuleDefs()) {
            getRuleDao().saveOrUpdate(ruleBeanWrapper.getAuditableBean());
        }
        for (AuditableBeanWrapper<RuleBean> ruleBeanWrapper : rulesContainer.getDuplicateRuleDefs()) {
            getRuleDao().saveOrUpdate(ruleBeanWrapper.getAuditableBean());
        }

        for (AuditableBeanWrapper<RuleSetBean> ruleBeanWrapper : rulesContainer.getValidRuleSetDefs()) {
            loadRuleSetRuleWithPersistentRules(ruleBeanWrapper.getAuditableBean());
            saveRuleSet(ruleBeanWrapper.getAuditableBean());
        }

        for (AuditableBeanWrapper<RuleSetBean> ruleBeanWrapper : rulesContainer.getDuplicateRuleSetDefs()) {
            loadRuleSetRuleWithPersistentRules(ruleBeanWrapper.getAuditableBean());
            replaceRuleSet(ruleBeanWrapper.getAuditableBean());
        }
    }


    public void saveImport(RuleSetRuleBean ruleSetRule) {
        getRuleDao().saveOrUpdate(ruleSetRule.getRuleBean());
        getRuleSetDao().saveOrUpdate(ruleSetRule.getRuleSetBean());
    }

    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.service.rule.RuleSetServiceInterface#updateRuleSet(org.akaza.openclinica.domain.rule.RuleSetBean,
     * org.akaza.openclinica.bean.login.UserAccountBean, org.akaza.openclinica.domain.Status)
     */
    public RuleSetBean updateRuleSet(RuleSetBean ruleSetBean, UserAccountBean user, Status status) {
        ruleSetBean.setStatus(status);
        ruleSetBean.setUpdater(user);
        for (RuleSetRuleBean ruleSetRuleBean : ruleSetBean.getRuleSetRules()) {
            ruleSetRuleBean.setStatus(status);
            ruleSetRuleBean.setUpdater(user);
        }
        ruleSetBean = saveRuleSet(ruleSetBean);
        ruleSetAuditDao.saveOrUpdate(createRuleSetAuditBean(ruleSetBean, user, status));
        return ruleSetBean;

    }

    private RuleSetAuditBean createRuleSetAuditBean(RuleSetBean ruleSetBean, UserAccountBean user, Status status) {
        RuleSetAuditBean ruleSetAuditBean = new RuleSetAuditBean();
        ruleSetAuditBean.setRuleSetBean(ruleSetBean);
        ruleSetAuditBean.setStatus(status);
        ruleSetAuditBean.setUpdater(user);
        return ruleSetAuditBean;
    }

    public void loadRuleSetRuleWithPersistentRulesWithHashMap(RuleSetBean ruleSetBean,HashMap<String,RuleBean> persistentRules) {
        for (RuleSetRuleBean ruleSetRule : ruleSetBean.getRuleSetRules()) {
            if (ruleSetRule.getId() == null) {
                String ruleOid = ruleSetRule.getOid();
                ruleSetRule.setRuleBean(persistentRules.get(ruleOid));
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.service.rule.RuleSetServiceInterface#loadRuleSetRuleWithPersistentRules(org.akaza.openclinica.domain.rule.RuleSetBean)
     */
    public void loadRuleSetRuleWithPersistentRules(RuleSetBean ruleSetBean) {
        for (RuleSetRuleBean ruleSetRule : ruleSetBean.getRuleSetRules()) {
            if (ruleSetRule.getId() == null) {
                String ruleOid = ruleSetRule.getOid();
                ruleSetRule.setRuleBean(ruleDao.findByOid(ruleOid, ruleSetBean.getStudyId()));
            }
        }
    }

    /*
     * public RuleSetBean replaceRuleSet(RuleSetBean ruleSetBean) { RuleSetBean persistentRuleSetBean = ruleSetBean; // Invalidate Previous RuleSetRules
     * logger.info("RuleSet Id " + persistentRuleSetBean.getId()); getRuleSetRuleDao().removeByRuleSet(persistentRuleSetBean); // Save RuleSetRules for
     * (RuleSetRuleBean ruleSetRule : persistentRuleSetBean.getRuleSetRules()) { ruleSetRule.setRuleSetBean(persistentRuleSetBean);
     * getRuleSetRuleDao().saveOrUpdate(ruleSetRule); // Save Actions for (RuleActionBean action : ruleSetRule.getActions()) {
     * action.setRuleSetRule(ruleSetRule); getRuleActionDao().saveOrUpdate(action); } } return persistentRuleSetBean; }
     */

    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.service.rule.RuleSetServiceInterface#replaceRuleSet(org.akaza.openclinica.domain.rule.RuleSetBean)
     */
    public RuleSetBean replaceRuleSet(RuleSetBean ruleSetBean) {
        RuleSetBean detachedRuleSetBean = ruleSetBean;

        for (RuleSetRuleBean ruleSetRuleBean : detachedRuleSetBean.getRuleSetRules()) {
            if (ruleSetRuleBean.getId() != null && ruleSetRuleBean.getRuleSetRuleBeanImportStatus() == RuleSetRuleBeanImportStatus.TO_BE_REMOVED) {
                ruleSetRuleBean.setStatus(org.akaza.openclinica.domain.Status.DELETED);
            }
        }
        return getRuleSetDao().saveOrUpdate(detachedRuleSetBean);
    }

    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.service.rule.RuleSetServiceInterface#runRulesInBulk(java.lang.String, java.lang.Boolean,
     * org.akaza.openclinica.bean.managestudy.StudyBean, org.akaza.openclinica.bean.login.UserAccountBean)
     */
    public HashMap<RuleBulkExecuteContainer, HashMap<RuleBulkExecuteContainerTwo, Set<String>>> runRulesInBulk(String crfId, ExecutionMode executionMode,
            StudyBean currentStudy, UserAccountBean ub) {
        CRFBean crf = new CRFBean();
        crf.setId(Integer.valueOf(crfId));
        List<RuleSetBean> ruleSets = getRuleSetsByCrfAndStudy(crf, currentStudy);
        ruleSets = filterByStatusEqualsAvailable(ruleSets);
        ruleSets = filterRuleSetsByStudyEventOrdinal(ruleSets, null);
        ruleSets = filterRuleSetsByGroupOrdinal(ruleSets);
        CrfBulkRuleRunner ruleRunner = new CrfBulkRuleRunner(dataSource, requestURLMinusServletPath, contextPath, mailSender);
        dynamicsMetadataService.setExpressionService(getExpressionService());
        ruleRunner.setDynamicsMetadataService(dynamicsMetadataService);
        ruleRunner.setRuleActionRunLogDao(ruleActionRunLogDao);
        return ruleRunner.runRulesBulk(ruleSets, executionMode, currentStudy, null, ub);
        // return runRulesBulk(ruleSets, dryRun, currentStudy, null, ub);
    }

    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.service.rule.RuleSetServiceInterface#runRulesInBulk(java.lang.String, java.lang.String, java.lang.Boolean,
     * org.akaza.openclinica.bean.managestudy.StudyBean, org.akaza.openclinica.bean.login.UserAccountBean)
     */
    public HashMap<RuleBulkExecuteContainer, HashMap<RuleBulkExecuteContainerTwo, Set<String>>> runRulesInBulk(String ruleSetRuleId, String crfVersionId,
            ExecutionMode executionMode, StudyBean currentStudy, UserAccountBean ub) {

        List<RuleSetBean> ruleSets = new ArrayList<RuleSetBean>();
        RuleSetBean ruleSet = getRuleSetBeanByRuleSetRuleAndSubstituteCrfVersion(ruleSetRuleId, crfVersionId, currentStudy);
        if (ruleSet != null) {
            ruleSets.add(ruleSet);
        }
        ruleSets = filterByStatusEqualsAvailable(ruleSets);
        ruleSets = filterRuleSetsByStudyEventOrdinal(ruleSets, crfVersionId);
        ruleSets = filterRuleSetsByGroupOrdinal(ruleSets);
        CrfBulkRuleRunner ruleRunner = new CrfBulkRuleRunner(dataSource, requestURLMinusServletPath, contextPath, mailSender);
        dynamicsMetadataService.setExpressionService(getExpressionService());
        ruleRunner.setDynamicsMetadataService(dynamicsMetadataService);
        ruleRunner.setRuleActionRunLogDao(ruleActionRunLogDao);
        return ruleRunner.runRulesBulk(ruleSets, executionMode, currentStudy, null, ub);
        // return runRulesBulk(ruleSets, dryRun, currentStudy, null, ub);
    }

    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.service.rule.RuleSetServiceInterface#runRulesInBulk(java.util.List, java.lang.Boolean,
     * org.akaza.openclinica.bean.managestudy.StudyBean, org.akaza.openclinica.bean.login.UserAccountBean)
     */
    public List<RuleSetBasedViewContainer> runRulesInBulk(List<RuleSetBean> ruleSets, Boolean dryRun, StudyBean currentStudy, UserAccountBean ub , boolean jobTrigger) {
        ruleSets = filterByStatusEqualsAvailable(ruleSets);
        ruleSets = filterRuleSetsByStudyEventOrdinal(ruleSets, null);
        if(jobTrigger) {
        try {
			ruleSets = filterRuleSetsByStudySubject(ruleSets);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        }
        ruleSets = filterRuleSetsByGroupOrdinal(ruleSets);
        RuleSetBulkRuleRunner ruleRunner = new RuleSetBulkRuleRunner(dataSource, requestURLMinusServletPath, contextPath, mailSender);
        dynamicsMetadataService.setExpressionService(getExpressionService());
        ruleRunner.setDynamicsMetadataService(dynamicsMetadataService);
        ruleRunner.setRuleActionRunLogDao(ruleActionRunLogDao);
        ExecutionMode executionMode = dryRun == true ? ExecutionMode.DRY_RUN : ExecutionMode.SAVE;
        logger.debug("in runRulesinBulk method");
        return ruleRunner.runRulesBulkFromRuleSetScreen(ruleSets, executionMode, currentStudy, null, ub);
        // return runRulesBulkFromRuleSetScreen(ruleSets, dryRun, currentStudy, null, ub);
    }

    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.service.rule.RuleSetServiceInterface#runRulesInDataEntry(java.util.List, java.lang.Boolean,
     * org.akaza.openclinica.bean.managestudy.StudyBean, org.akaza.openclinica.bean.login.UserAccountBean, java.util.HashMap)
     */
    public MessageContainer runRulesInDataEntry(List<RuleSetBean> ruleSets, Boolean dryRun, StudyBean currentStudy, UserAccountBean ub,
            HashMap<String, String> variableAndValue, Phase phase,EventCRFBean ecb, HttpServletRequest request) {
        DataEntryRuleRunner ruleRunner = new DataEntryRuleRunner(dataSource, requestURLMinusServletPath, contextPath, mailSender,ecb);
        dynamicsMetadataService.setExpressionService(getExpressionService());
        ruleRunner.setDynamicsMetadataService(dynamicsMetadataService);
        ruleRunner.setRuleActionRunLogDao(ruleActionRunLogDao);
        // TODO: KK return the new object && Pass in the Execution Mode
        ExecutionMode executionMode = dryRun == true ? ExecutionMode.DRY_RUN : ExecutionMode.SAVE;
        return ruleRunner.runRules(ruleSets, executionMode, currentStudy, variableAndValue, ub, phase, request);
        // return new HashMap<String, ArrayList<String>>();
        // return runRules(ruleSets, dryRun, currentStudy, c.variableAndValue, ub);
    }


    public HashMap<String, ArrayList<String>> runRulesInImportData(List<ImportDataRuleRunnerContainer> containers,
            StudyBean study, UserAccountBean ub, ExecutionMode executionMode) {
        ImportDataRuleRunner ruleRunner = new ImportDataRuleRunner(dataSource, requestURLMinusServletPath, contextPath, mailSender);
        dynamicsMetadataService.setExpressionService(getExpressionService());
        ruleRunner.setDynamicsMetadataService(dynamicsMetadataService);
        ruleRunner.setRuleActionRunLogDao(ruleActionRunLogDao);

        return ruleRunner.runRules(containers, study, ub, executionMode);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.akaza.openclinica.service.rule.RuleSetServiceInterface#getRuleSetsByCrfStudyAndStudyEventDefinition(org.akaza.openclinica.bean.managestudy.StudyBean,
     * org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean, org.akaza.openclinica.bean.submit.CRFVersionBean)
     */

    public List<RuleSetBean> getRuleSetsByCrfStudyAndStudyEventDefinition(StudyBean study, StudyEventDefinitionBean sed, CRFVersionBean crfVersion) {
        CRFBean crf = getCrfDao().findByVersionId(crfVersion.getId());
        logger.debug("crfVersionID : " + crfVersion.getId() + " studyId : " + study.getId() + " studyEventDefinition : " + sed.getId());
        List<RuleSetBean> ruleSets = getRuleSetDao().findByCrfVersionOrCrfAndStudyAndStudyEventDefinition(crfVersion, crf, study, sed);
        logger.info("getRuleSetsByCrfStudyAndStudyEventDefinition() : ruleSets Size {} : ", ruleSets.size());
        if(ruleSets!=null&&ruleSets.size()>0) {
            for (RuleSetBean ruleSetBean : ruleSets) {
                getObjects(ruleSetBean);
            }
        } else {
            ruleSets = new ArrayList<RuleSetBean>();
        }
        return ruleSets;
        // return eagerFetchRuleSet(ruleSets);
    }

    public int getCountWithFilter(ViewRuleAssignmentFilter viewRuleAssignmentFilter) {
        int count = getRuleSetRuleDao().getCountWithFilter(viewRuleAssignmentFilter);
        return count;

    }


    public int getCountByStudy(StudyBean study) {
        int count = getRuleSetRuleDao().getCountByStudy(study);
        return count;

    }

    public List<RuleSetRuleBean> getWithFilterAndSort(ViewRuleAssignmentFilter viewRuleAssignmentFilter, ViewRuleAssignmentSort viewRuleAssignmentSort,
            int rowStart, int rowEnd) {

        // List<RuleSetBean> ruleSets = getRuleSetDao().getWithFilterAndSort(viewRuleAssignmentFilter, viewRuleAssignmentSort, rowStart, rowEnd);
        List<RuleSetRuleBean> ruleSetRules = getRuleSetRuleDao().getWithFilterAndSort(viewRuleAssignmentFilter, viewRuleAssignmentSort, rowStart, rowEnd);
        // for (RuleSetBean ruleSetBean : ruleSets) {
        // getObjects(ruleSetBean);
        // }
        // logger.info("getRuleSetsByStudy() : ruleSets Size : {}", ruleSets.size());
        return ruleSetRules;

    }

    /*
     * Used to Manage RuleSets ,Hence will return all RuleSets whether removed or not
     */
    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.service.rule.RuleSetServiceInterface#getRuleSetsByStudy(org.akaza.openclinica.bean.managestudy.StudyBean)
     */
    public List<RuleSetBean> getRuleSetsByStudy(StudyBean study) {
        logger.debug(" Study Id {} ", study.getId());
        List<RuleSetBean> ruleSets = getRuleSetDao().findAllByStudy(study);
        for (RuleSetBean ruleSetBean : ruleSets) {
            getObjects(ruleSetBean);
        }
        logger.info("getRuleSetsByStudy() : ruleSets Size : {}", ruleSets.size());
        return ruleSets;
        // return eagerFetchRuleSet(ruleSets);
    }

    // . TODO: why are we including study but not using it in query
    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.service.rule.RuleSetServiceInterface#getRuleSetById(org.akaza.openclinica.bean.managestudy.StudyBean, java.lang.String)
     */
    public RuleSetBean getRuleSetById(StudyBean study, String id) {
        logger.debug(" Study Id {} ", study.getId());
        RuleSetBean ruleSetBean = getRuleSetDao().findById(Integer.valueOf(id));
        if (ruleSetBean != null) {
            getObjects(ruleSetBean);
        }
        return ruleSetBean;

    }

    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.service.rule.RuleSetServiceInterface#getRuleSetById(org.akaza.openclinica.bean.managestudy.StudyBean, java.lang.String,
     * org.akaza.openclinica.domain.rule.RuleBean)
     */
    public List<RuleSetRuleBean> getRuleSetById(StudyBean study, String id, RuleBean ruleBean) {
        logger.debug(" Study Id {} ", study.getId());
        RuleSetBean ruleSetBean = getRuleSetDao().findById(Integer.valueOf(id));
        return getRuleSetRuleDao().findByRuleSetBeanAndRuleBean(ruleSetBean, ruleBean);
    }

    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.service.rule.RuleSetServiceInterface#getRuleSetsByCrfAndStudy(org.akaza.openclinica.bean.admin.CRFBean,
     * org.akaza.openclinica.bean.managestudy.StudyBean)
     */
    public List<RuleSetBean> getRuleSetsByCrfAndStudy(CRFBean crfBean, StudyBean study) {
        List<RuleSetBean> ruleSets = getRuleSetDao().findByCrf(crfBean, study);
        for (RuleSetBean ruleSetBean : ruleSets) {
            getObjects(ruleSetBean);
        }
        // return eagerFetchRuleSet(ruleSets);
        return ruleSets;
    }

    public RuleSetBean getObjects(RuleSetBean ruleSetBean) {
        ruleSetBean.setStudy((StudyBean) getStudyDao().findByPK(ruleSetBean.getStudyId()));
        if (ruleSetBean.getStudyEventDefinitionId() != null && ruleSetBean.getStudyEventDefinitionId() != 0) {
            ruleSetBean.setStudyEventDefinition((StudyEventDefinitionBean) getStudyEventDefinitionDao().findByPK(ruleSetBean.getStudyEventDefinitionId()));
        }
        if (ruleSetBean.getCrfId() != null && ruleSetBean.getCrfId() != 0) {
            ruleSetBean.setCrf((CRFBean) getCrfDao().findByPK(ruleSetBean.getCrfId()));
        }

        if (ruleSetBean.getCrfVersionId() != null) {
            ruleSetBean.setCrfVersion((CRFVersionBean) getCrfVersionDao().findByPK(ruleSetBean.getCrfVersionId()));
        }
        ruleSetBean.setItemGroup(getExpressionService().getItemGroupExpression(ruleSetBean.getTarget().getValue()));
        if (ruleSetBean.getItemId() != null && ruleSetBean.getItemId() != 0) {
            ruleSetBean.setItem((ItemBean) getItemDao().findByPK(ruleSetBean.getItemId()));
        }
        // ruleSetBean.setItem(getExpressionService().getItemExpression(ruleSetBean.getTarget().getValue(), ruleSetBean.getItemGroup()));

        return ruleSetBean;

    }

    // TODO: look into the commented line make sure logic doesn't break
    private RuleSetBean getRuleSetBeanByRuleSetRuleAndSubstituteCrfVersion(String ruleSetRuleId, String crfVersionId, StudyBean currentStudy) {
        RuleSetBean ruleSetBean = null;
        if (ruleSetRuleId != null && crfVersionId != null && ruleSetRuleId.length() > 0 && crfVersionId.length() > 0) {
            RuleSetRuleBean ruleSetRule = getRuleSetRuleDao().findById(Integer.valueOf(ruleSetRuleId));
            // ruleSetBean = getRuleSetById(currentStudy, String.valueOf(ruleSetRule.getRuleSetBean().getId()), ruleSetRule.getRuleBean());
            ruleSetBean = ruleSetRule.getRuleSetBean();
            filterByRules(ruleSetBean, ruleSetRule.getRuleBean().getId());
            CRFVersionBean crfVersion = (CRFVersionBean) getCrfVersionDao().findByPK(Integer.valueOf(crfVersionId));
            ruleSetBean = replaceCrfOidInTargetExpression(ruleSetBean, crfVersion.getOid());
        }
        return ruleSetBean;
    }

    public ExpressionBean replaceSEDOrdinal(ExpressionBean targetExpression, StudyEventBean studyEvent) {
        ExpressionBean expression = new ExpressionBean(targetExpression.getContext(), targetExpression.getValue());
        expression.setValue(getExpressionService().replaceStudyEventDefinitionOIDWith(expression.getValue(), String.valueOf(studyEvent.getId())));
        return expression;
    }

    private ExpressionBean replaceSEDOrdinal(ExpressionBean targetExpression, StudyEventBean studyEvent, String fullExpressionValue) {
        ExpressionBean expression = new ExpressionBean(targetExpression.getContext(), fullExpressionValue);
        expression.setValue(getExpressionService().replaceStudyEventDefinitionOIDWith(fullExpressionValue, String.valueOf(studyEvent.getId())));
        return expression;
    }

    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.service.rule.RuleSetServiceInterface#filterByStatusEqualsAvailableOnlyRuleSetRules(java.util.List)
     */
    public List<RuleSetBean> filterByStatusEqualsAvailableOnlyRuleSetRules(List<RuleSetBean> ruleSets) {
        for (RuleSetBean ruleSet : ruleSets) {
            for (Iterator<RuleSetRuleBean> i = ruleSet.getRuleSetRules().iterator(); i.hasNext();)
                if (i.next().getStatus() != org.akaza.openclinica.domain.Status.AVAILABLE) {
                    i.remove();
                }
        }
        return ruleSets;
    }

    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.service.rule.RuleSetServiceInterface#filterByStatusEqualsAvailable(java.util.List)
     */

    public List<RuleSetBean> filterByStatusEqualsAvailable(List<RuleSetBean> ruleSets) {
        for (Iterator<RuleSetBean> j = ruleSets.iterator(); j.hasNext();) {
            RuleSetBean ruleSet = j.next();
            if (ruleSet.getStatus() == org.akaza.openclinica.domain.Status.AVAILABLE) {
                for (Iterator<RuleSetRuleBean> i = ruleSet.getRuleSetRules().iterator(); i.hasNext();)
                    if (i.next().getStatus() != org.akaza.openclinica.domain.Status.AVAILABLE) {
                        i.remove();
                    }
            } else {
                j.remove();
            }
        }
        return ruleSets;
    }

    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.service.rule.RuleSetServiceInterface#filterByRules(org.akaza.openclinica.domain.rule.RuleSetBean, java.lang.Integer)
     */
    public RuleSetBean filterByRules(RuleSetBean ruleSet, Integer ruleBeanId) {

        for (Iterator<RuleSetRuleBean> i = ruleSet.getRuleSetRules().iterator(); i.hasNext();) {
            if (!i.next().getRuleBean().getId().equals(ruleBeanId)) {
                i.remove();
            }
        }
        return ruleSet;
    }

    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.service.rule.RuleSetServiceInterface#filterRuleSetsByStudyEventOrdinal(java.util.List,
     * org.akaza.openclinica.bean.managestudy.StudyEventBean)
     */

    public List<RuleSetBean> filterRuleSetsByStudyEventOrdinal(List<RuleSetBean> ruleSets, StudyEventBean studyEvent, CRFVersionBean crfVersion,
            StudyEventDefinitionBean studyEventDefinition) {
        ArrayList<RuleSetBean> validRuleSets = new ArrayList<RuleSetBean>();
        for (RuleSetBean ruleSetBean : ruleSets) {
            if (!getExpressionService().isExpressionPartial(ruleSetBean.getTarget().getValue())) {
                String studyEventDefinitionOrdinal = getExpressionService().getStudyEventDefinitionOrdninalCurated(ruleSetBean.getTarget().getValue());
                if (studyEventDefinitionOrdinal.equals("")) {
                    ruleSetBean.addExpression(replaceSEDOrdinal(ruleSetBean.getTarget(), studyEvent));
                    validRuleSets.add(ruleSetBean);
                }
                String compareOrdinal = Integer.toString(studyEvent.getSampleOrdinal());
                if (studyEventDefinitionOrdinal.equals(compareOrdinal)) {
                    ruleSetBean.addExpression(replaceSEDOrdinal(ruleSetBean.getTarget(), studyEvent));
                    validRuleSets.add(ruleSetBean);
                }
              /* if (studyEventDefinitionOrdinal.equals(String.valueOf(studyEvent.getSampleOrdinal()))) {
                    ruleSetBean.addExpression(replaceSEDOrdinal(ruleSetBean.getTarget(), studyEvent));
                    validRuleSets.add(ruleSetBean);
                }
                */
            } else {
                String expression =
                    getExpressionService().constructFullExpressionIfPartialProvided(ruleSetBean.getTarget().getValue(), crfVersion, studyEventDefinition);
                ruleSetBean.addExpression(replaceSEDOrdinal(ruleSetBean.getTarget(), studyEvent, expression));
                validRuleSets.add(ruleSetBean);

            }
        }
        logger.debug("Size of RuleSets post filterRuleSetsByStudyEventOrdinal() {} ", validRuleSets.size());
        return validRuleSets;
    }

    public List<RuleSetBean> filterRuleSetsByHiddenItems(List<RuleSetBean> ruleSets, EventCRFBean eventCrf, CRFVersionBean crfVersion,List<ItemBean> itemBeansWithSCDShown) {
        ArrayList<RuleSetBean> shownRuleSets = new ArrayList<RuleSetBean>();
        for (RuleSetBean ruleSetBean : ruleSets) {
            logMe("Entering the filterRuleSetsBy HiddenItems? Thread::"+Thread.currentThread()+"eventCrf?"+eventCrf+"crfVersion??"+crfVersion+"ruleSets?"+ruleSets);
            ItemBean target = ruleSetBean.getItem();
            ItemFormMetadataBean metadataBean = this.getItemFormMetadataDao().findByItemIdAndCRFVersionId(target.getId(), crfVersion.getId());
            ItemDataBean itemData = this.getItemDataDao().findByItemIdAndEventCRFId(target.getId(), eventCrf.getId());
            DynamicsItemFormMetadataBean dynamicsBean = this.getDynamicsItemFormMetadataDao().findByMetadataBean(metadataBean, eventCrf, itemData);
            if(itemBeansWithSCDShown==null)itemBeansWithSCDShown= new ArrayList<ItemBean>();
            if (dynamicsBean == null) {
                if (metadataBean.isShowItem()|| itemBeansWithSCDShown.contains(target)) {
                    logger.debug("just added rule set bean");
                    shownRuleSets.add(ruleSetBean);
                }
            } else {
                if (metadataBean.isShowItem() || dynamicsBean.isShowItem()) {
                    logger.debug("just added rule set bean 2, with dyn bean");
                    shownRuleSets.add(ruleSetBean);
                }
            }
        }
        return shownRuleSets;
    }

    private void logMe(String message) {
        logger.debug(message);
    }
    
    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.service.rule.RuleSetServiceInterface#filterRuleSetsByStudyEventOrdinal(java.util.List)
     */
    
    @SuppressWarnings("unchecked")
    public List<RuleSetBean> filterRuleSetsByStudySubject(List<RuleSetBean> ruleSets) throws NumberFormatException, ParseException {
        for (RuleSetBean ruleSet : ruleSets) {
            List<ExpressionBean> filteredExpressions = new ArrayList<ExpressionBean>();
            if (ruleSet.getExpressions()!=null){
            for (ExpressionBean expression : ruleSet.getExpressions()) {
                String studyEventId = getExpressionService().getStudyEventDefinitionOrdninalCurated(expression.getValue());
                  StudyEventBean studyEvent = (StudyEventBean) getStudyEventDao().findByPK(Integer.valueOf(studyEventId));
                  StudySubjectBean studySubject   = (StudySubjectBean) getStudySubjecdao().findByPK(studyEvent.getStudySubjectId());
                
                  
                  if (doTriggerRule(ruleSet, studySubject)){
                        ExpressionBean expBean = new ExpressionBean();
                        expBean.setValue(expression.getValue());
                        expBean.setContext(expression.getContext());
                        filteredExpressions.add(expBean);
                  }
                
             }
            ruleSet.setExpressions(filteredExpressions);
            }else{
                ruleSet.setExpressions(filteredExpressions);
            }
        }
        logExpressions(ruleSets);
        return ruleSets;
    }
    
        
    @SuppressWarnings("unchecked")
    public List<RuleSetBean> filterRuleSetsByStudyEventOrdinal(List<RuleSetBean> ruleSets, String crfVersionId) {
        ArrayList<RuleSetBean> validRuleSets = new ArrayList<RuleSetBean>();
        for (RuleSetBean ruleSetBean : ruleSets) {
           
            String studyEventDefinitionOrdinal = getExpressionService().getStudyEventDefinitionOrdninalCurated(ruleSetBean.getTarget().getValue());
            String studyEventDefinitionOid = getExpressionService().getStudyEventDefenitionOid(ruleSetBean.getTarget().getValue());
           
            String crfOrCrfVersionOid = getExpressionService().getCrfOid(ruleSetBean.getTarget().getValue());
            // whole expression is provided in target
            if (studyEventDefinitionOid != null && crfOrCrfVersionOid != null) {
            	List<StudyEventBean> studyEvents =null;
                if (crfOrCrfVersionOid.equals("STARTDATE") || crfOrCrfVersionOid.equals("STATUS")) {
                   StudyEventDefinitionBean sedBean = getStudyEventDefinitionDao().findByOid(studyEventDefinitionOid);
                
                   studyEvents = (List<StudyEventBean>) getStudyEventDao().findAllByDefinition(sedBean.getId());
                    logger.debug("studyEventDefinitionOrdinal {} , studyEventDefinitionOid {} , crfOrCrfVersionOid {} , studyEvents {}", new Object[] {
                        studyEventDefinitionOrdinal, studyEventDefinitionOid, crfOrCrfVersionOid, studyEvents.size() });
                	
                }else{

            	 studyEvents = getStudyEventDao().findAllByStudyEventDefinitionAndCrfOids(studyEventDefinitionOid, crfOrCrfVersionOid);
                logger.debug("studyEventDefinitionOrdinal {} , studyEventDefinitionOid {} , crfOrCrfVersionOid {} , studyEvents {}", new Object[] {
                    studyEventDefinitionOrdinal, studyEventDefinitionOid, crfOrCrfVersionOid, studyEvents.size() });
                }
                
                if (studyEventDefinitionOrdinal.equals("") && studyEvents.size() > 0) {
                    for (StudyEventBean studyEvent : studyEvents) {
                        ruleSetBean.addExpression(replaceSEDOrdinal(ruleSetBean.getTarget(), studyEvent));
                    }
                    validRuleSets.add(ruleSetBean);
                } else {
                    for (StudyEventBean studyEvent : studyEvents) {
                        if (studyEventDefinitionOrdinal.equals(String.valueOf(studyEvent.getSampleOrdinal()))) {
                            ruleSetBean.addExpression(replaceSEDOrdinal(ruleSetBean.getTarget(), studyEvent));
                        }
                    }
                    validRuleSets.add(ruleSetBean);
                }

            } else { // partial expression is provided in target
                CRFBean crf = null;
                List<CRFVersionBean> crfVersions = new ArrayList<CRFVersionBean>();
                CRFVersionBean crfVersion = null;
                if (crfOrCrfVersionOid == null) {
                    crf = getCrfDao().findByItemOid(getExpressionService().getItemOid(ruleSetBean.getTarget().getValue()));
                    if (crfVersionId != null) {
                        crfVersion = (CRFVersionBean) getCrfVersionDao().findByPK(Integer.valueOf(crfVersionId));
                        crfVersions.add(crfVersion);
                    } else {
                        crfVersions = (List<CRFVersionBean>) getCrfVersionDao().findAllByCRF(crf.getId());
                    }
                } else {
                    crf = getExpressionService().getCRFFromExpression(ruleSetBean.getTarget().getValue());
                    if (crfVersionId != null) {
                        crfVersion = (CRFVersionBean) getCrfVersionDao().findByPK(Integer.valueOf(crfVersionId));
                    } else {
                        crfVersion = getExpressionService().getCRFVersionFromExpression(ruleSetBean.getTarget().getValue());
                    }
                    if (crfVersion != null) {
                        crfVersions.add(crfVersion);
                    } else {
                        crfVersions = (List<CRFVersionBean>) getCrfVersionDao().findAllByCRF(crf.getId());
                    }
                }
                List<StudyEventDefinitionBean> studyEventDefinitions = getStudyEventDefinitionDao().findAllByCrf(crf);
                for (StudyEventDefinitionBean studyEventDefinitionBean : studyEventDefinitions) {
                    for (CRFVersionBean crfVersionBean : crfVersions) {
                        String expression =
                            getExpressionService().constructFullExpressionIfPartialProvided(ruleSetBean.getTarget().getValue(), crfVersionBean,
                                    studyEventDefinitionBean);
                        List<StudyEventBean> studyEvents =
                            getStudyEventDao().findAllByStudyEventDefinitionAndCrfOids(studyEventDefinitionBean.getOid(), crfVersionBean.getOid());
                        logger.debug("studyEventDefinitionOrdinal {} , studyEventDefinitionOid {} , crfOrCrfVersionOid {} , studyEvents {}", new Object[] {
                            studyEventDefinitionOrdinal, studyEventDefinitionBean.getOid(), crfVersionBean.getOid(), studyEvents.size() });
                        for (StudyEventBean studyEvent : studyEvents) {
                            ruleSetBean.addExpression(replaceSEDOrdinal(ruleSetBean.getTarget(), studyEvent, expression));
                        }
                    }
                }
                validRuleSets.add(ruleSetBean);

            }
        }
        logExpressions(validRuleSets);
        logger.debug("Size of RuleSets post filterRuleSetsByStudyEventOrdinal() {} ", validRuleSets.size());
        return validRuleSets;
    }

    private void logExpressions(List<RuleSetBean> validRuleSets) {
        if (logger.isDebugEnabled()) {
            for (RuleSetBean ruleSetBean : validRuleSets) {
                logger.debug("Expression : {} ", ruleSetBean.getTarget().getValue());
                List<ExpressionBean> expressions = ruleSetBean.getExpressions();
                if (expressions != null) {
                    for (ExpressionBean expression : expressions) {
                        logger.debug("Expression post filtering SEDs : {} ", expression.getValue());
                    }
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.service.rule.RuleSetServiceInterface#solidifyGroupOrdinalsUsingFormProperties(java.util.List, java.util.HashMap)
     */
    public List<RuleSetBean> solidifyGroupOrdinalsUsingFormProperties(List<RuleSetBean> ruleSets, HashMap<String, Integer> grouped) {
        for (RuleSetBean ruleSet : ruleSets) {
            ArrayList<ExpressionBean> expressionsWithCorrectGroupOrdinal = new ArrayList<ExpressionBean>();
            for (ExpressionBean expression : ruleSet.getExpressions()) {
                logger.debug("solidifyGroupOrdinals: Expression Value : " + expression.getValue());
                String groupOIDConcatItemOID = getExpressionService().getGroupOidConcatWithItemOid(expression.getValue());
                String itemOID = getExpressionService().getItemOid(expression.getValue());
                String groupOrdinal = getExpressionService().getGroupOrdninalCurated(expression.getValue());

                if (grouped.containsKey(groupOIDConcatItemOID) && groupOrdinal.equals("")) {
                    for (int i = 0; i < grouped.get(groupOIDConcatItemOID); i++) {
                        ExpressionBean expBean = new ExpressionBean();
                        expBean.setValue(getExpressionService().replaceGroupOidOrdinalInExpression(expression.getValue(), i + 1));
                        expBean.setContext(expression.getContext());
                        expressionsWithCorrectGroupOrdinal.add(expBean);
                    }
                } else if (grouped.containsKey(groupOIDConcatItemOID) && !groupOrdinal.equals("")) {
                    ExpressionBean expBean = new ExpressionBean();
                    expBean.setValue(expression.getValue());
                    expBean.setContext(expression.getContext());
                    expressionsWithCorrectGroupOrdinal.add(expBean);
                } else if (grouped.containsKey(itemOID)) {
                    ExpressionBean expBean = new ExpressionBean();
                    expBean.setValue(getExpressionService().replaceGroupOidOrdinalInExpression(expression.getValue(), null));
                    expBean.setContext(expression.getContext());
                    expressionsWithCorrectGroupOrdinal.add(expBean);
                }
            }
            ruleSet.setExpressions(expressionsWithCorrectGroupOrdinal);
            for (ExpressionBean expressionBean : ruleSet.getExpressions()) {
                logger.debug("expressionBean value : {} ", expressionBean.getValue());
            }
        }
        return ruleSets;
    }

    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.service.rule.RuleSetServiceInterface#filterRuleSetsBySectionAndGroupOrdinal(java.util.List, java.util.HashMap)
     */
    public List<RuleSetBean> filterRuleSetsBySectionAndGroupOrdinal(List<RuleSetBean> ruleSets, HashMap<String, Integer> grouped) {
        List<RuleSetBean> ruleSetsInThisSection = new ArrayList<RuleSetBean>();
        for (RuleSetBean ruleSet : ruleSets) {
            String ruleSetTargetValue =
                getExpressionService().isExpressionPartial(ruleSet.getTarget().getValue()) ? ruleSet.getExpressions().get(0).getValue() : ruleSet.getTarget()
                        .getValue();
            String expWithGroup = getExpressionService().getGroupOidConcatWithItemOid(ruleSetTargetValue);
            String expWithoutGroup = getExpressionService().getItemOid(ruleSetTargetValue);
            if (grouped.containsKey(expWithGroup)) {
                String ordinal = getExpressionService().getGroupOrdninalCurated(ruleSetTargetValue);
                if (ordinal.length() == 0 || grouped.get(expWithGroup) >= Integer.valueOf(ordinal)) {
                    ruleSetsInThisSection.add(ruleSet);
                }
            }
            if (grouped.containsKey(expWithoutGroup)) {
                ruleSetsInThisSection.add(ruleSet);
            }
        }
        logger.info("filterRuleSetsBySectionAndGroupOrdinal : ruleSets affecting the Whole Form : {} , ruleSets affecting this Section {} ", ruleSets.size(),
                ruleSetsInThisSection.size());
        return ruleSetsInThisSection;
    }

    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.service.rule.RuleSetServiceInterface#filterRuleSetsByGroupOrdinal(java.util.List)
     */
    public List<RuleSetBean> filterRuleSetsByGroupOrdinal(List<RuleSetBean> ruleSets) {

        for (RuleSetBean ruleSetBean : ruleSets) {
            List<ExpressionBean> expressionsWithCorrectGroupOrdinal = new ArrayList<>();
            if (ruleSetBean.getExpressions() != null) {
                for (ExpressionBean expression : ruleSetBean.getExpressions()) {
                    String studyEventId = getExpressionService().getStudyEventDefinitionOrdninalCurated(expression.getValue());
                    String itemOid = getExpressionService().getItemOid(expression.getValue());
                    String itemGroupOid = getExpressionService().getItemGroupOid(expression.getValue());
                    String groupOrdinal = getExpressionService().getGroupOrdninalCurated(expression.getValue());
                    List<ItemDataBean> itemDatas = getItemDataDao().findByStudyEventAndOids(Integer.valueOf(studyEventId), itemOid, itemGroupOid);
                    logger.debug(
                        "studyEventId {}, itemOid {}, itemGroupOid {}, groupOrdinal {}, itemDatas {}",
                        studyEventId, itemOid, itemGroupOid, groupOrdinal, itemDatas.size()
                    );

                    // case 1 : group ordinal = ""
                    if (groupOrdinal.equals("") && itemDatas.size() > 0) {
                        for (int k = 0; k < itemDatas.size(); k++) {
                            ExpressionBean expBean = new ExpressionBean();
                            expBean.setValue(getExpressionService().replaceGroupOidOrdinalInExpression(expression.getValue(), k + 1));
                            expBean.setContext(expression.getContext());
                            expressionsWithCorrectGroupOrdinal.add(expBean);
                        }
                    }
                    // case 2 : group ordinal = x and itemDatas should be size >= x
                    if (!groupOrdinal.equals("") && itemDatas.size() >= Integer.parseInt(groupOrdinal)) {
                        ExpressionBean expBean = new ExpressionBean();
                        expBean.setValue(getExpressionService().replaceGroupOidOrdinalInExpression(expression.getValue(), null));
                        expBean.setContext(expression.getContext());
                        expressionsWithCorrectGroupOrdinal.add(expBean);
                    }
                }
                ruleSetBean.setExpressions(expressionsWithCorrectGroupOrdinal);
            }
        }
        logExpressions(ruleSets);
        return ruleSets;
    }

    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.service.rule.RuleSetServiceInterface#getGroupOrdinalPlusItemOids(java.util.List)
     */
    public List<String> getGroupOrdinalPlusItemOids(List<RuleSetBean> ruleSets) {
        List<String> groupOrdinalPlusItemOid = new ArrayList<String>();
        for (RuleSetBean ruleSetBean : ruleSets) {
            String text = getExpressionService().getGroupOrdninalConcatWithItemOid(ruleSetBean.getTarget().getValue());
            groupOrdinalPlusItemOid.add(text);
            logger.debug("ruleSet id {} groupOrdinalPlusItemOid : {}", ruleSetBean.getId(), text);
        }
        return groupOrdinalPlusItemOid;
    }

    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.service.rule.RuleSetServiceInterface#replaceCrfOidInTargetExpression(org.akaza.openclinica.domain.rule.RuleSetBean,
     * java.lang.String)
     */
    public RuleSetBean replaceCrfOidInTargetExpression(RuleSetBean ruleSetBean, String replacementCrfOid) {
        String expression = getExpressionService().replaceCRFOidInExpression(ruleSetBean.getTarget().getValue(), replacementCrfOid);
        ruleSetBean.getTarget().setValue(expression);
        return ruleSetBean;
    }


    public boolean shouldRunRulesForRuleSets(List<RuleSetBean> ruleSets, Phase phase) {
        for(RuleSetBean ruleSetBean: ruleSets) {
            List<RuleSetRuleBean> ruleSetRuleBeans = ruleSetBean.getRuleSetRules();
            for(RuleSetRuleBean ruleSetRuleBean : ruleSetRuleBeans) {
                List<RuleActionBean> ruleActionBeans = ruleSetRuleBean.getActions();
                for(RuleActionBean ruleActionBean : ruleActionBeans) {
                    if(ruleActionBean.getRuleActionRun().canRun(phase)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * @return the contextPath
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * @param contextPath
     *            the contextPath to set
     */
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    /**
     * @param requestURLMinusServletPath
     *            the requestURLMinusServletPath to set
     */
    public void setRequestURLMinusServletPath(String requestURLMinusServletPath) {
        this.requestURLMinusServletPath = requestURLMinusServletPath;
    }

    public String getRequestURLMinusServletPath() {
        return requestURLMinusServletPath;
    }

    public void setStudyDao(StudyDAO studyDao) {
        this.studyDao = studyDao;
    }

    public RuleSetDao getRuleSetDao() {
        return ruleSetDao;
    }

    public void setRuleSetDao(RuleSetDao ruleSetDao) {
        this.ruleSetDao = ruleSetDao;
    }

    public void setRuleSetRuleDao(RuleSetRuleDao ruleSetRuleDao) {
        this.ruleSetRuleDao = ruleSetRuleDao;
    }

    public RuleSetRuleDao getRuleSetRuleDao() {
        return ruleSetRuleDao;
    }

    public RuleDao getRuleDao() {
        return ruleDao;
    }

    public void setRuleDao(RuleDao ruleDao) {
        this.ruleDao = ruleDao;
    }

    private CRFDAO getCrfDao() {
        //crfDao = this.crfDao != null ? crfDao : new CRFDAO(dataSource);
        return new CRFDAO(dataSource);
    }

    private StudyEventDAO getStudyEventDao() {
      //  studyEventDao = this.studyEventDao != null ? studyEventDao : new StudyEventDAO(dataSource);
        return new StudyEventDAO(dataSource);
    }

    private ItemDAO getItemDao() {
       // itemDao = this.itemDao != null ? itemDao : new ItemDAO(dataSource);
        return new ItemDAO(dataSource);
    }

    private ItemFormMetadataDAO getItemFormMetadataDao() {

       // itemFormMetadataDao = this.itemFormMetadataDao != null ? itemFormMetadataDao : new ItemFormMetadataDAO(dataSource);
      //  return itemFormMetadataDao;
        return new ItemFormMetadataDAO(dataSource);
    }

    private ExpressionService getExpressionService() {
        expressionService = this.expressionService != null ? expressionService : new ExpressionService(dataSource);
        return expressionService;
    }
    //JN:No reason to use global variables, they could cause potential concurrency issues.

    public StudyEventDefinitionDAO getStudyEventDefinitionDao() {
    //    studyEventDefinitionDao = this.studyEventDefinitionDao != null ? studyEventDefinitionDao : new StudyEventDefinitionDAO(dataSource);
        return  new StudyEventDefinitionDAO(dataSource);
    }


    public StudyDAO getStudyDao() {
       // studyDao = this.studyDao != null ? studyDao : new StudyDAO(dataSource);
        return new StudyDAO(dataSource);
    }

    private ItemDataDAO getItemDataDao() {
      //  itemDataDao = this.itemDataDao != null ? itemDataDao : new ItemDataDAO(dataSource);
        return  new ItemDataDAO(dataSource);
    }

    private CRFVersionDAO getCrfVersionDao() {
       // crfVersionDao = this.crfVersionDao != null ? crfVersionDao : new CRFVersionDAO(dataSource);
        return new CRFVersionDAO(dataSource);
    }

    public DynamicsItemFormMetadataDao getDynamicsItemFormMetadataDao() {
        return dynamicsItemFormMetadataDao;
    }

    public void setDynamicsItemFormMetadataDao(DynamicsItemFormMetadataDao dynamicsItemFormMetadataDao) {
        this.dynamicsItemFormMetadataDao = dynamicsItemFormMetadataDao;
    }

    public RuleSetAuditDao getRuleSetAuditDao() {
        return ruleSetAuditDao;
    }

    public void setRuleSetAuditDao(RuleSetAuditDao ruleSetAuditDao) {
        this.ruleSetAuditDao = ruleSetAuditDao;
    }

    public JavaMailSenderImpl getMailSender() {
        return mailSender;
    }

    public void setMailSender(JavaMailSenderImpl mailSender) {
        this.mailSender = mailSender;
    }

    public DynamicsMetadataService getDynamicsMetadataService() {
        return dynamicsMetadataService;
    }

    public void setDynamicsMetadataService(DynamicsMetadataService dynamicsMetadataService) {
        this.dynamicsMetadataService = dynamicsMetadataService;
    }

    public RuleActionRunLogDao getRuleActionRunLogDao() {
        return ruleActionRunLogDao;
    }

    public void setRuleActionRunLogDao(RuleActionRunLogDao ruleActionRunLogDao) {
        this.ruleActionRunLogDao = ruleActionRunLogDao;
    }

    /**
     * @return the dataSource
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * @param dataSource the dataSource to set
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    public void runRulesInBeanProperty(List<RuleSetBean> ruleSets,Integer userId,StudyEventChangeDetails changeDetails) {	
    	    BeanPropertyRuleRunner ruleRunner = new BeanPropertyRuleRunner(dataSource, requestURLMinusServletPath, contextPath, mailSender);
    	    ruleSets = (ArrayList<RuleSetBean>) filterByStatusEqualsAvailable(ruleSets);
    	    ruleSets = (ArrayList<RuleSetBean>) filterRuleSetsByStudyEventOrdinal(ruleSets, null);
    	    try {
				ruleSets = (ArrayList<RuleSetBean>) filterRuleSetsByStudySubject(ruleSets);
			} catch (NumberFormatException | ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

    		ruleRunner.runRules(ruleSets,dataSource,beanPropertyService, getStudyEventDomainDao(), getStudyEventDefDomainDao(),changeDetails,userId,mailSender);
}

    public void runIndividualRulesInBeanProperty(List<RuleSetBean> ruleSets,Integer userId,StudyEventChangeDetails changeDetails , Integer studyEventOrdinal) {	
    	ArrayList <RuleSetBean> ruleSetBeans = new ArrayList<>();
    	
    	for (RuleSetBean ruleSet : ruleSets){
    	String studyEventDefinitionOrdinal = getExpressionService().getStudyEventDefinitionOrdninalCurated(ruleSet.getOriginalTarget().getValue()+".A.B");      
        if (studyEventDefinitionOrdinal.equals("") || studyEventDefinitionOrdinal.equals(String.valueOf(studyEventOrdinal))) {
        	ruleSetBeans.add(ruleSet);	
          }
    	}    	
	    BeanPropertyRuleRunner ruleRunner = new BeanPropertyRuleRunner(dataSource, requestURLMinusServletPath, contextPath, mailSender);
 		ruleRunner.runRules(ruleSetBeans,dataSource,beanPropertyService, getStudyEventDomainDao(), getStudyEventDefDomainDao(),changeDetails,userId,mailSender);
}

    
    public StudyEventDao getStudyEventDomainDao() {
		return studyEventDomainDao;
	}

	public void setStudyEventDomainDao(StudyEventDao studyEventDomainDao) {
		this.studyEventDomainDao = studyEventDomainDao;
	}

	public StudyEventDefinitionDao getStudyEventDefDomainDao() {
		return studyEventDefDomainDao;
	}

	public void setStudyEventDefDomainDao(StudyEventDefinitionDao studyEventDefDomainDao) {
		this.studyEventDefDomainDao = studyEventDefDomainDao;
	}

	public StudySubjectDAO getStudySubjecdao() {
        return new StudySubjectDAO(dataSource);
	}

	public void setStudySubjecdao(StudySubjectDAO studySubjecdao) {
		this.studySubjecdao = studySubjecdao;
	}

	public Boolean calculateTimezoneDiff(TimeZone serverZone, TimeZone ssZone, int runTime, int serverTime) {
		int timeDifference = (serverZone.getRawOffset() + serverZone.getDSTSavings() - (ssZone.getRawOffset() + ssZone.getDSTSavings())) / (1000 * 60 * 60);
		int newSetTime = runTime + timeDifference;
		if (newSetTime > 23)
			newSetTime = newSetTime - 24;
		if (newSetTime < 0)
			newSetTime = newSetTime + 24;
		if (serverTime == newSetTime) {
			return true;
		} else {
			return false;
		}
	}

	public int getRunTimeWhenTimeIsNotSet(){		
		return 20;
	}
	
	
	public Boolean doTriggerRule(RuleSetBean ruleSet , StudySubjectBean studySubject) throws NumberFormatException, ParseException{
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH");
		Date now = new Date();
		int serverTime = Integer.parseInt(dateFormat.format(now));

		TimeZone serverZone = TimeZone.getDefault();

		TimeZone ssZone;
		int runTime = getRunTimeWhenTimeIsNotSet();
		Boolean doTrigger = false;
		if (ruleSet.getRunTime() != null)
			runTime = Integer.parseInt(dateFormat.format(dateFormat.parse(ruleSet.getRunTime())));

		String ssZoneId = studySubject.getTime_zone().trim();
		if (!ssZoneId.equals("") ) {
			ssZone = TimeZone.getTimeZone(ssZoneId);
		} else {
			ssZone = serverZone;
		}

		doTrigger = calculateTimezoneDiff(serverZone, ssZone, runTime, serverTime);
		return doTrigger;

	}


}
