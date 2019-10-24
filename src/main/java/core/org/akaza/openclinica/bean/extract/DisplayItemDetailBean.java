/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package core.org.akaza.openclinica.bean.extract;

import core.org.akaza.openclinica.bean.submit.ItemBean;

import java.util.HashMap;

/**
 * @author jxu
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class DisplayItemDetailBean {
    private ItemBean item;
    private HashMap metaForVersion = new HashMap();

    /**
     * @return Returns the item.
     */
    public ItemBean getItem() {
        return item;
    }

    /**
     * @param item
     *            The item to set.
     */
    public void setItem(ItemBean item) {
        this.item = item;
    }

    /**
     * @return Returns the metaForVersion.
     */
    public HashMap getMetaForVersion() {
        return metaForVersion;
    }

    /**
     * @param metaForVersion
     *            The metaForVersion to set.
     */
    public void setMetaForVersion(HashMap metaForVersion) {
        this.metaForVersion = metaForVersion;
    }
}
