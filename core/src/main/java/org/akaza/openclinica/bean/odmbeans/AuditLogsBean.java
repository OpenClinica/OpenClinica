/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2010 Akaza
 * Research
 *
 */

package org.akaza.openclinica.bean.odmbeans;

import java.util.ArrayList;


/**
 *
 * @author ywang (May, 2010)
 *
 */
public class AuditLogsBean {
    private String entityID;
    private String entityName;
  
    private ArrayList<AuditLogBean> auditLogs = new ArrayList<AuditLogBean>();
    
    public String getEntityID() {
        return entityID;
    }
    public void setEntityID(String entityID) {
        this.entityID = entityID;
    }
    
    public String getEntityName() {
		return entityName;
	}
	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}
	
    public ArrayList<AuditLogBean> getAuditLogs() {
        return auditLogs;
    }
    public void setAuditLogs(ArrayList<AuditLogBean> auditLogs) {
        this.auditLogs = auditLogs;
    }
}