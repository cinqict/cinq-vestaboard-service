package com.cinq.vestaboard.infrastructure.api.vestaboard.dto;

import lombok.Data;

@Data
public class SetMessageCharactersRequest {
    private int[][] characters = new int[22][6];
    private boolean forced = true;
}
