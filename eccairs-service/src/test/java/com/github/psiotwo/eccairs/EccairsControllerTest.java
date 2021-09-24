package com.github.psiotwo.eccairs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import com.github.psiotwo.eccairs.core.model.EccairsDictionary;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = EccairsController.class)
public class EccairsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EccairsService service;

    @Test
    void eccairsTaxonomyExistsCallsService_thenReturns200() throws Exception {
        when(service.eccairsTaxonomyExists(any())).thenReturn(true);
        mockMvc.perform(
            head("/taxonomy/{taxonomy}", "eccairs-aviation-3.4.0.2")
        ).andExpect(
            status().isOk()
        );
    }

    @Test
    void importEccairsCallsService_thenReturns201() throws Exception {
        when(service.importEccairs(any())).thenReturn(new EccairsDictionary());
        MockMultipartFile file =
            new MockMultipartFile("taxonomyFile",
                "filename.xml",
                "text/plain",
                "some xml".getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(
            MockMvcRequestBuilders
                .multipart("/taxonomy", "eccairs-aviation-3.4.0.2")
                .file(file)
        ).andExpect(
            status().isCreated()
        );
    }
}