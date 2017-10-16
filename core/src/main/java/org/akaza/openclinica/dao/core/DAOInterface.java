/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.dao.core;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.exception.OpenClinicaException;

import java.util.Collection;
import java.util.HashMap;

/**
 * DAOInterface.java, created to enforce several methods in our EntityDAO and
 * AuditableEntityDAO framework. Note that we have to enforce them as basic
 * objects, since we will be using them across all DAOs. This is the spot for
 * adding required classes such as update() insert() and other selects().
 *
 * @author thickerson
 *
 *
 */
public interface DAOInterface {
    // problem here is to prevent beans which recursively access themselves;
    // if we don't have a special boolean, the user account bean will recurse
    // until
    // the virtual machine runs out of memory, looking for its owner of its
    // owner.
    Object getEntityFromHashMap(HashMap hm);

    Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) throws OpenClinicaException;

    Collection findAll() throws OpenClinicaException;

    EntityBean findByPK(int id) throws OpenClinicaException;

    EntityBean create(EntityBean eb) throws OpenClinicaException;

    EntityBean update(EntityBean eb) throws OpenClinicaException;

    Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase)
            throws OpenClinicaException;

    Collection findAllByPermission(Object objCurrentUser, int intActionType) throws OpenClinicaException;
    // perhaps also add one with just object and int????

}
