package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.domain.rule.RuleSetBean;

import java.util.ArrayList;

public class RuleSetDao extends AbstractDomainDao<RuleSetBean> {

    @Override
    public Class<RuleSetBean> domainClass() {
        return RuleSetBean.class;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<RuleSetBean> findByCrfVersionOrCrfAndStudyAndStudyEventDefinition(CRFVersionBean crfVersion, CRFBean crfBean, StudyBean currentStudy,
            StudyEventDefinitionBean sed) {
        // Using a sql query because we are referencing objects not managed by hibernate
        String query =
            " select rs.* from rule_set rs where rs.study_id = :studyId " + " AND ( rs.study_event_definition_id = :studyEventDefinitionId "
                + " AND (( rs.crf_version_id = :crfVersionId AND rs.crf_id = :crfId ) "
                + " OR (rs.crf_version_id is null AND rs.crf_id = :crfId ))) OR ( rs.study_event_definition_id is null "
                + " and rs.item_id in (select item_id from item_form_metadata where crf_version_id = :crfVersionId)  )";
        org.hibernate.Query q = getCurrentSession().createSQLQuery(query).addEntity(domainClass());
        q.setInteger("crfVersionId", crfVersion.getId());
        q.setInteger("crfId", crfBean.getId());
        q.setInteger("studyId", currentStudy.getParentStudyId() != 0 ? currentStudy.getParentStudyId() : currentStudy.getId());
        q.setInteger("studyEventDefinitionId", sed.getId());
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

    public Long getCountByStudy(StudyBean currentStudy) {
        String query = "select count(*) from " + getDomainClassName() + " ruleSet  where ruleSet.studyId = :studyId and ruleSet.status = :status ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("studyId", currentStudy.getId());
        q.setParameter("status", org.akaza.openclinica.domain.Status.AVAILABLE);
        return (Long) q.uniqueResult();
    }

}
