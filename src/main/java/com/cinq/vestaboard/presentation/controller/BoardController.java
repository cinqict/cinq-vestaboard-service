package com.cinq.vestaboard.presentation.controller;

import com.cinq.vestaboard.application.BoardService;
import com.cinq.vestaboard.infrastructure.api.vestaboard.dto.SetMessageRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vestaboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Vestaboard", description = "Set and retrieve the current Vestaboard message")
public class BoardController {

    private final BoardService boardService;

    @GetMapping
    @Operation(summary = "Get current Vestaboard message")
    public String getCurrentMessage() {
        return boardService.getCurrentMessage();
    }

    @PostMapping
    @Operation(summary = "Set new Vestaboard message")
    public String setMessage(
            @Valid @RequestBody SetMessageRequest request,
            @Parameter(description = "Send the message even if it matches what is currently displayed")
            @RequestParam(defaultValue = "false") boolean force) {
        return boardService.setMessage(request, force);
    }
}
