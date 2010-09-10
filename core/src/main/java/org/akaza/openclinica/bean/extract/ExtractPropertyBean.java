package org.akaza.openclinica.bean.extract;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.akaza.openclinica.bean.service.ProcessingFunction;
/**
 * Extract Property Bean, by Tom Hickerson 09/2010
 * Placeholder for items from the extract.properties file, filled in 
 * by a method from CoreResources.java.
 * 
 * @author thickerson
 *
 */
public class ExtractPropertyBean {

    protected Logger logger = LoggerFactory.getLogger(getClass().getName());
    private String fileName;
    private String filedescription;
    private String linkText;
    private String helpText;
    private String fileLocation;
    private String exportFileName;
    private String[] rolesAllowed;
    private int id;
    private ProcessingFunction postProcessing;
    
    public ExtractPropertyBean() {
        fileName = "";
        filedescription = "";
        linkText = "";
        helpText = "";
        fileLocation = "";
        exportFileName = "";
        id = 0;
    }
    
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFiledescription() {
        return filedescription;
    }
    public void setFiledescription(String filedescription) {
        this.filedescription = filedescription;
    }
    public String getLinkText() {
        return linkText;
    }
    public void setLinkText(String linkText) {
        this.linkText = linkText;
    }
    public String getHelpText() {
        return helpText;
    }
    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }
    public String getFileLocation() {
        return fileLocation;
    }
    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }
    public String getExportFileName() {
        return exportFileName;
    }
    public void setExportFileName(String exportFileName) {
        this.exportFileName = exportFileName;
    }
    public String[] getRolesAllowed() {
        return rolesAllowed;
    }
    public void setRolesAllowed(String[] rolesAllowed) {
        this.rolesAllowed = rolesAllowed;
    }
    public ProcessingFunction getPostProcessing() {
        return postProcessing;
    }
    public void setPostProcessing(ProcessingFunction postProcessing) {
        this.postProcessing = postProcessing;
    }
}
