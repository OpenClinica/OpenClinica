package core.org.akaza.openclinica.dao.hibernate;

import core.org.akaza.openclinica.domain.datamap.AuditEvent;


public class AuditEventDao extends AbstractDomainDao<AuditEvent> {

	 @Override
	    public Class<AuditEvent> domainClass() {
	        return AuditEvent.class;
	    }
}
