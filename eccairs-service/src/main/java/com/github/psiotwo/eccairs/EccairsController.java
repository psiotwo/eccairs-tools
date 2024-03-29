package com.github.psiotwo.eccairs;

import static org.springframework.web.bind.annotation.RequestMethod.HEAD;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import com.github.psiotwo.eccairs.core.model.EccairsDictionary;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/taxonomy")
@Api(tags = "ECCAIRS")
@Slf4j
public class EccairsController {

    private final EccairsService eccairsService;

    @Autowired
    public EccairsController(EccairsService importService) {
        this.eccairsService = importService;
    }

    /**
     * Retrieve existing workspace.
     *
     * @param taxonomyFile file with ECCAIRS taxonomy.
     * @return Workspace specified by workspaceFragment and optionally namespace.
     */
    @RequestMapping(method = POST)
    @ApiOperation(value = "Imports ECCAIRS taxonomy.")
    public ResponseEntity<Void> importEccairs(
        @RequestParam("taxonomyFile") MultipartFile taxonomyFile) {
        try {
            final EccairsDictionary result = eccairsService
                .importEccairs(taxonomyFile.getInputStream());
            final URI location = ServletUriComponentsBuilder.fromCurrentRequestUri().path("/{taxonomy}")
                .buildAndExpand(result.getTaxonomy() + " - " + result.getVersion()).toUri();
            return ResponseEntity.created(location).build();
        } catch (IOException e) {
            log.warn("Unable to read the uploaded file.", e);
            return null;
        }
    }

    /**
     * Checks whether ECCAIRS taxonomy is uploaded.
     *
     * @param taxonomyName name of the ECCAIRS taxonomy, e.g. 'aviation'.
     * @param taxonomyVersion ID of the ECCAIRS taxonomy, e.g. '3.4.0.2'.
     */
    @RequestMapping(method = HEAD, value = "/{taxonomyName}/{taxonomyVersion}")
    @ApiOperation(value = "Checks whether ECCAIRS taxonomy is uploaded.")
    public ResponseEntity<URI> isEccairsTaxonomyPresent(
        @PathVariable("taxonomyName") String taxonomyName,
        @PathVariable("taxonomyVersion") String taxonomyVersion ) {
        final boolean result = eccairsService
            .eccairsTaxonomyExists(taxonomyName, taxonomyVersion);
        final URI location = ServletUriComponentsBuilder.fromCurrentRequestUri().path("/{taxonomyName}/{taxonomyVersion}")
            .buildAndExpand(taxonomyName,taxonomyVersion).toUri();
        return result ?
            ResponseEntity.ok(location) :
            ResponseEntity.notFound().build();
    }
}
