package org.akaza.openclinica.service.rule;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.templates.HibernateOcDbTestCase;

import java.util.List;

public class RuleSetServiceTest extends HibernateOcDbTestCase {

    public RuleSetServiceTest() {
        super();
    }

    public void testGetRuleSetsByCrfStudyAndStudyEventDefinition() {
        List<RuleSetBean> ruleSets = getRuleSetsByCrfStudyAndStudyEventDefinition();
        assertEquals("RuleSet size should be 4", 4, ruleSets.size());
        assertNotNull(ruleSets.get(0).getRuleSetRules());
    }

    public void testFilterByStatusEqualsAvailable() {
        List<RuleSetBean> ruleSets = getRuleSetsByCrfStudyAndStudyEventDefinition();
        assertEquals("RuleSet size should be 4", 4, ruleSets.size());

        RuleSetServiceInterface instance = (RuleSetServiceInterface) getContext().getBean("ruleSetService");
        ruleSets = instance.filterByStatusEqualsAvailable(ruleSets);
        assertEquals("RuleSet size should be 3", 3, ruleSets.size());
        assertEquals("There should not be any RuleSetRules in this RuleSet", 0, ruleSets.get(2).getRuleSetRules().size());
    }

    // @pgawade 08-NOV-2010 Commented out the test
    // "testFilterRuleSetsByStudyEventOrdinal" temporarily. Krikor will look
    // into this.
    // public void testFilterRuleSetsByStudyEventOrdinal() {
    // List<RuleSetBean> ruleSets = getRuleSets();
    // StudyEventDAO studyEventDao = new StudyEventDAO(getDataSource());
    // StudyEventBean studyEventBean = (StudyEventBean)
    // studyEventDao.findByPK(1);
    //
    // RuleSetServiceInterface instance = (RuleSetServiceInterface)
    // getContext().getBean("ruleSetService");
    // List<RuleSetBean> ruleSets1 =
    // instance.filterRuleSetsByStudyEventOrdinal(ruleSets, studyEventBean);
    //
    // assertEquals("Expressions Size inside this RuleSet should be 1", 1,
    // ruleSets1.get(0).getExpressions().size());
    // assertEquals("Expression Value should be SE_ED2REPEA[1].F_CONC_V20.IG_CONC_CONCOMITANTMEDICATIONS.I_CONC_CON_MED_NAME",
    // "SE_ED2REPEA[1].F_CONC_V20.IG_CONC_CONCOMITANTMEDICATIONS.I_CONC_CON_MED_NAME",
    // ruleSets1.get(0).getExpressions().get(0).getValue());
    // }
    //
    // @pgawade 08-NOV-2010 Commented out the test
    // "testFilterRuleSetsByStudyEventOrdinalWithALL" temporarily. Krikor will
    // look into this.
    // public void testFilterRuleSetsByStudyEventOrdinalWithALL() {
    // List<RuleSetBean> ruleSets = getRuleSets();
    // StudyEventDAO studyEventDao = new StudyEventDAO(getDataSource());
    // StudyEventBean studyEventBean = (StudyEventBean)
    // studyEventDao.findByPK(2);
    //
    // RuleSetServiceInterface instance = (RuleSetServiceInterface)
    // getContext().getBean("ruleSetService");
    // ruleSets.get(0).getTarget().setValue("SE_ED2REPEA[ALL].F_CONC_V20.IG_CONC_CONCOMITANTMEDICATIONS.I_CONC_CON_MED_NAME");
    // List<RuleSetBean> ruleSets2 =
    // instance.filterRuleSetsByStudyEventOrdinal(ruleSets, studyEventBean);
    //
    // assertEquals("Expressions Size inside this RuleSet should be 1", 1,
    // ruleSets2.get(0).getExpressions().size());
    // assertEquals("Expression Value should be SE_ED2REPEA[2].F_CONC_V20.IG_CONC_CONCOMITANTMEDICATIONS.I_CONC_CON_MED_NAME",
    // "SE_ED2REPEA[2].F_CONC_V20.IG_CONC_CONCOMITANTMEDICATIONS.I_CONC_CON_MED_NAME",
    // ruleSets2.get(0).getExpressions().get(0).getValue());
    // }

    private List<RuleSetBean> getRuleSetsByCrfStudyAndStudyEventDefinition() {
        StudyDAO studyDao = new StudyDAO(getDataSource());
        StudyBean study = (StudyBean) studyDao.findByPK(1);
        assertNotNull(study);

        StudyEventDefinitionDAO studyEventDefinitionDao = new StudyEventDefinitionDAO(getDataSource());
        StudyEventDefinitionBean studyEventDefinition = (StudyEventDefinitionBean) studyEventDefinitionDao.findByPK(2);
        assertNotNull(studyEventDefinition);

        CRFVersionDAO crfVersionDao = new CRFVersionDAO(getDataSource());
        CRFVersionBean crfVersion = (CRFVersionBean) crfVersionDao.findByPK(2);
        assertNotNull(crfVersion);

        RuleSetServiceInterface instance = (RuleSetServiceInterface) getContext().getBean("ruleSetService");
        List<RuleSetBean> ruleSets = instance.getRuleSetsByCrfStudyAndStudyEventDefinition(study, studyEventDefinition, crfVersion);

        return ruleSets;

    }

    private List<RuleSetBean> getRuleSets() {
        List<RuleSetBean> ruleSets = null;
        ruleSets = getRuleSetsByCrfStudyAndStudyEventDefinition();

        RuleSetServiceInterface instance = (RuleSetServiceInterface) getContext().getBean("ruleSetService");
        ruleSets = instance.filterByStatusEqualsAvailable(ruleSets);
        return ruleSets;

    }

}