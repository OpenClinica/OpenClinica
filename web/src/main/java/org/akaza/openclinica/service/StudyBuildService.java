package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.controller.dto.ModuleConfigAttributeDTO;
import org.akaza.openclinica.controller.dto.ModuleConfigDTO;
import org.akaza.openclinica.controller.helper.StudyInfoObject;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.user.UserAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

/**
 * Created by yogi on 11/10/16.
 */
public interface StudyBuildService {
    Logger logger = LoggerFactory.getLogger(StudyBuildService.class);

    StudyInfoObject process(HttpServletRequest request, Study study, UserAccountBean ub) throws Exception;

    boolean saveStudyEnvRoles(HttpServletRequest request, UserAccountBean ub) throws Exception;

    ResponseEntity<List<StudyEnvironmentRoleDTO>> getUserRoles(HttpServletRequest request);

    ResponseEntity getUserDetails(HttpServletRequest request);

    void updateStudyUsername(UserAccountBean ub, KeycloakUser user);

    boolean updateStudyUserRoles(HttpServletRequest request, UserAccount ub, int userActiveStudyId, String altStudyEnvUuid);

    UserAccount getUserAccountObject(UserAccountBean ubIn);

    void updateParticipateModuleStatusInOC(HttpServletRequest request, String studyOid);

    List<ModuleConfigDTO> getParticipateModuleFromStudyService(HttpServletRequest request, Study study);

    ModuleConfigDTO getModuleConfig(List<ModuleConfigDTO> moduleConfigDTOs, Study study);

    ModuleConfigAttributeDTO getModuleConfigAttribute(Set<ModuleConfigAttributeDTO> moduleConfigAttributeDTOs, Study study);

    }