/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.core;

import org.akaza.openclinica.domain.datamap.FormLayout;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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


    private final Map<String, Integer> lockedCRFs = Collections.synchronizedMap(new HashMap<>());

    /**
     * Locks a CRF for an user.
     *
     */
    public boolean lock(StudyEvent se, FormLayout fl, String schemaName, int userId) {
        if (!isLocked(se, fl, schemaName, userId)) {
            lockedCRFs.put(createEventCrfLockKey(se, fl, schemaName), userId);
            logger.info("::::::::::::::::::::::Successfully locked for user:" + userId);
            return true;
        }
        logger.info("::::::::::::::::::::::Could not lock for user:" + userId);
        return false;
    }

    public boolean lock(String ecId, Integer userId) {
        if (!isLocked(ecId, userId)) {
            lockedCRFs.put(ecId, userId);
            logger.info("::::::::::::::::::::::Successfully locked ecId: " + ecId + " for user:" + userId);
            return true;
        }
        logger.info("::::::::::::::::::::::Could not lock ecId: " + ecId + " for user:" + userId);
        return false;
    }

    /**
     * Unlocks a CRF.
     *
     * @param eventCrf The ID of the CRF to be unlocked
     */
    public void unlock(String eventCrf) {
        lockedCRFs.remove(eventCrf);
    }

    /**
     * Release all the locks owned by a user.
     *
     * @param userId ID of the user.
     */
    public void unlockAllForUser(int userId) {
        logger.info("::::::::::::::::::::::Unlock for user:" + userId);
        lockedCRFs.forEach((k,v) -> logger.info("::::::::::::::::::::::unlockAllForUser key: "+k+" value:"+v));

        Set<Entry<String, Integer>> entries = lockedCRFs.entrySet();
        Iterator<Entry<String, Integer>> it = entries.iterator();
        while (it.hasNext()) {
            Entry<String, Integer> entry = it.next();
            if (entry.getValue().equals(userId)) {
                logger.debug("Removed lock:" + entry.getKey() + " for user:" + entry.getValue() );
                it.remove();
            }
        }

    }


    /**
     * If the CRF is locked by a user.
     *
     */
    public boolean isLocked(StudyEvent se, FormLayout fl, String schemaName, Integer requestUserId) {
       return isLocked(createEventCrfLockKey(se, fl, schemaName), requestUserId);
    }


    public boolean isLocked(String ecId, Integer requestUserId) {
        logger.info("::::::::::::::::::::::Check lock for ecId:" + ecId + " and requestUserId:" + requestUserId);
        lockedCRFs.forEach((k,v) -> logger.info("::::::::::::::::::::::key: "+k+" value:"+v));

        Integer userId = lockedCRFs.get(ecId);

        if (userId != null) {
            if (requestUserId != null && requestUserId == userId) {
                logger.info("::::::::::::::::::::::Not locked");
                return false;
            }
            logger.info("::::::::::::::::::::::Locked by:" + userId);
            return true;
        }
        logger.info("::::::::::::::::::::::Not locked");
        return false;
    }

    /**
     * Identifies the owner of a CRF lock.
     *
     */
    public Integer getLockOwner(StudyEvent se, FormLayout fl, String schemaName) {

        return lockedCRFs.get(createEventCrfLockKey(se, fl, schemaName));
    }

    public String createEventCrfLockKey(StudyEvent se, FormLayout fl, String schemaName) {
        String key = "";
        if (se != null && fl != null) {
            key = schemaName + se.getStudyEventId() + fl.getFormLayoutId();
        }
        return key;
    }

    public Integer getLockOwner(String ecId) {
        return lockedCRFs.get(ecId);
    }

}
