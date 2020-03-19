/**
 *
 */
package org.akaza.openclinica.service;

import core.org.akaza.openclinica.bean.login.StudyUserRoleBean;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.domain.datamap.EventDefinitionCrf;
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
 */
public interface ValidateService {

    boolean isStudyAvailable(String studyOid);

    boolean isStudyOidValid(String studyOid);

    boolean isStudyOidValidStudyLevelOid(String studyOid);

    boolean isSiteOidValid(String siteOid);

    boolean isSiteAvailable(String siteOid);

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

    boolean isUserHas_DM_MON_RoleInStudy(List<StudyUserRoleBean> userRoles, String studyOID);

    void validateStudyAndRoles(String studyOid, UserAccountBean userAccountBean);

    void validateStudyAndRoles(String studyOid, String siteOid, UserAccountBean userAccountBean);

    void validateStudyAndRolesForRead(String studyOid, String siteOid, UserAccountBean userAccountBean, boolean includePII);

    void validateStudyAndRolesForExtractParticipantInfo(String studyOid, String siteOid, UserAccountBean userAccountBean, boolean includePII);

    boolean hasCRFpermissionTag(EventDefinitionCrf edc, List<String> permissionTags);

    ParameterizedErrorVM getResponseForException(OpenClinicaSystemException e, String studyOid, String siteOid);

    void validateStudyAndRolesForPdfCaseBook(String studyOid, String siteOid, UserAccountBean userAccountBean);

    public void validateForSdvItemForm(String studyOid, String studyEventOid, String studySubjectLabel, String formOid, UserAccountBean userAccount, int ordinal);

    boolean isCrfPresent(String formOid);

    boolean isEventPresent(String studyEventOid);

    boolean isEventOidAndParticipantIdAreLinked(String studyEventOid, int studySubjectId, int ordinal);

    boolean isStudySubjectPresent(String studySubjectLabel, Study study);
}