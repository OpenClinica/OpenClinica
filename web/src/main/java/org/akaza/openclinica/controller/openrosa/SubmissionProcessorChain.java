package org.akaza.openclinica.controller.openrosa;

import java.util.List;

import org.akaza.openclinica.controller.openrosa.processor.InstanceIdProcessor;
import org.akaza.openclinica.controller.openrosa.processor.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SubmissionProcessorChain {
    public enum ProcessorEnum {PROCEED, DO_NOT_PROCEED, PROCESS_ALL, INSTANCE_ID_PROCESSOR,
    FIELD_SUBMISSION_RPOCESSOR, SUBMISSION_PROCESSOR};
    @Autowired
    List<Processor> processors;

    public void processSubmission(SubmissionContainer container) throws Exception {
        for (Processor processor:processors) {
            if (processor.process(container) != ProcessorEnum.PROCEED) {
                break;
            }
        }
    }
}
