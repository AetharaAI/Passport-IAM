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

import org.passport.adapters.saml.config.PassportSamlAdapter;
import org.passport.saml.common.exceptions.ParsingException;
import org.passport.saml.common.util.StaxParserUtil;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class PassportSamlAdapterV1Parser extends AbstractPassportSamlAdapterV1Parser<PassportSamlAdapter> {

    private static final PassportSamlAdapterV1Parser INSTANCE = new PassportSamlAdapterV1Parser();

    private PassportSamlAdapterV1Parser() {
        super(PassportSamlAdapterV1QNames.PASSPORT_SAML_ADAPTER);
    }

    public static PassportSamlAdapterV1Parser getInstance() {
        return INSTANCE;
    }

    @Override
    protected PassportSamlAdapter instantiateElement(XMLEventReader xmlEventReader, StartElement element) throws ParsingException {
        return new PassportSamlAdapter();
    }

    @Override
    protected void processSubElement(XMLEventReader xmlEventReader, PassportSamlAdapter target, PassportSamlAdapterV1QNames element, StartElement elementDetail) throws ParsingException {
        switch (element) {
            case SP:
                target.addSp(SpParser.getInstance().parse(xmlEventReader));
                break;

            default:
                // Ignore unknown tags
                StaxParserUtil.bypassElementBlock(xmlEventReader);
        }
    }
}
