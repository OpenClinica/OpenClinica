package org.akaza.openclinica.validator;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.managestudy.SubjectTransferBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

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
	            e.reject("subjectTransferValidator.study_does_not_exist", new Object[] { subjectTransferBean.getStudyOid() }, "Study identifier you specified "
	                + subjectTransferBean.getStudyOid() + " does not correspond to a valid study.");
	            return;
	        }

	        /**
	         *  check role permission at study  and site level
	         */
	        	
	        String userName = subjectTransferBean.getOwner().getName();
	        String studyOid = currentStudy.getOid();
	        String siteOid = subjectTransferBean.getSiteIdentifier();
	        
	        StudyUserRoleBean studyLevelRole = this.getUserAccountDAO().findTheRoleByUserNameAndStudyOid(userName,studyOid);
			if(studyLevelRole == null) {
				if (subjectTransferBean.getSiteIdentifier() != null) {
	 	        	siteStudy = getStudyDAO().findSiteByOid(subjectTransferBean.getStudyOid(), siteOid);
	 	        	if (siteStudy == null) {
	 	 	            e.reject("subjectTransferValidator.site_does_not_exist", new Object[] { siteOid },
	 	 	                    "Site identifier you specified does not correspond to a valid site.");
	 	 	            return;
	 	 	        }else {
	 	 	        	StudyUserRoleBean siteLevelRole = this.getUserAccountDAO().findTheRoleByUserNameAndStudyOid(userName,siteOid);
	 	 	        	if(siteLevelRole == null) {
	 	 	        		 e.reject("subjectTransferValidator.no_roles", "You do not have any role set up for user " + userName + " in study " + siteOid );
	 	 	        		return;
	 	 	        	}else if(siteLevelRole.getId() == 0 || siteLevelRole.getRole().equals(Role.MONITOR)) {
	 	 				    e.reject("subjectTransferValidator.insufficient_permissions", "You do not have sufficient privileges to proceed with this operation.");
	 	 				    return;
	 	 				}
	 	 	        }
		        }else {
		        	 e.reject("subjectTransferValidator.no_roles", "You do not have any role set up for user " + userName + " in study " + studyOid );
		        	 return;
		        }
				
				 
			}else if(studyLevelRole.getId() == 0 || studyLevelRole.getRole().equals(Role.MONITOR)) {
			    e.reject("subjectTransferValidator.insufficient_permissions", "You do not have sufficient privileges to proceed with this operation.");
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
		        	 e.reject("study_subject_id_can_not_contain_html_lessthan_or_greaterthan_elements","Participant ID provided in the request contains unsupported  HTML (< and >) characters"); 
		        	 return;
	            }
	        }
	        
	        
	        if (isEnrollmentCapped()){
	        	 e.reject("current_study_full","Participants enrollment cap has reached and hence NO MORE participant can be added to the study");   
	        	 return;
            }
	        
	        /**
			 * Applicable ONLY when manual entry: Participant ID provided in the request is a duplicate. A participant already exists with that ID
			 * in study level
			 */
	        StudyBean checkStudy = currentStudy;
	        
	        if(getStudySubjectDao().findByLabelAndStudy(subjectTransferBean.getPersonId(), checkStudy).getId() != 0) {
	        	 e.reject("Study Participant", "Participant ID " + subjectTransferBean.getPersonId() + " already exists with that ID, please use different ID");
		         return;				
			}
        	        
	        StudyParameterValueBean studyParameter = getStudyParameterValueDAO().findByHandleAndStudy(handleStudyId, "subjectPersonIdRequired");
	        String personId = subjectTransferBean.getPersonId();
	        if ("required".equals(studyParameter.getValue()) && (personId == null || personId.length() < 1)) {
	            e.reject("subjectTransferValidator.personId_required", new Object[] { currentStudy.getName() }, "personId is required for the study: " + currentStudy.getName());
	            return;
	        }

	        if (personId != null && personId.length() > 255) {
	            e.reject("subjectTransferValidator.personId_invalid_length", new Object[] { personId }, "personId: " + personId
	                + " cannot be longer than 255 characters.");
	            return;
	        }
			
	        
	        String studySubjectId = subjectTransferBean.getStudySubjectId();
	        if (studySubjectId == null || studySubjectId.length() < 1) {
	            e.reject("subjectTransferValidator.studySubjectId_required");
	            return;
	        } else if (studySubjectId.length() > 30) {
	            e.reject("subjectTransferValidator.studySubjectId_invalid_length", new Object[] { studySubjectId }, "studySubjectId: " + studySubjectId
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
		        	 e.reject("study_subject_id_can_not_contain_html_lessthan_or_greaterthan_elements","Participant ID provided in the request contains unsupported  HTML (< and >) characters"); 
		        	 return;
	            }
	        }
	        
	        
	        if (isEnrollmentCapped()){
	        	 e.reject("current_study_full","Participants enrollment cap has reached and hence NO MORE participant can be added to the study");   
	        	 return;
         }
	        
	        /**
			 * Applicable ONLY when manual entry: Participant ID provided in the request is a duplicate. A participant already exists with that ID
			 * in study level
			 */
	        StudyBean checkStudy = currentStudy;
      	        
	        if(getStudySubjectDao().findByLabelAndStudy(subjectTransferBean.getPersonId(), checkStudy).getId() != 0) {
	        	 e.reject("Study Participant", "Participant ID " + subjectTransferBean.getPersonId() + " already exists with that ID, please use different ID");
		         return;				
			}
     	        
	        StudyParameterValueBean studyParameter = getStudyParameterValueDAO().findByHandleAndStudy(handleStudyId, "subjectPersonIdRequired");
	        String personId = subjectTransferBean.getPersonId();
	        if ("required".equals(studyParameter.getValue()) && (personId == null || personId.length() < 1)) {
	            e.reject("subjectTransferValidator.personId_required", new Object[] { currentStudy.getName() }, "personId is required for the study: " + currentStudy.getName());
	            return;
	        }

	        if (personId != null && personId.length() > 255) {
	            e.reject("subjectTransferValidator.personId_invalid_length", new Object[] { personId }, "personId: " + personId
	                + " cannot be longer than 255 characters.");
	            return;
	        }
			
	        
	        String studySubjectId = subjectTransferBean.getStudySubjectId();
	        if (studySubjectId == null || studySubjectId.length() < 1) {
	            e.reject("subjectTransferValidator.studySubjectId_required");
	            return;
	        } else if (studySubjectId.length() > 30) {
	            e.reject("subjectTransferValidator.studySubjectId_invalid_length", new Object[] { studySubjectId }, "studySubjectId: " + studySubjectId
	                + " cannot be longer than 30 characters.");
	            return;
	        }
     	        
	        

	        
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

}
