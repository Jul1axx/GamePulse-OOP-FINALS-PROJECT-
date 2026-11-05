
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordUtil {

    private static final int ITERATIONS = 120000;
    private static final int KEY_LENGTH = 256;

    public static String generateSalt() {

        byte[] salt = new byte[16];

        new SecureRandom().nextBytes(salt);

        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hashPassword(
            char[] password, String salt) {

        byte[] hash = deriveHash(password, salt);

        return Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verifyPassword(
            char[] password,
            String salt,
            String storedHash) {

        byte[] actualHash = deriveHash(password, salt);

        byte[] expectedHash =
                Base64.getDecoder().decode(storedHash);

        return MessageDigest.isEqual(
                expectedHash, actualHash);
    }

    private static byte[] deriveHash(
            char[] password, String salt) {

        PBEKeySpec spec = new PBEKeySpec(
                password,
                Base64.getDecoder().decode(salt),
                ITERATIONS,
                KEY_LENGTH
        );

        try {

            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance(
                            "PBKDF2WithHmacSHA256");

            return factory.generateSecret(spec).getEncoded();

        } catch (GeneralSecurityException e) {

            throw new IllegalStateException(
                    "Password hashing failed.", e);

        } finally {

            spec.clearPassword();
        }
    }
}
