package com.yas.system.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class StringUtilsTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("cleanHtmlToPlainText returns empty string for null or blank input")
    void cleanHtmlToPlainText_blankInput_returnsEmpty(String input) {
        assertThat(StringUtils.cleanHtmlToPlainText(input)).isEmpty();
        assertThat(StringUtils.cleanHtmlToPlainText(input, 100)).isEmpty();
    }

    @Test
    @DisplayName("cleanHtmlToPlainText converts HTML tags, decodes entities, and collapses spaces")
    void cleanHtmlToPlainText_htmlWithEntities_convertsCleanly() {
        String html = "<p>Pro&nbsp;laptop&nbsp;powerhouse&nbsp;featuring&nbsp;M3&nbsp;Pro&nbsp;chip,"
                + "&nbsp;Liquid&nbsp;Retina&nbsp;XDR&nbsp;display,&nbsp;advanced&nbsp;connectivity,"
                + "&nbsp;and&nbsp;extreme&nbsp;battery&nbsp;life.</p>";

        String expected = "Pro laptop powerhouse featuring M3 Pro chip, Liquid Retina XDR display, advanced connectivity, and extreme battery life.";

        String result = StringUtils.cleanHtmlToPlainText(html);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("cleanHtmlToPlainText handles multiple tags and decodes common entities")
    void cleanHtmlToPlainText_complexHtml_convertsAccurately() {
        String html = "<div><h1>Title</h1><p>Line 1<br/>Line 2 &amp; &quot;quote&quot; &lt;3</p><ul><li>Item A</li><li>Item B</li></ul></div>";

        String result = StringUtils.cleanHtmlToPlainText(html);
        assertThat(result).isEqualTo("Title Line 1 Line 2 & \"quote\" <3 Item A Item B");
    }

    @Test
    @DisplayName("cleanHtmlToPlainText truncates correctly when maxLength is specified")
    void cleanHtmlToPlainText_withMaxLength_truncatesCorrectly() {
        String html = "<p>Hello&nbsp;World&nbsp;from&nbsp;Spring&nbsp;Boot!</p>";

        String result = StringUtils.cleanHtmlToPlainText(html, 11);
        assertThat(result).isEqualTo("Hello World");
    }
}
