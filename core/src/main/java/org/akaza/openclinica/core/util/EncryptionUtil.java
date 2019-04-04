package org.akaza.openclinica.core.util;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;

/**
 * Utility class to help with encryption/decryption.
 * @author svadla@openclinica.com
 */
public class EncryptionUtil {
    private static final String ENCRYPTION_ALGORITHM = "AES/ECB/NoPadding";
    private static final String KEY_ALGORITHM = "AES";
    private static final int ENCRYPTION_BLOCK_SIZE = 16;
    private static final char PADDING_CHAR = ' ';

    /**
     * Encrypts the plain text using the given encryption key using AES/ECB/NoPadding algorithm and hex encodes the encrypted text.
     * @param plainText plain text to encrypt
     * @param encryptionKey encryption key
     * @return encrypted text
     */
    public static String encryptValue(String plainText, String encryptionKey) {
        if (plainText == null) {
            return null;
        }
        Key key = new SecretKeySpec(encryptionKey.getBytes(), KEY_ALGORITHM);
        try {

            Cipher c = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            c.init(Cipher.ENCRYPT_MODE, key);
            int plainTextLength = plainText.length();
            // AES/ECB algorithm requires the encryption block size to be multiple of 16 bytes, so pad the
            // plain text to ensure that the length is multiple of 16.
            int paddingSize = ENCRYPTION_BLOCK_SIZE - plainTextLength % ENCRYPTION_BLOCK_SIZE;
            String paddingString = StringUtils.repeat(PADDING_CHAR, paddingSize);
            String paddedPlainText = plainText.concat(paddingString);
            byte[] encryptedValue = c.doFinal(paddedPlainText.getBytes());

            String encryptedTextWithEncoding = DatatypeConverter.printHexBinary(encryptedValue);
            return encryptedTextWithEncoding;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Hex decodes the encrypted text and decrypts the encrypted text using the given encryption key using AES/ECB/NoPadding algorithm.
     * @param encryptedText encrypted text to decrypt
     * @param encryptionKey encryption key
     * @return decrypted text
     */
    public static String decryptValue(String encryptedText, String encryptionKey) {
        String decryptedText = null;
        if (encryptedText != null) {
            Key key = new SecretKeySpec(encryptionKey.getBytes(), KEY_ALGORITHM);
            try {

                Cipher c = Cipher.getInstance(ENCRYPTION_ALGORITHM);
                c.init(Cipher.DECRYPT_MODE, key);
                byte[] decodedValue = DatatypeConverter.parseHexBinary(encryptedText);
                byte[] decryptedValue = c.doFinal(decodedValue);

                decryptedText = new String(decryptedValue).trim();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return decryptedText;
    }
}
