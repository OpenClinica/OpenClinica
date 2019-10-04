/*
 * Created on Sep 1, 2005
 *
 *
 */
package core.org.akaza.openclinica.logic.core;

import core.org.akaza.openclinica.bean.core.EntityBean;

/**
 * @author thickerson
 *
 *
 */
public interface BusinessRule {
    public abstract boolean isPropertyTrue(String s);

    public abstract EntityBean doAction(EntityBean o);
}
