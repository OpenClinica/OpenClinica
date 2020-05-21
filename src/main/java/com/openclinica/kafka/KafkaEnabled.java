package com.openclinica.kafka;

import core.org.akaza.openclinica.dao.core.CoreResources;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class KafkaEnabled extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata annotatedTypeMetadata) {
        if (CoreResources.getField("kafka.auditing").equals("true"))
            return new ConditionOutcome(true, "It Matches!");
        else
            return new ConditionOutcome(false, "No Match!");
    }
}
