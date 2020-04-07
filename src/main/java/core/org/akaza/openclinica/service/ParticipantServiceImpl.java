package core.org.akaza.openclinica.service;

import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.bean.core.Status;
import core.org.akaza.openclinica.bean.login.StudyParticipantDetailDTO;
import core.org.akaza.openclinica.bean.login.StudyUserRoleBean;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.bean.managestudy.StudyType;
import core.org.akaza.openclinica.bean.managestudy.SubjectTransferBean;
import core.org.akaza.openclinica.bean.submit.SubjectBean;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import core.org.akaza.openclinica.dao.hibernate.UserAccountDao;
import core.org.akaza.openclinica.dao.login.UserAccountDAO;
import core.org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import core.org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import core.org.akaza.openclinica.dao.submit.SubjectDAO;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.datamap.StudySubject;
import core.org.akaza.openclinica.domain.datamap.StudySubjectDetail;
import core.org.akaza.openclinica.domain.enumsupport.JobType;
import core.org.akaza.openclinica.domain.user.UserAccount;
import core.org.akaza.openclinica.exception.OpenClinicaException;
import core.org.akaza.openclinica.exception.OpenClinicaSystemException;
import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.ValidateService;
import org.akaza.openclinica.web.restful.errors.ErrorConstants;
import org.akaza.openclinica.service.UserService;
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
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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
    private StudySubjectDAO ssDao;

    @Autowired
    private StudyDao studyDao;

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

    
    public List<StudySubjectBean> getStudySubject(Study study) {
        return getStudySubjectDao().findAllByStudy(study);

    }

   /**
    * 
    * @param subjectTransfer
    * @param currentStudy
    * @return
    * @throws OpenClinicaException
    */
    public String createParticipant(SubjectTransferBean subjectTransfer,Study currentStudy,String accessToken,
                                    String realm,String customerUuid, UserAccountBean userAccountBean, Locale locale) throws Exception {


        // create subject
        Study siteStudy = subjectTransfer.getSiteStudy();
        StudySubject studySubject=null;

        StudySubjectBean studySubjectBean  =getStudySubjectDao().findByLabelAndStudyForCreatingParticipant(subjectTransfer.getPersonId(), currentStudy.getStudyId());

        StudySubjectBean studySubjectBeanInParent = new StudySubjectBean();
        if (currentStudy.isSite()) {
            studySubjectBeanInParent = getStudySubjectDao().findByLabelAndStudyForCreatingParticipant(subjectTransfer.getPersonId(), currentStudy.getStudy().getStudyId());
        }
        if(!validateService.isStudyAvailable(currentStudy.getOc_oid()))
            throw new OpenClinicaSystemException(ErrorConstants.ERR_STUDY_NOT_AVAILABLE);


        if(!validateService.isSiteAvailable(siteStudy.getOc_oid()))
            throw new OpenClinicaSystemException(ErrorConstants.ERR_SITE_NOT_AVAILABLE);

        if(!utilService.isParticipantUniqueToSite(siteStudy.getOc_oid(),subjectTransfer.getStudySubjectId()))
            throw new OpenClinicaSystemException(ErrorConstants.ERR_PARTICIPANT_NOT_FOUND);


        if(studySubjectBean==null || (!studySubjectBean.isActive() && !studySubjectBeanInParent.isActive())) {
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
                studySubjectBean.setStudyId(siteStudy.getStudyId());
            } else {
                studySubjectBean.setStudyId(subjectTransfer.getStudy().getStudyId());
            }

            studySubjectBean.setLabel(subjectTransfer.getStudySubjectId());
            studySubjectBean.setStatus(Status.AVAILABLE);
            studySubjectBean.setUserStatus(UserStatus.CREATED);
            studySubjectBean.setOwner(subjectTransfer.getOwner());
            Date now = new Date();
            studySubjectBean.setCreatedDate(now);
            studySubjectBean = this.getStudySubjectDao().createWithoutGroup(studySubjectBean);

        }
        // OC-11095 Adding Contact information Through Rest API on a signed participant is failing
        if(studySubjectBean!=null && !(studySubjectBean.getStatus().equals(Status.AVAILABLE) || studySubjectBean.getStatus().equals(Status.SIGNED)))
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
            userService.connectParticipant(currentStudy.getOc_oid(), subjectTransfer.getPersonId(),
                    oCParticipantDTO, accessToken, userAccountBean, realm,customerUuid, textsBundle);
        }


        return studySubject.getLabel();
   }

/**
 * @param currentStudy
 */
private void updateStudySubjectSize(Study currentStudy) {
	int subjectCount = getSubjectCount(currentStudy);
	   currentStudy.setSubjectCount(subjectCount+1);
	   studyDao.update(currentStudy);
}
    /**
     * Validate the listStudySubjectsInStudy request.
     * @param studyOid
     * @param siteOid
     * @param request
     * @return
     */
    public Study validateRequestAndReturnStudy(String studyOid, String siteOid,HttpServletRequest request) throws OpenClinicaSystemException{

        String userName = getUserAccount(request).getName();
        Study study = null;
        Study site = null;
        
        if (studyOid == null && siteOid == null) {
            throw new OpenClinicaSystemException("errorCode.invalidStudyAndSiteIdentifier", "Provide a valid study/site.");
        }else if (studyOid != null && siteOid == null) {
            study = studyDao.findByOcOID(studyOid);
            if (study == null) {
                throw new OpenClinicaSystemException("errorCode.invalidStudyIdentifier", "The study identifier you provided is not valid.");
            }


            StudyUserRoleBean studyLevelRole = getUserAccountDao().findTheRoleByUserNameAndStudyOid(userName, studyOid);
            if (studyLevelRole == null) {
                throw new OpenClinicaSystemException("errorCode.noRoleSetUp",
                        "You do not have sufficient privileges to proceed with this operation.");
            }else if(studyLevelRole.getId() == 0 || studyLevelRole.getRole().equals(Role.MONITOR)) {
            	throw new OpenClinicaSystemException("errorCode.noSufficientPrivileges", "You do not have sufficient privileges to proceed with this operation.");
            } 
            
            
        }else if (studyOid != null && siteOid != null) {
            study = studyDao.findByOcOID(studyOid);
            site = studyDao.findByOcOID(siteOid);
            if (study == null) {
                throw new OpenClinicaSystemException("errorCode.invalidStudyIdentifier",
                        "The study identifier you provided is not valid.");
            }


            if (site == null || site.getStudy().getStudyId() != study.getStudyId()) {
                throw new OpenClinicaSystemException("errorCode.invalidSiteIdentifier",
                        "The site identifier you provided is not valid.");
            }


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

    public int getSubjectCount(Study currentStudy) {
        int subjectCount = 0;
        Study studyBean = (Study) studyDao.findByPK(currentStudy.getStudyId());
        if (studyBean != null)
            subjectCount = studyBean.getSubjectCount();

        if(subjectCount==0) {
            StudySubjectDAO ssdao = this.getStudySubjectDao();
            ArrayList ss = ssdao.findAllBySiteId(currentStudy.getStudyId());
            if (ss != null) {
                subjectCount = ss.size();
            }
        }
        return subjectCount;
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
            logger.error("Error while accessing file: ",e);
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
