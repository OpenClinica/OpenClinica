package org.akaza.openclinica.controller.openrosa.processor;

import org.akaza.openclinica.controller.openrosa.SubmissionContainer;
import org.akaza.openclinica.controller.openrosa.SubmissionProcessorChain;

public interface Processor {

    public SubmissionProcessorChain.ProcessorEnum process(SubmissionContainer container) throws Exception;

}
