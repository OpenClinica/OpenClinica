/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.core;

import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.submit.FormLayoutBean;
import org.akaza.openclinica.domain.datamap.FormLayout;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Data strucutre used to keep track of CRFs locked by users. The synchronization of access to the locks is implemented
 * internally, so clients of this class don't have to deal with it.
 *
 * @author Yogi Shridhare
 *
 */
public class EventCRFLocker implements Serializable {

    private static final long serialVersionUID = -541015729642748245L;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ConcurrentHashMap<String, LockInfo> lockedCRFs = new ConcurrentHashMap<>();
    /**
     * Locks a CRF for an user.
     *
     */
    public synchronized boolean lock(StudyEvent se, FormLayout fl, String schemaName, int userId, String sessionId) {
        if (!isLocked(se, fl, schemaName, userId, sessionId)) {
            lockedCRFs.put(createEventCrfLockKey(se, fl, schemaName), new LockInfo(userId, sessionId));
            logger.info("::::::::::::::::::::::Successfully locked for user:" + userId);
            return true;
        }
        logger.info("::::::::::::::::::::::Could not lock for user:" + userId);
        return false;
    }

    public synchronized boolean lock(String ecId, Integer userId, String sessionId) {
        if (!isLocked(ecId, userId, sessionId)) {
            lockedCRFs.put(ecId, new LockInfo(userId, sessionId));
            logger.debug("::::::::::::::::::::::Successfully locked ecId: " + ecId + " for user:" + userId);
            return true;
        }
        logger.debug("::::::::::::::::::::::Could not lock ecId: " + ecId + " for user:" + userId);
        return false;
    }
    
    /**
     * Release all the locks owned by a user.
     *
     * @param userId ID of the user.
     */
    public void unlockAllForUser(int userId) {
        synchronized (lockedCRFs) {
            logger.debug("::::::::::::::::::::::Unlock for user:" + userId);
            lockedCRFs.forEach((k, v) -> logger.debug("::::::::::::::::::::::unlockAllForUser key: " + k + " value:" + v));

            Set<Entry<String, LockInfo>> entries = lockedCRFs.entrySet();
            Iterator<Entry<String, LockInfo>> it = entries.iterator();
            while (it.hasNext()) {
                Entry<String, LockInfo> entry = it.next();
                if (entry.getValue().getUserId() == userId) {
                    logger.info("Removed lock:" + entry.getKey() + " for user:" + entry.getValue());
                    it.remove();
                }
            }
        }

    }

    public void unlockAllForUserPerSession(int userId, String sessionId) {
        synchronized (lockedCRFs) {
            logger.debug("::::::::::::::::::::::Unlock for user:" + userId);
            lockedCRFs.forEach((k, v) -> logger.debug("::::::::::::::::::::::unlockAllForUser key: " + k + " value:" + v));

            Set<Entry<String, LockInfo>> entries = lockedCRFs.entrySet();
            Iterator<Entry<String, LockInfo>> it = entries.iterator();
            while (it.hasNext()) {
                Entry<String, LockInfo> entry = it.next();
                if ((entry.getValue().getUserId() == userId)
                    && (entry.getValue().getSessionId().equals(sessionId))) {
                    logger.info("Removed lock:" + entry.getKey() + " for user:" + entry.getValue());
                    it.remove();
                }
            }
        }

    }


    /**
     * If the CRF is locked by a user.
     *
     */
    public boolean isLocked(StudyEvent se, FormLayout fl, String schemaName, Integer requestUserId, String sessionId) {
       return isLocked(createEventCrfLockKey(se, fl, schemaName), requestUserId, sessionId);
    }


    public boolean isLocked(String ecId, Integer requestUserId, String sessionId) {
        logger.debug("::::::::::::::::::::::Check lock for ecId:" + ecId + " and requestUserId:" + requestUserId);
        lockedCRFs.forEach((k,v) -> logger.debug("::::::::::::::::::::::key: "+k+" value:"+v));

        LockInfo lockInfo = lockedCRFs.get(ecId);
        if (lockInfo != null) {
            Integer userId = lockInfo.userId;

            if (requestUserId != null && requestUserId == userId) {
                logger.debug("::::::::::::::::::::::Not locked");
                return false;
            }
            logger.debug("::::::::::::::::::::::Locked by:" + userId);
            return true;
        }
        logger.debug("::::::::::::::::::::::Not locked");
        return false;
    }

    /**
     * Identifies the owner of a CRF lock.
     *
     */
    public LockInfo getLockOwner(StudyEvent se, FormLayout fl, String schemaName) {

        return lockedCRFs.get(createEventCrfLockKey(se, fl, schemaName));
    }

    public LockInfo getLockOwner(StudyEventBean se, FormLayoutBean fl, String schemaName) {

        return lockedCRFs.get(createEventCrfLockKey(se, fl, schemaName));
    }

    public String createEventCrfLockKey(StudyEvent se, FormLayout fl, String schemaName) {
        String key = "";
        if (se != null && fl != null) {
            key = schemaName + se.getStudyEventId() + fl.getFormLayoutId();
        }
        return key;
    }

    public String createEventCrfLockKey(StudyEventBean se, FormLayoutBean fl, String schemaName) {
        String key = "";
        if (se != null && fl != null) {
            key = schemaName + se.getId() + fl.getId();
        }
        return key;
    }

    public LockInfo getLockOwner(String ecId) {
        return lockedCRFs.get(ecId);
    }

}
