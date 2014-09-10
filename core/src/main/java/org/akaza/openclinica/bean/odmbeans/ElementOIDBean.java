/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2005 Akaza
 * Research
 *
 */

package org.akaza.openclinica.bean.odmbeans;


/**
 * 
 * @author ywang (May, 2008)
 * 
 */
public class ElementOIDBean implements Comparable {
    private String oid;

    public int compareTo(Object o) {
        ElementOIDBean b = (ElementOIDBean) o;
        return this.oid.compareTo(b.getOid());
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getOid() {
        return this.oid;
    }
}