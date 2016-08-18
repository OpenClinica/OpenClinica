/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.domain.usageStats;

import java.sql.Timestamp;

import javax.persistence.*;

import org.akaza.openclinica.domain.AbstractMutableDomainObject;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 * <p>
 * Log Usage Statistics
 * </p>
 * 
 * @author Pradnya Gawade
 */
@Entity
@Table(name = "usage_statistics_data")
@GenericGenerator(name = "id-generator", strategy = "native", parameters = { @Parameter(name = "sequence_name", value = "usage_statistics_data_id_seq") })
public class LogUsageStatsBean extends AbstractMutableDomainObject {

    private String param_key;
    private String param_value;
    private Timestamp update_timestamp;

    /**
     * @return the param_key
     */
    public String getParam_key() {
        return param_key;
    }

    /**
     * @param param_key
     *            the param_key to set
     */
    public void setParam_key(String param_key) {
        this.param_key = param_key;
    }

    /**
     * @return the param_value
     */
    public String getParam_value() {
        return param_value;
    }

    /**
     * @param param_value
     *            the param_value to set
     */
    public void setParam_value(String param_value) {
        this.param_value = param_value;
    }

    /**
     * @return the update_timestamp
     */
    public Timestamp getUpdate_timestamp() {
        return update_timestamp;
    }

    /**
     * @param update_timestamp
     *            the update_timestamp to set
     */
    public void setUpdate_timestamp(Timestamp update_timestamp) {
        this.update_timestamp = update_timestamp;
    }

}