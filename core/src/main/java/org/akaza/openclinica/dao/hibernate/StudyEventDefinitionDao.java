package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.StudyEventDefinition;


public class StudyEventDefinitionDao extends AbstractDomainDao<StudyEventDefinition> {
	
    @Override
    public Class<StudyEventDefinition> domainClass() {
        return StudyEventDefinition.class;
    }
}
