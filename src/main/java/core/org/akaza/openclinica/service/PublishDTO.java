/**
 * 
 */
package core.org.akaza.openclinica.service;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.akaza.openclinica.service.Page;
import org.cdisc.ns.odm.v130.ODM;
import java.util.List;

/**
 * @author joekeremian
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class PublishDTO {

	@XmlElement
	private List<Page> pages;
	@XmlElement
	private ODM odm;

	public String getBoardId() {
		return boardId;
	}

	public void setBoardId(String boardId) {
		this.boardId = boardId;
	}

	@XmlElement
	private String boardId;

	public List<Page> getPages() {
		return pages;
	}

	public void setPages(List<Page> pages) {
		this.pages = pages;
	}

	public ODM getOdm() {
		return odm;
	}

	public void setOdm(ODM odm) {
		this.odm = odm;
	}



}
