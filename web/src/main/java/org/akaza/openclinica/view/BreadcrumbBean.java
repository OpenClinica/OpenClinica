/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.view;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.core.Status;

/**
 * <P>
 * BreadcrumbBean.java, by Tom Hickerson.
 * <P>
 * A bean to be used in a BreadcrumbTrail.
 * <P>
 * TODO make sure that Page does not lead to the JSP directly, but back to the
 * servlet; ie, "EditDataset?datasetId=9" instead of "editDataset.jsp", since
 * the link will break if it's just the JSP.
 *
 * @author thickerson
 *
 */
public class BreadcrumbBean extends EntityBean {

    private String url;
    private Status status;

    public BreadcrumbBean(String name, String url, int statusId) {
        this.setName(name);
        this.setUrl(url);
        this.setStatus(Status.get(statusId));
        // Available == clickable
        // Unavailable == grayed out
        // Pending == current link
    }

    public BreadcrumbBean(String name, String url, Status status) {
        this.setName(name);
        this.setUrl(url);
        this.setStatus(status);
        // Available == clickable
        // Unavailable == grayed out
        // Pending == current link
    }

    /**
     * @return Returns the url.
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url
     *            The url to set.
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return Returns the status.
     */
    public Status getStatus() {
        return status;
    }

    /**
     * @param status
     *            The status to set.
     */
    public void setStatus(Status status) {
        this.status = status;
    }
}
