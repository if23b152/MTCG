package Server;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Password hashing with PBKDF2 (part of the JDK, no extra dependency).
 * Format: pbkdf2$<iterations>$<salt base64>$<hash base64>
 */
public final class PasswordHasher {

    private static final String PREFIX = "pbkdf2$";
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 210_000;
    private static final int SALT_BYTES = 16;
    private static final int HASH_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() { }

    public static String hash(String password) {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(password.toCharArray(), salt, ITERATIONS);
        Base64.Encoder enc = Base64.getEncoder();
        return PREFIX + ITERATIONS + "$" + enc.encodeToString(salt) + "$" + enc.encodeToString(hash);
    }

    public static boolean verify(String password, String stored) {
        if (password == null || stored == null) {
            return false;
        }
        if (!isHashed(stored)) {
            // Legacy plaintext entry (created before hashing was introduced)
            return MessageDigest.isEqual(password.getBytes(StandardCharsets.UTF_8),
                    stored.getBytes(StandardCharsets.UTF_8));
        }
        String[] parts = stored.split("\\$");
        if (parts.length != 4) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = pbkdf2(password.toCharArray(), salt, iterations);
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean isHashed(String stored) {
        return stored != null && stored.startsWith(PREFIX);
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, HASH_BITS);
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("Password hashing failed", e);
        }
    }
}
