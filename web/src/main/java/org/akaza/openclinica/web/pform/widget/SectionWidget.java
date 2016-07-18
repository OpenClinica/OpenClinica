package org.akaza.openclinica.web.pform.widget;

import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.SectionBean;
import org.akaza.openclinica.web.pform.dto.Bind;
import org.akaza.openclinica.web.pform.dto.UserControl;

public class SectionWidget extends BaseWidget {
	private SectionBean section = null;
	private CRFVersionBean version = null;
	private String expression = null;

	public SectionWidget(SectionBean section, CRFVersionBean version, String expression) {
		this.section = section;
		this.version = version;
		this.expression = expression;
	}

	@Override
	public Bind getBinding() {
		Bind binding = new Bind();
		String relevant = expression;
		if (relevant != null)
			binding.setRelevant(relevant);

		binding.setNodeSet("/" + version.getOid() + "/" + "SECTION_"+ section.getLabel().replaceAll("\\W", "_"));
		return binding;
	}

	@Override
	public UserControl getUserControl() {
		// TODO Auto-generated method stub
		return null;
	}

}
