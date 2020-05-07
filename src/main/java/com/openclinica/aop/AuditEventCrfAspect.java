package com.openclinica.aop;

import com.openclinica.kafka.KafkaService;
import core.org.akaza.openclinica.bean.managestudy.StudyEventBean;
import core.org.akaza.openclinica.bean.submit.EventCRFBean;
import core.org.akaza.openclinica.domain.datamap.EventCrf;
import core.org.akaza.openclinica.domain.datamap.StudyEvent;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
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

    @AfterReturning("execution(* core.org.akaza.openclinica.dao.hibernate.EventCrfDao.saveOrUpdate(..))")
    public void onEventCrfDaoSaveOrUpdate(JoinPoint joinPoint) {
        log.info("AoP: onEventCrfDaoSaveOrUpdate triggered");
        try {
            kafkaService.sendFormAttributeChangeMessage((EventCrf) joinPoint.getArgs()[0]);
        } catch (Exception e) {
            log.error("Could not send kafka message triggered by EventCrfDao.saveOrUpdate: ", e);
        }
    }

    @AfterReturning("execution(* core.org.akaza.openclinica.dao.hibernate.EventCrfDao.save(..))")
    public void onEventCrfDaoSave(JoinPoint joinPoint) {
        log.info("AoP: onEventCrfDaoSave triggered");
        try {
            kafkaService.sendFormAttributeChangeMessage((EventCrf) joinPoint.getArgs()[0]);
        } catch (Exception e) {
            log.error("Could not send kafka message triggered by EventCrfDao.save: ", e);
        }
    }

    @AfterReturning("execution(* core.org.akaza.openclinica.dao.submit.EventCRFDAO.update(..))")
    public void onEventCrfDAOupdate(JoinPoint joinPoint) {
        log.info("AoP: onEventCrfDAOupdate triggered");
        try {
            kafkaService.sendFormAttributeChangeMessage((EventCRFBean) joinPoint.getArgs()[0]);
        } catch (Exception e) {
            log.error("Could not send kafka message triggered by EventCRFDAO.update: ", e);
        }
    }

    @AfterReturning("execution(* core.org.akaza.openclinica.dao.submit.EventCRFDAO.markComplete(..))")
    public void onEventCrfDAOmarkComplete(JoinPoint joinPoint) {
        log.info("AoP: onEventCrfDAOmarkComplete triggered");
        try {
            kafkaService.sendFormAttributeChangeMessage((EventCRFBean) joinPoint.getArgs()[0]);
        } catch (Exception e) {
            log.error("Could not send kafka message triggered by EventCRFDAO.markComplete: ", e);
        }
    }

    @AfterReturning("execution(* core.org.akaza.openclinica.dao.submit.EventCRFDAO.create(..))")
    public void onEventCrfDAOcreate(JoinPoint joinPoint) {
        log.info("AoP: onEventCrfDAOcreate triggered");
        try {
            kafkaService.sendFormAttributeChangeMessage((EventCRFBean) joinPoint.getArgs()[0]);
        } catch (Exception e) {
            log.error("Could not send kafka message triggered by EventCRFDAO.create: ", e);
        }
    }

    @AfterReturning("execution(* core.org.akaza.openclinica.dao.submit.EventCRFDAO.updateFormLayoutID(..))")
    public void onEventCrfDAOupdateFormLayoutID(JoinPoint joinPoint) {
        log.info("AoP: onEventCrfDAOupdateFormLayoutID triggered");
        try {
            kafkaService.sendFormAttributeChangeMessage((EventCRFBean) joinPoint.getArgs()[0]);
        } catch (Exception e) {
            log.error("Could not send kafka message triggered by EventCRFDAO.updateFormLayoutID: ", e);
        }
    }
}
