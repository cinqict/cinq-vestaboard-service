package com.cinq.vestaboard.infrastructure.api.vestaboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

@Data
public class SetMessageRequest {
    @Schema(description = "The type of message to be displayed on the Vestaboard", allowableValues = {"text", "progress"})
    private String type;
    @Schema(description = "Parameters for the Vestaboard message. Values depend on the message type. " +
            "Please refer to this project's README for all message types and their corresponding parameters")
    private Map<String, Object> params;
}
