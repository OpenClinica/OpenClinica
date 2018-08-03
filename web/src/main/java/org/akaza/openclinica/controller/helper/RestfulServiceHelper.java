package org.akaza.openclinica.controller.helper;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import liquibase.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class RestfulServiceHelper {
	
	private final static Logger log = LoggerFactory.getLogger("RestfulServiceHelper");
	
	//CSV file header	
	private static final String [] FILE_HEADER_MAPPING = {"ParticipantID"};
	
	     

	
	/**
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static ArrayList<String> readCSVFile(MultipartFile file) throws IOException {
		
		ArrayList<String> subjectKeyList = new ArrayList<>();
		 
		try {
			 BufferedReader reader;
				
			 String line;
			 InputStream is = file.getInputStream();
			 reader = new BufferedReader(new InputStreamReader(is));
			 
			//Create the CSVFormat object with the header mapping		 
			 CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader(FILE_HEADER_MAPPING).withFirstRecordAsHeader().withTrim();

	         CSVParser csvParser = new CSVParser(reader, csvFileFormat);
	       
	         //Get a list of CSV file records              	         
	         for (CSVRecord csvRecord : csvParser) {		      	
	        	     	          	 
	        	  String participantID = csvRecord.get("ParticipantID");
	        	  
	        	  if (StringUtils.isNotEmpty(participantID)) {
	     			 subjectKeyList.add(participantID);     							     				         
	     		 }
	         }
		}catch (Exception e) {
			log.error("Exception with cause = {} {}", e.getCause(), e.getMessage());
	    }
		
        
		 
		return subjectKeyList;
	}
	
	
	/**
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private ArrayList<String> readFile(MultipartFile file) throws IOException {
		
		ArrayList<String> subjectKeyList = new ArrayList<>();
		Scanner sc = null;
		try {
			BufferedReader br;
			 
			 String line;
			 InputStream inputStream = file.getInputStream();
			 sc = new Scanner(inputStream, "UTF-8");
			 
			 while (sc.hasNextLine()) {
				 line = sc.nextLine();
				 subjectKeyList.add(line);
								     				         
			 }
			
		} catch (Exception e) {
			log.error("Exception with cause = {} {}", e.getCause(), e.getMessage());
	    }
		
		return subjectKeyList;
	}
	

}
