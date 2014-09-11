/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2011 Akaza Research
 */
package org.akaza.openclinica.service.crfdata.front;


import java.io.Serializable;
import java.util.Map;

/**
 * Collection of InstantOnChangeFrontStrGroup in a crf section.
 */
//ywang (Aug., 2011)
public class InstantOnChangeFrontStrParcel implements Serializable{
    private static final long serialVersionUID = -2240052862489035165L;

    int sectionId;
    int crfVersionId;
    /**
     * String key = origin item-group-oid; Integer key = origin item_id
     */
    Map<String,Map<Integer,InstantOnChangeFrontStrGroup>> repOrigins;
    /**
     * Include both non-repeating group & Ungrouped. key = origin item_id
     */
    Map<Integer,InstantOnChangeFrontStrGroup> nonRepOrigins;

    public int getSectionId() {
        return sectionId;
    }
    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }
    public int getCrfVersionId() {
        return crfVersionId;
    }
    public void setCrfVersionId(int crfVersionId) {
        this.crfVersionId = crfVersionId;
    }
    public Map<String, Map<Integer, InstantOnChangeFrontStrGroup>> getRepOrigins() {
        return repOrigins;
    }
    public void setRepOrigins(Map<String, Map<Integer, InstantOnChangeFrontStrGroup>> repOrigins) {
        this.repOrigins = repOrigins;
    }
    public Map<Integer, InstantOnChangeFrontStrGroup> getNonRepOrigins() {
        return nonRepOrigins;
    }
    public void setNonRepOrigins(Map<Integer, InstantOnChangeFrontStrGroup> nonRepOrigins) {
        this.nonRepOrigins = nonRepOrigins;
    }

}