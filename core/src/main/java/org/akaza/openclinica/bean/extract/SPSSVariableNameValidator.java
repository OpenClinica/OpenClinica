/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2010 Akaza
 * Research
 *
 */

package org.akaza.openclinica.bean.extract;

/**
 * Validate if a SPSS variable name is valid.
 * <p>
 * A valid SPSS variable name should follow the rule that
 * <li> it can be up to eight characters long
 * <li> it must begin with a letter
 * <li> it cannot end with a period
 * <li> its remaining characters can be any letter, any digit, a period or the
 * symbols @, #, _, or $
 *         <li> it should be avoided of ending with an underscore
 *         <li> it can not be any reserved keyword, i.e., ALL, AND, BY, EQ, GE,
 *         GT, LE, LT, NE, NOT, OR, TO, WITH.
 *
 * <br>
 * <p>
 * Rules for creating a valid SPSS variable name:
 * <li> Replace any invalid character with the symbol #
 * <li> If the first character is not a letter, letter V will be used as first
 * letter
 * <li> If the last character is a period or underscore, it will replaced by #
 * <li> If a name is longer than 64 characters, it will be truncated to 64
 * characters. If it results in non-unique name in a data file, sequential
 * numbers are used to replace its letters at the end. By default, the size of
 * sequential numbers is 3.
 * <li> If a reserved keyword has been used as a variable name, squential
 * numbers are apended to its end.
 *
 * @auther ywang
 */
@Deprecated
public class SPSSVariableNameValidator extends NameValidator {
    private final String[] reservedKeywords = { "all", "and", "by", "eq", "ge", "gt", "le", "lt", "ne", "not", "or", "to", "with" };
    private char replacingChar = '#';
    private char replacingFirstChar = 'V';
    private int nameMaxLength = 64;

    /**
     * Given a variable name, this methods returns a valid SPSS variable name
     * and it guarantees the uniqueness of this name
     *
     * @param variableName
     *            String
     * @return String
     */
    @Override
    public String getValidName(String variableName) {
        int maxValue = this.computeMaxValue(variableName.length());
        
        // if variableName is null, automatically generate
        if (variableName == null || variableName.trim().length() == 0) {
            return getNextSequentialString(maxValue);
        }
        int i;

        String temp = variableName.trim();

        // first, check if reserved keywords have been used.
        if (isReservedKeyword(temp)) {
            String s = temp + this.getNextSequentialString(maxValue);
            return s;
        } else { // variable name is not reserved keyword
            // get all chars from the string first and get at most nameMaxLength
            // characters
            char c[] = temp.length() > this.nameMaxLength ? temp.substring(0, this.nameMaxLength).toCharArray() : temp.toCharArray();

            int len = c.length;

            // replacing every invalid character with the replacingChar
            for (i = 0; i < len; ++i) {
                if (!isValid(c[i])) {
                    c[i] = this.replacingChar;
                }
            }

            // if the first one is not a letter
            if (!(c[0] >= 'a' && c[0] <= 'z' || c[0] >= 'A' && c[0] <= 'Z')) {
                // if there is already 8 characters
                if (len >= this.nameMaxLength) {
                    for (i = len - 1; i >= 1; --i) {
                        c[i] = c[i - 1];
                    }
                    c[0] = this.replacingFirstChar;
                } else {
                    char cc[] = new char[len + 1];
                    cc[0] = this.replacingFirstChar;
                    for (i = 1; i < cc.length; ++i) {
                        cc[i] = c[i - 1];
                    }
                    c = cc;
                }
            }

            // if the last one is "." or "_"
            if (c[len - 1] == '.' || c[len - 1] == '_') {
                c[len - 1] = this.replacingChar;
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
    }

    @Override
    protected boolean isValid(char c) {
        return c >= '0' && c <= '9' || c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '.' || c == '@' || c == '#' || c == '_' || c == '$';
    }

    /**
     *
     * @return String
     */
    @Override
    public String getNextSequentialString(int maxValue) {
        if(this.sequential>=maxValue) {
        	 System.exit(1);
        }
        String s = "" + this.sequential;
        int len = s.length();
        if(len<this.digitSize) {
            for (int i = len; i < this.digitSize; ++i) {
                s = "0" + s;
            }
        }
        this.sequential ++;
        return s;
    }
    
    private int computeMaxValue(int nameLength) {
        int len = nameLength > this.nameMaxLength ? this.digitSize : this.nameMaxLength - nameLength;
        len = len < this.digitSize ? this.digitSize : len; 
        int maxValue = len > 9 ? (int) Math.pow(10, 9) : (int) Math.pow(10, len);
        return maxValue;
    }

    private boolean isReservedKeyword(String s) {
        // The maxium length of reserved keywords are 4
        if (s.length() < 5) {
            for (int i = 0; i < this.reservedKeywords.length; ++i) {
                if (s.equalsIgnoreCase(this.reservedKeywords[i])) {
                    return true;
                }
            }
        }
        return false;
    }
    
    

    @Override
    public char getReplacingChar() {
        return replacingChar;
    }

    @Override
    public void setReplacingChar(char replacingChar) {
        this.replacingChar = replacingChar;
    }

    public int getNameMaxLength() {
        return nameMaxLength;
    }

    public void setNameMaxLength(int nameMaxLength) {
        this.nameMaxLength = nameMaxLength;
    }

    public String[] getReservedKeywords() {
        return reservedKeywords;
    }

    public void setReplacingFirstChar(char fc) {
        this.replacingFirstChar = fc;
    }

    public char getReplacingFirstChar() {
        return this.replacingFirstChar;
    }
}