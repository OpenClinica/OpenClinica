package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.AuditLogEvent;
import org.hibernate.Filter;
import org.hibernate.impl.FilterImpl;

public class AuditLogEventDao extends AbstractDomainDao<AuditLogEvent> {

	@Override
	public Class<AuditLogEvent> domainClass() {
		return AuditLogEvent.class;
	}

	@SuppressWarnings("unchecked")
	public <T> T findByParam(AuditLogEvent auditLogEvent, String anotherAuditTable) {
		getSessionFactory().getStatistics().logSummary();
		String query = "from " + getDomainClassName();
		String buildQuery = "";
		if (auditLogEvent.getEntityId() != null && auditLogEvent.getAuditTable() != null && anotherAuditTable == null) {
			buildQuery += "do.entityId =:entity_id ";
			buildQuery += " and  do.auditTable =:audit_table order by do.auditId ";
		} else if (auditLogEvent.getEntityId() != null && auditLogEvent.getAuditTable() != null && anotherAuditTable != null) {
			buildQuery += "do.entityId =:entity_id ";
			buildQuery += " and ( do.auditTable =:audit_table or do.auditTable =:anotherAuditTable) order by do.auditId ";
		}
		if (!buildQuery.isEmpty())
			query = "from " + getDomainClassName() + " do  where " + buildQuery;
		else
			query = "from " + getDomainClassName();
		org.hibernate.Query q = getCurrentSession().createQuery(query);
		if (auditLogEvent.getEntityId() != null && auditLogEvent.getAuditTable() != null && anotherAuditTable == null) {
			q.setInteger("entity_id", auditLogEvent.getEntityId());
			q.setString("audit_table", auditLogEvent.getAuditTable());
		} else if (auditLogEvent.getEntityId() != null && auditLogEvent.getAuditTable() != null && anotherAuditTable != null) {
			q.setInteger("entity_id", auditLogEvent.getEntityId());
			q.setString("audit_table", auditLogEvent.getAuditTable());
			q.setString("anotherAuditTable", anotherAuditTable);
		}
		return (T) q.list();
	}

	@SuppressWarnings("unchecked")
	public <T> T findByParam(AuditLogEvent auditLogEvent) {
		getSessionFactory().getStatistics().logSummary();
		
		String query = "Select do.eventCrfId, do.eventCrfVersionId from "
				+ getDomainClassName()
				+ " do  where do.auditTable =:audit_table and do.auditLogEventType.auditLogEventTypeId =:auditLogEventTypeId and do.studyEventId =:studyEventId GROUP BY do.eventCrfId, do.eventCrfVersionId";
		org.hibernate.Query q = getCurrentSession().createQuery(query);

		q.setInteger("studyEventId", auditLogEvent.getStudyEventId());
		q.setString( "audit_table", auditLogEvent.getAuditTable());
		q.setInteger("auditLogEventTypeId", auditLogEvent.getAuditLogEventType().getAuditLogEventTypeId());

		return (T) q.list();
	}

	@SuppressWarnings("unchecked")
	public <T> T findByParamForEventCrf(AuditLogEvent auditLogEvent) {
		getSessionFactory().getStatistics().logSummary();
		
		String query = "from "+ getDomainClassName() + " do where ";
				
				if (auditLogEvent.getAuditTable()!=null)
				query = query + " do.auditTable =:audit_table and " ;
				query = query + " do.eventCrfId =:eventCrfId";
		org.hibernate.Query q = getCurrentSession().createQuery(query);

		q.setString( "audit_table", auditLogEvent.getAuditTable());
		q.setInteger( "eventCrfId", auditLogEvent.getEventCrfId());

		return (T) q.list();
	}

}