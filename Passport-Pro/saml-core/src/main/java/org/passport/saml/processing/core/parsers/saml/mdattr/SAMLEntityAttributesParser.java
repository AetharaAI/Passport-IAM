package org.passport.saml.processing.core.parsers.saml.mdattr;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import org.passport.dom.saml.v2.mdattr.EntityAttributes;
import org.passport.saml.common.exceptions.ParsingException;
import org.passport.saml.common.util.StaxParserUtil;
import org.passport.saml.processing.core.parsers.saml.assertion.SAMLAssertionParser;
import org.passport.saml.processing.core.parsers.saml.assertion.SAMLAttributeParser;
import org.passport.saml.processing.core.parsers.saml.metadata.AbstractStaxSamlMetadataParser;
import org.passport.saml.processing.core.parsers.saml.metadata.SAMLMetadataQNames;

public class SAMLEntityAttributesParser extends AbstractStaxSamlMetadataParser<EntityAttributes> {
    private static final SAMLEntityAttributesParser INSTANCE = new SAMLEntityAttributesParser();

    private SAMLEntityAttributesParser() {
        super(SAMLMetadataQNames.ENTITY_ATTRIBUTES);
    }

    public static SAMLEntityAttributesParser getInstance() {
        return INSTANCE;
    }

    @Override
    protected EntityAttributes instantiateElement(XMLEventReader xmlEventReader, StartElement element) throws ParsingException {
        return new EntityAttributes();
    }

    @Override
    protected void processSubElement(XMLEventReader xmlEventReader, EntityAttributes target, SAMLMetadataQNames element,
        StartElement elementDetail) throws ParsingException {
        switch (element) {
            case ATTRIBUTE:
                target.addAttribute(SAMLAttributeParser.getInstance().parse(xmlEventReader));
                break;
            case ASSERTION:
                target.addAssertion(SAMLAssertionParser.getInstance().parse(xmlEventReader));
                break;
            default:
                throw LOGGER.parserUnknownTag(StaxParserUtil.getElementName(elementDetail), elementDetail.getLocation());
        }
    }
}
