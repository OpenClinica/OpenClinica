package org.akaza.openclinica.service;

import liquibase.util.StringUtils;
import core.org.akaza.openclinica.bean.core.ApplicationConstants;
import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.bean.core.UserType;
import core.org.akaza.openclinica.bean.login.StudyUserRoleBean;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.hibernate.*;
import core.org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import core.org.akaza.openclinica.domain.Status;
import core.org.akaza.openclinica.domain.datamap.*;
import core.org.akaza.openclinica.domain.user.UserAccount;
import core.org.akaza.openclinica.exception.OpenClinicaSystemException;
import core.org.akaza.openclinica.service.auth.TokenService;
import core.org.akaza.openclinica.service.rest.errors.ParameterizedErrorVM;
import org.akaza.openclinica.web.restful.errors.ErrorConstants;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

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

    private static final String ADVANCE_SEARCH = "contactsModule";

    @Autowired
    @Qualifier( "dataSource" )
    private BasicDataSource dataSource;

    @Autowired
    StudyDao studyDao;

    @Autowired
    StudyParameterValueDao studyParameterValueDao;

    @Autowired
    EventDefinitionCrfPermissionTagDao eventDefinitionCrfPermissionTagDao;



    @Autowired
    private TokenService tokenService;


    public boolean isStudyOidValid(String studyOid) {
        Study publicStudy = getPublicStudy(studyOid);
        if (publicStudy != null) {
            return true;
        }
        return false;
    }


    public boolean isStudyAvailable(String studyOid) {
        Study publicStudy = getPublicStudy(studyOid);
        if (publicStudy != null && publicStudy.getStatus().equals(Status.AVAILABLE) && !(publicStudy.isSite())) {
            return true;
        }
        return false;
    }

    
    public boolean isSiteAvailable(String siteOid) {
        Study publicStudy = getPublicStudy(siteOid);
        if (publicStudy != null && publicStudy.getStatus().equals(Status.AVAILABLE) && publicStudy.isSite()) {
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
                    || (userRole.getRole().equals(Role.RESEARCHASSISTANT) && publicSite.getStudy().getStudyId() == userRole.getStudyId())
                    || (userRole.getRole().equals(Role.INVESTIGATOR) && publicSite.getStudy().getStudyId() == userRole.getStudyId())
                    || (userRole.getRole().equals(Role.COORDINATOR) && publicSite.getStudy().getStudyId() == userRole.getStudyId()))
                return true;
        }
        return false;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean isUserHas_CRC_INV_DM_DEP_DS_SM_RoleInSite(List<StudyUserRoleBean> userRoles, String siteOid) {
        Study publicSite = getPublicStudy(siteOid);
        for (StudyUserRoleBean userRole : userRoles) {
            if ((userRole.getRole().equals(Role.RESEARCHASSISTANT) && publicSite.getStudyId() == userRole.getStudyId())
                    || (userRole.getRole().equals(Role.INVESTIGATOR) && publicSite.getStudyId() == userRole.getStudyId())
                    || (userRole.getRole().equals(Role.RESEARCHASSISTANT) && publicSite.getStudy().getStudyId() == userRole.getStudyId())
                    || (userRole.getRole().equals(Role.INVESTIGATOR) && publicSite.getStudy().getStudyId() == userRole.getStudyId())
                    || (userRole.getRole().equals(Role.COORDINATOR) && publicSite.getStudy().getStudyId() == userRole.getStudyId())
                    || (userRole.getRole().equals(Role.MONITOR) && publicSite.getStudy().getStudyId() == userRole.getStudyId()))
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

    public boolean isUserSystemUser(HttpServletRequest request){
        boolean skipRoleCheck = false;
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && !authHeader.isEmpty()) {
            StringTokenizer st = new StringTokenizer(authHeader);
            if (st.hasMoreTokens()) {
                String basic = st.nextToken();
                if (basic.equalsIgnoreCase("Bearer")) {
                    String accessToken = st.nextToken();
                    final Map<String, Object> decodedToken = tokenService.decodeAndVerify(accessToken);
                    if (accessToken != null && !accessToken.isEmpty()) {
                        LinkedHashMap<String, Object> userContextMap = (LinkedHashMap<String, Object>) decodedToken.get("https://www.openclinica.com/userContext");
                        String userType = (String) userContextMap.get("userType");
                        if (userType.equals(core.org.akaza.openclinica.service.UserType.SYSTEM.getName())){
                            String clientId = decodedToken.get("clientId").toString();
                            if (org.apache.commons.lang.StringUtils.equalsIgnoreCase(clientId, ApplicationConstants.RANDOMIZE_CLIENT)
                                    || org.apache.commons.lang.StringUtils.equalsIgnoreCase(clientId, ApplicationConstants.DICOM_CLIENT)){
                                skipRoleCheck = true;
                            }

                        }
                    }
                }
            }
        }
        return skipRoleCheck;
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
        if (siteOid != null && !isSiteAvailable(siteOid)) {
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
     *  this is used by case book PDF process
     *  studyOid only at study level
     */
    public void validateStudyAndRoles(String studyOid,  UserAccountBean userAccountBean) {

        ArrayList<StudyUserRoleBean> userRoles = userAccountBean.getRoles();
        if (studyOid != null)
            studyOid = studyOid.toUpperCase();

        if (!isStudyOidValid(studyOid)) {
            throw new OpenClinicaSystemException(ErrorConstants.ERR_STUDY_NOT_EXIST);
        }

        if (!isStudyAvailable(studyOid)) {
            throw new OpenClinicaSystemException(ErrorConstants.ERR_STUDY_NOT_AVAILABLE);
        }

        
        if (!isUserHasAccessToStudy(userRoles, studyOid)) {
            throw new OpenClinicaSystemException(ErrorConstants.ERR_NO_ROLE_SETUP);
        }

    }

    public boolean hasCRFpermissionTag(EventDefinitionCrf edc,List<String> permissionTags) {
        boolean formIsTagged= isFormTagged(edc);
        if(formIsTagged) {
            List<EventDefinitionCrfPermissionTag> list = eventDefinitionCrfPermissionTagDao.findByEdcIdTagId(edc.getEventDefinitionCrfId(), edc.getParentId() !=null ? edc.getParentId():0, permissionTags);

            return (list.size() > 0 ?  true:  false) ;
        }else {
            return true;
        }


    }

    private boolean isFormTagged(EventDefinitionCrf edc) {
        logger.debug("Begin to permissionTagsLookup");
        ArrayList list = (ArrayList) eventDefinitionCrfPermissionTagDao.findTagsForEDC(edc);

        return (list.size() > 0 ?  true:  false) ;
    }

    /**
     *  this method is used when get/extract participant information
     *  
     * @param studyOid
     * @param siteOid
     * @param userAccountBean
     * @param includePII
     */
    public void validateStudyAndRolesForRead(String studyOid, String siteOid, UserAccountBean userAccountBean, boolean includePII) {

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
        } else if (!isUserHas_CRC_INV_DM_DEP_DS_SM_RoleInSite(userRoles, siteOid)) {
            throw new OpenClinicaSystemException(ErrorConstants.ERR_NO_SUFFICIENT_PRIVILEGES);
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



