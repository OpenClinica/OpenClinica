package com.openclinica.aop;

import com.openclinica.kafka.KafkaService;
import core.org.akaza.openclinica.bean.submit.ItemDataBean;
import core.org.akaza.openclinica.domain.datamap.ItemData;
import org.akaza.openclinica.controller.openrosa.SubmissionContainer;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditItemDataAspect {
    protected final Logger log = LoggerFactory.getLogger(getClass().getName());
    private KafkaService kafkaService;

    public AuditItemDataAspect(KafkaService kafkaService){
        this.kafkaService = kafkaService;
    }

    // Point these to ItemProcessor and FSItemProcessor
    @AfterReturning("execution(* org.akaza.openclinica.service.DataSaveServiceImpl.saveOrUpdate(..))")
    public void onDataSaveServiceSaveOrUpdate(JoinPoint joinPoint) {
        log.info("AoP: onDataSaveServiceSaveOrUpdate triggered");
        try {
            kafkaService.sendItemDataChangeMessage((ItemData) joinPoint.getArgs()[0]);
        } catch (Exception e) {
            log.error("Could not send kafka message triggered by ItemDataDao.auditedSaveOrUpdate: ", e);
        }
    }

    // Determine if we need to cover these ItemDataDAOs...
    @AfterReturning("execution(* core.org.akaza.openclinica.dao.submit.ItemDataDAO.create(..))")
    public void onItemDataDAOCreate(JoinPoint joinPoint) {
        ItemDataBean itemDataBean = (ItemDataBean) joinPoint.getArgs()[0];
        log.info("AoP: onItemDataDAOCreate triggered");
        try {
            kafkaService.sendItemDataChangeMessage(itemDataBean);
        } catch (Exception e) {
            log.error("Could not send kafka message triggered by ItemDataDAO.create: ", e);
        }
    }

    @AfterReturning("execution(* core.org.akaza.openclinica.dao.submit.ItemDataDAO.update(..))")
    public void onItemDataDAOUpdate(JoinPoint joinPoint) {
        ItemDataBean itemDataBean = (ItemDataBean) joinPoint.getArgs()[0];
        log.info("AoP: onItemDataDAOUpdate triggered");
        try {
            kafkaService.sendItemDataChangeMessage(itemDataBean);
        } catch (Exception e) {
            log.error("Could not send kafka message triggered by ItemDataDAO.update: ", e);
        }
    }

    @AfterReturning("execution(* core.org.akaza.openclinica.dao.submit.ItemDataDAO.delete(..))")
    public void onItemDataDAODelete(JoinPoint joinPoint) {
        ItemDataBean itemDataBean = (ItemDataBean) joinPoint.getArgs()[0];
        log.info("AoP: onItemDataDAODelete triggered");
        try {
            kafkaService.sendItemDataChangeMessage(itemDataBean);
        } catch (Exception e) {
            log.error("Could not send kafka message triggered by ItemDataDAO.delete: ", e);
        }
    }

}
