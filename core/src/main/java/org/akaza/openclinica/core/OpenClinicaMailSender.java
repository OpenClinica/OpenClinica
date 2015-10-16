package org.akaza.openclinica.core;

import java.io.File;
import java.net.URL;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.akaza.openclinica.bean.service.EmailTemplateDTO;
import org.springframework.core.io.Resource;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring3.SpringTemplateEngine;

public class OpenClinicaMailSender {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    JavaMailSenderImpl mailSender;

    private SpringTemplateEngine templateEngine;

    public void sendEmail(String to, String subject, String body, Boolean htmlEmail) throws OpenClinicaSystemException {
        sendEmail(to, EmailEngine.getAdminEmail(), subject, body, htmlEmail);
    }

    public void sendEmail(String to, String from, String subject, String body, Boolean htmlEmail) throws OpenClinicaSystemException {
        try {

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, htmlEmail);
            helper.setFrom(from);
            helper.setTo(processMultipleImailAddresses(to.trim()));
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(mimeMessage);
            logger.debug("Email sent successfully on {}", new Date());
        } catch (MailException me) {
            logger.debug("Email could not be sent on {} due to: {}", new Date(), me.toString());
            throw new OpenClinicaSystemException(me.getMessage());
        } catch (MessagingException e) {
            logger.debug("Email could not be sent on {} due to: {}", new Date(), e.toString());
            throw new OpenClinicaSystemException(e.getMessage());
        }
    }

    public void sendEmail(String to, String subject, EmailTemplateDTO emailContext) throws OpenClinicaSystemException {
        sendEmail(to, EmailEngine.getAdminEmail(), subject, emailContext);
    }

    public void sendEmail(String to, String from, String subject, EmailTemplateDTO emailContext) throws OpenClinicaSystemException {
        String baseUrl = EmailEngine.getBaseUrl();
        emailContext.setBaseUrl(baseUrl);
        Context context = emailContext.getContext();

        Boolean isHtml = !emailContext.getBody().isEmpty();
        String htmlContent = isHtml ? templateEngine.process("emailTemplate", context) : null;
        String plainContent = emailContext.getPlaintextBody();

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, isHtml);
            helper.setFrom(from);
            helper.setTo(processMultipleImailAddresses(to.trim()));
            helper.setSubject(subject);
            if (plainContent != null && htmlContent != null) {
                helper.setText(plainContent, htmlContent);
            } else if (plainContent != null) {
                helper.setText(plainContent);
            } else {
                helper.setText(htmlContent, true);
            }

            // Append inline images
            ClassLoader classLoader = getClass().getClassLoader();
            URL resource;

            // Needed by emailTemplate.html
            resource = classLoader.getResource("templates/email/images/blockquote-bottomright.png");
            if (resource != null) {
                helper.addInline("background-blockquote-bottomright", new File(resource.getFile()));
            }
            resource = classLoader.getResource("templates/email/images/blockquote-bottomright@2x.png");
            if (resource != null) {
                helper.addInline("background-blockquote-bottomright_2x", new File(resource.getFile()));
            }
            resource = classLoader.getResource("templates/email/images/blockquote-topleft.png");
            if (resource != null) {
                helper.addInline("background-blockquote-topleft", new File(resource.getFile()));
            }
            resource = classLoader.getResource("templates/email/images/blockquote-topleft@2x.png");
            if (resource != null) {
               helper.addInline("background-blockquote-topleft_2x", new File(resource.getFile()));
            }

            // User images
            for (HashMap<String, String> img: emailContext.getInlineImages()) {
                helper.addInline(img.get("id"), new File(img.get("filepath")));
            }

            mailSender.send(mimeMessage);
            logger.debug("Email sent successfully on {}", new Date());
        } catch (MailException me) {
            logger.debug("Email could not be sent on {} due to: {}", new Date(), me.toString());
            throw new OpenClinicaSystemException(me.getMessage());
        } catch (MessagingException e) {
            logger.debug("Email could not be sent on {} due to: {}", new Date(), e.toString());
            throw new OpenClinicaSystemException(e.getMessage());
        }
    }

    public String renderTemplate(String templateName, HashMap context, Locale locale) {
        if (locale == null) {
            locale = Locale.ENGLISH;
        }
        if (!context.containsKey("baseUrl")) {
            context.put("baseUrl", EmailEngine.getBaseUrl());
        }
        Context templateContext = new Context(locale, context);
        return templateEngine.process(templateName, templateContext);
    }

    private InternetAddress[] processMultipleImailAddresses(String to) throws MessagingException {
        ArrayList<String> recipientsArray = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(to, ",");
        while (st.hasMoreTokens()) {
            recipientsArray.add(st.nextToken());
        }

        int sizeTo = recipientsArray.size();
        InternetAddress[] addressTo = new InternetAddress[sizeTo];
        for (int i = 0; i < sizeTo; i++) {
            addressTo[i] = new InternetAddress(recipientsArray.get(i).toString());
        }
        return addressTo;
    }

    public JavaMailSenderImpl getMailSender() {
        return mailSender;
    }

    public void setMailSender(JavaMailSenderImpl mailSender) {
        this.mailSender = mailSender;
    }

    public void setTemplateEngine(SpringTemplateEngine value) {
        this.templateEngine = value;
    }
}
