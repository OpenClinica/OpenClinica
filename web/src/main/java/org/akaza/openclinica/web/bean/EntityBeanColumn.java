/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.web.bean;

/**
 * @author ssachs
 */
public class EntityBeanColumn {
    private String name;
    private boolean showLink;

    public EntityBeanColumn() {
        name = "";
        showLink = true;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the showLink.
     */
    public boolean isShowLink() {
        return showLink;
    }

    /**
     * @param showLink
     *            The showLink to set.
     */
    public void setShowLink(boolean showLink) {
        this.showLink = showLink;
    }
}
