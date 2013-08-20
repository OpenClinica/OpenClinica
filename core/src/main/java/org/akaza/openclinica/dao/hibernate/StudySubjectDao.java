package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.StudySubject;

public class StudySubjectDao extends AbstractDomainDao<StudySubject> {

	@Override
	Class<StudySubject> domainClass() {
		// TODO Auto-generated method stub
		return StudySubject.class;
	}

}
