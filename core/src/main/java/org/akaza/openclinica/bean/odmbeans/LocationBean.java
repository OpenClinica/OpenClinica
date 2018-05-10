/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2010 Akaza
 * Research
 *
 */

package org.akaza.openclinica.bean.odmbeans;


/**
 *
 * @author ywang (March, 2010)
 *
 */
public class LocationBean extends ElementOIDBean {
    private String name;
    private MetaDataVersionRefBean metaDataVersionRef;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public MetaDataVersionRefBean getMetaDataVersionRef() {
        return metaDataVersionRef;
    }
    public void setMetaDataVersionRef(MetaDataVersionRefBean metaDataVersionRef) {
        this.metaDataVersionRef = metaDataVersionRef;
    }
}