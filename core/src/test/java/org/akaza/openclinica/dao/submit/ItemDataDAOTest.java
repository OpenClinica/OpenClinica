package org.akaza.openclinica.dao.submit;

import junit.framework.TestCase;
import org.akaza.openclinica.bean.core.ItemDataType;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;

import java.util.Locale;

public class ItemDataDAOTest extends TestCase {
	public ItemDataDAOTest() {
	}

	// Scenario
	// Cannot insert a value using rule into a PDATE field from another PDATE field
	public void test_itemDataDaoTest_() {
		ResourceBundleProvider.updateLocale(new Locale("en"));

		ItemDataDAO itemDataDAO = new ItemDataDAO(null);
		ItemDataBean idb = new ItemDataBean();

		idb.setValue("05-Sep-2014");
		ItemDataBean result1 = itemDataDAO.setItemDataBeanIfDateOrPdate(idb, "dd-MMM-yyyy",ItemDataType.DATE);
		assertEquals("2014-09-05", result1.getValue());
		
		idb.setValue("2014-09-05");
		ItemDataBean result2 = itemDataDAO.setItemDataBeanIfDateOrPdate(idb, "yyyy-MM-dd",ItemDataType.DATE);
		assertEquals("2014-09-05", result2.getValue());
		
		
		idb.setValue("05-Sep-2014");
		ItemDataBean result3 = itemDataDAO.setItemDataBeanIfDateOrPdate(idb, "PDATE DOES NOT USE FORMAT",ItemDataType.PDATE);
		assertEquals("2014-09-05", result3.getValue());
		
		idb.setValue("2014-09-05");
		ItemDataBean result4 = itemDataDAO.setItemDataBeanIfDateOrPdate(idb, "PDATE DOES NOT USE FORMAT",ItemDataType.PDATE);
		assertNotSame("2014-09-05", result4.getValue());
		
	
		idb.setValue("Sep-2014");
		ItemDataBean result5 = itemDataDAO.setItemDataBeanIfDateOrPdate(idb, "PDATE DOES NOT USE FORMAT",ItemDataType.PDATE);
		assertEquals("2014-09", result5.getValue());
		
		idb.setValue("2014-09");
		ItemDataBean result6 = itemDataDAO.setItemDataBeanIfDateOrPdate(idb, "PDATE DOES NOT USE FORMAT",ItemDataType.PDATE);
		assertNotSame("2014-09", result6.getValue());
		
		
		idb.setValue("2014");
		ItemDataBean result7 = itemDataDAO.setItemDataBeanIfDateOrPdate(idb, "PDATE DOES NOT USE FORMAT",ItemDataType.PDATE);
		assertEquals("2014", result7.getValue());
		
	
		

		
	}
}