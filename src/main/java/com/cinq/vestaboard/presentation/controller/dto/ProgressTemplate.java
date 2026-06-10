package com.cinq.vestaboard.presentation.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProgressTemplate {
    private String title;
    private int current;
    private int total;
}
