package org.akaza.openclinica.service.crfdata;

/**
 * MetadataServiceInterface, our abstract interface for Dynamics
 * @author thickerson, Mar 3rd, 2010
 * initial methods: isShown, show and hide
 * (can add others later to enable/disable/color/uncolor, etc
 * initial implementations: ItemMetadataService and GroupMetadataService
 *
 */
public interface MetadataServiceInterface {

    public abstract boolean isShown(Object metadataBean);
    
    public abstract boolean hide(Object metadataBean);
    
    public abstract boolean show(Object metadataBean);
}
