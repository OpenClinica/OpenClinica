package com.openclinica.kafka;

import core.org.akaza.openclinica.bean.service.StudyParameterValueBean;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.enumsupport.ModuleStatus;
import org.akaza.openclinica.config.StudyParamNames;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


// This class is used to enable or disable Kafka service. Currently we are determining the status from the datainfo.properties
// file but this may change depending on how rules-engine integrates with runtime.
@Aspect
@Component
public class KafkaControlAspect {
    protected final Logger log = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    StudyParameterValueDAO studyParameterValueDAO;

    @Around("execution(* com.openclinica.kafka.KafkaService.*Message(..))")
    public void isKafkaEnabled(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
            //TODO How to get the right study for this?
        // ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes(); ? Use this?
        if (CoreResources.isKafkaEnabled()){
            proceedingJoinPoint.proceed();}
    }

    private boolean isModuleEnabled(Study study) {
        StudyParameterValueBean randomizeStudyParameterValue = studyParameterValueDAO.findByHandleAndStudy(study.getStudyId(), StudyParamNames.STUDY_CALENDAR);
        if (randomizeStudyParameterValue.isActive()) {
            if (ModuleStatus.valueOf(randomizeStudyParameterValue.getValue()).equals(ModuleStatus.ACTIVE)) {
                return true;
            }
        }
        return false;
    }
}
