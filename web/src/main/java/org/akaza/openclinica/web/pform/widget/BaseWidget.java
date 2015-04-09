package org.akaza.openclinica.web.pform.widget;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.ItemDataType;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.web.pform.dto.Bind;
import org.akaza.openclinica.web.pform.dto.UserControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseWidget implements Widget {

    protected final Logger log = LoggerFactory.getLogger(BaseWidget.class);
	private DataSource ds = null;

	@Override
	public abstract UserControl getUserControl();
	
	@Override
	public abstract Bind getBinding();

	protected String getDataType(ItemBean item)
	{
		String type = ItemDataType.get(item.getItemDataTypeId()).getName();
		
		switch(type)
		{
		case "st": return "string";
		case "int": return "int";
		case "date": return "date";
		case "real": return "float";
		case "bl": return "boolean";
		//TODO: "BN","ED","TEL",FILE"
		case "pdate":
		default: 
			log.debug("Unsupported item data type encountered.  Returning null.");
			System.out.println("Unsupported item data type encountered: " + type + ".  Returning null.");
			return null;
		}
	}
}
