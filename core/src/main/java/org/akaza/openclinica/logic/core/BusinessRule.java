/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
/*
 * Created on Sep 1, 2005
 *
 *
 */
package org.akaza.openclinica.logic.core;

import org.akaza.openclinica.bean.core.EntityBean;

/**
 * @author thickerson
 *
 *
 */
public interface BusinessRule {
    public abstract boolean isPropertyTrue(String s);

    public abstract EntityBean doAction(EntityBean o);
}
