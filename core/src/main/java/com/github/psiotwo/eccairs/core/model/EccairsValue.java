package com.github.psiotwo.eccairs.core.model;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.github.psiotwo.eccairs.core.StringTrimAdapter;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@XmlAccessorType(XmlAccessType.FIELD)
public class EccairsValue implements EccairsTerm {
    @XmlAttribute(name = "ID", required = true)
    private int id;
    @XmlJavaTypeAdapter(StringTrimAdapter.class)
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
