package com.openclinica.aop;

import com.openclinica.kafka.KafkaService;
import core.org.akaza.openclinica.bean.managestudy.StudyEventBean;
import core.org.akaza.openclinica.bean.submit.EventCRFBean;
import core.org.akaza.openclinica.dao.hibernate.EventCrfDao;
import core.org.akaza.openclinica.domain.datamap.EventCrf;
import core.org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.domain.enumsupport.EventCrfWorkflowStatusEnum;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditEventCrfAspect {
    protected final Logger log = LoggerFactory.getLogger(getClass().getName());
    private KafkaService kafkaService;
    private EventCrfDao eventCrfDao;

    public AuditEventCrfAspect(KafkaService kafkaService, EventCrfDao eventCrfDao){
        this.kafkaService = kafkaService;
        this.eventCrfDao = eventCrfDao;
    }

    @Around("execution(* core.org.akaza.openclinica.dao.hibernate.EventCrfDao.saveOrUpdate(..))")
    public EventCrf beforeEventCrfDaoSaveOrUpdate(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        EventCrf eventCrf = (EventCrf) proceedingJoinPoint.getArgs()[0];
        boolean eventCrfIsSame = eventCrfIsTheSame(eventCrf);

        EventCrf afterEventCrf = (EventCrf) proceedingJoinPoint.proceed();

        if (!eventCrfIsSame){
            log.info("AoP: triggered sendFormAttributeChangeMessage");
            kafkaService.sendFormAttributeChangeMessage(afterEventCrf);
        }

        return afterEventCrf;
    }

    public boolean eventCrfIsTheSame(EventCrf eventCrf){
        EventCrf existingEventCrf = eventCrfDao.findById(eventCrf.getEventCrfId());

        if (existingEventCrf != null){
            if (existingEventCrf.getWorkflowStatus().equals(eventCrf.getWorkflowStatus())) {
                log.info("AoP: Workflow status does match!");
                return true; }
            else {
                log.info("AoP: Workflow status does NOT match!");
                return false; }
        }
        log.info("AoP: CRF must not exist, so it has changed.");
        return false;
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
