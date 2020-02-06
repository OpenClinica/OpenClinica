/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.web.job;

import org.quartz.StatefulJob;

public class XalanStatefulJob extends XalanTransformJob
    implements StatefulJob {
    
    public XalanStatefulJob() {
        super();
    }

}
