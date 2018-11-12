/**
 * 
 */
package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.controller.dto.AuditLogEventDTO;
import org.akaza.openclinica.controller.dto.CommonEventContainerDTO;
import org.akaza.openclinica.controller.dto.ViewStudySubjectDTO;
import org.akaza.openclinica.controller.helper.RestfulServiceHelper;
import org.akaza.openclinica.domain.datamap.AuditLogEvent;
import org.akaza.openclinica.domain.user.UserAccount;

import javax.servlet.http.HttpServletRequest;

/**
 * @author joekeremian
 *
 */
public interface AuditLogEventService {

	/**
	 * @param auditLogEventDTO
	 * @return
	 */
	 AuditLogEvent saveAuditLogEvent(AuditLogEventDTO auditLogEventDTO,UserAccountBean ub);

	public RestfulServiceHelper getRestfulServiceHelper();

	}
