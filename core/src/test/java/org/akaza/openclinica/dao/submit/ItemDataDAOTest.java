package org.akaza.openclinica.dao.submit;

import java.util.Locale;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.core.ItemDataType;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;

import static org.mockito.Mockito.*;
import junit.framework.TestCase;

public class ItemDataDAOTest extends TestCase {
	public ItemDataDAOTest() {
	}

	// Scenario
	// Cannot insert a value using rule into a PDATE field from another PDATE field
	public void test_itemDataDaoTest_() {
		ResourceBundleProvider.updateLocale(new Locale("en"));

		ItemDataDAO itemDataDAO = new ItemDataDAO(null);
		ItemDataBean idb = new ItemDataBean();

		// Positive Testing PDATE Year , Month and Day
		idb.setValue("05-Sep-2014");
		ItemDataBean result1 = itemDataDAO.convertValueByDataType(idb, ItemDataType.PDATE);
		assertEquals("2014-09-05", result1.getValue());

		// Positive Testing PDATE Year and Month
		idb.setValue("Sep-2014");
		ItemDataBean result2 = itemDataDAO.convertValueByDataType(idb, ItemDataType.PDATE);
		assertEquals("2014-09", result2.getValue());

		// Positive Testing PDATE Year only
		idb.setValue("2014");
		ItemDataBean result3 = itemDataDAO.convertValueByDataType(idb, ItemDataType.PDATE);
		assertEquals("2014", result3.getValue());

		// Positive Testing DATE Year , Month and Day
		idb.setValue("05-Sep-2014");
		ItemDataBean result4 = itemDataDAO.convertValueByDataType(idb, ItemDataType.DATE);
		assertEquals("2014-09-05", result4.getValue());

		
		
		
		// Negative Testing
		idb.setValue("05-Sep-2014");
		ItemDataBean result5 = itemDataDAO.convertValueByDataType(idb, ItemDataType.BL);
		assertEquals("05-Sep-2014", result5.getValue());

		// Negative Testing
		idb.setValue("Sep-2014");
		ItemDataBean result6 = itemDataDAO.convertValueByDataType(idb, ItemDataType.ST);
		assertEquals("Sep-2014", result6.getValue());

		// Negative Testing Full Year
		idb.setValue("2014");
		ItemDataBean result7 = itemDataDAO.convertValueByDataType(idb, ItemDataType.INTEGER);
		assertEquals("2014", result7.getValue());

	}
}