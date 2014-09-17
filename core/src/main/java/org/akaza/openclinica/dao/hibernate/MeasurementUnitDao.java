/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */

package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;
import java.util.TreeSet;

import org.akaza.openclinica.domain.admin.MeasurementUnit;

public class MeasurementUnitDao extends AbstractDomainDao<MeasurementUnit> {
    @Override
    Class<MeasurementUnit> domainClass() {
        return MeasurementUnit.class;
    }

    public TreeSet<String> findAllOIDs() {
        String query = "select mu.ocOid from  " + this.getDomainClassName() + " mu order by mu.ocOid asc";
        org.hibernate.Query q = this.getCurrentSession().createQuery(query);
        return new TreeSet<String>(q.list());
    }

    public TreeSet<String> findAllNames() {
        String query = "select distinct mu.name from  " + this.getDomainClassName() + " mu order by mu.name asc";
        org.hibernate.Query q = this.getCurrentSession().createQuery(query);
        return new TreeSet<String>(q.list());
    }

    public TreeSet<String> findAllNamesInUpperCase() {
        String query = "select upper(mu.name) from  " + this.getDomainClassName() + " mu order by mu.name asc";
        org.hibernate.Query q = this.getCurrentSession().createQuery(query);
        ArrayList<String> l = (ArrayList<String>)q.list();
        TreeSet<String> newSet = new TreeSet<String>();
        for(String i : l) {
            newSet.add(i);
        }
        return newSet;
    }
}