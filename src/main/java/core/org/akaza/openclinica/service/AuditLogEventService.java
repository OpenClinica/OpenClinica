/**
 * 
 */
package core.org.akaza.openclinica.service;

import core.org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.controller.dto.AuditLogEventDTO;
import core.org.akaza.openclinica.domain.datamap.AuditLogEvent;

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

	/**
	 * @param auditLogEvent
	 * @return
	 */
	AuditLogEvent saveAuditLogEvent(AuditLogEvent auditLogEvent, UserAccountBean ub);


	}
