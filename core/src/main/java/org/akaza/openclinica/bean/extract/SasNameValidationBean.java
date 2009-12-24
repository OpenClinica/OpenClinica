/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2005 Akaza
 * Research
 *
 * Created on Oct., 2006
 */

package org.akaza.openclinica.bean.extract;

import java.util.TreeSet;

/**
 * Validate if a SAS variable name is valid.
 * 
 * <p>
 * A valid SAS variable name should follow the rule that
 * <li> it can be up to eight characters long
 * <li> it can consist of only letters, digits and underscore characters.
 * <li> its first character cannot be a digit <br>
 * <p>
 * Rules for creating a valid SAS variable name:
 * <li> Replace any invalid character with an underscore
 * <li> If the first character is a digit, it is replaced by an underscore
 * <li> If a name is longer than 8 characters, it will be truncated to 8
 * characters. If it results in non-unique name in a data file, sequential
 * numbers are used to replace its letters at the end. By default, the size of
 * sequential numbers is 3.
 * 
 * 
 * @auther ywang
 */

public class SasNameValidationBean {
    private TreeSet<String> uniqueNameTable = new TreeSet<String>();
    private int digitSize;
    private int sequential;
    private char replacingChar;
    private int nameMaxLength; // 8;// 32;
    private int largestValue;

    public SasNameValidationBean() {
        this.digitSize = 3;
        this.sequential = 1;
        this.replacingChar = '_';
        this.nameMaxLength = 8;
        this.largestValue = 1000;
    }

    /**
     * Given a variable name, this methods returns a valid SAS name and it
     * guarantees the uniqueness of this name
     * 
     * @param variableName
     *            String
     * @return String
     */
    public String getValidSasName(String variableName) {
        // if variableName is null, automatically generate
        if (variableName == null || variableName.trim().length() == 0) {
            return getNextSequentialString();
        }
        int i;

        // get all chars from the string first
        String temp = variableName.trim();
        char c[] = temp.length() > this.nameMaxLength ? temp.substring(0, this.nameMaxLength).toCharArray() : temp.toCharArray();

        // replacing every invalid character with the replacingChar
        for (i = 0; i < c.length; ++i) {
            if (!isValid(c[i])) {
                c[i] = this.replacingChar;
            }
        }

        // if the first one is a digit
        if (c[0] >= '0' && c[0] <= '9') {
            // if there is already 32 characters
            if (c.length >= this.nameMaxLength) {
                for (i = c.length - 1; i >= 1; --i) {
                    c[i] = c[i - 1];
                }
                c[0] = this.replacingChar;
            } else {
                char cc[] = new char[c.length + 1];
                cc[0] = this.replacingChar;
                for (i = 1; i < cc.length; ++i) {
                    cc[i] = c[i - 1];
                }
                c = cc;
            }
        }
        String s = new String(c);
        String s2 = s;
        int mysize = this.nameMaxLength - this.digitSize;
        // if not unique
        while (this.uniqueNameTable.contains(s2)) {
            if (s.length() > mysize) {
                s2 = s.substring(0, mysize) + this.getNextSequentialString();
            } else {
                s2 = s + this.getNextSequentialString();
            }
        }
        uniqueNameTable.add(s2);
        return s2;
    }

    // only alphabets, digits, and _ are valid
    private static boolean isValid(char c) {
        return c >= '0' && c <= '9' || c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_';
    }

    /**
     * Get next sequential String
     * 
     * @return String
     */
    public String getNextSequentialString() {
        String s = "" + sequential;
        for (int i = s.length(); i < this.digitSize; ++i) {
            s = "0" + s;
        }
        this.sequential++;
        if (this.sequential >= this.largestValue) {
            this.sequential = 1;
        }
        return s;
    }

    /**
     * Set maximum digits of a sequential number
     * 
     * @param size
     *            int
     */
    public void setDigitSizeAndLargestValue(int size) {
        this.digitSize = size;
        this.largestValue = (int) Math.pow(10, this.digitSize);
    }

    /**
     * @return int
     */
    public int getDigitSize() {
        return this.digitSize;
    }

    /**
     * @param c
     *            char
     */
    public void setReplacingChar(char c) {
        this.replacingChar = c;
    }

    /**
     * @return char
     */
    public char getReplacingChar() {
        return this.replacingChar;
    }

    /**
     * 
     * @return int
     */
    public int getMaxNameLength() {
        return this.nameMaxLength;
    }

    /**
     * @param len
     *            int
     */
    public void setMaxNameLength(int len) {
        this.nameMaxLength = len;
    }

    public void setUniqueNameTable(TreeSet<String> nameTable) {
        this.uniqueNameTable = nameTable;
    }

    public TreeSet<String> getUniqueNameTable() {
        return this.uniqueNameTable;
    }
}