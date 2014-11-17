package org.akaza.openclinica.service.rule.expression;

import java.util.Locale;

import javax.validation.constraints.AssertTrue;

import junit.framework.TestCase;

import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.dao.hibernate.AuthoritiesDao;
import org.akaza.openclinica.domain.user.AuthoritiesBean;
import org.akaza.openclinica.service.PformSubmissionService;
import org.akaza.openclinica.service.PformValidator;
import org.akaza.openclinica.templates.HibernateOcDbTestCase;
import org.springframework.validation.Errors;

import static org.mockito.Mockito.*;

public class PformSubmissionServiceTest extends TestCase {

	PformSubmissionService pformSubmissionService = new PformSubmissionService(null, null);
	Errors errors = pformSubmissionService.instanciateErrors();
    ItemDataBean itemDataBean = new ItemDataBean();
    ItemBean itemBean = new ItemBean();
	
	
 // item Data Type is Integer , expecting integer value 
	public void testIntegerValues() throws Exception {
	    
		itemDataBean.setValue("123");   // Pass
		itemBean.setItemDataTypeId(6);
		errors = pformSubmissionService.validateItemData(itemDataBean, itemBean);
		assertFalse(errors.hasErrors());
		
		itemDataBean.setValue("123be");  //Fail
		itemBean.setItemDataTypeId(6);
		errors = pformSubmissionService.validateItemData(itemDataBean, itemBean);
		assertTrue(errors.hasErrors());
	
		itemDataBean.setValue("123.45");   //Fail
		itemBean.setItemDataTypeId(6);
		errors = pformSubmissionService.validateItemData(itemDataBean, itemBean);
		assertTrue(errors.hasErrors());

		itemDataBean.setValue("hello");  //Fail
		itemBean.setItemDataTypeId(6);
		errors = pformSubmissionService.validateItemData(itemDataBean, itemBean);
		assertTrue(errors.hasErrors());
	}


	 // item Data Type is Float/Real , expecting Float value 
		public void testFloatValues() throws Exception {
		
			itemDataBean.setValue("123.45");  //Pass
			itemBean.setItemDataTypeId(7);
			errors = pformSubmissionService.validateItemData(itemDataBean, itemBean);
			assertFalse(errors.hasErrors());

			itemDataBean.setValue("123");  //Pass
			itemBean.setItemDataTypeId(7);
			errors = pformSubmissionService.validateItemData(itemDataBean, itemBean);
			assertFalse(errors.hasErrors());
			
			itemDataBean.setValue("123be");  //Fail
			itemBean.setItemDataTypeId(7);
			errors = pformSubmissionService.validateItemData(itemDataBean, itemBean);
			assertTrue(errors.hasErrors());
		

			itemDataBean.setValue("hello");  //Fail
			itemBean.setItemDataTypeId(7);
			errors = pformSubmissionService.validateItemData(itemDataBean, itemBean);
			assertTrue(errors.hasErrors());
		}



		 // item Data Type is Date , expecting Date value 
			public void testDateValues() throws Exception {

				itemDataBean.setValue("2014-10-10");  //Pass
				itemBean.setItemDataTypeId(9);
				errors = pformSubmissionService.validateItemData(itemDataBean, itemBean);
				assertFalse(errors.hasErrors());

				itemDataBean.setValue("2014-10");  //Fail
				itemBean.setItemDataTypeId(9);
				errors = pformSubmissionService.validateItemData(itemDataBean, itemBean);
				assertTrue(errors.hasErrors());

				itemDataBean.setValue("2014-31-31");  //Fail
				itemBean.setItemDataTypeId(9);
				errors = pformSubmissionService.validateItemData(itemDataBean, itemBean);
				assertTrue(errors.hasErrors());

				itemDataBean.setValue("01-01-2014");  //Fail
				itemBean.setItemDataTypeId(9);
				errors = pformSubmissionService.validateItemData(itemDataBean, itemBean);
				assertTrue(errors.hasErrors());

				itemDataBean.setValue("10-Mar-2014");  //Fail
				itemBean.setItemDataTypeId(9);
				errors = pformSubmissionService.validateItemData(itemDataBean, itemBean);
				assertTrue(errors.hasErrors());
				
				itemDataBean.setValue("123.45");  //Fail
				itemBean.setItemDataTypeId(9);
				errors = pformSubmissionService.validateItemData(itemDataBean, itemBean);
				assertTrue(errors.hasErrors());

				itemDataBean.setValue("123");  //Fail
				itemBean.setItemDataTypeId(9);
				errors = pformSubmissionService.validateItemData(itemDataBean, itemBean);
				assertTrue(errors.hasErrors());
				
				itemDataBean.setValue("123be");  //Fail
				itemBean.setItemDataTypeId(9);
				errors = pformSubmissionService.validateItemData(itemDataBean, itemBean);
				assertTrue(errors.hasErrors());

				itemDataBean.setValue("hello");  //Fail
				itemBean.setItemDataTypeId(9);
				errors = pformSubmissionService.validateItemData(itemDataBean, itemBean);
				assertTrue(errors.hasErrors());
			}


}