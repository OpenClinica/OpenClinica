/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.bean.service;

import org.akaza.openclinica.bean.core.AuditableEntityBean;

public class StudyParameter extends AuditableEntityBean {
    /*
     * study_parameter_id serial NOT NULL, handle varchar(50), name varchar(50),
     * description varchar(255), default_value varchar(50), inheritable bool
     * DEFAULT true, overridable bool,
     */
    private String handle;
    private String name;
    private String description;
    private String defaultValue;
    private boolean inheritable;
    private boolean overridable;

    public StudyParameter() {
        handle = "";
        name = "";
        description = "";
        defaultValue = "";
        inheritable = true;
        overridable = false;
    }

    /**
     * @return Returns the defaultValue.
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * @param defaultValue
     *            The defaultValue to set.
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description
     *            The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return Returns the handle.
     */
    public String getHandle() {
        return handle;
    }

    /**
     * @param handle
     *            The handle to set.
     */
    public void setHandle(String handle) {
        this.handle = handle;
    }

    /**
     * @return Returns the inheritable.
     */
    public boolean isInheritable() {
        return inheritable;
    }

    /**
     * @param inheritable
     *            The inheritable to set.
     */
    public void setInheritable(boolean inheritable) {
        this.inheritable = inheritable;
    }

    /**
     * @return Returns the name.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            The name to set.
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the overridable.
     */
    public boolean isOverridable() {
        return overridable;
    }

    /**
     * @param overridable
     *            The overridable to set.
     */
    public void setOverridable(boolean overridable) {
        this.overridable = overridable;
    }

}
