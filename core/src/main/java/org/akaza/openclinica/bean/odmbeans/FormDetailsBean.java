/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 
 *
 */

package org.akaza.openclinica.bean.odmbeans;

import java.util.ArrayList;
import java.util.List;

public class FormDetailsBean extends ElementDefBean {
    // attributes
    private String parentFormOid;

    // elements
    private String description;
    private String versionDescription;
    private String revisionNotes;
    private ArrayList<PresentInEventDefinitionBean> presentInEventDefinitions = new ArrayList<PresentInEventDefinitionBean>();

    private List<SectionDetails> SectionDetails;

    public ArrayList<SectionDetails> getSectionDetails() {
        return (ArrayList<org.akaza.openclinica.bean.odmbeans.SectionDetails>) SectionDetails;
    }

    public void setSectionDetails(List<SectionDetails> sectionDetails) {
        SectionDetails = sectionDetails;
    }

    public String getParentFormOid() {
        return parentFormOid;
    }

    public void setParentFormOid(String parentFormOid) {
        this.parentFormOid = parentFormOid;
    }

    public String getVersionDescription() {
        return versionDescription;
    }

    public void setVersionDescription(String versionDescription) {
        this.versionDescription = versionDescription;
    }

    public String getRevisionNotes() {
        return revisionNotes;
    }

    public void setRevisionNotes(String revisionNotes) {
        this.revisionNotes = revisionNotes;
    }

    public ArrayList<PresentInEventDefinitionBean> getPresentInEventDefinitions() {
        return presentInEventDefinitions;
    }

    public void setPresentInEventDefinitions(ArrayList<PresentInEventDefinitionBean> presentInEventDefinitions) {
        this.presentInEventDefinitions = presentInEventDefinitions;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}