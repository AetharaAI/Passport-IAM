package org.passport.crypto.def.test;

import org.passport.common.crypto.CryptoConstants;
import org.passport.common.crypto.CryptoIntegration;
import org.passport.crypto.def.AesKeyWrapAlgorithmProvider;
import org.passport.jose.jwe.alg.JWEAlgorithmProvider;
import org.passport.rule.CryptoInitRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DefaultCryptoUnitTest {

    @ClassRule
    public static CryptoInitRule cryptoInitRule = new CryptoInitRule();

    @Test
    public void testDefaultCrypto() throws Exception {
        JWEAlgorithmProvider jweAlg = CryptoIntegration.getProvider().getAlgorithmProvider(JWEAlgorithmProvider.class, CryptoConstants.A128KW);
        Assert.assertEquals(jweAlg.getClass(), AesKeyWrapAlgorithmProvider.class);
    }
}
