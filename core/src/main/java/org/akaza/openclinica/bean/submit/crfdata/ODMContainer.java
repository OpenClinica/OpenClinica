package org.akaza.openclinica.bean.submit.crfdata;

/**
 * ODM Container, the surrounding tag for Clinical Data together with meta data
 * 
 * @author thickerson, 04/2008
 * 
 */
public class ODMContainer {

    private CRFDataPostImportContainer crfDataPostImportContainer;

    public CRFDataPostImportContainer getCrfDataPostImportContainer() {
        return crfDataPostImportContainer;
    }

    public void setCrfDataPostImportContainer(CRFDataPostImportContainer crfDataPostImportContainer) {
        this.crfDataPostImportContainer = crfDataPostImportContainer;
    }

}
