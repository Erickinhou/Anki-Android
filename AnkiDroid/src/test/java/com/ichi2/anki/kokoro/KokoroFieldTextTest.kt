// SPDX-License-Identifier: GPL-3.0-or-later

package com.ichi2.anki.kokoro

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class KokoroFieldTextTest {
    @Test
    fun hasSoundDetectsAnExistingTag() {
        assertThat(KokoroFieldText.hasSound("Hello [sound:old.mp3]"), equalTo(true))
        assertThat(KokoroFieldText.hasSound("Hello"), equalTo(false))
    }

    @Test
    fun speechTextStripsHtmlAndExistingSound() {
        assertThat(
            KokoroFieldText.speechText("<b>Hello</b> [sound:old.mp3]"),
            equalTo("Hello"),
        )
    }

    @Test
    fun speechTextIsNullWhenNothingIsLeftToSpeak() {
        assertThat(KokoroFieldText.speechText("[sound:old.mp3]"), nullValue())
        assertThat(KokoroFieldText.speechText("<br>"), nullValue())
    }

    @Test
    fun appendSoundAddsTagToTheField() {
        assertThat(KokoroFieldText.appendSound("Hello", "a.mp3"), equalTo("Hello [sound:a.mp3]"))
        assertThat(KokoroFieldText.appendSound("", "a.mp3"), equalTo("[sound:a.mp3]"))
        assertThat(
            KokoroFieldText.appendSound("Hello [sound:a.mp3]", "a.mp3"),
            equalTo("Hello [sound:a.mp3]"),
        )
    }
}
