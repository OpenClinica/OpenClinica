package core.org.akaza.openclinica.service.rest.errors;

public class ErrorConstants {

	
	/**
	 *  this section is "synchronized" from SBS
	 */
	public static final String ERR_CONCURRENCY_FAILURE = "errorCode.concurrencyFailure";
    public static final String ERR_ACCESS_DENIED = "errorCode.accessDenied";
    public static final String ERR_VALIDATION = "errorCode.validation";
    public static final String ERR_METHOD_NOT_SUPPORTED = "errorCode.methodNotSupported";
    public static final String ERR_INTERNAL_SERVER_ERROR = "errorCode.internalServerError";

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
    public static final String PARTICIPANT_ID_MISSING_PARTICIPANT_ID_DATA = "errorCode.missingParticipantIdData";
    public static final String PARTICIPANT_ID_MULTIPLE_PARTICIPANT_ID_HEADERS = "errorCode.multipleParticipantIdHeaders";

    /**
     *  this is new for RunTime
     */
    public static final String ERR_NOT_XML_FILE = "errorCode.notXMLfile";
    public static final String ERR_BLANK_FILE = "errorCode.blankFile";
    public static final String ERR_NO_MAPPING_FILE = "errorCode.noMappingfile";
    public static final String ERR_NOT_SUPPORTED_FILE_FORMAT = "errorCode.notSupportedFileFormat";
    public static final String ERR_XML_NOT_WELL_FORMED = "errorCode.xmlNotWellFormed";
    
    public static final String ERR_STUDY_NOT_EXIST = "errorCode.studyNotExist";
    public static final String ERR_SITE_NOT_EXIST = "errorCode.siteNotExist";
    public static final String ERR_NO_ROLE_SETUP = "errorCode.noRoleSetUp";
    public static final String ERR_NO_SUFFICIENT_PRIVILEGES = "errorCode.noSufficientPrivileges";
    public static final String ERR_PARTICIPANTS_ENROLLMENT_CAP_REACHED = "errorCode.participantsEnrollmentCapReached";

    public static final String ERR_NO_SUBJECT_FOUND = "errorCode.noSubjectFound";
    public static final String ERR_EVENT_NOT_EXIST = "errorCode.eventNotExist";
    public static final String ERR_NOT_INTEGER = "errorCode.notInteger";
    public static final String ERR_ORDINAL_TOO_BIG = "errorCode.ordinalTooBig";
    
    public static final String ERR_NO_START_DATE = "errorCode.noStartDate";
    public static final String ERR_START_DATE = "errorCode.startDateError";
    public static final String ERR_PARSE_DATE = "errorCode.dateParsedError";
    
    public static final String ERR_END_DATE_BEFORE_START_DATE = "errorCode.endDateBeforeStartDate";
    public static final String ERR_STUDY_LOCKED = "errorCode.studyLocked";
    
    public static final String ERR_SUBJECT_REMOVED = "errorCode.subjectRemoved";
    public static final String ERR_WRONG_EVENT_TYPE = "errorCode.wrongEventType";
    public static final String ERR_ORDINAL_NOT_ONE_FOR_NONREPEATING = "errorCode.ordinalNot1ForNONRepeating";
    public static final String ERR_NON_REPEATING_ALREADY_EXISIT = "errorCode.nonRepeatingAlreadyExist";
    public static final String ERR_ALREADY_EXISIT = "errorCode.alreadyExist";
    
    public static final String ERR_GREATER_THAN_MAX_ORDINAL = "errorCode.greaterThanMaxSampleOrdinal";
    public static final String ERR_EVENT_NOT_ACTIVE = "errorCode.notActive";
    public static final String ERR_LOG_FILE = "errorCode.logFile";
    public static final String ERR_DATA_MISSING_PIPE = "errorCode.dataRowMissingPipe";
    public static final String ERR_STUDY_NOT_Valid_OID = "errorCode.studyNotValidOid";
    public static final String ERR_SITE_NOT_Valid_OID = "errorCode.siteNotValidOid";
    public static final String ERR_STUDY_TO_SITE_NOT_Valid_OID = "errorCode.studyTositeNotValidOid";
    public static final String ERR_PARTICIPATE_INACTIVE = "errorCode.participateInActive";
    
    public static final String ERR_FORM = "errorCode.formNotFound";
    public static final String ERR_FORM_LAYOUT = "errorCode.formLayOut";
    public static final String ERR_ITEM_OR_ITEM_GROUP_OID ="errorCode.ItemOrItemGroupOIDValidationFailed";
    public static final String ERR_ITEM_OID ="errorCode.ItemOIDValidationFailed";
    public static final String ERR_ITEM_GROUP_OID ="errorCode.ItemGroupOIDValidationFailed";

    public static final String ERR_ENKETO_CLIENT = "errorCode.enketoClient";
    public static final String ERR_ENKETO_SERVER = "errorCode.enketoServer";



}
