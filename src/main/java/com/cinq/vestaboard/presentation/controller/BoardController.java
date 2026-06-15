package com.cinq.vestaboard.presentation.controller;

import com.cinq.vestaboard.application.BoardService;
import com.cinq.vestaboard.domain.MessageType;
import com.cinq.vestaboard.infrastructure.api.vestaboard.dto.ScoreBoardEntry;
import com.cinq.vestaboard.infrastructure.api.vestaboard.dto.SetMessageRequest;
import com.cinq.vestaboard.presentation.controller.dto.CelebrationTemplate;
import com.cinq.vestaboard.presentation.controller.dto.ProgressTemplate;
import com.cinq.vestaboard.presentation.controller.dto.ScoreboardTemplate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/text")
    @Operation(summary = "Get current Vestaboard message as text")
    public String getCurrentTextMessage() {
        GetCurrentMessageResponse currentMessage = vestaBoardGateway.getCurrentMessage();
        String layout = currentMessage.getCurrentMessage().getLayout();

        String stripped = layout.replaceAll("\\s", "").replaceAll("^\\[\\[|]]$", "");
        String[] rows = stripped.split("],\\[");

        StringBuilder sb = new StringBuilder();
        for (String row : rows) {
            for (String code : row.split(",")) {
                sb.append(decodeCharacter(Integer.parseInt(code)));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private static String decodeCharacter(int code) {
        if (code == 0) return " ";
        if (code >= 1 && code <= 26) return String.valueOf((char) ('A' + code - 1));
        return switch (code) {
            case 27 -> "1";
            case 28 -> "2";
            case 29 -> "3";
            case 30 -> "4";
            case 31 -> "5";
            case 32 -> "6";
            case 33 -> "7";
            case 34 -> "8";
            case 35 -> "9";
            case 36 -> "0";
            case 37 -> "!";
            case 38 -> "@";
            case 39 -> "#";
            case 40 -> "$";
            case 41 -> "(";
            case 42 -> ")";
            case 44 -> "-";
            case 46 -> "+";
            case 47 -> "&";
            case 48 -> "=";
            case 49 -> ";";
            case 50 -> ":";
            case 52 -> "'";
            case 53 -> "\"";
            case 54 -> "%";
            case 55 -> ",";
            case 56 -> ".";
            case 59 -> "/";
            case 60 -> "?";
            case 62 -> "°";
            default -> "{" + code + "}";
        };
    }

    @PostMapping
    @Operation(summary = "Set new Vestaboard message")
    public String setMessage(
            @Valid @RequestBody SetMessageRequest request,
            @Parameter(description = "Send the message even if it matches what is currently displayed")
            @RequestParam(defaultValue = "false") boolean force,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("Set message requested by: {}", jwt.getClaimAsString("email"));
        return boardService.setMessage(request, force);
    }

    @GetMapping("/template")
    @Operation(summary = "Get template params required for type")
    public Object getTemplateParams(
            @Parameter(description = "Send the message even if it matches what is currently displayed")
            @RequestParam MessageType type
    ) {
        switch (type) {
            case PROGRESS:
                return new ProgressTemplate("Title", 25, 100);
            case CELEBRATION:
                return new CelebrationTemplate("Title");
            case SCOREBOARD:
                return new ScoreboardTemplate("Title", List.of(
                        new ScoreBoardEntry(1, "name", 10),
                        new ScoreBoardEntry(2, "name2", 7),
                        new ScoreBoardEntry(3, "name3", 4)
                ));
            default:
                return "";
        }
    }

    @PostMapping(value = "/compose")
    @Operation(summary = "Compose new Vestaboard message")
    public int[][] compose(
            @Valid @RequestBody SetMessageRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("Request by: {}", jwt.getClaimAsString("email"));

        return boardService.compose(request);
    }
}
