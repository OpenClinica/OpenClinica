/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.bean.extract;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.login.UserAccountBean;

import java.util.Date;

/**
 * <P>
 * Hold metadata for files found in the system. This includes:
 * <P>
 * Size
 * <P>
 * Date last run
 * <P>
 * average run time?
 * <P>
 * other comments
 *
 * @author thickerson
 *
 * TODO sync up fields with the table, eventually
 */
public class ArchivedDatasetFileBean extends EntityBean {

    private int id;
    private int datasetId;
    private int exportFormatId;
    private ExportFormatBean exportFormatBean;
    private UserAccountBean owner;
    private int fileSize;
    private String webPath;
    private String fileReference;
    private double runTime;
    private Date dateCreated;
    private int ownerId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    

    /**
     * @return Returns the datasetId.
     */
    public int getDatasetId() {
        return datasetId;
    }

    /**
     * @param datasetId
     *            The datasetId to set.
     */
    public void setDatasetId(int datasetId) {
        this.datasetId = datasetId;
    }

    /**
     * @return Returns the dateCreated.
     */
    public Date getDateCreated() {
        return dateCreated;
    }

    /**
     * @param dateCreated
     *            The dateCreated to set.
     */
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    /**
     * @return Returns the exportFormatId.
     */
    public int getExportFormatId() {
        return exportFormatId;
    }

    /**
     * @param exportFormatId
     *            The exportFormatId to set.
     */
    public void setExportFormatId(int exportFormatId) {
        this.exportFormatId = exportFormatId;
    }

    /**
     * @return Returns the fileSize.
     */
    public int getFileSize() {
        return fileSize;
    }

    /**
     * @param fileSize
     *            The fileSize to set.
     */
    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * @return Returns the ownerId.
     */
    public int getOwnerId() {
        return ownerId;
    }

    /**
     * @param ownerId
     *            The ownerId to set.
     */
    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    /**
     * @return Returns the runTime.
     */
    public double getRunTime() {
        return runTime;
    }

    /**
     * @param runTime
     *            The runTime to set.
     */
    public void setRunTime(double runTime) {
        this.runTime = runTime;
    }

    /**
     * @return Returns the webPath.
     */
    public String getWebPath() {
        return webPath;
    }

    /**
     * @param webPath
     *            The webPath to set.
     */
    public void setWebPath(String webPath) {
        this.webPath = webPath;
    }

    /**
     * @return Returns the exportFormatBean.
     */
    public ExportFormatBean getExportFormatBean() {
        return exportFormatBean;
    }

    /**
     * @param exportFormatBean
     *            The exportFormatBean to set.
     */
    public void setExportFormatBean(ExportFormatBean exportFormatBean) {
        this.exportFormatBean = exportFormatBean;
    }

    /**
     * @return Returns the fileReference.
     */
    public String getFileReference() {
        return fileReference;
    }

    /**
     * @param fileReference
     *            The fileReference to set.
     */
    public void setFileReference(String fileReference) {
        this.fileReference = fileReference;
    }

    /**
     * @return Returns the owner.
     */
    public UserAccountBean getOwner() {
        return owner;
    }

    /**
     * @param owner
     *            The owner to set.
     */
    public void setOwner(UserAccountBean owner) {
        this.owner = owner;
    }
}
