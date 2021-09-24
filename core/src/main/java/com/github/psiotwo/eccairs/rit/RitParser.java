package com.github.psiotwo.eccairs.rit;

import com.github.psiotwo.eccairs.rit.model.RitAttribute;
import com.github.psiotwo.eccairs.rit.model.RitEntity;
import com.github.psiotwo.eccairs.rit.model.RitModel;
import com.github.psiotwo.eccairs.rit.model.Value;
import com.github.psiotwo.eccairs.rit.model.ValueList;
import com.opencsv.CSVParser;
import com.opencsv.CSVReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Setter
@Getter
@Slf4j
public class RitParser {

    public static final int ATTRIBUTE_SYNONYM = 0;
    public static final int PARENT_ENTITY_SYNONYM = 1;
    public static final int ATTRIBUTE_ID = 2;
    public static final int PARENT_ENTITY_ID = 3;
    public static final int ECCAIRS_DATATYPE = 4;
    public static final int UM_DEFAULT = 5;
    public static final int VALUELIST_ID = 6;
    public static final int SPECIAL_ATTRIBUTE_ID = 7;
    public static final int MIN_INSTANCES = 8;
    public static final int MAX_INSTANCES = 9;
    public static final int ATTRIBUTE_SEQUENCE = 10;

    public static final String DOCUMENTS_ATTRIBUTE_LIST_CSV = "/documents/AttributeList.csv";
    public static final String MAPPINGS_ATTRIBUTES_CSV = "/mappings/Attributes.csv";
    private String dir;

    public RitParser(String dir) {
        this.dir = dir;
    }

    private void parseAttributeList(final RitModel model) {
        try (CSVReader csvReader
                 = new CSVReader(new FileReader(dir + DOCUMENTS_ATTRIBUTE_LIST_CSV, Charset.defaultCharset()), '\t',
            CSVParser.DEFAULT_QUOTE_CHARACTER, 1)) {
            String[] values;
            while ((values = csvReader.readNext()) != null) {
                //        "Entity ID"	"Entity name"	"Entity synonym for RIT"	"EntityID path"	"Entity name path"	"Entity name path for RIT"	"Attribute ID"	"Short Description"	"Detailed Description"	"Attribute synonym for RIT"
                int attributeId = Integer.parseInt(values[6]);
                RitAttribute a = new RitAttribute()
                    .setId(attributeId)
                    .setShortDescription(values[7])
                    .setDetailedDescription(values[8])
                    .setSynonymForRit(values[9]);
                model.getAttributes().put(attributeId, a);

                Integer entityId = Integer.parseInt(values[0]);
                model.getEntities().putIfAbsent(entityId, new RitEntity()
                    .setId(Integer.parseInt(values[0]))
                    .setName(values[1])
                    .setSynonymForRit(values[2])
                    .setIdPath(values[3])
                    .setNamePath(values[4])
                    .setNamePathForRit(values[5])
                );
                RitEntity e = model.getEntities().get(entityId);
                e.getAttributes().add(a);
            }
        } catch (IOException e) {
            log.error("Error during attribute list parsing. ", e);
        }
    }

    private void parseAttributes(final RitModel model) {
        try (CSVReader csvReader
                 = new CSVReader(new FileReader(dir + MAPPINGS_ATTRIBUTES_CSV, Charset.defaultCharset()), '\t',
            CSVParser.DEFAULT_QUOTE_CHARACTER, 1)) {
            String[] values;
            while ((values = csvReader.readNext()) != null) {
                final Integer id = Integer.parseInt(values[ATTRIBUTE_ID]);
                if (!model.getAttributes().containsKey(id)) {
                    log.error("Row {} does not match any attribute", Arrays.asList(values));
                }

                RitAttribute a = model.getAttributes().get(id);
                if (!a.getSynonymForRit().equals(values[ATTRIBUTE_SYNONYM])) {
                    log.error("Mismatch in synonym for RIT: {} vs. {}", a, values);
                }

                a.setEccairsDatatype(Integer.parseInt(values[ECCAIRS_DATATYPE]))
                    .setUmDefault(values[UM_DEFAULT])
                    .setValueListId(values[VALUELIST_ID])
                    .setMinInstances(Integer.parseInt(values[MIN_INSTANCES]))
                    .setMaxInstances(Integer.parseInt(values[MAX_INSTANCES]))
                    .setAttributeSequence(Integer.parseInt(values[ATTRIBUTE_SEQUENCE]));

                if (!values[SPECIAL_ATTRIBUTE_ID].isEmpty()) {
                    a.setSpecialAttributeId(Integer.parseInt(values[SPECIAL_ATTRIBUTE_ID]));
                }

                Integer entityId = Integer.parseInt(values[PARENT_ENTITY_ID]);
                model.getEntities().putIfAbsent(entityId, new RitEntity()
                    .setId(Integer.parseInt(values[PARENT_ENTITY_ID]))
                    .setSynonymForRit(values[1])
                );
                RitEntity e = model.getEntities().get(entityId);
                e.getAttributes().add(a);
            }
        } catch (IOException e) {
            log.error("Error during attributes parsing. ", e);
        }
    }

    public void parseValueList(final RitModel model, final File f) {
        final ValueList vl = new ValueList();
        vl.setId(f.getName().substring(0, f.getName().lastIndexOf(".")));
        try (CSVReader csvReader
                 = new CSVReader(new FileReader(f, Charset.defaultCharset()), '\t', CSVParser.DEFAULT_QUOTE_CHARACTER, 1)) {
            String[] values;
            while ((values = csvReader.readNext()) != null) {
                // Value Synonym	Value ID
                final Integer id = Integer.parseInt(values[1]);
                // TODO synonym
                Value a = new Value()
                    .setValueId(id)
                    .setValueListId(vl.getId());
                vl.getValues().add(a);
            }
            model.getValueLists().put(vl.getId(), vl);
        } catch (IOException e) {
            log.error("Error during valuelist parsing. ", e);
        }
    }

    public RitModel parse() {
        log.info("Parsing RIT distribution");
        final RitModel model = new RitModel();
        model.setDir(dir);

        log.info("Parsing {}", DOCUMENTS_ATTRIBUTE_LIST_CSV);
        parseAttributeList(model);

        log.info("Parsing {}", MAPPINGS_ATTRIBUTES_CSV);
        parseAttributes(model);

        File[] valueListFiles = new File(dir + "/mappings/")
            .listFiles(n ->
                n.getName().startsWith("VL")
            );
        assert valueListFiles != null;
        for (File f : valueListFiles) {
            parseValueList(model, f);
        }
        return model;
    }


    public static void main(String[] args) {
        final RitParser m = new RitParser(args[0]);
        final RitModel model = m.parse();
        System.out.println(model);
    }
}
