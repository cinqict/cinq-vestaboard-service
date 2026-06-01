package com.cinq.vestaboard.infrastructure.api.vestaboard;

import com.cinq.vestaboard.domain.MessageType;
import com.cinq.vestaboard.infrastructure.api.vestaboard.dto.VestaboardMessage;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MessageCreatorTest {

    private final MessageCreator messageCreator = new MessageCreator();

    // --- PROGRESS ---

    @Test
    void progress_returnsTwoComponents() {
        VestaboardMessage result = messageCreator.create(MessageType.PROGRESS, Map.of("title", "Sprint 12", "current", 5, "total", 10));
        assertThat(result.getComponents()).hasSize(2);
    }

    @Test
    void progress_firstComponentIsTitle() {
        VestaboardMessage result = messageCreator.create(MessageType.PROGRESS, Map.of("title", "Sprint 12", "current", 5, "total", 10));
        assertThat(result.getComponents().get(0).getTemplate()).isEqualTo("Sprint 12");
    }

    @Test
    void progress_secondComponentContainsPercentage() {
        VestaboardMessage result = messageCreator.create(MessageType.PROGRESS, Map.of("title", "Sprint 12", "current", 5, "total", 10));
        assertThat(result.getComponents().get(1).getTemplate()).contains("50%");
    }

    @Test
    void progress_atZeroPercent() {
        VestaboardMessage result = messageCreator.create(MessageType.PROGRESS, Map.of("title", "T", "current", 0, "total", 100));
        assertThat(result.getComponents().get(1).getTemplate()).contains("0%");
    }

    @Test
    void progress_atHundredPercent() {
        VestaboardMessage result = messageCreator.create(MessageType.PROGRESS, Map.of("title", "T", "current", 100, "total", 100));
        assertThat(result.getComponents().get(1).getTemplate()).contains("100%");
    }

    @Test
    void progress_missingTitle_throwsException() {
        assertThatThrownBy(() -> messageCreator.create(MessageType.PROGRESS, Map.of("current", 5, "total", 10)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("title");
    }

    @Test
    void progress_missingCurrent_throwsException() {
        assertThatThrownBy(() -> messageCreator.create(MessageType.PROGRESS, Map.of("title", "T", "total", 10)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("current");
    }

    @Test
    void progress_missingTotal_throwsException() {
        assertThatThrownBy(() -> messageCreator.create(MessageType.PROGRESS, Map.of("title", "T", "current", 5)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("total");
    }

    @Test
    void progress_totalIsZero_throwsException() {
        assertThatThrownBy(() -> messageCreator.create(MessageType.PROGRESS, Map.of("title", "T", "current", 0, "total", 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("total");
    }

    @Test
    void progress_currentExceedsTotal_throwsException() {
        assertThatThrownBy(() -> messageCreator.create(MessageType.PROGRESS, Map.of("title", "T", "current", 11, "total", 10)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("current");
    }

    @Test
    void progress_invalidCurrentFormat_throwsException() {
        assertThatThrownBy(() -> messageCreator.create(MessageType.PROGRESS, Map.of("title", "T", "current", "abc", "total", 10)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("current");
    }

    // --- CELEBRATION ---

    @Test
    void celebration_returnsOneComponent() {
        VestaboardMessage result = messageCreator.create(MessageType.CELEBRATION, Map.of("title", "Great job!"));
        assertThat(result.getComponents()).hasSize(1);
    }

    @Test
    void celebration_templateIsNotBlank() {
        VestaboardMessage result = messageCreator.create(MessageType.CELEBRATION, Map.of("title", "Great job!"));
        assertThat(result.getComponents().get(0).getTemplate()).isNotBlank();
    }

    @Test
    void celebration_longTextIsTrimmedToFitBoard() {
        // 81 chars — one word over the 80-character board limit (20 cols × 4 rows)
        String longText = "This is a test string that is just slightly over the eighty character board limit";
        VestaboardMessage result = messageCreator.create(MessageType.CELEBRATION, Map.of("title", longText));
        assertThat(result.getProps().get("text").length()).isLessThan(longText.length());
    }

    @Test
    void celebration_missingTitle_throwsException() {
        assertThatThrownBy(() -> messageCreator.create(MessageType.CELEBRATION, Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("title");
    }

    @Test
    void celebration_blankTitle_throwsException() {
        assertThatThrownBy(() -> messageCreator.create(MessageType.CELEBRATION, Map.of("title", "   ")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("title");
    }
}
