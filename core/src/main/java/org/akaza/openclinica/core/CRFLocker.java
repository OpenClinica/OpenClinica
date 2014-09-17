/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.core;

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
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 *
 */
public class CRFLocker implements Serializable {

    private static final long serialVersionUID = -541015729642748245L;

    private final ConcurrentMap<Integer, Integer> lockedCRFs = new ConcurrentHashMap<Integer, Integer>();

    /**
     * Locks a CRF for an user.
     *
     * @param crfId ID of the CFR to be locked.
     * @param userId ID of the user who owns the lock.
     */
    public void lock(int crfId, int userId) {
        lockedCRFs.put(crfId, userId);
    }

    /**
     * Unlocks a CRF.
     *
     * @param crfId The ID of the CRF to be unlocked
     */
    public void unlock(int crfId) {
        lockedCRFs.remove(crfId);
    }

    /**
     * Release all the locks owned by a user.
     *
     * @param userId ID of the user.
     */
    public void unlockAllForUser(int userId) {
        synchronized (lockedCRFs) {
            Set<Entry<Integer, Integer>> entries = lockedCRFs.entrySet();
            Iterator<Entry<Integer, Integer>> it = entries.iterator();
            while (it.hasNext()) {
                Entry<Integer, Integer> entry = it.next();
                if (entry.getValue().equals(userId)) {
                    it.remove();
                }
            }
        }
    }

    /**
     * If the CRF is locked by a user.
     *
     * @param crfId ID of the CRF.
     * @return
     */
    public boolean isLocked(int crfId) {
        return lockedCRFs.containsKey(crfId);
    }

    /**
     * Identifies the owner of a CRF lock.
     *
     * @param crfId ID of the CRF.
     * @return ID of the user who owns the lock, <code>null</code> if the CRF is not locked.
     */
    public Integer getLockOwner(int crfId) {
        return lockedCRFs.get(crfId);
    }

}
