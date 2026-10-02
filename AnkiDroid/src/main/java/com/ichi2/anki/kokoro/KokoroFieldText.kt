// SPDX-License-Identifier: GPL-3.0-or-later

package com.ichi2.anki.kokoro

import com.ichi2.anki.backend.stripHTML
import com.ichi2.anki.libanki.SOUND_RE

/** Text sent to Kokoro and the `[sound:]` tag written back onto the first field. */
object KokoroFieldText {
    fun hasSound(field: String): Boolean = SOUND_RE.containsMatchIn(field)

    fun speechText(field: String): String? {
        val withoutSound = SOUND_RE.replace(field, " ")
        val plain = stripHTML(withoutSound).replace(WHITESPACE, " ").trim()
        return plain.ifEmpty { null }
    }

    fun appendSound(
        field: String,
        filename: String,
    ): String {
        val tag = "[sound:$filename]"
        if (field.contains(tag)) return field
        if (field.isBlank()) return tag
        return if (field.endsWith(" ")) "$field$tag" else "$field $tag"
    }

    private val WHITESPACE = Regex("\\s+")
}
