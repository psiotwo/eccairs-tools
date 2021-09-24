package com.github.psiotwo.eccairs.core.model;

import java.text.MessageFormat;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Main element of an ECCAIRS/RIT data format. It encapsulates all incident report sections.
 */
@Data
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
public class EccairsEntity implements EccairsTerm {
    @XmlAttribute(name = "ID", required = true)
    private int id;
    @XmlAttribute(name = "DESCRIPTION")
    private String description;
    @XmlAttribute(name = "DETAILED-DESCRIPTION")
    private String detailedDescription;
    @XmlAttribute(name = "EXPLANATION")
    private String explanation;

    @XmlAttribute(name = "MIN-INSTANCE")
    private String minInstance;

    @XmlAttribute(name = "MAX-INSTANCE")
    private String maxInstance;

    @XmlAttribute(name = "XSD-TAG")
    private String xsdTag;

    @XmlAttribute(name = "IS-LINK")
    private String isLink;

    @XmlElementWrapper(name = "ATTRIBUTES")
    @XmlElement(name = "ATTRIBUTE")
    private Set<EccairsAttribute> attributes;

    @XmlElementWrapper(name = "ENTITIES")
    @XmlElement(name = "ENTITY")
    private Set<EccairsEntity> entities;

    public String toString() {
        return MessageFormat.format("{0} - {1}",
            this.getId(),
            this.getDescription()) +
            System.lineSeparator() +
            MessageFormat.format(" - # entities = {0}, # attributes = {1}",
                this.getEntities(),
                this.getAttributes()) +
            System.lineSeparator() +
            MessageFormat.format(" - detailed description: {0}",
                this.getDetailedDescription()) +
            System.lineSeparator() +
            MessageFormat.format(" - explanation: {0}",
                this.getExplanation()) +
            System.lineSeparator() +
            MessageFormat.format(" - minInstance: {0}, maxInstance: {1}, isLink: {2}",
                this.getMinInstance(),
                this.getMaxInstance(),
                this.getIsLink()) +
            System.lineSeparator();
    }
}
