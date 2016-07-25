package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.expression.ExpressionBean;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.ArrayList;
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class RuleSetDao extends AbstractDomainDao<RuleSetBean> {

    @Override
    public Class<RuleSetBean> domainClass() {
        return RuleSetBean.class;
    }

    @SuppressWarnings("unchecked")
    public RuleSetBean findById(Integer id, StudyBean study) {
        String query = "from " + getDomainClassName() + " ruleSet  where ruleSet.id = :id and ruleSet.studyId = :studyId ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("id", id);
        q.setInteger("studyId", study.getId());
        return (RuleSetBean) q.uniqueResult();
    }

    public Long count(StudyBean study) {
        String query = "select count(*) from " + domainClass().getName() + " ruleSet where ruleSet.studyId = :studyId " + " AND ruleSet.status != :status ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("studyId", study.getId());
        q.setParameter("status", Status.DELETED);
        return (Long) q.uniqueResult();

    }

    public int getCountWithFilter(final ViewRuleAssignmentFilter filter) {

        // Using a sql query because we are referencing objects not managed by hibernate
        String query =
            "select COUNT(DISTINCT(rs.id)) from rule_set rs "
                + " left outer join study_event_definition sed on rs.study_event_definition_id = sed.study_event_definition_id "
                + " left outer join crf_version cv on rs.crf_version_id = cv.crf_version_id " + " left outer join crf c on rs.crf_id = c.crf_id "
                + " left outer join item i on rs.item_id = i.item_id " + " left outer join item_group ig on rs.item_group_id = ig.item_group_id "
                + " join rule_expression re on rs.rule_expression_id = re.id " + " join rule_set_rule rsr on rs.id = rsr.rule_set_id  "
                + " join rule r on r.id = rsr.rule_id " + " join rule_expression rer on r.rule_expression_id = rer.id " + " where ";

        query += filter.execute("");
        org.hibernate.Query q = getCurrentSession().createSQLQuery(query);

        return ((BigInteger) q.uniqueResult()).intValue();
    }

    @SuppressWarnings("unchecked")
    public ArrayList<RuleSetBean> getWithFilterAndSort(final ViewRuleAssignmentFilter filter, final ViewRuleAssignmentSort sort, final int rowStart,
            final int rowEnd) {

        String query =
            "select DISTINCT(rs.*) from rule_set rs "
                + " left outer join study_event_definition sed on rs.study_event_definition_id = sed.study_event_definition_id "
                + " left outer join crf_version cv on rs.crf_version_id = cv.crf_version_id " + " left outer join crf c on rs.crf_id = c.crf_id "
                + " left outer join item i on rs.item_id = i.item_id " + " left outer join item_group ig on rs.item_group_id = ig.item_group_id "
                + " join rule_expression re on rs.rule_expression_id = re.id " + " join rule_set_rule rsr on rs.id = rsr.rule_set_id "
                + " join rule r on r.id = rsr.rule_id " + " join rule_expression rer on r.rule_expression_id = rer.id " + " where ";

        query += filter.execute("");
        org.hibernate.Query q = getCurrentSession().createSQLQuery(query).addEntity(domainClass());
        q.setFirstResult(rowStart);
        q.setMaxResults(rowEnd - rowStart);
        return (ArrayList<RuleSetBean>) q.list();
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public ArrayList<RuleSetBean> findByCrfVersionOrCrfAndStudyAndStudyEventDefinition(CRFVersionBean crfVersion, CRFBean crfBean, StudyBean currentStudy,
            StudyEventDefinitionBean sed) {
        // Using a sql query because we are referencing objects not managed by hibernate
        String query =
            " select rs.* from rule_set rs where rs.study_id = :studyId " + " AND (( rs.study_event_definition_id = :studyEventDefinitionId "
                + " AND (( rs.crf_version_id = :crfVersionId AND rs.crf_id = :crfId ) "
                + " OR (rs.crf_version_id is null AND rs.crf_id = :crfId ))) OR ( rs.study_event_definition_id is null "
                + " and rs.item_id in (select item_id from item_form_metadata where crf_version_id = :crfVersionId)  ))";
        org.hibernate.Query q = getCurrentSession().createSQLQuery(query).addEntity(domainClass());
        q.setInteger("crfVersionId", crfVersion.getId());
        q.setInteger("crfId", crfBean.getId());
        q.setInteger("studyId", currentStudy.getParentStudyId() != 0 ? currentStudy.getParentStudyId() : currentStudy.getId());
        q.setInteger("studyEventDefinitionId", sed.getId());
        q.setCacheable(true);

        return (ArrayList<RuleSetBean>) q.list();
    }

    @SuppressWarnings("unchecked")
    public ArrayList<RuleSetBean> findAllByStudy(StudyBean currentStudy) {
        String query = "from " + getDomainClassName() + " ruleSet  where ruleSet.studyId = :studyId  ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("studyId", currentStudy.getId());
        return (ArrayList<RuleSetBean>) q.list();
    }
    
   

    @SuppressWarnings("unchecked")
    public ArrayList<RuleSetBean> findByCrf(CRFBean crfBean, StudyBean currentStudy) {
        String query =
            " select rs.* from rule_set rs where rs.study_id = :studyId "
                + " AND rs.item_id in ( select distinct(item_id) from item_form_metadata ifm,crf_version cv "
                + " where ifm.crf_version_id = cv.crf_version_id and cv.crf_id = :crfId) ";
        // Using a sql query because we are referencing objects not managed by hibernate
        org.hibernate.Query q = getCurrentSession().createSQLQuery(query).addEntity(domainClass());
        q.setInteger("crfId", crfBean.getId());
        q.setInteger("studyId", currentStudy.getId());
        return (ArrayList<RuleSetBean>) q.list();
    }

    public RuleSetBean findByExpression(RuleSetBean ruleSet) {
        String query = "from " + getDomainClassName() + " ruleSet  where ruleSet.originalTarget.value = :value AND ruleSet.originalTarget.context = :context ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setString("value", ruleSet.getTarget().getValue());
        q.setParameter("context", ruleSet.getTarget().getContext());
        return (RuleSetBean) q.uniqueResult();
    }

    public RuleSetBean findByExpressionAndStudy(RuleSetBean ruleSet, Integer studyId) {
        String query = "from " + getDomainClassName() + " ruleSet  where ruleSet.originalTarget.value = :value " +
        		"AND ruleSet.originalTarget.context = :context " +
        		"AND ruleSet.studyId = :studyId ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setString("value", ruleSet.getTarget().getValue());
        q.setParameter("context", ruleSet.getTarget().getContext());
        q.setInteger("studyId", studyId);
        return (RuleSetBean) q.uniqueResult();
    }

    public Long getCountByStudy(StudyBean currentStudy) {
        String query = "select count(*) from " + getDomainClassName() + " ruleSet  where ruleSet.studyId = :studyId and ruleSet.status = :status ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("studyId", currentStudy.getId());
        q.setParameter("status", org.akaza.openclinica.domain.Status.AVAILABLE);
        return (Long) q.uniqueResult();
    }

    public ArrayList<RuleSetBean> findAllByStudyEventDef(StudyEventDefinitionBean sed){
    	String query = "from " + getDomainClassName() + " ruleSet  where ruleSet.studyEventDefinitionId = :studyEventDefId  ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("studyEventDefId", sed.getId());
        return (ArrayList<RuleSetBean>) q.list();
    }
    public ArrayList<RuleSetBean> findAllEventActions(StudyBean currentStudy){
    	String query = "from " + getDomainClassName() + " ruleSet  where ruleSet.originalTarget.value LIKE '%.STARTDATE%' or ruleSet.originalTarget.value LIKE '%.STATUS%' and ruleSet.studyId = :studyId ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("studyId", currentStudy.getId());
        return (ArrayList<RuleSetBean>) q.list();
    }

    @Transactional
    public ArrayList<RuleSetBean> findAllRunOnSchedules(Boolean shedule){
    	String query = "from " + getDomainClassName() + " ruleSet  where ruleSet.runSchedule = :shedule";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setBoolean("shedule", shedule);
        return (ArrayList<RuleSetBean>) q.list();
    }

    
    @Transactional
    public ArrayList<RuleSetBean> findAllByStudyEventDefIdWhereItemIsNull(Integer studyEventDefId){
    	String query = "from " + getDomainClassName() + " ruleSet  where ruleSet.studyEventDefinitionId = :studyEventDefId  and ruleSet.itemId is null";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("studyEventDefId", studyEventDefId);
        return (ArrayList<RuleSetBean>) q.list();
    }
}
