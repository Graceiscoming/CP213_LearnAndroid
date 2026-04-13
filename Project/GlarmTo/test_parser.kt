import java.util.Locale

object ExercisePresets {
    val categories = mapOf(
        "Chest" to listOf("Bench Press", "Incline DB Press", "Chest Fly", "Pushups", "Dips"),
        "Back" to listOf("Deadlift", "Lat Pulldown", "Bent Over Row", "Pullups", "Seated Cable Row"),
        "Legs" to listOf("Squat", "Leg Press", "Leg Extension", "Leg Curl", "Lunges"),
        "Shoulders" to listOf("Overhead Press", "Lateral Raise", "Front Raise", "Face Pulls"),
        "Arms" to listOf("Bicep Curl", "Tricep Pushdown", "Hammer Curl", "Skull Crusher")
    )
    val allExercises = categories.values.flatten().sorted()
}

fun parseVoiceCommand(command: String): Triple<String, String, String>? {
    val lowerCommand = command.lowercase(Locale.getDefault()).trim()
    val numberRegex = Regex("""(\d+(?:\.\d+)?)""")
    val matches = numberRegex.findAll(lowerCommand).toList()
    if (matches.isEmpty()) return null

    var weight = ""
    var reps = ""
    val weightWords = listOf("kg", "kilo", "kilos", "กิโล", "โล", "ปอนด์", "lbs", "kg.")
    val repWords = listOf("reps", "rep", "ครั้ง", "ที", "sets", "เซต")

    val explicitWeights = mutableListOf<String>()
    val explicitReps = mutableListOf<String>()
    val unassignedNumbers = mutableListOf<String>()

    for (match in matches) {
        val numStr = match.value
        val numIndex = match.range.last
        val textAfter = lowerCommand.substring(numIndex + 1).take(15).trim()
        val textBeforeStr = lowerCommand.substring(0, match.range.first).takeLast(15).trim()
        val nextWord = textAfter.split(Regex("""\s+""")).firstOrNull()?.replace(Regex("[^a-zก-๙]"), "") ?: ""

        val isWeightUnit = weightWords.any { nextWord.startsWith(it) }
        val isRepUnit = repWords.any { nextWord.startsWith(it) }
        val attachedWeight = weightWords.any { textAfter.startsWith(it) }
        val attachedRep = repWords.any { textAfter.startsWith(it) }

        if (isWeightUnit || attachedWeight) {
            explicitWeights.add(numStr)
        } else if (isRepUnit || attachedRep) {
            explicitReps.add(numStr)
        } else {
            unassignedNumbers.add(numStr)
        }
    }

    if (matches.size == 1) {
        weight = explicitWeights.firstOrNull() ?: "0"
        reps = explicitReps.firstOrNull() ?: unassignedNumbers.firstOrNull() ?: matches.first().value
        if (explicitWeights.isNotEmpty() && explicitReps.isEmpty()) reps = "1"
    } else {
        weight = explicitWeights.firstOrNull() ?: ""
        reps = explicitReps.firstOrNull() ?: ""

        if (weight.isEmpty() && unassignedNumbers.isNotEmpty()) {
            val candidate = unassignedNumbers.removeAt(0)
            if (reps.isEmpty() && unassignedNumbers.isNotEmpty()) {
                val cand2 = unassignedNumbers.removeAt(0)
                val f1 = candidate.toFloatOrNull() ?: 0f
                val f2 = cand2.toFloatOrNull() ?: 0f
                if (f1 > f2) {
                    weight = candidate; reps = cand2
                } else {
                    weight = cand2; reps = candidate
                }
            } else {
                weight = candidate 
            }
        }
        if (reps.isEmpty() && unassignedNumbers.isNotEmpty()) {
            reps = unassignedNumbers.removeAt(0)
        }
    }
    
    if (weight.isEmpty()) weight = "0"
    if (reps.isEmpty()) reps = "1"
    reps = reps.substringBefore(".")

    var textOnly = lowerCommand.replace(numberRegex, " ")
    val allUnits = weightWords + repWords + listOf("for", "with", "ทำ", "เล่น", "น้ำหนัก")
    allUnits.forEach { unit ->
        textOnly = textOnly.replace(Regex("\\b$unit\\b", RegexOption.IGNORE_CASE), " ")
    }
    textOnly = textOnly.replace(Regex("\\s+"), " ").trim()

    var finalExercise = textOnly
    val extractedWords = textOnly.split(" ").filter { it.length > 2 }
    
    if (textOnly.isNotBlank()) {
        val bestMatch = ExercisePresets.allExercises.maxByOrNull { preset ->
            val pLower = preset.lowercase(Locale.getDefault())
            if (pLower == textOnly) 1000
            else if (pLower.contains(textOnly) || textOnly.contains(pLower)) 500
            else {
                val presetWords = pLower.split(" ")
                extractedWords.count { presetWords.contains(it) } * 10
            }
        }
        
        val pLower = bestMatch?.lowercase(Locale.getDefault()) ?: ""
        if (bestMatch != null && (pLower.contains(textOnly) || textOnly.contains(pLower) || extractedWords.any { pLower.contains(it) })) {
            finalExercise = bestMatch
        } else {
            finalExercise = textOnly.split(" ").joinToString(" ") { 
                if (it.isNotEmpty()) it.replaceFirstChar { char -> char.uppercase() } else ""
            }
        }
    } else {
        return null
    }

    return Triple(finalExercise, weight, reps)
}

fun main() {
    println("1: " + parseVoiceCommand("  Overhead Shoulder Press  40.25   kg  12 reps  "))
    println("2: " + parseVoiceCommand("100 kg 8 reps for Squat"))
    println("3: " + parseVoiceCommand("Pull up 15 ครั้ง"))
    println("4: " + parseVoiceCommand("สควอท 100 กิโล 8 ครั้ง"))
}
