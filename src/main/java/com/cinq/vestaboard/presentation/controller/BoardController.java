package com.cinq.vestaboard.presentation.controller;

import com.cinq.vestaboard.infrastructure.api.vestaboard.MessageCreator;
import com.cinq.vestaboard.infrastructure.api.vestaboard.VestaBoardGateway;
import com.cinq.vestaboard.infrastructure.api.vestaboard.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/vestaboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Vestaboard", description = "Set and retrieve the current Vestaboard message")
public class BoardController {

    @Autowired
    private VestaBoardGateway vestaBoardGateway;
    @Autowired
    private MessageCreator messageCreator;

    @GetMapping
    @Operation(summary = "Get current Vestaboard message")
    public String getCurrentMessage() {
        GetCurrentMessageResponse currentMessage = vestaBoardGateway.getCurrentMessage();

        String layout = currentMessage.getCurrentMessage().getLayout();
        return layout;
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
            @RequestBody SetMessageRequest message) {
        VestaboardMessage vestaboardMessage = messageCreator.create(message.getType(), message.getParams());
        int[][] composedMessage = vestaBoardGateway.compose(vestaboardMessage);
        SetMessageCharactersRequest setMessageRequest = new SetMessageCharactersRequest();
        setMessageRequest.setCharacters(composedMessage);

        GetCurrentMessageResponse currentMessage = vestaBoardGateway.getCurrentMessage();
        String composeMessageString = Arrays.deepToString(composedMessage).replaceAll("\\s", "");
        if(!currentMessage.getCurrentMessage().getLayout().equals(composeMessageString)) {
            SetMessageResponse response = vestaBoardGateway.setMessage(setMessageRequest);

            return response.getStatus();
        }

        return "no change";
    }
}
