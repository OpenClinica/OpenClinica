package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.hibernate.AuditLogEventDao;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.service.RetreiveDeletedDataService;
import org.akaza.openclinica.templates.HibernateOcDbTestCase;
import org.akaza.openclinica.templates.OcDbTestCase;

public class RetrieveDeletedDataServiceTest extends HibernateOcDbTestCase {
	AuditLogEventDao auditLogEventDao;

    public void testStatement() {
    	auditLogEventDao = (AuditLogEventDao)getContext().getBean("auditLogEventDaoDomain");
    	RetreiveDeletedDataService retrieve = new RetreiveDeletedDataService(getDataSource(),auditLogEventDao);
   // 	retrieve.retrieveProcess(55);
    	
    	
    	    }
}

	
	
	


