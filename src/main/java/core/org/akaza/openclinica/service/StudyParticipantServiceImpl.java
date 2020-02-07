package core.org.akaza.openclinica.service;


import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.dao.hibernate.*;
import core.org.akaza.openclinica.dao.login.UserAccountDAO;
import core.org.akaza.openclinica.domain.xform.dto.Bind;
import core.org.akaza.openclinica.service.crfdata.EnketoUrlService;
import core.org.akaza.openclinica.service.crfdata.xform.EnketoAPI;
import core.org.akaza.openclinica.service.crfdata.xform.PFormCacheSubjectContextEntry;
import core.org.akaza.openclinica.web.pform.OpenRosaServices;
import core.org.akaza.openclinica.web.pform.PFormCache;
import liquibase.util.StringUtils;
import core.org.akaza.openclinica.bean.core.Status;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.controller.dto.*;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import core.org.akaza.openclinica.dao.service.StudyConfigService;
import core.org.akaza.openclinica.dao.submit.SubjectDAO;
import core.org.akaza.openclinica.domain.datamap.*;
import core.org.akaza.openclinica.domain.enumsupport.JobType;
import core.org.akaza.openclinica.exception.OpenClinicaSystemException;

import org.akaza.openclinica.service.PdfService;
import org.akaza.openclinica.service.ValidateService;
import org.akaza.openclinica.web.restful.errors.ErrorConstants;
import org.akaza.openclinica.service.ImportService;
import org.akaza.openclinica.service.UserService;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This Service class is used with Add Participant Rest Api
 *
 * @author joekeremian
 */

@Service( "studyParticipantService" )
public class StudyParticipantServiceImpl implements StudyParticipantService {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    @Qualifier( "dataSource" )
    private BasicDataSource dataSource;

    @Autowired
    StudyDao studyHibDao;

    @Autowired
    SubjectDao subjectHibDao;

    @Autowired
    StudySubjectDao studySubjectHibDao;

    @Autowired
    UserService userService;

    @Autowired
    CSVService csvService;

    @Autowired
    ImportService importService;

    @Autowired
    UtilService utilService;
    @Autowired
    ValidateService validateService;
    
    @Autowired
    FormLayoutDao formLayoutDao;
    
    @Autowired
    EnketoUrlService urlService;
    
    @Autowired
    OpenRosaServices openRosaServices;

    
    @Autowired
    PdfService pdfService;
    
    @Autowired
    PermissionService permissionService;
    
    @Autowired
    EventDefinitionCrfDao eventDefinitionCrfDao;

    @Autowired
    private StudyDao studyDao;
    private StudySubjectDAO studySubjectDao;
    private SubjectDAO subjectDao;  

    SimpleDateFormat sdf_fileName = new SimpleDateFormat("yyyy-MM-dd'-'HHmmssSSS'Z'");
    public static final String DASH = "-";
    public static final String SCHEDULE_EVENT = "_Schedule Event";
    public static final String SUCCESS = "Success";
    public static final String FAILED = "Failed";
    public static final String US_PHONE_PATTERN = "^[0-9]{10,10}$";
    public static final String INTL_PHONE_PATTERN = "^\\+[0-9]{1,3} [0-9]{1,14}$";
    public static final String EMAIL_PATTERN = "^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?$";


   

    public AddParticipantResponseDTO addParticipant(AddParticipantRequestDTO addParticipantRequestDTO, UserAccountBean userAccountBean, Study tenantStudy, Study tenantSite, String realm,String customerUuid, ResourceBundle textsBundle,String accessToken, String register ) {
        boolean createNewParticipant=false;
        String studyOid = tenantStudy.getOc_oid();
        String siteOid = tenantSite.getOc_oid();

        if (isEnrollmentCapped(tenantStudy,tenantSite))
            throw new OpenClinicaSystemException( ErrorConstants.ERR_PARTICIPANTS_ENROLLMENT_CAP_REACHED);
        
        if (StringUtils.isEmpty(addParticipantRequestDTO.getSubjectKey()))
            throw new OpenClinicaSystemException(ErrorConstants.ERR_MISSING_PARTICIPANT_ID_DATA);

        if (!StringUtils.isEmpty(addParticipantRequestDTO.getSubjectKey()) && addParticipantRequestDTO.getSubjectKey() .length() > 30)
            throw new OpenClinicaSystemException(ErrorConstants.ERR_PARTICIPANT_ID_TOO_LONG);

        if (!StringUtils.isEmpty(addParticipantRequestDTO.getFirstName()) && addParticipantRequestDTO.getFirstName().length() > 35)
            throw new OpenClinicaSystemException(ErrorConstants.ERR_FIRST_NAME_TOO_LONG);


        if (!StringUtils.isEmpty(addParticipantRequestDTO.getLastName()) && addParticipantRequestDTO.getLastName().length() > 35)
            throw new OpenClinicaSystemException(ErrorConstants.ERR_LAST_NAME_TOO_LONG);

        if (!StringUtils.isEmpty(addParticipantRequestDTO.getIdentifier()) && addParticipantRequestDTO.getIdentifier().length() > 35)
            throw new OpenClinicaSystemException(ErrorConstants.ERR_IDENTIFIER_TOO_LONG);

        if (!StringUtils.isEmpty(addParticipantRequestDTO.getEmailAddress())) {
            Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);
            Matcher emailMatch = emailPattern.matcher(addParticipantRequestDTO.getEmailAddress());
            if (addParticipantRequestDTO.getEmailAddress().length() > 255)
                throw new OpenClinicaSystemException(ErrorConstants.ERR_EMAIL_ADDRESS_TOO_LONG);
            if (!emailMatch.matches())
                throw new OpenClinicaSystemException( ErrorConstants.ERR_INVALID_EMAIL_ADDRESS);
        }

        if (!StringUtils.isEmpty(addParticipantRequestDTO.getPhoneNumber())) {
            Pattern intlPhonePattern = Pattern.compile(INTL_PHONE_PATTERN);
            Matcher intlPhoneMatch = intlPhonePattern.matcher(addParticipantRequestDTO.getPhoneNumber());
            if (addParticipantRequestDTO.getPhoneNumber().length() > 17)
                throw new OpenClinicaSystemException( ErrorConstants.ERR_PHONE_NUMBER_TOO_LONG);
            if (!(intlPhoneMatch.matches() && addParticipantRequestDTO.getPhoneNumber().length() < 18))
                throw new OpenClinicaSystemException(ErrorConstants.ERR_INVALID_PHONE_NUMBER);
        }


        if (!utilService.isParticipantUniqueToSite(siteOid, addParticipantRequestDTO.getSubjectKey()))
            throw new OpenClinicaSystemException( ErrorConstants.ERR_PARTICIPANT_NOT_FOUND);


        Subject subject = null;
        StudySubject studySubject = null;
        StudySubjectBean studySubjectBean = getStudySubjectDao().findByLabelAndStudyForCreatingParticipant(addParticipantRequestDTO.getSubjectKey(), tenantStudy.getStudyId());

        StudySubjectBean studySubjectBeanInParent = new StudySubjectBean();
        if (tenantStudy.isSite()) {
            studySubjectBeanInParent = getStudySubjectDao().findByLabelAndStudyForCreatingParticipant(addParticipantRequestDTO.getSubjectKey(), tenantStudy.getStudy().getStudyId());// <
        }
        if (studySubjectBean == null || (!studySubjectBean.isActive() && !studySubjectBeanInParent.isActive())) {
            createNewParticipant=true;
            // Create New Study Subject
            SubjectBean subjectBean = new SubjectBean();
            subjectBean.setStatus(Status.AVAILABLE);
            subjectBean.setOwner(userAccountBean);
            subjectBean = getSubjectDao().create(subjectBean);
            studySubjectBean = new StudySubjectBean();

            studySubjectBean.setSubjectId(subjectBean.getId());
            if (tenantSite != null) {
                studySubjectBean.setStudyId(tenantSite.getStudyId());
            } else {
                studySubjectBean.setStudyId(tenantStudy.getStudyId());
            }

            studySubjectBean.setLabel(addParticipantRequestDTO.getSubjectKey());
            studySubjectBean.setStatus(Status.AVAILABLE);
            studySubjectBean.setOwner(userAccountBean);
            Date now = new Date();
            studySubjectBean.setCreatedDate(now);
            studySubjectBean = this.getStudySubjectDao().createWithoutGroup(studySubjectBean);
        }

        // OC-11095 Adding Contact information Through Rest API on a signed participant is failing
        if(studySubjectBean!=null && !(studySubjectBean.getStatus().equals(Status.AVAILABLE) || studySubjectBean.getStatus().equals(Status.SIGNED)))
            throw new OpenClinicaSystemException(ErrorConstants.ERR_PARTICIPANT_ID_NOT_AVAILABLE);

        studySubject = saveOrUpdateStudySubjectDetails(studySubjectBean, addParticipantRequestDTO, userAccountBean);

        if(createNewParticipant) {
                //update at site level
                studyHibDao.saveOrUpdate(tenantSite);
                // update at parent level
                studyHibDao.saveOrUpdate(tenantStudy);
        }

        if ((register.equalsIgnoreCase("y") || register.equalsIgnoreCase("yes") ) && validateService.isParticipateActive(tenantStudy)) {
            OCParticipantDTO oCParticipantDTO = new OCParticipantDTO();
            oCParticipantDTO.setFirstName(addParticipantRequestDTO.getFirstName());
            oCParticipantDTO.setLastName(addParticipantRequestDTO.getLastName());
            oCParticipantDTO.setEmail(addParticipantRequestDTO.getEmailAddress());
            oCParticipantDTO.setPhoneNumber(addParticipantRequestDTO.getPhoneNumber());
            oCParticipantDTO.setIdentifier(addParticipantRequestDTO.getIdentifier());
            userService.connectParticipant(studyOid, addParticipantRequestDTO.getSubjectKey(),
                    oCParticipantDTO, accessToken, userAccountBean, realm,customerUuid, textsBundle);
            studySubject=studySubjectHibDao.findById(studySubject.getStudySubjectId());
        }



        AddParticipantResponseDTO addParticipantResponseDTO = new AddParticipantResponseDTO();
        addParticipantResponseDTO.setSubjectKey(studySubject.getLabel());
        addParticipantResponseDTO.setSubjectOid(studySubject.getOcOid());
        addParticipantResponseDTO.setStatus(studySubject.getStatus().getName());
        addParticipantResponseDTO.setParticipateStatus(studySubject.getUserStatus() != null ? studySubject.getUserStatus().getValue() : "");

        return addParticipantResponseDTO;
    }


    public void startBulkAddParticipantJob(MultipartFile file, Study study, Study site, UserAccountBean userAccountBean,  JobDetail jobDetail, String schema,String realm,String customerUuid, ResourceBundle textsBundle,String accessToken, String register) {
        ResponseEntity response = null;
        String logFileName = null;
        CoreResources.setRequestSchema(schema);

        sdf_fileName.setTimeZone(TimeZone.getTimeZone("GMT"));
        String fileName = study.getUniqueIdentifier() + DASH + study.getEnvType() + SCHEDULE_EVENT + "_" + sdf_fileName.format(new Date()) + ".csv";

        String filePath = userService.getFilePath(JobType.SCHEDULE_EVENT) + File.separator + fileName;
        jobDetail.setLogPath(filePath);
        List<DataImportReport> dataImportReports = new ArrayList<>();
        try {
            // read csv file
            List<AddParticipantRequestDTO> addParticipantRequestDTOs = csvService.readAddParticipantBulkCSVFile(file, study.getOc_oid(), site.getOc_oid());

            for (AddParticipantRequestDTO addParticipantRequestDTO : addParticipantRequestDTOs) {
                String participantId = addParticipantRequestDTO.getSubjectKey();
                Integer rowNumber = addParticipantRequestDTO.getRowNumber();
                AddParticipantResponseDTO result = null;
                DataImportReport dataImportReport = null;
                try {
                    result = addParticipant(addParticipantRequestDTO, userAccountBean, study, site, realm,customerUuid, textsBundle, accessToken, register);
                    dataImportReport = new DataImportReport(participantId, ((AddParticipantResponseDTO) result).getSubjectOid(), ((AddParticipantResponseDTO) result).getStatus(), ((AddParticipantResponseDTO) result).getParticipateStatus(), SUCCESS, rowNumber);
                } catch (OpenClinicaSystemException ose) {
                    dataImportReport = new DataImportReport(rowNumber, participantId, FAILED, ose.getMessage());
                }
                dataImportReports.add(dataImportReport);
            }
            importService.writeToFile(dataImportReports, fileName, JobType.BULK_ADD_PARTICIPANTS);
            userService.persistJobCompleted(jobDetail, fileName);

        } catch (Exception e) {
            userService.persistJobFailed(jobDetail, fileName);
            logger.error("Error " + e.getMessage());
        }


    }

    public StudySubjectDAO getStudySubjectDao() {
        studySubjectDao = studySubjectDao != null ? studySubjectDao : new StudySubjectDAO(dataSource);
        return studySubjectDao;
    }

    public SubjectDAO getSubjectDao() {
        subjectDao = subjectDao != null ? subjectDao : new SubjectDAO(dataSource);
        return subjectDao;
    }

    private StudySubject saveOrUpdateStudySubjectDetails(StudySubjectBean studySubjectBean, AddParticipantRequestDTO addParticipantRequestDTO,
                                                         UserAccountBean userAccountBean) {
        StudySubject studySubject = studySubjectHibDao.findById(studySubjectBean.getId());



        StudySubjectDetail studySubjectDetail = studySubject.getStudySubjectDetail();
        studySubject.setUpdateId(userAccountBean.getId());
        studySubject.setDateUpdated(new Date());

        if (studySubjectDetail == null) {
         //   studySubjectBean.setUpdater(userAccountBean);
         //   studySubjectBean.setUpdatedDate(new Date());
         //   studySubjectDao.update(studySubjectBean);
            studySubjectHibDao.saveOrUpdate(studySubject);

            studySubjectDetail = new StudySubjectDetail();
        }

        if (addParticipantRequestDTO.getFirstName() != null)
            studySubjectDetail.setFirstName(addParticipantRequestDTO.getFirstName() != null ? addParticipantRequestDTO.getFirstName() : "");
        if (addParticipantRequestDTO.getLastName() != null)
            studySubjectDetail.setLastName(addParticipantRequestDTO.getLastName() != null ? addParticipantRequestDTO.getLastName() : "");
        if (addParticipantRequestDTO.getEmailAddress() != null)
            studySubjectDetail.setEmail(addParticipantRequestDTO.getEmailAddress() != null ? addParticipantRequestDTO.getEmailAddress() : "");
        if (addParticipantRequestDTO.getPhoneNumber() != null)
            studySubjectDetail.setPhone(addParticipantRequestDTO.getPhoneNumber() != null ? addParticipantRequestDTO.getPhoneNumber() : "");
        if (addParticipantRequestDTO.getIdentifier() != null)
            studySubjectDetail.setIdentifier(addParticipantRequestDTO.getIdentifier() != null ? addParticipantRequestDTO.getIdentifier() : "");

        studySubject.setStudySubjectDetail(studySubjectDetail);

        studySubject = studySubjectHibDao.saveOrUpdate(studySubject);

        return studySubject;

    }

    public int getSubjectCount(Study currentStudy) {
        int subjectCount = 0;
        Study studyBean = (Study) studyDao.findByPK(currentStudy.getStudyId());
        if (studyBean != null)
            subjectCount = studyBean.getSubjectCount();

        if (subjectCount == 0) {
            StudySubjectDAO ssdao = this.getStudySubjectDao();
            ArrayList ss = ssdao.findAllBySiteId(currentStudy.getStudyId());
            if (ss != null) {
                subjectCount = ss.size();
            }
        }
        return subjectCount;
    }


    private boolean isEnrollmentCapped(Study currentStudy,Study siteStudy){

        boolean capIsOn = isEnrollmentCapEnforced(currentStudy,siteStudy);

        StudySubjectDAO studySubjectDAO = this.getStudySubjectDao();
        int numberOfSubjects = studySubjectDAO.getCountofActiveStudySubjects();

        Study sb = null;
        if(currentStudy.isSite()){
            sb = (Study) studyDao.findByPK(currentStudy.getStudy().getStudyId());
        }else{
             sb = (Study) studyDao.findByPK(currentStudy.getStudyId());
        }
        int  expectedTotalEnrollment = sb.getExpectedTotalEnrollment();

        if (numberOfSubjects >= expectedTotalEnrollment && capIsOn)
            return true;
        else
            return false;
    }

    /**
     * if it's site level, then also need to check study 
     * @return
     */
    private boolean isEnrollmentCapEnforced(Study currentStudy,Study siteStudy){

        boolean capEnforcedSite = false;
        String  enrollmentCapStatusSite = null;
        
        String enrollmentCapStatus = currentStudy.getEnforceEnrollmentCap();
        boolean capEnforced = Boolean.valueOf(enrollmentCapStatus);
        
        // check at the site level
        if(siteStudy != null) {
        	int siteId = siteStudy.getStudyId();
        	enrollmentCapStatusSite =siteStudy.getEnforceEnrollmentCap();
        	capEnforcedSite = Boolean.valueOf(enrollmentCapStatusSite);        	
        }
        
        if(capEnforcedSite || capEnforced) {
        	return true;
        }else {
        	return false;
        }
        
    }
  
    @Transactional
    public void startCaseBookPDFJob(JobDetail jobDetail,
    		                        String schema,
						    		Study study,
						    		Study site,
						            StudySubject ss,            
						            ServletContext servletContext,
						            String userAccountID,                    
						            String fullFinalFilePathName,
						            String format, 
						            String margin, 
						            String landscape,
						            List<String> permissionTags) throws Exception {
		
    	    CoreResources.setRequestSchema(schema);
    	    ArrayList<File> pdfFiles = new ArrayList<File>();
    	    ArrayList<String> pdfHeaders = new ArrayList<String>();
		    File mergedPdfFile = null;
		    String mergedPdfFileNm = null;
		    int studyId = Integer.parseInt((String) servletContext.getAttribute("studyID"));
		    
		    // pdf header
		    String pdfHeader = null;
		   
			/**
			 *  need to check the number of study/events/forms for this subject
			 *  each for need a rest service call to Enketo
			 */
		    String studyEventDefinitionID = null;
		    String formLayoutOID = null;
		    String studyEventID = null;
		    String studySubjectOID = ss.getOcOid();
		    String studyEventOrdinal = null;
		    
		    String studyOID = null;
	        if(study != null) {										
				studyOID = study.getOc_oid();
			}
		    if(site !=null) {		    							    	
		    	studyOID = site.getOc_oid();
		    }
		    
		   
		    try {
		    	
			    ArrayList<StudyEvent> subjectStudyEvents = studySubjectHibDao.fetchListSEs(studySubjectOID);
			    for(StudyEvent studyEvent : subjectStudyEvents) {

			    	// prepare  pdf header
				    pdfHeader = this.pdfService.preparePdfHeader(study, site, ss.getLabel(),studyEvent);
				    
			    	List<EventCrf> tmp = studyEvent.getEventCrfs();
			    	
			    	/*
			    	 * OC-11782
			    	 * check the CRF ordinal and reorder by the ordinal 
			    	 */
			    	List<EventDefinitionCrf> edfcs = studyEvent.getStudyEventDefinition().getEventDefinitionCrfs();
			    	ArrayList<EventCrf> eventCRFs = new ArrayList<EventCrf>(tmp);
			    	
			    	if(eventCRFs.size() > 1) {			    		
			    		eventCRFs.sort(EventCrf.getCompareByOrdinal());
			    	}			    	
			    	
			    	for(EventCrf eventCrf : eventCRFs) {
			    		formLayoutOID = eventCrf.getFormLayout().getOcOid();
			    		
			    		int studyEventDefinitionId = studyEvent.getStudyEventDefinition().getStudyEventDefinitionId();
			    		EventDefinitionCrf edc = eventDefinitionCrfDao.findByStudyEventDefinitionIdAndCRFIdAndStudyIdorSiteId(studyEventDefinitionId, eventCrf.getCrfVersion().getCrf().getCrfId(), studyId);
			    	    if(edc != null && permissionService.hasFormAccess(edc, permissionTags)) {
			    	    	PFormCacheSubjectContextEntry subjectContext = new PFormCacheSubjectContextEntry();
				    		studyEventDefinitionID = studyEventDefinitionId + "";
					        subjectContext.setStudyEventDefinitionId(studyEventDefinitionID);
					        subjectContext.setFormLayoutOid(formLayoutOID);
					        studyEventID = studyEvent.getStudyEventId()+"";
					        subjectContext.setStudyEventId(studyEventID);
		
					        subjectContext.setStudySubjectOid(studySubjectOID);
					        studyEventOrdinal = studyEvent.getSampleOrdinal() +"";
					        subjectContext.setOrdinal(studyEventOrdinal);
					        subjectContext.setUserAccountId(userAccountID);
					        UserAccountDAO udao = new UserAccountDAO(dataSource);
					        UserAccountBean ub = (UserAccountBean) udao.findByPK(Integer.parseInt(userAccountID));
		
					        FormLayout formLayout = formLayoutDao.findByOcOID(subjectContext.getFormLayoutOid());
					        Role role = Role.RESEARCHASSISTANT;
					        String mode = PFormCache.VIEW_MODE;
					        
							List<Bind> binds = openRosaServices.getBinds(formLayout,EnketoAPI.QUERY_FLAVOR,studyOID);
					        boolean formContainsContactData=false;
					        if(openRosaServices.isFormContainsContactData(binds))
					            formContainsContactData=true;
		
					        String subjectContextKey;
					        
							subjectContextKey = this.createSubjectContextKey(studyOID, formLayout, studyEvent, studySubjectOID, userAccountID, servletContext);
							File pdfFile = urlService.getFormPdf(subjectContextKey, subjectContext, studyOID, studySubjectOID,formLayout, EnketoAPI.QUERY_FLAVOR, null, role, mode, null, false,formContainsContactData,binds,ub,
									format, margin, landscape);
							
							if(pdfFile !=null) {
								pdfFiles.add(pdfFile);
								pdfHeaders.add(pdfHeader);
							}
			    	    }										
				        
			    	}//for-loop-2	    						
			    }//for-loop-1		   
			    
				mergedPdfFile = pdfService.mergePDF(pdfFiles, fullFinalFilePathName,pdfHeaders);
				mergedPdfFileNm = mergedPdfFile.getName();
				userService.persistJobCompleted(jobDetail, mergedPdfFileNm);
							
				
			} catch (Exception e) {				
				if(mergedPdfFileNm == null) {
					int index= fullFinalFilePathName.lastIndexOf(File.separator);
					mergedPdfFileNm = fullFinalFilePathName.substring(index+1);					
				}
				
	            userService.persistJobFailed(jobDetail, mergedPdfFileNm);
	            this.pdfService.writeToFile(e.getMessage(), fullFinalFilePathName,ss);
	            throw e;
	        }
		    
			
		}
  
    
   
   
	    
	/**
	 * 
	 * @param studyOID
	 * @param formLayout
	 * @param studyEvent
	 * @param studySubjectOID
	 * @param userAccountID
	 * @param servletContext
	 * @return
	 * @throws Exception
	 */
    private String createSubjectContextKey(String studyOID, FormLayout formLayout, StudyEvent studyEvent, String studySubjectOID, String userAccountID,ServletContext servletContext) throws Exception {
    	String accessToken = (String) servletContext.getAttribute("accessToken");
        PFormCache cache = PFormCache.getInstance(servletContext);
        String subjectContextKey = cache.putSubjectContext(studySubjectOID, String.valueOf(studyEvent.getStudyEventDefinition().getStudyEventDefinitionId()), String.valueOf(studyEvent.getSampleOrdinal()),
                formLayout.getOcOid(),userAccountID ,String.valueOf(studyEvent.getStudyEventId()), studyOID, PFormCache.PARTICIPATE_MODE,accessToken);
      
        return subjectContextKey;
    }
}