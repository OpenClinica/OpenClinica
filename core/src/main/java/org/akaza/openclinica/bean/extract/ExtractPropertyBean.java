package org.akaza.openclinica.bean.extract;

import java.io.Serializable;

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
//JN: added serializable, as this bean needs be to passed over in -non-static manner over to job. 
public class ExtractPropertyBean implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = -2807419666500498114L;
    /**
     * 
     */
    
    protected Logger logger = LoggerFactory.getLogger(getClass().getName());
    private String[] fileName;
    private String filedescription;
    private String linkText;
    private String helpText;
    private String fileLocation;
    private String[] exportFileName;
    private String[] rolesAllowed;
    private int id;
    private String format;
    private ProcessingFunction postProcessing;
    private boolean zipFormat;
    private boolean deleteOld;
    private String successMessage;
    private String failureMessage;
    //Post Processing Parameters
	private String postProcLocation;
	private String postProcExportName;
	private boolean postProcDeleteOld;
	private boolean postProcZip;
	private String[] doNotDelFiles;
	
	private String datasetId;
	private String zipName;
	
	
	private String odmType;
	
	private String datasetName;
	
	public String getOdmType() {
        return odmType;
    }


    public void setOdmType(String odmType) {
        this.odmType = odmType;
    }


    public String getZipName() {
		return zipName;
	}


	public void setZipName(String zipName) {
		this.zipName = zipName;
	}


	//Associating epBean with datasetId, since core resources to get around with coreResources.findExtractPropertyBeanById, to overcome the problems of static method
    public String getDatasetId() {
		return datasetId;
	}


	public void setDatasetId(String datasetId) {
		this.datasetId = datasetId;
	}


	public String[] getDoNotDelFiles() {
		return doNotDelFiles;
	}


	public void setDoNotDelFiles(String[] doNotDelFiles) {
		this.doNotDelFiles = doNotDelFiles;
	}


	public ExtractPropertyBean() {
        fileName = new String[10];
        filedescription = "";
        linkText = "";
        helpText = "";
        fileLocation = "";
        exportFileName =new String[10];
        id = 0;
        zipFormat = true;
        deleteOld = true;
        postProcessing = null;
        successMessage=null;
        failureMessage=null;
        datasetId = null;
        postProcLocation = null;
        postProcExportName = null;
         postProcDeleteOld = true;
         postProcZip = true;
        doNotDelFiles=null;
        
    }
    
    
    public String[] getFileName() {
        return fileName;
    }
    
    public void setFileName(String[] fileName) {
        this.fileName = fileName;
    }
    
    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
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
    /*
     * we need to set the file location and generate a unique name for the output.
     * unique tags include: $date, $type, $datetime
     * however, if we generate it here, we run the risk of generating a file name
     * before other variables are set, i.e. type.
     * also, we can't run the risk of generating two datestamps which may be different.
     * tbh, 9/22/2010
     */
    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }
    public String[] getExportFileName() {
        return exportFileName;
    }
    public void setExportFileName(String[] exportFileName) {
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
    public void setZipFormat(boolean zipFormat)
    {
    	this.zipFormat = zipFormat;
    }
    public boolean getZipFormat()
    {
    	return zipFormat;
    }
    public void setDeleteOld(boolean deleteOld)
    {
    	this.deleteOld = deleteOld;
    }
    public boolean getDeleteOld()
    {
    	return deleteOld;
    }
    public String getSuccessMessage() {
		return successMessage;
	}


	public void setSuccessMessage(String successMessage) {
		this.successMessage = successMessage;
	}


	public String getFailureMessage() {
		return failureMessage;
	}


	public void setFailureMessage(String failureMessage) {
		this.failureMessage = failureMessage;
	}


	public String getPostProcLocation() {
		return postProcLocation;
	}


	public void setPostProcLocation(String postProcLocation) {
		this.postProcLocation = postProcLocation;
	}


	public String getPostProcExportName() {
		return postProcExportName;
	}


	public void setPostProcExportName(String postProcExportName) {
		this.postProcExportName = postProcExportName;
	}


	public boolean getPostProcDeleteOld() {
		return postProcDeleteOld;
	}


	public void setPostProcDeleteOld(boolean postProcDeleteOld) {
		this.postProcDeleteOld = postProcDeleteOld;
	}


	public boolean getPostProcZip() {
		return postProcZip;
	}


	public void setPostProcZip(boolean postProcZip) {
		this.postProcZip = postProcZip;
	}


    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }


    public String getDatasetName() {
        return datasetName;
    }
}
