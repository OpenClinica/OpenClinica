/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.logic.expressionTree;

import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.IllegalFormatException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Krikor Krumlian
 * 
 */
public class TextIO {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    /**
     * The value returned by the peek() method when the input is at end-of-file. (The value of this constant is (char)0xFFFF.)
     */
    public final char EOF = (char) 0xFFFF;

    /**
     * The value returned by the peek() method when the input is at end-of-line. The value of this constant is the character '\n'.
     */
    public final char EOLN = '\n'; // The value returned by peek() when

    // Matcher
    private Matcher integerMatcher; // Used for reading integer numbers;
    private Matcher floatMatcher; // Used for reading floating point
    private Matcher dateMatcher; // Used for reading floating point
    // Pattern
    private final Pattern integerRegex = Pattern.compile("(\\+|-)?[0-9]+");
    private final Pattern floatRegex = Pattern.compile("(\\+|-)?(([0-9]+(\\.[0-9]*)?)|(\\.[0-9]+))((e|E)(\\+|-)?[0-9]+)?");
    private final Pattern dateRegex = Pattern.compile(ExpressionTreeHelper.yyyyMMddDashes);

    private String buffer = null; // One line read from input.
    private int pos = 0; // Position of next char in input line that has

    /**
     * Write a single value to the current output destination, using the default format and no extra spaces. This method will handle any type of parameter, even
     * one whose type is one of the primitive types.
     */
    public void put(Object x) {
        logger.info("X : " + x);
    }

    /**
     * Write a single value to the current output destination, using the default format and outputting at least minChars characters (with extra spaces added
     * before the output value if necessary). This method will handle any type of parameter, even one whose type is one of the primitive types.
     * 
     * @param x
     *            The value to be output, which can be of any type.
     * @param minChars
     *            The minimum number of characters to use for the output. If x requires fewer then this number of characters, then extra spaces are added to the
     *            front of x to bring the total up to minChars. If minChars is less than or equal to zero, then x will be printed in the minumum number of
     *            spaces possible.
     */
    public void put(Object x, int minChars) {
        if (minChars <= 0){
            if ( x!=null){
            	logger.debug(x.toString()) ;
            }
        }
        else
        	logger.debug("%" + minChars + "s", x);
    }

    /**
     * This is equivalent to put(x), followed by an end-of-line.
     */
    public void putln(Object x) {
        logger.info("X : " + x);
    }

    /**
     * This is equivalent to put(x,minChars), followed by an end-of-line.
     */
    public void putln(Object x, int minChars) {
        put(x, minChars);
    }

    /**
     * Write an end-of-line character to the current output destination.
     */
    public void putln() {
        logger.info("EOL");
    }

    /**
     * Writes formatted output values to the current output destination. This method has the same function as System.out.printf(); the details of formatted
     * output are not discussed here. The first parameter is a string that describes the format of the output. There can be any number of additional parameters;
     * these specify the values to be output and can be of any type. This method will throw an IllegalArgumentException if the format string is null or if the
     * format string is illegal for the values that are being output.
     */
    public void putf(String format, Object... items) {
        if (format == null)
            throw new IllegalArgumentException("Null format string in TextIO.putf() method.");
        try {
        	logger.debug(format, items);
        } catch (IllegalFormatException e) {
            throw new IllegalArgumentException("Illegal format string in TextIO.putf() method.");
        }
    }

    // *************************** Input Methods
    // *********************************

    /**
     * Test whether the next character in the current input source is an end-of-line. Note that this method does NOT skip whitespace before testing for
     * end-of-line -- if you want to do that, call skipBlanks() first.
     */
    public boolean eoln() {
        return peek() == '\n';
    }

    /**
     * Test whether the next character in the current input source is an end-of-file. Note that this method does NOT skip whitespace before testing for
     * end-of-line -- if you want to do that, call skipBlanks() or skipWhitespace() first.
     */
    public boolean eof() {
        return peek() == EOF;
    }

    /**
     * Reads the next character from the current input source. The character can be a whitespace character; compare this to the getChar() method, which skips
     * over whitespace and returns the next non-whitespace character. An end-of-line is always returned as the character '\n', even when the actual end-of-line
     * in the input source is something else, such as '\r' or "\r\n". This method will throw an IllegalArgumentException if the input is at end-of-file (which
     * will not ordinarily happen if reading from standard input).
     */
    public char getAnyChar() {
        return readChar();
    }

    public String getAnyString(int x) {
        return readString(x);
    }

    /**
     * Returns the next character in the current input source, without actually removing that character from the input. The character can be a whitespace
     * character and can be the end-of-file character (specfied by the constant TextIO.EOF).An end-of-line is always returned as the character '\n', even when
     * the actual end-of-line in the input source is something else, such as '\r' or "\r\n". This method never causes an error.
     */
    public char peek() {
        return lookChar();
    }

    public String peek(int x) {
        return lookString(x);
    }

    /**
     * Skips over any whitespace characters, except for end-of-lines. After this method is called, the next input character is either an end-of-line, an
     * end-of-file, or a non-whitespace character. This method never causes an error. (Ordinarly, end-of-file is not possible when reading from standard input.)
     */
    public void skipBlanks() {
        char ch = lookChar();
        while (ch != EOF && ch != '\n' && Character.isWhitespace(ch)) {
            readChar();
            ch = lookChar();
        }
    }

    /**
     * Skips over any whitespace characters, including for end-of-lines. After this method is called, the next input character is either an end-of-file or a
     * non-whitespace character. This method never causes an error. (Ordinarly, end-of-file is not possible when reading from standard input.)
     */
    private void skipWhitespace() {
        char ch = lookChar();
        while (ch != EOF && Character.isWhitespace(ch)) {
            readChar();
            ch = lookChar();
        }
    }

    /**
     * Skips whitespace characters and then reads a value of type byte from input, discarding the rest of the current line of input (including the next
     * end-of-line character, if any). When using standard IO, this will not produce an error; the user will be prompted repeatedly for input until a legal
     * value is input. In other cases, an IllegalArgumentException will be thrown if a legal value is not found.
     */
    public byte getlnByte() {
        byte x = getByte();
        emptyBuffer();
        return x;
    }

    /**
     * Skips whitespace characters and then reads a value of type short from input, discarding the rest of the current line of input (including the next
     * end-of-line character, if any). When using standard IO, this will not produce an error; the user will be prompted repeatedly for input until a legal
     * value is input. In other cases, an IllegalArgumentException will be thrown if a legal value is not found.
     */
    public short getlnShort() {
        short x = getShort();
        emptyBuffer();
        return x;
    }

    /**
     * Skips whitespace characters and then reads a value of type int from input, discarding the rest of the current line of input (including the next
     * end-of-line character, if any). When using standard IO, this will not produce an error; the user will be prompted repeatedly for input until a legal
     * value is input. In other cases, an IllegalArgumentException will be thrown if a legal value is not found.
     */
    public int getlnInt() {
        int x = getInt();
        emptyBuffer();
        return x;
    }

    /**
     * Skips whitespace characters and then reads a value of type long from input, discarding the rest of the current line of input (including the next
     * end-of-line character, if any). When using standard IO, this will not produce an error; the user will be prompted repeatedly for input until a legal
     * value is input. In other cases, an IllegalArgumentException will be thrown if a legal value is not found.
     */
    public long getlnLong() {
        long x = getLong();
        emptyBuffer();
        return x;
    }

    /**
     * Skips whitespace characters and then reads a value of type float from input, discarding the rest of the current line of input (including the next
     * end-of-line character, if any). When using standard IO, this will not produce an error; the user will be prompted repeatedly for input until a legal
     * value is input. In other cases, an IllegalArgumentException will be thrown if a legal value is not found.
     */
    public float getlnFloat() {
        float x = getFloat();
        emptyBuffer();
        return x;
    }

    /**
     * Skips whitespace characters and then reads a value of type double from input, discarding the rest of the current line of input (including the next
     * end-of-line character, if any). When using standard IO, this will not produce an error; the user will be prompted repeatedly for input until a legal
     * value is input. In other cases, an IllegalArgumentException will be thrown if a legal value is not found.
     */
    public double getlnDouble() {
        double x = getDouble();
        emptyBuffer();
        return x;
    }

    /**
     * Skips whitespace characters and then reads a value of type char from input, discarding the rest of the current line of input (including the next
     * end-of-line character, if any). Note that the value that is returned will be a non-whitespace character; compare this with the getAnyChar() method. When
     * using standard IO, this will not produce an error. In other cases, an error can occur if an end-of-file is encountered.
     */
    public char getlnChar() {
        char x = getChar();
        emptyBuffer();
        return x;
    }

    /**
     * Skips whitespace characters and then reads a value of type boolean from input, discarding the rest of the current line of input (including the next
     * end-of-line character, if any). When using standard IO, this will not produce an error; the user will be prompted repeatedly for input until a legal
     * value is input. In other cases, an IllegalArgumentException will be thrown if a legal value is not found.
     * <p>
     * Legal inputs for a boolean input are: true, t, yes, y, 1, false, f, no, n, and 0; letters can be either upper case or lower case. One "word" of input is
     * read, using the getWord() method, and it must be one of these; note that the "word" must be terminated by a whitespace character (or end-of-file).
     */
    public boolean getlnBoolean() {
        boolean x = getBoolean();
        emptyBuffer();
        return x;
    }

    /**
     * Skips whitespace characters and then reads one "word" from input, discarding the rest of the current line of input (including the next end-of-line
     * character, if any). A word is defined as a sequence of non-whitespace characters (not just letters!). When using standard IO, this will not produce an
     * error. In other cases, an IllegalArgumentException will be thrown if an end-of-file is encountered.
     */
    public String getlnWord() {
        String x = getWord();
        emptyBuffer();
        return x;
    }

    /**
     * This is identical to getln().
     */
    public String getlnString() {
        return getln();
    }

    /**
     * Reads all the charcters from the current input source, up to the next end-of-line. The end-of-line is read but is not included in the return value. Any
     * other whitespace characters on the line are retained, even if they occur at the start of input. The return value will be an empty string if there are no
     * no characters before the end-of-line. When using standard IO, this will not produce an error. In other cases, an IllegalArgumentException will be thrown
     * if an end-of-file is encountered.
     */
    public String getln() {
        StringBuffer s = new StringBuffer(100);
        char ch = readChar();
        while (ch != '\n') {
            s.append(ch);
            ch = readChar();
        }
        return s.toString();
    }

    /**
     * Skips whitespace characters and then reads a value of type byte from input. Any additional characters on the current line of input are retained, and will
     * be read by the next input operation. When using standard IO, this will not produce an error; the user will be prompted repeatedly for input until a legal
     * value is input. In other cases, an IllegalArgumentException will be thrown if a legal value is not found.
     */
    public byte getByte() {
        return (byte) readInteger(-128L, 127L);
    }

    /**
     * Skips whitespace characters and then reads a value of type short from input. Any additional characters on the current line of input are retained, and
     * will be read by the next input operation. When using standard IO, this will not produce an error; the user will be prompted repeatedly for input until a
     * legal value is input. In other cases, an IllegalArgumentException will be thrown if a legal value is not found.
     */
    public short getShort() {
        return (short) readInteger(-32768L, 32767L);
    }

    /**
     * Skips whitespace characters and then reads a value of type int from input. Any additional characters on the current line of input are retained, and will
     * be read by the next input operation. When using standard IO, this will not produce an error; the user will be prompted repeatedly for input until a legal
     * value is input. In other cases, an IllegalArgumentException will be thrown if a legal value is not found.
     */
    public int getInt() {
        return (int) readInteger(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    /**
     * Skips whitespace characters and then reads a value of type long from input. Any additional characters on the current line of input are retained, and will
     * be read by the next input operation. When using standard IO, this will not produce an error; the user will be prompted repeatedly for input until a legal
     * value is input. In other cases, an IllegalArgumentException will be thrown if a legal value is not found.
     */
    public long getLong() {
        return readInteger(Long.MIN_VALUE, Long.MAX_VALUE);
    }

    /**
     * Skips whitespace characters and then reads a single non-whitespace character from input. Any additional characters on the current line of input are
     * retained, and will be read by the next input operation. When using standard IO, this will not produce an error. In other cases, an
     * IllegalArgumentException will be thrown if an end-of-file is encountered.
     */
    public char getChar() {
        skipWhitespace();
        return readChar();
    }

    /**
     * Skips whitespace characters and then reads a value of type float from input. Any additional characters on the current line of input are retained, and
     * will be read by the next input operation. When using standard IO, this will not produce an error; the user will be prompted repeatedly for input until a
     * legal value is input. In other cases, an IllegalArgumentException will be thrown if a legal value is not found.
     */
    public float getFloat() {
        float x = 0.0F;
        while (true) {
            String str = readRealString();
            if (str == null) {
                throw new OpenClinicaSystemException("OCRERR_0014");
                // errorMessage("Floating point number not found.", "Real number
                // in the range " + -Float.MAX_VALUE + " to " +
                // Float.MAX_VALUE);
            } else {
                try {
                    x = Float.parseFloat(str);
                } catch (NumberFormatException e) {
                    errorMessage("Illegal floating point input, " + str + ".", "Real number in the range " + -Float.MAX_VALUE + " to " + Float.MAX_VALUE);
                    continue;
                }
                if (Float.isInfinite(x)) {
                    errorMessage("Floating point input outside of legal range, " + str + ".", "Real number in the range " + -Float.MAX_VALUE + " to "
                        + Float.MAX_VALUE);
                    continue;
                }
                break;
            }
        }
        return x;
    }

    /**
     * Skips whitespace characters and then reads a value of type double from input. Any additional characters on the current line of input are retained, and
     * will be read by the next input operation. When using standard IO, this will not produce an error; the user will be prompted repeatedly for input until a
     * legal value is input. In other cases, an IllegalArgumentException will be thrown if a legal value is not found.
     */
    public String getDate() {
        String str = readDateString();
        return str;

    }

    /**
     * Skips whitespace characters and then reads a value of type double from input. Any additional characters on the current line of input are retained, and
     * will be read by the next input operation. When using standard IO, this will not produce an error; the user will be prompted repeatedly for input until a
     * legal value is input. In other cases, an IllegalArgumentException will be thrown if a legal value is not found.
     */
    public double getDouble() {
        double x = 0.0;
        while (true) {
            String str = readRealString();
            if (str == null) {
                errorMessage("Floating point number not found.", "Real number in the range " + -Double.MAX_VALUE + " to " + Double.MAX_VALUE);
            } else {
                try {
                    x = Double.parseDouble(str);
                } catch (NumberFormatException e) {
                    errorMessage("Illegal floating point input, " + str + ".", "Real number in the range " + -Double.MAX_VALUE + " to " + Double.MAX_VALUE);
                    continue;
                }
                if (Double.isInfinite(x)) {
                    errorMessage("Floating point input outside of legal range, " + str + ".", "Real number in the range " + -Double.MAX_VALUE + " to "
                        + Double.MAX_VALUE);
                    continue;
                }
                break;
            }
        }
        return x;
    }

    /**
     * Skips whitespace characters and then reads one "word" from input. Any additional characters on the current line of input are retained, and will be read
     * by the next input operation. A word is defined as a sequence of non-whitespace characters (not just letters!). When using standard IO, this will not
     * produce an error. In other cases, an IllegalArgumentException will be thrown if an end-of-file is encountered.
     */
    public String getDoubleQuoteWord() {
        skipWhitespace();
        StringBuffer str = new StringBuffer(50);
        char ch = lookChar();
        int quoteCount = 0;
        while (ch != EOF && quoteCount < 2) {
            if (Character.toString(ch).equals("\"")) {
                quoteCount++;
            }
            str.append(readChar());
            ch = lookChar();
        }
        return str.toString().replaceAll("\"", "");
    }

    /**
     * Skips whitespace characters and then reads one "word" from input. Any additional characters on the current line of input are retained, and will be read
     * by the next input operation. A word is defined as a sequence of non-whitespace characters (not just letters!). When using standard IO, this will not
     * produce an error. In other cases, an IllegalArgumentException will be thrown if an end-of-file is encountered.
     */
    public String getWord() {
        skipWhitespace();
        StringBuffer str = new StringBuffer(50);
        char ch = lookChar();
        //while (ch == EOF || !Character.isWhitespace(ch)) {
        while (ch == EOF || !Character.isWhitespace(ch) && ch != ')') {
            str.append(readChar());
            ch = lookChar();
        }
        return str.toString();
    }

    /**
     * Skips whitespace characters and then reads a value of type boolean from input. Any additional characters on the current line of input are retained, and
     * will be read by the next input operation. When using standard IO, this will not produce an error; the user will be prompted repeatedly for input until a
     * legal value is input. In other cases, an IllegalArgumentException will be thrown if a legal value is not found.
     * <p>
     * Legal inputs for a boolean input are: true, t, yes, y, 1, false, f, no, n, and 0; letters can be either upper case or lower case. One "word" of input is
     * read, using the getWord() method, and it must be one of these; note that the "word" must be terminated by a whitespace character (or end-of-file).
     */
    public boolean getBoolean() {
        boolean ans = false;
        while (true) {
            String s = getWord();
            if (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("t") || s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("y") || s.equals("1")) {
                ans = true;
                break;
            } else if (s.equalsIgnoreCase("false") || s.equalsIgnoreCase("f") || s.equalsIgnoreCase("no") || s.equalsIgnoreCase("n") || s.equals("0")) {
                ans = false;
                break;
            } else
                errorMessage("Illegal boolean input value.", "one of:  true, false, t, f, yes, no, y, n, 0, or 1");
        }

        return ans;
    }

    private String readDateString() { // read chars from input
        // following syntax of real
        // numbers
        skipWhitespace();
        if (lookChar() == EOF)
            return null;
        if (dateMatcher == null)
            dateMatcher = dateRegex.matcher(buffer);
        dateMatcher.region(pos, buffer.length());
        if (dateMatcher.lookingAt()) {
            String str = dateMatcher.group();
            pos = dateMatcher.end();
            return str;
        } else
            return null;
    }

    private String readRealString() { // read chars from input
        // following syntax of real
        // numbers
        skipWhitespace();
        if (lookChar() == EOF)
            return null;
        if (floatMatcher == null)
            floatMatcher = floatRegex.matcher(buffer);
        floatMatcher.region(pos, buffer.length());
        if (floatMatcher.lookingAt()) {
            String str = floatMatcher.group();
            pos = floatMatcher.end();
            return str;
        } else
            return null;
    }

    private String readIntegerString() { // read chars from input
        // following syntax of integers
        skipWhitespace();
        if (lookChar() == EOF)
            return null;
        if (integerMatcher == null)
            integerMatcher = integerRegex.matcher(buffer);
        integerMatcher.region(pos, buffer.length());
        if (integerMatcher.lookingAt()) {
            String str = integerMatcher.group();
            pos = integerMatcher.end();
            return str;
        } else
            return null;
    }

    private long readInteger(long min, long max) { // read long
        // integer, limited
        // to specified
        // range
        long x = 0;
        while (true) {
            String s = readIntegerString();
            if (s == null) {
                errorMessage("Integer value not found in input.", "Integer in the range " + min + " to " + max);
            } else {
                String str = s.toString();
                try {
                    x = Long.parseLong(str);
                } catch (NumberFormatException e) {
                    errorMessage("Illegal integer input, " + str + ".", "Integer in the range " + min + " to " + max);
                    continue;
                }
                if (x < min || x > max) {
                    errorMessage("Integer input outside of legal range, " + str + ".", "Integer in the range " + min + " to " + max);
                    continue;
                }
                break;
            }
        }

        return x;
    }

    private void errorMessage(String message, String expecting) { // Report
        logger.info("ERROR");
    }

    private char lookChar() { // return next character from input
        if (buffer == null || pos > buffer.length())
            return EOF;
        else if (pos == buffer.length())
            return '\n';
        else
            return buffer.charAt(pos);
    }

    private String lookString(int x) { // return next String from
        // input
        if (buffer == null || pos > buffer.length() || pos + x > buffer.length())
            return null;
        else if (pos == buffer.length())
            return "\n";
        else
            return buffer.substring(pos, pos + x);
    }

    private char readChar() { // return and discard next character
        // from input
        char ch = lookChar();
        if (buffer == null) {
            throw new IllegalArgumentException("Attempt to read past end-of-string");
        }
        pos++;
        return ch;
    }

    private String readString(int x) { // return and discard next
        // character
        // from input
        String str = lookString(x);
        if (buffer == null) {
            throw new IllegalArgumentException("Attempt to read past end-of-string");
        }
        pos += x;
        return str;
    }

    /*
     * private void fillBuffer() { // Wait for user to type a line and // press return, try { buffer = in.readLine(); } catch (Exception e) { if
     * (readingStandardInput) throw new IllegalArgumentException("Error while reading standard input???"); else if (inputFileName != null) throw new
     * IllegalArgumentException("Error while attempting to read from file \"" + inputFileName + "\"."); else throw new IllegalArgumentException("Errow while
     * attempting to read form an input stream."); } pos = 0; floatMatcher = null; integerMatcher = null; }
     */

    public void fillBuffer(String expression) { // Wait for user to type a line
        // and
        // press return,
        try {
            buffer = expression;
        } catch (Exception e) {
            throw new IllegalArgumentException("Errow while attempting to read form an input stream.");
        }
        pos = 0;
        floatMatcher = null;
        integerMatcher = null;
    }

    private void emptyBuffer() { // discard the rest of the current line
        // of input
        buffer = null;
    }

} // end of class TextIO
