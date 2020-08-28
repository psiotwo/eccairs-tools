package com.github.psiotwo.eccairs.core.model;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class EccairsValue implements EccairsTerm {
    @XmlAttribute(name = "ID")
    private int id;
    @XmlAttribute(name = "DESCRIPTION")
    private String description;
    @XmlAttribute(name = "DETAILED-DESCRIPTION")
    private String detailedDescription;
    @XmlAttribute(name = "EXPLANATION")
    private String explanation;
    @XmlAttribute(name = "DOMAINS")
    private String domains;

    @XmlAttribute(name = "LEVEL")
    private String level;

    @XmlElement(name = "VALUE")
    private List<EccairsValue> values;

}
