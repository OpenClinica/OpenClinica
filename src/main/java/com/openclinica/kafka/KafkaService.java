package com.openclinica.kafka;

import core.org.akaza.openclinica.domain.datamap.EventCrf;
import core.org.akaza.openclinica.domain.datamap.Study;
import com.openclinica.kafka.dto.FormStatusChangeDTO;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class KafkaService {
  @Autowired
  private KafkaTemplate kafkaTemplate;

  public void sendFormCompleteMessage(EventCrf eventCrf) throws Exception {
    Study study = eventCrf.getStudySubject().getStudy();
    FormStatusChangeDTO formStatusChangeDTO = new FormStatusChangeDTO();
    String studyOid;
    String siteOid;
    if (study.getStudy() == null) {
      // Study-level
      studyOid = study.getOc_oid();
      siteOid = null;
    } else {
      // Site-level
      siteOid = study.getOc_oid();
      studyOid = study.getStudy().getOc_oid();
    }
    formStatusChangeDTO.setStudyOid(studyOid);
    formStatusChangeDTO.setSiteOid(siteOid);
    formStatusChangeDTO.setParticipantId(eventCrf.getStudySubject().getLabel());
    formStatusChangeDTO.setParticipantOid(eventCrf.getStudySubject().getOcOid());
    formStatusChangeDTO.setFormOid(eventCrf.getFormLayout().getCrf().getOcOid());
    formStatusChangeDTO.setEventOid(eventCrf.getStudyEvent().getStudyEventDefinition().getOc_oid());
    Headers headers = new RecordHeaders();
    headers.add(new Header() {
      @Override
      public String key() {
        return "__TypeId__";
      }

      @Override
      public byte[] value() {
        return "com.openclinica.kafka.dto.FormStatusChangeDTO".getBytes();
      }
    });
    ProducerRecord producerRecord = new ProducerRecord(KafkaConfig.FORM_STATUS_CHANGE_TOPIC, null, null, null, formStatusChangeDTO, headers);
    kafkaTemplate.send(producerRecord);
  }

}
