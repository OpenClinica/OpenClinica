package org.akaza.openclinica.bean.service;

import org.akaza.openclinica.domain.enumsupport.CodedEnum;

import java.util.HashMap;

public enum ProcessingResultType implements CodedEnum {
    SUCCESS(1, "SUCCESS"), FAIL(2, "FAIL"), WARNING(3, "WARNING");
    
    ProcessingResultType(int code) {
        this(code, null);
    }
    
    ProcessingResultType(int code, String description) {
        this.code = code;
        this.description = description;
    }
    
    ProcessingResultType(int code, String description, String url) {
        this.code = code;
        this.description = description;
        this.url = url;
    }
    
    ProcessingResultType(int code, String description, String url, String archiveMessage) {
        this.code = code;
        this.description = description;
        this.url = url;
        this.archiveMessage = archiveMessage;
    }
    
    private int code;
    private String description;
    private String url;
    private String archiveMessage;
    
    public static ProcessingResultType getByCode(Integer code) {
        HashMap<Integer, ProcessingResultType> enumObjects = new HashMap<Integer, ProcessingResultType>();
        for (ProcessingResultType theEnum : ProcessingResultType.values()) {
            enumObjects.put(theEnum.getCode(), theEnum);
        }
        return enumObjects.get(Integer.valueOf(code));
    }
    
    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getArchiveMessage() {
        return archiveMessage;
    }

    public void setArchiveMessage(String archiveMessage) {
        this.archiveMessage = archiveMessage;
    }

    public void setCode(int code) {
        this.code = code;
    }
    

}
