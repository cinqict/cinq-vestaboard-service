package com.cinq.vestaboard.infrastructure.api.vestaboard.dto;

import lombok.Data;

@Data
public class SetMessageResponse {
    private String status;
    private String id;
    private long created;
}
