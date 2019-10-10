package core.org.akaza.openclinica.service;

import java.io.File;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface LogFileService {

	public List<File> getUserImportLogFiles(HttpServletRequest request);

	public List<File> getUserStudyEventScheduleLogFiles(HttpServletRequest request);
	
	public void dowloadFile(File f, String contentType,HttpServletResponse response) throws Exception;
	
	public File getLogFileByStudyIDParentNm(String studyID, String parentNm, String fileNm,String typeDir);
}
