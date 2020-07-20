package com.openclinica.kafka;

import com.openclinica.kafka.dto.EventAttributeChangeDTO;
import com.openclinica.kafka.dto.FormChangeDTO;
import com.openclinica.kafka.dto.ItemDataChangeDTO;
import com.openclinica.kafka.dto.StudyPublishDTO;
import core.org.akaza.openclinica.bean.managestudy.StudyEventBean;
import core.org.akaza.openclinica.bean.submit.EventCRFBean;
import core.org.akaza.openclinica.bean.submit.ItemDataBean;
import core.org.akaza.openclinica.dao.hibernate.*;
import core.org.akaza.openclinica.domain.datamap.*;
import core.org.akaza.openclinica.domain.user.UserAccount;
import core.org.akaza.openclinica.ocobserver.StudyEventContainer;
import org.akaza.openclinica.domain.enumsupport.EventCrfWorkflowStatusEnum;
import org.akaza.openclinica.service.CoreUtilServiceImpl;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class KafkaService {
    @Autowired
    UserAccountDao userAccountDao;
    @Autowired
    StudyEventDao studyEventDao;
    @Autowired
    EventCrfDao eventCrfDao;
    @Autowired
    ItemDao itemDao;
    @Autowired
    CrfDao crfDao;
    @Autowired
    CrfVersionDao crfVersionDao;
    @Autowired
    private KafkaTemplate kafkaTemplate;
    @Autowired
    private StudySubjectDao studySubjectDao;
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

        FormChangeDTO formAttributeChangeDTO = constructEventCrfAttributeChangeDTO(eventCrf);

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

    public void sendEventAttributeChangeMessage(StudyEventContainer studyEventContainer) throws Exception {
        Headers headers = buildHeaders("com.openclinica.kafka.dto.EventAttributeChangeDTO");

        EventAttributeChangeDTO eventAttributeChangeDTO = constructEventChangeDTO(studyEventContainer.getEvent());

        ProducerRecord producerRecord = new ProducerRecord(KafkaConfig.EVENT_ATTRIBUTE_CHANGE_TOPIC, null, null, null, eventAttributeChangeDTO, headers);
        kafkaTemplate.send(producerRecord);
    }

    public void sendItemDataChangeMessage(ItemData itemData) throws Exception {
        ItemDataChangeDTO itemDataChangeDTO = constructItemDataChangeDTO(itemData);

        Headers headers = buildHeaders("com.openclinica.kafka.dto.ItemDataChangeDTO");
        ProducerRecord producerRecord = new ProducerRecord(KafkaConfig.ITEM_DATA_CHANGE_TOPIC, null, null, null, itemDataChangeDTO, headers);
        kafkaTemplate.send(producerRecord);
    }

    public void sendItemDataChangeMessage(ItemDataBean itemDataBean) throws Exception {
        ItemDataChangeDTO itemDataChangeDTO = constructItemDataChangeDTO(itemDataBean);

        Headers headers = buildHeaders("com.openclinica.kafka.dto.ItemDataChangeDTO");
        ProducerRecord producerRecord = new ProducerRecord(KafkaConfig.ITEM_DATA_CHANGE_TOPIC, null, null, null, itemDataChangeDTO, headers);
        kafkaTemplate.send(producerRecord);
    }

    public void sendStudyPublishMessage(Study study) throws Exception {
        StudyPublishDTO studyPublishDTO = new StudyPublishDTO();

        studyPublishDTO.setCustomerUuid(coreUtilService.getCustomerUuid());
        studyPublishDTO.setStudyUuid(study.getStudyUuid());
        studyPublishDTO.setStudyEnvironmentUuid(study.getStudyEnvUuid());

        Headers headers = buildHeaders("com.openclinica.kafka.dto.StudyPublishDTO");
        ProducerRecord producerRecord = new ProducerRecord(KafkaConfig.STUDY_PUBLISH_TOPIC, null, null, null, studyPublishDTO, headers);
        kafkaTemplate.send(producerRecord);
    }

    private Headers buildHeaders(String dtoType) {
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

    private ItemDataChangeDTO constructItemDataChangeDTO(ItemData itemData) {
        ItemDataChangeDTO itemDataChangeDTO = new ItemDataChangeDTO();

        Study study = itemData.getEventCrf().getStudySubject().getStudy();

        itemDataChangeDTO.setCustomerUuid(coreUtilService.getCustomerUuid());
        itemDataChangeDTO.setStudyUuid(getStudyUuid(study));
        itemDataChangeDTO.setStudyEnvironmentUuid(getStudyEnvUuid(study));
        itemDataChangeDTO.setStudyOid(getStudyOid(study));
        itemDataChangeDTO.setSiteOid(getSiteOid(study));
        itemDataChangeDTO.setParticipantId(itemData.getEventCrf().getStudySubject().getLabel());
        itemDataChangeDTO.setParticipantOid(itemData.getEventCrf().getStudySubject().getOcOid());

        itemDataChangeDTO.setEventOid(itemData.getEventCrf().getStudyEvent().getStudyEventDefinition().getOc_oid());
        itemDataChangeDTO.setEventRepeatKey(getEventRepeatKey(itemData.getEventCrf().getStudyEvent()));

        itemDataChangeDTO.setFormOid(itemData.getEventCrf().getFormLayout().getCrf().getOcOid());

        itemDataChangeDTO.setItemGroupOid(itemData.getItem().getItemGroupMetadatas().get(0).getItemGroup().getOcOid());
        itemDataChangeDTO.setItemGroupRepeatKey(itemData.getOrdinal());

        itemDataChangeDTO.setItemDataType(itemData.getItem().getItemDataType().getName());
        itemDataChangeDTO.setItemName(itemData.getItem().getName());
        itemDataChangeDTO.setItemOid(itemData.getItem().getOcOid());
        itemDataChangeDTO.setItemData(itemData.getValue());

        return itemDataChangeDTO;
    }

    private ItemDataChangeDTO constructItemDataChangeDTO(ItemDataBean itemDataBean) {
        ItemDataChangeDTO itemDataChangeDTO = new ItemDataChangeDTO();

        EventCrf eventCrf = eventCrfDao.findById(itemDataBean.getEventCRFId());
        Item item = itemDao.findById(itemDataBean.getItemId());
        Study study = eventCrf.getStudySubject().getStudy();

        itemDataChangeDTO.setCustomerUuid(coreUtilService.getCustomerUuid());
        itemDataChangeDTO.setStudyUuid(getStudyUuid(study));
        itemDataChangeDTO.setStudyEnvironmentUuid(getStudyEnvUuid(study));
        itemDataChangeDTO.setStudyOid(getStudyOid(study));
        itemDataChangeDTO.setSiteOid(getSiteOid(study));
        itemDataChangeDTO.setParticipantId(eventCrf.getStudySubject().getLabel());
        itemDataChangeDTO.setParticipantOid(eventCrf.getStudySubject().getOcOid());

        itemDataChangeDTO.setEventOid(eventCrf.getStudyEvent().getStudyEventDefinition().getOc_oid());
        itemDataChangeDTO.setEventRepeatKey(getEventRepeatKey(eventCrf.getStudyEvent()));

        itemDataChangeDTO.setFormOid(eventCrf.getFormLayout().getCrf().getOcOid());

        itemDataChangeDTO.setItemGroupOid(item.getItemGroupMetadatas().get(0).getItemGroup().getOcOid());
        itemDataChangeDTO.setItemGroupRepeatKey(itemDataBean.getOrdinal());

        itemDataChangeDTO.setItemDataType(item.getItemDataType().getName());
        itemDataChangeDTO.setItemName(item.getName());
        itemDataChangeDTO.setItemOid(item.getOcOid());
        itemDataChangeDTO.setItemData(itemDataBean.getValue());

        return itemDataChangeDTO;
    }

    private EventAttributeChangeDTO constructEventChangeDTO(StudyEventBean studyEventBean) {
        EventAttributeChangeDTO eventAttributeChangeDTO = new EventAttributeChangeDTO();
        StudySubject studySubject = studySubjectDao.findById(studyEventBean.getStudySubjectId());
        Study study = studySubject.getStudy();

        eventAttributeChangeDTO.setCustomerUuid(coreUtilService.getCustomerUuid());
        eventAttributeChangeDTO.setStudyUuid(getStudyUuid(study));
        eventAttributeChangeDTO.setStudyEnvironmentUuid(getStudyEnvUuid(study));
        eventAttributeChangeDTO.setStudyOid(getStudyOid(study));
        eventAttributeChangeDTO.setSiteOid(getSiteOid(study));

        eventAttributeChangeDTO.setParticipantId(studySubject.getLabel());
        eventAttributeChangeDTO.setParticipantOid(studySubject.getOcOid());

        if (studyEventBean.getStudyEventDefinitionId() != 0) {
            StudyEventDefinition studyEventDefinition = studyEventDefinitionDao.findByStudyEventDefinitionId(studyEventBean.getStudyEventDefinitionId());
            eventAttributeChangeDTO.setEventOid(studyEventDefinition.getOc_oid());
            eventAttributeChangeDTO.setEventRepeatKey(getEventRepeatKey(studyEventBean));
        }

        eventAttributeChangeDTO.setEventWorkflowStatus(studyEventBean.getWorkflowStatus().getEnglishDisplayValue());

        if (studyEventBean.getCreatedDate() != null){
            eventAttributeChangeDTO.setEventStartDate(studyEventBean.getCreatedDate().toString());
        }
        if (studyEventBean.getRemoved() != null){
            eventAttributeChangeDTO.setEventRemoved(studyEventBean.getRemoved().toString());
        }
        if (studyEventBean.getArchived() != null){
            eventAttributeChangeDTO.setEventArchived(studyEventBean.getArchived().toString());
        }
        if (studyEventBean.getLocked() != null){
            eventAttributeChangeDTO.setEventLocked(studyEventBean.getLocked().toString());
        }
        if (studyEventBean.getSigned() != null){
            eventAttributeChangeDTO.setEventSigned(studyEventBean.getSigned().toString());
        }
        return eventAttributeChangeDTO;
    }

    private EventAttributeChangeDTO constructEventChangeDTO(StudyEvent studyEvent) {
        EventAttributeChangeDTO eventAttributeChangeDTO = new EventAttributeChangeDTO();
        StudySubject studySubject = studySubjectDao.findById(studyEvent.getStudySubject().getStudySubjectId());
        Study study = studySubject.getStudy();

        eventAttributeChangeDTO.setCustomerUuid(coreUtilService.getCustomerUuid());
        eventAttributeChangeDTO.setStudyUuid(getStudyUuid(study));
        eventAttributeChangeDTO.setStudyEnvironmentUuid(getStudyEnvUuid(study));
        eventAttributeChangeDTO.setStudyOid(getStudyOid(study));
        eventAttributeChangeDTO.setSiteOid(getSiteOid(study));

        eventAttributeChangeDTO.setParticipantId(studySubject.getLabel());
        eventAttributeChangeDTO.setParticipantOid(studySubject.getOcOid());

        if (studyEvent.getStudyEventDefinition().getOc_oid() != null) {
            eventAttributeChangeDTO.setEventOid(studyEvent.getStudyEventDefinition().getOc_oid());
        }
        eventAttributeChangeDTO.setEventRepeatKey(getEventRepeatKey(studyEvent));
        if (studyEvent.getDateCreated() != null){
            eventAttributeChangeDTO.setEventStartDate(studyEvent.getDateCreated().toString());
        }
        eventAttributeChangeDTO.setEventWorkflowStatus(studyEvent.getWorkflowStatus().name());
        if (studyEvent.getRemoved() != null){
            eventAttributeChangeDTO.setEventRemoved(studyEvent.getRemoved().toString());
        }
        if (studyEvent.getArchived() != null){
            eventAttributeChangeDTO.setEventArchived(studyEvent.getArchived().toString());
        }
        if (studyEvent.getLocked() != null){
            eventAttributeChangeDTO.setEventLocked(studyEvent.getLocked().toString());
        }
        if (studyEvent.getSigned() != null){
            eventAttributeChangeDTO.setEventSigned(studyEvent.getSigned().toString());
        }

        return eventAttributeChangeDTO;
    }

    public FormChangeDTO constructEventCrfAttributeChangeDTO(EventCRFBean eventCrfBean) {
        FormChangeDTO formChangeDTO = new FormChangeDTO();

        UserAccount creatorAccount = userAccountDao.findByUserId(eventCrfBean.getOwnerId());
        UserAccount updaterAccount = userAccountDao.findByUserId(eventCrfBean.getUpdaterId());

        StudyEvent studyEvent = studyEventDao.findByStudyEventId(eventCrfBean.getStudyEventId());
        StudyEventDefinition studyEventDefinition = studyEvent.getStudyEventDefinition();
        StudySubject studySubject = studySubjectDao.findById(eventCrfBean.getStudySubjectId());
        Study study = studySubject.getStudy();

        formChangeDTO.setCustomerUuid(coreUtilService.getCustomerUuid());
        formChangeDTO.setStudyUuid(getStudyUuid(study));
        formChangeDTO.setStudyEnvironmentUuid(getStudyEnvUuid(study));
        formChangeDTO.setStudyOid(getStudyOid(study));
        formChangeDTO.setSiteOid(getSiteOid(study));

        formChangeDTO.setParticipantId(String.valueOf(studySubject.getLabel()));
        formChangeDTO.setParticipantOid(studySubject.getOcOid());

        formChangeDTO.setEventOid(studyEventDefinition.getOc_oid());
        formChangeDTO.setEventRepeatKey(getEventRepeatKey(studyEvent));

        String formOid = crfVersionDao.findByCrfVersionId(eventCrfBean.getCRFVersionId()).getCrf().getOcOid();
        formChangeDTO.setFormOid(formOid);

        formChangeDTO.setFormCreatedDate(eventCrfBean.getCreatedDate().toString());
        formChangeDTO.setFormUpdatedDate(eventCrfBean.getUpdatedDate().toString());

        formChangeDTO.setFormCreatedBy(creatorAccount.getUserName());
        formChangeDTO.setFormUpdatedBy(updaterAccount.getUserName());

        formChangeDTO.setFormWorkflowStatus(eventCrfBean.getWorkflowStatus().getEnglishDisplayValue());
        if (eventCrfBean.getSdvStatus() != null){
            formChangeDTO.setFormSdvStatus(eventCrfBean.getSdvStatus().getEnglishDisplayValue());}
        if (eventCrfBean.getRemoved() != null){
        formChangeDTO.setFormRemoved(eventCrfBean.getRemoved().toString());}
        if (eventCrfBean.getArchived() != null){
        formChangeDTO.setFormArchived(eventCrfBean.getArchived().toString());}

        return formChangeDTO;
    }

    public FormChangeDTO constructEventCrfAttributeChangeDTO(EventCrf eventCrf) {
        FormChangeDTO formChangeDTO = new FormChangeDTO();

        UserAccount updaterAccount = userAccountDao.findByUserId(eventCrf.getUpdateId());
        Study study = eventCrf.getStudySubject().getStudy();

        formChangeDTO.setCustomerUuid(coreUtilService.getCustomerUuid());
        formChangeDTO.setStudyUuid(getStudyUuid(study));
        formChangeDTO.setStudyEnvironmentUuid(getStudyEnvUuid(study));
        formChangeDTO.setStudyOid(getStudyOid(study));
        formChangeDTO.setSiteOid(getSiteOid(study));

        formChangeDTO.setParticipantId(eventCrf.getStudySubject().getLabel());
        formChangeDTO.setParticipantOid(eventCrf.getStudySubject().getOcOid());

        formChangeDTO.setEventOid(eventCrf.getStudyEvent().getStudyEventDefinition().getOc_oid());
        formChangeDTO.setEventRepeatKey(getEventRepeatKey(eventCrf.getStudyEvent()));

        formChangeDTO.setFormOid(eventCrf.getFormLayout().getCrf().getOcOid());
        formChangeDTO.setFormCreatedDate(eventCrf.getDateCreated().toString());
        formChangeDTO.setFormUpdatedDate(eventCrf.getDateUpdated().toString());

        formChangeDTO.setFormCreatedBy(eventCrf.getUserAccount().getUserName());
        formChangeDTO.setFormUpdatedBy(updaterAccount.getUserName());

        formChangeDTO.setFormWorkflowStatus(eventCrf.getWorkflowStatus().getEnglishDisplayValue());
        if (eventCrf.getSdvStatus() != null){
        formChangeDTO.setFormSdvStatus(eventCrf.getSdvStatus().getEnglishDisplayValue());}
        if (eventCrf.getRemoved() != null){
        formChangeDTO.setFormRemoved(eventCrf.getRemoved().toString());}
        if (eventCrf.getArchived() != null){
        formChangeDTO.setFormArchived(eventCrf.getArchived().toString());}

        return formChangeDTO;
    }

    public FormChangeDTO constructFormChangeDTO(EventCrf eventCrf) {
        FormChangeDTO formChangeDTO = new FormChangeDTO();

        Study study = eventCrf.getStudySubject().getStudy();

        formChangeDTO.setCustomerUuid(coreUtilService.getCustomerUuid());
        formChangeDTO.setStudyUuid(getStudyUuid(study));
        formChangeDTO.setStudyEnvironmentUuid(getStudyEnvUuid(study));
        formChangeDTO.setStudyOid(getStudyOid(study));
        formChangeDTO.setSiteOid(getSiteOid(study));

        formChangeDTO.setParticipantId(eventCrf.getStudySubject().getLabel());
        formChangeDTO.setParticipantOid(eventCrf.getStudySubject().getOcOid());

        formChangeDTO.setEventOid(eventCrf.getStudyEvent().getStudyEventDefinition().getOc_oid());
        formChangeDTO.setEventRepeatKey(getEventRepeatKey(eventCrf.getStudyEvent()));

        formChangeDTO.setFormOid(eventCrf.getFormLayout().getCrf().getOcOid());
        formChangeDTO.setFormCreatedDate(eventCrf.getDateCreated().toString());
        if (eventCrf.getDateUpdated() != null) {
            formChangeDTO.setFormUpdatedDate(eventCrf.getDateUpdated().toString());
        } else {
            formChangeDTO.setFormUpdatedDate(eventCrf.getDateCreated().toString());
        }
        formChangeDTO.setFormWorkflowStatus(eventCrf.getWorkflowStatus().getEnglishDisplayValue());

        return formChangeDTO;
    }

    public FormChangeDTO constructNewFormChangeDTO(Study study, StudyEvent studyEvent, FormLayout formLayout) {
        FormChangeDTO formChangeDTO = new FormChangeDTO();

        formChangeDTO.setCustomerUuid(coreUtilService.getCustomerUuid());
        formChangeDTO.setStudyUuid(getStudyUuid(study));
        formChangeDTO.setStudyEnvironmentUuid(getStudyEnvUuid(study));
        formChangeDTO.setStudyOid(getStudyOid(study));
        formChangeDTO.setSiteOid(getSiteOid(study));

        formChangeDTO.setParticipantId(studyEvent.getStudySubject().getLabel());
        formChangeDTO.setParticipantOid(studyEvent.getStudySubject().getOcOid());
        formChangeDTO.setFormOid(formLayout.getCrf().getOcOid());
        formChangeDTO.setEventOid(studyEvent.getStudyEventDefinition().getOc_oid());
        formChangeDTO.setEventRepeatKey(getEventRepeatKey(studyEvent));
        formChangeDTO.setFormWorkflowStatus(EventCrfWorkflowStatusEnum.INITIAL_DATA_ENTRY.getEnglishDisplayValue());

        return formChangeDTO;
    }

    private String getStudyEnvUuid(Study study) {
        if (study.getStudy() == null) {
            return study.getStudyEnvUuid();
        } else {
            return study.getStudy().getStudyEnvUuid();
        }
    }

    private String getStudyUuid(Study study) {
        if (study.getStudy() == null) {
            return study.getStudyUuid();
        } else {
            return study.getStudy().getStudyUuid();
        }
    }

    private String getSiteOid(Study study) {
        if (study.getStudy() == null) {
            return null;
        } else {
            return study.getOc_oid();
        }
    }

    private String getStudyOid(Study study) {
        if (study.getStudy() == null) {
            return study.getOc_oid();
        } else {
            return study.getStudy().getOc_oid();
        }
    }

    private int getEventRepeatKey(StudyEvent studyEvent) {
        if (studyEvent.getStudyEventDefinition().isRepeating()) {
            return studyEvent.getSampleOrdinal();
        } else {
            return 0;
        }
    }

    private int getEventRepeatKey(StudyEventBean studyEventBean) {
        StudyEventDefinition studyEventDefinition = studyEventDefinitionDao.findByStudyEventDefinitionId(studyEventBean.getStudyEventDefinitionId());
        if (studyEventDefinition.isRepeating()) {
            return studyEventBean.getSampleOrdinal();
        } else {
            return 0;
        }
    }
}
