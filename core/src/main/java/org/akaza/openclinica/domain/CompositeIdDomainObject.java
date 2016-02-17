package org.akaza.openclinica.domain;

public interface CompositeIdDomainObject {

    /**
     * @return the internal database identifier for this object
     */
    Object getId();

    /**
     * Set the internal database identifier for this object.  In practice this should not be
     * called by application code -- just the persistence mechanism.
     * @param id
     */
    void setId(Object id);

}
