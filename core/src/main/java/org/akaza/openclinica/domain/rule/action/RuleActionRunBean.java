/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.domain.rule.action;

import java.io.Serializable;
import java.util.ArrayList;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.akaza.openclinica.domain.AbstractMutableDomainObject;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 * @author Krikor Krumlian
 */

@Entity
@Table(name = "rule_action_run")
@GenericGenerator(name = "id-generator", strategy = "native", parameters = { @Parameter(name = "sequence", value = "rule_action_run_id_seq") })
public class RuleActionRunBean extends AbstractMutableDomainObject implements Serializable{

    Boolean administrativeDataEntry;
    Boolean initialDataEntry;
    Boolean doubleDataEntry;
    Boolean importDataEntry;
    Boolean batch;
    //study event stage
    Boolean not_started;
    Boolean scheduled;
    Boolean data_entry_started;
    Boolean complete;
    Boolean skipped;
    Boolean stopped;
    
    public RuleActionRunBean() {
        // TODO Auto-generated constructor stub
        this.administrativeDataEntry = true;
        this.initialDataEntry = true;
        this.doubleDataEntry = true;
        this.importDataEntry = false;
        this.batch = true;
    }

    public RuleActionRunBean(Boolean administrativeDataEntry, Boolean initialDataEntry, Boolean doubleDataEntry, Boolean importDataEntry, Boolean batch) {
        super();
        this.administrativeDataEntry = administrativeDataEntry;
        this.initialDataEntry = initialDataEntry;
        this.doubleDataEntry = doubleDataEntry;
        this.importDataEntry = importDataEntry;
        this.batch = batch;
    }
    
    
    
    public RuleActionRunBean(Boolean administrativeDataEntry, Boolean initialDataEntry, Boolean doubleDataEntry, Boolean importDataEntry, Boolean batch,Boolean NOT_STARTED,Boolean scheduled,
    		Boolean DATA_ENTRY_STARTED,Boolean complete,Boolean skipped,Boolean stopped  ) {
        super();
        this.administrativeDataEntry = administrativeDataEntry;
        this.initialDataEntry = initialDataEntry;
        this.doubleDataEntry = doubleDataEntry;
        this.importDataEntry = importDataEntry;
        this.batch = batch;
        this.not_started = NOT_STARTED;
        this.scheduled = scheduled;
        this.data_entry_started = DATA_ENTRY_STARTED;
        this.complete = complete;
        this.skipped = skipped;
        this.stopped = stopped;
    }
    
    

    public enum Phase {
        ADMIN_EDITING, INITIAL_DATA_ENTRY, DOUBLE_DATA_ENTRY, IMPORT, BATCH
    }
    
    public enum studyEventPhase{
    	NOT_STARTED,
    	SCHEDULED,
    	DATA_ENTRY_STARTED,
    	COMPLETE,
    	SKIPPED,
    	STOPPED
    }

    public Boolean getAdministrativeDataEntry() {
        return administrativeDataEntry;
    }

    public void setAdministrativeDataEntry(Boolean administrativeDataEntry) {
        this.administrativeDataEntry = administrativeDataEntry;
    }

    public Boolean getInitialDataEntry() {
        return initialDataEntry;
    }

    public void setInitialDataEntry(Boolean initialDataEntry) {
        this.initialDataEntry = initialDataEntry;
    }

    public Boolean getDoubleDataEntry() {
        return doubleDataEntry;
    }

    public void setDoubleDataEntry(Boolean doubleDataEntry) {
        this.doubleDataEntry = doubleDataEntry;
    }

    public Boolean getImportDataEntry() {
        return importDataEntry;
    }

    public void setImportDataEntry(Boolean importDataEntry) {
        this.importDataEntry = importDataEntry;
    }

    public Boolean getBatch() {
        return batch;
    }

    public void setBatch(Boolean batch) {
        this.batch = batch;
    }

    public Boolean getNot_started() {
		return not_started;
	}

	public void setNot_started(Boolean not_started) {
		this.not_started = not_started;
	}

	public Boolean getScheduled() {
		return scheduled;
	}

	public void setScheduled(Boolean scheduled) {
		this.scheduled = scheduled;
	}

	public Boolean getData_entry_started() {
		return data_entry_started;
	}

	public void setData_entry_started(Boolean data_entry_started) {
		this.data_entry_started = data_entry_started;
	}

	public Boolean getComplete() {
		return complete;
	}

	public void setComplete(Boolean complete) {
		this.complete = complete;
	}

	public Boolean getSkipped() {
		return skipped;
	}

	public void setSkipped(Boolean skipped) {
		this.skipped = skipped;
	}

	public Boolean getStopped() {
		return stopped;
	}

	public void setStopped(Boolean stopped) {
		this.stopped = stopped;
	}

	@Transient
    public String getRunActionRunsForDisplay() {

        ArrayList<String> r = new ArrayList<String>();
        if (getAdministrativeDataEntry() == true)
            r.add("Administrative Data Entry");
        if (getInitialDataEntry() == true)
            r.add("Initial Data Entry");
        if (getDoubleDataEntry() == true)
            r.add("Double Data Entry");
        if (getImportDataEntry() == true)
            r.add("Import Data Entry");
        if (getBatch() == true)
            r.add("Batch");
        return seperateStringBasedListBy(r, ", ", "");

    }

    @Transient
    private String seperateStringBasedListBy(ArrayList<String> list, String seperator, String terminator) {
        StringBuffer sb = new StringBuffer();
        if (list.size() == 0)
            return sb.toString();
        if (list.size() == 1) {
            sb.append(list.get(0));
        } else {
            for (int i = 0; i < list.size() - 1; i++) {
                sb.append(list.get(i) + seperator);
            }
            sb.append(list.get(list.size() - 1));
        }
        sb.append(terminator);
        return sb.toString();

    }

    @Transient
    public Boolean canRun(Phase phase) {
        switch (phase) {
        case ADMIN_EDITING: {
            return getAdministrativeDataEntry();
        }
        case INITIAL_DATA_ENTRY: {
            return getInitialDataEntry();
        }
        case DOUBLE_DATA_ENTRY: {
            return getDoubleDataEntry();
        }
        case IMPORT: {
            return getImportDataEntry();
        }
        case BATCH: {
            return getBatch();
        }

        default:
            return false;
        }

    }

}
