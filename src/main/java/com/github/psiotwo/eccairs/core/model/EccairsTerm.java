package com.github.psiotwo.eccairs.core.model;

/**
 * General ECCAIRS taxonomy element - wraps entities, attributes and values.
 */
public interface EccairsTerm {

    int getId();

    String getDescription();

    String getDetailedDescription();

    String getExplanation();
}
