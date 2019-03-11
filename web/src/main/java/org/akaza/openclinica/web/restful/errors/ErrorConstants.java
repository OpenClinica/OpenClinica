package org.akaza.openclinica.web.restful.errors;

public class ErrorConstants {
	
	/**
	 *  this section is "synchronized" from SBS
	 */
	public static final String ERR_CONCURRENCY_FAILURE = "error.concurrencyFailure";
    public static final String ERR_ACCESS_DENIED = "error.accessDenied";
    public static final String ERR_VALIDATION = "error.validation";
    public static final String ERR_METHOD_NOT_SUPPORTED = "error.methodNotSupported";
    public static final String ERR_INTERNAL_SERVER_ERROR = "error.internalServerError";

    public static final String UUID_EXISTS = "errorCode.uuidExists";
    public static final String STUDY_DOESNT_EXIST = "errorCode.studyDoesntExist";
    public static final String NAME_REQUIRED = "errorCode.nameRequired";
    public static final String NAME_NOT_UNIQUE = "errorCode.nameNotUnique";
    public static final String STATUS_REQUIRED = "errorCode.statusRequired";
    public static final String UNIQUE_IDENTIFIER_REQUIRED = "errorCode.uniqueIdentifierRequired";
    public static final String UNIQUE_IDENTIFIER_NOT_UNIQUE = "errorCode.uniqueIdentifierNotUnique";
    public static final String PRINCIPAL_INVESTIGATOR_REQUIRED = "errorCode.principalInvestigatorRequired";
    public static final String EXPECTED_ENROLLMENT_REQUIRED = "errorCode.expectedEnrollmentRequired";
    public static final String STUDY_ENVIRONMENT_UUID_DOESNT_EXIST = "errorCode.studyEnvironmentUuidDoesntExist";
    public static final String STUDY_ENVIRONMENT_SITE_DOESNT_EXIST = "errorCode.studyEnvironmentSiteDoesntExist";
    public static final String STUDY_SITE_DOESNT_EXIST = "errorCode.studySiteDoesntExist";
    public static final String SITE_DOESNT_EXIST = "errorCode.siteDoesntExist";
    public static final String SITE_NAME_ALREADY_EXISTS = "errorCode.siteNameAlreadyExists";
    public static final String START_DATE_AFTER_END_DATE = "errorCode.startDateAfterEndDate";
    public static final String PARTICIPANT_ID_TEMPLATE_TOO_LONG = "errorCode.participantIdTemplateTooLong";

    /**
     *  this is new for RunTime
     */
    public static final String ERR_NOT_XML_FILE = "errorCode.notXMLfile";
    public static final String ERR_BLANK_FILE = "error.blankFile";
    public static final String ERR_NO_MAPPING_FILE = "error.noMappingfile";
    public static final String ERR_NOT_SUPPORTED_FILE_FORMAT = "error.notSupportedFileFormat";
    public static final String ERR_XML_NOT_WELL_FORMED = "error.xmlNotWellFormed";
    
    public static final String ERR_STUDY_NOT_EXIST = "errorCode.studyNotExist";
    public static final String ERR_SITE_NOT_EXIST = "error.siteNotExist";
    public static final String ERR_NO_ROLE_SETUP = "error.noRoleSetUp";
    public static final String ERR_NO_SUFFICIENT_PRIVILEGES = "error.noSufficientPrivileges";
    public static final String ERR_PARTICIPANTS_ENROLLMENT_CAP_REACHED = "error.participantsEnrollmentCapReached";

    public static final String ERR_NO_SUBJECT_FOUND = "errorCode.noSubjectFound";
    public static final String ERR_EVENT_NOT_EXIST = "error.eventNotExist";
    public static final String ERR_NOT_INTEGER = "error.notInteger";
    public static final String ERR_ORDINAL_TOO_BIG = "error.ordinalTooBig";
    
    public static final String ERR_NO_START_DATE = "errorCode.noStartDate";
    public static final String ERR_START_DATE = "error.startDateError";
    public static final String ERR_END_DATE_BEFORE_START_DATE = "error.endDateBeforeStartDate";
    public static final String ERR_STUDY_LOCKED = "error.studyLocked";
    
    public static final String ERR_SUBJECT_REMOVED = "errorCode.subjectRemoved";
    public static final String ERR_WRONG_EVENT_TYPE = "error.wrongEventType";
    public static final String ERR_ORDINAL_NOT_ONE_FOR_NONREPEATING = "error.ordinalNot1ForNONRepeating";
    public static final String ERR_NON_REPEATING_ALREADY_EXISIT = "error.nonRepeatingAlreadyExist";
    
    public static final String ERR_GREATER_THAN_MAX_ORDINAL = "errorCode.greaterThanMaxSampleOrdinal";
    public static final String ERR_EVENT_NOT_ACTIVE = "error.notActive";
    
    

}
