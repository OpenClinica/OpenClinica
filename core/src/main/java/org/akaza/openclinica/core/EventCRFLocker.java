/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.core;

import org.akaza.openclinica.domain.datamap.FormLayout;
import org.akaza.openclinica.domain.datamap.StudyEvent;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
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

    private final ConcurrentMap<String, Integer> lockedCRFs = new ConcurrentHashMap<>();

    /**
     * Locks a CRF for an user.
     *
     */
    public void lock(StudyEvent se, FormLayout fl, String schemaName, int userId) {
        lockedCRFs.put(createEventCrfLockKey(se, fl, schemaName), userId);
    }

    public void lock(String ecId, Integer userId) {
        lockedCRFs.put(ecId, userId);
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
        synchronized (lockedCRFs) {
            Set<Entry<String, Integer>> entries = lockedCRFs.entrySet();
            Iterator<Entry<String, Integer>> it = entries.iterator();
            while (it.hasNext()) {
                Entry<String, Integer> entry = it.next();
                if (entry.getValue().equals(userId)) {
                    it.remove();
                }
            }
        }
    }

    /**
     * If the CRF is locked by a user.
     *
     */
    public boolean isLocked(StudyEvent se, FormLayout fl, String schemaName) {

        return lockedCRFs.containsKey(createEventCrfLockKey(se, fl, schemaName));
    }


    public boolean isLocked(String ecId) {

        return lockedCRFs.containsKey(ecId);
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
