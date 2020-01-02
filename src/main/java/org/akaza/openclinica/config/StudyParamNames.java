package org.akaza.openclinica.config;

import java.util.HashMap;
import java.util.Map;

public class StudyParamNames {
    public static final String PERSON_ID_SHOWN_ON_CRF = "personIdShownOnCRF";
    public static final String INTERVIEW_DATE_EDITABLE = "interviewDateEditable";
    public static final String INTERVIEW_DATE_DEFAULT = "interviewDateDefault";
    public static final String INTERVIEWER_NAME_EDITABLE = "interviewerNameEditable";
    public static final String INTERVIEW_DATE_REQUIRED = "interviewDateRequired";
    public static final String INTERVIEWER_NAME_DEFAULT = "interviewerNameDefault";
    public static final String INTERVIEWER_NAME_REQUIRED = "interviewerNameRequired";
    public static final String SUBJECT_ID_PREFIX_SUFFIX = "subjectIdPrefixSuffix";
    public static final String GENDER_REQUIRED = "genderRequired";
    public static final String SUBJECT_ID_GENERATION = "subjectIdGeneration";
    public static final String SUBJECT_PERSON_ID_REQUIRED = "subjectPersonIdRequired";
    public static final String DISCREPANCY_MANAGEMENT = "discrepancyManagement";
    public static final String COLLECT_DOB = "collectDob";
    public static final String EVENT_LOCATION_REQUIRED = "eventLocationRequired";
    public static final String SECONDARY_LABEL_VIEWABLE = "secondaryLabelViewable";
    public static final String ADMIN_FORCED_REASON_FOR_CHANGE = "adminForcedReasonForChange";
    public static final String PARTICIPANT_PORTAL = "participantPortal";
    public static final String RANDOMIZATION = "randomization";
    public static final String ENFORCE_ENROLLMENT_CAP = "enforceEnrollmentCap";
    public static final String PARTICIPANT_ID_TEMPLATE = "participantIdTemplate";
    public static final String CONTACTS_MODULE = "contactsModule";

    private static final Map<String, String> defaultValuesMap = new HashMap<String, String>();
    static {
        defaultValuesMap.put(PERSON_ID_SHOWN_ON_CRF, "false");
        defaultValuesMap.put(INTERVIEW_DATE_EDITABLE, "true");
        defaultValuesMap.put(INTERVIEW_DATE_DEFAULT, "blank");
        defaultValuesMap.put(INTERVIEWER_NAME_EDITABLE, "true");
        defaultValuesMap.put(INTERVIEW_DATE_REQUIRED, "not_used");
        defaultValuesMap.put(INTERVIEWER_NAME_DEFAULT, "blank");
        defaultValuesMap.put(INTERVIEWER_NAME_REQUIRED, "not_used");
        defaultValuesMap.put(SUBJECT_ID_PREFIX_SUFFIX, "true");
        defaultValuesMap.put(GENDER_REQUIRED, "true");
        defaultValuesMap.put(SUBJECT_ID_GENERATION, "manual");
        defaultValuesMap.put(SUBJECT_PERSON_ID_REQUIRED, "required");
        defaultValuesMap.put(DISCREPANCY_MANAGEMENT, "true");
        defaultValuesMap.put(COLLECT_DOB, "1");
        defaultValuesMap.put(EVENT_LOCATION_REQUIRED, "not_used");
        defaultValuesMap.put(SECONDARY_LABEL_VIEWABLE, "false");
        defaultValuesMap.put(ADMIN_FORCED_REASON_FOR_CHANGE, "true");
        defaultValuesMap.put(PARTICIPANT_PORTAL, "disabled");
        defaultValuesMap.put(RANDOMIZATION, "disabled");
        defaultValuesMap.put(ENFORCE_ENROLLMENT_CAP, "false");
        defaultValuesMap.put(PARTICIPANT_ID_TEMPLATE, "");
    }

    public static String getDefaultValues(String name){
        return defaultValuesMap.getOrDefault(name, "");
    }
}
