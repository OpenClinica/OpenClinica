package org.akaza.openclinica.controller.openrosa.processor;

import java.util.Date;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.controller.openrosa.SubmissionContainer;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.StudyUserRoleDao;
import org.akaza.openclinica.dao.hibernate.UserAccountDao;
import org.akaza.openclinica.dao.hibernate.UserTypeDao;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyUserRole;
import org.akaza.openclinica.domain.datamap.StudyUserRoleId;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.domain.user.UserType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class UserProcessor implements Processor, Ordered {

    @Autowired
    UserAccountDao userAccountDao;
    
    @Autowired
    UserTypeDao userTypeDao;
    
    @Autowired
    StudyUserRoleDao studyUserRoleDao;
    
    @Autowired
    StudyDao studyDao;
    
    public static final String INPUT_FIRST_NAME = "Participant";
    public static final String INPUT_LAST_NAME = "User";
    public static final String INPUT_EMAIL = "email";
    public static final String INPUT_INSTITUTION = "PFORM";
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Override
    public void process(SubmissionContainer container) throws Exception{
        logger.info("Executing User Processor.");
        Errors errors = container.getErrors();

        String contextStudySubjectOid = container.getSubjectContext().get("studySubjectOID");
        String studySubjectOid = container.getSubject().getOcOid();
        String parentStudyOid = getParentStudy(container.getStudy().getOc_oid()).getOc_oid();

        // if study subject oid is not null, just look up user account
        if (contextStudySubjectOid != null) {
            String userName = parentStudyOid + "." + contextStudySubjectOid;
            UserAccount existingAccount = userAccountDao.findByUserName(userName);
            if (existingAccount == null) {
                logger.info("Could not find existing user account.  Aborting submission.");
                errors.reject("Could not find existing user account.  Aborting submission.");
                throw new Exception("Could not find existing user account.  Aborting submission.");
            }
            container.setUser(existingAccount);
        } else {
            String userName = parentStudyOid + "." + studySubjectOid;
            UserAccount existingAccount = userAccountDao.findByUserName(userName);
            if (existingAccount != null) {
                container.setUser(existingAccount);;
            } else {
                //Create user account
                UserAccount rootUser = userAccountDao.findByUserId(1);
                UserAccount createdUser = new UserAccount();
                createdUser.setUserName(parentStudyOid + "." + container.getSubject().getOcOid());
                createdUser.setFirstName(INPUT_FIRST_NAME);
                createdUser.setLastName(INPUT_LAST_NAME);
                createdUser.setEmail(INPUT_EMAIL);
                createdUser.setInstitutionalAffiliation(INPUT_INSTITUTION);
                createdUser.setActiveStudy(container.getStudy());
                String passwordHash = UserAccountBean.LDAP_PASSWORD;
                createdUser.setPasswd(passwordHash);
                createdUser.setPasswdTimestamp(null);
                createdUser.setDateLastvisit(null);
                createdUser.setStatus(Status.DELETED);
                createdUser.setPasswdChallengeQuestion("");
                createdUser.setPasswdChallengeAnswer("");
                createdUser.setPhone("");
                createdUser.setUserAccount(rootUser);
                createdUser.setRunWebservices(false);
                createdUser.setActiveStudy(container.getStudy());
                UserType type = userTypeDao.findByUserTypeId(2);
                createdUser.setUserType(type);
                createdUser.setEnabled(true);
                createdUser.setAccountNonLocked(true);
                createdUser.setAccessCode("");
                createdUser.setApiKey("");
                createdUser = userAccountDao.saveOrUpdate(createdUser);
                container.setUser(createdUser);
                
                //Create study user role
                Date date = new Date();
                StudyUserRoleId studyUserRoleId = new StudyUserRoleId(Role.RESEARCHASSISTANT2.getName(), container.getStudy().getStudyId(), Status.AUTO_DELETED.getCode(),
                        rootUser.getUserId(), date,
                        createdUser.getUserName());
                StudyUserRole studyUserRole = new StudyUserRole(studyUserRoleId);
                studyUserRoleDao.saveOrUpdate(studyUserRole);
                //TODO: StudyUserRole object had to be heavily modified.  May need fixing.  Also roleName specified
                // doesn't exist in role table.  May need to fix that.
                // This table should also foreign key to user_account but doesn't.
            }
        }
    }


    @Override
    public int getOrder() {
        return 2;
    }
    private Study getParentStudy(String studyOid) {
        Study study = studyDao.findByOcOID(studyOid);
        Study parentStudy = study.getStudy();
        if (parentStudy != null && parentStudy.getStudyId() > 0)
            return parentStudy;
        else 
            return study;
    }

}
