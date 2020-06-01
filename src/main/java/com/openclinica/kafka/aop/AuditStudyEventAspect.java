package com.openclinica.kafka.aop;

import com.openclinica.kafka.KafkaService;
import core.org.akaza.openclinica.bean.managestudy.StudyEventBean;
import core.org.akaza.openclinica.domain.datamap.StudyEvent;
import core.org.akaza.openclinica.ocobserver.StudyEventContainer;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
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

    public AuditStudyEventAspect(KafkaService kafkaService){
        this.kafkaService = kafkaService;
    }

    @AfterReturning("execution(* core.org.akaza.openclinica.dao.hibernate.StudyEventDao.saveOrUpdate(..))")
    public void onStudyEventDaoSaveOrUpdate(JoinPoint joinPoint) {
        log.info("AoP: onStudyEventDaoSaveOrUpdate triggered");
        try {
            kafkaService.sendEventAttributeChangeMessage((StudyEvent) joinPoint.getArgs()[0]);
        } catch (Exception e) {
            log.error("Could not send kafka message triggered by StudyEventDao.saveOrUpdate: ", e);
        }
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
