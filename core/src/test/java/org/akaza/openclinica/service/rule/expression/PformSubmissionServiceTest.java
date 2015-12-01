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

	PformSubmissionService pformSubmissionService = new PformSubmissionService(null);
	Errors errors = pformSubmissionService.instanciateErrors();
	ItemDataBean itemDataBean = new ItemDataBean();
	ItemBean itemBean = new ItemBean();

	// item Data Type is Integer , expecting integer value
	public void testIntegerValues() throws Exception {

		itemDataBean.setValue("123"); // Pass
		itemBean.setItemDataTypeId(6);
		errors = pformSubmissionService.validateItemData(itemDataBean, itemBean, 1);
		assertFalse(errors.hasErrors());

		itemDataBean.setValue("123be"); // Fail
		itemBean.setItemDataTypeId(6);
		errors = pformSubmissionService.validateItemData(itemDataBean, itemBean, 1);
		assertTrue(errors.hasErrors());

		itemDataBean.setValue("123.45"); // Fail
		itemBean.setItemDataTypeId(6);
		errors = pformSubmissionService.validateItemData(itemDataBean, itemBean, 1);
		assertTrue(errors.hasErrors());

		itemDataBean.setValue("hello"); // Fail
		itemBean.setItemDataTypeId(6);
		errors = pformSubmissionService.validateItemData(itemDataBean, itemBean, 1);
		assertTrue(errors.hasErrors());
	}

	// item Data Type is Float/Real , expecting Float value
	public void testFloatValues() throws Exception {

		itemDataBean.setValue("123.45"); // Pass
		itemBean.setItemDataTypeId(7);
		errors = pformSubmissionService.validateItemData(itemDataBean, itemBean, 1);
		assertFalse(errors.hasErrors());

		itemDataBean.setValue("123"); // Pass
		itemBean.setItemDataTypeId(7);
		errors = pformSubmissionService.validateItemData(itemDataBean, itemBean, 1);
		assertFalse(errors.hasErrors());

		itemDataBean.setValue("123be"); // Fail
		itemBean.setItemDataTypeId(7);
		errors = pformSubmissionService.validateItemData(itemDataBean, itemBean, 1);
		assertTrue(errors.hasErrors());

		itemDataBean.setValue("hello"); // Fail
		itemBean.setItemDataTypeId(7);
		errors = pformSubmissionService.validateItemData(itemDataBean, itemBean, 1);
		assertTrue(errors.hasErrors());
	}

	// item Data Type is Date , expecting Date value
	public void testDateValues() throws Exception {

		itemDataBean.setValue("2014-10-10"); // Pass
		itemBean.setItemDataTypeId(9);
		errors = pformSubmissionService.validateItemData(itemDataBean, itemBean, 1);
		assertFalse(errors.hasErrors());

		itemDataBean.setValue("2014-10"); // Fail
		itemBean.setItemDataTypeId(9);
		errors = pformSubmissionService.validateItemData(itemDataBean, itemBean, 1);
		assertTrue(errors.hasErrors());

		itemDataBean.setValue("2014-31-31"); // Fail
		itemBean.setItemDataTypeId(9);
		errors = pformSubmissionService.validateItemData(itemDataBean, itemBean, 1);
		assertTrue(errors.hasErrors());

		itemDataBean.setValue("01-01-2014"); // Fail
		itemBean.setItemDataTypeId(9);
		errors = pformSubmissionService.validateItemData(itemDataBean, itemBean, 1);
		assertTrue(errors.hasErrors());

		itemDataBean.setValue("10-Mar-2014"); // Fail
		itemBean.setItemDataTypeId(9);
		errors = pformSubmissionService.validateItemData(itemDataBean, itemBean, 1);
		assertTrue(errors.hasErrors());

		itemDataBean.setValue("123.45"); // Fail
		itemBean.setItemDataTypeId(9);
		errors = pformSubmissionService.validateItemData(itemDataBean, itemBean, 1);
		assertTrue(errors.hasErrors());

		itemDataBean.setValue("123"); // Fail
		itemBean.setItemDataTypeId(9);
		errors = pformSubmissionService.validateItemData(itemDataBean, itemBean, 1);
		assertTrue(errors.hasErrors());

		itemDataBean.setValue("123be"); // Fail
		itemBean.setItemDataTypeId(9);
		errors = pformSubmissionService.validateItemData(itemDataBean, itemBean, 1);
		assertTrue(errors.hasErrors());

		itemDataBean.setValue("hello"); // Fail
		itemBean.setItemDataTypeId(9);
		errors = pformSubmissionService.validateItemData(itemDataBean, itemBean, 1);
		assertTrue(errors.hasErrors());
	}

	// Checkbox or MultiSelect Response Type test: item Data Type is Integer ,
	// expecting integer value
	public void testIntegerValuesInCheckBox() throws Exception {

		itemDataBean.setValue("123,456,789,001"); // Pass
		itemBean.setItemDataTypeId(6);
		errors = pformSubmissionService.validateItemData(itemDataBean, itemBean, 3);
		assertFalse(errors.hasErrors());

		itemDataBean.setValue("123,456,789,001"); // Pass
		itemBean.setItemDataTypeId(6);
		errors = pformSubmissionService.validateItemData(itemDataBean, itemBean, 7);
		assertFalse(errors.hasErrors());

		itemDataBean.setValue("12.3,456,789,001"); // Fail
		itemBean.setItemDataTypeId(6);
		errors = pformSubmissionService.validateItemData(itemDataBean, itemBean, 3);
		assertTrue(errors.hasErrors());

		itemDataBean.setValue("123,456,789,aaa"); // Fail
		itemBean.setItemDataTypeId(6);
		errors = pformSubmissionService.validateItemData(itemDataBean, itemBean, 7);
		assertTrue(errors.hasErrors());

		// Negative testing for response type of Radio Button
		itemDataBean.setValue("123,456,789,001"); // Fail
		itemBean.setItemDataTypeId(6);
		errors = pformSubmissionService.validateItemData(itemDataBean, itemBean, 5);
		assertTrue(errors.hasErrors());

		// Negative testing for response type of Single Select
		itemDataBean.setValue("123,456,789,001"); // Fail
		itemBean.setItemDataTypeId(6);
		errors = pformSubmissionService.validateItemData(itemDataBean, itemBean, 6);
		assertTrue(errors.hasErrors());

	}

	// Checkbox or MultiSelect Response Type test: item Data Type is Float/Real
	// , expecting Float value
	public void testFloatValuesInCheckbox() throws Exception {

		itemDataBean.setValue("123.45,56.75,67,982.25"); // Pass
		itemBean.setItemDataTypeId(7);
		errors = pformSubmissionService.validateItemData(itemDataBean, itemBean, 3);
		assertFalse(errors.hasErrors());

		itemDataBean.setValue("123.45,56.75,67,982.25"); // Pass
		itemBean.setItemDataTypeId(7);
		errors = pformSubmissionService.validateItemData(itemDataBean, itemBean, 7);
		assertFalse(errors.hasErrors());

		itemDataBean.setValue("123.45,56.75,abc,982.25"); // Fail
		itemBean.setItemDataTypeId(7);
		errors = pformSubmissionService.validateItemData(itemDataBean, itemBean, 3);
		assertTrue(errors.hasErrors());

		itemDataBean.setValue("123.45,56.75,67,2014-12-10"); // Fail
		itemBean.setItemDataTypeId(7);
		errors = pformSubmissionService.validateItemData(itemDataBean, itemBean, 7);
		assertTrue(errors.hasErrors());

		// Negative testing for response type of Radio Button
		itemDataBean.setValue("123.45,56.75,67,982.25"); // Fail
		itemBean.setItemDataTypeId(7);
		errors = pformSubmissionService.validateItemData(itemDataBean, itemBean, 5);
		assertTrue(errors.hasErrors());

		// Negative testing for response type of Single Select
		itemDataBean.setValue("123.45,56.75,67,982.25"); // Fail
		itemBean.setItemDataTypeId(7);
		errors = pformSubmissionService.validateItemData(itemDataBean, itemBean, 6);
		assertTrue(errors.hasErrors());

	}

	// Checkbox or MultiSelect Response Type test: item Data Type is Date ,
	// expecting Date value
	public void testDateValuesInCheckbox() throws Exception {

		itemDataBean.setValue("2014-10-10,2000-01-01,1962-09-05,1999-07-09"); // Pass
		itemBean.setItemDataTypeId(9);
		errors = pformSubmissionService.validateItemData(itemDataBean, itemBean, 3);
		assertFalse(errors.hasErrors());

		itemDataBean.setValue("2014-10-10,2000-01-01,1962-09-05,1999-07-09"); // Pass
		itemBean.setItemDataTypeId(9);
		errors = pformSubmissionService.validateItemData(itemDataBean, itemBean, 7);
		assertFalse(errors.hasErrors());

		itemDataBean.setValue("2014-32-10,2000-01-01,1962-09-05,1999-07-09"); // Fail
		itemBean.setItemDataTypeId(9);
		errors = pformSubmissionService.validateItemData(itemDataBean, itemBean, 3);
		assertTrue(errors.hasErrors());

		itemDataBean.setValue("2014-10-10,2000-01-01,1962-31-31,1999-07-09"); // Fail
		itemBean.setItemDataTypeId(9);
		errors = pformSubmissionService.validateItemData(itemDataBean, itemBean, 7);
		assertTrue(errors.hasErrors());

		// Negative testing for response type of Radio Button
		itemDataBean.setValue("2014-10-10,2000-01-01,1962-09-05,1999-07-09"); // Fail
		itemBean.setItemDataTypeId(9);
		errors = pformSubmissionService.validateItemData(itemDataBean, itemBean, 5);
		assertTrue(errors.hasErrors());

		// Negative testing for response type of Single Select
		itemDataBean.setValue("2014-10-10,2000-01-01,1962-09-05,1999-07-09"); // Fail
		itemBean.setItemDataTypeId(9);
		errors = pformSubmissionService.validateItemData(itemDataBean, itemBean, 6);
		assertTrue(errors.hasErrors());

	}

}
