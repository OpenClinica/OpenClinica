package core.org.akaza.openclinica.service;

import core.org.akaza.openclinica.domain.datamap.EventCrf;
import core.org.akaza.openclinica.domain.datamap.EventDefinitionCrf;

import core.org.akaza.openclinica.domain.datamap.Study;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface PermissionService {
    List<String> getPermissionTagsList(HttpServletRequest request);

    String getPermissionTagsString(HttpServletRequest request);

    String[] getPermissionTagsStringArray(HttpServletRequest request);

    ResponseEntity<List<StudyEnvironmentRoleDTO>> getUserRoles(HttpServletRequest request);

    List<String> getPermissionTagsList(Study study,HttpServletRequest request);

    String getPermissionTagsString(Study study,HttpServletRequest request);

    String[] getPermissionTagsStringArray(Study study ,HttpServletRequest request);

    boolean hasFormAccess(EventCrf ec, Integer formLayoutId, Integer studyEventId, HttpServletRequest request);
    
    boolean hasFormAccess(EventDefinitionCrf edc,List<String> permissionTagsList);

    boolean isUserHasPermission(String column, HttpServletRequest request, Study studyBean);

}