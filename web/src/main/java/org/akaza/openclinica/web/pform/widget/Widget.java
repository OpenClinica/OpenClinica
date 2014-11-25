package org.akaza.openclinica.web.pform.widget;

import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.web.pform.dto.Bind;
import org.akaza.openclinica.web.pform.dto.UserControl;

public interface Widget {
	
	public static final String APPEARANCE_FULL = "full";
	public static final String APPEARANCE_COMPACT = "compact";
	public static final String APPEARANCE_MINIMAL = "minimal";
	public static final String APPEARANCE_MULTILINE = "multiline";
	
	public UserControl getUserControl();
	public Bind getBinding();
}
