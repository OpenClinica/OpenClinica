package org.akaza.openclinica.service;

import com.openclinica.kafka.KafkaService;
import com.openclinica.kafka.dto.FormChangeDTO;
import core.org.akaza.openclinica.domain.datamap.EventCrf;
import core.org.akaza.openclinica.domain.datamap.FormLayout;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.datamap.StudyEvent;
import net.jodah.expiringmap.ExpirationListener;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

// Service class currently used by Kafka Service to track if a form is open. When a form stored in the cache is closed
// it should trigger a formChange topic message which is used by rules-engine to know when it should re-process rules
// on the new changes.

@Service
public class FormCacheServiceImpl {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    private KafkaService kafkaService;

    private ExpiringMap<String, FormChangeListener> expiringMap;
    private ExpirationListener<String, FormChangeListener> formListener;

    public FormCacheServiceImpl(){
        formListener = (key, formChange) -> formChange.expired(key, formChange);
        expiringMap = ExpiringMap.
                builder().
                asyncExpirationListener(formListener).
                maxSize(256).
                expirationPolicy(ExpirationPolicy.ACCESSED).
                expiration(3600, TimeUnit.SECONDS).
                build();
    }

    // TODO Currently we are adding a form to the cache when it is opened through the participant's details page.
    public void addEditFormToFormCache(String ecId, EventCrf eventCrf){
        FormChangeDTO formChangeDTO = kafkaService.constructFormChangeDTO(eventCrf);
        expiringMap.put(ecId, new FormChangeListener(formChangeDTO, kafkaService));
    }

    public void addNewFormToFormCache(String ecId, Study currentStudy, StudyEvent studyEvent, FormLayout formLayout) {
        FormChangeDTO formChangeDTO = kafkaService.constructNewFormChangeDTO(currentStudy, studyEvent, formLayout);
        expiringMap.put(ecId, new FormChangeListener(formChangeDTO, kafkaService));
    }

    public boolean resetExpiration(String ecId){
        boolean successfullyReset = expiringMap.containsKey(ecId);
        reportContentsOfMap();
        expiringMap.resetExpiration(ecId);
        reportContentsOfMap();
        return successfullyReset;
    }

    public boolean expireAndRemoveForm(String ecId){
        boolean successfullyRemoved;
        if (expiringMap.containsKey(ecId)){
            reportContentsOfMap();
            expiringMap.get(ecId).expired(null, null);
            expiringMap.remove(ecId);
            reportContentsOfMap();
            successfullyRemoved = true;
        }
        else {
            successfullyRemoved = false;
        }
        return successfullyRemoved;
    }

    public void reportContentsOfMap(){
        logger.info("Reporting contents of map...");
        for (ExpiringMap.Entry<String, FormChangeListener> entry : expiringMap.entrySet()){
            logger.info("Entry: " + entry.getKey() + " : " + expiringMap.getExpectedExpiration(entry.getKey()));
        }
    }

}
