package org.akaza.openclinica.web.pform.widget;

import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.web.pform.dto.Bind;
import org.akaza.openclinica.web.pform.dto.Hint;
import org.akaza.openclinica.web.pform.dto.Input;
import org.akaza.openclinica.web.pform.dto.Label;
import org.akaza.openclinica.web.pform.dto.UserControl;

public class InputWidget extends BaseWidget {
	private ItemBean item = null;
	private CRFVersionBean version = null;
	private String appearance = null;

	public InputWidget(CRFVersionBean version, ItemBean item, String appearance)
	{
		this.item = item;
		this.version = version;
		this.appearance = appearance;
	}
	
	@Override
	public UserControl getUserControl() {
		Input input = new Input();
		Label label = new Label();
		label.setLabel(item.getItemMeta().getLeftItemText());
		input.setLabel(label);
		Hint hint = new Hint();
		hint.setHint(item.getDescription());
		input.setHint(hint);
		if (appearance != null) input.setAppearance(appearance);
		input.setRef("/" + version.getOid() + "/" + item.getOid());
		return input;
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
