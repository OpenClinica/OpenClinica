package com.openclinica.kafka.aop;

import com.openclinica.kafka.KafkaService;
import core.org.akaza.openclinica.bean.managestudy.StudyEventBean;
import core.org.akaza.openclinica.dao.hibernate.StudyEventDao;
import core.org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import core.org.akaza.openclinica.dao.hibernate.UserAccountDao;
import core.org.akaza.openclinica.domain.datamap.StudyEvent;
import core.org.akaza.openclinica.domain.datamap.StudySubject;
import core.org.akaza.openclinica.ocobserver.StudyEventContainer;
import core.org.akaza.openclinica.service.managestudy.StudySubjectService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Calendar;

@Aspect
@Component
public class AuditStudyEventAspect {
    protected final Logger log = LoggerFactory.getLogger(getClass().getName());
    private KafkaService kafkaService;
    private StudySubjectService studySubjectService;
    private UserAccountDao userAccountDao;
    private StudySubjectDao studySubjectDao;
    private StudyEventDao studyEventDao;

    public AuditStudyEventAspect(KafkaService kafkaService, StudySubjectService studySubjectService, UserAccountDao userAccountDao, StudySubjectDao studySubjectDao, StudyEventDao studyEventDao){
        this.kafkaService = kafkaService;
        this.studySubjectService = studySubjectService;
        this.userAccountDao = userAccountDao;
        this.studySubjectDao = studySubjectDao;
        this.studyEventDao = studyEventDao;
        this.studyEventDao = studyEventDao;
    }

    @Around("execution(* core.org.akaza.openclinica.dao.hibernate.StudyEventDao.saveOrUpdate(..))")
    public StudyEvent onStudyEventDaoSaveOrUpdate(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        log.info("AoP: onStudyEventDaoSaveOrUpdate triggered");

        StudyEvent studyEvent = (StudyEvent) proceedingJoinPoint.getArgs()[0];
        boolean studyEventIsTheSame = studyEventIsTheSame(studyEvent);

        StudyEvent afterStudyEvent = (StudyEvent) proceedingJoinPoint.proceed();
        updateStudySubjectLastModifiedDetails(studyEvent);

        if (!studyEventIsTheSame){
            log.info("AoP: triggered sendFormAttributeChangeMessage");
            kafkaService.sendEventAttributeChangeMessage(afterStudyEvent);
        }

        return afterStudyEvent;

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
            StudyEvent studyEvent = ((StudyEventContainer) joinPoint.getArgs()[0]).getEvent();
            kafkaService.sendEventAttributeChangeMessage((StudyEventContainer) joinPoint.getArgs()[0]);
            updateStudySubjectLastModifiedDetails(studyEvent);
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
            updateStudySubjectLastModifiedDetails(studyEventBean);
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
            updateStudySubjectLastModifiedDetails(studyEventBean);
        } catch (Exception e) {
            log.error("Could not send kafka message triggered by StudyEventDAO.create: ", e);
        }
    }

    private void updateStudySubjectLastModifiedDetails(StudyEvent studyEvent){
        if(studyEvent.getUpdateId() != null && studyEvent.getUpdateId() > 0 )
            studySubjectService.updateStudySubject(studyEvent.getStudySubject(), studyEvent.getUpdateId());
        else
            studySubjectService.updateStudySubject(studyEvent.getStudySubject(), studyEvent.getUserAccount().getUserId());
    }

    private void updateStudySubjectLastModifiedDetails(StudyEventBean studyEventBean){
        StudySubject studySubject = studySubjectDao.findByPK(studyEventBean.getStudySubjectId());
        if (studyEventBean.getUpdater() != null && studyEventBean.getUpdater().getId() > 0)
            studySubjectService.updateStudySubject(studySubject, studyEventBean.getUpdater().getId());
        else
            studySubjectService.updateStudySubject(studySubject, studyEventBean.getOwnerId());
    }

}
