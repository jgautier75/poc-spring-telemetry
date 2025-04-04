package com.acme.jga.rest.config;

import com.acme.jga.crypto.CryptoEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.vault.annotation.VaultPropertySource;

import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

@Configuration
@VaultPropertySource("dev-secrets/creds")
@Slf4j
public class VaultSecrets {
    @Autowired
    Environment env;

    @Bean
    public CryptoEngine cryptoEngine() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        CryptoEngine cryptoEngine = new CryptoEngine();
        cryptoEngine.initCrypto(Objects.requireNonNull(env.getProperty("cipherKey")));
        return cryptoEngine;
    }
}
