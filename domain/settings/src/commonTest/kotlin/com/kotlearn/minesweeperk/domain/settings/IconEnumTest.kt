package com.kotlearn.minesweeperk.domain.settings

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertNotNull

class IconEnumTest {

    @Test
    fun flag_fromName_falls_back_to_default_when_unknown() {
        assertEquals(FlagIcon.DEFAULT, FlagIcon.fromName("nope"))
        assertEquals(FlagIcon.DEFAULT, FlagIcon.fromName(null))
    }

    @Test
    fun mine_fromName_parses_known_value() {
        assertEquals(MineIcon.SKULL, MineIcon.fromName("SKULL"))
    }

    @Test
    fun emoji_icons_carry_an_emoji_vector_icons_do_not() {
        assertNotNull(FlagIcon.RED_FLAG.emoji)
        assertNull(FlagIcon.MATERIAL_FILLED.emoji)
        assertNotNull(MineIcon.BOMB.emoji)
        assertNull(MineIcon.CLASSIC_DOT.emoji)
    }
}
