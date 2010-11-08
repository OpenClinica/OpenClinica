package org.akaza.openclinica.bean.service;

/**
 * Superclass for processing functions, by Tom Hickerson 09/2010
 * Note that this function is run by the XsltTransformJob when an extract is
 * run.  When the run() function is executed, it is assumed that all of the below
 * private getters/setters have been loaded by the job. 
 * @author thickerson
 *
 */
public abstract class ProcessingFunction implements ProcessingInterface {
    private String ODMXMLFileName;
    private String xslFileName;
    private String transformFileName;
    public String fileType;
    // the following is for the properties of custom-post processing. Some of them could be redundant.
    private String location;
    private String exportFileName;
    private boolean deleteOld;
    private boolean zip;
    
    
    public String getODMXMLFileName() {
        return ODMXMLFileName;
    }

    public void setODMXMLFileName(String fileName) {
        ODMXMLFileName = fileName;
    }

    public String getXslFileName() {
        return xslFileName;
    }

    public void setXslFileName(String xslFileName) {
        this.xslFileName = xslFileName;
    }

    public String getTransformFileName() {
        return transformFileName;
    }

    public void setTransformFileName(String transformFileName) {
        this.transformFileName = transformFileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getExportFileName() {
		return exportFileName;
	}

	public void setExportFileName(String exportFileName) {
		this.exportFileName = exportFileName;
	}

	public boolean isDeleteOld() {
		return deleteOld;
	}

	public void setDeleteOld(boolean deleteOld) {
		this.deleteOld = deleteOld;
	}

	public boolean isZip() {
		return zip;
	}

	public void setZip(boolean zip) {
		this.zip = zip;
	}
    
}
