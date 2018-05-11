/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2010 Akaza
 * Research
 */

package org.akaza.openclinica.bean.extract;


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
@Deprecated
public class SasNameValidator extends NameValidator {
    private int nameMaxLength = 8; // 8;// 32;

    /**
     * Get unique SAS name using 36 radix
     * 
     * @param String
     * 
     * @return String
     */
    @Override
    public String getValidName(String variableName) {
        int maxValue = this.computeMaxValue(36, this.digitSize);
        // if variableName is null, automatically generate
        if (variableName == null || variableName.trim().length() == 0) {
            return getNextSequentialString(maxValue);
        }
        int i;

        // get all chars from the string first
        String temp = variableName.trim();
        char c[] = temp.length() > this.nameMaxLength ? temp.substring(0, this.nameMaxLength).toCharArray() : temp.toCharArray();

        // replacing every invalid character with the replacingChar
        for (i = 0; i < c.length; ++i) {
            if (!isValid(c[i])) {
                c[i] = replacingChar;
            }
        }

        // if the first one is a digit
        if (c[0] >= '0' && c[0] <= '9') {
            // if there is already 32 characters
            if (c.length >= this.nameMaxLength) {
                for (i = c.length - 1; i >= 1; --i) {
                    c[i] = c[i - 1];
                }
                c[0] = replacingChar;
            } else {
                char cc[] = new char[c.length + 1];
                cc[0] = replacingChar;
                for (i = 1; i < cc.length; ++i) {
                    cc[i] = c[i - 1];
                }
                c = cc;
            }
        }
        String s = new String(c);
        String s2 = s;
        int mysize = this.nameMaxLength - digitSize;
        // if not unique
        while (uniqueNameTable.contains(s2)) {
            if (s.length() > mysize) {
                s2 = s.substring(0, mysize) + this.getNextSequentialString(maxValue);
            } else {
                s2 = s + this.getNextSequentialString(maxValue);
            }
        }
        uniqueNameTable.add(s2);
        return s2;
    }

    // only alphabets, digits, and _ are valid
    @Override
    protected boolean isValid(char c) {
        return c >= '0' && c <= '9' || c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_';
    }
    
    /**
     * Get next sequential String using 36 radix. This sequential string should be smaller than maxValue
     * 
     * @return String
     */
    @Override
    public String getNextSequentialString(int maxValue) {
        if(this.sequential>=maxValue) {
            System.exit(1);
        }
        String s = "" + Integer.toString(sequential,36);
        int len = s.length();
        if(len<this.digitSize) {
            for (int i = len; i < this.digitSize; ++i) {
                s = "0" + s;
            }
        }
        ++this.sequential;
        return s;
    }
    
    private int computeMaxValue(int base, int digitSize) {
        return (int) Math.pow(base, digitSize);
    }

    public int getNameMaxLength() {
        return nameMaxLength;
    }

    public void setNameMaxLength(int nameMaxLength) {
        this.nameMaxLength = nameMaxLength;
    }
}