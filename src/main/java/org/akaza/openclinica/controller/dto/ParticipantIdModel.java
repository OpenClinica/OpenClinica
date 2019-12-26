package org.akaza.openclinica.controller.dto;

import org.akaza.openclinica.controller.helper.TemplateHelper;
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
        variables.add(new ParticipantIdVariable("siteId","Site ID","SiteA"));
        variables.add(new ParticipantIdVariable("siteParticipantCount","Number of participants at given site",276));
        variables.add(new ParticipantIdVariable("helper","generate random numbers",new TemplateHelper()));


        ParticipantIdExample example1 = new ParticipantIdExample();
        example1.setTemplate("${siteId}-${(siteParticipantCount+1)?string[\"000\"]}");
        example1.setDescription("Site ID followed by site participant count in a three digit number format");
        examples.add(example1);

    }


}