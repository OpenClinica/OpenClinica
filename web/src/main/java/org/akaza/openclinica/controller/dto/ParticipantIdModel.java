package org.akaza.openclinica.controller.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class ParticipantIdModel {
    private static final String[] examples={"${(siteParticipantCount+1)?string[\"0000\"]}", "${siteId}-${(siteParticipantCount+1)?string[\"0000\"]}"};

   private static Map<String, Object> data = new HashMap<String, Object>();
    static {
        data.put("siteId", "HELLO THERE");
        data.put("siteParticipantCount", 25);
    }


    public  Set<String> getVariables() {
        return data.keySet();
    }

    public  String[] getExamples() {
        return examples;
    }

    public static Map<String, Object> getData() {
        return data;
    }

    public static void setData(Map<String, Object> data) {
        ParticipantIdModel.data = data;
    }
}
