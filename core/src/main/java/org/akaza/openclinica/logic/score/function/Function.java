package org.akaza.openclinica.logic.score.function;

import org.akaza.openclinica.bean.submit.ItemBean;

import java.util.HashMap;
import java.util.List;

/**
 * The Function defines the interface for the calculation, which includes
 * operator, fields and sub functions.
 *
 * @author Hailong Wang
 * @version 1.0
 * @since 08/21/2006
 *
 * <p>
 * modified by ywang 1-16-2008
 */

public interface Function {
    /**
     * @return the value which was calculated out by this function based on the
     *         arguments.
     */
    public String getValue();

    /**
     * Sets the function value to the aValue.
     *
     * @param new
     *            Value a new value.
     */
    public void setValue(String newValue);

    /**
     * Adds an argument to the function.
     *
     * @param arg
     *            an argument value.
     */
    public void addArgument(Object arg);

    /**
     * Returns the argument at the specified index.
     *
     * @param index
     *            an index
     * @return the argument at the specified index.
     */
    public Object getArgument(int index);

    /**
     * @param arguments
     *            the argument list for the function.
     */
    public void setArguments(List<Object> arguments);

    /**
     * Performs the function specified by this <code>Function</code>.
     */
    public void execute();

    /**
     * @return the number of the arguments this function has.
     */
    public int argumentCount();

    /**
     * @return the error messages.
     */
    public HashMap<Integer, String> getErrors();

    /*
     * @return the collection of variables
     */
    // public Set<ItemBean> getVariables(Collection<ItemBean> variables);
    /**
     * @return the javascript code that is equivalent to this function.
     */
    public List<Object> getScript();

    public HashMap<ItemBean, String> getAssignments();
}
