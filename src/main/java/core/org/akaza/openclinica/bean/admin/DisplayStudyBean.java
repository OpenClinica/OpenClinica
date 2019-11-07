/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright  2003-2005 Akaza Research
 */

package core.org.akaza.openclinica.bean.admin;

import core.org.akaza.openclinica.bean.core.AuditableEntityBean;
import core.org.akaza.openclinica.domain.datamap.Study;

import java.util.ArrayList;

/**
 * @author Jun Xu
 *
 * A class for display study list properly, group studies by parent and child
 * relationship
 */
public class DisplayStudyBean extends AuditableEntityBean {
    private Study parent;
    private ArrayList children;

    /**
     * @return Returns the children.
     */
    public ArrayList getChildren() {
        return children;
    }

    /**
     * @param children
     *            The children to set.
     */
    public void setChildren(ArrayList children) {
        this.children = children;
    }

    /**
     * @return Returns the parent.
     */
    public Study getParent() {
        return parent;
    }

    /**
     * @param parent
     *            The parent to set.
     */
    public void setParent(Study parent) {
        this.parent = parent;
    }
}
