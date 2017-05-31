/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.bean.submit;

import java.util.Date;

import org.akaza.openclinica.bean.core.AuditableEntityBean;
import org.akaza.openclinica.bean.oid.CrfVersionOidGenerator;
import org.akaza.openclinica.bean.oid.OidGenerator;

/**
 * The object to carry CRF versions in the application.
 * 
 * @author thickerson
 * 
 */
public class FormLayoutBean extends AuditableEntityBean {

    private String description = "";
    private int crfId = 0;
    private int statusId = 1;
    private String revisionNotes = "";
    private Date date_created;

    // not in DB, tells whether the spreadsheet is downloadable
    private boolean downloadable = false;
    // not in DB, stores a URL for displaying the CRF in Enketo
    private String enketoURL = "";

    private String oid;
    private OidGenerator oidGenerator;

    private String xform;
    private String xformName;
    private String url;

    public FormLayoutBean() {
        this.oidGenerator = new CrfVersionOidGenerator();
        date_created = new Date();
    }

    /**
     * @return date_created Date
     */
    public Date getDateCreated() {
        return date_created;
    }

    /**
     * @return Returns the cRFId.
     */
    public int getCrfId() {
        return crfId;
    }

    /**
     * @param id
     *            The cRFId to set.
     */
    public void setCrfId(int id) {
        crfId = id;
    }

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description
     *            The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return Returns the revisionNotes.
     */
    public String getRevisionNotes() {
        return revisionNotes;
    }

    /**
     * @param revisionNotes
     *            The revisionNotes to set.
     */

    public void setRevisionNotes(String revisionNotes) {
        this.revisionNotes = revisionNotes;

    }

    /**
     * @return Returns the statusId.
     */
    public int getStatusId() {
        return statusId;
    }

    /**
     * @param statusId
     *            The statusId to set.
     */
    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    /**
     * @return Returns the downloadable.
     */
    public boolean isDownloadable() {
        return downloadable;
    }

    /**
     * @param downloadable
     *            The downloadable to set.
     */
    public void setDownloadable(boolean downloadable) {
        this.downloadable = downloadable;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public OidGenerator getOidGenerator() {
        return oidGenerator;
    }

    public void setOidGenerator(OidGenerator oidGenerator) {
        this.oidGenerator = oidGenerator;
    }

    public String getXform() {
        return xform;
    }

    public void setXform(String xform) {
        this.xform = xform;
    }

    public String getXformName() {
        return xformName;
    }

    public void setXformName(String xformName) {
        this.xformName = xformName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (oid == null ? 0 : oid.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final FormLayoutBean other = (FormLayoutBean) obj;
        if (oid == null) {
            if (other.oid != null)
                return false;
        } else if (!oid.equals(other.oid))
            return false;
        return true;
    }
}
