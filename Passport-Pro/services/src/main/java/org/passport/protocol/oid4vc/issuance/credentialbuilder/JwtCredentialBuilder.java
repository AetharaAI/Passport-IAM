/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.passport.protocol.oid4vc.issuance.credentialbuilder;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;

import org.passport.jose.jws.JWSBuilder;
import org.passport.models.PassportSession;
import org.passport.models.oid4vci.CredentialScopeModel;
import org.passport.protocol.oid4vc.issuance.TimeClaimNormalizer;
import org.passport.protocol.oid4vc.issuance.TimeProvider;
import org.passport.protocol.oid4vc.model.CredentialBuildConfig;
import org.passport.protocol.oid4vc.model.CredentialDefinition;
import org.passport.protocol.oid4vc.model.CredentialSubject;
import org.passport.protocol.oid4vc.model.Format;
import org.passport.protocol.oid4vc.model.SupportedCredentialConfiguration;
import org.passport.protocol.oid4vc.model.VerifiableCredential;
import org.passport.representations.JsonWebToken;

import static org.passport.OID4VCConstants.CLAIM_NAME_SUBJECT_ID;

public class JwtCredentialBuilder implements CredentialBuilder {

    private static final String VC_CLAIM_KEY = "vc";

    private final TimeProvider timeProvider;
    private final UnaryOperator<Instant> issuanceTimeNormalizer;

    public JwtCredentialBuilder(TimeProvider timeProvider) {
        this(timeProvider, instant -> {
            throw new IllegalStateException("PassportSession must not be null when defaulting issuance time");
        });
    }

    public JwtCredentialBuilder(TimeProvider timeProvider, PassportSession session) {
        this(timeProvider, new TimeClaimNormalizer(
                Objects.requireNonNull(session, "PassportSession must not be null when defaulting issuance time")
        )::normalize);
    }

    public JwtCredentialBuilder(TimeProvider timeProvider, UnaryOperator<Instant> issuanceTimeNormalizer) {
        this.timeProvider = Objects.requireNonNull(timeProvider, "TimeProvider must not be null");
        this.issuanceTimeNormalizer = Objects.requireNonNull(issuanceTimeNormalizer, "Issuance time normalizer must not be null");
    }

    @Override
    public String getSupportedFormat() {
        return Format.JWT_VC;
    }

    @Override
    public JwtCredentialBody buildCredentialBody(
            VerifiableCredential verifiableCredential,
            CredentialBuildConfig credentialBuildConfig
    ) throws CredentialBuilderException {
        // Populate the issuer field of the VC
        verifiableCredential.setIssuer(credentialBuildConfig.getCredentialIssuer());

        // Get the issuance date from the credential. Since nbf is mandatory, we set it to the current time if not
        // provided. Only normalize if we're using the default time, as VC issuanceDate is already normalized.
        Instant issuanceInstant = Optional.ofNullable(verifiableCredential.getIssuanceDate())
                .orElseGet(() -> {
                    Instant defaultTime = Instant.ofEpochSecond(timeProvider.currentTimeSeconds());
                    return issuanceTimeNormalizer.apply(defaultTime);
                });

        long iat = issuanceInstant.getEpochSecond();

        // set mandatory fields
        JsonWebToken jsonWebToken = new JsonWebToken()
                .issuer(verifiableCredential.getIssuer().toString())
                .nbf(iat)
                .id(CredentialBuilderUtils.createCredentialId(verifiableCredential));
        jsonWebToken.setOtherClaims(VC_CLAIM_KEY, verifiableCredential);

        // expiry is optional
        Optional.ofNullable(verifiableCredential.getExpirationDate())
                .ifPresent(d -> jsonWebToken.exp(d.getEpochSecond()));

        // sub should only be set if the credential subject has an id.
        CredentialSubject subject = verifiableCredential.getCredentialSubject();
        Optional.ofNullable(subject.getClaims().get(CLAIM_NAME_SUBJECT_ID))
                .map(Object::toString)
                .ifPresent(jsonWebToken::subject);

        JWSBuilder.EncodingBuilder jwsBuilder = new JWSBuilder()
                .type(credentialBuildConfig.getTokenJwsType())
                .jsonContent(jsonWebToken);

        return new JwtCredentialBody(jwsBuilder);
    }

    @Override
    public void contributeToMetadata(SupportedCredentialConfiguration credentialConfig, CredentialScopeModel credentialScope) {
        CredentialDefinition credentialDefinition = CredentialDefinition.parse(credentialScope);
        credentialConfig.setCredentialDefinition(credentialDefinition);
    }
}
