package com.openclinica.kafka;

import core.org.akaza.openclinica.dao.core.CoreResources;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class KafkaControlAspect {
    protected final Logger log = LoggerFactory.getLogger(getClass().getName());

    @Around("execution(* com.openclinica.kafka.KafkaService.*(..))")
    public void isKafkaEnabled(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        if (CoreResources.isKafkaEnabled()){
            proceedingJoinPoint.proceed();}
    }
}
