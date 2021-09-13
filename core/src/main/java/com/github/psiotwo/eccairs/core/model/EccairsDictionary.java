package com.github.psiotwo.eccairs.core.model;

import java.text.MessageFormat;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@XmlRootElement(name = "DICTIONARY")
@XmlAccessorType(XmlAccessType.FIELD)
public class EccairsDictionary {
    @XmlAttribute(name = "TAXONOMY")
    private String taxonomy;
    @XmlAttribute(name = "VERSION")
    private String version;
    @XmlAttribute(name = "LANGUAGE")
    private String language;
    @XmlAttribute(name = "KEY")
    private String key;

    @XmlElement(name = "ENTITY")
    private List<EccairsEntity> entities;

    public String toString() {
        return MessageFormat.format("ECCAIRS Taxonomy {0} (ver. {1}), language {2}",
            this.getTaxonomy(),
            this.getVersion(),
            this.getLanguage()) +
            System.lineSeparator() +
            MessageFormat.format(" - # root entities = {0}",
                this.getEntities().size()) +
            System.lineSeparator();
    }
}
