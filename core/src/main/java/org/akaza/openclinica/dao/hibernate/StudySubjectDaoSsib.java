package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;

import org.akaza.openclinica.bean.oid.OidGenerator;
import org.akaza.openclinica.bean.oid.StudySubjectOidGenerator;
import org.akaza.openclinica.dao.hibernate.AbstractDomainDao;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.domain.datamap.StudySubject;

/**
 * Consultas adicionales para la integración SSIB.
 *
 * @TODO ¿Es necesario que esté en el package de OC?
 */
public class StudySubjectDaoSsib
	extends AbstractDomainDao<StudySubject> {

	@Override
	Class<StudySubject> domainClass() {
		// TODO Auto-generated method stub
		return
			StudySubject.class;
	}

	public ArrayList<StudySubject> findByStudyOid(
		String studyOid) {
		getSessionFactory().getStatistics().logSummary();

		String query = 
			"from StudySubject ss where ss.study.oc_oid = :oc_oid";
		org.hibernate.Query q =
			getCurrentSession().createQuery(query);
		q.setString("oc_oid", studyOid);
		return
			(ArrayList<StudySubject>) q.list();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<StudySubject> getBySubjectUID(
		String subjectUID) {

		String hql =
			" from StudySubject ss "
				+ "where ss.subject.uniqueIdentifier= :subjectUID";
		Query query =
			getCurrentSession().
			createQuery(
				hql);
		query.setString(
			"subjectUID",
			subjectUID);

		return
			(List<StudySubject>) query.list();
	}
}
