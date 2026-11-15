package com.products.infrastructure.auth;

import org.eclipse.microprofile.config.spi.ConfigSource;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Map;
import java.util.Set;

/**
 * Generates a fresh RSA-2048 keypair once per JVM and exposes it as inline PEM values for
 * {@code smallrye.jwt.sign.key} / {@code mp.jwt.verify.publickey}. This is only consulted when no
 * higher-ordinal source (application.properties, env vars, a mounted K8s Secret) already provides
 * those properties, so a real deployment can override it by setting SMALLRYE_JWT_SIGN_KEY /
 * MP_JWT_VERIFY_PUBLICKEY (or the *_LOCATION variants) — see chart/templates/jwt-keys-job.yaml for
 * how a real deploy provisions a Secret shared across all replicas instead of relying on this.
 *
 * <p>Not committing a static demo keypair to source control avoids it being flagged as a leaked
 * secret by static analysis (it previously was, even though it was demo-only).
 */
public class EphemeralJwtKeyConfigSource implements ConfigSource {

    private static final String SIGN_KEY_PROPERTY = "smallrye.jwt.sign.key";
    private static final String VERIFY_KEY_PROPERTY = "mp.jwt.verify.publickey";

    private static final KeyPair KEY_PAIR = generateKeyPair();

    private static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("RSA algorithm unavailable, cannot generate ephemeral JWT keypair", e);
        }
    }

    private static String toPem(String type, byte[] encoded) {
        String base64 = Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(encoded);
        return "-----BEGIN " + type + "-----\n" + base64 + "\n-----END " + type + "-----\n";
    }

    @Override
    public Map<String, String> getProperties() {
        return Map.of(
                SIGN_KEY_PROPERTY, toPem("PRIVATE KEY", ((RSAPrivateKey) KEY_PAIR.getPrivate()).getEncoded()),
                VERIFY_KEY_PROPERTY, toPem("PUBLIC KEY", ((RSAPublicKey) KEY_PAIR.getPublic()).getEncoded()));
    }

    @Override
    public Set<String> getPropertyNames() {
        return Set.of(SIGN_KEY_PROPERTY, VERIFY_KEY_PROPERTY);
    }

    @Override
    public String getValue(String propertyName) {
        return getProperties().get(propertyName);
    }

    @Override
    public String getName() {
        return "ephemeral-jwt-key";
    }

    /** Low ordinal: only used as a fallback when nothing else configures these two properties. */
    @Override
    public int getOrdinal() {
        return 50;
    }
}
