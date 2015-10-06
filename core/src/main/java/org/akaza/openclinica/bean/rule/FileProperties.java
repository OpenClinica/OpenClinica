/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2010 Akaza Research

 * fileSizeMax set to Long.MAX_VALUE : derivitive work (under orig license) by Nor Consult, LLC in 2015

 */
package org.akaza.openclinica.bean.rule;

import org.akaza.openclinica.exception.OpenClinicaSystemException;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Setting configuration for file extensions & maxSize of files being uploaded
 * 
 * @author Krikor Krumlian
 * @since 3.1.0
 * @see FileUploadHelper
 */
public class FileProperties {
	
	private String extensions;
    private ExtensionSettings extensionSettings;
    private final Integer MB = 1024 * 1024;
    private final Long fileSizeMax = Long.MAX_VALUE;

    enum ExtensionSettings {
        VALID, INVALID
    }
	
    public FileProperties() {
        setExtensions("");
        setExtensionSettings(ExtensionSettings.VALID);
    }

    public FileProperties(String extensions) {
        setExtensions(extensions);
        setExtensionSettings(ExtensionSettings.VALID);
    }
	
	public FileProperties(String extensions, String extensionSettings) {
        setExtensions(extensions);
		this.extensionSettings = getExtensionSettings(extensionSettings);
	}

	public void isValidExtension(String uploadedFileExtension){
        uploadedFileExtension =
            uploadedFileExtension.lastIndexOf(".") == -1 ? " " : uploadedFileExtension
                    .substring(uploadedFileExtension.lastIndexOf(".") + 1, uploadedFileExtension.length()).trim().toUpperCase();
		ArrayList<String> extensionsList = new ArrayList<String>();
		Collections.addAll(extensionsList, extensions.trim().toUpperCase().split(","));
        if (extensions.trim().length() == 0) { // if no list is defined
            if (extensionSettings.equals(ExtensionSettings.VALID)) {
                return;
            } else {
                throw new OpenClinicaSystemException("prohibited_file_extensions", new Object[] { "ALL" });
            }
        }
		if (extensionSettings.equals(ExtensionSettings.VALID)) { // if the list is flagged as valid list
			if (!extensionsList.contains(uploadedFileExtension)) // if valid list does not contain extension
				throw new OpenClinicaSystemException("permitted_file_extensions", new Object[] { extensions});
		}else{  // if the list is flagged as prohibited list
			if (extensionsList.contains(uploadedFileExtension)) // if deny list contains extension
				throw new OpenClinicaSystemException("prohibited_file_extensions", new Object[] { extensions});
		}
	}

	public String getExtensions() {
		return extensions;
	}

	public void setExtensions(String extensions) {
		this.extensions = extensions == null ? "" : extensions.trim();
	}

    public ExtensionSettings getExtensionSettings() {
        return extensionSettings;
    }

    public void setExtensionSettings(ExtensionSettings extensionSettings) {
        this.extensionSettings = extensionSettings;
    }
    
    public ExtensionSettings getExtensionSettings(String extensionSettings) {
        if (extensionSettings == null || extensionSettings.length() == 0 ) {
            return ExtensionSettings.VALID;
        }else{
            try
            {
                return ExtensionSettings.valueOf(extensionSettings.trim().toUpperCase());
            }
            catch(IllegalArgumentException ex)
            {
                // Do Nothing for now. Is this a good practice ?
            }
            return ExtensionSettings.VALID;
        }
    }

    public Long getFileSizeMax() {
        return fileSizeMax;
    }

    public Long getFileSizeMaxInMb() {
        return fileSizeMax / MB;
    }
}
