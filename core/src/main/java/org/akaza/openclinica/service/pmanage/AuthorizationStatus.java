/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.service.pmanage;

public class AuthorizationStatus {

    private long id;	    
    private String status;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    
    public String getStatus() {
	    return status;
    }

	public void setStatus(String status) {
		this.status = status;
    }


}
