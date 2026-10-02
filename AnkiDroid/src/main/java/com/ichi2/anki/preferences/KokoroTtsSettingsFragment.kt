// SPDX-License-Identifier: GPL-3.0-or-later

package com.ichi2.anki.preferences

import com.ichi2.anki.R

/** OpenRouter Kokoro voice used when adding a note. */
class KokoroTtsSettingsFragment : SettingsFragment() {
    override val preferenceResource: Int
        get() = R.xml.preferences_kokoro_tts
    override val analyticsScreenNameConstant: String
        get() = "prefs.kokoro_tts"

    override fun initSubscreen() = Unit
}
