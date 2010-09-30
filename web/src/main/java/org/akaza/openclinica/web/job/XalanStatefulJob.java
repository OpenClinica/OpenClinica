package org.akaza.openclinica.web.job;

import org.quartz.StatefulJob;

public class XalanStatefulJob extends XalanTransformJob
    implements StatefulJob {
    
    public XalanStatefulJob() {
        super();
    }

}
