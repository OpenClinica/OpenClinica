package com.openclinica.kafka;

import core.org.akaza.openclinica.dao.core.CoreResources;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


// This class is used to enable or disable Kafka service. Currently we are determining the status from the datainfo.properties
// file but this may change depending on how rules-engine integrates with runtime.
@Aspect
@Component
public class KafkaControlAspect {
    protected final Logger log = LoggerFactory.getLogger(getClass().getName());

    @Around("execution(* com.openclinica.kafka.KafkaService.*Message(..))")
    public void isKafkaEnabled(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        if (CoreResources.isKafkaEnabled()){
            proceedingJoinPoint.proceed();}
    }
}
