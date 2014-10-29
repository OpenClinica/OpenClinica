package org.akaza.openclinica.web.pform.widget;

import java.util.ArrayList;

import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ResponseOptionBean;
import org.akaza.openclinica.web.pform.dto.Bind;
import org.akaza.openclinica.web.pform.dto.Hint;
import org.akaza.openclinica.web.pform.dto.Item;
import org.akaza.openclinica.web.pform.dto.Label;
import org.akaza.openclinica.web.pform.dto.Select1;
import org.akaza.openclinica.web.pform.dto.UserControl;

public class Select1Widget extends BaseWidget {

	private ItemBean item = null;
	private CRFVersionBean version = null;
	private String appearance = null;

	public Select1Widget(CRFVersionBean version, ItemBean item, String appearance)
	{
		this.item = item;
		this.version = version;
		this.appearance = appearance;
	}

	@Override
	public UserControl getUserControl() {
		Select1 select1 = new Select1();
		Label label = new Label();
		label.setLabel(item.getItemMeta().getHeader());
		select1.setLabel(label);
		Hint hint = new Hint();
		hint.setHint(item.getItemMeta().getLeftItemText());
		select1.setHint(hint);

		select1.setRef("/" + version.getOid() + "/" + item.getOid());
		select1.setAppearance(appearance);

		ArrayList<Item> itemList = new ArrayList<Item>();
		select1.setItem(itemList);

		ArrayList<ResponseOptionBean> options = item.getItemMeta().getResponseSet().getOptions();
		for (ResponseOptionBean option:options)
		{
			Item item = new Item();
			Label itemLabel = new Label();
			itemLabel.setLabel(option.getText());
			item.setValue(option.getValue());
			item.setLabel(itemLabel);
			itemList.add(item);
		}
		return select1;

	}

	@Override
	public Bind getBinding() {
		Bind binding = new Bind();
		binding.setNodeSet("/" + version.getOid() + "/" + item.getOid());
		binding.setType(getDataType(item));
		if (item.getItemMeta().isRequired()) binding.setRequired("true()");
		return binding;
	}

}
