package org.akaza.openclinica.web.pform.widget;

import java.util.ArrayList;

import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.bean.submit.ResponseOptionBean;
import org.akaza.openclinica.bean.submit.SectionBean;
import org.akaza.openclinica.domain.rule.expression.ExpressionBean;
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
	private ItemGroupBean itemGroupBean=null;
	private ItemFormMetadataBean itemFormMetadataBean=null;
	private Integer itemGroupRepeatNumber;
	private boolean isItemRequired;
	private boolean isGroupRepeating;
	private ItemBean itemTargetBean;
	private String expression;
	private SectionBean section;
	
	public Select1Widget(CRFVersionBean version, ItemBean item, String appearance, ItemGroupBean itemGroupBean,
			ItemFormMetadataBean itemFormMetadataBean, Integer itemGroupRepeatNumber, boolean isItemRequired,
			boolean isGroupRepeating, ItemBean itemTargetBean , String expression, SectionBean section)
	{
		this.item = item;
		this.version = version;
		this.appearance = appearance;
		this.itemGroupBean=itemGroupBean;
		this.itemFormMetadataBean=itemFormMetadataBean;
		this.itemGroupRepeatNumber=itemGroupRepeatNumber;
        this.isItemRequired=isItemRequired;
        this.isGroupRepeating=isGroupRepeating;
        this.itemTargetBean=itemTargetBean;
        this.expression=expression;
        this.section=section;
	}
	
	

	@Override
	public UserControl getUserControl() {
		Select1 select1 = new Select1();
		Label label = new Label();
			label.setLabel(itemFormMetadataBean.getLeftItemText());
		select1.setLabel(label);
		//Hint hint = new Hint();
		//hint.setHint(item.getItemMeta().getLeftItemText());
		//select1.setHint(hint);
		select1.setRef("/" + version.getOid()+ "/"+itemGroupBean.getOid()+"/" + item.getOid());
		select1.setAppearance(appearance);

		ArrayList<Item> itemList = new ArrayList<Item>();
		select1.setItem(itemList);

		ArrayList<ResponseOptionBean> options = itemFormMetadataBean.getResponseSet().getOptions();
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
		String relevant=null;
		binding.setNodeSet("/" + version.getOid() +"/" + itemGroupBean.getOid() +"/" + item.getOid());
		if (itemTargetBean!=null){
			relevant=expression;
		}
	    	binding.setRelevant(relevant);
		binding.setType("select1");

		if (isItemRequired) binding.setRequired("true()");
		return binding;
	}



	


}
