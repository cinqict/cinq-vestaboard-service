package com.cinq.vestaboard.infrastructure.api.vestaboard.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VestaboardMessage {

    private Map<String, String> props;
    private List<VestaboardComponent> components;
}
