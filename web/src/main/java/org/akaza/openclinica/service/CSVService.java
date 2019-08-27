package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.managestudy.SubjectTransferBean;
import org.akaza.openclinica.controller.dto.AddParticipantRequestDTO;
import org.akaza.openclinica.controller.dto.StudyEventScheduleDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

public interface CSVService {
    List<SubjectTransferBean> readBulkParticipantCSVFile(MultipartFile file) throws Exception;
    void validateBulkParticipantCSVFile(MultipartFile file) throws Exception;
    List<StudyEventScheduleDTO> readStudyEventScheduleBulkCSVFile(MultipartFile file, String studyOID, String siteOID) throws Exception;
    List<AddParticipantRequestDTO> readAddParticipantBulkCSVFile(MultipartFile file, String studyOID, String siteOID) throws Exception ;


    void validateCSVFileHeaderForScheduleEvents(MultipartFile file, String studyOID, String siteOID)throws Exception ;
    void validateCSVFileHeaderForAddParticipants(MultipartFile file, String studyOID, String siteOID)throws Exception ;

}
