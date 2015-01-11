package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.hibernate.AuditLogEventDao;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.service.RetrieveDeletedDataService;
import org.akaza.openclinica.templates.HibernateOcDbTestCase;
import org.akaza.openclinica.templates.OcDbTestCase;

public class RetrieveDeletedDataServiceTest extends HibernateOcDbTestCase {
	AuditLogEventDao auditLogEventDao;

	public void testStatement() {
		auditLogEventDao = (AuditLogEventDao) getContext().getBean("auditLogEventDaoDomain");
		RetrieveDeletedDataService retrieve = new RetrieveDeletedDataService(getDataSource(), auditLogEventDao);
		// retrieve.retrieveProcess(55);

	}
}
