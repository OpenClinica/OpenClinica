package org.akaza.openclinica.web.pform.widget;

import org.akaza.openclinica.web.pform.dto.Bind;
import org.akaza.openclinica.web.pform.dto.Input;
import org.akaza.openclinica.web.pform.dto.Label;
import org.akaza.openclinica.web.pform.dto.UserControl;

public class SectionTextWidget implements Widget {

	private String versionOid = null;
	private String text = null;
	private Integer sectionId = 0;
	private String textType = null;
	
	public SectionTextWidget(String versionOid, String text, Integer sectionId, String textType)
	{
		this.versionOid = versionOid;
		this.text = text;
		this.sectionId = sectionId;
		this.textType = textType;
	}
	
	@Override
	public UserControl getUserControl() {
		Input input = new Input();
		Label label = new Label();
		label.setLabel(text);
		input.setLabel(label);
		input.setRef("/" + versionOid + "/SECTION_" + String.valueOf(sectionId) + "." + textType);
		return input;
	}

	@Override
	public Bind getBinding() {
		Bind binding = new Bind();
		binding.setNodeSet("/" + versionOid + "/SECTION_" + String.valueOf(sectionId) + "." + textType);
		binding.setType("string");
		binding.setReadOnly("true()");
		return binding;
	}

}
