package com.cinq.vestaboard.infrastructure.api.vestaboard.dto;

import com.cinq.vestaboard.domain.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class SetMessageRequest {
    @NotNull
    @Schema(description = "The type of message to display on the Vestaboard", allowableValues = {"PROGRESS", "CELEBRATION"})
    private MessageType type;

    @NotNull
    @Schema(description = "Parameters for the message type. " +
            "PROGRESS: title (string), current (int), total (int). " +
            "CELEBRATION: title (string).")
    private Map<String, Object> params;
}
