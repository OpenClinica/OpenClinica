package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.dao.core.CoreResources;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.security.Key;
import java.util.Base64;

@Converter
public class CryptoConverter implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";
    private static final byte[] KEY = CoreResources.getField("encryptionKey").getBytes();

    @Override
    public String convertToDatabaseColumn(String ccNumber) {

        // do some encryption
        String encryptedData = null;
        if (ccNumber != null) {
            Key key = new SecretKeySpec(KEY, "AES");
            try {
                Cipher c = Cipher.getInstance(ALGORITHM);
                c.init(Cipher.ENCRYPT_MODE, key);
                encryptedData = Base64.getEncoder().encodeToString(c.doFinal(ccNumber.getBytes()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return encryptedData;

    }

    @Override
    public String convertToEntityAttribute(String dbData) {

        String decryptedText = null;
        if (dbData != null) {
            // do some decryption
            Key key = new SecretKeySpec(KEY, "AES");
            try {
                Cipher c = Cipher.getInstance(ALGORITHM);
                c.init(Cipher.DECRYPT_MODE, key);
                decryptedText = new String(c.doFinal(Base64.getDecoder().decode(dbData)));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return decryptedText;
    }
}