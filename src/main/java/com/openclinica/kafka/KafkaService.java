package com.openclinica.kafka;

import com.openclinica.kafka.dto.*;
import core.org.akaza.openclinica.bean.managestudy.StudyEventBean;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.bean.submit.EventCRFBean;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.hibernate.*;
import core.org.akaza.openclinica.domain.datamap.*;
import core.org.akaza.openclinica.service.UtilService;
import org.akaza.openclinica.service.CoreUtilServiceImpl;
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
  @Autowired
  private StudySubjectDao studySubjectDao;
  @Autowired
  StudyEventDao studyEventDao;
  @Autowired
  private StudyEventDefinitionDao studyEventDefinitionDao;
  @Autowired
  private CoreUtilServiceImpl coreUtilService;

  public void sendFormChangeMessage(FormChangeDTO formChangeDTO) throws Exception {
    Headers headers = buildHeaders("com.openclinica.kafka.dto.FormChangeDTO");

    ProducerRecord producerRecord = new ProducerRecord(KafkaConfig.FORM_CHANGE_TOPIC, null, null, null, formChangeDTO, headers);
    kafkaTemplate.send(producerRecord);
  }

  public void sendFormAttributeChangeMessage(EventCrf eventCrf) throws Exception {
    Headers headers = buildHeaders("com.openclinica.kafka.dto.FormChangeDTO");

    FormChangeDTO formAttributeChangeDTO = constructEditFormDTO(eventCrf);

    ProducerRecord producerRecord = new ProducerRecord(KafkaConfig.FORM_ATTRIBUTE_CHANGE_TOPIC, null, null, null, formAttributeChangeDTO, headers);
    kafkaTemplate.send(producerRecord);
  }

  public void sendFormAttributeChangeMessage(EventCRFBean eventCrfBean) throws Exception {
    Headers headers = buildHeaders("com.openclinica.kafka.dto.FormChangeDTO");

    FormChangeDTO formAttributeChangeDTO = constructEventCrfAttributeChangeDTO(eventCrfBean);

    ProducerRecord producerRecord = new ProducerRecord(KafkaConfig.FORM_ATTRIBUTE_CHANGE_TOPIC, null, null, null, formAttributeChangeDTO, headers);
    kafkaTemplate.send(producerRecord);
  }

  public void sendEventAttributeChangeMessage(StudyEventBean studyEventBean) throws Exception {
    Headers headers = buildHeaders("com.openclinica.kafka.dto.EventAttributeChangeDTO");

    EventAttributeChangeDTO eventAttributeChangeDTO = constructEventChangeDTO(studyEventBean);

    ProducerRecord producerRecord = new ProducerRecord(KafkaConfig.EVENT_ATTRIBUTE_CHANGE_TOPIC, null, null, null, eventAttributeChangeDTO, headers);
    kafkaTemplate.send(producerRecord);
  }

  public void sendEventAttributeChangeMessage(StudyEvent studyEvent) throws Exception {
    Headers headers = buildHeaders("com.openclinica.kafka.dto.EventAttributeChangeDTO");

    EventAttributeChangeDTO eventAttributeChangeDTO = constructEventChangeDTO(studyEvent);

    ProducerRecord producerRecord = new ProducerRecord(KafkaConfig.EVENT_ATTRIBUTE_CHANGE_TOPIC, null, null, null, eventAttributeChangeDTO, headers);
    kafkaTemplate.send(producerRecord);
  }

  public void sendItemDataChangeMessage(ItemData itemData) throws Exception {

    ItemDataChangeDTO itemDataChangeDTO = constructItemDataChangeDTO(itemData);

    Headers headers = buildHeaders("com.openclinica.kafka.dto.ItemDataChangeDTO");
    ProducerRecord producerRecord = new ProducerRecord(KafkaConfig.ITEM_DATA_CHANGE_TOPIC, null, null, null, itemDataChangeDTO, headers);
    kafkaTemplate.send(producerRecord);
  }

  public void sendStudyPublishMessage(Study study) throws Exception {
    StudyPublishDTO studyPublishDTO = new StudyPublishDTO();

    String customerUuid = coreUtilService.getCustomerUuid();

    studyPublishDTO.setCustomerUuid(customerUuid);
    studyPublishDTO.setStudyUuid(study.getStudyUuid());
    studyPublishDTO.setStudyEnvironmentUuid(study.getStudyEnvUuid());

    Headers headers = buildHeaders("com.openclinica.kafka.dto.StudyPublishDTO");
    ProducerRecord producerRecord = new ProducerRecord(KafkaConfig.STUDY_PUBLISH_TOPIC, null, null, null, studyPublishDTO, headers);
    kafkaTemplate.send(producerRecord);
  }

  private Headers buildHeaders(String dtoType){
    Headers headers = new RecordHeaders();
    headers.add(new Header() {
      @Override
      public String key() {
        return "__TypeId__";
      }

      @Override
      public byte[] value() {
        return dtoType.getBytes();
      }
    });
    return headers;
  }

  private ItemDataChangeDTO constructItemDataChangeDTO(ItemData itemData){
    ItemDataChangeDTO itemDataChangeDTO = new ItemDataChangeDTO();

    Study study = itemData.getEventCrf().getStudySubject().getStudy();
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
    itemDataChangeDTO.setEventRepeatKey(itemData.getEventCrf().getStudyEvent().getSampleOrdinal());

    itemDataChangeDTO.setItemGroupOid(itemData.getItem().getItemGroupMetadatas().get(0).getItemGroup().getOcOid());
    itemDataChangeDTO.setItemGroupRepeatKey(itemData.getOrdinal());

    itemDataChangeDTO.setItemDataType(itemData.getItem().getItemDataType().getName());
    itemDataChangeDTO.setItemName(itemData.getItem().getName());
    itemDataChangeDTO.setItemOid(itemData.getItem().getOcOid());
    itemDataChangeDTO.setItemData(itemData.getValue());

    return itemDataChangeDTO;
  }

  private EventAttributeChangeDTO constructEventChangeDTO(StudyEventBean studyEventBean){
    EventAttributeChangeDTO formChangeDTO = new EventAttributeChangeDTO();
    StudySubject studySubject = studySubjectDao.findById(studyEventBean.getStudySubjectId());
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

    String customerUuid = coreUtilService.getCustomerUuid();

    formChangeDTO.setCustomerUuid(customerUuid);
    formChangeDTO.setStudyUuid(studyUuid);
    formChangeDTO.setStudyEnvironmentUuid(studyEnvUuid);
    formChangeDTO.setStudyOid(studyOid);
    formChangeDTO.setSiteOid(siteOid);

    formChangeDTO.setParticipantId(studySubject.getLabel());
    formChangeDTO.setParticipantOid(studySubject.getOcOid());
    if (studyEventBean.getStudyEventDefinitionId() != 0){
      StudyEventDefinition studyEventDefinition = studyEventDefinitionDao.findByStudyEventDefinitionId(studyEventBean.getStudyEventDefinitionId());
      formChangeDTO.setEventOid(studyEventDefinition.getOc_oid());
    }

    return formChangeDTO;
  }

  private EventAttributeChangeDTO constructEventChangeDTO(StudyEvent studyEvent){
    EventAttributeChangeDTO formChangeDTO = new EventAttributeChangeDTO();
    StudySubject studySubject = studySubjectDao.findById(studyEvent.getStudySubject().getStudySubjectId());
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

    String customerUuid = coreUtilService.getCustomerUuid();

    formChangeDTO.setCustomerUuid(customerUuid);
    formChangeDTO.setStudyUuid(studyUuid);
    formChangeDTO.setStudyEnvironmentUuid(studyEnvUuid);
    formChangeDTO.setStudyOid(studyOid);
    formChangeDTO.setSiteOid(siteOid);

    formChangeDTO.setParticipantId(studySubject.getLabel());
    formChangeDTO.setParticipantOid(studySubject.getOcOid());
    if (studyEvent.getStudyEventDefinition().getOc_oid() != null){
      formChangeDTO.setEventOid(studyEvent.getStudyEventDefinition().getOc_oid());
    }

    return formChangeDTO;
  }

  public FormChangeDTO constructEventCrfAttributeChangeDTO(EventCRFBean eventCrfBean){
    FormChangeDTO formChangeDTO = new FormChangeDTO();

    StudyEventDefinition studyEventDefinition = studyEventDao.findById(eventCrfBean.getStudyEventId()).getStudyEventDefinition();
    StudySubject studySubject = studySubjectDao.findById(eventCrfBean.getStudySubjectId());
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
    String customerUuid = coreUtilService.getCustomerUuid();

    formChangeDTO.setCustomerUuid(customerUuid);
    formChangeDTO.setStudyUuid(studyUuid);
    formChangeDTO.setStudyEnvironmentUuid(studyEnvUuid);
    formChangeDTO.setStudyOid(studyOid);
    formChangeDTO.setSiteOid(siteOid);
    formChangeDTO.setParticipantId(String.valueOf(studySubject.getLabel()));
    formChangeDTO.setParticipantOid(studySubject.getOcOid());
    formChangeDTO.setFormOid(eventCrfBean.getCrf().getOid());
    formChangeDTO.setEventOid(studyEventDefinition.getOc_oid());

    return formChangeDTO;
  }

  public FormChangeDTO constructEditFormDTO(EventCrf eventCrf){
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
    String customerUuid = coreUtilService.getCustomerUuid();

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

  //TODO This is crashing if I open/view from a non-participant source since the study subject is null.
  public FormChangeDTO constructNewFormDTO(Study currentStudy, StudyEvent studyEvent, FormLayout formLayout){
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

    String customerUuid = coreUtilService.getCustomerUuid();

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
