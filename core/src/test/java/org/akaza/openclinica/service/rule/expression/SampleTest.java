package org.akaza.openclinica.service.rule.expression;


import java.util.Locale;

import org.akaza.openclinica.dao.hibernate.AuthoritiesDao;
import org.akaza.openclinica.domain.user.AuthoritiesBean;
import org.akaza.openclinica.service.PformSubmissionService;
import org.akaza.openclinica.templates.HibernateOcDbTestCase;

import static org.mockito.Mockito.*;

public class SampleTest extends HibernateOcDbTestCase {
String body;

    
    public void testStatement() throws Exception {
 //       org.apache.commons.dbcp.BasicDataSource ds = new org.apache.commons.dbcp.BasicDataSource();

   //     StudyDAO studyDao = new StudyDAO(getDataSource());
    //    StudyBean study = (StudyBean) studyDao.fitndByPK(1);
     //   assertNotNull(study);
        AuthoritiesDao authoritiesDaoMock=mock(AuthoritiesDao.class);

        
/*    	when(authoritiesDaoMock.saveOrUpdate(new AuthoritiesBean("username"))).thenReturn(new AuthoritiesDao());
        
        Datasource ds = new datasource 
        	    PformSubmissionService pform = new PFormSubm(ds)
        	    
        	    
        	    when(mock.saveOrupdate).then(return new Authorities)
        	    PformSubmission pFormSubmission = new PformSubmission(getDatasource(), authorititesDao);
*/    	
  PformSubmissionService pformSubmissionService = new PformSubmissionService(getDataSource(),authoritiesDaoMock);
    pformSubmissionService.saveProcess(body);
    }
}