package org.akaza.openclinica.service.rule.expression;


import java.util.Locale;

import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.PformSubmissionService;
import org.akaza.openclinica.templates.OcDbTestCase;


public class SampleTest extends OcDbTestCase {


    public void setup(){
    	ResourceBundleProvider.updateLocale(new Locale("en"));
    	    	
    }
    
    public void testStatement() throws Exception {

   //     StudyDAO studyDao = new StudyDAO(getDataSource());
    //    StudyBean study = (StudyBean) studyDao.fitndByPK(1);
     //   assertNotNull(study);
    //    AuthoritiesDao authoritiesDaoMock=mock(AuthoritiesDao.class);
    //	when(authoritiesDaoMock.saveOrUpdate(new AuthoritiesBean("username"))).thenReturn(new AuthoritiesDao());
        
    	
PformSubmissionService pformSubmissionService = new PformSubmissionService(getDataSource());
    pformSubmissionService.saveProcess();
    }
}