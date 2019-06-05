package org.akaza.openclinica.validator;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.managestudy.SubjectTransferBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ParticipantValidator extends SubjectTransferValidator {

	private StudySubjectDAO studySubjectDao;
	private UserAccountDAO userAccountDAO;
	private StudyParameterValueDAO studyParameterValueDAO;
	private StudyBean currentStudy;
	private StudyBean siteStudy;
	
	private boolean isBulkMode = false;
	
	@Autowired
    private Configuration freemarkerConfiguration;

	
	
	public ParticipantValidator(DataSource dataSource) {
		super(dataSource);		
	}
	
	/**
     * @return the subjectDao
     */
    public StudySubjectDAO getStudySubjectDao() {
        studySubjectDao = studySubjectDao != null ? studySubjectDao : new StudySubjectDAO(dataSource);
        return studySubjectDao;
    }
    
    
    public UserAccountDAO getUserAccountDAO() {
    	userAccountDAO = userAccountDAO != null ? userAccountDAO : new UserAccountDAO(dataSource);
        return userAccountDAO;
    }
    
    public StudyParameterValueDAO getStudyParameterValueDAO() {
        return this.studyParameterValueDAO != null ? studyParameterValueDAO : new StudyParameterValueDAO(dataSource);
    }
         
    protected boolean isEnrollmentCapped(){

        boolean capIsOn = isEnrollmentCapEnforced();

        StudySubjectDAO studySubjectDAO = this.getStudySubjectDAO();;
        int numberOfSubjects = studySubjectDAO.getCountofActiveStudySubjects();

        StudyDAO studyDAO = this.getStudyDAO();
        StudyBean sb = null;
        if(currentStudy.getParentStudyId()!=0){
            sb = (StudyBean) studyDAO.findByPK(currentStudy.getParentStudyId());
        }else{
             sb = (StudyBean) studyDAO.findByPK(currentStudy.getId());
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
    private boolean isEnrollmentCapEnforced(){
        StudyParameterValueDAO studyParameterValueDAO = new StudyParameterValueDAO(this.dataSource);
       
        boolean capEnforcedSite = false;
        String  enrollmentCapStatusSite = null;
        
        String enrollmentCapStatus = studyParameterValueDAO.findByHandleAndStudy(currentStudy.getId(), "enforceEnrollmentCap").getValue();
        boolean capEnforced = Boolean.valueOf(enrollmentCapStatus);
        
        // check at the site level
        if(siteStudy != null) {
        	int siteId = siteStudy.getId();
        	enrollmentCapStatusSite = studyParameterValueDAO.findByHandleAndStudy(siteId, "enforceEnrollmentCap").getValue();
        	capEnforcedSite = Boolean.valueOf(enrollmentCapStatusSite);        	
        }
        
        if(capEnforcedSite || capEnforced) {
        	return true;
        }else {
        	return false;
        }
        
    }

    public String generateParticipantIdUsingTemplate(StudyBean currentStudy) {
        Map<String, Object> data = new HashMap<String, Object>();
        String templateID = "";
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(dataSource);
        StudyParameterValueBean spv = spvdao.findByHandleAndStudy(currentStudy.getParentStudyId()==0 ?currentStudy.getId():currentStudy.getParentStudyId(), "participantIdTemplate");
        if (spv != null)
            templateID = spv.getValue();

        int subjectCount = currentStudy.getSubjectCount();
        if(subjectCount==0) {
            StudySubjectDAO ssdao = new StudySubjectDAO(dataSource);
            ArrayList ss = ssdao.findAllBySiteId(currentStudy.getId());
            if (ss != null) {
                subjectCount = ss.size();
            }
        }
        String siteId = currentStudy.getIdentifier();

        // Adding Sample data to validate templateID
        data.put("siteId", siteId);
        data.put("siteParticipantCount", subjectCount);
        StringWriter wtr = new StringWriter();
        Template template = null;

        try {
            template = new Template("template name", new StringReader(templateID), freemarkerConfiguration);
            template.process(data, wtr);

        } catch (TemplateException te) {
            te.printStackTrace();


        } catch (IOException ioe) {
            ioe.printStackTrace();


        }
        return wtr.toString();
    }
    
	 public void validate(Object obj, Errors e) {
	        SubjectTransferBean subjectTransferBean = (SubjectTransferBean) obj;
	        currentStudy = subjectTransferBean.getStudy();

	        
	        if (currentStudy == null) {
	        	currentStudy = getStudyDAO().findByPublicOid(subjectTransferBean.getStudyOid());
	        }
	        
	        if (currentStudy == null) {
	            e.reject("errorCode.studyNotExist", new Object[] { subjectTransferBean.getStudyOid() }, "Study identifier you specified "
	                + subjectTransferBean.getStudyOid() + " does not correspond to a valid study.");
	            return;
	        }
	        
	        String siteOid = subjectTransferBean.getSiteIdentifier();
	        StudyBean siteStudy = subjectTransferBean.getSiteStudy();
	        if(siteOid !=null && siteStudy == null) {
	        	 e.reject("errorCode.siteNotExist", new Object[] { siteOid },
	 	                    "Site identifier you specified does not correspond to a valid site.");
	 	            return;
	        }

	        /**
	         *  check role permission at study  and site level
	         */
	        	
	        String userName = subjectTransferBean.getOwner().getName();
	        String studyOid = currentStudy.getOid();	        
	        
	        StudyUserRoleBean studyLevelRole = this.getUserAccountDAO().findTheRoleByUserNameAndStudyOid(userName,studyOid);
			if(studyLevelRole == null) {
				if (subjectTransferBean.getSiteIdentifier() != null) {
	 	        	siteStudy = subjectTransferBean.getSiteStudy();
	 	        	if (siteStudy == null) {
	 	 	            e.reject("errorCode.siteNotExist", new Object[] { siteOid },
	 	 	                    "Site identifier you specified does not correspond to a valid site.");
	 	 	            return;
	 	 	        }else {
	 	 	        	StudyUserRoleBean siteLevelRole = this.getUserAccountDAO().findTheRoleByUserNameAndStudyOid(userName,siteOid);
	 	 	        	if(siteLevelRole == null) {
	 	 	        		 e.reject("errorCode.noRoleSetUp", "You do not have any role set up for user " + userName + " in study site " + siteOid );
	 	 	        		return;
	 	 	        	}else if(siteLevelRole.getId() == 0 || siteLevelRole.getRole().equals(Role.MONITOR)) {
	 	 				    e.reject("errorCode.noSufficientPrivileges", "You do not have sufficient privileges to proceed with this operation.");
	 	 				    return;
	 	 				}
	 	 	        }
		        }else {
		        	 e.reject("errorCode.noRoleSetUp", "You do not have any role set up for user " + userName + " in study " + studyOid );
		        	 return;
		        }
				
				 
			}else if(studyLevelRole.getId() == 0 || studyLevelRole.getRole().equals(Role.MONITOR)) {
			    e.reject("errorCode.noSufficientPrivileges", "You do not have sufficient privileges to proceed with this operation.");
			    return;
			}
				          
	        int handleStudyId = currentStudy.getParentStudyId() > 0 ? currentStudy.getParentStudyId() : currentStudy.getId();
	        String idSetting = "";
	        StudyParameterValueBean subjectIdGenerationParameter = getStudyParameterValueDAO().findByHandleAndStudy(handleStudyId, "subjectIdGeneration");
	        idSetting = subjectIdGenerationParameter.getValue();
	        if ((idSetting.equals("auto editable") || idSetting.equals("auto non-editable")) && ! this.isBulkMode()) {
	        	
	        	/**
				 *  Participant ID auto generate
				 */
	        	String nextLabel = null;
		        StudyParameterValueBean participantIdTemplateSetting = this.getStudyParameterValueDAO().findByHandleAndStudy(handleStudyId, "participantIdTemplate");
		        if (participantIdTemplateSetting!=null && participantIdTemplateSetting.getValue() != null) {
		        	nextLabel = this.generateParticipantIdUsingTemplate(this.currentStudy);		        			        	   
	            }else {
	            	// generated by default template
		        	nextLabel = createSubjectIdByDefaultTemplate(); 		        	
	            }
		        
		        subjectTransferBean.setPersonId(nextLabel);
	            subjectTransferBean.setStudySubjectId(nextLabel);
	        	
	        }else {
	        	// check the manually provided value
	        	if (subjectTransferBean.getStudySubjectId().contains("<") || subjectTransferBean.getStudySubjectId().contains(">")) {
		        	 e.reject("errorCode.participantIDContainsUnsupportedHTMLCharacter","Participant ID provided in the request contains unsupported  HTML (< and >) characters"); 
		        	 return;
	            }
	        }
	        
	        
	        if (isEnrollmentCapped()){
	        	 e.reject("errorCode.participantsEnrollmentCapReached","Participants enrollment cap has reached and hence NO MORE participant can be added to the study");   
	        	 return;
            }
	        
	        /**
			 * Applicable ONLY when manual entry: Participant ID provided in the request is a duplicate. A participant already exists with that ID
			 * in study level
			 */
	        StudyBean checkStudy = currentStudy;
        	        
	        StudyParameterValueBean studyParameter = getStudyParameterValueDAO().findByHandleAndStudy(handleStudyId, "subjectPersonIdRequired");
	        String personId = subjectTransferBean.getPersonId();
	        if ("required".equals(studyParameter.getValue()) && (personId == null || personId.length() < 1)) {
	            e.reject("errorCode.subjectPersonIdRequired", new Object[] { currentStudy.getName() }, "personId is required for the study: " + currentStudy.getName());
	            return;
	        }
	        
	        String studySubjectId = subjectTransferBean.getStudySubjectId();
	        if (studySubjectId == null || studySubjectId.length() < 1) {
	            e.reject("errorCode.participantIDEmpty");
	            return;
	        } else if (studySubjectId.length() > 30) {
	            e.reject("errorCode.participantIDLongerThan30Characters", new Object[] { studySubjectId }, "studySubjectId: " + studySubjectId
	                + " cannot be longer than 30 characters.");
	            return;
	        }
     	        
	    }

	 public void validateBulk(Object obj, Errors e) {
	        SubjectTransferBean subjectTransferBean = (SubjectTransferBean) obj;
	        currentStudy = subjectTransferBean.getStudy();	        
	        
	        if (currentStudy == null) {
	        	currentStudy = getStudyDAO().findByPublicOid(subjectTransferBean.getStudyOid());
	        }
        	          	       
        	// check the manually provided value
        	if (subjectTransferBean.getStudySubjectId().contains("<") || subjectTransferBean.getStudySubjectId().contains(">")) {
	        	 e.reject("errorCode.participantIDContainsUnsupportedHTMLCharacter","Participant ID provided in the request contains unsupported  HTML (< and >) characters"); 
	        	 return;
            }
    	        	        
	        if (isEnrollmentCapped()){
	        	 e.reject("errorCode.participantsEnrollmentCapReached","Participants enrollment cap has reached and hence NO MORE participant can be added to the study");   
	        	 return;
	        }

	        int handleStudyId = currentStudy.getParentStudyId() > 0 ? currentStudy.getParentStudyId() : currentStudy.getId();		     
	        StudyParameterValueBean studyParameter = getStudyParameterValueDAO().findByHandleAndStudy(handleStudyId, "subjectPersonIdRequired");
	        String personId = subjectTransferBean.getPersonId();
	        if ("required".equals(studyParameter.getValue()) && (personId == null || personId.length() < 1)) {
	            e.reject("errorCode.subjectPersonIdRequired", new Object[] { currentStudy.getName() }, "personId is required for the study: " + currentStudy.getName());
	            return;
	        }
	        
	        String studySubjectId = subjectTransferBean.getStudySubjectId();
	        if (studySubjectId == null || studySubjectId.length() < 1) {
	            e.reject("errorCode.participantIDEmpty");
	            return;
	        } else if (studySubjectId.length() > 30) {
	            e.reject("errorCode.participantIDLongerThan30Characters", new Object[] { studySubjectId }, "ParticipantID: " + studySubjectId
	                + " cannot be longer than 30 characters.");
	            return;
	        }

	        validateParticipantData(subjectTransferBean, e);

	    }

	/**
	 * @param e
	 * @param userName
	 * @param studyOid
	 */
	private boolean checkUserPermission(Errors e, String userName, String studyOid) {
		boolean hasPermission =true;
		
		StudyUserRoleBean role = this.getUserAccountDAO().findTheRoleByUserNameAndStudyOid(userName,studyOid);
		if(role == null) {
			 e.reject("subjectTransferValidator.no_roles", "You do not have any role set up for user " + userName + " in study " + studyOid );
			 hasPermission = false;
		}else if(role.getId() == 0 || role.getRole().equals(Role.MONITOR)) {
		    e.reject("subjectTransferValidator.insufficient_permissions", "You do not have sufficient privileges to proceed with this operation.");
		    hasPermission = false;
		}
		
		return  hasPermission;
	}

	/**
	 * @return
	 */
	private String createSubjectIdByDefaultTemplate() {
		String nextLabel = null;   
		StudySubjectBean studySubjectBean = new StudySubjectBean();
		while(studySubjectBean!=null) {
		    Random rnd = new Random();
		    int n = 100000 + rnd.nextInt(900000);
		    nextLabel = this.currentStudy.getOid() + "-" + n;
		    studySubjectBean= this.getStudySubjectDao().findByLabel(nextLabel);
		if(studySubjectBean!=null && !studySubjectBean.isActive())
		    studySubjectBean=null;
		}
		return nextLabel;
	}

	public boolean isBulkMode() {
		return isBulkMode;
	}

	public void setBulkMode(boolean isBulkMode) {
		this.isBulkMode = isBulkMode;
	}

	public void validateParticipantData(SubjectTransferBean subjectTransferBean, Errors errors) {
		if (subjectTransferBean.getFirstName()!=null && subjectTransferBean.getFirstName().length()>35){
			errors.reject("errorCode.firsNameTooLong","First name length should not exceed 35 characters");
		}
		if (subjectTransferBean.getLastName()!=null && subjectTransferBean.getLastName().length()>35){
			errors.reject("errorCode.lastNameTooLong","Last name length should not exceed 35 characters");
		}
		if (subjectTransferBean.getIdentifier()!=null && subjectTransferBean.getIdentifier().length()>35){
			errors.reject("errorCode.identifierTooLong","Identifier length should not exceed 35 characters");
		}
		if (subjectTransferBean.getEmailAddress()!=null &&  subjectTransferBean.getEmailAddress().length()>255){
			errors.reject("errorCode.emailAddressTooLong","Email Address length should not exceed 255 characters");
		}

		if (subjectTransferBean.getEmailAddress()!=null &&  ! EmailValidator.getInstance().isValid(subjectTransferBean.getEmailAddress())&& subjectTransferBean.getEmailAddress().length()!=0){
			errors.reject("errorCode.invalidEmailAddress","Email Address contains invalid characters or format");
		}

		if (subjectTransferBean.getPhoneNumber()!=null && subjectTransferBean.getPhoneNumber().length()>15){
			errors.reject("errorCode.phoneNumberTooLong","Phone number length should not exceed 15 characters");
		}

		if (subjectTransferBean.getPhoneNumber()!=null && !onlyContainsNumbers(subjectTransferBean.getPhoneNumber()) && subjectTransferBean.getPhoneNumber().length()!=0) {
			errors.reject("errorCode.invalidPhoneNumber","Phone number should not containe alphabetic characters");
		}

	}

	private boolean onlyContainsNumbers(String text) {
		try {
			Long.parseLong(text);
			return true;
		} catch (NumberFormatException ex) {
			return false;
		}
	}
}
