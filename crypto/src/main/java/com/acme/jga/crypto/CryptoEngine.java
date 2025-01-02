package com.acme.jga.crypto;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class CryptoEngine {
    private static final String ALGORITHM = "AES";
    private Cipher encodingCipher;
    private Cipher decodingCipher;

    public CryptoEngine() {
        // Default constructor
    }

    public void initCrypto(String secretKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
        this.encodingCipher = Cipher.getInstance(ALGORITHM);
        this.encodingCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        this.decodingCipher = Cipher.getInstance(ALGORITHM);
        this.decodingCipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
    }

    public String encode(String toEncode) {
        try {
            byte[] encrypted = encodingCipher.doFinal(toEncode.getBytes());
            return new String(Base64.getEncoder().encode(encrypted));
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    public String decode(String toDecode) {
        try {
            byte[] decoded = this.decodingCipher.doFinal(Base64.getDecoder().decode(toDecode));
            return new String(decoded);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

}
