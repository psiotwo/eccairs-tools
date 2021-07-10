package com.github.psiotwo.eccairs.rit.util;

import com.github.psiotwo.eccairs.rit.model.Value;
import com.github.psiotwo.eccairs.rit.model.ValueList;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@Slf4j
public class E5XXSDValueListParser {

    private ValueList createValueList(final NodeList values, final XPath xpath, final String id)
        throws XPathExpressionException {
        // output file
        final ValueList valueList = new ValueList();
        for (int i = 0; i < values.getLength(); i++) {
            Element n = (Element) values.item(i);
            String valueId = n.getAttribute("value");
            if ( valueId == null || valueId.isEmpty()) {
                valueId = "0";
            }
            final NodeList values2 = (NodeList) xpath.compile(
                "annotation/documentation").evaluate(n, XPathConstants.NODESET);
            String description = null;
            String detailedDescription = null;
            String explanation = null;
            for (int j = 0; j < values2.getLength(); j++) {
                final Element nx = (Element) values2.item(j);
                final String source = nx.getAttribute("source");
                switch (source) {
                    case "description":
                        description = nx.getTextContent();
                        break;
                    case "detailedDescription":
                        detailedDescription = nx.getTextContent();
                        break;
                    case "explanation":
                        explanation = nx.getTextContent();
                        break;
                }
            }
            valueList.getValues().add(new Value()
                .setValueId(Integer.parseInt(valueId))
                .setDescription(description)
                .setDetailedDescription(detailedDescription)
                .setExplanation(explanation)
                .setValueListId(id));
        }
        return valueList;
    }

    public ValueList parse(String vlName, File file) {
        log.info("Processing file {}", file);
        try (final FileInputStream fis = new FileInputStream(file);) {
            // Setup classes to parse XSD file for complex types
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            final DocumentBuilder db = dbf.newDocumentBuilder();
            final Document doc = db.parse(fis);

            // Given the id, go to correct place in XSD to get all the parameters
            final XPath xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(new NamespaceResolver(doc));
            final NodeList values = (NodeList) xpath.compile(
                "//*[@name='" + vlName + "']/restriction/enumeration")
                .evaluate(doc, XPathConstants.NODESET);

            return createValueList(values, xpath, file.getAbsolutePath());
        } catch (IOException | SAXException | XPathExpressionException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
}
