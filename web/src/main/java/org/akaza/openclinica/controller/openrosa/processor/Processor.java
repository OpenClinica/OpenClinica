/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.controller.openrosa.processor;

import org.akaza.openclinica.controller.openrosa.SubmissionContainer;

public interface Processor {

    public void process(SubmissionContainer container) throws Exception;

}
