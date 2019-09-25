package org.akaza.openclinica.service;

import liquibase.util.StringUtils;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.UserType;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.*;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.*;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.service.rest.errors.ParameterizedErrorVM;
import org.akaza.openclinica.web.restful.errors.ErrorConstants;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This Service class is used with View Study Subject Page
 *
 * @author joekeremian
 */

@Service( "validateService" )
public class ValidateServiceImpl implements ValidateService {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    public static final String ENABLED = "enabled";
    public static final String DISABLED = "enabled";

    private static String sbsUrl = CoreResources.getField("SBSUrl");
    private static final String ADVANCE_SEARCH = "contactsModule";

    @Autowired
    @Qualifier( "dataSource" )
    private BasicDataSource dataSource;

    @Autowired
    StudyDao studyDao;

    @Autowired
    StudyParameterValueDao studyParameterValueDao;


    public boolean isStudyOidValid(String studyOid) {
        Study publicStudy = getPublicStudy(studyOid);
        if (publicStudy != null) {
            return true;
        }
        return false;
    }


    public boolean isStudyAvailable(String studyOid) {
        Study publicStudy = getPublicStudy(studyOid);
        if (publicStudy != null && publicStudy.getStatus().equals(Status.AVAILABLE)) {
            return true;
        }
        return false;
    }



    public boolean isStudyOidValidStudyLevelOid(String studyOid) {
        Study publicStudy = getPublicStudy(studyOid);
        if (publicStudy != null && publicStudy.getStudy() == null) {
            return true;
        }
        return false;
    }

    public boolean isSiteOidValid(String siteOid) {
        Study publicSite = getPublicStudy(siteOid);
        if (publicSite != null) {
            return true;
        }
        return false;
    }

    public boolean isSiteOidValidSiteLevelOid(String siteOid) {
        Study publicSite = getPublicStudy(siteOid);
        if (publicSite != null && publicSite.getStudy() != null) {
            return true;
        }
        return false;
    }


    public boolean isStudyToSiteRelationValid(String studyOid, String siteOid) {
        Study publicStudy = getPublicStudy(studyOid);
        Study publicSite = getPublicStudy(siteOid);
        if (publicStudy != null && publicSite != null && publicSite.getStudy().getStudyId() == publicStudy.getStudyId()) {
            return true;
        }
        return false;
    }




    public boolean isUserHasTechAdminRole(UserAccount userAccount) {
        if (userAccount.getUserType().getUserTypeId() == UserType.TECHADMIN.getId())
            return true;
        return false;
    }


    public boolean isParticipateActive(Study tenantStudy) {
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(dataSource);
        String participateFormStatus = spvdao.findByHandleAndStudy(tenantStudy.getStudy() != null ? tenantStudy.getStudy().getStudyId() : tenantStudy.getStudyId(), "participantPortal").getValue();
        if (participateFormStatus.equals(ENABLED))
            return true;
        return false;
    }

    public boolean isAdvanceSearchEnabled(Study tenantStudy) {
        StudyParameterValue spv = studyParameterValueDao.findByStudyIdParameter(tenantStudy.getStudy() == null ? tenantStudy.getStudyId() : tenantStudy.getStudy().getStudyId(), ADVANCE_SEARCH);

        if (spv != null && spv.getValue().equals(ENABLED))
            return true;


        return false;
    }

    private Study getPublicStudy(String studyOid) {
        return studyDao.findPublicStudy(studyOid);
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean isUserHas_CRC_INV_RoleInSite(List<StudyUserRoleBean> userRoles, String siteOid) {
        Study publicSite = getPublicStudy(siteOid);

        for (StudyUserRoleBean userRole : userRoles) {
            if ((userRole.getRole().equals(Role.RESEARCHASSISTANT) && publicSite.getStudyId() == userRole.getStudyId())
                    || (userRole.getRole().equals(Role.INVESTIGATOR) && publicSite.getStudyId() == userRole.getStudyId()))
                return true;
        }

        return false;
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean isUserHas_DM_DEP_DS_RoleInStudy(List<StudyUserRoleBean> userRoles, String studyOid) {
        Study publicStudy = getPublicStudy(studyOid);

        for (StudyUserRoleBean userRole : userRoles) {
            if ((userRole.getRole().equals(Role.RESEARCHASSISTANT) && publicStudy.getStudyId() == userRole.getStudyId())
                    || (userRole.getRole().equals(Role.INVESTIGATOR) && publicStudy.getStudyId() == userRole.getStudyId())
                    || (userRole.getRole().equals(Role.COORDINATOR) && publicStudy.getStudyId() == userRole.getStudyId()))
                return true;

        }
        return false;
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean isUserHas_CRC_INV_DM_DEP_DS_RoleInSite(List<StudyUserRoleBean> userRoles, String siteOid) {
        Study publicSite = getPublicStudy(siteOid);
        for (StudyUserRoleBean userRole : userRoles) {
            if ((userRole.getRole().equals(Role.RESEARCHASSISTANT) && publicSite.getStudyId() == userRole.getStudyId())
                    || (userRole.getRole().equals(Role.INVESTIGATOR) && publicSite.getStudyId() == userRole.getStudyId())
                    || (userRole.getRole().equals(Role.RESEARCHASSISTANT) && (publicSite.getStudy().getStudyId() == userRole.getStudyId()))
                    || (userRole.getRole().equals(Role.INVESTIGATOR) && (publicSite.getStudy().getStudyId() == userRole.getStudyId()))
                    || (userRole.getRole().equals(Role.COORDINATOR) && (publicSite.getStudy().getStudyId() == userRole.getStudyId())))
                return true;

        }
        return false;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean isUserHasAccessToStudy(List<StudyUserRoleBean> userRoles, String studyOid) {
        Study publicStudy = getPublicStudy(studyOid);

        for (StudyUserRoleBean userRole : userRoles) {
            if (publicStudy.getStudyId() == userRole.getStudyId()) {
                return true;
            }
        }
        return false;
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public boolean isUserHasAccessToSite(List<StudyUserRoleBean> userRoles, String siteOid) {
        Study publicSite = getPublicStudy(siteOid);
        for (StudyUserRoleBean userRole : userRoles) {
            if ((publicSite.getStudyId() == userRole.getStudyId()) || (publicSite.getStudy() != null ? publicSite.getStudy().getStudyId() == userRole.getStudyId() : false)) {
                return true;
            }
        }
        return false;
    }

    public void validateStudyAndRoles(String studyOid, String siteOid, UserAccountBean userAccountBean) {

        ArrayList<StudyUserRoleBean> userRoles = userAccountBean.getRoles();
        if (studyOid != null)
            studyOid = studyOid.toUpperCase();
        if (siteOid != null)
            siteOid = siteOid.toUpperCase();


        if (!isStudyOidValid(studyOid)) {
            throw new OpenClinicaSystemException(ErrorConstants.ERR_STUDY_NOT_EXIST);
        }
        if (!isStudyOidValidStudyLevelOid(studyOid)) {
            throw new OpenClinicaSystemException(ErrorConstants.ERR_STUDY_NOT_Valid_OID);
        }
        if (!isSiteOidValid(siteOid)) {
            throw new OpenClinicaSystemException(ErrorConstants.ERR_SITE_NOT_EXIST);
        }
        if (!isSiteOidValidSiteLevelOid(siteOid)) {
            throw new OpenClinicaSystemException(ErrorConstants.ERR_SITE_NOT_Valid_OID);
        }
        if (!isStudyAvailable(studyOid)) {
            throw new OpenClinicaSystemException(ErrorConstants.ERR_STUDY_NOT_AVAILABLE);
        }
        if (siteOid != null && !isStudyAvailable(siteOid)) {
            throw new OpenClinicaSystemException(ErrorConstants.ERR_SITE_NOT_AVAILABLE);
        }
        if (!isStudyToSiteRelationValid(studyOid, siteOid)) {
            throw new OpenClinicaSystemException(ErrorConstants.ERR_STUDY_TO_SITE_NOT_Valid_OID);
        }

        if (!isUserHasAccessToStudy(userRoles, studyOid) && !isUserHasAccessToSite(userRoles, siteOid)) {
            throw new OpenClinicaSystemException(ErrorConstants.ERR_NO_ROLE_SETUP);
        } else if (!isUserHas_CRC_INV_DM_DEP_DS_RoleInSite(userRoles, siteOid)) {
            throw new OpenClinicaSystemException(ErrorConstants.ERR_NO_SUFFICIENT_PRIVILEGES);
        }

    }
    
    /**
     *  this method is used when get/extract participant information
     *  
     * @param studyOid
     * @param siteOid
     * @param userAccountBean
     * @param includeAccessCode
     */
    public void validateStudyAndRolesForRead(String studyOid, String siteOid, UserAccountBean userAccountBean,boolean includePII) {

    	Study tenantStudy = getTenantStudy(studyOid);
        ArrayList<StudyUserRoleBean> userRoles = userAccountBean.getRoles();      

        if (!isStudyOidValid(studyOid)) {
            throw new OpenClinicaSystemException(ErrorConstants.ERR_STUDY_NOT_EXIST);
        }
        if (!isStudyOidValidStudyLevelOid(studyOid)) {
            throw new OpenClinicaSystemException(ErrorConstants.ERR_STUDY_NOT_Valid_OID);
        }
        if (!isSiteOidValid(siteOid)) {
            throw new OpenClinicaSystemException(ErrorConstants.ERR_SITE_NOT_EXIST);
        }
        if (!isSiteOidValidSiteLevelOid(siteOid)) {
            throw new OpenClinicaSystemException(ErrorConstants.ERR_SITE_NOT_Valid_OID);
        }
       
        if (!isStudyToSiteRelationValid(studyOid, siteOid)) {
            throw new OpenClinicaSystemException(ErrorConstants.ERR_STUDY_TO_SITE_NOT_Valid_OID);
        }

        if (!isUserHasAccessToStudy(userRoles, studyOid) && !isUserHasAccessToSite(userRoles, siteOid)) {
            throw new OpenClinicaSystemException(ErrorConstants.ERR_NO_ROLE_SETUP);
        } else {
        	if(includePII) {
        		if (!isUserHas_CRC_INV_RoleInSite(userRoles, siteOid)) {
                    throw new OpenClinicaSystemException(ErrorConstants.ERR_NO_SUFFICIENT_PRIVILEGES);
        		}
            }else {
            	if (!isUserHas_CRC_INV_DM_DEP_DS_RoleInSite(userRoles, siteOid)) {
    	            throw new OpenClinicaSystemException(ErrorConstants.ERR_NO_SUFFICIENT_PRIVILEGES);
    	        }
            }           	        	
        }
        
        if (!isParticipateActive(tenantStudy)) {
            throw new OpenClinicaSystemException(ErrorConstants.ERR_PARTICIPATE_INACTIVE);
        }

    }

    private Study getTenantStudy(String studyOid) {
        return studyDao.findByOcOID(studyOid);
    }
    
    public ParameterizedErrorVM getResponseForException(OpenClinicaSystemException e, String studyOid, String siteOid) {
        String errorMsg = e.getErrorCode();
        HashMap<String, String> map = new HashMap<>();
        map.put("studyOid", studyOid);
        map.put("siteOid", siteOid);
      return  new ParameterizedErrorVM(errorMsg, map);
    }


}



