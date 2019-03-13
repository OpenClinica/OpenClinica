package org.akaza.openclinica.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.ParticipantDTO;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.controller.dto.AuditLogEventDTO;
import org.akaza.openclinica.controller.dto.ModuleConfigAttributeDTO;
import org.akaza.openclinica.controller.dto.ModuleConfigDTO;
import org.akaza.openclinica.controller.helper.RestfulServiceHelper;
import org.akaza.openclinica.core.EmailEngine;
import org.akaza.openclinica.core.OpenClinicaMailSender;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.*;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.*;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.web.rest.client.auth.impl.KeycloakClientImpl;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This Service class is used with View Study Subject Page
 *
 * @author joekeremian
 */

@Service( "validateService" )
public class ValidateServiceImpl implements ValidateService {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    @Qualifier( "dataSource" )
    private BasicDataSource dataSource;

    @Autowired
    StudyDao studyDao;


    public boolean isStudyOidValid(String studyOid) {
        Study publicStudy = getPublicStudy(studyOid);
        if (publicStudy != null) {
            return true;
        }
        return false;
    }

    public boolean isStudyOidValidStudyLevelOid(String studyOid) {
        Study publicStudy = getPublicStudy(studyOid);
        if (publicStudy.getStudy() == null) {
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
        if (publicSite.getStudy() != null) {
            return true;
        }
        return false;
    }


    public boolean isStudyToSiteRelationValid(String studyOid, String siteOid) {
        Study publicStudy = getPublicStudy(studyOid);
        Study publicSite = getPublicStudy(siteOid);
        if (publicSite.getStudy().getStudyId() == publicStudy.getStudyId()) {
            return true;
        }
        return false;
    }


    public boolean isUserHasCrcOrInvestigaterRole(List<StudyUserRoleBean> userRoles) {
        for (StudyUserRoleBean userRole : userRoles) {
            if (userRole.getRole().equals(Role.RESEARCHASSISTANT) || userRole.getRole().equals(Role.INVESTIGATOR)) {
                return true;
            }
        }
        return false;
    }

    public boolean isUserRoleHasAccessToSite(ArrayList<StudyUserRoleBean> userRoles, String siteOid) {
        Study publicSite = getPublicStudy(siteOid);
        for (StudyUserRoleBean userRole : userRoles) {
            if (userRole.getStudyId() == publicSite.getStudyId())
                return true;
        }
        return false;
    }


    public boolean isParticipateActive(Study tenantStudy) {
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(dataSource);
        String participateFormStatus = spvdao.findByHandleAndStudy(tenantStudy.getStudy() != null ? tenantStudy.getStudy().getStudyId() : tenantStudy.getStudyId(), "participantPortal").getValue();
        if (participateFormStatus.equals("enabled"))
            return true;
        return false;
    }


    private Study getPublicStudy(String studyOid) {
        return studyDao.findPublicStudy(studyOid);
    }

}