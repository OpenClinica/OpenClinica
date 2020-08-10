package com.openclinica.kafka.aop;

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
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

    // The reasoning for this check is that the eventCRF's updatedBy and lastUpdated fields are updated every time a single item data
    // field within the form is changed. We don't want to trigger the processing of rules in rules-engine every time a single item
    // data field is changed so we are filtering out those minimal changes here just to look for changes in the status of the eventCRF.
    public boolean eventCrfIsTheSame(EventCrf eventCrf){
        EventCrf existingEventCrf = getExistingEventCrf(eventCrf);

        if (existingEventCrf != null){
            if (areEventCrfStatusesTheSame(eventCrf, existingEventCrf)) {
                log.info("AoP: eventCRF is the same! Skipping kafka message.");
                return true; }
            else {
                log.info("AoP: eventCRF is not the same!");
                return false; }
        }
        // Captures a scenario where a new eventCRF was being saved multiple times before the transaction was completed.
        if (existingEventCrf == null && eventCrf.getEventCrfId() != 0){
            return true;
        }
        log.info("AoP: CRF must not exist, so it has changed.");
        return false;
    }

    private EventCrf getExistingEventCrf(EventCrf eventCrf) {
        Session session = eventCrfDao.getSessionFactory().openSession();
        EventCrf existingEventCrf = session.find(EventCrf.class, eventCrf.getEventCrfId());
        session.close();
        return existingEventCrf;
    }

    public boolean areEventCrfStatusesTheSame(EventCrf eventCrf, EventCrf existingEventCrf){
        if (!existingEventCrf.getWorkflowStatus().equals(eventCrf.getWorkflowStatus())){
            return false;}
        if (existingEventCrf.getSdvStatus() != null && eventCrf.getSdvStatus() != null && !existingEventCrf.getSdvStatus().equals(eventCrf.getSdvStatus())){
            return false;}
        if (existingEventCrf.getArchived() != null && eventCrf.getArchived() != null && !existingEventCrf.getArchived().equals(eventCrf.getArchived())){
            return false;}
        if (eventCrf.getRemoved() != null){
            if ((existingEventCrf.getRemoved() != null && !existingEventCrf.getRemoved().equals(eventCrf.getRemoved())) || existingEventCrf.getRemoved() == null){
                return false;}}
        if (eventCrf.getEditable() != null){
            if ((existingEventCrf.getEditable() != null && !existingEventCrf.getEditable().equals(eventCrf.getEditable())) || existingEventCrf.getEditable() == null){
                return false;}}
        if (eventCrf.getRequired() != null){
            if ((existingEventCrf.getRequired() != null && !existingEventCrf.getRequired().equals(eventCrf.getRequired())) || existingEventCrf.getRequired() == null){
                return false;}}
        if (eventCrf.getRelevant() != null){
            if ((existingEventCrf.getRelevant() != null && !existingEventCrf.getRelevant().equals(eventCrf.getRelevant())) || existingEventCrf.getRelevant() == null){
                return false;}}
        if (eventCrf.getEditable() != null){
            if ((existingEventCrf.getEditable() != null && !existingEventCrf.getEditable().equals(eventCrf.getEditable())) || existingEventCrf.getEditable() == null){
                return false;}}

        return true;
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
