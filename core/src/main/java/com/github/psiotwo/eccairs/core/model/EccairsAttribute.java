package com.github.psiotwo.eccairs.core.model;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Representation of ECCAIRS attribute.
 */
@Data
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
public class EccairsAttribute implements EccairsTerm {
    @XmlAttribute(name = "ID", required = true)
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

    @XmlAttribute(name = "SIZE")
    private String size;

    @XmlAttribute(name = "MIN-INSTANCE")
    private String minInstance;

    @XmlAttribute(name = "MAX-INSTANCE")
    private String maxInstance;

    @XmlAttribute(name = "SPECIAL-ATTRIBUTE")
    private String specialAttribute;

    @XmlAttribute(name = "XSD-TAG")
    private String xsdTag;

    @XmlAttribute(name = "DOMAINS")
    private String domains;

    @XmlAttribute(name = "DEFAULT-UNIT")
    private String defaultUnit;

    @XmlElementWrapper(name = "VALUES")
    @XmlElement(name = "VALUE")
    private List<EccairsValue> values;
}
