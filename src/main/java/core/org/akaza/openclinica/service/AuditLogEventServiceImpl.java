package core.org.akaza.openclinica.service;


import core.org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.controller.dto.AuditLogEventDTO;
import org.akaza.openclinica.controller.helper.RestfulServiceHelper;
import core.org.akaza.openclinica.dao.hibernate.*;
import core.org.akaza.openclinica.domain.datamap.*;
import core.org.akaza.openclinica.domain.user.UserAccount;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

/**
 * This Service class is used to Insert records in Audit Log Event Table
 *
 * @author joekeremian
 */
@Service("auditLogEventService")
public class AuditLogEventServiceImpl implements AuditLogEventService {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    @Qualifier( "dataSource" )
    private BasicDataSource dataSource;

    @Autowired
    private AuditLogEventDao auditLogEventDao;

    @Autowired
    private UserAccountDao userAccountDao;

    @Autowired
    private CryptoConverter cryptoConverter;
    private RestfulServiceHelper restfulServiceHelper;


    public AuditLogEvent saveAuditLogEvent(AuditLogEventDTO auditLogEventDTO, UserAccountBean ub) {
        logger.debug("Request to save AuditLogEvent : {}", auditLogEventDTO);
        AuditLogEvent auditLogEvent = convertDTOtoEntity(auditLogEventDTO,ub);
        auditLogEvent = (AuditLogEvent) auditLogEventDao.saveOrUpdate(auditLogEvent);
        return auditLogEvent;
    }

    public AuditLogEvent saveAuditLogEvent(AuditLogEvent auditLogEvent, UserAccountBean ub) {
        auditLogEvent.setUserAccount(userAccountDao.findByUserId(ub.getId()));
        auditLogEvent.setAuditDate(new Timestamp(System.currentTimeMillis()));
        logger.debug("Request to save AuditLogEvent : {}", auditLogEvent);
        return (AuditLogEvent) auditLogEventDao.saveOrUpdate(auditLogEvent);
    }


    private AuditLogEvent convertDTOtoEntity(AuditLogEventDTO auditLogEventDTO,UserAccountBean ub) {
        AuditLogEvent auditLogEvent = new AuditLogEvent();

        AuditLogEventType auditLogEventType = new AuditLogEventType();
        auditLogEventType.setAuditLogEventTypeId(auditLogEventDTO.getAuditLogEventTypId());
        auditLogEvent.setAuditLogEventType(auditLogEventType);

        auditLogEvent.setEntityId(Integer.valueOf(auditLogEventDTO.getEntityId()));
        auditLogEvent.setEntityName(auditLogEventDTO.getEntityName());
        auditLogEvent.setAuditTable(auditLogEventDTO.getAuditTable());

        auditLogEvent.setOldValue(cryptoConverter.convertToDatabaseColumn(auditLogEventDTO.getOldValue()));
        auditLogEvent.setNewValue(cryptoConverter.convertToDatabaseColumn(auditLogEventDTO.getNewValue()));

        auditLogEvent.setAuditDate(new Timestamp(System.currentTimeMillis()));
        UserAccount userAccount = userAccountDao.findByUserId(ub.getId());
        auditLogEvent.setUserAccount(userAccount);


        return auditLogEvent;
    }

}
