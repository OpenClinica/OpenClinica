package org.akaza.openclinica.controller;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.akaza.openclinica.bean.login.ErrorObject;
import org.akaza.openclinica.bean.login.ImportDataResponseDTO;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.rule.XmlSchemaValidationHelper;
import org.akaza.openclinica.bean.submit.DisplayItemBeanWrapper;
import org.akaza.openclinica.bean.submit.crfdata.ODMContainer;
import org.akaza.openclinica.bean.submit.crfdata.SubjectDataBean;
import org.akaza.openclinica.control.submit.ImportCRFInfo;
import org.akaza.openclinica.control.submit.ImportCRFInfoContainer;
import org.akaza.openclinica.controller.helper.RestfulServiceHelper;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.logic.rulerunner.ExecutionMode;
import org.akaza.openclinica.logic.rulerunner.ImportDataRuleRunnerContainer;
import org.akaza.openclinica.service.rule.RuleSetServiceInterface;

import org.akaza.openclinica.web.crfdata.ImportCRFDataService;

import org.akaza.openclinica.web.restful.data.DataImportService;
import org.akaza.openclinica.web.restful.data.bean.BaseStudyDefinitionBean;
import org.akaza.openclinica.web.restful.data.validator.CRFDataImportValidator;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
* 
* @author Tao Li
*
*/
@Controller
@RequestMapping(value = "/auth/api/v1/data")
@Api(value = "DataImport", tags = { "DataImport" }, description = "REST API for Study Data Import")
public class DataController {
	
	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
	public static final String USER_BEAN_NAME = "userBean";
	@Autowired
	@Qualifier("dataSource")
	private DataSource dataSource;
	
	private final Locale locale= new Locale("en_US");
	
	private ImportCRFDataService dataService;
	private RestfulServiceHelper serviceHelper;
	@Autowired
	private RuleSetServiceInterface ruleSetService;

 
   private final DataImportService dataImportService = new DataImportService();
   @Autowired
   private CoreResources coreResources;
	XmlSchemaValidationHelper schemaValidator = new XmlSchemaValidationHelper();
	
	protected UserAccountBean userBean;	
	private ImportDataResponseDTO responseDTO;

	@ApiOperation(value = "To import study data in XML file",  notes = "Will read the data in XML file and validate study,event and participant against the  setup first, for more detail please refer to OpenClinical online document  ")
	@ApiResponses(value = {
	        @ApiResponse(code = 200, message = "Successful operation"),
	        @ApiResponse(code = 400, message = "Bad Request -- Normally means Found validation errors, for detail please see the error message")})

	@RequestMapping(value = "/import", method = RequestMethod.POST)
	public ResponseEntity<Object> importDataXMLFile(HttpServletRequest request, MultipartFile file) throws Exception {
	
   	ArrayList<ErrorObject> errorObjects = new ArrayList<ErrorObject>();				
		ResponseEntity<Object> response = null;

		String validation_failed_message = "VALIDATION FAILED";
		String validation_passed_message = "SUCCESS";
		
		String importXml = null;	    
		responseDTO = new ImportDataResponseDTO();
		
       try {
    	   	String fileNm = file.getOriginalFilename();
    	   //only support XML file
			if(!(fileNm.endsWith(".xml")) ){
				 throw new OpenClinicaSystemException("errorCode.notXMLfile","The file format is not supported, please send correct XML file, like *.xml ");
				 
			 }
			
			importXml = RestfulServiceHelper.readFileToString(file);
       		errorObjects = importDataInTransaction(importXml,request);
       } catch (OpenClinicaSystemException e) {
    	 
    	   String err_msg = e.getMessage();
           ErrorObject error = createErrorObject(e.getErrorCode(), err_msg, "error");
    	   errorObjects.add(error);
           
       }catch (Exception e) {
    	  
    	   String err_msg = "Error processing data import request.";
           ErrorObject error = createErrorObject("Import Data Validation", err_msg, "error");
    	   errorObjects.add(error);
           
       }

		if (errorObjects != null && errorObjects.size() != 0) {
			responseDTO.setMessage(validation_failed_message);
			responseDTO.setErrors(errorObjects);
			response = new ResponseEntity(responseDTO, org.springframework.http.HttpStatus.BAD_REQUEST);
		} else {
			responseDTO.setMessage(validation_passed_message);
			response = new ResponseEntity(responseDTO, org.springframework.http.HttpStatus.OK);
		}
		
		return response;
	 }
	
	/**
	 * 
	 * @param importXml
	 * @param request
	 * @return
	 * @throws Exception
	 */
	protected ArrayList<ErrorObject> importDataInTransaction(String importXml,HttpServletRequest request) throws Exception {
	        ResourceBundleProvider.updateLocale(new Locale("en_US"));
	        ArrayList<ErrorObject> errorObjects = new ArrayList<ErrorObject>();
	       
	        try {
	        	// check more xml format--  can't be blank
	            if (importXml == null) {
	                String err_msg = "Your XML content is blank.";
	                ErrorObject errorOBject = createErrorObject("Import Data Validation", err_msg, "error");
	        		errorObjects.add(errorOBject);
	        		
	        		return errorObjects;
	            }
	            // check more xml format--  must put  the xml content in <ODM> tag
	            int beginIndex = importXml.indexOf("<ODM>");
	            if(beginIndex < 0) {
	            	beginIndex = importXml.indexOf("<ODM ");
	            }
	            int endIndex = importXml.indexOf("</ODM>");	            
	            if(beginIndex < 0 || endIndex < 0) {
	            	String err_msg = "Please send valid content with correct root tag";
	                ErrorObject errorOBject = createErrorObject("Import Data Validation", err_msg, "error");
	        		errorObjects.add(errorOBject);	        		
	        		return errorObjects;
	            }
	            
	            importXml = importXml.substring(beginIndex, endIndex + 6);
	            
	            userBean = this.getRestfulServiceHelper().getUserAccount(request);
	            
	            if (userBean == null) {
	                String err_msg = "Please send request as a valid user";
	                ErrorObject errorOBject = createErrorObject("Import Data Validation", err_msg, "error");
	        		errorObjects.add(errorOBject);	        		
	        		return errorObjects;
	            }
	          		         
	            File xsdFile = this.getRestfulServiceHelper().getXSDFile(request, "ODM1-3-0.xsd");
	            File xsdFile2 = this.getRestfulServiceHelper().getXSDFile(request, "ODM1-2-1.xsd");
	           
	            Mapping myMap = new Mapping();                
               String ODM_MAPPING_DIRPath = CoreResources.ODM_MAPPING_DIR;
               myMap.loadMapping(ODM_MAPPING_DIRPath + File.separator + "cd_odm_mapping.xml");

               Unmarshaller um1 = new Unmarshaller(myMap);             
               boolean fail = false;
               ODMContainer odmContainer = new ODMContainer();
              
               try {                   
               	// unmarshal xml to java
               	InputStream is = new ByteArrayInputStream(importXml.getBytes());	                	
                   InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                   odmContainer = (ODMContainer) um1.unmarshal(isr);
                 
               } catch (Exception me1) {
	                    me1.printStackTrace();
	                    // expanding it to all exceptions, but hoping to catch Marshal
	                    // Exception or SAX Exceptions
	                    logger.info("found exception with xml transform");	                    
	                    logger.info("trying 1.2.1");
	                    try {
	                        schemaValidator.validateAgainstSchema(importXml, xsdFile2);
	                        // for backwards compatibility, we also try to validate vs
	                        // 1.2.1 ODM 06/2008
	                        InputStream is = new ByteArrayInputStream(importXml.getBytes());	                	
	                        InputStreamReader isr = new InputStreamReader(is, "UTF-8");
	                        odmContainer = (ODMContainer) um1.unmarshal(isr);
	                    } catch (Exception me2) {
	                        // not sure if we want to report me2
	                        MessageFormat mf = new MessageFormat("");
	                     
	                        Object[] arguments = { me1.getMessage() };
	                        String errCode = mf.format(arguments);
	                       
	                        String err_msg = "Your XML is not well-formed.";
	    	                ErrorObject errorOBject = createErrorObject("Import Data Validation", err_msg, errCode);
	    	        		errorObjects.add(errorOBject);
	                    }
	                }	               
               
               String studyUniqueID = odmContainer.getCrfDataPostImportContainer().getStudyOID();                          
               BaseStudyDefinitionBean crfDataImportBean = new BaseStudyDefinitionBean(studyUniqueID, userBean);

               DataBinder dataBinder = new DataBinder(crfDataImportBean);
               Errors errors = dataBinder.getBindingResult();
               
               // set DB schema
               try {
            	   getRestfulServiceHelper().setSchema(studyUniqueID, request);
               }catch(OpenClinicaSystemException e) {
            	   errors.reject(e.getErrorCode(), e.getMessage());
               }
               
               CRFDataImportValidator crfDataImportValidator = new CRFDataImportValidator(dataSource);
               
               // if no error then continue to validate
               if (!errors.hasErrors()) {            	  
                   crfDataImportValidator.validate(crfDataImportBean, errors); 
               }
              
               
               // if no error then continue to validate
               if (!errors.hasErrors()) {
                   StudyBean studyBean = crfDataImportBean.getStudy();

                   List<DisplayItemBeanWrapper> displayItemBeanWrappers = new ArrayList<DisplayItemBeanWrapper>();
                   HashMap<Integer, String> importedCRFStatuses = new HashMap<Integer, String>();

                   List<String> errorMessagesFromValidation = dataImportService.validateMetaData(odmContainer, dataSource, coreResources, studyBean, userBean,
                           displayItemBeanWrappers, importedCRFStatuses);

                   if (errorMessagesFromValidation.size() > 0) {
                       String err_msg = convertToErrorString(errorMessagesFromValidation);
                       
   	                ErrorObject errorOBject = createErrorObject("Import Data Validation", err_msg, "Error");
   	        		errorObjects.add(errorOBject);
   	        		
   	        		return errorObjects;
                   }
                
                   errorMessagesFromValidation = dataImportService.validateData(odmContainer, dataSource, coreResources, studyBean, userBean,
                           displayItemBeanWrappers, importedCRFStatuses);

                   if (errorMessagesFromValidation.size() > 0) {
                       String err_msg = convertToErrorString(errorMessagesFromValidation);
                       ErrorObject errorOBject = createErrorObject("Import Data Validation", err_msg, "Error");
   	        		errorObjects.add(errorOBject); 
   	        		
   	        		return errorObjects;
                   }

                   // setup ruleSets to run if applicable
                   ArrayList<SubjectDataBean> subjectDataBeans = odmContainer.getCrfDataPostImportContainer().getSubjectData();
                   List<ImportDataRuleRunnerContainer> containers = dataImportService.runRulesSetup(dataSource, studyBean, userBean, subjectDataBeans,
                           ruleSetService);

                   List<String> auditMsgs = new DataImportService().submitData(odmContainer, dataSource, studyBean, userBean, displayItemBeanWrappers,
                           importedCRFStatuses);

                   // run rules if applicable
                   List<String> ruleActionMsgs = dataImportService.runRules(studyBean, userBean, containers, ruleSetService, ExecutionMode.SAVE);
                   
                   ImportCRFInfoContainer importCrfInfo = new ImportCRFInfoContainer(odmContainer, dataSource);
                   List<String> skippedCRFMsgs = getSkippedCRFMessages(importCrfInfo);

                   // add detail messages to reponseDTO
                   ArrayList<String> detailMessages = new ArrayList();
                   detailMessages.add("Audit messages:" + convertToErrorString(auditMsgs));
                   detailMessages.add("Rule Action messages:" + convertToErrorString(ruleActionMsgs));
                   detailMessages.add("Skip CRF messages:" + convertToErrorString(skippedCRFMsgs));
                   this.responseDTO.setDetailMessages(detailMessages);
               
               } else {
               	for (ObjectError error : errors.getAllErrors()) {
               		String err_msg = error.getDefaultMessage();
               		String errCode = error.getCode();
   	                ErrorObject errorOBject = createErrorObject("Import Data Validation", err_msg, errCode);
   	        		errorObjects.add(errorOBject);
               	}
               }

	            return errorObjects;
	            
	        } catch (Exception e) {	         
	            logger.error("Error processing data import request", e);
	            throw new Exception(e);
	        }	       
	    }
	    
	    public ErrorObject createErrorObject(String resource, String code, String field) {
			ErrorObject errorOBject = new ErrorObject();
			errorOBject.setResource(resource);
			errorOBject.setCode(code);
			errorOBject.setField(field);
			return errorOBject;
		}
	    
	    private String convertToErrorString(List<String> errorMessages) {
	        StringBuilder result = new StringBuilder();
	        for (String str : errorMessages) {
	            result.append(str + " \n");
	        }

	        return result.toString();
	    }
	    
	    private List<String> getSkippedCRFMessages(ImportCRFInfoContainer importCrfInfo) {
	        List<String> msgList = new ArrayList<String>();

	        ResourceBundle respage = ResourceBundleProvider.getPageMessagesBundle();
	        ResourceBundle resword = ResourceBundleProvider.getWordsBundle();
	        MessageFormat mf = new MessageFormat("");
	        try {
	        	  mf.applyPattern(respage.getString("crf_skipped"));
	        }catch(Exception e) {
	        	// no need break, just continue
	        	String formatStr="StudyOID {0}, StudySubjectOID {1}, StudyEventOID {2}, FormOID {3}, preImportStatus {4}";
	        	mf.applyPattern(formatStr);
	        }
	      

	        for (ImportCRFInfo importCrf : importCrfInfo.getImportCRFList()) {
	            if (!importCrf.isProcessImport()) {
	                String preImportStatus = "";

	                if (importCrf.getPreImportStage().isInitialDE())
	                    preImportStatus = resword.getString("initial_data_entry");
	                else if (importCrf.getPreImportStage().isInitialDE_Complete())
	                    preImportStatus = resword.getString("initial_data_entry_complete");
	                else if (importCrf.getPreImportStage().isDoubleDE())
	                    preImportStatus = resword.getString("double_data_entry");
	                else if (importCrf.getPreImportStage().isDoubleDE_Complete())
	                    preImportStatus = resword.getString("data_entry_complete");
	                else if (importCrf.getPreImportStage().isAdmin_Editing())
	                    preImportStatus = resword.getString("administrative_editing");
	                else if (importCrf.getPreImportStage().isLocked())
	                    preImportStatus = resword.getString("locked");
	                else
	                    preImportStatus = resword.getString("invalid");

	                Object[] arguments = { importCrf.getStudyOID(), importCrf.getStudySubjectOID(), importCrf.getStudyEventOID(), importCrf.getFormOID(),
	                        preImportStatus };
	                msgList.add(mf.format(arguments));
	            }
	        }
	        return msgList;
	    }
	    
	    public ImportCRFDataService getImportCRFDataService() {
	        dataService = this.dataService != null ? dataService : new ImportCRFDataService(dataSource, locale);
	        return dataService;
	    }

		public RuleSetServiceInterface getRuleSetService() {
			return ruleSetService;
		}

		public void setRuleSetService(RuleSetServiceInterface ruleSetService) {
			this.ruleSetService = ruleSetService;
		} 
		
		public RestfulServiceHelper getRestfulServiceHelper() {
			if(serviceHelper == null) {
				serviceHelper = new RestfulServiceHelper(this.dataSource);
			}
			
			return serviceHelper; 
		}
		
}
