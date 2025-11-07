//package com.in28minutes.rest.basic.auth;
//
//
//import com.nimbusds.jose.JOSEException;
//import com.nimbusds.jose.jwk.RSAKey;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.oauth2.jwt.JwtDecoder;
//import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
//
//import javax.crypto.spec.SecretKeySpec;
//import java.security.KeyPair;
//import java.security.KeyPairGenerator;
//import java.security.interfaces.RSAPrivateKey;
//import java.security.interfaces.RSAPublicKey;
//import java.util.UUID;
//
//@Configuration
//public class JwtConfig {
//
//    @Bean
//    public RSAKey rsaKey() {
//        KeyPair keyPair = keyPair();
//
//        return new RSAKey
//                .Builder((RSAPublicKey) keyPair.getPublic())
//                .privateKey((RSAPrivateKey) keyPair.getPrivate())
//                .keyID(UUID.randomUUID().toString())
//                .build();
//    }
//
//    @Bean
//    JwtDecoder jwtDecoder(RSAKey rsaKey) throws JOSEException {
//        return NimbusJwtDecoder
//                .withPublicKey(rsaKey.toRSAPublicKey())
//                .build();
//    }
//
//    @Bean
//    public KeyPair keyPair() {
//        try {
//            var keyPairGenerator = KeyPairGenerator.getInstance("RSA");
//            keyPairGenerator.initialize(2048);
//            return keyPairGenerator.generateKeyPair();
//        } catch (Exception e) {
//            throw new IllegalStateException("Unable to generate an RSA Key Pair", e);
//        }
//    }
//}
