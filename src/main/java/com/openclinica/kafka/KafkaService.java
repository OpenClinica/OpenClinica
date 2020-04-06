package com.openclinica.kafka;

import com.openclinica.kafka.dto.ItemDataChangeDTO;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.domain.datamap.EventCrf;
import core.org.akaza.openclinica.domain.datamap.ItemData;
import core.org.akaza.openclinica.domain.datamap.Study;
import com.openclinica.kafka.dto.FormStatusChangeDTO;
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

  public void sendFormCompleteMessage(String customerUuid, EventCrf eventCrf) throws Exception {
    Study study = eventCrf.getStudySubject().getStudy();
    FormStatusChangeDTO formStatusChangeDTO = new FormStatusChangeDTO();
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

    formStatusChangeDTO.setCustomerUuid(customerUuid);
    formStatusChangeDTO.setStudyUuid(studyUuid);
    formStatusChangeDTO.setStudyEnvironmentUuid(studyEnvUuid);
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

  // Removed for now. Will likely be removed?
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

}
