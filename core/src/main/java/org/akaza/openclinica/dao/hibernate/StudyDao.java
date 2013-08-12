package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.Study;


public class StudyDao extends AbstractDomainDao<Study> {
	
    @Override
    public Class<Study> domainClass() {
        return Study.class;
    }
}
