package com.cinq.vestaboard.infrastructure.api.vestaboard;

import com.cinq.vestaboard.domain.MessageType;
import com.cinq.vestaboard.infrastructure.api.vestaboard.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MessageCreator {

    @Autowired
    private ObjectMapper objectMapper;

    private static final int BOARD_WIDTH = 22;
    private static final int CONTENT_WIDTH = 20;
    private static final int BOARD_HEIGHT = 6;
    private static final int CONTENT_HEIGHT = 4;
    private static final int CHAR_LIMIT = CONTENT_WIDTH * CONTENT_HEIGHT;

    public VestaboardMessage create(MessageType type, Map<String, Object> params) {
        return switch (type) {
            case PROGRESS -> {
                String title = requireStringParam(params, "title");
                int current = requireIntParam(params, "current");
                int total = requireIntParam(params, "total");
                if (total <= 0) throw new IllegalArgumentException("Parameter 'total' must be greater than 0");
                if (current < 0 || current > total) throw new IllegalArgumentException("Parameter 'current' must be between 0 and total");
                yield createProgressMessage(title, current, total);
            }
            case CELEBRATION -> {
                String title = requireStringParam(params, "title");
                yield createBorderedMessage(title);
            }
            case SCOREBOARD -> {
                String title = (String) params.get("title");
                List<Object> entryObjects = (List<Object>) params.get("ranks");

                List<ScoreBoardEntry> scoreBoardEntries = new ArrayList<>();
                for (Object entryObject : entryObjects) {
                    ScoreBoardEntry scoreBoardEntry = objectMapper.convertValue(entryObject, ScoreBoardEntry.class);

                    String name = scoreBoardEntry.getName();
                    String normalizesName = Normalizer.normalize(name, Normalizer.Form.NFD);
                    String sanitizedName = normalizesName.replaceAll("[^\\p{ASCII}]", "");
                    scoreBoardEntry.setName(sanitizedName);

                    scoreBoardEntries.add(scoreBoardEntry);
                }

                yield createScoreBoardMessage(title, scoreBoardEntries);
            }
        };
    }

    private String requireStringParam(Map<String, Object> params, String key) {
        Object value = params == null ? null : params.get(key);
        if (value == null) throw new IllegalArgumentException("Missing required parameter: '" + key + "'");
        String str = value.toString();
        if (str.isBlank()) throw new IllegalArgumentException("Parameter '" + key + "' must not be blank");
        return str;
    }

    private int requireIntParam(Map<String, Object> params, String key) {
        Object value = params == null ? null : params.get(key);
        if (value == null) throw new IllegalArgumentException("Missing required parameter: '" + key + "'");
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Parameter '" + key + "' must be a valid integer");
        }
    }

    private VestaboardMessage createScoreBoardMessage(String title, List<ScoreBoardEntry> scoreBoardEntries) {
        VestaboardStyle messageComponentStyle = new VestaboardStyle();
        messageComponentStyle.setHeight(1);
        messageComponentStyle.setWidth(BOARD_WIDTH);
        messageComponentStyle.setAlign(VestaboardAlign.CENTER);
        messageComponentStyle.setJustify(VestaboardJustify.CENTER);

        if (title.length() > CONTENT_WIDTH) {
            title = title.substring(0, CONTENT_WIDTH - 1);
        }

        VestaboardComponent messageComponent = new VestaboardComponent();
        messageComponent.setTemplate(title);
        messageComponent.setStyle(messageComponentStyle);

        StringBuilder contentBuilder = new StringBuilder();
        boolean alternate = false;
        for (ScoreBoardEntry scoreBoardEntry : scoreBoardEntries) {
            int rankLength = String.valueOf(scoreBoardEntry.getRank()).length() + 2; // Add 2 for spaces between components
            int scoreLength = String.valueOf(scoreBoardEntry.getScore()).length() + 2; // Add 2 for spaces between components
            int nameLength = CONTENT_WIDTH - rankLength - scoreLength;
            StringBuilder builder = new StringBuilder();

            if (scoreBoardEntry.getName().length() > nameLength) {
                scoreBoardEntry.setName(scoreBoardEntry.getName().substring(0, nameLength));
            }

            builder.append(scoreBoardEntry.getName());
            addPadding(builder, nameLength);

            builder.insert(0, " ");
            builder.insert(0, scoreBoardEntry.getRank());
            builder.insert(0, " ");

            builder.append(" ");
            builder.append(scoreBoardEntry.getScore());
            builder.append(" ");

            addBorder(alternate, builder);
            alternate = !alternate;

            contentBuilder.append(builder);
        }

        if(scoreBoardEntries.size() < BOARD_HEIGHT) {
            int emptyRows = BOARD_HEIGHT - scoreBoardEntries.size();

            for (int i = 0; i < emptyRows; i++) {
                StringBuilder emptyLineBuilder = new StringBuilder();
                addEmptyRow(emptyLineBuilder);
                addBorder(alternate, emptyLineBuilder);
                contentBuilder.append(emptyLineBuilder);
                alternate = !alternate;
            }
        }

        VestaboardStyle contentStyle = new VestaboardStyle();
//        contentStyle.setAlign(VestaboardAlign.CENTER);
//        contentStyle.setJustify(VestaboardJustify.CENTER);
        contentStyle.setHeight(BOARD_HEIGHT-1);
        contentStyle.setWidth(BOARD_WIDTH);

        VestaboardComponent contentComponent = new VestaboardComponent();
        contentComponent.setStyle(contentStyle);
        contentComponent.setTemplate(contentBuilder.toString());

        VestaboardMessage vestaboardMessage = new VestaboardMessage();
        vestaboardMessage.setProps(Map.of());
        vestaboardMessage.setComponents(List.of(
                messageComponent,
                contentComponent));

        return vestaboardMessage;
    }

    private void addEmptyRow(StringBuilder builder) {
        for (int i = 0; i < CONTENT_WIDTH; i++) {
            builder.append("{0}");
        }
    }

    private void addBorder(boolean alternate, StringBuilder builder) {
        if(alternate) {
            log.info("orange");
            builder.insert(0, "{64}");
            builder.append("{64}");
        } else {
            log.info("blue");
            builder.insert(0, "{67}");
            builder.append("{67}");
        }
    }

    private VestaboardMessage createProgressMessage(String title, int current, int total) {
        VestaboardStyle messageComponentStyle = new VestaboardStyle();
        messageComponentStyle.setAlign(VestaboardAlign.CENTER);
        messageComponentStyle.setJustify(VestaboardJustify.CENTER);
        messageComponentStyle.setHeight(BOARD_HEIGHT/2);
        messageComponentStyle.setWidth(BOARD_WIDTH);

        VestaboardComponent messageComponent = new VestaboardComponent();
        messageComponent.setTemplate(title);
        messageComponent.setStyle(messageComponentStyle);

        VestaboardStyle percentageComponentStyle = new VestaboardStyle();
        percentageComponentStyle.setAlign(VestaboardAlign.CENTER);
        percentageComponentStyle.setJustify(VestaboardJustify.CENTER);
        percentageComponentStyle.setHeight(BOARD_HEIGHT/2);
        percentageComponentStyle.setWidth(BOARD_WIDTH);

        double percentage = (double) current / (double) total * 100;
        // Make percentage fit on board
        int dividePercentage = 100 / CONTENT_WIDTH;
        double progress = Math.round(percentage / dividePercentage);
        String progressBarText = Math.round(percentage) + "%";
        StringBuilder progressBarBuilder = new StringBuilder();
        int barSize = CONTENT_WIDTH - progressBarText.length();
        int middle = barSize / 2;
        for(int x = 0; x < barSize+1; x++) {
            if(x < middle) {
                if(x < progress) {
                    progressBarBuilder.append("{64}");
                } else {
                    progressBarBuilder.append("{67}");
                }
            } else if(x > middle) {
                if(x < (progress - progressBarText.length())) {
                    progressBarBuilder.append("{64}");
                } else {
                    progressBarBuilder.append("{67}");
                }
            } else {
                progressBarBuilder.append(progressBarText);
            }
        }
        String progressBar = progressBarBuilder.toString();

        VestaboardComponent percentageComponent = new VestaboardComponent();
        percentageComponent.setTemplate(progressBar);
        percentageComponent.setStyle(percentageComponentStyle);

        VestaboardMessage vestaboardMessage = new VestaboardMessage();
        vestaboardMessage.setProps(Map.of("current", String.valueOf(current), "total", String.valueOf(total)));
        vestaboardMessage.setComponents(List.of(
                messageComponent,
                percentageComponent));

        return vestaboardMessage;
    }

    private VestaboardMessage createBorderedMessage(String text) {
        VestaboardStyle messageComponentStyle = new VestaboardStyle();
        messageComponentStyle.setAlign(VestaboardAlign.CENTER);
        messageComponentStyle.setJustify(VestaboardJustify.CENTER);

        text = trimTextToBoardSize(text);
        List<String> lines = splitTextIntoLines(text);
        addEmptyLines(lines);
        String template = addBorder(lines);

        VestaboardComponent messageComponent = new VestaboardComponent();
        messageComponent.setTemplate(template);
        messageComponent.setStyle(messageComponentStyle);

        VestaboardMessage vestaboardMessage = new VestaboardMessage();
        vestaboardMessage.setComponents(List.of(messageComponent));
        vestaboardMessage.setProps(Map.of("text", text));

        return vestaboardMessage;
    }

    private String trimTextToBoardSize(String text) {
        // If more text then space on board remove all words that won't fit.
        if(text.length() > CHAR_LIMIT) {
            text = text.trim();
            text = text.substring(0, text.lastIndexOf(' '));
        }
        return text;
    }

    private List<String> splitTextIntoLines(String text) {
        // Split text into lines
        List<String> words = Arrays.stream(text.split("\\s")).toList();
        List<String> lines = new ArrayList<>();
        StringBuilder lineBuilder = new StringBuilder();

        for(int i = 0; i < words.size(); i++) {
            String word = words.get(i);
            if(word.length() + lineBuilder.length() < CONTENT_WIDTH) {
                addWord(lineBuilder, word);

                if(i == words.size() - 1) {
                    addPadding(lineBuilder, CONTENT_WIDTH);
                }
            } else {
                addPadding(lineBuilder, CONTENT_WIDTH);
                lines.add(lineBuilder.toString());
                lineBuilder = new StringBuilder();
                addWord(lineBuilder, word);

                if(i == words.size() - 1) {
                    addPadding(lineBuilder, CONTENT_WIDTH);
                }
            }
        }
        lines.add(lineBuilder.toString());
        return lines;
    }

    private void addEmptyLines(List<String> lines) {
        StringBuilder lineBuilder;
        if(lines.size() < CONTENT_HEIGHT) {
            int emptyLines = CONTENT_HEIGHT - lines.size();
            for(int line = 0; line < emptyLines; line++) {
                lineBuilder = new StringBuilder();
                lineBuilder.append("{0}".repeat(CONTENT_WIDTH));

                if(line %2 == 0) {
                    lines.add(lineBuilder.toString());
                } else {
                    lines.addFirst(lineBuilder.toString());
                }
            }
        }
    }

    private String addBorder(List<String> lines) {
        String horizontalBorder1 = "{67}{64}{67}{64}{67}{64}{67}{64}{67}{64}{67}{64}{67}{64}{67}{64}{67}{64}{67}{64}{67}{64}";
        String horizontalBorder2 = "{64}{67}{64}{67}{64}{67}{64}{67}{64}{67}{64}{67}{64}{67}{64}{67}{64}{67}{64}{67}{64}{67}";
        StringBuilder stringBuilder = new StringBuilder();
        boolean alternate = true;

        stringBuilder.append(horizontalBorder1);
        for (String line : lines) {
            String lineWithBorder = addVerticalBorder(line, alternate);
            stringBuilder.append(lineWithBorder);
            alternate = !alternate;
        }
        stringBuilder.append(horizontalBorder2);

        return stringBuilder.toString();
    }

    private void addPadding(StringBuilder stringBuilder, int maxWidth) {
        int padding = maxWidth - stringBuilder.length();

        for(int i = 0; i < padding; i++) {
            if(i%2 == 0) {
                stringBuilder.insert(0, "{0}");
            } else {
                stringBuilder.append("{0}");
            }
        }
    }

    private String addVerticalBorder(String line, boolean alternate) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(line);
        if(alternate) {
            stringBuilder.insert(0, "{64}");
            stringBuilder.append("{67}");
        } else {
            stringBuilder.insert(0, "{67}");
            stringBuilder.append("{64}");
        }

        return stringBuilder.toString();
    }

    private void addWord(StringBuilder stringBuilder, String word) {
        if(!stringBuilder.isEmpty()) {
            stringBuilder.append(" ");
        }

        if(word.length() + stringBuilder.length() < CHAR_LIMIT) {
            stringBuilder.append(word);
        }
    }
}
