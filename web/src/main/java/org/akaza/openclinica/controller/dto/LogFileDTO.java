package org.akaza.openclinica.controller.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.TimeUnit;

public class LogFileDTO {

	private static final Logger logger= LoggerFactory.getLogger(LogFileDTO.class);
	private File file;
	private String jobType;
	private String parentRootDir;
	private String fileName;
	private String jobTypeDescrption;
	
	public String getJobTypeDescrption() {
		return jobTypeDescrption;
	}
	public void setJobTypeDescrption(String jobTypeDescrption) {
		this.jobTypeDescrption = jobTypeDescrption;
	}
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}
	public String getJobType() {
		return jobType;
	}
	public void setJobType(String jobType) {
		this.jobType = jobType;
	}
	public String getParentRootDir() {
		return parentRootDir;
	}
	public void setParentRootDir(String parentRootDir) {
		this.parentRootDir = parentRootDir;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	
	public long getFileCreatedTime() {
		BasicFileAttributes attr;
		try {
			attr = Files.readAttributes(this.file.toPath(), BasicFileAttributes.class);
			return attr.creationTime().to(TimeUnit.MILLISECONDS);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("Error accessing the file for extracting created time: ",e);
		}

		 return 0;
	}
}
