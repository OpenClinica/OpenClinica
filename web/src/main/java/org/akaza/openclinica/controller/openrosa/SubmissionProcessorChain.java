package org.akaza.openclinica.controller.openrosa;

import org.akaza.openclinica.controller.openrosa.processor.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;

@Component
public class SubmissionProcessorChain {
    
    @Autowired
    List<Processor> processors;
    
    @PostConstruct
    public void init() {
        Collections.sort(processors, AnnotationAwareOrderComparator.INSTANCE);
    }

    public void processSubmission(SubmissionContainer container) throws Exception {
        for (Processor processor:processors) {
            processor.process(container);
        }
    }
}
