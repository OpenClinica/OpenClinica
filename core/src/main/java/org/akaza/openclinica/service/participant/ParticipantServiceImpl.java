package org.akaza.openclinica.service.participant;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.managestudy.StudyType;
import org.akaza.openclinica.bean.managestudy.SubjectTransferBean;
import org.akaza.openclinica.bean.oid.OidGenerator;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.core.SessionManager;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import org.akaza.openclinica.dao.hibernate.SubjectDao;
import org.akaza.openclinica.dao.hibernate.UserAccountDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.domain.datamap.*;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.service.dto.AuditLogEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service("ParticipantService")
@Transactional(propagation= Propagation.REQUIRED,isolation= Isolation.DEFAULT)
public class ParticipantServiceImpl implements ParticipantService {
	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
	
	private SubjectDAO subjectDao;
	private StudyParameterValueDAO studyParameterValueDAO;		
	private StudySubjectDAO studySubjectDao;
	private StudyDAO studyDao;
	
	@Autowired
	private UserAccountDAO userAccountDao;



    @Autowired
    private StudySubjectDao studySubjectHibDao;

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
    public String createParticipant(SubjectTransferBean subjectTransfer,StudyBean currentStudy,String accessToken) throws Exception {
   	   // create subject
        StudyBean siteStudy = subjectTransfer.getSiteStudy();
        String siteOid = subjectTransfer.getSiteIdentifier();
        StudySubject studySubject=null;
      StudySubjectBean studySubjectBean  =getStudySubjectDao().findByLabelAndStudy(subjectTransfer.getPersonId(), currentStudy);

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
    studySubjectBean.setOwner(subjectTransfer.getOwner());
    Date now = new Date();
    studySubjectBean.setCreatedDate(now);
    studySubjectBean.setUpdater(subjectTransfer.getOwner());
    studySubjectBean.setUpdatedDate(now);
    studySubjectBean = this.getStudySubjectDao().createWithoutGroup(studySubjectBean);

}
        studySubject=saveOrUpdateStudySubjectDetails( studySubjectBean,  subjectTransfer,accessToken,currentStudy.getOid(),subjectTransfer.getOwner());

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
    
    private StudySubjectBean createStudySubject(SubjectBean subject, StudyBean studyBean, Date enrollmentDate, String secondaryId) {
        StudySubjectBean studySubject = new StudySubjectBean();
        studySubject.setSecondaryLabel(secondaryId);
        studySubject.setOwner(getUserAccount());
        studySubject.setEnrollmentDate(enrollmentDate);
        studySubject.setSubjectId(subject.getId());
        studySubject.setStudyId(studyBean.getId());
        studySubject.setStatus(Status.AVAILABLE);
        
        int handleStudyId = studyBean.getParentStudyId() > 0 ? studyBean.getParentStudyId() : studyBean.getId();
        StudyParameterValueBean subjectIdGenerationParameter = getStudyParameterValueDAO().findByHandleAndStudy(handleStudyId, "subjectIdGeneration");
        String idSetting = subjectIdGenerationParameter.getValue();
        if (idSetting.equals("auto editable") || idSetting.equals("auto non-editable")) {
        	// Warning: Here we have a race condition. 
        	// At least, a uniqueness constraint should be set on the database! Better provide an atomic method which stores a new label in the database and returns it.  
            int nextLabel = getStudySubjectDao().findTheGreatestLabel() + 1;
            studySubject.setLabel(Integer.toString(nextLabel));
        } else {
        	studySubject.setLabel(subject.getLabel());
        	subject.setLabel(null);
        }
        
        return studySubject;

    }

    /**
     * Validate the listStudySubjectsInStudy request.
     * 
     * @param studyRef
     * @return StudyBean
     */
    public StudyBean validateRequestAndReturnStudy(String studyOid, String siteOid,HttpServletRequest request) {

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
            
            if (site == null || site.getParentStudyId() != study.getId()) {
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
    public void validateSubjectTransfer(SubjectTransferBean subjectTransferBean) {
        // TODO: Validate here
    }

    /**
     * Getting the first user account from the database. This would be replaced by an authenticated user who is doing the SOAP requests .
     * 
     * @return UserAccountBean
     */
    private UserAccountBean getUserAccount() {

        UserAccountBean user = new UserAccountBean();
        user.setId(1);
        return user;
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
     * @param datasource
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



    private AuditLogEventDTO populateAuditLogEventDTO(int entityId, String oldValue, String newValue, int auditLogEventTypeId, String entityName , int userId) {
        AuditLogEventDTO auditLogEventDTO=null;
            auditLogEventDTO = new AuditLogEventDTO();
            auditLogEventDTO.setOldValue(oldValue);
            auditLogEventDTO.setNewValue(newValue);
            auditLogEventDTO.setEntityId(entityId);
            auditLogEventDTO.setEntityName(entityName);
            auditLogEventDTO.setAuditTable("study_subject");
            auditLogEventDTO.setAuditLogEventTypId(auditLogEventTypeId);
            auditLogEventDTO.setUserId(userId);



        return auditLogEventDTO;

    }

    private StudySubject saveOrUpdateStudySubjectDetails(StudySubjectBean studySubjectBean, SubjectTransferBean subjectTransfer, String accessToken, String studyOid , UserAccountBean userAccountBean) {
        StudySubject studySubject = studySubjectHibDao.findById(studySubjectBean.getId());
        String firstNameOldValue = "";
        String lastNameOldValue = "";
        String emailOldValue = "";
        String phoneOldValue = "";
        String identifierOldValue = "";


        StudySubjectDetail studySubjectDetail = studySubject.getStudySubjectDetail();


        if (studySubjectDetail == null) {
            studySubjectDetail = new StudySubjectDetail();
        } else {
            firstNameOldValue = studySubjectDetail.getFirstName();
            lastNameOldValue = studySubjectDetail.getLastName();
            emailOldValue = studySubjectDetail.getEmail();
            phoneOldValue = studySubjectDetail.getPhone();
            identifierOldValue = studySubjectDetail.getIdentifier();
        }


        studySubjectDetail.setFirstName(subjectTransfer.getFirstName());
        studySubjectDetail.setLastName(subjectTransfer.getLastName());
        studySubjectDetail.setIdentifier(subjectTransfer.getIdentifier());
        studySubjectDetail.setEmail(subjectTransfer.getEmailAddress());
        studySubjectDetail.setPhone(subjectTransfer.getPhoneNumber());

        studySubject.setStudySubjectDetail(studySubjectDetail);

        studySubject = studySubjectHibDao.saveOrUpdate(studySubject);

        int studySubjectId = studySubject.getStudySubjectId();
        String firstNameNewValue = subjectTransfer.getFirstName();
        String lastNameNewValue = subjectTransfer.getLastName();
        String emailNewValue = subjectTransfer.getEmailAddress();
        String phoneNewValue = subjectTransfer.getPhoneNumber();
        String identifierNewValue = subjectTransfer.getIdentifier();
        AuditLogEventDTO auditLogEventDTO=null;

        if (!firstNameNewValue.equals(firstNameOldValue)) {
            auditLogEventDTO=populateAuditLogEventDTO(studySubjectId, firstNameOldValue, firstNameNewValue, (firstNameOldValue == null || firstNameOldValue == "") ? 43 : 44, "Participant first name",userAccountBean.getId() );
            auditEvent(auditLogEventDTO, accessToken, studyOid);
        }

        if (!emailNewValue.equals(emailOldValue)) {
            auditLogEventDTO=    populateAuditLogEventDTO(studySubjectId, emailOldValue, emailNewValue, (emailOldValue == null || emailOldValue == "") ? 46 : 47, "Participant email address",userAccountBean.getId());
            auditEvent(auditLogEventDTO, accessToken, studyOid);
        }

        if (!phoneNewValue.equals(phoneOldValue)) {
            auditLogEventDTO= populateAuditLogEventDTO(studySubjectId, phoneOldValue, phoneNewValue, (phoneOldValue == null || phoneOldValue == "") ? 49 : 50, "Participant phone number",userAccountBean.getId());
            auditEvent(auditLogEventDTO, accessToken, studyOid);
        }
        if (!lastNameNewValue.equals(lastNameOldValue)) {
            auditLogEventDTO= populateAuditLogEventDTO(studySubjectId, lastNameOldValue, lastNameNewValue, (lastNameOldValue == null || lastNameOldValue == "") ? 52 : 53, "Participant last name",userAccountBean.getId());
            auditEvent(auditLogEventDTO, accessToken, studyOid);
        }
        if (!identifierNewValue.equals(identifierOldValue)) {
            auditLogEventDTO= populateAuditLogEventDTO(studySubjectId, identifierOldValue, identifierNewValue, (identifierOldValue == null || identifierOldValue == "") ? 55 : 56, "Participant identifier",userAccountBean.getId());
            auditEvent(auditLogEventDTO, accessToken, studyOid);
        }
        return studySubject;

    }
    public void auditEvent(AuditLogEventDTO auditLogEventDTO, String accessToken, String studyOid) {
        String urlBase = CoreResources.getField("sysURL").split("/MainMenu")[0];


        String uri = urlBase + "/pages/auth/api/studies/" + studyOid + "/auditEvents";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Accept-Charset", "UTF-8");
        StudyBean studyBean = null;
        HttpEntity<AuditLogEventDTO> entity = new HttpEntity<>(auditLogEventDTO,headers);

        try {

          ResponseEntity responseEntity=  restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
         if(responseEntity!=null){
             logger.info("Auditing complete");
         }

        } catch (Exception e) {
            logger.error("Auditing error");
        }

    }

}
