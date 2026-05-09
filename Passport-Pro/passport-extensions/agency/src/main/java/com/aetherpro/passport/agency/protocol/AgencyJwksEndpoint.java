package com.aetherpro.passport.agency.protocol;

import com.aetherpro.passport.agency.AgencyProvider;
import com.aetherpro.passport.agency.jpa.AgencyKeypairEntity;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWKS (JSON Web Key Set) endpoint for Agency public keys
 *
 * This endpoint publishes public keys for cryptographic verification
 * without requiring authentication. Services can verify Agent Passport
 * signatures by fetching keys from this endpoint.
 *
 * URL: GET /realms/{realm}/agency/jwks
 */
public class AgencyJwksEndpoint {

    private static final Logger logger = Logger.getLogger(AgencyJwksEndpoint.class);

    private final PassportSession session;
    private final RealmModel realm;

    public AgencyJwksEndpoint(PassportSession session, RealmModel realm) {
        this.session = session;
        this.realm = realm;
    }

    /**
     * Returns all ACTIVE public keys for this realm in JWKS format
     *
     * Format follows RFC 7517 (JSON Web Key)
     * https://www.rfc-editor.org/rfc/rfc7517.html
     */
    @GET
    @Path("/jwks")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getJwks() {
        AgencyProvider agency = session.getProvider(AgencyProvider.class);

        List<Map<String, Object>> keys = new ArrayList<>();

        try {
            // Get issuer (realm) keypair
            List<AgencyKeypairEntity> issuerKeys = agency.getKeypairs(realm, "ISSUER", realm.getId());
            for (AgencyKeypairEntity keypair : issuerKeys) {
                if ("ACTIVE".equals(keypair.getStatus())) {
                    keys.add(buildJwk(keypair, "sig", "Issuer key for " + realm.getName()));
                }
            }

            logger.debugf("Returning %d active keys for realm %s", keys.size(), realm.getName());

        } catch (Exception e) {
            logger.error("Error fetching JWKS for realm " + realm.getName(), e);
        }

        Map<String, Object> jwks = new HashMap<>();
        jwks.put("keys", keys);
        return jwks;
    }

    /**
     * Build a JWK (JSON Web Key) object for Ed25519 public key
     */
    private Map<String, Object> buildJwk(AgencyKeypairEntity keypair, String use, String description) {
        Map<String, Object> jwk = new HashMap<>();

        // Key type
        jwk.put("kty", "OKP"); // Octet Key Pair (for EdDSA)

        // Curve
        jwk.put("crv", "Ed25519");

        // Key ID
        jwk.put("kid", keypair.getKid());

        // Algorithm
        jwk.put("alg", "EdDSA");

        // Use (sig = signature, enc = encryption)
        jwk.put("use", use);

        // Public key (base64url encoded)
        String publicKeyBase64 = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(keypair.getPublicKeyBytes());
        jwk.put("x", publicKeyBase64);

        // Metadata
        if (description != null) {
            jwk.put("x5t", description); // Certificate thumbprint field repurposed for description
        }

        jwk.put("entity_type", keypair.getEntityType());
        jwk.put("entity_id", keypair.getEntityId());

        return jwk;
    }
}
