/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2005 Akaza
 * Research
 *
 * Created on May, 2008
 */

package org.akaza.openclinica.bean.extract;

import java.util.TreeSet;

/**
 * Validate if a customized SASFormatName is valid.
 * 
 * <p>
 * A valid customized SASFormatName should follow the rule that
 * <ul>
 * <li> it can be up to eight characters long
 * <li> it can consist of only letters, digits and underscore characters, but
 * there are exceptions for its first character.
 * <li> if it formats a character variable, it should start with a dollar sign,
 * i.e. "$"
 * <li> its first character cannot be a digit and underscore is not recommended
 * <li> a character format name cannot end with a number.
 * </ul>
 * <br>
 * <p>
 * Rules for creating a valid customized SASFormatName:
 * <ul>
 * <li> Replace any invalid character with an underscore
 * <li> If the first character is a digit, in front of it puts letter "X".
 * <li> If a name is longer than 8 characters, it will be truncated to 8
 * characters. If it results in non-unique name in a data file, the last 3
 * characters will be changed as the pattern sequentialNumber-englishAlphabet.
 * The size of sequential number is 2.
 * </ul>
 * 
 * 
 * @auther ywang (May, 2008)
 */

public class ODMSASFormatNameValidator {
    private TreeSet<String> uniqueNameTable = new TreeSet<String>();
    private int digitSize;
    private char[] tails;
    private int sequential;
    private char firstChar;
    private char replacingChar;
    private int nameMaxLength;// 8;// 32;
    private int largestValue;

    public ODMSASFormatNameValidator() {
        this.digitSize = 2;
        this.largestValue = 100;
        this.sequential = 1;
        this.firstChar = 'X';
        this.replacingChar = '_';
        this.nameMaxLength = 8;
        this.initTails();
    }

    /**
     * Given a name, this methods returns a valid SASFormatName and it
     * guarantees the uniqueness of this name
     * 
     * @param name
     *            String
     * @return String
     */
    public String getValidSASFormatName(String name, boolean isCharacter) {
        // if passed name is null, automatically generate
        if (name == null || name.trim().length() == 0) {
            String sas = this.firstChar + getNextSequentialString() + this.firstChar;
            return isCharacter ? "$" + sas : sas;
        }

        // get all chars from the string first
        String temp = isCharacter && !name.trim().startsWith("$") ? "$" + name.trim().toUpperCase() : name.trim().toUpperCase();
        char c[] = temp.length() > this.nameMaxLength ? temp.substring(0, this.nameMaxLength).toCharArray() : temp.toCharArray();

        // replacing every invalid character with the replacingChar
        int j = isCharacter ? 1 : 0;
        int i;
        for (i = j; i < c.length; ++i) {
            if (!isValid(c[i])) {
                c[i] = this.replacingChar;
            }
        }

        // if the first one is a digit or underscore
        char f = isCharacter ? c[1] : c[0];
        if (f >= '0' && f <= '9' || f == '_') {
            // if there is already max length characters
            if (c.length >= this.nameMaxLength) {
                for (i = c.length - 1; i >= 1; --i) {
                    c[i] = c[i - 1];
                }
                c[0] = this.firstChar;
            } else {
                char cc[] = new char[c.length + 1];
                cc[0] = this.firstChar;
                for (i = 1; i < cc.length; ++i) {
                    cc[i] = c[i - 1];
                }
                c = cc;
            }
        }
        if (isCharacter && c[1] == '$') {
            char t = c[0];
            c[0] = c[1];
            c[1] = t;
        }

        int mysize = this.nameMaxLength - this.digitSize - 1;
        String s = new String(c);
        // handle last character as a number
        if (Character.isDigit(c[c.length - 1])) {
            if (s.length() < this.nameMaxLength) {
                s += "X";
            } else {
                s =
                    Character.isDigit(c[this.nameMaxLength - 2]) ? s.substring(0, this.nameMaxLength - 1) + "X" : s.substring(0, mysize)
                        + this.getNextSequentialString() + 'X';
            }
        }
        String s2 = s;
        int index = 0;
        // if not unique
        while (this.uniqueNameTable.contains(s2)) {
            String seq = this.getNextSequentialString();
            s2 = s.length() > mysize ? s.substring(0, mysize) + seq + tails[index] : s + seq + tails[index];
            index = Integer.valueOf(seq) >= this.largestValue - 1 ? index + 1 : index;
        }
        uniqueNameTable.add(s2);
        return s2;
    }

    private void initTails() {
        String a = "XYZABCDEFGHIJKLMNOPQRSTUVW";
        this.tails = a.toCharArray();
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

    public void setFirstChar(char c) {
        this.firstChar = c;
    }

    public char getFirstChar() {
        return this.firstChar;
    }

    public void setLargestValue(int v) {
        this.largestValue = v;
    }

    public int getLargestValue() {
        return this.largestValue;
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