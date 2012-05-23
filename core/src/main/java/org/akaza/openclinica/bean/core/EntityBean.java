/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.bean.core;

/**
 * @author thickerson
 *
 *
 */
public class EntityBean implements java.io.Serializable {

    // ss - changed visibility of these fields so Term could see them
    // think we should change all fields to protected here
    protected String name;
    protected int id;

    /**
     * The setID method has changed so that if the id is greater than 0, active
     * is set to true. This reflects our notion that an entity is active if it
     * comes from the database, and otherwise inactive. Note however that if a
     * bean is retrieved from the database, changed in the application, and then
     * updated in the databse, it should be changed when it is changed  the
     * notion being that the bean no longer reflects the current state of the
     * database. The relevant DAOs update method should set active to true
     * again once the database has been successfully changed.
     */
    protected boolean active = false;

    /*
     * private java.util.Date createdDate; private java.util.Date updatedDate;
     * private Object owner;//to be replaced by UserBean, when written private
     * Object updater;//to be replaced by UserBean
     */

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (active ? 1231 : 1237);
        result = prime * result + id;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EntityBean other = (EntityBean) obj;
        if (active != other.active)
            return false;
        if (id != other.id)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    public EntityBean() {
        id = 0;
        name = "";
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setId(int id) {
        this.id = id;

        if (id > 0) {
            active = true;
        }
    }

    public int getId() {
        return this.id;
    }

    /*
     * public void setCreatedDate(Date date) { this.createdDate = date; } public
     * Date getCreatedDate() { return this.createdDate; } public void
     * setUpdatedDate(Date date) { this.updatedDate = date; } public Date
     * getUpdatedDate() { return this.updatedDate; } public void setOwner(Object
     * user) { this.owner = user;//to be replaced by userbean } public Object
     * getOwner() { return this.owner; } public void setUpdater(Object user) {
     * this.updater = user; } public Object getUpdater() { return this.updater; }
     */
    // the above will now be part of auditable entity bean, tbh
}
