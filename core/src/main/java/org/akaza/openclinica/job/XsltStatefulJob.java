package org.akaza.openclinica.job;

import org.quartz.StatefulJob;

public class XsltStatefulJob extends XsltTransformJob
    implements StatefulJob {
    
    public XsltStatefulJob() {
        super();
    }

}
