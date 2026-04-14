Adding Monthly History View
The user wants to be able to choose between viewing their workout history for a specific day or for the entire month when clicking the History icon on the Dashboard.

User Review Required
IMPORTANT

Currently, the HistoryScreen is designed to display logs for one specific day at a time. To support viewing the whole month, I will need to build out a new UI state.

My plan is to list out all workout sessions for the entire month, grouped by date (e.g., scrolling down will show "April 14: 2 sessions", "April 12: 1 session", etc.). Does this format sound good to you, or were you picturing a visual Calendar grid instead?

Proposed Changes
1. DashboardScreen.kt
History Menu: Instead of navigating directly when clicking the Icons.Filled.History button, it will open a small dropdown menu with two options:
"Daily View" (ประวัติรายวัน)
"Monthly View" (ประวัติรายเดือน)
Modify the onNavigateToHistory callback to pass a mode parameter (e.g., onNavigateToHistory(isMonthly: Boolean)).
2. MainActivity.kt (Routing)
Update the navigation route for "history" to accept an optional argument: "history?mode={mode}".
Pass this mode into the HistoryScreen.
3. HistoryViewModel.kt
Add a viewMode flow ("Daily" or "Monthly").
Modify the data fetching logic:
If "Daily", fetch workouts and nutrition for the selectedDate (as it currently does).
If "Monthly", determine the start and end of the month based on selectedDate and fetch all WorkoutSessionEntity and NutritionEntity entries for that entire month.
4. HistoryScreen.kt
Receive the initial mode from the navigation argument.
Adjust the UI based on viewMode:
Daily Mode: Keep the current UI exactly as it is.
Monthly Mode: Group sessions by date and display them in a list. When parsing workouts, it will show a high-level summary of each day (e.g., "3 Workouts, Total Volume: 4000kg") so the screen isn't overwhelmed.
Allow the DatePicker to select the Target Month and Year when in Monthly mode, rather than an exact day.
Open Questions
When viewing the "Monthly" history, do you want it to list out every single set played that month, or just a summary (e.g., Day > Session Name > Total Volume and Time), keeping the details hidden unless clicked? (I recommend hiding the deep details by default to prevent the list from getting too long).