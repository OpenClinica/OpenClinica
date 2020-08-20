package core.org.akaza.openclinica.service;

import core.org.akaza.openclinica.bean.managestudy.SubjectTransferBean;
import org.akaza.openclinica.controller.dto.AddParticipantRequestDTO;
import org.akaza.openclinica.controller.dto.StudyEventScheduleDTO;
import org.akaza.openclinica.controller.helper.PIIEnum;
import core.org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.web.restful.errors.ErrorConstants;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import static core.org.akaza.openclinica.service.rest.errors.ErrorConstants.PARTICIPANT_ID_MISSING_PARTICIPANT_ID_DATA;
import static core.org.akaza.openclinica.service.rest.errors.ErrorConstants.PARTICIPANT_ID_MULTIPLE_PARTICIPANT_ID_HEADERS;

@Service( "CSVService" )
public class CSVServiceImpl implements CSVService {
    private final static Logger log = LoggerFactory.getLogger(CSVServiceImpl.class);
    private static final String PARTICIPANT_ID = "ParticipantID";
    private static final String FIRST_NAME = "FirstName";
    private static final String LAST_NAME = "LastName";
    private static final String EMAIL_ADDRESS = "EmailAddress";
    private static final String MOBILE_NUMBER = "MobileNumber";
    //Study event bulk schedule CSV file header
    private static final String STUDY_EVENT_OID = "StudyEventOID";
    private static final String STUDY_EVENT_REPEAT_KEY = "StudyEventRepeatKey";
    private static final String START_DATE = "StartDate";
    private static final String END_DATE = "EndDate";
    private static final String STUDY_EVENT_STATUS = "StudyEventStatus";
    private static final String NON_PRINTABLE_CHAR_REGEX = "\\p{C}";




    /**
     * @param file
     * @param studyOID
     * @param siteOID
     * @return
     * @throws Exception
     */

    public List<StudyEventScheduleDTO> readStudyEventScheduleBulkCSVFile(MultipartFile file, String studyOID, String siteOID) throws Exception {

        List<StudyEventScheduleDTO> studyEventScheduleDTOList = new ArrayList<>();

        //Study event bulk schedule CSV file header position
        int participantID_index = -1;
        int studyEventOID_index = -1;
        int studyEventRepeatKey_index = -1;
        int startDate_index = -1;
        int endDate_index = -1;
        int studyEventStatus_index = -1;

        try (Scanner sc = new Scanner(file.getInputStream())) {

            String line;

            int rowNumber = 1;

            while (sc.hasNextLine()) {
                line = sc.nextLine();

                //in case the last column is empty
                if (line.endsWith(",")) {
                    line = line + " ,";
                }

                String[] lineVal = line.split(",", 0);

                // check ParticipantID column number
                if (rowNumber == 1) {

                    for (int i = 0; i < lineVal.length; i++) {
                        String currentHeader = lineVal[i].trim();

                        if (currentHeader.equalsIgnoreCase(PARTICIPANT_ID)) {
                            participantID_index = i;
                        } else if (currentHeader.equalsIgnoreCase(STUDY_EVENT_OID)) {
                            studyEventOID_index = i;
                        } else if (currentHeader.equalsIgnoreCase(STUDY_EVENT_REPEAT_KEY)) {
                            studyEventRepeatKey_index = i;
                        } else if (currentHeader.equalsIgnoreCase(START_DATE)) {
                            startDate_index = i;
                        } else if (currentHeader.equalsIgnoreCase(END_DATE)) {
                            endDate_index = i;
                        } else if (currentHeader.equalsIgnoreCase(STUDY_EVENT_STATUS)) {
                            studyEventStatus_index = i;
                        } else {
                            // Incorrect Header name

                        }
                    }

                } else {
                    StudyEventScheduleDTO studyEventScheduleDTO = new StudyEventScheduleDTO();

                    studyEventScheduleDTO.setStudyOID(studyOID.trim());
                    if (siteOID != null) {
                        studyEventScheduleDTO.setSiteOID(siteOID.trim());
                    }

                    if (participantID_index != -1 && lineVal[participantID_index] != null && lineVal[participantID_index].trim().length() > 0) {
                        studyEventScheduleDTO.setSubjectKey(lineVal[participantID_index].trim());
                    }

                    if (studyEventOID_index != -1 && lineVal[studyEventOID_index] != null && lineVal[studyEventOID_index].trim().length() > 0) {
                        studyEventScheduleDTO.setStudyEventOID(lineVal[studyEventOID_index].trim());
                    }

                    if (studyEventRepeatKey_index != -1 && lineVal[studyEventRepeatKey_index] != null && lineVal[studyEventRepeatKey_index].trim().length() > 0) {
                        studyEventScheduleDTO.setOrdinal(lineVal[studyEventRepeatKey_index].trim());
                    }
                    if (startDate_index != -1 && lineVal[startDate_index] != null && lineVal[startDate_index].trim().length() > 0) {
                        studyEventScheduleDTO.setStartDate(lineVal[startDate_index].trim());
                    }
                    if (endDate_index != -1 && lineVal[endDate_index] != null && lineVal[endDate_index].trim().length() > 0) {
                        studyEventScheduleDTO.setEndDate(lineVal[endDate_index].trim());
                    }

                    if (studyEventStatus_index != -1 && lineVal[studyEventStatus_index] != null && lineVal[studyEventStatus_index].trim().length() > 0) {
                        studyEventScheduleDTO.setStudyEventStatus(lineVal[studyEventStatus_index].trim());
                    }
                    studyEventScheduleDTO.setRowNum(rowNumber-1);

                    studyEventScheduleDTOList.add(studyEventScheduleDTO);
                }

                rowNumber++;
            }

        } catch (Exception e) {
            log.error("Exception with cause = {} {}", e.getCause(), e.getMessage());
        }



        return studyEventScheduleDTOList;
    }

    public List<AddParticipantRequestDTO> readAddParticipantBulkCSVFile(MultipartFile file, String studyOID, String siteOID) throws Exception {

        List<AddParticipantRequestDTO> addParticipantRequestDTOs = new ArrayList<>();

        //Study event bulk schedule CSV file header position
        int participantID_index = -1;
        int firstName_index = -1;
        int lastName_index = -1;
        int emailAddress_index = -1;
        int mobileNumber_index = -1;
        try (Scanner sc = new Scanner(file.getInputStream())) {
            String line;
            int rowNumber = 1;
            while (sc.hasNextLine()) {
                line = sc.nextLine();

                //in case the last column is empty
                if (line.endsWith(",")) {
                    line = line + " ,";
                }
                String[] lineVal = line.split(",", 0);

                // check ParticipantID column number
                if (rowNumber == 1) {
                    for (int i = 0; i < lineVal.length; i++) {
                        String currentHeader = lineVal[i].trim();
                        // removes non-printable characters from Unicode
                        currentHeader = currentHeader.replaceAll(NON_PRINTABLE_CHAR_REGEX, "");
                        if (currentHeader.equalsIgnoreCase(PARTICIPANT_ID)) {
                            participantID_index = i;
                        } else if (currentHeader.equalsIgnoreCase(FIRST_NAME)) {
                            firstName_index = i;
                        } else if (currentHeader.equalsIgnoreCase(LAST_NAME)) {
                            lastName_index = i;
                        } else if (currentHeader.equalsIgnoreCase(EMAIL_ADDRESS)) {
                            emailAddress_index = i;
                        } else if (currentHeader.equalsIgnoreCase(MOBILE_NUMBER)) {
                            mobileNumber_index = i;
                        } else {
                            // Incorrect Header name
                        }
                    }
                } else {
                    // removes non-printable characters from first column
                    if(lineVal.length > 0){
                        lineVal[0] = lineVal[0].replaceAll(NON_PRINTABLE_CHAR_REGEX, "");
                    }
                    AddParticipantRequestDTO addParticipantRequestDTO = new AddParticipantRequestDTO();

                    if (participantID_index != -1 && lineVal[participantID_index] != null && lineVal[participantID_index].trim().length() > 0) {
                        addParticipantRequestDTO.setSubjectKey(lineVal[participantID_index].trim());
                    }
                    if (firstName_index != -1 && lineVal[firstName_index] != null && lineVal[firstName_index].trim().length() > 0) {
                        addParticipantRequestDTO.setFirstName(lineVal[firstName_index].trim());
                    }
                    if (lastName_index != -1 && lineVal[lastName_index] != null && lineVal[lastName_index].trim().length() > 0) {
                        addParticipantRequestDTO.setLastName(lineVal[lastName_index].trim());
                    }
                    if (emailAddress_index != -1 && lineVal[emailAddress_index] != null && lineVal[emailAddress_index].trim().length() > 0) {
                        addParticipantRequestDTO.setEmailAddress(lineVal[emailAddress_index].trim());
                    }
                    if (mobileNumber_index != -1 && lineVal[mobileNumber_index] != null && lineVal[mobileNumber_index].trim().length() > 0) {
                        addParticipantRequestDTO.setPhoneNumber(lineVal[mobileNumber_index].trim());
                    }
                    addParticipantRequestDTO.setRowNumber(rowNumber - 1);
                    addParticipantRequestDTOs.add(addParticipantRequestDTO);
                }
                rowNumber++;
            }

        } catch (Exception e) {
            log.error("Exception with cause = {} {}", e.getCause(), e.getMessage());
        }
        return addParticipantRequestDTOs;
    }


    /**
     * @param file
     * @return
     * @throws Exception
     */
    public void validateBulkParticipantCSVFile(MultipartFile file) throws Exception {

        ArrayList<String> subjectKeyList = new ArrayList<>();
        CSVParser csvParser = null;
        try {
            BufferedReader reader;

            InputStream is = file.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is));

            //Create the CSVFormat object with the header mapping
            CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader().withTrim();
            try {
                csvParser = new CSVParser(reader, csvFileFormat);
            } catch (IllegalArgumentException e) {
                throw new Exception(PARTICIPANT_ID_MULTIPLE_PARTICIPANT_ID_HEADERS);
            }
            Map<String, Integer> headerMap = csvParser.getHeaderMap();
            if (CollectionUtils.isEmpty(headerMap))
                throw new Exception(PARTICIPANT_ID_MISSING_PARTICIPANT_ID_DATA);
            final long participantIDCount = headerMap.entrySet().stream().filter(x -> x.getKey().equals(PARTICIPANT_ID)).count();

            if (participantIDCount == 0) {
                throw new Exception(PARTICIPANT_ID_MISSING_PARTICIPANT_ID_DATA);
            } else if (participantIDCount > 1) {
                throw new Exception(PARTICIPANT_ID_MULTIPLE_PARTICIPANT_ID_HEADERS);
            }
        } catch (Exception e) {
            String message = " This CSV format is not supported ";
            if (StringUtils.isNotEmpty(e.getMessage()))
                message = e.getMessage();
            throw new Exception(message);
        } finally {
            if (csvParser != null)
                csvParser.close();
        }
    }

    private List<SubjectTransferBean> populateCSVColumns(CSVParser parser) throws Exception {
        List<SubjectTransferBean> transferBeans = new ArrayList<>();
        Map<String, Integer> headerMap = parser.getHeaderMap();
        BidiMap<String, Integer> bidiMap = new DualHashBidiMap();
        bidiMap.putAll(headerMap);
        try {
            parser.getRecords().forEach(record -> {
                SubjectTransferBean transferBean = new SubjectTransferBean();
                int headerIndex = -1;
                Iterator<String> iterator = record.iterator();
                while (iterator.hasNext()) {
                    String column = iterator.next();
                    if (StringUtils.isNotEmpty(column)) {
                        ++headerIndex;
                        PIIEnum piiEnum = EnumUtils.getEnum(PIIEnum.class, bidiMap.getKey(headerIndex));
                        if (piiEnum == null)
                            continue;
                        switch (piiEnum) {
                            case EmailAddress:
                                transferBean.setEmailAddress(column);
                                break;
                            case FirstName:
                                transferBean.setFirstName(column);
                                break;
                            case LastName:
                                transferBean.setLastName(column);
                                break;
                            case Identifier:
                                transferBean.setIdentifier(column);
                                break;
                            case ParticipantID:
                                transferBean.setStudySubjectId(column);
                                break;
                            case MobileNumber:
                                transferBean.setPhoneNumber(column);
                                break;
                            default:
                                // ignore all others
                                break;
                        }
                    }
                }
                transferBeans.add(transferBean);
            });
        } catch (IOException e) {
            log.error("Exception reading file:" + e);
            throw e;
        }
        return transferBeans;
    }

    public List<SubjectTransferBean> readBulkParticipantCSVFile(MultipartFile file) throws Exception {

        List<SubjectTransferBean> subjectKeyList = null;
        CSVParser csvParser = null;

        try {
            BufferedReader reader;

            String line;
            InputStream is = file.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is));

            //Create the CSVFormat object with the header mapping
            CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader().withTrim();
            csvParser = new CSVParser(reader, csvFileFormat);
            subjectKeyList = populateCSVColumns(csvParser);
        } catch (Exception e) {
            String message = " This CSV format is not supported ";
            if (StringUtils.isNotEmpty(e.getMessage()))
                message = e.getMessage();
            throw new Exception(message);
        } finally {
            if (csvParser != null)
                csvParser.close();
        }

        return subjectKeyList;
    }


    public void validateCSVFileHeaderForScheduleEvents(MultipartFile file, String studyOID, String siteOID) throws Exception {

        //Study event bulk schedule CSV file header position
        int participantID_index = -1;
        int studyEventOID_index = -1;
        int studyEventRepeatKey_index = -1;
        int startDate_index = -1;
        int endDate_index = -1;
        int studyEventStatus_index = -1;

        Scanner sc = new Scanner(file.getInputStream());
        String line;
        while (sc.hasNextLine()) {
            line = sc.nextLine();

            //in case the last column is empty
            if (line.endsWith(",")) {
                line = line + " ,";
            }

            String[] lineVal = line.split(",", 0);
            for (int i = 0; i < lineVal.length; i++) {
                String currentHeader = lineVal[i].trim();
                if (currentHeader.equalsIgnoreCase(PARTICIPANT_ID)) {
                    if (participantID_index == -1) {
                        participantID_index = i;
                    } else {
                        throw new OpenClinicaSystemException(ErrorConstants.ERR_MULTIPLE_PARTICIPANT_ID_HEADERS);
                    }
                } else if (currentHeader.equalsIgnoreCase(STUDY_EVENT_OID)) {
                    if (studyEventOID_index == -1) {
                        studyEventOID_index = i;
                    } else {
                        throw new OpenClinicaSystemException(ErrorConstants.ERR_MULTIPLE_STUDY_EVENT_OID_HEADERS);
                    }
                } else if (currentHeader.equalsIgnoreCase(STUDY_EVENT_REPEAT_KEY)) {
                    if (studyEventRepeatKey_index == -1) {
                        studyEventRepeatKey_index = i;
                    } else {
                        throw new OpenClinicaSystemException(ErrorConstants.ERR_MULTIPLE_STUDY_EVENT_REPEAT_KEY_HEADERS);
                    }
                } else if (currentHeader.equalsIgnoreCase(START_DATE)) {
                    if (startDate_index == -1) {
                        startDate_index = i;
                    } else {
                        throw new OpenClinicaSystemException(ErrorConstants.ERR_MULTIPLE_START_DATE_HEADERS);
                    }
                } else if (currentHeader.equalsIgnoreCase(END_DATE)) {
                    if (endDate_index == -1) {
                        endDate_index = i;
                    } else {
                        throw new OpenClinicaSystemException(ErrorConstants.ERR_MULTIPLE_END_DATE_HEADERS);
                    }
                } else if (currentHeader.equalsIgnoreCase(STUDY_EVENT_STATUS)) {
                    if (studyEventStatus_index == -1) {
                        studyEventStatus_index = i;
                    } else {
                        throw new OpenClinicaSystemException(ErrorConstants.ERR_MULTIPLE_STUDY_EVENT_STATUS_HEADERS);
                    }
                }
            }
            if (participantID_index == -1) {
                throw new OpenClinicaSystemException(ErrorConstants.ERR_MISSING_PARTICIPANT_ID_DATA);

            } else if (studyEventOID_index == -1) {
                throw new OpenClinicaSystemException(ErrorConstants.ERR_MISSING_STUDY_EVENT_OID_DATA);

            } else if (startDate_index == -1) {
                throw new OpenClinicaSystemException(ErrorConstants.ERR_MISSING_START_DATE_DATA);
            }
        }
    }

    public void validateCSVFileHeaderForAddParticipants(MultipartFile file, String studyOID, String siteOID) throws Exception {
        int participantID_index = -1;
        int firstName_index = -1;
        int lastName_index = -1;
        int emailAddress_index = -1;
        int phoneNumber_index = -1;
        Scanner sc = new Scanner(file.getInputStream());
        String line;
        while (sc.hasNextLine()) {
            line = sc.nextLine();

            //in case the last column is empty
            if (line.endsWith(",")) {
                line = line + " ,";
            }

            String[] lineVal = line.split(",", 0);
            for (int i = 0; i < lineVal.length; i++) {
                String currentHeader = lineVal[i].trim();
                // removes non-printable characters from Unicode
                currentHeader = currentHeader.replaceAll(NON_PRINTABLE_CHAR_REGEX, "");
                if (currentHeader.equalsIgnoreCase(PARTICIPANT_ID)) {
                    if (participantID_index == -1) {
                        participantID_index = i;
                    } else {
                        throw new OpenClinicaSystemException(ErrorConstants.ERR_MULTIPLE_PARTICIPANT_ID_HEADERS);
                    }
                } else if (currentHeader.equalsIgnoreCase(FIRST_NAME)) {
                    if (firstName_index == -1) {
                        firstName_index = i;
                    } else {
                        throw new OpenClinicaSystemException(ErrorConstants.ERR_MULTIPLE_FIRST_NAME_HEADERS);
                    }
                } else if (currentHeader.equalsIgnoreCase(LAST_NAME)) {
                    if (lastName_index == -1) {
                        lastName_index = i;
                    } else {
                        throw new OpenClinicaSystemException(ErrorConstants.ERR_MULTIPLE_LAST_NAME_HEADERS);
                    }
                } else if (currentHeader.equalsIgnoreCase(EMAIL_ADDRESS)) {
                    if (emailAddress_index == -1) {
                        emailAddress_index = i;
                    } else {
                        throw new OpenClinicaSystemException(ErrorConstants.ERR_MULTIPLE_EMAIL_ADDRESS_HEADERS);
                    }
                } else if (currentHeader.equalsIgnoreCase(MOBILE_NUMBER)) {
                    if (phoneNumber_index == -1) {
                        phoneNumber_index = i;
                    } else {
                        throw new OpenClinicaSystemException(ErrorConstants.ERR_MULTIPLE_MOBILE_PHONE_HEADERS);
                    }
                }
            }
            if (participantID_index == -1) {
                throw new OpenClinicaSystemException(ErrorConstants.ERR_MISSING_PARTICIPANT_ID);
            }
        }
    }


}
