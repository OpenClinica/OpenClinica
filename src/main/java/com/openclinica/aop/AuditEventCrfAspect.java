package com.openclinica.aop;

import com.openclinica.kafka.KafkaService;
import core.org.akaza.openclinica.bean.managestudy.StudyEventBean;
import core.org.akaza.openclinica.domain.datamap.StudyEvent;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditEventCrfAspect {
    protected final Logger log = LoggerFactory.getLogger(getClass().getName());
    private KafkaService kafkaService;

    public AuditEventCrfAspect(KafkaService kafkaService){
        this.kafkaService = kafkaService;
    }

/*    @AfterReturning("execution(* core.org.akaza.openclinica.dao.hibernate.EventCrfDao. ????????? (..))")
    public void onStudyEventDaoSaveOrUpdate(JoinPoint joinPoint) {
        log.info("============= Hitting the onStudyEventDaoSaveOrUpdate pointcut");
        try {
            kafkaService.sendEventAttributeChangeMessage((StudyEvent) joinPoint.getArgs()[0]);
        } catch (Exception e) {
            log.error("Could not send kafka message triggered by StudyEventDao.saveOrUpdate: ", e);
        }
    }*/


    @AfterReturning("execution(* core.org.akaza.openclinica.dao.submit.EventCRFDAO.update(..))")
    public void onEventCrfDAOupdate(JoinPoint joinPoint) {
        log.info("============= Hitting the onEventCrfDAOupdate pointcut");
        try {
            kafkaService.sendFormChangeMessage(null);
        } catch (Exception e) {
            log.error("Could not send kafka message triggered by EventCRFDAO.update: ", e);
        }
    }

    @AfterReturning("execution(* core.org.akaza.openclinica.dao.submit.EventCRFDAO.markComplete(..))")
    public void onEventCrfDAOmarkComplete(JoinPoint joinPoint) {
        log.info("============= Hitting the onEventCrfDAOmarkComplete pointcut");
        try {
            kafkaService.sendFormChangeMessage(null);
        } catch (Exception e) {
            log.error("Could not send kafka message triggered by EventCRFDAO.markComplete: ", e);
        }
    }

    @AfterReturning("execution(* core.org.akaza.openclinica.dao.submit.EventCRFDAO.create(..))")
    public void onEventCrfDAOcreate(JoinPoint joinPoint) {
        log.info("============= Hitting the onEventCrfDAOcreate pointcut");
        try {
            kafkaService.sendFormChangeMessage(null);
        } catch (Exception e) {
            log.error("Could not send kafka message triggered by EventCRFDAO.create: ", e);
        }
    }

    @AfterReturning("execution(* core.org.akaza.openclinica.dao.submit.EventCRFDAO.updateFormLayoutID(..))")
    public void onEventCrfDAOupdateFormLayoutID(JoinPoint joinPoint) {
        log.info("============= Hitting the onEventCrfDAOupdateFormLayoutID pointcut");
        try {
            kafkaService.sendFormChangeMessage(null);
        } catch (Exception e) {
            log.error("Could not send kafka message triggered by EventCRFDAO.updateFormLayoutID: ", e);
        }
    }
}
