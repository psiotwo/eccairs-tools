package com.github.psiotwo.eccairs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "eccairs")
@Getter
@Setter
public class Conf {
    private String baseUri;
    private String sparqlQueryEndpoint;
    private String sparqlGspEndpointTemplate;
}