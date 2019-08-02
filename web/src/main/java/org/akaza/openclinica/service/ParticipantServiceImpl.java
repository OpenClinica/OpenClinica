package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.StudyParticipantDTO;
import org.akaza.openclinica.bean.login.StudyParticipantDetailDTO;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.managestudy.StudyType;
import org.akaza.openclinica.bean.managestudy.SubjectTransferBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import org.akaza.openclinica.dao.hibernate.UserAccountDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.domain.datamap.JobDetail;
import org.akaza.openclinica.domain.datamap.StudySubject;
import org.akaza.openclinica.domain.datamap.StudySubjectDetail;
import org.akaza.openclinica.domain.enumsupport.JobType;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.validator.ParticipantValidator;
import org.akaza.openclinica.web.restful.errors.ErrorConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.akaza.openclinica.service.UserService.BULK_JOBS;
import static org.akaza.openclinica.service.UserServiceImpl.SEPERATOR;

@Service("ParticipantService")
@Transactional(propagation= Propagation.REQUIRED,isolation= Isolation.DEFAULT)
public class ParticipantServiceImpl implements ParticipantService {
	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    public static final String PARTICIPANT_IMPORT = "_Participant_Import";

    private SubjectDAO subjectDao;
	private StudyParameterValueDAO studyParameterValueDAO;		
	private StudySubjectDAO studySubjectDao;
	private StudyDAO studyDao;
    private StudySubjectDAO ssDao;


    @Autowired
	private UserAccountDAO userAccountDao;

    @Autowired
    private UserAccountDao userAccountHibDao;

    @Autowired
    private StudySubjectDao studySubjectHibDao;

    @Autowired
    private CSVService csvService;

    @Autowired
    private UserService userService;

    @Autowired
    private ValidateService validateService;

    @Autowired
    private UtilService utilService;

    @Autowired
	@Qualifier("dataSource")
	private DataSource dataSource;

    
    public List<StudySubjectBean> getStudySubject(StudyBean study) {
        return getStudySubjectDao().findAllByStudy(study);

    }

   /**
    * 
    * @param subjectTransfer
    * @param currentStudy
    * @return
    * @throws OpenClinicaException
    */
    public String createParticipant(SubjectTransferBean subjectTransfer,StudyBean currentStudy,String accessToken,
                                    String customerUuid, UserAccountBean userAccountBean, Locale locale) throws Exception {


        // create subject
        StudyBean siteStudy = subjectTransfer.getSiteStudy();
        StudySubject studySubject=null;
        StudySubjectBean studySubjectBean  =getStudySubjectDao().findByLabelAndStudy(subjectTransfer.getPersonId(), currentStudy);

        if(!validateService.isStudyAvailable(currentStudy.getOid()))
            throw new OpenClinicaSystemException(ErrorConstants.ERR_STUDY_NOT_AVAILABLE);


        if(!validateService.isStudyAvailable(siteStudy.getOid()))
            throw new OpenClinicaSystemException(ErrorConstants.ERR_SITE_NOT_AVAILABLE);

        if(!utilService.isParticipantUniqueToSite(siteStudy.getOid(),subjectTransfer.getStudySubjectId()))
            throw new OpenClinicaSystemException(ErrorConstants.ERR_PARTICIPANT_NOT_FOUND);


        if(studySubjectBean==null || !studySubjectBean.isActive()) {
            // Create New Study Subject
            SubjectBean subjectBean = new SubjectBean();
            subjectBean.setStatus(Status.AVAILABLE);
            subjectBean.setOwner(subjectTransfer.getOwner());

            subjectBean = this.getSubjectDao().create(subjectBean);
            if (!subjectBean.isActive()) {
                throw new OpenClinicaException("Could not create subject", "3");
            }

            studySubjectBean = new StudySubjectBean();
            studySubjectBean.setSubjectId(subjectBean.getId());
            if (siteStudy != null) {
                studySubjectBean.setStudyId(siteStudy.getId());
            } else {
                studySubjectBean.setStudyId(subjectTransfer.getStudy().getId());
            }

            studySubjectBean.setLabel(subjectTransfer.getStudySubjectId());
            studySubjectBean.setStatus(Status.AVAILABLE);
            studySubjectBean.setUserStatus(UserStatus.CREATED);
            studySubjectBean.setOwner(subjectTransfer.getOwner());
            Date now = new Date();
            studySubjectBean.setCreatedDate(now);
            studySubjectBean = this.getStudySubjectDao().createWithoutGroup(studySubjectBean);

        }
        if(studySubjectBean!=null && !studySubjectBean.getStatus().equals(Status.AVAILABLE))
            throw new OpenClinicaSystemException(ErrorConstants.ERR_PARTICIPANT_ID_NOT_AVAILABLE);

        studySubject = saveOrUpdateStudySubjectDetails( studySubjectBean,  subjectTransfer,userAccountBean);

        if (!studySubjectBean.isActive() || studySubject==null) {
            throw new OpenClinicaException("Could not create study subject", "4");
        }

       //update subject account
       if(siteStudy != null) {
    	   //update at site level
    	   updateStudySubjectSize(subjectTransfer.getSiteStudy());
    	   // update at parent level
    	   updateStudySubjectSize(currentStudy);
       }else {
    	   updateStudySubjectSize(currentStudy);
       }


        if(subjectTransfer.isRegister()) {
            OCParticipantDTO oCParticipantDTO = new OCParticipantDTO();
            oCParticipantDTO.setFirstName(subjectTransfer.getFirstName());
            oCParticipantDTO.setLastName(subjectTransfer.getLastName());
            oCParticipantDTO.setEmail(subjectTransfer.getEmailAddress());
            oCParticipantDTO.setPhoneNumber(subjectTransfer.getPhoneNumber());
            oCParticipantDTO.setIdentifier(subjectTransfer.getIdentifier());
            ResourceBundle textsBundle = ResourceBundleProvider.getTextsBundle(locale);
            userService.connectParticipant(currentStudy.getOid(), subjectTransfer.getPersonId(),
                    oCParticipantDTO, accessToken, userAccountBean, customerUuid, textsBundle);
        }


        return studySubject.getLabel();
   }

/**
 * @param currentStudy
 */
private void updateStudySubjectSize(StudyBean currentStudy) {
	int subjectCount = getSubjectCount(currentStudy);

	   StudyDAO studydao = this.getStudyDao();
	   currentStudy.setSubjectCount(subjectCount+1);
	   currentStudy.setType(StudyType.GENETIC);
	   studydao.update(currentStudy);
}
    /**
     * Validate the listStudySubjectsInStudy request.
     * @param studyOid
     * @param siteOid
     * @param request
     * @return
     */
    public StudyBean validateRequestAndReturnStudy(String studyOid, String siteOid,HttpServletRequest request) throws OpenClinicaSystemException{

        String userName = getUserAccount(request).getName();
        StudyBean study = null;
        StudyBean site = null;
        
        if (studyOid == null && siteOid == null) {
            throw new OpenClinicaSystemException("errorCode.invalidStudyAndSiteIdentifier", "Provide a valid study/site.");
        }else if (studyOid != null && siteOid == null) {
            study = getStudyDao().findByOid(studyOid);
            if (study == null) {
                throw new OpenClinicaSystemException("errorCode.invalidStudyIdentifier", "The study identifier you provided is not valid.");
            }
            checkStudyOrSiteStatus(study);
            
            StudyUserRoleBean studyLevelRole = getUserAccountDao().findTheRoleByUserNameAndStudyOid(userName, studyOid);
            if (studyLevelRole == null) {
                throw new OpenClinicaSystemException("errorCode.noRoleSetUp",
                        "You do not have sufficient privileges to proceed with this operation.");
            }else if(studyLevelRole.getId() == 0 || studyLevelRole.getRole().equals(Role.MONITOR)) {
            	throw new OpenClinicaSystemException("errorCode.noSufficientPrivileges", "You do not have sufficient privileges to proceed with this operation.");
            } 
            
            
        }else if (studyOid != null && siteOid != null) {
            study = getStudyDao().findByOid(studyOid);
            site = getStudyDao().findByOid(siteOid);
            if (study == null) {
                throw new OpenClinicaSystemException("errorCode.invalidStudyIdentifier",
                        "The study identifier you provided is not valid.");
            }
            checkStudyOrSiteStatus(study);
            
            if (site == null || site.getParentStudyId() != study.getId()) {
                throw new OpenClinicaSystemException("errorCode.invalidSiteIdentifier",
                        "The site identifier you provided is not valid.");
            }
            checkStudyOrSiteStatus(site);
            
            /**
             * check study level
             */
            StudyUserRoleBean studyLevelRole = getUserAccountDao().findTheRoleByUserNameAndStudyOid(userName, studyOid);
            if (studyLevelRole == null) {
            	/**
                 * continue to check site level
                 */
                StudyUserRoleBean siteLevelRole = getUserAccountDao().findTheRoleByUserNameAndStudyOid(getUserAccount(request).getName(), siteOid);
                if (siteLevelRole == null) {
                    throw new OpenClinicaSystemException("errorCode.noRoleSetUp",
                    		"You do not have any role set up for user " + userName + " in study site " + siteOid );
                }else if(siteLevelRole.getId() == 0 || siteLevelRole.getRole().equals(Role.MONITOR)) {
                	throw new OpenClinicaSystemException("errorCode.noSufficientPrivileges", "You do not have sufficient privileges to proceed with this operation.");
                }
            }else if(studyLevelRole.getId() == 0 || studyLevelRole.getRole().equals(Role.MONITOR)) {
            	throw new OpenClinicaSystemException("errorCode.noSufficientPrivileges", "You do not have sufficient privileges to proceed with this operation.");
            }  		                           
           
   		}
        
       
        return study;
        
    }

	/**
	  * OC-11162
	  * AC1: When bulk add participant API is called for a study OID which is either in Design, Frozen or Locked status then 
	  * instead of starting the process, system should return an errorcode.studyNotAvailable.
	  * AC2: When bulk add participant API is called for a site OID which is either in Design, Frozen or Locked status then 
	  * instead of starting the process, system should return an errorcode.siteNotAvailable.
	 * @param study
	 * @throws OpenClinicaSystemException
	 */
	private void checkStudyOrSiteStatus(StudyBean study) throws OpenClinicaSystemException {
		
		int parentStudyId = study.getParentStudyId();
		String errorCode=null;
		String msg = null;
		boolean isNotAvailableStatus = false;
		
		// site
		if(parentStudyId > 0) {
			errorCode = "errorCode.siteNotAvailable"; 
			msg = "The site is not available,";
		}else {
			errorCode = "errorCode.studyNotAvailable"; 
			msg = "The study is not available,";
		}
		
		String studyStatus = study.getStatus().getName().toString().toLowerCase();
		if(studyStatus != null ) {
			if(studyStatus.equals("design")) {
				isNotAvailableStatus = true;				
				msg = msg + "it is in design status.";
			}else if(studyStatus.equals("locked")) {
				isNotAvailableStatus = true;				
				msg = msg + "it is in locked status.";
			}else if(studyStatus.equals("frozen")) {
				isNotAvailableStatus = true;				
				msg = msg + "it is in frozen status.";
			}
			if(isNotAvailableStatus) {
				throw new OpenClinicaSystemException(errorCode, msg);
			}
			
		}
	}
    
           

    
    /**
     * Helper Method to get the user account
     * 
     * @return UserAccountBean
     */
    public UserAccountBean getUserAccount(HttpServletRequest request) {
    	UserAccountBean userBean;    
    	
    	if(request.getSession().getAttribute("userBean") != null) {
    		userBean = (UserAccountBean) request.getSession().getAttribute("userBean");
    		
    	}else {
    		 Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    	        String username = null;
    	        if (principal instanceof UserDetails) {
    	            username = ((UserDetails) principal).getUsername();
    	        } else {
    	            username = principal.toString();
    	        }
    	        UserAccountDAO userAccountDao = new UserAccountDAO(dataSource);
    	        userBean = (UserAccountBean) userAccountDao.findByUserName(username);
    	}
    	
    	return userBean;
       
    }

    /**
     * @return the subjectDao
     */
    public SubjectDAO getSubjectDao() {
        subjectDao = subjectDao != null ? subjectDao : new SubjectDAO(dataSource);
        return subjectDao;
    }
    
    public StudyParameterValueDAO getStudyParameterValueDAO() {
        return this.studyParameterValueDAO != null ? studyParameterValueDAO : new StudyParameterValueDAO(dataSource);
    }

    /**
     * @return the subjectDao
     */
    public StudyDAO getStudyDao() {
        studyDao = studyDao != null ? studyDao : new StudyDAO(dataSource);
        return studyDao;
    }

    /**
     * @return the subjectDao
     */
    public StudySubjectDAO getStudySubjectDao() {
        studySubjectDao = studySubjectDao != null ? studySubjectDao : new StudySubjectDAO(dataSource);
        return studySubjectDao;
    }

    /**
     * @return the UserAccountDao
     */
    public UserAccountDAO getUserAccountDao() {
        userAccountDao = userAccountDao != null ? userAccountDao : new UserAccountDAO(dataSource);
        return userAccountDao;
    }

    /**
     * @return the datasource
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * @param dataSource
     *            the datasource to set
     */
    public void setDatasource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int getSubjectCount(StudyBean currentStudy) {
        int subjectCount = 0;
        StudyDAO sdao = new StudyDAO(dataSource);
        StudyBean studyBean = (StudyBean) sdao.findByPK(currentStudy.getId());
        if (studyBean != null)
            subjectCount = studyBean.getSubjectCount();

        if(subjectCount==0) {
            StudySubjectDAO ssdao = this.getStudySubjectDao();
            ArrayList ss = ssdao.findAllBySiteId(currentStudy.getId());
            if (ss != null) {
                subjectCount = ss.size();
            }
        }
        return subjectCount;
    }


    /**
     * Create the Subject object if it is not already in the system.
     */
    @Transactional
    public void processBulkParticipants(StudyBean study, String studyOid, StudyBean siteStudy, String siteOid,
                                         UserAccountBean user, String accessToken, String customerUuid, MultipartFile file,
                                        JobDetail jobDetail, Locale locale, String uri, Map<String, Object> map) throws Exception {

        List<OCParticipateImportDTO> ocParticipateImportDTOs = new ArrayList<>();
        String fileName = null;
        int index = 1;
        try {
            List<SubjectTransferBean> subjects = csvService.readBulkParticipantCSVFile(file);
            fileName = study.getIdentifier()
                    + UserServiceImpl.DASH
                    + study.getEnvType()
                    + PARTICIPANT_IMPORT + new SimpleDateFormat("_yyyy-MM-dd-hhmmssS'.txt'").format(new Date());
            ResourceBundleProvider.updateLocale(locale);
            final boolean siteImport = uri.indexOf("/sites/") > 0 ? true: false;

            for (SubjectTransferBean subject: subjects) {
                ArrayList<String> errorMsgs = new ArrayList<String>();
                subject.setPersonId((String) subject.getStudySubjectId());
                subject.setStudyOid(studyOid);
                subject.setStudy(study);
                subject.setOwner(user);
                subject.setSiteStudy(siteStudy);
                subject.setRegister((boolean) map.get("register"));
                if (siteImport)
                    subject.setSiteIdentifier(siteOid);


                StudyParticipantDTO studyParticipantDTO = new StudyParticipantDTO();
                studyParticipantDTO.setSubjectKey(subject.getStudySubjectId());

                ParticipantValidator participantValidator = new ParticipantValidator(dataSource);
                participantValidator.setBulkMode(true);
                Errors errors = null;

                DataBinder dataBinder = new DataBinder(subject);
                errors = dataBinder.getBindingResult();
                participantValidator.validateBulk(subject, errors);

                if (errors.hasErrors()) {
                    ArrayList validerrors = new ArrayList(errors.getAllErrors());
                    Iterator errorIt = validerrors.iterator();
                    while (errorIt.hasNext()) {
                        ObjectError oe = (ObjectError) errorIt.next();

                        if (oe.getCode() != null) {
                            errorMsgs.add(oe.getCode());
                        } else {
                            errorMsgs.add(oe.getDefaultMessage());
                        }
                    }
                }

                if (errorMsgs != null && errorMsgs.size() != 0) {
                    StudySubjectBean byLabelAndStudy = getStudySubjectDao().findByLabelAndStudy(subject.getPersonId(), study);

                    OCParticipateImportDTO p = new OCParticipateImportDTO();
                    p.setRow(index);
                    p.setParticipantId(subject.getStudySubjectId());

                    if (byLabelAndStudy != null && byLabelAndStudy.getOid() != null) {
                        p.setParticipantOid(byLabelAndStudy.getOid());
                        p.setParticipateStatus(byLabelAndStudy.getStatus().getName());
                    }
                    p.setStatus("Failed");

                    p.setMessage(errorMsgs.get(0));
                    ocParticipateImportDTOs.add(p);
                } else {
                    String label = null;
                    try {
                        label = createParticipant(subject, study, accessToken, customerUuid, user, locale);
                    } catch (Exception e) {
                        OCParticipateImportDTO p = new OCParticipateImportDTO();
                        p.setRow(index);
                        p.setParticipantId(subject.getStudySubjectId());
                        p.setStatus("Failed");

                        p.setMessage(e.getMessage());
                        ocParticipateImportDTOs.add(p);
                        ++index;
                        continue;
                    }
                    StudySubjectBean subjectBean = this.getStudySubjectDAO().findByLabel(label);
                    studyParticipantDTO.setSubjectOid(subjectBean.getOid());

                    OCParticipateImportDTO p = new OCParticipateImportDTO();
                    p.setRow(index);
                    p.setParticipantId(subjectBean.getLabel());
                    p.setParticipantOid(subjectBean.getOid());
                    p.setParticipateStatus(subjectBean.getStatus() != null ? subjectBean.getStatus().getName() : "");
                    p.setStatus("Success");
                    p.setMessage("");
                    ocParticipateImportDTOs.add(p);

                }
                ++index;
            }
        } catch (Exception e) {
            logger.error("Exception while processing batch file." + e);
        } finally {
            // write out any DTOs that have been processed
            if (CollectionUtils.isNotEmpty(ocParticipateImportDTOs)) {
                writeToFile(ocParticipateImportDTOs, studyOid, fileName);
                logger.info(ocParticipateImportDTOs.size() + " records were processed");
            } else {
                logger.error("No records were processed.");
            }
        }
        logger.info("Bulk participant job completed");
        userService.persistJobCompleted(jobDetail, fileName);
    }

    public StudySubjectDAO getStudySubjectDAO() {
        ssDao = ssDao != null ? ssDao : new StudySubjectDAO(dataSource);
        return ssDao;
    }

    private void writeToFile(List<OCParticipateImportDTO> participateImportDTOS, String studyOid, String fileName) {
        String filePath = getFilePath(JobType.BULK_ADD_PARTICIPANTS) + File.separator + fileName;

        File file = new File(filePath);

        PrintWriter writer = null;
        try {
            writer = openFile(file);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            writer.print(writeToStringBuffer(participateImportDTOS));
            closeFile(writer);
        }
    }



    public String getFilePath(JobType jobType) {
        String dirPath= CoreResources.getField("filePath") + BULK_JOBS + File.separator+ jobType.toString().toLowerCase();
        File directory = new File(dirPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return dirPath;
    }

    private PrintWriter openFile(File file) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter(file.getPath(), "UTF-8");
        return writer;
    }


    private void closeFile(PrintWriter writer) {
        writer.close();
    }


    private String writeToStringBuffer(List<OCParticipateImportDTO> participateImportDTOs) {

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("Row");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append("ParticipantID");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append("Participant OID");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append("Participate Status");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append("Status");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append("Message");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append('\n');

        participateImportDTOs.forEach(p->{
            stringBuffer.append(p.getRow());
            stringBuffer.append(SEPERATOR);
            stringBuffer.append(p.getParticipantId() != null ? p.getParticipantId() : "");
            stringBuffer.append(SEPERATOR);
            stringBuffer.append(p.getParticipantOid() != null ? p.getParticipantOid() : "");
            stringBuffer.append(SEPERATOR);
            stringBuffer.append(p.getParticipateStatus() != null ? p.getParticipateStatus() : "");
            stringBuffer.append(SEPERATOR);
            stringBuffer.append(p.getStatus() != null ? p.getStatus() : "");
            stringBuffer.append(SEPERATOR);
            stringBuffer.append(p.getMessage() != null ? p.getMessage() : "");
            stringBuffer.append(SEPERATOR);
            stringBuffer.append('\n');
        });


        StringBuilder sb = new StringBuilder();
        sb.append(stringBuffer.toString() + "\n");

        return sb.toString();
    }

    private StudySubject saveOrUpdateStudySubjectDetails(StudySubjectBean studySubjectBean, SubjectTransferBean subjectTransfer,
                                                          UserAccountBean userAccountBean) throws Exception {
        StudySubject studySubject = studySubjectHibDao.findById(studySubjectBean.getSubjectId());

        studySubjectBean.setUpdater(userAccountBean);
        studySubjectBean.setUpdatedDate(new Date());

        StudySubjectDetail studySubjectDetail = studySubject.getStudySubjectDetail();
        UserAccount userAccount = userAccountHibDao.findById(userAccountBean.getId());
        studySubject.setUpdateId(userAccount.getUserId());
        studySubject.setDateUpdated(new Date());

        if (studySubjectDetail == null) {
            studySubjectDao.update(studySubjectBean);
            studySubjectDetail = new StudySubjectDetail();
        }

        if (subjectTransfer.getFirstName() != null)
            studySubjectDetail.setFirstName(subjectTransfer.getFirstName() != null ? subjectTransfer.getFirstName() : "");
        if (subjectTransfer.getLastName() != null)
            studySubjectDetail.setLastName(subjectTransfer.getLastName() != null ? subjectTransfer.getLastName() : "");
        if (subjectTransfer.getEmailAddress() != null)
            studySubjectDetail.setEmail(subjectTransfer.getEmailAddress() != null ? subjectTransfer.getEmailAddress() : "");
        if (subjectTransfer.getPhoneNumber() != null)
            studySubjectDetail.setPhone(subjectTransfer.getPhoneNumber() != null ? subjectTransfer.getPhoneNumber() : "");
        if (subjectTransfer.getIdentifier() != null)
            studySubjectDetail.setIdentifier(subjectTransfer.getIdentifier() != null ? subjectTransfer.getIdentifier() : "");

        studySubject.setStudySubjectDetail(studySubjectDetail);

        studySubject = studySubjectHibDao.saveOrUpdate(studySubject);

        return studySubject;

    }
    
    public StudyParticipantDetailDTO buildStudyParticipantDetailDTO(StudySubject studySubject) {
    	StudyParticipantDetailDTO spDTO = new StudyParticipantDetailDTO();
        
        StudySubjectDetail studySubjectDetail = studySubject.getStudySubjectDetail();
        spDTO.setSubjectKey(studySubject.getLabel());
    	spDTO.setSubjectOid(studySubject.getOcOid());
		
        spDTO.setFirstName(studySubjectDetail.getFirstName());
        spDTO.setLastName(studySubjectDetail.getLastName());
        spDTO.setEmail(studySubjectDetail.getEmail());
        spDTO.setMobileNumber(studySubjectDetail.getPhone());           
        spDTO.setSecondaryID(studySubject.getSecondaryLabel());
        spDTO.setStatus(studySubject.getUserStatus().name());
    
        return spDTO;
    }

}
