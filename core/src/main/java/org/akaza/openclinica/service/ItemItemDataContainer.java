package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;

public class ItemItemDataContainer {
	private ItemBean itemBean;
	private ItemDataBean itemDataBean;
	
	public ItemItemDataContainer(ItemBean itemBean, ItemDataBean itemDataBean) {
		super();
		this.itemBean = itemBean;
		this.itemDataBean = itemDataBean;
	}
	public ItemBean getItemBean() {
		return itemBean;
	}
	public void setItemBean(ItemBean itemBean) {
		this.itemBean = itemBean;
	}
	public ItemDataBean getItemDataBean() {
		return itemDataBean;
	}
	public void setItemDataBean(ItemDataBean itemDataBean) {
		this.itemDataBean = itemDataBean;
	}
}
