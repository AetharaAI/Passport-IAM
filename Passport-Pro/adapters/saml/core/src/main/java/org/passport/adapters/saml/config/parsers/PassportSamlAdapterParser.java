/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates
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

import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.passport.saml.common.ErrorCodes;
import org.passport.saml.common.exceptions.ParsingException;
import org.passport.saml.common.parsers.AbstractParser;
import org.passport.saml.common.parsers.StaxParser;
import org.passport.saml.common.util.StaxParserUtil;

/**
 *
 * @author hmlnarik
 */
public class PassportSamlAdapterParser extends AbstractParser {

    private interface ParserFactory {
        public StaxParser create();
    }
    private static final Map<QName, ParserFactory> PARSERS = new HashMap<QName, ParserFactory>();

    // No-namespace variant
    private static final QName ALTERNATE_PASSPORT_SAML_ADAPTER_V1 = new QName(PassportSamlAdapterV1QNames.PASSPORT_SAML_ADAPTER.getQName().getLocalPart());

    static {
        PARSERS.put(PassportSamlAdapterV1QNames.PASSPORT_SAML_ADAPTER.getQName(),   new ParserFactory() { @Override public StaxParser create() { return PassportSamlAdapterV1Parser.getInstance(); }});
        PARSERS.put(ALTERNATE_PASSPORT_SAML_ADAPTER_V1,                             new ParserFactory() { @Override public StaxParser create() { return PassportSamlAdapterV1Parser.getInstance(); }});
    }

    private static final PassportSamlAdapterParser INSTANCE = new PassportSamlAdapterParser();

    public static PassportSamlAdapterParser getInstance() {
        return INSTANCE;
    }

    protected PassportSamlAdapterParser() {
    }

    /**
     * @see {@link org.passport.saml.common.parsers.ParserNamespaceSupport#parse(XMLEventReader)}
     */
    @Override
    public Object parse(XMLEventReader xmlEventReader) throws ParsingException {
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = StaxParserUtil.peek(xmlEventReader);

            if (xmlEvent instanceof StartElement) {
                StartElement startElement = (StartElement) xmlEvent;
                final QName name = startElement.getName();

                ParserFactory pf = PARSERS.get(name);
                if (pf == null) {
                    throw logger.parserException(new RuntimeException(ErrorCodes.UNKNOWN_START_ELEMENT + name + "::location="
                            + startElement.getLocation()));
                }

                return pf.create().parse(xmlEventReader);
            }

            StaxParserUtil.getNextEvent(xmlEventReader);
        }

        throw new RuntimeException(ErrorCodes.FAILED_PARSING + "SAML Parsing has failed");
    }
}
