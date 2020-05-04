package org.akaza.openclinica.service.aspect;

import com.openclinica.kafka.KafkaService;
import core.org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditStudyEventAspect {
    protected final Logger log = LoggerFactory.getLogger(getClass().getName());
    private KafkaService kafkaService;

    public AuditStudyEventAspect(KafkaService kafkaService){
        this.kafkaService = kafkaService;
    }

    // This should be working now. Triggered on schedule event via API.
    @AfterReturning("execution(* core.org.akaza.openclinica.dao.hibernate.StudyEventDao.saveOrUpdate(..))")
    public void onStudyEventUpdateDao(JoinPoint joinPoint) {
        Object[] methodArgs = joinPoint.getArgs();
        //String userUuid = (String) methodArgs[0];
        log.info("============= Hitting the onStudyEventUpdateDao audit");
    }

    // This is back to what we had before. It's triggering on the constructor sometime during server startup.
    // The same constructor, called later, does not trigger it.
    // This is with a wildcard on the method. If you remove that you probably won't see anything because it
    // won't hit the constructor.
    // This is capturing the spring bean "studyEventDAO" when I wire it as a @Component
    // The actual methods are being called in the spring bean "studyeventdaojdbc"
/*    @AfterReturning("execution(* core.org.akaza.openclinica.dao.managestudy.StudyEventDAO.*(..))")
    public void onStudyEventUni(JoinPoint joinPoint) {
        Object[] methodArgs = joinPoint.getArgs();
        //String userUuid = (String) methodArgs[0];
        log.info("============= Hitting the * studyEventDAO uni");
    }*/

   // @AfterReturning("execution(* core.org.akaza.openclinica.dao.managestudy.StudyEventDAO.update(core.org.akaza.openclinica.bean.core.EntityBean, java.sql.Connection, boolean))")
    //@AfterReturning("execution(* core.org.akaza.openclinica.dao.managestudy.StudyEventDAO.update(core.org.akaza.openclinica.bean.managestudy.StudyEventBean, java.sql.Connection, boolean))")
    //@AfterReturning("execution(core.org.akaza.openclinica.bean.core.EntityBean core.org.akaza.openclinica.dao.managestudy.StudyEventDAO.update(..))")
    //@AfterReturning("execution(core.org.akaza.openclinica.bean.core.EntityBean core.org.akaza.openclinica.dao.managestudy.StudyEventDAO.update(core.org.akaza.openclinica.bean.core.EntityBean, java.sql.Connection, boolean))")
    @AfterReturning("execution(* core.org.akaza.openclinica.dao.managestudy.StudyEventDAO.update(..))")
    public void onStudyEventUpdateDAO(JoinPoint joinPoint) {
        Object[] methodArgs = joinPoint.getArgs();
        StudyEventBean studyEventBean = (StudyEventBean) joinPoint.getArgs()[0];
        try {
            kafkaService.sendEventAttributeChangeMessage(studyEventBean);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("============= Hitting the * studyEventDAO audit");
    }

/*    @After("execution(* core.org.akaza.openclinica.dao.managestudy.StudyEventDAO.create(..))")
    public void onStudyEventCreateDAO(JoinPoint joinPoint) {
        Object[] methodArgs = joinPoint.getArgs();
        //String userUuid = (String) methodArgs[0];
        log.info("============= Hitting the onStudyEventCreateDAO audit");
    }*/

/*        @After("execution(* core.org.akaza.openclinica.dao.managestudy.StudyEventDAO.update(EntityBean, Connection, boolean))")
    public void onStudyEventUpdateDAOSpecificPathParam(JoinPoint joinPoint) {
        Object[] methodArgs = joinPoint.getArgs();
        //String userUuid = (String) methodArgs[0];
        log.info("============= Hitting the onStudyEventUpdateDAOSpecificPathParam audit");
    }*/
}
