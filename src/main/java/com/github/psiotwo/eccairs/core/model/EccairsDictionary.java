package com.github.psiotwo.eccairs.core.model;

import com.github.psiotwo.eccairs.core.EccairsTaxonomyUtils;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
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
        return new StringBuilder()
            .append(MessageFormat.format("ECCAIRS Taxonomy {0} (ver. {1}), language {2}",
                this.getTaxonomy(),
                this.getVersion(),
                this.getLanguage())
            )
            .append(System.lineSeparator())
            .append(MessageFormat.format(" - # root entities = {0}",
                this.getEntities().size()))
            .append(System.lineSeparator())
            .toString();
    }
}
