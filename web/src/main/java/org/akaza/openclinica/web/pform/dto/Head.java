/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.web.pform.dto;

public class Head {
	
	private String title;
	private Model model;

	public String getTitle() {
	  return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Model getModel() {
		return model;
	}
	public void setModel(Model model) {
		this.model = model;
	}
}