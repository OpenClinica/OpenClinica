package org.akaza.openclinica.control.form;

/**
 * Created by IntelliJ IDEA.
 * User: A. Hamid.
 * Date: Mar 13, 2009
 * Time: 7:37:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class EanCheckDigit {

    private static final int[] POSITION_WEIGHT = new int[] {3, 1};
    private int modulus = 10;

    public boolean isValid(String code) {

            if (code.length() != 13) {
                return false;
            }
            if (code.equals("0000000000000")) {
                return true;
            }
            try {
                int modulusResult = calculateModulus(code, true);
                return (modulusResult == 0);
            } catch (Exception ex) {
                return false;
            }
    }

    protected int calculateModulus(String code, boolean includesCheckDigit) throws Exception {
        int total = 0;
        for (int i = 0; i < code.length(); i++) {
            int lth = code.length() + (includesCheckDigit ? 0 : 1);
            int leftPos  = i + 1;
            int rightPos = lth - i;
            int charValue = toInt(code.charAt(i), leftPos, rightPos);
            total += weightedValue(charValue, leftPos, rightPos);
        }
        if (total == 0) {
            throw new Exception("Invalid code, sum is zero");
        }
        return (total % modulus);
    }


    protected int toInt(char character, int leftPos, int rightPos)
            throws Exception {
        if (Character.isDigit(character)) {
            return Character.getNumericValue(character);
        } else {
            throw new Exception("Invalid Character[" +
                    leftPos + "] = '" + character + "'");
        }
    }

    protected int weightedValue(int charValue, int leftPos, int rightPos) {
        int weight = POSITION_WEIGHT[rightPos % 2];
        return (charValue * weight);
    }
}