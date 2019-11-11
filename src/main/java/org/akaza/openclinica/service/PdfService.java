/**
 * 
 */
package org.akaza.openclinica.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Tao Li
 *
 */
public interface PdfService {

	File mergePDF(ArrayList<File> files,
	         String fullFinalFilePathName) throws IOException;
	
	String getCaseBookFileRootPath();
	
	void addFooter(String mergedFilePath, String footerMsg) throws IOException;
}
