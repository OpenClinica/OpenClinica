package org.akaza.openclinica.controller.openrosa.processor;

import org.akaza.openclinica.controller.openrosa.SubmissionContainer;

public interface Processor {

    public void process(SubmissionContainer container) throws Exception;

}
