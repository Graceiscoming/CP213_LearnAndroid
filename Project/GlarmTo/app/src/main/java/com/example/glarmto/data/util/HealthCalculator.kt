package com.example.glarmto.data.util

import kotlin.math.roundToInt

object HealthCalculator {
    /**
     * Calculates Total Daily Energy Expenditure (TDEE) based on Mifflin-St Jeor formula
     * and assumes a moderate activity level (multiplier of 1.55).
     * 
     * @param age User's age in years
     * @param weight User's weight in kilograms
     * @param height User's height in centimeters
     * @param isMale True if male, false if female
     * @return The daily calorie goal (maintenance TDEE) rounded to the nearest integer.
     */
    fun calculateTdee(age: Int, weight: Double, height: Double, isMale: Boolean): Int {
        if (age <= 0 || weight <= 0.0 || height <= 0.0) return 2000 // Default fallback

        // Mifflin-St Jeor Equation for BMR
        val bmr = if (isMale) {
            (10 * weight) + (6.25 * height) - (5 * age) + 5
        } else {
            (10 * weight) + (6.25 * height) - (5 * age) - 161
        }
        
        // Multiplier for moderate activity (1.55)
        return (bmr * 1.55).roundToInt()
    }
}
