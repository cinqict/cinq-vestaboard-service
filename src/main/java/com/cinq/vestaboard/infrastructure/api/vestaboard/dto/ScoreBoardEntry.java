package com.cinq.vestaboard.infrastructure.api.vestaboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreBoardEntry {
    private int rank;
    private String name;
    private int score;
}
