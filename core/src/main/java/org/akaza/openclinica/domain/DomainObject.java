/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.domain;

public interface DomainObject {

    /**
     * @return the internal database identifier for this object
     */
    Integer getId();

    /**
     * Set the internal database identifier for this object.  In practice this should not be
     * called by application code -- just the persistence mechanism.
     * @param id
     */
    void setId(Integer id);

}
