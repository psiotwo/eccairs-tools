package com.github.psiotwo.eccairs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class EccairsServiceTest {

    @Mock
    private EccairsDao dao;

    @InjectMocks
    private EccairsService sut;

    private InputStream getResource(String name) throws IOException {
        return EccairsServiceTest.class.getResource(name).openStream();
    }

    @Test
    public void importEccairsSavesTheTaxonomy() throws IOException {
        sut.importEccairs(getResource("/data/ECCAIRS Aviation v.4.1.0.7-sample.xml"));
        verify(dao, times(1)).saveEccairs(any());
    }

    @Test
    public void eccairsTaxonomyExistsUsesDaoCorrectly() {
        when(dao.eccairsTaxonomyExists(anyString(), anyString())).thenReturn(true);
        final boolean exists = sut.eccairsTaxonomyExists("aviation", "3.4.0.2");
        Assertions.assertTrue(exists);
        verify(dao, times(1)).eccairsTaxonomyExists("aviation", "3.4.0.2");
    }
}
