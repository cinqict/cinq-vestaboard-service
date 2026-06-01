package com.cinq.vestaboard.infrastructure.api.vestaboard.dto;

import lombok.Data;

@Data
public class ScoreBoardEntry {
    private int rank;
    private String name;
    private int score;
}
