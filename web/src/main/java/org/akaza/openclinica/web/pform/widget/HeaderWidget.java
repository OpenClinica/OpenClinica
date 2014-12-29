package org.akaza.openclinica.web.pform.widget;

import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.web.pform.dto.Bind;
import org.akaza.openclinica.web.pform.dto.Input;
import org.akaza.openclinica.web.pform.dto.Label;
import org.akaza.openclinica.web.pform.dto.UserControl;

public class HeaderWidget extends BaseWidget {
	private ItemBean item = null;
	private ItemFormMetadataBean itemMetaData = null;
	private ItemGroupBean itemGroup = null;
	private CRFVersionBean version = null;
	private String appearance = null;
	private String expression = null;

	public HeaderWidget(CRFVersionBean version, ItemBean item, ItemFormMetadataBean itemMetaData, ItemGroupBean itemGroup,
			String appearance, String expression) {
		this.item = item;
		this.itemMetaData = itemMetaData;
		this.itemGroup = itemGroup;
		this.version = version;
		this.appearance = appearance;
		this.expression = expression;
	}

	@Override
	public UserControl getUserControl() {
		Input input = new Input();
		Label label = new Label();
		label.setLabel("__" + itemMetaData.getHeader() + "__");
		input.setLabel(label);
		input.setRef("/" + version.getOid() + "/" + itemGroup.getOid() + "/" + item.getOid() + ".HEADER");
		return input;
	}

	@Override
	public Bind getBinding() {
		Bind binding = new Bind();
		binding.setNodeSet("/" + version.getOid() + "/" + itemGroup.getOid() + "/" + item.getOid() + ".HEADER");
		binding.setType("string");
		binding.setReadOnly("true()");
		String relevant = null;
		relevant = expression;
		if (relevant != null)
			binding.setRelevant(relevant);

		return binding;
	}

}
