package org.akaza.openclinica.logic.score.function;

/**
 * This is the base class for the Function interface, which contains common
 * fields and methods.
 *
 * @author Hailong Wang, Ph.D
 * @veresion 1.0 08/25/2006
 *
 * <p>Modified [ywang 1-16-2008]. Variable and argument parse process have been removed.
 */

import org.akaza.openclinica.bean.submit.ItemBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public abstract class AbstractFunction implements Function {
    /**
     * The argument list of this function
     */
    protected List<Object> arguments;

    /**
     * The value calculated out by this function on the arguments.
     */
    protected String value;

    /**
     * The error messages when performing the evaluation happened.
     */
    protected HashMap<Integer, String> errors;

    protected int errorCount;

    /**
     * The logger.
     */
    protected Logger logger;

    public AbstractFunction() {
        logger = LoggerFactory.getLogger(getClass().getName());
        arguments = new Vector<Object>(20);
        errors = new HashMap<Integer, String>(10);
        errorCount = 0;
    }

    /**
     * @see Function#addArgument(Object)
     */
    public void addArgument(Object arg) {
        arguments.add(arg);
    }

    /**
     * @see Function#setValue(String)
     */
    public void setValue(String newValue) {
        this.value = newValue;
    }

    /**
     * @see Function#getValue()
     */
    public String getValue() {
        return value;
    }

    /**
     * @see Function#argumentCount()
     */
    public int argumentCount() {
        return arguments.size();
    }

    /**
     * @see Function#getArgument(int)
     */
    public Object getArgument(int index) {
        return arguments.get(index);
    }

    /**
     * @see Function#setArguments(List)
     */
    public void setArguments(List<Object> arguments) {
        this.arguments = arguments;
    }

    /**
     * @see Function#getErrors()
     */
    public HashMap<Integer, String> getErrors() {
        return errors;
    }

    /**
     * has not been implemented
     */
    public List<Object> getScript() {
        return null;
    }

    /**
     * has not been implemented
     */
    public HashMap<ItemBean, String> getAssignments() {
        return null;
    }
}
