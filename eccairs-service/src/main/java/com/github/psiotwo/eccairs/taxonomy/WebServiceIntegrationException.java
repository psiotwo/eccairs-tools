package com.github.psiotwo.eccairs.taxonomy;

/**
 * Indicates that a request to a remote service failed.
 */
public class WebServiceIntegrationException extends RuntimeException {

    public WebServiceIntegrationException(String message) {
        super(message);
    }

    public WebServiceIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
