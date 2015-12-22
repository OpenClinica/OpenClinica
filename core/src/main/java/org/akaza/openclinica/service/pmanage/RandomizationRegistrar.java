package org.akaza.openclinica.service.pmanage;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpSession;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.akaza.openclinica.bean.login.ParticipantDTO;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.core.EmailEngine;
import org.akaza.openclinica.dao.cache.EhCacheWrapper;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.rule.action.RuleActionBean;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.cdisc.ns.odm.v130.ODMcomplexTypeDefinitionGlobalVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.CommonsClientHttpRequestFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.client.RestTemplate;

public class RandomizationRegistrar {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    public static final String AVAILABLE = "available";
    public static final String UNAVAILABLE = "unavailable";
    public static final String INVALID = "invalid";
    public static final String UNKNOWN = "unknown";
    public static final int RANDOMIZATION_READ_TIMEOUT = 10000;
    private static final String CACHE_KEY = "randomizeObject";
    private CacheManager cacheManager;
    private net.sf.ehcache.Cache cache;

    public RandomizationRegistrar() {
        cacheManager = CacheManager.getInstance();
        this.cache = cacheManager.getCache(CACHE_KEY);

    }

    // Rest Call to OCUI to get Randomization
    public SeRandomizationDTO getRandomizationDTOObject(String studyOid) {
        String ocUrl = CoreResources.getField("sysURL.base") + "rest2/openrosa/" + studyOid;
        String randomizationUrl = CoreResources.getField("configServerUrl") + "/app/rest/oc/se_randomizations?studyoid=" + studyOid + "&instanceurl=" + ocUrl;
        CommonsClientHttpRequestFactory requestFactory = new CommonsClientHttpRequestFactory();

        requestFactory.setReadTimeout(RANDOMIZATION_READ_TIMEOUT);
        RestTemplate rest = new RestTemplate(requestFactory);

        try {
            SeRandomizationDTO response = rest.getForObject(randomizationUrl, SeRandomizationDTO.class);
            if (response != null) {
                return response;
            } else {
                return null;
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
            System.out.println(e.getMessage());
            System.out.println(ExceptionUtils.getStackTrace(e));

        }
        return null;
    }

    public SeRandomizationDTO getCachedRandomizationDTOObject(String studyOid) throws Exception {
        SeRandomizationDTO seRandomizationDTO = null; // check if exist in cache ;
        String ocUrl = CoreResources.getField("sysURL.base");
        String mapKey = ocUrl + studyOid;
        Element element = cache.get(mapKey);
        if (element != null && element.getObjectValue() != null) {
            seRandomizationDTO = (SeRandomizationDTO) element.getObjectValue();
        }

        if (seRandomizationDTO == null) {
            seRandomizationDTO = getRandomizationDTOObject(studyOid);
        }
        if (seRandomizationDTO != null) {
            cache.put(new Element(mapKey, seRandomizationDTO));
        }
        return seRandomizationDTO;
    }

    public String randomizeStudy(String studyOid) {
        return randomizeStudy(studyOid, null);
    }

    public void sendEmail(JavaMailSenderImpl mailSender, UserAccountBean user, String emailSubject, String message) throws OpenClinicaSystemException {

        logger.info("Sending email...");
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
            helper.setFrom(EmailEngine.getAdminEmail());
            helper.setTo(user.getEmail());
            helper.setSubject(emailSubject);
            helper.setText(message);

            mailSender.send(mimeMessage);
            logger.debug("Email sent successfully on {}", new Date());
        } catch (MailException me) {
            logger.error("Email could not be sent");
            throw new OpenClinicaSystemException(me.getMessage());
        } catch (MessagingException me) {
            logger.error("Email could not be sent");
            throw new OpenClinicaSystemException(me.getMessage());
        }
    }

    public String randomizeStudy(String studyOid, String studyName) {

        String ocUrl = CoreResources.getField("sysURL.base") + "rest2/openrosa/" + studyOid;
        String randomizationUrl = CoreResources.getField("configServerUrl") + "/app/rest/oc/se_randomizations";
        SeRandomizationDTO seRandomizationDTO = new SeRandomizationDTO();
        seRandomizationDTO.setStudyOid(studyOid);
        seRandomizationDTO.setInstanceUrl(ocUrl);

        CommonsClientHttpRequestFactory requestFactory = new CommonsClientHttpRequestFactory();
        requestFactory.setReadTimeout(RANDOMIZATION_READ_TIMEOUT);
        RestTemplate rest = new RestTemplate(requestFactory);

        try {
            SeRandomizationDTO response = rest.postForObject(randomizationUrl, seRandomizationDTO, SeRandomizationDTO.class);
            if (response != null && response.getStatus() != null)
                return response.getStatus();

        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
            System.out.println(e.getMessage());
            System.out.println(ExceptionUtils.getStackTrace(e));

        }
        return "";
    }

}
