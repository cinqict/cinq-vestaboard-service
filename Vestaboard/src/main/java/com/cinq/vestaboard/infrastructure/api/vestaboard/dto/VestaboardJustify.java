package com.cinq.vestaboard.infrastructure.api.vestaboard.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum VestaboardJustify {
    LEFT("left"),RIGHT("right"),CENTER("center"),JUSTIFIED("justified");

    @JsonValue
    public final String value;

    VestaboardJustify(String value) {
        this.value = value;
    }
}
