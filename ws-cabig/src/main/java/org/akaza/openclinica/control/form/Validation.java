/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2005 Akaza
 * Research
 */
package org.akaza.openclinica.control.form;

import java.util.ArrayList;

public class Validation {
    private int type;

    private String errorMessage;

    private boolean errorMessageSet;

    private ArrayList arguments;

    private boolean alwaysExecuted;

    public Validation(int type) {
        this.type = type;
        arguments = new ArrayList();
        errorMessage = "";
        alwaysExecuted = false;
    }

    /**
     * @return Returns the arguments.
     */
    public ArrayList getArguments() {
        return arguments;
    }

    /**
     * @param arguments
     *            The arguments to set.
     */
    public void setArguments(ArrayList arguments) {
        this.arguments = arguments;
    }

    /**
     * @return Returns the type.
     */
    public int getType() {
        return type;
    }

    /**
     * @param type
     *            The type to set.
     */
    public void setType(int type) {
        this.type = type;
    }

    public void addArgument(Object arg) {
        arguments.add(arg);
    }

    public void addArgument(int arg) {
        arguments.add(new Integer(arg));
    }

    public void addArgument(boolean b) {
        arguments.add(new Boolean(b));
    }

    public void addArgument(float arg) {
        arguments.add(new Float(arg));
    }

    /*
     * Gets the boolean value of the argument with the specified index.
     */
    public boolean getBoolean(int index) {
        if (index >= arguments.size()) {
            return false;
        }

        Boolean b = (Boolean) arguments.get(index);
        return b.booleanValue();
    }

    /*
     * Gets the integer value of the argument with the specified index.
     */
    public int getInt(int index) {
        if (index >= arguments.size()) {
            return 0;
        }

        Integer i = (Integer) arguments.get(index);
        return i.intValue();
    }

    /*
     * Gets the float value of the argument with the specified index.
     */
    public float getFloat(int index) {
        if (index >= arguments.size()) {
            return 0;
        }

        try {
            Float i = (Float) arguments.get(index);
            return i.floatValue();

        } catch (ClassCastException ce) {
            Integer i = (Integer) arguments.get(index);
            return i.intValue();
        }

    }

    public String getString(int index) {
        if (index >= arguments.size()) {
            return "";
        }

        String s = (String) arguments.get(index);
        return s;
    }

    public Object getArg(int index) {
        if (index >= arguments.size()) {
            return null;
        }

        return arguments.get(index);
    }

    /**
     * @return Returns the errorMessage.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @param errorMessage
     *            The errorMessage to set.
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        errorMessageSet = true;
    }

    /**
     * @return Returns the errorMessageSet.
     */
    public boolean isErrorMessageSet() {
        return errorMessageSet;
    }

    /**
     * @return Returns the alwaysExecuted.
     */
    public boolean isAlwaysExecuted() {
        return alwaysExecuted;
    }

    /**
     * @param alwaysExecuted
     *            The alwaysExecuted to set.
     */
    public void setAlwaysExecuted(boolean alwaysExecuted) {
        this.alwaysExecuted = alwaysExecuted;
    }
}