package com.kotlearn.minesweeperk.feature.play

import kotlin.test.Test
import kotlin.test.assertEquals

class FormatElapsedTimeTest {

    @Test
    fun formatsZeroAsClock() {
        assertEquals("0:00", formatElapsedTime(0))
    }

    @Test
    fun padsSecondsToTwoDigits() {
        assertEquals("0:07", formatElapsedTime(7))
    }

    @Test
    fun formatsMinutesAndSeconds() {
        assertEquals("1:05", formatElapsedTime(65))
    }

    @Test
    fun countsMinutesBeyondTen() {
        assertEquals("12:09", formatElapsedTime(729))
    }
}
