package com.github.psiotwo.eccairs.core.model;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Main element of an ECCAIRS/RIT data format. It encapsulates all incident report sections.
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class EccairsAttribute implements EccairsTerm {
    @XmlAttribute(name = "ID")
    private int id;
    @XmlAttribute(name = "DESCRIPTION")
    private String description;
    @XmlAttribute(name = "DETAILED-DESCRIPTION")
    private String detailedDescription;
    @XmlAttribute(name = "EXPLANATION")
    private String explanation;

    @XmlAttribute(name = "VALUE-TYPE")
    private String valueType;

    @XmlAttribute(name = "DATA-TYPE")
    private String dataType;

    @XmlAttribute(name = "DATA-TYPE")
    private String size;

    @XmlAttribute(name = "MIN-INSTANCE")
    private String minInstance;

    @XmlAttribute(name = "MAX-INSTANCE")
    private String maxInstance;

    @XmlAttribute(name = "SPECIAL-ATTRIBUTE")
    private String specialAttribute;

    @XmlAttribute(name = "XSD-TAG")
    private String xsdTag;

    @XmlElementWrapper(name = "VALUES")
    @XmlElement(name = "VALUE")
    private List<EccairsValue> values;
}
