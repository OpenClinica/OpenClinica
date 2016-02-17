package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.StudyUserRole;

public class StudyUserRoleDao extends CompositeIdAbstractDomainDao<StudyUserRole> {
	
    @Override
    public Class<StudyUserRole> domainClass() {
        return StudyUserRole.class;
    }

}
