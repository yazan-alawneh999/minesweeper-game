package com.kotlearn.minesweeperk.domain.settings

/**
 * A player-selectable mine icon. Emoji variants carry an [emoji] string;
 * vector variants leave it null and render from a Compose vector/drawable.
 */
enum class MineIcon(val emoji: String?) {
    BOMB(emoji = "💣"),      // 💣
    EXPLOSION(emoji = "💥"), // 💥
    SKULL(emoji = "☠️"),     // ☠️
    CLASSIC_DOT(emoji = null),
    SPIKED(emoji = null);

    companion object {
        // Matches the mine shown before this feature (the bundled spiked drawable).
        val DEFAULT = SPIKED
        fun fromName(name: String?): MineIcon =
            entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}
