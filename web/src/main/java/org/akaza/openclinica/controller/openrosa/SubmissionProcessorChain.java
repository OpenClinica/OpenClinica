package org.akaza.openclinica.controller.openrosa;

import org.akaza.openclinica.controller.openrosa.processor.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SubmissionProcessorChain {
    public enum ProcessorEnum {PROCEED, DO_NOT_PROCEED, PROCESS_ALL, INSTANCE_ID_PROCESSOR,
    FIELD_SUBMISSION_RPOCESSOR, SUBMISSION_PROCESSOR};
    @Autowired
    List<Processor> processors;

    public void processSubmission(SubmissionContainer container, boolean fieldSubmissionFlag) throws Exception {
        for (Processor processor:processors) {
            if (processor.process(container, fieldSubmissionFlag) != ProcessorEnum.PROCEED) {
                break;
            }
        }
    }
}
