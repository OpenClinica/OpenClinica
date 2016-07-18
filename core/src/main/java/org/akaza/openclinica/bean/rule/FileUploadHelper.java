/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2010 Akaza Research
 */
package org.akaza.openclinica.bean.rule;

import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FileUploadHelper {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    FileProperties fileProperties;
    FileRenamePolicy fileRenamePolicy;
    

    public FileUploadHelper() {
        fileProperties = new FileProperties();
	}

    public FileUploadHelper(FileProperties fileProperties) {
        super();
        this.fileProperties = fileProperties;
    }

    public List<File> returnFiles(HttpServletRequest request, ServletContext context) {

        // Check that we have a file upload request
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        return isMultipart ? getFiles(request, context, null) : new ArrayList<File>();
    }

    public List<File> returnFiles(HttpServletRequest request, ServletContext context, FileRenamePolicy fileRenamePolicy) {

        // Check that we have a file upload request
        this.fileRenamePolicy = fileRenamePolicy;
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        return isMultipart ? getFiles(request, context, null) : new ArrayList<File>();
    }

    public List<File> returnFiles(HttpServletRequest request, ServletContext context, String dirToSaveUploadedFileIn, FileRenamePolicy fileRenamePolicy) {

        // Check that we have a file upload request
        this.fileRenamePolicy = fileRenamePolicy;
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        return isMultipart ? getFiles(request, context, createDirectoryIfDoesntExist(dirToSaveUploadedFileIn)) : new ArrayList<File>();
    }

    public List<File> returnFiles(HttpServletRequest request, ServletContext context, String dirToSaveUploadedFileIn) {

        // Check that we have a file upload request
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        return isMultipart ? getFiles(request, context, createDirectoryIfDoesntExist(dirToSaveUploadedFileIn)) : new ArrayList<File>();
    }

    @SuppressWarnings("unchecked")
    private List<File> getFiles(HttpServletRequest request, ServletContext context, String dirToSaveUploadedFileIn) {
        List<File> files = new ArrayList<File>();

        // FileCleaningTracker fileCleaningTracker =
        // FileCleanerCleanup.getFileCleaningTracker(context);

        // Create a factory for disk-based file items
        DiskFileItemFactory factory = new DiskFileItemFactory();

        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setFileSizeMax(getFileProperties().getFileSizeMax());
        try {
            // Parse the request
            List<FileItem> items = upload.parseRequest(request);
            // Process the uploaded items

            Iterator<FileItem> iter = items.iterator();
            while (iter.hasNext()) {
                FileItem item = iter.next();

                if (item.isFormField()) {
                    request.setAttribute(item.getFieldName(), item.getString());
                    // DO NOTHING , THIS SHOULD NOT BE Handled here
                } else {
                    getFileProperties().isValidExtension(item.getName());
                	files.add(processUploadedFile(item, dirToSaveUploadedFileIn));
                		
                }
            }
            return files;
        }catch (FileSizeLimitExceededException slee) {
            throw new OpenClinicaSystemException("exceeds_permitted_file_size", new Object[] { String.valueOf(getFileProperties().getFileSizeMaxInMb()) },
                    slee.getMessage());
		}catch (FileUploadException fue) {
            throw new OpenClinicaSystemException("file_upload_error_occured", new Object[] { fue.getMessage() }, fue.getMessage());
        }
    }

    private File processUploadedFile(FileItem item, String dirToSaveUploadedFileIn) {
        dirToSaveUploadedFileIn = dirToSaveUploadedFileIn == null ? System.getProperty("java.io.tmpdir") : dirToSaveUploadedFileIn;
        String fileName = item.getName();
        // Some browsers IE 6,7 getName returns the whole path
        int startIndex = fileName.lastIndexOf('\\');
        if (startIndex != -1) {
            fileName = fileName.substring(startIndex + 1, fileName.length());
        }

        File uploadedFile = new File(dirToSaveUploadedFileIn + File.separator + fileName);
        if (fileRenamePolicy != null) {
        	try {
        		uploadedFile = fileRenamePolicy.rename(uploadedFile, item.getInputStream());
        	} catch (IOException e) {
        		throw new OpenClinicaSystemException(e.getMessage());
        	}
        }
        try {
			item.write(uploadedFile);
		} catch (Exception e) {
			throw new OpenClinicaSystemException(e.getMessage());
		}
        return uploadedFile;

    }

    private String createDirectoryIfDoesntExist(String theDir) {
        if (!new File(theDir).isDirectory()) {
            new File(theDir).mkdirs();
        }
        return new File(theDir).toString();
    }

    public FileProperties getFileProperties() {
        return fileProperties;
    }

    public void setFileProperties(FileProperties fileProperties) {
        this.fileProperties = fileProperties;
    }

}
