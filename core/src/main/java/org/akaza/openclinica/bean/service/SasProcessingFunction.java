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
