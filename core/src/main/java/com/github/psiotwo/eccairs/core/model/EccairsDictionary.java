package com.github.psiotwo.eccairs.core.model;

import java.text.MessageFormat;
import java.util.List;
import javax.xml.bind.annotation.*;

import com.github.psiotwo.eccairs.core.EccairsTaxonomyUtils;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@XmlRootElement(name = "DICTIONARY")
@XmlAccessorType(XmlAccessType.FIELD)
public class EccairsDictionary {

    @XmlTransient
    private String taxonomy;

    @XmlAttribute(name = "VERSION")
    private String version;
    @XmlAttribute(name = "LANGUAGE")
    private String language;
    @XmlAttribute(name = "KEY")
    private String key;

    @XmlElement(name = "ENTITY")
    private List<EccairsEntity> entities;

    @XmlAttribute(name = "TAXONOMY")
    public String getTaxonomy() {
        return taxonomy;
    }

    public void setTaxonomy( final String taxonomy ) {
        this.taxonomy = taxonomy == null ? taxonomy : EccairsTaxonomyUtils.getTaxonomyName(taxonomy);
    }

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
