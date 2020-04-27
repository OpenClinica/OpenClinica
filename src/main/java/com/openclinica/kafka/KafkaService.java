package com.openclinica.kafka;

import com.openclinica.kafka.dto.*;
import core.org.akaza.openclinica.domain.datamap.*;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaService {
  @Autowired
  private KafkaTemplate kafkaTemplate;

  public void sendFormChangeMessage(FormChangeDTO formChangeDTO) throws Exception {
    Headers headers = new RecordHeaders();
    headers.add(new Header() {
      @Override
      public String key() {
        return "__TypeId__";
      }

      @Override
      public byte[] value() {
        return "com.openclinica.kafka.dto.FormChangeDTO".getBytes();
      }
    });
    ProducerRecord producerRecord = new ProducerRecord(KafkaConfig.FORM_CHANGE_TOPIC, null, null, null, formChangeDTO, headers);
    kafkaTemplate.send(producerRecord);
  }

  public void sendEventAttributeChangeMessage(String customerUuid, EventCrf eventCrf) throws Exception {
    EventAttributeChangeDTO eventAttributeChangeDTO = new EventAttributeChangeDTO();

    Headers headers = new RecordHeaders();
    headers.add(new Header() {
      @Override
      public String key() {
        return "__TypeId__";
      }

      @Override
      public byte[] value() {
        return "com.openclinica.kafka.dto.EventAttributeChangeDTO".getBytes();
      }
    });
    ProducerRecord producerRecord = new ProducerRecord(KafkaConfig.EVENT_ATTRIBUTE_CHANGE_TOPIC, null, null, null, eventAttributeChangeDTO, headers);
    kafkaTemplate.send(producerRecord);
  }

  public void sendStudyPublishMessage(String customerUuid, Study study) throws Exception {
    StudyPublishDTO studyPublishDTO = new StudyPublishDTO();

    studyPublishDTO.setCustomerUuid(customerUuid);
    studyPublishDTO.setStudyUuid(study.getStudyUuid());
    studyPublishDTO.setStudyEnvironmentUuid(study.getStudyEnvUuid());

    Headers headers = new RecordHeaders();
    headers.add(new Header() {
      @Override
      public String key() {
        return "__TypeId__";
      }

      @Override
      public byte[] value() {
        return "com.openclinica.kafka.dto.StudyPublishDTO".getBytes();
      }
    });
    ProducerRecord producerRecord = new ProducerRecord(KafkaConfig.STUDY_PUBLISH_TOPIC, null, null, null, studyPublishDTO, headers);
    kafkaTemplate.send(producerRecord);
  }

  // Removed temporarily until ODM updates are in.
  public void sendItemDataChangeMessage(ItemData itemData) throws Exception {

/*    Study study = itemData.getEventCrf().getStudySubject().getStudy();
    ItemDataChangeDTO itemDataChangeDTO = new ItemDataChangeDTO();

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

    itemDataChangeDTO.setCustomerUuid(CoreResources.getKeyCloakConfig().getRealm());
    itemDataChangeDTO.setStudyOid(studyOid);
    itemDataChangeDTO.setSiteOid(siteOid);
    itemDataChangeDTO.setParticipantId(itemData.getEventCrf().getStudySubject().getLabel());
    itemDataChangeDTO.setParticipantOid(itemData.getEventCrf().getStudySubject().getOcOid());
    itemDataChangeDTO.setFormOid(itemData.getEventCrf().getFormLayout().getCrf().getOcOid());
    itemDataChangeDTO.setEventOid(itemData.getEventCrf().getStudyEvent().getStudyEventDefinition().getOc_oid());

    itemDataChangeDTO.setItemGroupOid(itemData.getItem().getItemGroupMetadatas().get(0).getItemGroup().getOcOid());
    itemDataChangeDTO.setItemDataType(itemData.getItem().getItemDataType().getName());
    itemDataChangeDTO.setItemName(itemData.getItem().getName());
    itemDataChangeDTO.setItemOid(itemData.getItem().getOcOid());
    itemDataChangeDTO.setItemData(itemData.getValue());

    Headers headers = new RecordHeaders();
    headers.add(new Header() {
      @Override
      public String key() {
        return "__TypeId__";
      }

      @Override
      public byte[] value() {
        return "com.openclinica.kafka.dto.ItemDataChangeDTO".getBytes();
      }
    });
    ProducerRecord producerRecord = new ProducerRecord(KafkaConfig.ITEM_DATA_CHANGE_TOPIC, null, null, null, itemDataChangeDTO, headers);
    kafkaTemplate.send(producerRecord);*/
  }

  public FormChangeDTO constructEditFormDTO(String customerUuid, EventCrf eventCrf){
    FormChangeDTO formChangeDTO = new FormChangeDTO();

    Study study = eventCrf.getStudySubject().getStudy();
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
    formChangeDTO.setParticipantId(eventCrf.getStudySubject().getLabel());
    formChangeDTO.setParticipantOid(eventCrf.getStudySubject().getOcOid());
    formChangeDTO.setFormOid(eventCrf.getFormLayout().getCrf().getOcOid());
    formChangeDTO.setEventOid(eventCrf.getStudyEvent().getStudyEventDefinition().getOc_oid());

    return formChangeDTO;
  }

  public FormChangeDTO constructNewFormDTO(String customerUuid, Study currentStudy, StudyEvent studyEvent, FormLayout formLayout){
    FormChangeDTO formChangeDTO = new FormChangeDTO();

    String studyOid;
    String siteOid;
    String studyUuid;
    String studyEnvUuid;
    if (currentStudy.getStudy() == null) {
      // Study-level
      studyOid = currentStudy.getOc_oid();
      siteOid = null;
      studyUuid = currentStudy.getStudyUuid();
      studyEnvUuid = currentStudy.getStudyEnvUuid();
    } else {
      // Site-level
      siteOid = currentStudy.getOc_oid();
      studyOid = currentStudy.getStudy().getOc_oid();
      studyUuid = currentStudy.getStudy().getStudyUuid();
      studyEnvUuid = currentStudy.getStudy().getStudyEnvUuid();
    }

    formChangeDTO.setCustomerUuid(customerUuid);
    formChangeDTO.setStudyUuid(studyUuid);
    formChangeDTO.setStudyEnvironmentUuid(studyEnvUuid);
    formChangeDTO.setStudyOid(studyOid);
    formChangeDTO.setSiteOid(siteOid);
    formChangeDTO.setParticipantId(studyEvent.getStudySubject().getLabel());
    formChangeDTO.setParticipantOid(studyEvent.getStudySubject().getOcOid());
    formChangeDTO.setFormOid(formLayout.getCrf().getOcOid());
    formChangeDTO.setEventOid(studyEvent.getStudyEventDefinition().getOc_oid());

    return formChangeDTO;
  }

}
