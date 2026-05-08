package com.cinq.vestaboard.infrastructure.api.vestaboard.dto;

import lombok.Data;

@Data
public class CurrentMessage {
    private String id;
    private long appeared;
    private String layout;
}
