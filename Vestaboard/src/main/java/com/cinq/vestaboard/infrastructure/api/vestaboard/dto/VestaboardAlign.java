package com.cinq.vestaboard.infrastructure.api.vestaboard.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum VestaboardAlign {
    TOP("top"),BOTTOM("bottom"),CENTER("center"),JUSTIFIED("justified");

    @JsonValue
    public final String value;

    VestaboardAlign(String value) {
        this.value = value;
    }
}
