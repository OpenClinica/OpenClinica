package core.org.akaza.openclinica.service;

import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.service.modules.ModuleProcessor;
import org.akaza.openclinica.controller.dto.ModuleConfigAttributeDTO;
import org.akaza.openclinica.controller.dto.ModuleConfigDTO;
import org.akaza.openclinica.controller.helper.StudyInfoObject;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.user.UserAccount;
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

    boolean saveStudyEnvRoles(HttpServletRequest request, UserAccountBean ub, boolean isLogin) throws Exception;

    ResponseEntity<List<StudyEnvironmentRoleDTO>> getUserRoles(HttpServletRequest request, boolean isLogin);

    ResponseEntity getUserDetails(HttpServletRequest request);

    void updateStudyUsername(UserAccountBean ub, KeycloakUser user);

    boolean updateStudyUserRoles(HttpServletRequest request, UserAccount ub, int userActiveStudyId, String altStudyEnvUuid, boolean isLogin);

    UserAccount getUserAccountObject(UserAccountBean ubIn);

    List<ModuleConfigDTO> getModuleConfigsFromStudyService(String accessToken, Study study);

    ModuleConfigDTO getModuleConfig(List<ModuleConfigDTO> moduleConfigDTOs, Study study, ModuleProcessor.Modules module);

    ModuleConfigAttributeDTO getModuleConfigAttribute(Set<ModuleConfigAttributeDTO> moduleConfigAttributeDTOs, Study study);

    void processSingleModule(Study study, List<ModuleConfigDTO> moduleConfigDTOs, ModuleProcessor.Modules module);

    void processModules(List<ModuleConfigDTO> moduleConfigDTOs, Study study);

    Study getPublicStudy(String ocId);

    Study getParentPublicStudy(String ocId);

    Study getPublicStudy(int id);

    Boolean isPublicStudySameAsTenantStudy(Study tenantStudy, String publicStudyOID);

    public void setRequestSchemaByStudy(String ocId);

    public void setRequestSchemaByStudyOrParentStudy(String ocId);

    public String getCurrentBoardUrl(String accessToken, Study study);

}
