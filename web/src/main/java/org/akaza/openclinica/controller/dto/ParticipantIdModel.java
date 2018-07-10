package org.akaza.openclinica.controller.dto;

public class ParticipantIdModel {
    private static final String[] participantIdVariables= {"siteId", "siteParticipantCount"};
    private static final String[] participantIdExamples={"Example1", "Example2", "Example3"};


    public static String[] getParticipantIdVariables() {
        return participantIdVariables;
    }

    public static String[] getParticipantIdExamples() {
        return participantIdExamples;
    }
}
