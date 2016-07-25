package org.akaza.openclinica.service.pmanage;

import java.util.Date;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.core.EmailEngine;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.client.RestTemplate;

public class RandomizationRegistrar {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    public static final String AVAILABLE = "available";
    public static final String UNAVAILABLE = "unavailable";
    public static final String INVALID = "invalid";
    public static final String UNKNOWN = "unknown";
    public static final int RANDOMIZATION_READ_TIMEOUT = 5000;
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
        String randomizationUrl = CoreResources.getField("moduleManager") + "/app/rest/oc/se_randomizations?studyoid=" + studyOid + "&instanceurl=" + ocUrl;
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

        requestFactory.setReadTimeout(RANDOMIZATION_READ_TIMEOUT);
        RestTemplate rest = new RestTemplate(requestFactory);

        try {
            SeRandomizationDTO response = rest.getForObject(randomizationUrl, SeRandomizationDTO.class);
            if (response.getStudyOid() != null) {
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

    public SeRandomizationDTO getCachedRandomizationDTOObject(String studyOid, Boolean resetCache) throws Exception {
        SeRandomizationDTO seRandomizationDTO = null; // check if exist in cache ;
        String ocUrl = CoreResources.getField("sysURL.base");
        String mapKey = ocUrl + studyOid;
        Element element = cache.get(mapKey);
        if (element != null && element.getObjectValue() != null && !resetCache) {
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
        } catch (MessagingException me) {
            logger.error("Email could not be sent");
        }
    }

        public String randomizeStudy(String studyOid, String studyName,UserAccountBean userAccount) {
            
            String ocUrl = CoreResources.getField("sysURL.base") + "rest2/openrosa/" + studyOid;
            String randomizationUrl = CoreResources.getField("moduleManager") + "/app/rest/oc/se_randomizations";
            SeRandomizationDTO seRandomizationDTO = new SeRandomizationDTO();
            seRandomizationDTO.setStudyOid(studyOid);
            seRandomizationDTO.setInstanceUrl(ocUrl);
            seRandomizationDTO.setOcUser_username(userAccount.getName());
            seRandomizationDTO.setOcUser_name(userAccount.getFirstName());
            seRandomizationDTO.setOcUser_lastname(userAccount.getLastName());
            seRandomizationDTO.setOcUser_emailAddress(userAccount.getEmail());
            seRandomizationDTO.setStudyName(studyName);
            seRandomizationDTO.setOpenClinicaVersion(CoreResources.getField("OpenClinica.version"));

            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
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
