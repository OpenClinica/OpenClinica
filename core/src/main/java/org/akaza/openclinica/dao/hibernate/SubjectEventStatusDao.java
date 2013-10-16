package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.SubjectEventStatus;

public class SubjectEventStatusDao extends AbstractDomainDao<SubjectEventStatus> {
	@Override
	Class<SubjectEventStatus> domainClass() {
		// TODO Auto-generated method stub
		return SubjectEventStatus.class;
	}
}
