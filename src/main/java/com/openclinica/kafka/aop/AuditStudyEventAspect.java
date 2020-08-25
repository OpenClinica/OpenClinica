package com.openclinica.kafka.aop;

import com.openclinica.kafka.KafkaService;
import core.org.akaza.openclinica.bean.managestudy.StudyEventBean;
import core.org.akaza.openclinica.dao.hibernate.StudyEventDao;
import core.org.akaza.openclinica.domain.datamap.EventCrf;
import core.org.akaza.openclinica.domain.datamap.StudyEvent;
import core.org.akaza.openclinica.ocobserver.StudyEventContainer;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.Calendar;

@Aspect
@Component
public class AuditStudyEventAspect {
    protected final Logger log = LoggerFactory.getLogger(getClass().getName());
    private KafkaService kafkaService;
    private StudyEventDao studyEventDao;

    public AuditStudyEventAspect(KafkaService kafkaService, StudyEventDao studyEventDao){
        this.kafkaService = kafkaService;
        this.studyEventDao = studyEventDao;
    }

    @Around("execution(* core.org.akaza.openclinica.dao.hibernate.StudyEventDao.saveOrUpdate(..))")
    public StudyEvent onStudyEventDaoSaveOrUpdate(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        log.info("AoP: onStudyEventDaoSaveOrUpdate triggered");


        StudyEvent studyEvent = (StudyEvent) proceedingJoinPoint.getArgs()[0];
        boolean studyEventIsTheSame = studyEventIsTheSame(studyEvent);

        StudyEvent afterStudyEvent = (StudyEvent) proceedingJoinPoint.proceed();

        if (!studyEventIsTheSame){
            log.info("AoP: triggered sendFormAttributeChangeMessage");
            kafkaService.sendEventAttributeChangeMessage(afterStudyEvent);
        }

        return afterStudyEvent;

/*        try {
            kafkaService.sendEventAttributeChangeMessage((StudyEvent) joinPoint.getArgs()[0]);
        } catch (Exception e) {
            log.error("Could not send kafka message triggered by StudyEventDao.saveOrUpdate: ", e);
        }*/
    }

    public boolean studyEventIsTheSame(StudyEvent studyEvent){
        StudyEvent existingStudyEvent = getExistingStudyEvent(studyEvent);

        if (existingStudyEvent != null){
            if (areStudyEventStatusesTheSame(studyEvent, existingStudyEvent)) {
                log.info("AoP: studyEvent is the same! Skipping kafka message.");
                return true; }
            else {
                log.info("AoP: studyEvent is not the same!");
                return false; }
        }
        // Captures a scenario where a new eventCRF was being saved multiple times before the transaction was completed.
        if (existingStudyEvent == null && studyEvent.getStudyEventId() != 0){
            return true;
        }
        log.info("AoP: studyEvent must not exist, so it has changed.");
        return false;
    }

    public boolean areStudyEventStatusesTheSame(StudyEvent studyEvent, StudyEvent existingStudyEvent){
        if (!existingStudyEvent.getWorkflowStatus().equals(studyEvent.getWorkflowStatus())){
            return false;}
        Calendar existingEventCal = Calendar.getInstance();
        existingEventCal.setTime(existingStudyEvent.getDateStart());
        Calendar studyEventCal = Calendar.getInstance();
        studyEventCal.setTime(studyEvent.getDateStart());

        if (existingEventCal.get(Calendar.YEAR) != (studyEventCal.get(Calendar.YEAR))){
            return false;}
        if (existingEventCal.get(Calendar.MONTH) != (studyEventCal.get(Calendar.MONTH))){
            return false;}
        if (existingEventCal.get(Calendar.DAY_OF_MONTH) != (studyEventCal.get(Calendar.DAY_OF_MONTH))){
            return false;}
        return true;
    }

    private StudyEvent getExistingStudyEvent(StudyEvent studyEvent) {
        Session session = studyEventDao.getSessionFactory().openSession();
        StudyEvent existingStudyEvent = session.find(StudyEvent.class, studyEvent.getStudyEventId());
        session.close();
        return existingStudyEvent;
    }

    @AfterReturning("execution(* core.org.akaza.openclinica.dao.hibernate.StudyEventDao.saveOrUpdateTransactional(..))")
    public void onStudyEventDaoSaveOrUpdateTransactional(JoinPoint joinPoint) {
        log.info("AoP: onStudyEventDaoSaveOrUpdate triggered");
        try {
            kafkaService.sendEventAttributeChangeMessage((StudyEventContainer) joinPoint.getArgs()[0]);
        } catch (Exception e) {
            log.error("Could not send kafka message triggered by StudyEventDao.saveOrUpdate: ", e);
        }
    }

    @AfterReturning("execution(* core.org.akaza.openclinica.dao.managestudy.StudyEventDAO.update(..))")
    public void onStudyEventDAOUpdate(JoinPoint joinPoint) {
        StudyEventBean studyEventBean = (StudyEventBean) joinPoint.getArgs()[0];
        log.info("AoP: onStudyEventDAOUpdate triggered");
        try {
            kafkaService.sendEventAttributeChangeMessage(studyEventBean);
        } catch (Exception e) {
            log.error("Could not send kafka message triggered by StudyEventDAO.update: ", e);
        }
    }

    @AfterReturning("execution(* core.org.akaza.openclinica.dao.managestudy.StudyEventDAO.create(..))")
    public void onStudyEventDAOCreate(JoinPoint joinPoint) {
        StudyEventBean studyEventBean = (StudyEventBean) joinPoint.getArgs()[0];
        log.info("AoP: onStudyEventDAOCreate triggered");
        try {
            kafkaService.sendEventAttributeChangeMessage(studyEventBean);
        } catch (Exception e) {
            log.error("Could not send kafka message triggered by StudyEventDAO.create: ", e);
        }
    }

}
