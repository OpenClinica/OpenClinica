package org.akaza.openclinica.bean.service;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import org.thymeleaf.context.Context;

public class EmailTemplateDTO {

    private String baseUrl;
    private String subject;
    private String logoUrl;
    private String title;
    private List<HashMap<String, String>> body;
    private String plaintextBody;
    private HashMap<String, String> action;
    private HashMap<String, String> closing;
    private Locale locale;
    private List<HashMap<String, String>> inlineImages;

    public EmailTemplateDTO() {
        this.body = new ArrayList<>();
        this.inlineImages = new ArrayList<>();
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

    public List<HashMap<String, String>> getInlineImages() {
        return inlineImages;
    }

    public void addInlineImage(String imageId, String imagePath) {
        HashMap<String, String> value = new HashMap<>();
        value.put("id", imageId);
        value.put("filepath", imagePath);
        inlineImages.add(value);
    }

    public void addInlineImageFromResource(String imageId, String imagePath) throws FileNotFoundException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(imagePath);
        if (resource == null) {
            throw new FileNotFoundException(imagePath);
        }
        HashMap<String, String> value = new HashMap<>();
        value.put("id", imageId);
        value.put("filepath", resource.getFile());
        inlineImages.add(value);
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
        return result;
    }

    public Context getContext() {
        Context result = new Context(getLocale());
        result.setVariable("baseUrl", baseUrl);
        result.setVariable("subject", subject);
        result.setVariable("logoUrl", logoUrl);
        result.setVariable("title", title);
        result.setVariable("body", body);
        result.setVariable("textBody", plaintextBody);
        result.setVariable("action", action);
        result.setVariable("closing", closing);
        return result;
    }
}
