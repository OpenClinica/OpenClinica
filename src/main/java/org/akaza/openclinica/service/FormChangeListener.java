package org.akaza.openclinica.service;

import com.openclinica.kafka.KafkaService;
import com.openclinica.kafka.dto.FormChangeDTO;
import net.jodah.expiringmap.ExpirationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FormChangeListener implements ExpirationListener {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private FormChangeDTO formChangeDTO;
    private KafkaService kafkaService;

    public FormChangeListener(FormChangeDTO formChangeDTO, KafkaService kafkaService){
        this.formChangeDTO = formChangeDTO;
        this.kafkaService = kafkaService;
    }

    @Override
    public void expired(Object key, Object value) {
        logger.info("Expired entry in map.");
        try {
            kafkaService.sendFormChangeMessage(formChangeDTO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
