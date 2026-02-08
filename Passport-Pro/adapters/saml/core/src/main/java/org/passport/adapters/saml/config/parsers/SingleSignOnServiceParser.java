/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.passport.adapters.saml.config.parsers;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import org.passport.adapters.saml.config.IDP;
import org.passport.saml.common.exceptions.ParsingException;
import org.passport.saml.common.util.StaxParserUtil;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class SingleSignOnServiceParser extends AbstractPassportSamlAdapterV1Parser<IDP.SingleSignOnService> {

    private static final SingleSignOnServiceParser INSTANCE = new SingleSignOnServiceParser();

    private SingleSignOnServiceParser() {
        super(PassportSamlAdapterV1QNames.SINGLE_SIGN_ON_SERVICE);
    }

    public static SingleSignOnServiceParser getInstance() {
        return INSTANCE;
    }

    @Override
    protected IDP.SingleSignOnService instantiateElement(XMLEventReader xmlEventReader, StartElement element) throws ParsingException {
        final IDP.SingleSignOnService sso = new IDP.SingleSignOnService();

        sso.setSignRequest(StaxParserUtil.getBooleanAttributeValueRP(element, PassportSamlAdapterV1QNames.ATTR_SIGN_REQUEST));
        sso.setValidateResponseSignature(StaxParserUtil.getBooleanAttributeValueRP(element, PassportSamlAdapterV1QNames.ATTR_VALIDATE_RESPONSE_SIGNATURE));
        sso.setValidateAssertionSignature(StaxParserUtil.getBooleanAttributeValueRP(element, PassportSamlAdapterV1QNames.ATTR_VALIDATE_ASSERTION_SIGNATURE));
        sso.setRequestBinding(StaxParserUtil.getAttributeValueRP(element, PassportSamlAdapterV1QNames.ATTR_REQUEST_BINDING));
        sso.setResponseBinding(StaxParserUtil.getAttributeValueRP(element, PassportSamlAdapterV1QNames.ATTR_RESPONSE_BINDING));
        sso.setBindingUrl(StaxParserUtil.getAttributeValueRP(element, PassportSamlAdapterV1QNames.ATTR_BINDING_URL));
        sso.setAssertionConsumerServiceUrl(StaxParserUtil.getAttributeValueRP(element, PassportSamlAdapterV1QNames.ATTR_ASSERTION_CONSUMER_SERVICE_URL));

        return sso;
    }

    @Override
    protected void processSubElement(XMLEventReader xmlEventReader, IDP.SingleSignOnService target, PassportSamlAdapterV1QNames element, StartElement elementDetail) throws ParsingException {
    }
}
