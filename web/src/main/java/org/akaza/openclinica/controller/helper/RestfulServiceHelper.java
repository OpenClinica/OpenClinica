package org.akaza.openclinica.controller.helper;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;

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

public class RestfulServiceHelper {
	
	//CSV file header	
	private static final String [] FILE_HEADER_MAPPING = {"ParticipantID"};
	
	     

	
	/**
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static ArrayList<String> readCSVFile(MultipartFile file) throws IOException {
		
		 BufferedReader reader;
		 ArrayList<String> subjectKeyList = new ArrayList<>();
		 String line;
		 InputStream is = file.getInputStream();
		 reader = new BufferedReader(new InputStreamReader(is));
		 
		//Create the CSVFormat object with the header mapping		 
		 CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader(FILE_HEADER_MAPPING).withFirstRecordAsHeader().withTrim();

         CSVParser csvParser = new CSVParser(reader, csvFileFormat);
       
         //Get a list of CSV file records              
         Iterable<CSVRecord> csvRecords = csvParser.getRecords();
         for (CSVRecord csvRecord : csvRecords) {		      	
        	     	          	 
        	  String participantID = csvRecord.get("ParticipantID");
        	  
        	  if (participantID != null && !(participantID.isEmpty())) {
     			 subjectKeyList.add(participantID);     							     				         
     		 }
         }
        
		 
		return subjectKeyList;
	}
	
	
	/**
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private ArrayList<String> readFile(MultipartFile file) throws IOException {
		BufferedReader br;
		 ArrayList<String> subjectKeyList = new ArrayList<>();
		 String line;
		 InputStream is = file.getInputStream();
		 br = new BufferedReader(new InputStreamReader(is));
		 while ((line = br.readLine()) != null && !(line.isEmpty())) {
			 subjectKeyList.add(line);
							     				         
		 }
		return subjectKeyList;
	}
	

}
