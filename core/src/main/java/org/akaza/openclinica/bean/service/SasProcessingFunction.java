/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.bean.service;

/**
 * SasProcessingFunction, a post-processing function for Extract Data
 * By Tom Hickerson, 09/2010
 * @author thickerson
 *
 */
public class SasProcessingFunction extends ProcessingFunction {

    public SasProcessingFunction() {
        fileType = "sas";
    }
    
    public ProcessingResultType run() {
        return null;
    }
    
}
