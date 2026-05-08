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

        return currentMessage.getCurrentMessage().getLayout();
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
