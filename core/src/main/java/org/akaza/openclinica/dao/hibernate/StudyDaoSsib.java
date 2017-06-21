package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.akaza.openclinica.bean.oid.OidGenerator;
import org.akaza.openclinica.bean.oid.StudySubjectOidGenerator;
import org.akaza.openclinica.dao.hibernate.AbstractDomainDao;
import org.akaza.openclinica.domain.datamap.Study;

/**
 * Consultas adicionales para la integración SSIB.
 *
 * @TODO ¿Es necesario que esté en el package de OC?
 */
public class StudyDaoSsib
	extends AbstractDomainDao<Study> {

	private static final Logger LOGGER =
		LoggerFactory.getLogger(
			StudyDaoSsib.class);

	@Override
	Class<Study> domainClass() {
		return
			Study.class;
	}

	public Study findByOid(
		String studyOid) {

		LOGGER.info(
			"Obteniendo Study con oid = "
				+ studyOid);
		getSessionFactory().getStatistics().logSummary();

		String query = 
			"from Study s where s.oc_oid = :oc_oid";
		org.hibernate.Query q =
			getCurrentSession().createQuery(query);
		q.setString("oc_oid", studyOid);
		return
			(Study) q.uniqueResult();
	}
}
