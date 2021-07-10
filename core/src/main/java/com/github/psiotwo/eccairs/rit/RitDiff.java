package com.github.psiotwo.eccairs.rit;

import com.github.psiotwo.eccairs.rit.model.RitAttribute;
import com.github.psiotwo.eccairs.rit.model.RitEntity;
import com.github.psiotwo.eccairs.rit.model.RitModel;
import java.io.IOException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RitDiff {
    private RitModel m1;
    private RitModel m2;

    public RitDiff(RitModel m1, RitModel m2) {
        this.m1 = m1;
        this.m2 = m2;
    }

    public void analyse() {
        this.compareTo(m1, m2);
        this.compareTo(m2, m1);
    }

    private void compareTo(RitModel m1, RitModel m2) {
        log.info("Comparing {} to {}", m1.getDir(), m2.getDir());
        for (RitEntity e : m1.getEntities().values()) {
            if (!m2.getEntities().containsKey(e.getId())) {
                log.info(
                    "[E NOT PRESENT] {} - {} : {}", e.getId(), e.getSynonymForRit(), m2.getDir());
                continue;
            }

            final RitEntity dEntity = m2.getEntities().get(e.getId());

            if (!e.getSynonymForRit().equals(dEntity.getSynonymForRit())) {
                log.info("[RENAMED TO] {}", dEntity.getSynonymForRit());
                continue;
            }

            for (RitAttribute a : e.getAttributes()) {
                if (!dEntity.getAttributes().stream().map(aa -> aa.getId()).collect(
                    Collectors.toList()).contains(a.getId())) {
                    log.info(
                        " - [A NOT PRESENT] {} - {} in {}", a.getId(), a.getSynonymForRit(),
                        m2.getDir());
                    continue;
                } else if (!dEntity.getAttributes().contains(a)) {
                    log.info(
                        " - [A CHANGED] {} - {} in {}", a.getId(), a.getSynonymForRit(),
                        m2.getDir());
                    continue;
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        final String d1d = args[0];
        final String d2d = args[1];
        new RitDiff(
            new RitParser(d1d).parse(),
            new RitParser(d2d).parse()
        ).analyse();
    }
}
