/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.bean.extract;

import org.akaza.openclinica.bean.core.AuditableEntityBean;

import java.util.ArrayList;

/**
 * FilterBean.java, meant to take the place of Query Bean.
 *
 * @author thickerson
 *
 *
 */

public class FilterBean extends AuditableEntityBean {
    private String description;
    private String SQLStatement;
    private ArrayList filterDataObjects;
    private String explanation;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSQLStatement() {
        return SQLStatement;
    }

    public void setSQLStatement(String statement) {
        SQLStatement = statement;
    }

    public ArrayList getFilterDataObjects() {
        return filterDataObjects;
    }

    public void setFilterDataObjects(ArrayList filterDataObjects) {
        this.filterDataObjects = filterDataObjects;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
