package org.akaza.openclinica.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.UserType;
import org.akaza.openclinica.bean.login.ParticipantDTO;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
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
    public static final String ENABLED = "enabled";
    public static final String DISABLED = "enabled";

    private static String sbsUrl = CoreResources.getField("SBSUrl");
    private static final String ADVANCE_SEARCH = "contactsModule";

    @Autowired
    @Qualifier( "dataSource" )
    private BasicDataSource dataSource;

    @Autowired
    StudyDao studyDao;

    @Autowired
    StudyParameterValueDao studyParameterValueDao;

    public boolean isStudyOidValid(String studyOid) {
        Study publicStudy = getPublicStudy(studyOid);
        if (publicStudy != null) {
            return true;
        }
        return false;
    }

    public boolean isStudyOidValidStudyLevelOid(String studyOid) {
        Study publicStudy = getPublicStudy(studyOid);
        if (publicStudy!=null && publicStudy.getStudy() == null) {
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
        if (publicSite!=null && publicSite.getStudy() != null) {
            return true;
        }
        return false;
    }


    public boolean isStudyToSiteRelationValid(String studyOid, String siteOid) {
        Study publicStudy = getPublicStudy(studyOid);
        Study publicSite = getPublicStudy(siteOid);
        if (publicStudy!=null && publicSite!=null && publicSite.getStudy().getStudyId() == publicStudy.getStudyId()) {
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
    public boolean isUserHasTechAdminRole(UserAccount userAccount){
        if(userAccount.getUserType().getUserTypeId()== UserType.TECHADMIN.getId())
            return true;
        return false;
    }


    public boolean isUserRoleHasAccessToSite(ArrayList<StudyUserRoleBean> userRoles, String siteOid) {
        Study publicSite = getPublicStudy(siteOid);
        if(publicSite==null)
            return false;
        for (StudyUserRoleBean userRole : userRoles) {
            if ((userRole.getStudyId() == publicSite.getStudyId()) || (userRole.getStudyId()==publicSite.getStudy().getStudyId()))
                return true;
        }
        return false;
    }


    public boolean isParticipateActive(Study tenantStudy) {
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(dataSource);
        String participateFormStatus = spvdao.findByHandleAndStudy(tenantStudy.getStudy() != null ? tenantStudy.getStudy().getStudyId() : tenantStudy.getStudyId(), "participantPortal").getValue();
        if (participateFormStatus.equals(ENABLED))
            return true;
        return false;
    }

    public boolean isAdvanceSearchEnabled(Study tenantStudy) {
        StudyParameterValue spv = studyParameterValueDao.findByStudyIdParameter(tenantStudy.getStudy()==null?tenantStudy.getStudyId():tenantStudy.getStudy().getStudyId() , ADVANCE_SEARCH);

        if (spv != null && spv.getValue().equals(ENABLED))
            return true;


        return false;
    }

    private Study getPublicStudy(String studyOid) {
        return studyDao.findPublicStudy(studyOid);
    }





}