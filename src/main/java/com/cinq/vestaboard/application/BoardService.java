package com.cinq.vestaboard.application;

import com.cinq.vestaboard.infrastructure.api.vestaboard.MessageCreator;
import com.cinq.vestaboard.infrastructure.api.vestaboard.VestaBoardGateway;
import com.cinq.vestaboard.infrastructure.api.vestaboard.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final VestaBoardGateway vestaBoardGateway;
    private final MessageCreator messageCreator;

    public String getCurrentMessage() {
        return vestaBoardGateway.getCurrentMessage().getCurrentMessage().getLayout();
    }

    public String setMessage(SetMessageRequest request, boolean force) {
        VestaboardMessage vestaboardMessage = messageCreator.create(request.getType(), request.getParams());
        int[][] composedMessage = vestaBoardGateway.compose(vestaboardMessage);

        if (!force) {
            String currentLayout = vestaBoardGateway.getCurrentMessage().getCurrentMessage().getLayout();
            String newLayout = Arrays.deepToString(composedMessage).replaceAll("\\s", "");
            if (currentLayout.equals(newLayout)) {
                return "no change";
            }
        }

        SetMessageCharactersRequest setMessageRequest = new SetMessageCharactersRequest();
        setMessageRequest.setCharacters(composedMessage);
        return vestaBoardGateway.setMessage(setMessageRequest).getStatus();
    }
}
