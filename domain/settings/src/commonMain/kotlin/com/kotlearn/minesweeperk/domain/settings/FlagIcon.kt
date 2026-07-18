package com.kotlearn.minesweeperk.domain.settings

/**
 * A player-selectable flag icon. Emoji variants carry an [emoji] string;
 * vector variants leave it null and are rendered from a Compose vector in the
 * UI layer (keeps this module free of Compose types).
 */
enum class FlagIcon(val emoji: String?) {
    RED_FLAG(emoji = "🚩"),   // 🚩
    BLACK_FLAG(emoji = "🏴"), // 🏴
    PIN(emoji = "📍"),        // 📍
    MATERIAL_FILLED(emoji = null),
    MATERIAL_OUTLINED(emoji = null);

    companion object {
        // Matches the flag shown before this feature (Material filled flag).
        val DEFAULT = MATERIAL_FILLED
        fun fromName(name: String?): FlagIcon =
            entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}
