/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2010 Akaza
 * Research
 * 
 */

package org.akaza.openclinica.bean.extract;

import java.util.TreeSet;

/**
 * Abstract class for special name validation
 * 
 * @auther ywang
 */

public abstract class NameValidator {
    protected TreeSet<String> uniqueNameTable = new TreeSet<String>();
    protected int digitSize;
    protected int sequential;
    protected char replacingChar;

    /**
     * By default, digitSize=3, sequential=0, replacingChar='_', 
     */
    public NameValidator() {
        this.digitSize = 3;
        this.sequential = 1;
        this.replacingChar = '_';
    }

    /**
     * Given a variable name, this methods returns a valid SAS name and it
     * guarantees the uniqueness of this name
     * 
     * @param variableName
     *            String
     * @return String
     */
    public abstract String getValidName(String variableName);

    protected abstract boolean isValid(char c);
    
    protected abstract String getNextSequentialString(int maxValue);
    
  

    public TreeSet<String> getUniqueNameTable() {
        return uniqueNameTable;
    }

    public void setUniqueNameTable(TreeSet<String> uniqueNameTable) {
        this.uniqueNameTable = uniqueNameTable;
    }

    public int getDigitSize() {
        return digitSize;
    }

    public void setDigitSize(int digitSize) {
        this.digitSize = digitSize;
    }

    public int getSequential() {
        return sequential;
    }

    public void setSequential(int sequential) {
        this.sequential = sequential;
    }

    public char getReplacingChar() {
        return replacingChar;
    }

    public void setReplacingChar(char replacingChar) {
        this.replacingChar = replacingChar;
    }
}