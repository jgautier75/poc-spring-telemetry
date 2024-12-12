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
    private static final String SECRET_KEY = "1c9e1cfbe63844b1a0772aea4cba5gg6";
    private Cipher encodingCipher;
    private Cipher decodingCipher;

    public CryptoEngine() {
        try {
            initCrypto();
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public void initCrypto() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
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
