/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.domain.rule.expression;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.akaza.openclinica.domain.AbstractAuditableMutableDomainObject;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

/**
 * @author Krikor Krumlian
 * 
 */

@Entity
@Table(name = "rule_expression")
@GenericGenerator(name = "id-generator", strategy = "native", parameters = { @Parameter(name = "sequence", value = "rule_expression_id_seq") })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ExpressionBean extends AbstractAuditableMutableDomainObject implements Serializable {

    private Context context;
    private String value;
    private String contextName;

    public ExpressionBean() {
    }

    public ExpressionBean(Context context, String value) {
        this.context = context;
        this.value = value;
    }

    @Type(type = "ruleContext")
    @Column(name = "context")
    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Do not use this method for getting Context name. Use of this is strictly
     * for import validation.
     * 
     * @return String
     */
    @Transient
    public String getContextName() {
        return this.contextName;
    }

    /**
     * Do not use this method for setting Context name. Use of this is strictly
     * for import validation.
     * 
     * @param contextName
     */
    public void setContextName(String contextName) {
        this.contextName = contextName;
        this.context = Context.getByName(contextName);
    }

    @Override
    public String toString() {
        return "ExpressionBean [context=" + context + ", contextName=" + contextName + ", value=" + value + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((context == null) ? 0 : context.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
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
        ExpressionBean other = (ExpressionBean) obj;
        if (context == null) {
            if (other.context != null)
                return false;
        } else if (!context.equals(other.context))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }
}
