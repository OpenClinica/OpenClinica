package org.akaza.openclinica.logic.importdata;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PipeDelimitedDataHelper extends ImportDataHelper {


	private String[] columnNms;
	private HashMap  mappedValues;

	/**
	 * 
	 * @param mappingFile
	 * @param rawItemDataFile
	 * @return
	 */
public String transformTextToODMxml(File mappingFile,File rawItemDataFile) {
	
	String rawMappingStr;
	String rawItemData;
	String odmXml = null;
	
	try {
		rawMappingStr = this.readFileToString(mappingFile);
		rawItemData = this.readFileToString(rawItemDataFile);
		
		odmXml = transformTextToODMxml(rawMappingStr,rawItemData); 
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
		return odmXml;
	}

/**
 * 
 * @param file
 * @return
 * @throws IOException
 */
public String readFileToString(File file) throws IOException{
    StringBuilder sb = new StringBuilder();
    try(Scanner sc = new Scanner(file)){
   	 String currentLine;
	
   	 while (sc.hasNextLine()) {
   		 currentLine = sc.nextLine();        		 
	         sb.append(currentLine);
	         sb.append("\r");
	     }
	
	 }
	
      return sb.toString();
}
	public String transformTextToODMxml(String rawMappingStr,String rawItemData) {
		
		String studyOID;
		String subjectKey;
		String studyEventOID;
		String formOID;
		String formVersion;
		String itemGroupOID;
		String itemOid;
		String itemName;
		String useRepeatingkey;
		String studyEventRepeatKey;
		String itemDataValue;
		String itemDataXMLValue;
		
		ArrayList itemDataValues;
		String fileNm;		
		
		

	     /**
	     * Hold all ItemOIDs coming from mapping file
	     * each ite, like:
	     *  ItemGroupOID--Item Name -- Item OID
	     */
		ArrayList mappedColumnNameList = null;
	     /**
	     * Hold all ItemGroupOIDs coming from mapping file
	     */
		Object[] mappingItemGroupOIDs;
	     
		String  mappingStr;

		try {
			
			columnNms = getDataColumnNames(rawItemData);	
			
			if(this.hasParticipantIDColumn()) {
				;
			}else {
				return "errorCode.noParticipantIDinDataFile";
			}
			
			mappedValues = getDataMappedValues(rawMappingStr,columnNms); 
			
			studyOID =(String) mappedValues.get("StudyOID");
			studyEventOID =(String) mappedValues.get("StudyEventOID");
			formOID =(String) mappedValues.get("FormOID");
			formVersion =(String) mappedValues.get("FormVersion");
			
			mappedColumnNameList =(ArrayList) mappedValues.get("mappedColumnNameList");
			ArrayList itemGroupOIDList = (ArrayList) mappedValues.get("itemGroupOIDList");
			
			mappingItemGroupOIDs=(Object[]) itemGroupOIDList.toArray();
			
			
//////////////////////////////////////////////////////////////////////////////////
			DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();

			DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();

			Document document = documentBuilder.newDocument();


			/**
			 * root element
			 * <ODM xmlns="http://www.cdisc.org/ns/odm/v1.3"
			 *      xmlns:OpenClinica="http://www.cdisc.org/ns/odm/v1.3 ODM1-3.xsd" 
			 *      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
			 *      CreationDateTime="2008-04-12T20:24:20" 
			 *      Description="Demographics Import" 
			 *      FileOID="1D20080412202420" 
			 *      FileType="Snapshot" 
			 *      ODMVersion="1.3">

			 */
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
			SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmmss");		    
		    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		    Date date = new Date(); 
		   
			String creationDateTime=new Timestamp(date.getTime()).toString();
			String fileOID=sdf2.format(timestamp);
			Element odmData = document.createElement("ODM");
			
			odmData.setAttribute("xmlns", "http://www.cdisc.org/ns/odm/v1.3");
			odmData.setAttribute("xmlns:OpenClinica", "http://www.cdisc.org/ns/odm/v1.3 ODM1-3.xsd");
			odmData.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			odmData.setAttribute("CreationDateTime", creationDateTime);
			odmData.setAttribute("Description", "Import");
			odmData.setAttribute("FileOID", fileOID);
			odmData.setAttribute("FileType", "Snapshot");
			odmData.setAttribute("ODMVersion", "1.3");
			document.appendChild(odmData);

			
			Element clinicalData = document.createElement("ClinicalData");
			clinicalData.setAttribute("StudyOID", studyOID);
			odmData.appendChild(clinicalData);

			// UpsertOn element
			Element upsertOn = document.createElement("UpsertOn");
			clinicalData.appendChild(upsertOn);

			// set an attribute to UpsertOn element
			Attr attr = document.createAttribute("DataEntryComplete");
			attr.setValue("true");
			upsertOn.setAttributeNode(attr);
			
			attr = document.createAttribute("DataEntryStarted");
			attr.setValue("true");
			upsertOn.setAttributeNode(attr);
			
			attr = document.createAttribute("NotStarted");
			attr.setValue("true");
			upsertOn.setAttributeNode(attr);

			String[] dataRows = rawItemData.split(new Character((char) 13).toString());
			
			int indexofParticipantID = 0;
			
			for(int i = 0;i<dataRows.length ;i++){
				//logger.info("++DEST++dataRows[i]g++++++" +dataRows[i]);
				// in each data row, first position is participant ID 
				String[] dataRow = this.toArray(dataRows[i].toString().replaceAll("\n", ""), "|");
				
				// find subject OID, It may be at any position
				if(i==0) {
					for(int k=0;i<dataRow.length;k++){
						if(dataRow[k].toString().trim().equals("ParticipantID") || dataRow[k].substring(1).trim().equals("ParticipantID")) {
							indexofParticipantID = k;
							
							break;
						}
					}
				}else {
					// start process item data
					// ignore blank line					
					if(dataRows[i].toString().replaceAll("/n|||/r", "").trim().length() > 0) {
						subjectKey = dataRow[indexofParticipantID].toString().trim();
						//logger.info(i+ "************dataRow************************"+ dataRow);
						
						if(subjectKey != null && subjectKey.trim().length()>0){
							Element subjectData = document.createElement("SubjectData");	
							subjectData.setAttribute("SubjectKey", subjectKey);
							
							Element studyEventData = document.createElement("StudyEventData");	
							studyEventData.setAttribute("StudyEventOID", studyEventOID);
							
							Element formData = document.createElement("FormData");	
							formData.setAttribute("FormOID", formOID);						
							formData.setAttribute("FormLayoutOID", formVersion);
							//OpenClinica:Status="initial data entry"
							formData.setAttribute("OpenClinica:Status", "initial data entry");
							
							studyEventData.appendChild(formData);
							subjectData.appendChild(studyEventData);
							clinicalData.appendChild(subjectData);
							
				              /**
				              * Loop through all item group OIDs list to create all item groups for each study event
				              */  
				              for(int j=0; j < mappingItemGroupOIDs.length;j++ ){
				
				 				//create item group, set OID and some default value
				            	  String currenItemGroupOID = mappingItemGroupOIDs[j].toString().trim();
				            	  Element itemGroupData = document.createElement("ItemGroupData");	
				            	  itemGroupData.setAttribute("ItemGroupOID", currenItemGroupOID);						
				            	  itemGroupData.setAttribute("TransactionType", "Insert");
									
				            	  formData.appendChild(itemGroupData);
				              	
								/**
							     * check and process data row value -- start from 2nd position
							     * after each while-loop, it will finish processing one data row
							     */							
								int columnSize = columnNms.length;
				
								for(int k=0; k < columnSize; k++){
									
									if(k != indexofParticipantID) {
										
										 itemName = columnNms[k].trim();
						          		  /**
						          		  *  data value must be in both columnNms and  mappingColumnNmsMap
						          		  *  check itemOid in mappingColumnNmsMap
						          		  *  if found in mapping file, then create the Item
						          		  */ 
						          		Iterator mappedColumnNameListIt=mappedColumnNameList.iterator();
						          		while(mappedColumnNameListIt.hasNext()){
						          		  	String[]  itemMappingRow = (String[]) mappedColumnNameListIt.next();
						          		  	String mappingItemGroupOID = itemMappingRow[0];           		  	
						          		    String mappingItemName = itemMappingRow[1].trim();
						          		    String mappingItemOID = itemMappingRow[2].trim();					
													
											if(mappingItemGroupOID.equals(currenItemGroupOID) && mappingItemName.equals(itemName)){
												//logger.info("----mappingItemName:"+ mappingItemName + "----itemName:"+ itemName);
												 itemDataValue = dataRow[k].toString();
						
												 Element itemData = document.createElement("ItemData");	
												 itemData.setAttribute("ItemOID", mappingItemOID);						
												 itemData.setAttribute("Value", itemDataValue);
																	              	
												 itemGroupData.appendChild(itemData);
												 
											} else{
												//logger.info(k+"----mappingItemName:"+ mappingItemName + "----itemName:"+ itemName);
												
												if(mappingItemGroupOID.equals("StudyEventRepeatKey") && mappingItemName.equals(itemName)){								  	
													studyEventRepeatKey = dataRow[k].toString();
													//logger.info(k+ "************studyEventRepeatKey*************************"+ studyEventRepeatKey);
													
												}
											}
						          		  } //end of innner for-loop    
						          		       		 			        
									}
					          	
					          		 
					               	
					               }// end of for-loop
				                   
				                 

							        
					             
				              }
				             
				             
						}
					}
					
						
					
				}// end of outter for-loop

					
				}
				
							
		
		

			
			// create the xml file
			//transform the DOM Object to an XML File
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			
			DOMSource domSource = new DOMSource(document);
			StringWriter writer = new StringWriter();
			StreamResult streamResult = new StreamResult(writer);

			
			transformer.transform(domSource, streamResult);
			
			String output = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +  writer.getBuffer().toString();

			//System.out.println("Done creating XML File output:" + output);
			
			return output;

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		}catch (Exception e) {
			return e.toString();
		}
		
		return "";
	}

	/**
	 * @param rawItemData
	 */
	private static String[]  getDataColumnNames(String rawItemData) {
		
		//System.out.println("getDataColumnNames==========================" + rawItemData);
		ArrayList columnNmsList= new ArrayList();
		//Iterate to get the item column values
		 String[] itemDataRows = rawItemData.split(new Character((char) 13).toString());		     

		 // process  the header row		
		 //String[] columnNms = itemDataRows[0].toString().split("|");
		 String columnNmsStr = itemDataRows[0].toString();
		 
		 String[] columnNms = toArray(columnNmsStr,"|");
		 
		 return columnNms;
	}

	/**
	 * @param columnNmsStr
	 * @return
	 */
	private static String[] toArray(String columnNmsStr,String delemiterStr) {
		StringTokenizer st = new StringTokenizer(columnNmsStr, delemiterStr);
		int size =st.countTokens();
		String[] columnNms = new String[size];

		int i=0;
		while (st.hasMoreElements()) {
			String e =st.nextElement().toString();
			columnNms[i]=e;
			i++;
			//System.out.println(e);
			
		}
		return columnNms;
	}
	
	 /**
     * hold mapped and filtered columns information for only items,Height.IG_VITAL_GROUP1.HeightOID
     * any columns if not configured in mapping file will not be used  as items to build ODM
     * 
     * return HashMap
     *         
     */
	private static HashMap  getDataMappedValues(String rawMappingStr,String[] columnNms) {
		
		HashMap mappedValues = new HashMap<>();
		ArrayList itemGroupOIDList = new ArrayList<>();
		ArrayList mappedColumnNameList = new ArrayList<>();
		String[] keyValueStr;
		String key;
		String val;
		
		//Iterate to get the item column values
		String[] rawMappingStrRows = rawMappingStr.split(new Character((char) 13).toString());		     
		 		 
		//Loop through all the rows 
		for(int j=0;j<rawMappingStrRows.length;j++){
			
			String rawMappingStrRowsStr = rawMappingStrRows[j];								
			
		     keyValueStr = rawMappingStrRowsStr.split("=");	
		     //logger.info("++keyValueStr======================+" +keyValueStr);
		    
		     if(keyValueStr.length < 2) {
		    	 ;
		     }else {
		    	 key = keyValueStr[0].trim();
			     val =  keyValueStr[1].trim().replaceAll("/n|||/r", "");	
		          			
			    //extract the configuration data
			    if(key.equals("StudyOID") || key.substring(1).equals("StudyOID")){
			    	mappedValues.put("StudyOID", val);		 					   			 
			    }else if(key.equals("StudyEventOID") || key.substring(1).equals("StudyEventOID")){		    		
			    	mappedValues.put("StudyEventOID", val);					 
			    }else  if(key.equals("FormOID") || key.substring(1).equals("FormOID")){		    	
			    	mappedValues.put("FormOID", val);						
			    }else  if(key.equals("FormVersion") || key.substring(1).equals("FormVersion")){			     	 
			    	mappedValues.put("FormVersion", val);
			    //SkipMatchCriteria	
			    }else  if(key.equals("SkipMatchCriteria") || key.substring(1).equals("SkipMatchCriteria")){			     	 
			    	mappedValues.put("SkipMatchCriteria", val);	
			    }else{
	                    // item OID: Height=IG_VITAL_GROUP1.HeightOID
			    	//boolean isCorrectFormat = checkFormItemMappingFormat(rawMappingStrRowsStr);
			    	boolean isCorrectFormat =true;
			    		if(isCorrectFormat) {
			    			 String  tempKeyValStr= key+val;
			                    if(tempKeyValStr != null && tempKeyValStr.trim().length() >0 && !(key.startsWith("#"))){                    	
				                     
				                     String[] itemMappingvalue = toArray(val,".");
				                    // logger.info("===********************itemMappingvalue:" + itemMappingvalue);
				                     /**
				                     * save data in each rwo:
				                     * itemGrpOid -- key -- itemOid
				                     * one special row for repeatingKey
				                     * Repeatingkey -- Repeatingkey -- true
				                     */ 
				                     //System.out.println(itemMappingvalue);
									 String itemGrpOid = itemMappingvalue[0]; 
									 String itemOid = itemMappingvalue[1];
									 String[]  itemMappingRow = {itemGrpOid,key,itemOid};
									 
									 mappedColumnNameList.add(itemMappingRow);
									 
									 //itemGroupOIDList contain unique groupOID
									 if(!(itemGroupOIDList.contains(itemGrpOid))) {
										 itemGroupOIDList.add(itemGrpOid);
									 }
									 
				               }
			    		}
			    	
	                   

					if(key.equals("Repeatingkey")){				    		
					    		mappedValues.put("useRepeatingkey", "TRUE")	;
					}
			    	     		   
			    }
			        
		     }
		     
		}
		
		 mappedValues.put("itemGroupOIDList", itemGroupOIDList)	;
		 mappedValues.put("mappedColumnNameList", mappedColumnNameList)	;
		 
		 return mappedValues;
	}

	/**
	 * Here is the expected format:
	 * Height Units=IG_VITAL_GROUP1.HeightUnitsOID
	 * 
	 * @param rawMappingStrRowsStr
	 */
	private static boolean checkFormItemMappingFormat(String rawMappingStrRowsStr) {
		// use regular exoression to check item configuration
		String regex = "^[A-Za-z0-9+_-[ ]*[\n]*]+=[A-Za-z0-9+_-]+[.][A-Za-z0-9+_-]+$";		 
		Pattern pattern = Pattern.compile(regex);		 
		
		Matcher matcher = pattern.matcher(rawMappingStrRowsStr);		
		return matcher.matches();
	}
	
	public  boolean hasParticipantIDColumn() {
		
		boolean found = false;
		String textStr;
		
		for(int i=0; i < this.columnNms.length; i++) {
			//System.out.println("columnNms==========================" + columnNms[i]);
			if(columnNms[i].trim().equals("ParticipantID")) {
				found = true;
				break;
			}			
			
			/**
			 *  in case data is in UTF-8 or  UTF-8-BOM etc encoding
			 */
			textStr = columnNms[i].substring(1);
			if(textStr.trim().equals("ParticipantID")) {
				found = true;
				break;
			}				
		
		}
		
		return found;
	}
	
    public  String getSkipMatchCriteria() {			
    	
		return (String) this.mappedValues.get("SkipMatchCriteria");
	}
    
    public  String getSkipMatchCriteria(String rawItemData, String rawMappingStr ) {		
		if(mappedValues == null) {
			columnNms = getDataColumnNames(rawItemData);								
			mappedValues = getDataMappedValues(rawMappingStr,columnNms);
		}
    	
		return (String) this.mappedValues.get("SkipMatchCriteria");
	}

    /**
     * 
     * @param rawItemDataFile
     * @param mappingFile
     * @return
     */
    public  String getSkipMatchCriteria(File rawItemDataFile, File mappingFile ) {		
		if(mappedValues == null) {
			 String rawMappingStr = null;
			 String rawItemData = null;
			
			try {
				rawMappingStr = this.readFileToString(mappingFile);
				rawItemData = this.readFileToString(rawItemDataFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			columnNms = getDataColumnNames(rawItemData);								
			mappedValues = getDataMappedValues(rawMappingStr,columnNms);
		}
    	
		return (String) this.mappedValues.get("SkipMatchCriteria");
	}
   
}
