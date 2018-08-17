package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.domain.datamap.EventCrf;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface PermissionService {
    List<String> getPermissionTagsList(HttpServletRequest request);

    String getPermissionTagsString(HttpServletRequest request);

    String[] getPermissionTagsStringArray(HttpServletRequest request);

    ResponseEntity<List<StudyEnvironmentRoleDTO>> getUserRoles(HttpServletRequest request);

    List<String> getPermissionTagsListWithoutRequest(StudyBean study, String userUuid);

    String getPermissionTagsStringWithoutRequest(StudyBean study, String userUuid);

    String[] getPermissionTagsStringArrayWithoutRequest(StudyBean study, String userUuid);

    ResponseEntity<List<StudyEnvironmentRoleDTO>> getUserRolesWithoutRequest(String userUuid);

    boolean hasFormAccess(EventCrf ec, Integer formLayoutId, Integer studyEventId, HttpServletRequest request);

    String getAccessToken();
}