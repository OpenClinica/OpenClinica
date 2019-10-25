package core.org.akaza.openclinica.web.pform.widget;

import core.org.akaza.openclinica.bean.submit.CRFVersionBean;
import core.org.akaza.openclinica.bean.submit.ItemBean;
import core.org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import core.org.akaza.openclinica.bean.submit.ItemGroupBean;
import core.org.akaza.openclinica.web.pform.dto.Bind;
import core.org.akaza.openclinica.web.pform.dto.Input;
import core.org.akaza.openclinica.web.pform.dto.Label;
import core.org.akaza.openclinica.web.pform.dto.UserControl;

public class SubHeaderWidget extends BaseWidget {
	private ItemBean item = null;
	private ItemFormMetadataBean itemMetaData = null;
	private ItemGroupBean itemGroup = null;
	private CRFVersionBean version = null;
	private String appearance = null;
	private String expression;

	public SubHeaderWidget(CRFVersionBean version, ItemBean item, ItemFormMetadataBean itemMetaData, ItemGroupBean itemGroup,
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
		label.setLabel(itemMetaData.getSubHeader());
		input.setLabel(label);
		input.setRef("/" + version.getOid() + "/" + itemGroup.getOid() + "/" + item.getOid() + ".SUBHEADER");
		return input;
	}

	@Override
	public Bind getBinding() {

		Bind binding = new Bind();
		binding.setNodeSet("/" + version.getOid() + "/" + itemGroup.getOid() + "/" + item.getOid() + ".SUBHEADER");
		binding.setType("string");
		binding.setReadOnly("true()");
		String relevant = null;
		relevant = expression;
		if (relevant != null)
			binding.setRelevant(relevant);

		return binding;
	}

}
