package org.akaza.openclinica.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.service.StudyBuildService;
import org.akaza.openclinica.controller.dto.LogFileDTO;
import org.akaza.openclinica.controller.helper.RestfulServiceHelper;
import core.org.akaza.openclinica.service.LogFileService;
import org.apache.commons.dbcp2.BasicDataSource;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller(value = "logFileController")
@RequestMapping( value = "/Log" )
public class LogFileController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	 @Autowired
	 @Qualifier( "dataSource" )
	 private BasicDataSource dataSource;

	 @Autowired
	 private StudyBuildService studyBuildService;

	@Autowired
	private StudyDao studyDao;
	  
	 private RestfulServiceHelper restfulServiceHelper;
	
	@Autowired
	LogFileService logFileService;
	
	@RequestMapping(value = "/listFiles", method = RequestMethod.GET)
	public ModelAndView  listLogFiles(HttpServletRequest request) throws Exception {
		
		ModelAndView  mv = new ModelAndView("submit/listLogFiles");
		
		List<LogFileDTO> allLogfileList = findAllLogFiles(request);
		
		mv.addObject("AllLogfileList",allLogfileList);
		
		return mv;
	}

	/**
	 * @param request
	 * @return
	 */
	private List<LogFileDTO> findAllLogFiles(HttpServletRequest request) {
		List<LogFileDTO> allLogfileList = new ArrayList<LogFileDTO>();
		/**
		 *  implort pipe delimited data log files
		 */				
		List<File> importLogfileList = new ArrayList<File>();
		importLogfileList = this.logFileService.getUserImportLogFiles(request);
		/**
		 *  covert to LogFileDTO
		 */
		for(File file:  importLogfileList) {
			LogFileDTO logFileDTO = new LogFileDTO();
			logFileDTO.setFile(file);
			logFileDTO.setJobType("import");
			logFileDTO.setJobTypeDescrption("Pipe Delimited Data Import");
			logFileDTO.setParentRootDir("import");
			logFileDTO.setFileName(file.getName());
			
			allLogfileList.add(logFileDTO);
		}
		
		
		/**
		 *  Bulk study event schedule log files
		 */				
		List<File> eventScheduleLogfileList = new ArrayList<File>();
		eventScheduleLogfileList = this.logFileService.getUserStudyEventScheduleLogFiles(request);
		
		/**
		 *  covert to LogFileDTO
		 */
		for(File file:  eventScheduleLogfileList) {
			LogFileDTO logFileDTO = new LogFileDTO();
			logFileDTO.setFile(file);
			logFileDTO.setJobTypeDescrption("Bulk Study Event Schedule");
			logFileDTO.setJobType("schedule");
			logFileDTO.setParentRootDir("study-event-schedule");
			logFileDTO.setFileName(file.getName());
			
			allLogfileList.add(logFileDTO);
		}
		return allLogfileList;
	}
	
	@RequestMapping(value = "/processFiles", method = RequestMethod.GET)
	public ModelAndView  processLogFiles(@RequestParam("action") String action,
										 @RequestParam("studyId") String studyId,
										 @RequestParam("parentNm") String parentNm,
										 @RequestParam("fileId") String fileId,
										 @RequestParam("type") String type,
			                             HttpServletRequest request,
			                             HttpServletResponse response) throws Exception {
		
		ModelAndView  mv = new ModelAndView("submit/listLogFiles");
		
		String inAction = action;
		String studyID= studyId;
    	String parentNM= parentNm;
        String fileName= fileId;
        
        String typeDir = "";
		if(type.equals("import")) {
			typeDir = "import";
		}else if(type.equals("schedule")) {
			typeDir = "study-event-schedule";
		}else {
			typeDir = "import";
		}	
		
		if(inAction != null && inAction.equalsIgnoreCase("delete")) {				
			
            File tempFile = logFileService.getLogFileByStudyIDParentNm(studyID, parentNM, fileName,typeDir);
           
        	if(tempFile!=null && tempFile.exists()) {
        		tempFile.delete();
        	}
		}else if ("download".equalsIgnoreCase(action)) {
			
            File file = logFileService.getLogFileByStudyIDParentNm(studyID, parentNM, fileName,typeDir);
            logFileService.dowloadFile(file, "text/xml",response);
		}
		
        List<LogFileDTO> allLogfileList = findAllLogFiles(request);		
		mv.addObject("AllLogfileList",allLogfileList);
		
		return mv;
	}
	
	 public RestfulServiceHelper getRestfulServiceHelper() {
	        if (restfulServiceHelper == null) {
	            restfulServiceHelper = new RestfulServiceHelper(this.dataSource, studyBuildService, studyDao);
	        }
	        return restfulServiceHelper;
	    }
	
}
