/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2010 Akaza
 * Research
 *
 */

package org.akaza.openclinica.bean.odmbeans;

import java.util.Date;

/**
 *
 * @author ywang (March, 2010)
 *
 */
public class ChildNoteBean extends ElementOIDBean {
    private String status;
    private Date dateCreated;
    private String description;
    private String detailedNote;
    private ElementRefBean userRef;
    
    private String ownerUserName="";
    private String ownerFirstName="";
    private String ownerLastName="";
    
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getDetailedNote() {
        return detailedNote;
    }
    public void setDetailedNote(String detailedNote) {
        this.detailedNote = detailedNote;
    }
    public Date getDateCreated() {
        return dateCreated;
    }
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }
    public ElementRefBean getUserRef() {
        return userRef;
    }
    public void setUserRef(ElementRefBean userRef) {
        this.userRef = userRef;
    }
	public String getOwnerLastName() {
		return ownerLastName;
	}
	public void setOwnerLastName(String ownerLastName) {
		this.ownerLastName = ownerLastName;
	}
	public String getOwnerUserName() {
		return ownerUserName;
	}
	public void setOwnerUserName(String ownerUserName) {
		this.ownerUserName = ownerUserName;
	}
	public String getOwnerFirstName() {
		return ownerFirstName;
	}
	public void setOwnerFirstName(String ownerFirstName) {
		this.ownerFirstName = ownerFirstName;
	}
}