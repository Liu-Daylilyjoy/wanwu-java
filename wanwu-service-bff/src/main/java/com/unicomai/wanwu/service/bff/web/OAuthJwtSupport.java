package com.unicomai.wanwu.service.bff.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

final class OAuthJwtSupport {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {
    };

    private final String issuer;
    private final byte[] hmacSecret;
    private final KeyPair rsaKeyPair;
    private final String kid;
    private final Map<String, Object> jwk;

    OAuthJwtSupport(String issuer, String hmacSecret) {
        this.issuer = issuer;
        this.hmacSecret = hmacSecret.getBytes(StandardCharsets.UTF_8);
        this.rsaKeyPair = generateRsaKeyPair();
        this.kid = UUID.randomUUID().toString();
        this.jwk = jwk((RSAPublicKey) rsaKeyPair.getPublic(), kid);
    }

    Map<String, Object> jwk() {
        return new LinkedHashMap<>(jwk);
    }

    String accessToken(String userId, String clientId, List<String> scopes, long expiresAtEpochSeconds) {
        Map<String, Object> claims = baseClaims("access", expiresAtEpochSeconds);
        claims.put("userId", userId);
        claims.put("clientId", clientId);
        claims.put("scope", scopes == null ? Collections.emptyList() : new ArrayList<>(scopes));
        return sign(hs256Header(), claims, new JwtSigner() {
            @Override
            public byte[] sign(byte[] data) throws Exception {
                Mac mac = Mac.getInstance("HmacSHA256");
                mac.init(new SecretKeySpec(hmacSecret, "HmacSHA256"));
                return mac.doFinal(data);
            }
        });
    }

    String idToken(String userId, String userName, String clientId, long expiresAtEpochSeconds) {
        Map<String, Object> claims = baseClaims(userId, expiresAtEpochSeconds);
        claims.put("aud", clientId);
        claims.put("userId", userId);
        claims.put("userName", userName);
        return sign(rs256Header(), claims, new JwtSigner() {
            @Override
            public byte[] sign(byte[] data) throws Exception {
                Signature signature = Signature.getInstance("SHA256withRSA");
                PrivateKey privateKey = rsaKeyPair.getPrivate();
                signature.initSign(privateKey);
                signature.update(data);
                return signature.sign();
            }
        });
    }

    AccessTokenClaims parseAccessToken(String token) {
        String[] parts = token == null ? new String[0] : token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("invalid access token");
        }
        Map<String, Object> header = decodeMap(parts[0]);
        Map<String, Object> claims = decodeMap(parts[1]);
        if (!"HS256".equals(text(header, "alg"))) {
            throw new IllegalArgumentException("invalid access token algorithm");
        }
        byte[] signed = (parts[0] + "." + parts[1]).getBytes(StandardCharsets.UTF_8);
        byte[] expected;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(hmacSecret, "HmacSHA256"));
            expected = mac.doFinal(signed);
        } catch (Exception ex) {
            throw new IllegalStateException("OAuth access token verification failed", ex);
        }
        if (!MessageDigest.isEqual(expected, base64UrlDecode(parts[2]))) {
            throw new IllegalArgumentException("invalid access token signature");
        }
        long now = Instant.now().getEpochSecond();
        if (longClaim(claims, "nbf") > now || longClaim(claims, "exp") < now) {
            throw new IllegalArgumentException("access token expired");
        }
        if (!"access".equals(text(claims, "sub"))) {
            throw new IllegalArgumentException("invalid access token subject");
        }
        return new AccessTokenClaims(text(claims, "userId"), text(claims, "clientId"), stringList(claims.get("scope")));
    }

    private Map<String, Object> baseClaims(String subject, long expiresAtEpochSeconds) {
        long now = Instant.now().getEpochSecond();
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("iss", issuer);
        claims.put("sub", subject);
        claims.put("nbf", now);
        claims.put("iat", now);
        claims.put("exp", expiresAtEpochSeconds);
        return claims;
    }

    private Map<String, Object> hs256Header() {
        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");
        return header;
    }

    private Map<String, Object> rs256Header() {
        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "RS256");
        header.put("typ", "JWT");
        header.put("kid", kid);
        return header;
    }

    private String sign(Map<String, Object> header, Map<String, Object> claims, JwtSigner signer) {
        try {
            String encodedHeader = base64Url(JSON.writeValueAsBytes(header));
            String encodedClaims = base64Url(JSON.writeValueAsBytes(claims));
            byte[] signed = (encodedHeader + "." + encodedClaims).getBytes(StandardCharsets.UTF_8);
            return encodedHeader + "." + encodedClaims + "." + base64Url(signer.sign(signed));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("OAuth JWT claims are invalid", ex);
        } catch (Exception ex) {
            throw new IllegalStateException("OAuth JWT signing failed", ex);
        }
    }

    private Map<String, Object> decodeMap(String value) {
        try {
            return JSON.readValue(base64UrlDecode(value), MAP_TYPE);
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid access token payload", ex);
        }
    }

    private Map<String, Object> jwk(RSAPublicKey publicKey, String kid) {
        Map<String, Object> key = new LinkedHashMap<>();
        key.put("kty", "RSA");
        key.put("use", "sig");
        key.put("kid", kid);
        key.put("alg", "RS256");
        key.put("n", base64Url(unsigned(publicKey.getModulus())));
        key.put("e", base64Url(unsigned(publicKey.getPublicExponent())));
        return key;
    }

    private KeyPair generateRsaKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException("OAuth RSA key generation failed", ex);
        }
    }

    private byte[] unsigned(BigInteger value) {
        byte[] bytes = value.toByteArray();
        if (bytes.length > 1 && bytes[0] == 0) {
            byte[] stripped = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, stripped, 0, stripped.length);
            return stripped;
        }
        return bytes;
    }

    private String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private byte[] base64UrlDecode(String value) {
        return Base64.getUrlDecoder().decode(value);
    }

    private long longClaim(Map<String, Object> claims, String key) {
        Object value = claims.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value != null) {
            return Long.parseLong(String.valueOf(value));
        }
        return 0L;
    }

    private String text(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private List<String> stringList(Object value) {
        if (!(value instanceof List)) {
            return Collections.emptyList();
        }
        List<?> source = (List<?>) value;
        List<String> result = new ArrayList<>();
        for (Object item : source) {
            if (item != null) {
                result.add(String.valueOf(item));
            }
        }
        return result;
    }

    static final class AccessTokenClaims {
        private final String userId;
        private final String clientId;
        private final List<String> scopes;

        private AccessTokenClaims(String userId, String clientId, List<String> scopes) {
            this.userId = userId;
            this.clientId = clientId;
            this.scopes = scopes == null ? Collections.<String>emptyList() : new ArrayList<>(scopes);
        }

        String userId() {
            return userId;
        }

        String clientId() {
            return clientId;
        }

        List<String> scopes() {
            return new ArrayList<>(scopes);
        }
    }

    private interface JwtSigner {
        byte[] sign(byte[] data) throws Exception;
    }
}
