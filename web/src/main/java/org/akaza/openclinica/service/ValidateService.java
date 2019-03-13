/**
 *
 */
package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.domain.datamap.Study;

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

    boolean isUserHasCrcOrInvestigaterRole(List<StudyUserRoleBean> userRoles);

    boolean isUserRoleHasAccessToSite(ArrayList<StudyUserRoleBean> userRoles, String siteOid);

    boolean isParticipateActive(Study tenantStudy);

}