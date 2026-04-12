import os

def fix_login(file_path):
    with open(file_path, "r", encoding="utf-8") as f:
        code = f.read()
    
    target = """    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {"""
    replacement = """    Box(
        modifier = Modifier.fillMaxSize().imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {"""
    code = code.replace(target, replacement, 1)

    code = code.replace("    }\n}\n", "    }\n    }\n}\n")
    
    with open(file_path, "w", encoding="utf-8") as f:
        f.write(code)


def fix_onboarding(file_path):
    with open(file_path, "r", encoding="utf-8") as f:
        code = f.read()
    
    target = """    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {"""
    replacement = """    Box(
        modifier = Modifier.fillMaxSize().imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {"""
    code = code.replace(target, replacement, 1)

    code = code.replace("    }\n}\n", "    }\n    }\n}\n")
    
    with open(file_path, "w", encoding="utf-8") as f:
        f.write(code)


def fix_workout(file_path):
    with open(file_path, "r", encoding="utf-8") as f:
        code = f.read()

    target1 = """    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {"""
    replacement1 = """    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {"""
    code = code.replace(target1, replacement1, 1)

    target2 = """        Divider()
        val sessions by viewModel.sessionsForDate.collectAsState()

        if (isWorkingOut) {"""
    replacement2 = """        Divider()
        val sessions by viewModel.sessionsForDate.collectAsState()
            }
        }

        if (isWorkingOut) {"""
    code = code.replace(target2, replacement2, 1)

    target3 = """            Text("Current Session Sets:", fontWeight = FontWeight.SemiBold)
            LazyColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(workouts) { workout ->"""
    replacement3 = """            item {
                Text("Current Session Sets:", fontWeight = FontWeight.SemiBold)
            }
            items(workouts) { workout ->"""
    code = code.replace(target3, replacement3, 1)
    
    target_rm1 = """                }
            }
        } else {"""
    repl_rm1 = """                }
        } else {"""
    code = code.replace(target_rm1, repl_rm1, 1)


    target4 = """            Text("Logged Sessions:", fontWeight = FontWeight.SemiBold)
            
            LazyColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Show Sessions
                items(sessions) { session ->"""
    replacement4 = """            item {
                Text("Logged Sessions:", fontWeight = FontWeight.SemiBold)
            }
            // Show Sessions
            items(sessions) { session ->"""
    code = code.replace(target4, replacement4, 1)

    target_rm2 = """                            }
                        }
                    }
                }
            }
        }
    }

    if (showRoutineDialog && customRoutines.isNotEmpty()) {"""
    repl_rm2 = """                            }
                        }
                    }
                }
            }
        }

    if (showRoutineDialog && customRoutines.isNotEmpty()) {"""
    code = code.replace(target_rm2, repl_rm2, 1)

    with open(file_path, "w", encoding="utf-8") as f:
        f.write(code)

def fix_nutrition(file_path):
    with open(file_path, "r", encoding="utf-8") as f:
        code = f.read()

    target1 = """    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {"""
    replacement1 = """    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {"""
    code = code.replace(target1, replacement1, 1)

    target2 = """        Divider()

        // List of eaten foods today
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(nutritions) { item ->"""
    replacement2 = """        Divider()
            }
        }

        // List of eaten foods today
        items(nutritions) { item ->"""
    code = code.replace(target2, replacement2, 1)

    target_rm1 = """            }
        }
    }
}"""
    repl_rm1 = """            }
        }
}"""
    code = code.replace(target_rm1, repl_rm1, 1)

    with open(file_path, "w", encoding="utf-8") as f:
        f.write(code)

def fix_simple(file_path, target, repl):
    with open(file_path, "r", encoding="utf-8") as f:
        code = f.read()
    code = code.replace(target, repl, 1)
    with open(file_path, "w", encoding="utf-8") as f:
        f.write(code)

os.makedirs('.gemini/scripts', exist_ok=True)
base = "app/src/main/java/com/example/glarmto/ui/"
fix_login(base + "login/LoginScreen.kt")
fix_onboarding(base + "onboarding/OnboardingScreen.kt")
fix_workout(base + "workout/WorkoutScreen.kt")
fix_nutrition(base + "nutrition/NutritionScreen.kt")

fix_simple(base + "calculator/CalculatorScreen.kt",
'''    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {''',
'''    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {''')

fix_simple(base + "routines/RoutinesScreen.kt",
'''        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),''',
'''        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
                .imePadding(),''')

print("Refactoring complete.")
