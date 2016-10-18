package org.akaza.openclinica.controller.openrosa.processor;

import org.akaza.openclinica.controller.openrosa.SubmissionContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static org.akaza.openclinica.controller.openrosa.SubmissionProcessorChain.ProcessorEnum;

@Component
@Order(value=1)
public class InstanceIdProcessor implements Processor {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public ProcessorEnum process(SubmissionContainer container, boolean fieldSubmissionFlag) throws Exception {
        logger.info("Executing InstanceId Processor.");
        if (container.getProcessorEnum() != ProcessorEnum.INSTANCE_ID_PROCESSOR)
            return ProcessorEnum.PROCEED;

        // do processing for instance id
        return ProcessorEnum.DO_NOT_PROCEED;

    }

}