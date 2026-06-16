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

    public int[][] compose(SetMessageRequest request) {
        VestaboardMessage vestaboardMessage = messageCreator.create(request.getType(), request.getParams());
        return vestaBoardGateway.compose(vestaboardMessage);
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

}
