package org.akaza.openclinica.bean.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.thymeleaf.context.Context;

/**
 * To be used as context to render emailTemplate.html with thymeleaf-spring.
 *
 * You can add images as data uri (set resourceType to "datauri" in setImage()),
 * or inline attachment, or external url (set resourceType to "external" in setImage()).
 *
 * For images as data-uri or external-url, the value will be available in template variable
 * as `images` HashMap.
 *
 * For images as inline-attachment, you need to set `src` attribute of <img/> (in email content)
 * as "cid:" + imageId (cid refers to content-id of attachment).
 */
public class EmailTemplateDTO {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private String baseUrl;
    private String subject;
    private String logoUrl;
    private String title;
    private final List<HashMap<String, String>> body;
    private String plaintextBody;
    private HashMap<String, String> action;
    private HashMap<String, String> closing;
    private Locale locale;
    private final HashMap<String, String> images;

    public EmailTemplateDTO() {
        this.body = new ArrayList<>();
        this.images = new HashMap<>();
    }

    private String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String value) {
        this.baseUrl = value;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String value) {
        this.subject = value;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String value) {
        this.logoUrl = value;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String value) {
        this.title = value;
    }

    public List<HashMap<String, String>> getBody() {
        return body;
    }

    public void addBody(String heading, String content) {
        HashMap<String, String> value = new HashMap<>();
        value.put("heading", heading);
        value.put("content", content);
        body.add(value);
    }

    public String getPlaintextBody() {
        return plaintextBody;
    }

    public void setPlaintextBody(String value) {
        this.plaintextBody = value;
    }

    public HashMap<String, String> getClosing() {
        return closing;
    }

    public void setClosingGreeting(String value) {
        if (closing == null) {
            this.closing = new HashMap<>();
        }
        closing.put("greeting", value);
    }

    public void setClosingName(String value) {
        if (closing == null) {
            this.closing = new HashMap<>();
        }
        closing.put("name", value);
    }

    public void setClosingImageUrl(String value) {
        if (closing == null) {
            this.closing = new HashMap<>();
        }
        closing.put("imageUrl", value);
    }

    public HashMap<String, String> getAction() {
        return action;
    }

    public void setActionUrl(String value) {
        if (action == null) {
            this.action = new HashMap<>();
        }
        action.put("url", value);
    }

    public void setActionText(String value) {
        if (action == null) {
            this.action = new HashMap<>();
        }
        action.put("text", value);
    }

    public Locale getLocale() {
        return locale != null ? locale : Locale.ENGLISH;
    }

    public void setLocale(Locale value) {
        this.locale = value;
    }

    public HashMap<String, String> getImages() {
        return images;
    }

    public void setImage(String imageId, String imagePath) {
        setImage(imageId, imagePath, "embed");
    }

    public void setImage(String imageId, String imagePath, String resourceType) {
        switch (resourceType) {
            case "datauri":
                try {
                    images.put(imageId, EmailTemplateDTO.createDataUri(imagePath));
                } catch (IOException e) {
                    logger.error("Failed to embed image {} as datauri in emails", imagePath);
                };
                break;
            case "external":
                // If not embedded than assume external url.
                images.put(imageId, getBaseUrl() + imagePath);
                break;
            default:
                // To be processed later by MimeMessageHelper.
                images.put(imageId, imagePath);
                break;
        }
    }

    public HashMap<String, ?> getVariables() {
        HashMap result = new HashMap<>();
        result.put("baseUrl", baseUrl);
        result.put("subject", subject);
        result.put("logoUrl", logoUrl);
        result.put("title", title);
        result.put("body", body);
        result.put("textBody", plaintextBody);
        result.put("action", action);
        result.put("closing", closing);

        // If you embedded image as data-uri, or if the image is externally hosted uri,
        // they will be available in the email template.
        // For embedded image as inline-attachment, you need to set the image's src attribute.
        HashMap<String, String> dataUriOrExternal = new HashMap<>();
        for (Entry<String, String> entry : images.entrySet()) {
            if (entry.getValue().matches("^(data|https?):.*")) {
                dataUriOrExternal.put(entry.getKey(), entry.getValue());
            }
        }
        result.put("images", dataUriOrExternal);
        return result;
    }

    public Context getContext() {
        Context result = new Context(getLocale());
        result.setVariables(getVariables());
        return result;
    }

    public static String createDataUri(String imagePath) throws IOException {
        InputStream is;
        if (imagePath.startsWith("classpath:")) {
            is = new ClassPathResource(imagePath.substring(10)).getInputStream();
        } else {
            is = new FileInputStream(imagePath);
        }
        return "data:" + URLConnection.guessContentTypeFromName(imagePath) + ";base64," +
                DatatypeConverter.printBase64Binary(IOUtils.toByteArray(is));
    }
}
