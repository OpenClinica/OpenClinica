/**
 *
 */
package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.user.UserAccount;

import java.util.ArrayList;
import java.util.List;

/**
 * @author joekeremian
 *
 */
public interface ValidateService {


    boolean isStudyOidValid(String studyOid);

    boolean isStudyOidValidStudyLevelOid(String studyOid);

    boolean isSiteOidValid(String siteOid);

    boolean isSiteOidValidSiteLevelOid(String siteOid);

    boolean isStudyToSiteRelationValid(String studyOid, String siteOid);

    boolean isUserHasCRC_INV_Role_And_AccessToSite(List<StudyUserRoleBean> userRoles, String siteOid);

    boolean isUserHasCRC_INV_DM_DEP_DS_Role_And_AccessToSite(List<StudyUserRoleBean> userRoles, String siteOid);

    boolean isUserHasTechAdminRole(UserAccount userAccount);

    boolean isParticipateActive(Study tenantStudy);

    boolean isAdvanceSearchEnabled(Study tenantStudy);

}