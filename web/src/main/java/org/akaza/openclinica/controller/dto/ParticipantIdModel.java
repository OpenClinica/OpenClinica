package org.akaza.openclinica.controller.dto;

import java.util.*;


public class ParticipantIdModel {

    private static List<ParticipantIdVariable> variables=new ArrayList<>();
    private static List<ParticipantIdExample> examples=new ArrayList<>();


    public static Map<String, Object>  getDataModel(){

        Map<String, Object> data = new HashMap<String, Object>();
        for(ParticipantIdVariable var : variables){
         data.put(var.getName(),var.getSampleValue());
        }

        return data;
    }

    public List<ParticipantIdVariable> getVariables() {
        return variables;
    }

    public void setVariables(List<ParticipantIdVariable> variables) {
        this.variables = variables;
    }

    public void setExamples(List<ParticipantIdExample> examples) {
        this.examples = examples;
    }


    public List<ParticipantIdExample> getExamples() {
        return examples;
    }

    static {
        ParticipantIdVariable variable1 = new ParticipantIdVariable();
        variable1.setName("siteId");
        variable1.setDescription("Site ID");
        variable1.setSampleValue("SiteA");
        variables.add(variable1);

        ParticipantIdVariable variable2 = new ParticipantIdVariable();
        variable2.setName("siteParticipantCount");
        variable2.setDescription("Number of participants at given site");
        variable2.setSampleValue(1);
        variables.add(variable2);

        ParticipantIdExample example1 = new ParticipantIdExample();
        example1.setTemplate("${(siteParticipantCount+1)?string[\"000\"]}" );
        example1.setDescription("Site participant count in a three digit number format");
        examples.add(example1);

        ParticipantIdExample example2 = new ParticipantIdExample();
        example2.setTemplate("${siteId}-${(siteParticipantCount+1)?string[\"00\"]}");
        example2.setDescription("Site ID followed by site participant count in a two digit number format");
        examples.add(example2);

    }


}