package com.cinq.vestaboard.infrastructure.api.vestaboard.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VestaboardComponent {

    private VestaboardStyle style;
    private String template;
}
