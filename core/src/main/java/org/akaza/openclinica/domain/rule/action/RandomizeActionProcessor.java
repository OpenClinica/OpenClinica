package org.akaza.openclinica.domain.rule.action;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.dao.hibernate.RuleActionRunLogDao;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.akaza.openclinica.logic.rulerunner.ExecutionMode;
import org.akaza.openclinica.logic.rulerunner.RuleRunner.RuleRunnerMode;
import org.akaza.openclinica.service.crfdata.DynamicsMetadataService;
import org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import org.akaza.openclinica.service.pmanage.RandomizationRegistrar;
import org.akaza.openclinica.service.pmanage.SeRandomizationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class RandomizeActionProcessor implements ActionProcessor {

    DataSource ds;
    DynamicsMetadataService itemMetadataService;
    RuleActionRunLogDao ruleActionRunLogDao;
    RuleSetBean ruleSet;
    RuleSetRuleBean ruleSetRule;
    StudyDAO sdao=null;
    RandomizationRegistrar randomizationRegistrar=null ;
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());


    public RandomizeActionProcessor(DataSource ds, DynamicsMetadataService itemMetadataService, RuleActionRunLogDao ruleActionRunLogDao, RuleSetBean ruleSet,
            RuleSetRuleBean ruleSetRule) {
        this.itemMetadataService = itemMetadataService;
        this.ruleSet = ruleSet;
        this.ruleSetRule = ruleSetRule;
        this.ruleActionRunLogDao = ruleActionRunLogDao;
        this.ds = ds;
    }

    public RuleActionBean execute(RuleRunnerMode ruleRunnerMode, ExecutionMode executionMode, RuleActionBean ruleAction, ItemDataBean itemDataBean,
            String itemData, StudyBean currentStudy, UserAccountBean ub, Object... arguments) {

        switch (executionMode) {
        case DRY_RUN: {
            if (ruleRunnerMode == RuleRunnerMode.DATA_ENTRY) {
                return null;
            } else {
                return ruleAction;
            }
        }
        case SAVE: {
            if (ruleRunnerMode == RuleRunnerMode.IMPORT_DATA) {
                return saveWithStatusUpdated(ruleAction, itemDataBean, itemData, currentStudy, ub);
            } else {
                return save(ruleAction, itemDataBean, itemData, currentStudy, ub);
            }
        }
        default:
            return ruleAction;
        }
    }

    private RuleActionBean saveWithStatusUpdated(RuleActionBean ruleAction, ItemDataBean itemDataBean, String itemData, StudyBean currentStudy, UserAccountBean ub) {
        itemDataBean.setStatus(Status.UNAVAILABLE);
        try {
            if (mayProceed(currentStudy.getOid())){
            getItemMetadataService().insert(itemDataBean, ((RandomizeActionBean) ruleAction).getProperties(), ub, ruleSet,((RandomizeActionBean) ruleAction).getStratificationFactors());
 //       ruleActionRunLogSaveOrUpdate(ruleAction, itemDataBean, itemData, currentStudy, ub);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ruleAction;
    }

    private RuleActionBean save(RuleActionBean ruleAction, ItemDataBean itemDataBean, String itemData, StudyBean currentStudy, UserAccountBean ub) {
        try {
            if (mayProceed(currentStudy.getOid())){
            getItemMetadataService().insert(itemDataBean.getId(), ((RandomizeActionBean) ruleAction).getProperties(), ub, ruleSet ,((RandomizeActionBean) ruleAction).getStratificationFactors());
  //      ruleActionRunLogSaveOrUpdate(ruleAction, itemDataBean, itemData, currentStudy, ub);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ruleAction;
    }

    private void ruleActionRunLogSaveOrUpdate(RuleActionBean ruleAction, ItemDataBean itemDataBean, String itemData, StudyBean currentStudy, UserAccountBean ub) {
        RuleActionRunLogBean ruleActionRunLog =
                new RuleActionRunLogBean(ruleAction.getActionType(), itemDataBean, itemDataBean.getValue(), ruleSetRule.getRuleBean().getOid());
        if (ruleActionRunLogDao.findCountByRuleActionRunLogBean(ruleActionRunLog) > 0) {
        } else {
            ruleActionRunLogDao.saveOrUpdate(ruleActionRunLog);
        }
    }

    private RuleActionBean saveAndReturnMessage(RuleActionBean ruleAction, ItemDataBean itemDataBean, String itemData, StudyBean currentStudy,
            UserAccountBean ub) {
        //
        return ruleAction;
    }


    private DynamicsMetadataService getItemMetadataService() {
        return itemMetadataService;
    }


    private boolean mayProceed(String studyOid) throws Exception {
        boolean accessPermission = false;
        StudyBean siteStudy = getStudy(studyOid);
        StudyBean study = getParentStudy(studyOid);
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(ds);
        StudyParameterValueBean pStatus = spvdao.findByHandleAndStudy(study.getId(), "randomization");

        randomizationRegistrar = new RandomizationRegistrar();
        SeRandomizationDTO seRandomizationDTO =randomizationRegistrar.getCachedRandomizationDTOObject(study.getOid().toString(),false);
        String randomizationStatusFromOCUI =seRandomizationDTO.getStatus();

        String randomizationStatusFromOC = pStatus.getValue().toString(); // enabled , disabled
        String studyStatus = study.getStatus().getName().toString(); // available , pending , frozen , locked
        String siteStatus = siteStudy.getStatus().getName().toString(); // available , pending , frozen , locked
        System.out.println("randomizationStatusFromOCUI: " + randomizationStatusFromOCUI + "  randomizationStatusFromOC: " + randomizationStatusFromOC + "   studyStatus: " + studyStatus + "   siteStatus: " + siteStatus);
        logger.info("randomizationStatusFromOCUI: " + randomizationStatusFromOCUI + "  randomizationStatusFromOC: " + randomizationStatusFromOC + "   studyStatus: " + studyStatus + "   siteStatus: " + siteStatus);
        if (randomizationStatusFromOC.equalsIgnoreCase("enabled") && studyStatus.equalsIgnoreCase("available") && siteStatus.equalsIgnoreCase("available") && randomizationStatusFromOCUI.equalsIgnoreCase("ACTIVE")) {
            accessPermission = true;
        }

        return accessPermission;
    }

    private StudyBean getStudy(String oid) {
        sdao = new StudyDAO(ds);
        StudyBean studyBean = (StudyBean) sdao.findByOid(oid);
        return studyBean;
    }

    private StudyBean getParentStudy(String studyOid) {
        StudyBean study = getStudy(studyOid);
        if (study.getParentStudyId() == 0) {
            return study;
        } else {
            StudyBean parentStudy = (StudyBean) sdao.findByPK(study.getParentStudyId());
            return parentStudy;
        }

    }


}
