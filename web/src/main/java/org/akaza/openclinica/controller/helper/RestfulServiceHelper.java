package org.akaza.openclinica.controller.helper;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.multipart.MultipartFile;

import liquibase.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

public class RestfulServiceHelper {
	
	private final static Logger log = LoggerFactory.getLogger("RestfulServiceHelper");
	
	//CSV file header	
	private static final String [] FILE_HEADER_MAPPING = {"ParticipantID"};
	private static final String ParticipantID_header = "ParticipantID";
	
	
	private DataSource dataSource;	
	private StudyDAO studyDao;     

	
	public RestfulServiceHelper(DataSource dataSource2) {
		dataSource = dataSource2;
	}


	/**
	 * @param file
	 * @return
	 * @throws Exception 
	 */
	public static ArrayList<String> readCSVFile(MultipartFile file) throws Exception {
		
		ArrayList<String> subjectKeyList = new ArrayList<>();
		 
		try {
			 BufferedReader reader;
				
			 String line;
			 InputStream is = file.getInputStream();
			 reader = new BufferedReader(new InputStreamReader(is));
			 
			//Create the CSVFormat object with the header mapping		 
			 CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader(FILE_HEADER_MAPPING).withFirstRecordAsHeader().withTrim();

	         CSVParser csvParser = new CSVParser(reader, csvFileFormat);
	       
	         try {
	        	//Get a list of CSV file records              	         
		         for (CSVRecord csvRecord : csvParser) {		      	
		        	     	          	 
		        	  String participantID = csvRecord.get(ParticipantID_header);
		        	  
		        	  if (StringUtils.isNotEmpty(participantID)) {
		     			 subjectKeyList.add(participantID);     							     				         
		     		 }
		         }
	         }catch(java.lang.IllegalArgumentException e) {
	        	 subjectKeyList = readFile(file);
	        	
		         
		     
	         }
	         
		}catch (Exception e) {
			throw new Exception(" This CSV format is not supported ");
	    }
		
        
		 
		return subjectKeyList;
	}
	
	
	/**
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private static ArrayList<String> readFile(MultipartFile file) throws IOException {
		
		ArrayList<String> subjectKeyList = new ArrayList<>();
		
		try(Scanner sc = new Scanner(file.getInputStream())){
			
			 String line;
			
			 int lineNm = 1;
			 int position = 0;
			 
			 while (sc.hasNextLine()) {
				 line = sc.nextLine();
				 String[] lineVal= line.split(",", 0);
				 
				 // check ParticipantID column number
				 if(lineNm ==1) {
					 
					 for(int i=0; i < lineVal.length;i++) {
						 lineVal.equals(ParticipantID_header);
						 position = i;
						 
						 break;
					 }
				 }else {
					 subjectKeyList.add(lineVal[position]);
				 }
				 
				 
				
				 lineNm++;
			 }
			
		} catch (Exception e) {
			log.error("Exception with cause = {} {}", e.getCause(), e.getMessage());
	    }
		
		return subjectKeyList;
	}
	
	 /**
	  * 
	  * @param studyOid
	  * @param request
	  * @return
	 * @throws Exception 
	  */
	 public StudyBean setSchema(String studyOid, HttpServletRequest request) throws OpenClinicaSystemException {
		// first time, the default DB schema for restful service is public
		 StudyBean study = getStudyDao().findByPublicOid(studyOid);
		
		 Connection con;
		 String schemaNm="";
		 
		 if (study == null) {
			 throw new OpenClinicaSystemException("errorCode.studyNotExist","The study identifier you provided:" + studyOid + " is not valid.");
			 
         }else {
       	  schemaNm = study.getSchemaName();
         }
		 
		 
		try {
			request.setAttribute("requestSchema",schemaNm);
			request.setAttribute("changeStudySchema",schemaNm);
			con = dataSource.getConnection();
			CoreResources.setSchema(con);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       // get correct study from the right DB schema 
		study = getStudyDao().findByOid(studyOid);
        
        return study;
	 }

    /**
	 * 
	 * @return
	 */
	 public StudyDAO getStudyDao() {
        studyDao = studyDao != null ? studyDao : new StudyDAO(dataSource);
        return studyDao;
    }
}
