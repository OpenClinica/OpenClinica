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

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.managestudy.StudyType;
import org.akaza.openclinica.bean.managestudy.SubjectTransferBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.core.SessionManager;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
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

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

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
    public String createParticipant(SubjectTransferBean subjectTransfer,StudyBean currentStudy) throws OpenClinicaException {
   	   // create subject
       SubjectBean subject = new SubjectBean();
       subject.setStatus(Status.AVAILABLE);
       subject.setOwner(subjectTransfer.getOwner());
      
       subject = this.getSubjectDao().create(subject);
       if (!subject.isActive()) {
           throw new OpenClinicaException("Could not create subject", "3");
       }
       
       // create study subject
       StudySubjectBean studySubject = new StudySubjectBean();
       studySubject.setSubjectId(subject.getId());
       studySubject.setStudyId(subjectTransfer.getStudy().getId());
       studySubject.setLabel(subjectTransfer.getStudySubjectId());
       studySubject.setStatus(Status.AVAILABLE);
       studySubject.setOwner(subjectTransfer.getOwner());
       studySubject = this.getStudySubjectDao().createWithoutGroup(studySubject);
       if (!studySubject.isActive()) {
           throw new OpenClinicaException("Could not create study subject", "4");
       }
       
       //update subject account
       if(currentStudy.getId() != subjectTransfer.getStudy().getId()) {
    	   //update at site level
    	   updateStudySubjectSize(subjectTransfer.getStudy());
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
	int subjectCount = currentStudy.getSubjectCount();
	   if(subjectCount==0) {
	       StudySubjectDAO ssdao = this.getStudySubjectDao();
	       ArrayList ss = ssdao.findAllBySiteId(currentStudy.getId());
	       if (ss != null) {
	           subjectCount = ss.size();
	       }
	   }
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

       
        if (studyOid == null && siteOid == null) {
            throw new OpenClinicaSystemException("studySubjectEndpoint.provide_valid_study_site", "Provide a valid study/site.");
        }
        if (studyOid != null && siteOid == null) {
            StudyBean study = getStudyDao().findByOid(studyOid);
            if (study == null) {
                throw new OpenClinicaSystemException("studySubjectEndpoint.invalid_study_identifier", "The study identifier you provided is not valid.");
            }
            StudyUserRoleBean studySur = getUserAccountDao().findRoleByUserNameAndStudyId(getUserAccount(request).getName(), study.getId());
            if (studySur.getStatus() != Status.AVAILABLE) {
                throw new OpenClinicaSystemException("studySubjectEndpoint.insufficient_permissions",
                        "You do not have sufficient privileges to proceed with this operation.");
            }
            return study;
        }
        if (studyOid != null && siteOid != null) {
            StudyBean study = getStudyDao().findByOid(studyOid);
            StudyBean site = getStudyDao().findByOid(siteOid);
            if (study == null || site == null || site.getParentStudyId() != study.getId()) {
                throw new OpenClinicaSystemException("studySubjectEndpoint.invalid_study_site_identifier",
                        "The study/site identifier you provided is not valid.");
            }
            StudyUserRoleBean siteSur = getUserAccountDao().findRoleByUserNameAndStudyId(getUserAccount(request).getName(), site.getId());
            if (siteSur.getStatus() != Status.AVAILABLE) {
                throw new OpenClinicaSystemException("studySubjectEndpoint.insufficient_permissions",
                        "You do not have sufficient privileges to proceed with this operation.");
            }
            return site;
        }
        return null;
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
}
