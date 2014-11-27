package org.akaza.openclinica.web.pform.widget;

import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WidgetFactory {

	public static final int TYPE_TEXT = 1;
	public static final int TYPE_TEXTAREA = 2;
	public static final int TYPE_CHECKBOX = 3;
	public static final int TYPE_FILE = 4;
	public static final int TYPE_RADIO = 5;
	public static final int TYPE_SINGLE_SELECT = 6;
	public static final int TYPE_MULTI_SELECT = 7;
	public static final int TYPE_CALCULATION = 8;
	public static final int TYPE_GROUP_CALCULATION = 9;
	public static final int TYPE_INSTANT_CALCULATION = 10;

    protected final Logger log = LoggerFactory.getLogger(WidgetFactory.class);

	private CRFVersionBean version = null;
	
	public WidgetFactory(CRFVersionBean version)
	{
		this.version = version;
	}
	public Widget getWidget(ItemBean item , Integer widgetType,ItemGroupBean itemGroupBean,ItemFormMetadataBean itemFormMetaDataBean , Integer itemGrouprepeatNumber, boolean isItemRequired,boolean isGroupRepeating)
	{		
	//	int widgetType = item.getItemMeta().getResponseSet().getResponseType().getId();                                                                                    
       // int widgetType = 1;		
		
		switch (widgetType)
		{
			case TYPE_TEXT: return new InputWidget(version, item, null,itemGroupBean,itemFormMetaDataBean,itemGrouprepeatNumber,isItemRequired,isGroupRepeating);
			case TYPE_SINGLE_SELECT: return new Select1Widget(version, item,Widget.APPEARANCE_MINIMAL,itemGroupBean,itemFormMetaDataBean,itemGrouprepeatNumber,isItemRequired,isGroupRepeating);
			case TYPE_RADIO:  return new Select1Widget(version,item,Widget.APPEARANCE_FULL,itemGroupBean,itemFormMetaDataBean,itemGrouprepeatNumber,isItemRequired,isGroupRepeating);
			case TYPE_MULTI_SELECT: return new SelectWidget(version,item,Widget.APPEARANCE_MINIMAL,itemGroupBean,itemFormMetaDataBean,itemGrouprepeatNumber,isItemRequired,isGroupRepeating);
			case TYPE_CHECKBOX: return new SelectWidget(version,item,Widget.APPEARANCE_FULL,itemGroupBean,itemFormMetaDataBean,itemGrouprepeatNumber,isItemRequired,isGroupRepeating);
			case TYPE_TEXTAREA: return new InputWidget(version, item, Widget.APPEARANCE_MULTILINE,itemGroupBean,itemFormMetaDataBean,itemGrouprepeatNumber,isItemRequired,isGroupRepeating);
			default: 
				log.debug("Unsupported form widget: " + widgetType + "  Skipping.");
				return null;
		}
	}
}
