/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */

package org.akaza.openclinica.domain.admin;

import org.akaza.openclinica.domain.AbstractMutableDomainObject;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 
 * @author ywang (May, 2009)
 * 
 */
@Entity
@Table(name = "measurement_unit")
@GenericGenerator(name = "id-generator", strategy = "native", parameters = { @Parameter(name = "sequence_name", value = "measurement_unit_measurement_unit_id") })
public class MeasurementUnit extends AbstractMutableDomainObject {
    private String name;
    private String description;
    private String ocOid;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOcOid() {
        return ocOid;
    }

    public void setOcOid(String ocOid) {
        this.ocOid = ocOid;
    }
}