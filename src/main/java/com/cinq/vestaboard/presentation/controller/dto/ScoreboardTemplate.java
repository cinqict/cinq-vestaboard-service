package com.cinq.vestaboard.presentation.controller.dto;

import com.cinq.vestaboard.infrastructure.api.vestaboard.dto.ScoreBoardEntry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScoreboardTemplate {
    private String title;
    private List<ScoreBoardEntry> entries;
}
