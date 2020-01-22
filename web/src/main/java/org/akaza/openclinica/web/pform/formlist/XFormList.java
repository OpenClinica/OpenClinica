/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.web.pform.formlist;

import java.util.ArrayList;
import java.util.List;

public class XFormList {
	private List<XForm> xforms = null;
	
	public XFormList()
	{
		xforms = new ArrayList<XForm>();
	}

	public void add(XForm xform)
	{
		xforms.add(xform);
	}

	public List<XForm> getXForms() {
		return xforms;
	}

	public void setXForms(List<XForm> xforms) {
		this.xforms = xforms;
	}
	
}
