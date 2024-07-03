package com.mgnt.common.util;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Slf4j
public class EncryptionUtil {

    private static final String ALGORITHM = "AES";
    private static final String SECRET_KEY = "1234567891234567"; // 16바이트 키

    public static String encrypt(String data) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encrypted = cipher.doFinal(data.getBytes());
        String encryptedString = Base64.getUrlEncoder().encodeToString(encrypted); // URL-safe Base64 인코딩 사용
        log.info("Encrypted data: {}", encryptedString); // 로그 추가
        return encryptedString;
    }

    public static String decrypt(String encryptedData) throws Exception {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decodedBytes = Base64.getUrlDecoder().decode(encryptedData); // URL-safe Base64 디코딩 사용
            byte[] decrypted = cipher.doFinal(decodedBytes);
            String result = new String(decrypted);
            log.info("Decrypted data: {}", result); // 로그 추가
            return result;
        } catch (IllegalArgumentException e) {
            log.error("Failed to decode Base64 data: {}", encryptedData, e);
            throw new IllegalArgumentException("Invalid Base64 input");
        }
    }
}
