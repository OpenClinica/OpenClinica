package com.openclinica.kafka;

import com.openclinica.kafka.dto.EventAttributeChangeDTO;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.datamap.StudySubject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class KafkaDTOService {

    @Autowired
    private StudySubjectDao studySubjectDao;

    public EventAttributeChangeDTO constructEventChangeDTO(String customerUuid, String eventOid, StudySubjectBean studySubjectBean){
        EventAttributeChangeDTO formChangeDTO = new EventAttributeChangeDTO();
        StudySubject studySubject = studySubjectDao.findById(studySubjectBean.getId());
        Study study = studySubject.getStudy();
        String studyOid;
        String siteOid;
        String studyUuid;
        String studyEnvUuid;
        if (study.getStudy() == null) {
            // Study-level
            studyOid = study.getOc_oid();
            siteOid = null;
            studyUuid = study.getStudyUuid();
            studyEnvUuid = study.getStudyEnvUuid();
        } else {
            // Site-level
            siteOid = study.getOc_oid();
            studyOid = study.getStudy().getOc_oid();
            studyUuid = study.getStudy().getStudyUuid();
            studyEnvUuid = study.getStudy().getStudyEnvUuid();
        }

        formChangeDTO.setCustomerUuid(customerUuid);
        formChangeDTO.setStudyUuid(studyUuid);
        formChangeDTO.setStudyEnvironmentUuid(studyEnvUuid);
        formChangeDTO.setStudyOid(studyOid);
        formChangeDTO.setSiteOid(siteOid);

        formChangeDTO.setParticipantId(studySubject.getLabel());
        formChangeDTO.setParticipantOid(studySubject.getOcOid());
        // Could this be optional, in the event of scheduling multiple events?
        if (eventOid != null){
            formChangeDTO.setEventOid(eventOid);
        }

        return formChangeDTO;
    }
}
