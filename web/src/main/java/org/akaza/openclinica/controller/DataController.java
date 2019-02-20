package org.akaza.openclinica.controller;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import javax.ws.rs.Consumes;

import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.ErrorMessage;
import org.akaza.openclinica.bean.login.ImportDataResponseFailureDTO;
import org.akaza.openclinica.bean.login.ImportDataResponseSuccessDTO;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.rule.XmlSchemaValidationHelper;
import org.akaza.openclinica.bean.submit.DisplayItemBeanWrapper;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.crfdata.ODMContainer;
import org.akaza.openclinica.bean.submit.crfdata.SubjectDataBean;
import org.akaza.openclinica.control.submit.ImportCRFInfo;
import org.akaza.openclinica.control.submit.ImportCRFInfoContainer;
import org.akaza.openclinica.control.submit.ImportCRFInfoSummary;
import org.akaza.openclinica.control.submit.UploadCRFDataToHttpServerServlet;
import org.akaza.openclinica.controller.helper.RestfulServiceHelper;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.logic.importdata.ImportDataHelper;
import org.akaza.openclinica.logic.rulerunner.ExecutionMode;
import org.akaza.openclinica.logic.rulerunner.ImportDataRuleRunnerContainer;
import org.akaza.openclinica.service.DataImportService;
import org.akaza.openclinica.service.OCUserDTO;
import org.akaza.openclinica.service.rule.RuleSetServiceInterface;

import org.akaza.openclinica.web.crfdata.ImportCRFDataService;
import org.akaza.openclinica.web.restful.data.bean.BaseStudyDefinitionBean;
import org.akaza.openclinica.web.restful.data.validator.CRFDataImportValidator;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author Tao Li
 */
@RestController
@RequestMapping(value = "/auth/api/clinicaldata")
@Api(value = "DataImport", tags = {"DataImport"}, description = "REST API for Study Data Import")
public class DataController {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final Locale locale = new Locale("en_US");
    public static final String USER_BEAN_NAME = "userBean";
  

    static {
        disableSslVerification();
    }

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    @Autowired
    private RuleSetServiceInterface ruleSetService;

    @Autowired
    private DataImportService dataImportService;

    @Autowired
    private CoreResources coreResources;


    private RestfulServiceHelper serviceHelper;
    protected UserAccountBean userBean;
    private ImportDataResponseSuccessDTO responseSuccessDTO;
    private XmlSchemaValidationHelper schemaValidator = new XmlSchemaValidationHelper();

    @ApiOperation(value = "To import study data in XML file", notes = "Will read the data in XML file and validate study,event and participant against the  setup first, for more detail please refer to OpenClinica online document  ")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 400, message = "Bad Request -- Normally means found validation errors, for detail please see the error message")})

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public ResponseEntity<Object> importDataXMLFile(HttpServletRequest request, MultipartFile file) throws Exception {

        ArrayList<ErrorMessage> errorMsgs = new ArrayList<ErrorMessage>();
        ResponseEntity<Object> response = null;

        String validation_failed_message = "VALIDATION FAILED";
        String validation_passed_message = "SUCCESS";

        String importXml = null;
        responseSuccessDTO = new ImportDataResponseSuccessDTO();

        try {       	         	  
              //only support XML file
              if (file !=null) {
            	  String fileNm = file.getOriginalFilename();
            	  
            	  if (fileNm!=null && fileNm.endsWith(".xml")) {
            		   importXml = RestfulServiceHelper.readFileToString(file);	
            	  }else {
            		  throw new OpenClinicaSystemException("errorCode.notXMLfile", "The file format is not supported, please send correct XML file, like *.xml ");

            	  }
            	 
              }else {
            	  
            	 /**
              	 *  if call is from the mirth server, then may have no attached file in the request
              	 *  
              	 */
              
          		  // Read from request content
          	    StringBuilder buffer = new StringBuilder();
          	    BufferedReader reader = request.getReader();
          	    String line;
          	    while ((line = reader.readLine()) != null) {
          	        buffer.append(line);
          	    }
          	    importXml = buffer.toString();
          	    
                
            	 
              }        	
          
            errorMsgs = importDataInTransaction(importXml, request);
        } catch (OpenClinicaSystemException e) {
        	e.printStackTrace();
            String err_msg = e.getMessage();
            ErrorMessage error = createErrorMessage(e.getErrorCode(), err_msg);
            errorMsgs.add(error);

        } catch (Exception e) {
            e.printStackTrace();
            String err_msg = "Error processing data import request.";
            ErrorMessage error = createErrorMessage("errorCode.Exception", err_msg);
            errorMsgs.add(error);

        }

        if (errorMsgs != null && errorMsgs.size() != 0) {
            ImportDataResponseFailureDTO responseDTO = new ImportDataResponseFailureDTO();
            responseDTO.setMessage(validation_failed_message);
            responseDTO.setErrors(errorMsgs);
            response = new ResponseEntity(responseDTO, org.springframework.http.HttpStatus.BAD_REQUEST);
        } else {
            responseSuccessDTO.setMessage(validation_passed_message);
            response = new ResponseEntity(responseSuccessDTO, org.springframework.http.HttpStatus.OK);
        }

        return response;
    }

    /**
     * @param importXml
     * @param request
     * @return
     * @throws Exception
     */
    protected ArrayList<ErrorMessage> importDataInTransaction(String importXml, HttpServletRequest request) throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        ArrayList<ErrorMessage> errorMsgs = new ArrayList<ErrorMessage>();
        
        Enumeration<String> headerNames = request.getHeaderNames();

     /*   if (headerNames != null) {
                while (headerNames.hasMoreElements()) {
                        System.out.println("Header: " + request.getHeader(headerNames.nextElement()));
                }
        }*/
        
       

        try {
            // check more xml format--  can't be blank
            if (importXml == null) {
                String err_msg = "Your XML content is blank.";
                ErrorMessage errorOBject = createErrorMessage("errorCode.BlankFile", err_msg);
                errorMsgs.add(errorOBject);

                return errorMsgs;
            }
            
            if(importXml.trim().equals("errorCode.noParticipantIDinDataFile")) {
            	  String err_msg = "Participant ID data not found in the data file.";
                  ErrorMessage errorOBject = createErrorMessage("errorCode.noParticipantIDinDataFile", err_msg);
                  errorMsgs.add(errorOBject);

                  return errorMsgs;
            }
            
            // check more xml format--  must put  the xml content in <ODM> tag
            int beginIndex = importXml.indexOf("<ODM>");
            if (beginIndex < 0) {
                beginIndex = importXml.indexOf("<ODM ");
            }
            int endIndex = importXml.indexOf("</ODM>");
            if (beginIndex < 0 || endIndex < 0) {
                String err_msg = "Please send valid content with correct root tag ODM";
                ErrorMessage errorOBject = createErrorMessage("errorCode.XmlRootTagisNotODM", err_msg);
                errorMsgs.add(errorOBject);
                return errorMsgs;
            }

            importXml = importXml.substring(beginIndex, endIndex + 6);

            userBean = this.getRestfulServiceHelper().getUserAccount(request);

            if (userBean == null) {
                String err_msg = "Please send request as a valid user";
                ErrorMessage errorOBject = createErrorMessage("errorCode.InvalidUser", err_msg);
                errorMsgs.add(errorOBject);
                return errorMsgs;
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

                    Object[] arguments = {me1.getMessage()};
                    String errCode = mf.format(arguments);

                    String err_msg = "Your XML file is not well-formed.";
                    ErrorMessage errorOBject = createErrorMessage("errorCode.XmlNotWellFormed", err_msg);
                    errorMsgs.add(errorOBject);
                }
            }

            String studyUniqueID = odmContainer.getCrfDataPostImportContainer().getStudyOID();
            BaseStudyDefinitionBean crfDataImportBean = new BaseStudyDefinitionBean(studyUniqueID, userBean);

            DataBinder dataBinder = new DataBinder(crfDataImportBean);
            Errors errors = dataBinder.getBindingResult();

            // set DB schema
            try {
                getRestfulServiceHelper().setSchema(studyUniqueID, request);
            } catch (OpenClinicaSystemException e) {
                errors.reject(e.getErrorCode(), e.getMessage());
                
                // log error into file             
                String studySubjectOID = null;
                try {
                	studySubjectOID = odmContainer.getCrfDataPostImportContainer().getSubjectData().get(0).getSubjectOID();	
                }catch(java.lang.NullPointerException e2) {
                	;
                }
                
                String originalFileName = request.getHeader("originalFileName");            	
            	String recordNum = null;
            	if(originalFileName !=null) {
            		recordNum = originalFileName.substring(originalFileName.lastIndexOf("_")+1,originalFileName.indexOf("."));
            		originalFileName = originalFileName.substring(0, originalFileName.lastIndexOf("_"));
            	}
            	String msg = e.getErrorCode() + ":" + e.getMessage();
            	msg = recordNum + "|" + studySubjectOID + "|FAILED|" + msg;
	    		this.dataImportService.getImportCRFDataService().getPipeDelimitedDataHelper().writeToMatchAndSkipLog(originalFileName, msg,request);
            }

            CRFDataImportValidator crfDataImportValidator = new CRFDataImportValidator(dataSource);

            // if no error then continue to validate
            if (!errors.hasErrors()) {
                crfDataImportValidator.validate(crfDataImportBean, errors,request);
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

                    ErrorMessage errorOBject = createErrorMessage("errorCode.ValidationFailed", err_msg);
                    errorMsgs.add(errorOBject);

                    /**
                     * log error into log file 
                     */                    
                    String studySubjectOID = odmContainer.getCrfDataPostImportContainer().getSubjectData().get(0).getSubjectOID();
                    String originalFileName = request.getHeader("originalFileName");
                	// sample file name like:originalFileName_123.txt,pipe_delimited_local_skip_2.txt
                	String recordNum = null;
                	if(originalFileName !=null) {
                		recordNum = originalFileName.substring(originalFileName.lastIndexOf("_")+1,originalFileName.indexOf("."));
                		originalFileName = originalFileName.substring(0, originalFileName.lastIndexOf("_"));
                	}
                	String msg = "errorCode.ValidationFailed:" + err_msg;
                	msg = recordNum + "|" + studySubjectOID + "|FAILED|" + msg;
    	    		this.dataImportService.getImportCRFDataService().getPipeDelimitedDataHelper().writeToMatchAndSkipLog(originalFileName, msg,request);
    	    		
                    return errorMsgs;
                }

                HashMap validateDataResult = (HashMap) dataImportService.validateData(odmContainer, dataSource, coreResources, studyBean, userBean,
                        displayItemBeanWrappers, importedCRFStatuses,request);
                ArrayList<StudyEventBean> newStudyEventBeans = (ArrayList<StudyEventBean>) validateDataResult.get("studyEventBeans");               
                List<EventCRFBean> eventCRFBeans = (List<EventCRFBean>) validateDataResult.get("eventCRFBeans");
                
                errorMessagesFromValidation = (List<String>) validateDataResult.get("errors");
                
                if (errorMessagesFromValidation.size() > 0) {
                    String err_msg = convertToErrorString(errorMessagesFromValidation);
                    ErrorMessage errorOBject = createErrorMessage("errorCode.ValidationFailed", err_msg);
                    errorMsgs.add(errorOBject);


                    /**
                     * log error into log file 
                     */ 
                    String studySubjectOID = odmContainer.getCrfDataPostImportContainer().getSubjectData().get(0).getSubjectOID();
                    String originalFileName = request.getHeader("originalFileName");
                	// sample file name like:originalFileName_123.txt,pipe_delimited_local_skip_2.txt
                	String recordNum = null;
                	if(originalFileName !=null) {
                		recordNum = originalFileName.substring(originalFileName.lastIndexOf("_")+1,originalFileName.indexOf("."));
                		originalFileName = originalFileName.substring(0, originalFileName.lastIndexOf("_"));
                	}
                	// for skip err_msg:1|SS_SITE_SB1|SUCCESS|Skip
                	String msg = null;
                	if(err_msg.indexOf("SUCCESS|Skip") > -1) {
                		msg = err_msg;
                	}else {
                		msg = "errorCode.ValidationFailed:" + err_msg;
                    	msg = recordNum + "|" + studySubjectOID + "|FAILED|" + msg;
                	}
                	
    	    		this.dataImportService.getImportCRFDataService().getPipeDelimitedDataHelper().writeToMatchAndSkipLog(originalFileName, msg,request);    	    		    	    		
                    return errorMsgs;
                }

                // setup ruleSets to run if applicable
                ArrayList<SubjectDataBean> subjectDataBeans = odmContainer.getCrfDataPostImportContainer().getSubjectData();
                List<ImportDataRuleRunnerContainer> containers = dataImportService.runRulesSetup(dataSource, studyBean, userBean, subjectDataBeans,
                        ruleSetService);

                // Now can create event and CRF beans here
                if(newStudyEventBeans != null && newStudyEventBeans.size() > 0) {                
                	ArrayList<StudyEventBean> studyEventBeanCreatedList = this.dataImportService.getImportCRFDataService().creatStudyEvent(newStudyEventBeans);
                	
                	ArrayList<EventCRFBean> tempEventCRFBeans = new ArrayList<>();
                	for(StudyEventBean studyEventBean:studyEventBeanCreatedList) {
                		tempEventCRFBeans.addAll(studyEventBean.getEventCRFs());
                	}
                	
                	ArrayList<Integer> permittedEventCRFIds = new ArrayList<Integer>();
                  
                    for (EventCRFBean eventCRFBean : tempEventCRFBeans) {
                         DataEntryStage dataEntryStage = eventCRFBean.getStage();
                         Status eventCRFStatus = eventCRFBean.getStatus();

                         if (eventCRFStatus.equals(Status.AVAILABLE) || dataEntryStage.equals(DataEntryStage.INITIAL_DATA_ENTRY)
                                 || dataEntryStage.equals(DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE) || dataEntryStage.equals(DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE)
                                 || dataEntryStage.equals(DataEntryStage.DOUBLE_DATA_ENTRY)) {
                             permittedEventCRFIds.add(new Integer(eventCRFBean.getId()));
                         } 
                     }
                	// now need to update displayItemBeanWrappers
                    // The following line updates a map that is used for setting the EventCRF status post import
                    this.dataImportService.getImportCRFDataService().fetchEventCRFStatuses(odmContainer, importedCRFStatuses);
                	displayItemBeanWrappers = this.dataImportService.getImportCRFDataService().getDisplayItemBeanWrappers(request, odmContainer, userBean, permittedEventCRFIds, locale);
                }
                
                List<String> auditMsgs = new DataImportService().submitData(odmContainer, dataSource, studyBean, userBean, displayItemBeanWrappers,
                        importedCRFStatuses);

                // run rules if applicable
                List<String> ruleActionMsgs = dataImportService.runRules(studyBean, userBean, containers, ruleSetService, ExecutionMode.SAVE);

                /**
                 *  Now it's time to log successful message into log file                      
                 */ 
                String studySubjectOID = odmContainer.getCrfDataPostImportContainer().getSubjectData().get(0).getSubjectOID();
                String originalFileName = request.getHeader("originalFileName");
            	// sample file name like:originalFileName_123.txt,pipe_delimited_local_skip_2.txt
            	String recordNum = null;
            	if(originalFileName !=null) {
            		recordNum = originalFileName.substring(originalFileName.lastIndexOf("_")+1,originalFileName.indexOf("."));
            		originalFileName = originalFileName.substring(0, originalFileName.lastIndexOf("_"));
            	}
            	String msg = "imported";
            	msg = recordNum + "|" + studySubjectOID + "|SUCCESS|" + msg;
	    		this.dataImportService.getImportCRFDataService().getPipeDelimitedDataHelper().writeToMatchAndSkipLog(originalFileName, msg,request);
	    	
                ImportCRFInfoContainer importCrfInfo = new ImportCRFInfoContainer(odmContainer, dataSource);
                List<String> skippedCRFMsgs = getSkippedCRFMessages(importCrfInfo);

                // add detail messages to reponseDTO
                ArrayList<String> detailMessages = new ArrayList();
                detailMessages.add("Audit Messages:" + convertToErrorString(auditMsgs));
                detailMessages.add("Rule Action Messages:" + convertToErrorString(ruleActionMsgs));
                detailMessages.add("Skip CRF Messages:" + convertToErrorString(skippedCRFMsgs));
                this.responseSuccessDTO.setDetailMessages(detailMessages);

            } else {

                for (ObjectError error : errors.getAllErrors()) {
                    String err_msg = error.getDefaultMessage();
                    String errCode = error.getCode();
                    ErrorMessage errorMessage = createErrorMessage(errCode, err_msg);
                    errorMsgs.add(errorMessage);
                }
            }

            return errorMsgs;

        } catch (Exception e) {
            logger.error("Error processing data import request");
            e.printStackTrace();
            throw new Exception(e);
        }
    }

    public ErrorMessage createErrorMessage(String code, String message) {
        ErrorMessage errorMsg = new ErrorMessage();
        errorMsg.setCode(code);
        errorMsg.setMessage(message);

        return errorMsg;
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
        } catch (Exception e) {
            // no need break, just continue
            String formatStr = "StudyOID {0}, StudySubjectOID {1}, StudyEventOID {2}, FormOID {3}, preImportStatus {4}";
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

                Object[] arguments = {importCrf.getStudyOID(), importCrf.getStudySubjectOID(), importCrf.getStudyEventOID(), importCrf.getFormOID(),
                        preImportStatus};
                msgList.add(mf.format(arguments));
            }
        }
        return msgList;
    }

    public RuleSetServiceInterface getRuleSetService() {
        return ruleSetService;
    }

    public void setRuleSetService(RuleSetServiceInterface ruleSetService) {
        this.ruleSetService = ruleSetService;
    }

    public RestfulServiceHelper getRestfulServiceHelper() {
        if (serviceHelper == null) {
            serviceHelper = new RestfulServiceHelper(this.dataSource);
        }

        return serviceHelper;
    }
    
    /**
     *  start upload file to mirth
     */

    @ApiOperation(value = "To create data channel", notes = "Will read the data in channel XML configuration file and set up  data channel ")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 400, message = "Bad Request -- for detail please see the error message")})
    @RequestMapping(value = "/createChannel", method = RequestMethod.POST)
    public ResponseEntity<String> createChanelwithXMLConfigurationFile(HttpServletRequest request, MultipartFile file) throws Exception {

        ArrayList<ErrorMessage> errorMsgs = new ArrayList<ErrorMessage>();
        ResponseEntity<String> response = null;
        String createChannelUrl = CoreResources.getField("MirthCreateChannelUrl");
        String validation_failed_message = "VALIDATION FAILED";
        String validation_passed_message = "SUCCESS";
      
        if(createChannelUrl == null || createChannelUrl.trim().length()==0) {
        	createChannelUrl = "https://10.0.11.149:8443/api/#!/Channels/createChannel";
        } 
        
        URL url = new URL(createChannelUrl);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "multipart/form-data");
		//Authorization
 		String loginPassword = "root:password";
 		String encoded = Base64.getEncoder().encodeToString(loginPassword.getBytes()); 		
 		conn.setRequestProperty ("Authorization", "Basic " + encoded);

		
		OutputStream os = conn.getOutputStream();
		os.write(file.getBytes());
		
		os.flush();
		os.close();
	

		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(
					(conn.getInputStream())));
		}catch(Exception e) {
			e.printStackTrace();
			try {
				br = new BufferedReader(new InputStreamReader(
						(conn.getErrorStream())));
			}catch(Exception e2) {
				System.out.println("===================================================================");
				e2.printStackTrace();
			}	
			
		}
		

		String output;
		StringBuffer fromMirthcall = new StringBuffer();
		System.out.println("Output from Server .... \n");
		while ((output = br.readLine()) != null) {
			fromMirthcall.append(output);
			System.out.println(output);
		}

		
		response = new ResponseEntity(conn.getResponseCode(), org.springframework.http.HttpStatus.OK);
		br.close();
		conn.disconnect();
		
        return response;
       
       
        
       
    
    }
    
    private static void disableSslVerification() {
        try{
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }
    
    public ResponseEntity<String> createChanelwithXMLConfigurationFileBAK(HttpServletRequest request, MultipartFile file) throws Exception {

        ArrayList<ErrorMessage> errorMsgs = new ArrayList<ErrorMessage>();
        ResponseEntity<String> response = null;
        String createChannelUrl = CoreResources.getField("MirthCreateChannelUrl");
        String validation_failed_message = "VALIDATION FAILED";
        String validation_passed_message = "SUCCESS";
      
        CloseableHttpClient httpClient = 
	    HttpClients.custom().setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
	    HttpComponentsClientHttpRequestFactory reqFactory = new HttpComponentsClientHttpRequestFactory();
	    reqFactory.setHttpClient(httpClient);

        RestTemplate restTemplate = new RestTemplate(reqFactory);
         
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_XML));
             
        //Authorization        
        String accessToken = (String) request.getSession().getAttribute("accessToken");
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Accept-Charset", "UTF-8");
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("username", "user1");
        map.put("password", "password");
        map.put("file", file);
        HttpEntity requestEntity = new HttpEntity<>(map, headers);
 		      
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setObjectMapper(objectMapper);                
        converters.add(jsonConverter);        
        restTemplate.setMessageConverters(converters);
               
        if(createChannelUrl == null || createChannelUrl.trim().length()==0) {
        	createChannelUrl = "https://10.0.11.149:8443/api/#!/Channels/createChannel";
        } 
        try {                
        	  
        	 response = restTemplate.postForEntity(createChannelUrl, requestEntity, String.class);
        }catch(Exception e){
        	e.printStackTrace();
        }
       
        return response;
       
       
        
       
    
    }
    
    @ApiOperation(value = "To import study data in Pipe Delimited Text File", notes = "Will read both the data text files and  one mapping text file, then validate study,event and participant against the  setup first, for more detail please refer to OpenClinica online document  ")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 400, message = "Bad Request -- Normally means found validation errors, for detail please see the error message")})

    @RequestMapping(value = "/pipe", method = RequestMethod.POST)
    public ResponseEntity<Object> importDataPipeDelimitedFile(HttpServletRequest request, MultipartFile dataFile,MultipartFile mappingFile) throws Exception {

        ArrayList<ErrorMessage> errorMsgs = new ArrayList<ErrorMessage>();
        ResponseEntity<Object> response = null;

        String validation_failed_message = "VALIDATION FAILED";
        String validation_passed_message = "SUCCESS";

        String importXml = null;
        ImportDataResponseSuccessDTO responseSuccessDTO = new ImportDataResponseSuccessDTO();
        ImportCRFInfoSummary importCRFInfoSummary = null;
      
        MultipartFile[] mFiles = new MultipartFile[2];
        mFiles[0] = mappingFile;
        mFiles[1] = dataFile;
        
        String studyOID = this.getRestfulServiceHelper().getImportDataHelper().getStudyOidFromMappingFile(mappingFile);
        
        
        try {       	         	  
              //only support text file
              if (mFiles[0] !=null) {
            	  boolean foundMappingFile = false;
            	  
            	 File[] files = this.dataImportService.getImportCRFDataService().getPipeDelimitedDataHelper().convert(mFiles,studyOID);
            	  
            	  for (File file : files) {           
                      
                      if (file == null || file.getName() == null) {
                          logger.info("file is empty.");
                 
                      }else {
                      	if(file.getName().toLowerCase().endsWith(".properties")) {
                      		foundMappingFile = true;
                      		logger.info("Found mapping property file and uploaded");
                      		
                      		this.dataImportService.getImportCRFDataService().getPipeDelimitedDataHelper().validateMappingFile(file);
                      		break;
                      	}
                      }
                  }
            	 
            	  if(files.length < 2) {
            		  throw new OpenClinicaSystemException("errorCode.notCorrectFileNumber", "When send files, Please send at least one data text files and  one mapping text file in correct format ");
            	  }
            	  if (!foundMappingFile) {            		
            		  throw new OpenClinicaSystemException("errorCode.noMappingfile", "When send files, please include one correct mapping file, named like *mapping.txt ");
            	  }
            	 
            	  importCRFInfoSummary = this.getRestfulServiceHelper().sendOneDataRowPerRequestByHttpClient(Arrays.asList(files), request);
              }else {
            	  
            	  throw new OpenClinicaSystemException("errorCode.notCorrectFileNumber", "Please send at least one data text files and  one mapping text file in correct format ");
              }	  	
        } catch (OpenClinicaSystemException e) {

            String err_msg = e.getMessage();
            ErrorMessage error = createErrorMessage(e.getErrorCode(), err_msg);
            errorMsgs.add(error);

        } catch (Exception e) {
            e.printStackTrace();
            String err_msg = "Error processing data import request.";
            ErrorMessage error = createErrorMessage("errorCode.Exception", err_msg);
            errorMsgs.add(error);

        }

        if (errorMsgs != null && errorMsgs.size() != 0) {
            ImportDataResponseFailureDTO responseDTO = new ImportDataResponseFailureDTO();
            responseDTO.setMessage(validation_failed_message);
            responseDTO.setErrors(errorMsgs);
            response = new ResponseEntity(responseDTO, org.springframework.http.HttpStatus.BAD_REQUEST);
        } else {
        	String msg = validation_passed_message;
        	if(importCRFInfoSummary != null) {
        		msg = validation_passed_message + "\n" + importCRFInfoSummary.getSummaryMsg();
        		ArrayList<String> detailMessages = new ArrayList();
        		detailMessages.add("Please see import log file");
        		responseSuccessDTO.setDetailMessages(detailMessages);
        	}
        	responseSuccessDTO.setMessage(msg);
            response = new ResponseEntity(responseSuccessDTO, org.springframework.http.HttpStatus.OK);
        }

        return response;
    }

	

	
        
          
}
