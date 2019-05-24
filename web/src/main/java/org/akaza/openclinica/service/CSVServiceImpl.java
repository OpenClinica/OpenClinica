package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.managestudy.SubjectTransferBean;
import org.akaza.openclinica.controller.dto.StudyEventScheduleDTO;
import org.akaza.openclinica.controller.helper.PIIEnum;
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

import static org.akaza.openclinica.service.rest.errors.ErrorConstants.PARTICIPANT_ID_MISSING_PARTICIPANT_ID_DATA;
import static org.akaza.openclinica.service.rest.errors.ErrorConstants.PARTICIPANT_ID_MULTIPLE_PARTICIPANT_ID_HEADERS;

@Service("CSVService")
public class CSVServiceImpl implements CSVService {
    private final static Logger log = LoggerFactory.getLogger(CSVServiceImpl.class);
    private static final String ParticipantID_header = "ParticipantID";
    //Study event bulk schedule CSV file header
    private static final String StudyEventOID_header = "StudyEventOID";
    private static final String Ordinal_header = "Ordinal";
    private static final String StartDate_header = "StartDate";
    private static final String EndDate_header = "EndDate";

    /**
     *
     * @param file
     * @param studyOID
     * @param siteOID
     * @return
     * @throws Exception
     */

    public ArrayList<StudyEventScheduleDTO> readStudyEventScheduleBulkCSVFile(MultipartFile file, String studyOID, String siteOID) throws Exception {

        ArrayList<StudyEventScheduleDTO> studyEventScheduleDTOList = new ArrayList<>();

        //Study event bulk schedule CSV file header position
        int ParticipantID_index = -999;
        int StudyEventOID_index = -999;
        int Ordinal_index = -999;
        int StartDate_index = -999;
        int EndDate_index = -999;

        try(Scanner sc = new Scanner(file.getInputStream())){

            String line;

            int lineNm = 1;
            int position = 0;

            while (sc.hasNextLine()) {
                line = sc.nextLine();

                //in case the last column is empty
                if(line.endsWith(",")) {
                    line = line + " ,";
                }

                String[] lineVal= line.split(",", 0);

                // check ParticipantID column number
                if(lineNm ==1) {

                    for(int i=0; i < lineVal.length;i++) {
                        String currentHeader = lineVal[i].trim();
                        if(currentHeader.equalsIgnoreCase(ParticipantID_header)) {
                            ParticipantID_index = i;
                        }else if(currentHeader.equalsIgnoreCase(StudyEventOID_header)) {
                            StudyEventOID_index = i;
                        }else if(currentHeader.equalsIgnoreCase(Ordinal_header)) {
                            Ordinal_index = i;
                        }else if(currentHeader.equalsIgnoreCase(StartDate_header)) {
                            StartDate_index = i;
                        }else if(currentHeader.equalsIgnoreCase(EndDate_header)) {
                            EndDate_index = i;
                        }else {
                            ;
                        }
                    }
                }else {
                    StudyEventScheduleDTO studyEventScheduleDTO = new StudyEventScheduleDTO();

                    studyEventScheduleDTO.setStudyOID(studyOID.trim());
                    if(siteOID != null) {
                        studyEventScheduleDTO.setSiteOID(siteOID.trim());
                    }

                    if(lineVal[ParticipantID_index] != null) {
                        studyEventScheduleDTO.setSubjectKey(lineVal[ParticipantID_index].trim());
                    }

                    if(lineVal[StudyEventOID_index] != null) {
                        studyEventScheduleDTO.setStudyEventOID(lineVal[StudyEventOID_index].trim());
                    }

                    if(lineVal[Ordinal_index] != null && lineVal[Ordinal_index].trim().length() > 0) {
                        studyEventScheduleDTO.setOrdinal(lineVal[Ordinal_index].trim());
                    }
                    if(lineVal[StartDate_index] != null) {
                        studyEventScheduleDTO.setStartDate(lineVal[StartDate_index].trim());
                    }
                    if(lineVal[EndDate_index] != null && lineVal[EndDate_index].trim().length()>0) {
                        studyEventScheduleDTO.setEndDate(lineVal[EndDate_index].trim());
                    }

                    studyEventScheduleDTO.setRowNum(lineNm - 1);

                    studyEventScheduleDTOList.add(studyEventScheduleDTO);
                }

                lineNm++;
            }

        } catch (Exception e) {
            log.error("Exception with cause = {} {}", e.getCause(), e.getMessage());
        }



        return studyEventScheduleDTOList;
    }

    /**
     * @param file
     * @return
     * @throws Exception
     */
    public void validateCSVFile(MultipartFile file) throws Exception {

        ArrayList<String> subjectKeyList = new ArrayList<>();

        try {
            BufferedReader reader;

            InputStream is = file.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is));

            //Create the CSVFormat object with the header mapping
            CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader().withTrim();
            CSVParser csvParser;
            try {
                csvParser = new CSVParser(reader, csvFileFormat);
            } catch (IllegalArgumentException e) {
                throw new Exception(PARTICIPANT_ID_MULTIPLE_PARTICIPANT_ID_HEADERS);
            }
            Map<String, Integer> headerMap = csvParser.getHeaderMap();
            if (CollectionUtils.isEmpty(headerMap))
                throw new Exception(PARTICIPANT_ID_MISSING_PARTICIPANT_ID_DATA);
            final long participantIDCount = headerMap.entrySet().stream().filter(x -> x.getKey().equals(ParticipantID_header)).count();

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
        }
    }

    public static List<SubjectTransferBean> populateCSVColumns(CSVParser parser) throws Exception {
        List<SubjectTransferBean> transferBeans = new ArrayList<>();
        Map<String, Integer> headerMap = parser.getHeaderMap();
        BidiMap<String, Integer> bidiMap = new DualHashBidiMap();
        bidiMap.putAll(headerMap);
        try {
            parser.getRecords().forEach(record -> {
                SubjectTransferBean transferBean = new SubjectTransferBean();
                int headerIndex = 0;
                Iterator<String> iterator = record.iterator();
                while (iterator.hasNext()) {
                    String column = iterator.next();
                    if (StringUtils.isNotEmpty(column)) {
                        switch (EnumUtils.getEnum(PIIEnum.class, bidiMap.getKey(headerIndex))) {
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
                        }
                    }
                    ++headerIndex;
                }
                transferBeans.add(transferBean);
            });
        } catch (IOException e) {
            log.error("Exception reading file:" + e);
            throw e;
        }
        return transferBeans;
    }

    public  List<SubjectTransferBean> readCSVFile(MultipartFile file) throws Exception {

        List<SubjectTransferBean> subjectKeyList = null;

        try {
            BufferedReader reader;

            String line;
            InputStream is = file.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is));

            //Create the CSVFormat object with the header mapping
            CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader().withTrim();
            CSVParser csvParser = null;
            csvParser = new CSVParser(reader, csvFileFormat);
            subjectKeyList = populateCSVColumns(csvParser);
        } catch (Exception e) {
            String message = " This CSV format is not supported ";
            if (StringUtils.isNotEmpty(e.getMessage()))
                message = e.getMessage();
            throw new Exception(message);
        }

        return subjectKeyList;
    }

    /**
     * @param file
     * @return
     * @throws IOException
     */
    private static ArrayList<String> readFile(MultipartFile file) throws IOException {

        ArrayList<String> subjectKeyList = new ArrayList<>();

        try(Scanner sc = new Scanner(file.getInputStream())){

            String line;

            int lineNm = 1;
            int position = 0;

            while (sc.hasNextLine()) {
                line = sc.nextLine();
                String[] lineVal= line.split(",", 0);

                // check ParticipantID column number
                if(lineNm ==1) {

                    for(int i=0; i < lineVal.length;i++) {
                        lineVal.equals(ParticipantID_header);
                        position = i;

                        break;
                    }
                }else {
                    subjectKeyList.add(lineVal[position]);
                }



                lineNm++;
            }

        } catch (Exception e) {
            log.error("Exception with cause = {} {}", e.getCause(), e.getMessage());
        }

        return subjectKeyList;
    }
}
