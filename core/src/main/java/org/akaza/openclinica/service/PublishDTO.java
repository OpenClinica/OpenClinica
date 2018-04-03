/**
 * 
 */
package org.akaza.openclinica.service;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.cdisc.ns.odm.v130.ODM;

/**
 * @author joekeremian
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class PublishDTO {

	@XmlElement
	private Page page;
	@XmlElement
	private ODM odm;

	public Page getPage() {
		return page;
	}

	public void setPage(Page page) {
		this.page = page;
	}

	public ODM getOdm() {
		return odm;
	}

	public void setOdm(ODM odm) {
		this.odm = odm;
	}

}
