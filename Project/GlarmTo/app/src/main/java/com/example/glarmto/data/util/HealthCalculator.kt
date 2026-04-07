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
    fun calculateTdee(age: Int, weight: Double, height: Double, isMale: Boolean, workoutDays: Int = 3, goal: String = "Maintain"): Int {
        if (age <= 0 || weight <= 0.0 || height <= 0.0) return 2000 // Default fallback

        // Mifflin-St Jeor Equation for BMR
        val bmr = if (isMale) {
            (10 * weight) + (6.25 * height) - (5 * age) + 5
        } else {
            (10 * weight) + (6.25 * height) - (5 * age) - 161
        }
        
        val activityMultiplier = when {
            workoutDays <= 1 -> 1.2
            workoutDays <= 3 -> 1.375
            workoutDays <= 5 -> 1.55
            else -> 1.725
        }
        
        val tdee = (bmr * activityMultiplier).roundToInt()
        
        return when (goal) {
            "Cut" -> (tdee - 400).coerceAtLeast(1200)
            "Bulk" -> tdee + 400
            else -> tdee
        }
    }
}
