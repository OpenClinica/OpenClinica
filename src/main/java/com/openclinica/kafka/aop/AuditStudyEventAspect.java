package com.openclinica.kafka.aop;

import com.openclinica.kafka.KafkaService;
import core.org.akaza.openclinica.bean.managestudy.StudyEventBean;
import core.org.akaza.openclinica.bean.submit.ItemDataBean;
import core.org.akaza.openclinica.dao.hibernate.StudyEventDao;
import core.org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import core.org.akaza.openclinica.dao.hibernate.UserAccountDao;
import core.org.akaza.openclinica.domain.datamap.ItemData;
import core.org.akaza.openclinica.domain.datamap.StudyEvent;
import core.org.akaza.openclinica.domain.datamap.StudySubject;
import core.org.akaza.openclinica.ocobserver.StudyEventContainer;
import core.org.akaza.openclinica.service.managestudy.StudySubjectService;
import org.apache.commons.lang3.BooleanUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

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
    }

    @AfterReturning("execution(* core.org.akaza.openclinica.service.StudyEventService.saveOrUpdate(..))")
    public void onStudyEventDaoSaveOrUpdate(JoinPoint joinPoint) {
        log.info("AoP: onStudyEventServiceSaveOrUpdate triggered");
        try {
            StudyEvent studyEvent = (StudyEvent) joinPoint.getArgs()[0];
            kafkaService.sendEventAttributeChangeMessage(studyEvent);
            updateStudySubjectLastModifiedDetails(studyEvent);
        } catch (Exception e) {
            log.error("Could not send kafka message triggered by StudyEventService.saveOrUpdate: ", e);
        }
    }

    @AfterReturning("execution(* core.org.akaza.openclinica.service.StudyEventService.saveOrUpdateTransactional(..))")
    public void onStudyEventDaoSaveOrUpdateTransactional(JoinPoint joinPoint) {
        log.info("AoP: onStudyEventServiceSaveOrUpdate triggered");
        try {
            StudyEvent studyEvent = ((StudyEventContainer) joinPoint.getArgs()[0]).getEvent();
            kafkaService.sendEventAttributeChangeMessage((StudyEventContainer) joinPoint.getArgs()[0]);
            updateStudySubjectLastModifiedDetails(studyEvent);
        } catch (Exception e) {
            log.error("Could not send kafka message triggered by StudyEventService.saveOrUpdate: ", e);
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
