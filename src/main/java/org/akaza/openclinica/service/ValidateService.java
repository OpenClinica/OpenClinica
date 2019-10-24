/**
 *
 */
package org.akaza.openclinica.service;

import core.org.akaza.openclinica.bean.login.StudyUserRoleBean;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.user.UserAccount;
import core.org.akaza.openclinica.exception.OpenClinicaSystemException;
import core.org.akaza.openclinica.service.rest.errors.ParameterizedErrorVM;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * @author joekeremian
 *
 */
public interface ValidateService {

    boolean isStudyAvailable(String studyOid);

    boolean isStudyOidValid(String studyOid);

    boolean isStudyOidValidStudyLevelOid(String studyOid);

    boolean isSiteOidValid(String siteOid);

    boolean isSiteOidValidSiteLevelOid(String siteOid);

    boolean isStudyToSiteRelationValid(String studyOid, String siteOid);

    boolean isUserHas_CRC_INV_RoleInSite(List<StudyUserRoleBean> userRoles, String siteOid);

    boolean isUserHasTechAdminRole(UserAccount userAccount);

    boolean isParticipateActive(Study tenantStudy);

    boolean isAdvanceSearchEnabled(Study tenantStudy);

    boolean isUserHasAccessToStudy(List<StudyUserRoleBean> userRoles, String studyOid);

    boolean isUserHasAccessToSite(List<StudyUserRoleBean> userRoles, String siteOid);

    boolean isUserSystemUser(HttpServletRequest request);

    boolean isUserHas_CRC_INV_DM_DEP_DS_RoleInSite(List<StudyUserRoleBean> userRoles, String siteOid);

    boolean isUserHas_DM_DEP_DS_RoleInStudy(List<StudyUserRoleBean> userRoles, String studyOid);

     void validateStudyAndRoles(String studyOid, String siteOid, UserAccountBean userAccountBean);
     void validateStudyAndRolesForRead(String studyOid, String siteOid, UserAccountBean userAccountBean,boolean includePII);

    ParameterizedErrorVM getResponseForException(OpenClinicaSystemException e, String studyOid, String siteOid);


    }