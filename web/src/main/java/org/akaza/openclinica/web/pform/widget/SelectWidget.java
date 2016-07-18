package org.akaza.openclinica.web.pform.widget;

import org.akaza.openclinica.bean.submit.*;
import org.akaza.openclinica.web.pform.dto.*;

import java.util.ArrayList;

public class SelectWidget extends BaseWidget {

	private ItemBean item = null;
	private CRFVersionBean version = null;
	private String appearance = null;
	private ItemGroupBean itemGroupBean = null;
	private ItemFormMetadataBean itemFormMetadataBean = null;
	private boolean isItemRequired;
	private String expression;

	public SelectWidget(CRFVersionBean version, ItemBean item, String appearance, ItemGroupBean itemGroupBean,
			ItemFormMetadataBean itemFormMetadataBean, boolean isItemRequired, 
			String expression)

	{
		this.item = item;
		this.version = version;
		this.appearance = appearance;
		this.itemGroupBean = itemGroupBean;
		this.itemFormMetadataBean = itemFormMetadataBean;
		this.isItemRequired = isItemRequired;
		this.expression = expression;
	}

	@Override
	public UserControl getUserControl() {
		Select select = new Select();
		Label label = new Label();
		label.setLabel(itemFormMetadataBean.getLeftItemText());
		select.setLabel(label);
		// Hint hint = new Hint();
		// hint.setHint(item.getItemMeta().getLeftItemText());
		// select.setHint(hint);

		select.setRef("/" + version.getOid() + "/" + itemGroupBean.getOid() + "/" + item.getOid());
		select.setAppearance(appearance);

		ArrayList<Item> itemList = new ArrayList<Item>();
		select.setItem(itemList);

		ArrayList<ResponseOptionBean> options = itemFormMetadataBean.getResponseSet().getOptions();
		for (ResponseOptionBean option : options) {
			Item item = new Item();
			Label itemLabel = new Label();
			itemLabel.setLabel(option.getText());
			item.setValue(option.getValue());
			item.setLabel(itemLabel);
			itemList.add(item);
		}
		return select;

	}

	@Override
	public Bind getBinding() {
		Bind binding = new Bind();
		String relevant = null;

		binding.setNodeSet("/" + version.getOid() + "/" + itemGroupBean.getOid() + "/" + item.getOid());
		relevant = expression;
		if (relevant != null)
			binding.setRelevant(relevant);
		binding.setType("select");
		if (isItemRequired)
			binding.setRequired("true()");
		return binding;
	}
}
