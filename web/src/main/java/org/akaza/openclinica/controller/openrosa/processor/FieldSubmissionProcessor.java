package org.akaza.openclinica.controller.openrosa.processor;

import org.akaza.openclinica.controller.openrosa.SubmissionContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static org.akaza.openclinica.controller.openrosa.SubmissionProcessorChain.ProcessorEnum;

@Component
@Order(value=2)
public class FieldSubmissionProcessor implements Processor {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public ProcessorEnum process(SubmissionContainer container, boolean fieldSubmissionFlag) throws Exception {
        logger.info("Executing Field Submission Processor.");
        if (container.getProcessorEnum() != ProcessorEnum.FIELD_SUBMISSION_RPOCESSOR)
            return ProcessorEnum.PROCEED;

        container.setRequestBody(parseFieldSubmission(container.getRequestBody()));

        return ProcessorEnum.PROCEED;

    }

    private String parseFieldSubmission(String body) {
        return "<instance>" + body + "</instance>";
    }

}