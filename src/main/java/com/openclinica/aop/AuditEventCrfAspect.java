package com.openclinica.aop;

import com.openclinica.kafka.KafkaService;
import core.org.akaza.openclinica.bean.managestudy.StudyEventBean;
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

    //TODO This worked with AfterReturning, I want to try with Pointcut first. Whatever works, combine it with an around method and save the data!
    @Pointcut("execution(* core.org.akaza.openclinica.dao.hibernate.AbstractDomainDao+.saveOrUpdate(..))")
    public void test1() {
        log.info("========================== Hitting the test1 pointcut");
/*        try {
            kafkaService.sendEventAttributeChangeMessage((StudyEvent) joinPoint.getArgs()[0]);
        } catch (Exception e) {
            log.error("Could not send kafka message triggered by AbstractDomainDao.saveOrUpdate: ", e);
        }*/
    }

    @Pointcut("execution(* core.org.akaza.openclinica.dao.hibernate.AbstractDomainDao+.save(..))")
    public void test2() {
        log.info("========================== Hitting the test2 pointcut");
/*        try {
            kafkaService.sendEventAttributeChangeMessage((StudyEvent) joinPoint.getArgs()[0]);
        } catch (Exception e) {
            log.error("Could not send kafka message triggered by AbstractDomainDao.saveOrUpdate: ", e);
        }*/
    }

    // This seems to get hit on data import.
    @Pointcut("target(core.org.akaza.openclinica.dao.hibernate.EventCrfDao)")
    public void test3() {
        log.info("========================= Hitting the test3 pointcut");
/*        try {
            kafkaService.sendEventAttributeChangeMessage((StudyEvent) joinPoint.getArgs()[0]);
        } catch (Exception e) {
            log.error("Could not send kafka message triggered by AbstractDomainDao.saveOrUpdate: ", e);
        }*/
    }

    // As it is currently, it's hitting this... although the log.infos in test1 and test3 are not getting hit...
    // Maybe pointcut does not execute its method?
    //TODO Update this to check for test2 and test3 as well, then see what arguments are available?
    // During debugging of import I hit this twice? Once with jobDetails as an argument, the second time with the eventCRF?
    @Around("(test1() && test3())")
    public void test4(JoinPoint joinPoint) {
        log.info("========================= Hitting the test4 pointcut");
/*        try {
            kafkaService.sendEventAttributeChangeMessage((StudyEvent) joinPoint.getArgs()[0]);
        } catch (Exception e) {
            log.error("Could not send kafka message triggered by AbstractDomainDao.saveOrUpdate: ", e);
        }*/
    }

    @AfterReturning("execution(* core.org.akaza.openclinica.dao.hibernate.AbstractDomainDao+.saveOrUpdate(..))")
    public void onAbstractDaoSaveOrUpdate(JoinPoint joinPoint) {
        log.info("============= Hitting the onAbstractDaoSaveOrUpdate pointcut");
/*        try {
            kafkaService.sendEventAttributeChangeMessage((StudyEvent) joinPoint.getArgs()[0]);
        } catch (Exception e) {
            log.error("Could not send kafka message triggered by AbstractDomainDao.saveOrUpdate: ", e);
        }*/
    }

    @AfterReturning("execution(* core.org.akaza.openclinica.dao.hibernate.AbstractDomainDao+.save(..))")
    public void onAbstractDaoSave(JoinPoint joinPoint) {
        log.info("============= Hitting the onAbstractDaoSave pointcut");
/*        try {
            kafkaService.sendEventAttributeChangeMessage((StudyEvent) joinPoint.getArgs()[0]);
        } catch (Exception e) {
            log.error("Could not send kafka message triggered by AbstractDomainDao.saveOrUpdate: ", e);
        }*/
    }

    @AfterReturning("execution(* core.org.akaza.openclinica.dao.hibernate.EventCrfDao.saveOrUpdate(..))")
    public void onStudyEventDaoSaveOrUpdate(JoinPoint joinPoint) {
        log.info("============= Hitting the onStudyEventDaoSaveOrUpdate pointcut");
        try {
            kafkaService.sendEventAttributeChangeMessage((StudyEvent) joinPoint.getArgs()[0]);
        } catch (Exception e) {
            log.error("Could not send kafka message triggered by StudyEventDao.saveOrUpdate: ", e);
        }
    }

    @AfterReturning("execution(* core.org.akaza.openclinica.dao.hibernate.EventCrfDao.save(..))")
    public void onStudyEventDaoSave(JoinPoint joinPoint) {
        log.info("============= Hitting the onStudyEventDaoSaveOrUpdate pointcut");
        try {
            kafkaService.sendEventAttributeChangeMessage((StudyEvent) joinPoint.getArgs()[0]);
        } catch (Exception e) {
            log.error("Could not send kafka message triggered by StudyEventDao.save: ", e);
        }
    }

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
