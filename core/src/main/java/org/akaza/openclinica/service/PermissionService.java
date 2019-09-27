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

    List<String> getPermissionTagsList(StudyBean study,HttpServletRequest request);

    String getPermissionTagsString(StudyBean study,HttpServletRequest request);

    String[] getPermissionTagsStringArray(StudyBean study ,HttpServletRequest request);

    boolean hasFormAccess(EventCrf ec, Integer formLayoutId, Integer studyEventId, HttpServletRequest request);

    String getAccessToken();

    boolean isUserHasPermission(String column,HttpServletRequest request,StudyBean studyBean);

    }