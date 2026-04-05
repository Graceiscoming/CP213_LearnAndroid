package com.example.glarmto

import com.example.glarmto.data.util.HealthCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Consolidated Unit Test for Login, Profile calculation and Health logic.
 */
class LoginSetupTest {

    @Test
    fun `test male TDEE calculation is correct`() {
        // Given: 25 years old male, 70kg, 175cm
        val age = 25
        val weight = 70.0
        val height = 175.0
        val isMale = true

        // BMR = (10 * 70) + (6.25 * 175) - (5 * 25) + 5
        // BMR = 700 + 1093.75 - 125 + 5 = 1673.75
        // TDEE = 1673.75 * 1.55 = 2594.3125 (Rounded to 2594)
        val expectedTdee = 2594

        val actualTdee = HealthCalculator.calculateTdee(age, weight, height, isMale)
        
        assertEquals("Male TDEE calculation failed", expectedTdee, actualTdee)
    }

    @Test
    fun `test female TDEE calculation is correct`() {
        // Given: 30 years old female, 60kg, 160cm
        val age = 30
        val weight = 60.0
        val height = 160.0
        val isMale = false

        // BMR = (10 * 60) + (6.25 * 160) - (5 * 30) - 161
        // BMR = 600 + 1000 - 150 - 161 = 1289
        // TDEE = 1289 * 1.55 = 1997.95 (Rounded to 1998)
        val expectedTdee = 1998

        val actualTdee = HealthCalculator.calculateTdee(age, weight, height, isMale)
        
        assertEquals("Female TDEE calculation failed", expectedTdee, actualTdee)
    }

    @Test
    fun `test calculation with invalid inputs returns default`() {
        // Given invalid inputs, it should return a safe default (e.g., 2000)
        assertEquals(2000, HealthCalculator.calculateTdee(0, 70.0, 175.0, true))
        assertEquals(2000, HealthCalculator.calculateTdee(25, -10.0, 175.0, true))
        assertEquals(2000, HealthCalculator.calculateTdee(25, 70.0, 0.0, true))
    }
}
