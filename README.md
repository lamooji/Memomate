# MemoMate - Task Management Application

## Overview

MemoMate is a comprehensive task management Android application designed to help users organize and
prioritize their tasks effectively. It solves the problem of scattered schedules and forgetfulness
by providing an all-in-one, streamlined solution. By integrating multiple views—such as the Main
List, Calendar, and 4 Quadrants of Time Management Matrix—along with intelligent features like
AI-driven suggestions, tMemoMate helps users stay on track, enhance productivity, and reduce stress.

## Key Benefits

- **Enhanced Organization:** Consolidates all tasks, deadlines, and priorities into one intuitive
  platform.
- **Improved Productivity:** Encourages effective task prioritization through multiple views and a
  powerful matrix system.
- **Smart Assistance:** Integrates with AI (including ChatGPT) to provide task suggestions, quick
  note-taking, and intelligent scheduling recommendations.
- **Stress Reduction:** Delivers a clear overview of commitments, helping users remain accountable
  and focused.

## Features

### Task Management

- Create, edit, and delete tasks
- Set task priorities (High/Red, Medium/Yellow, Low/Green)
- Add deadlines and descriptions
- Mark tasks as completed

### Multiple Views

- **Main List View:** Quick overview of all tasks
- **Calendar View:** Visualize tasks by date and sync with schedules
- **Eisenhower Matrix:** Organize tasks by urgency and importance in a matrix layout
- **Time-based Groups:** Group tasks by due date (3 days, 7 days, future) for better planning

### Smart Organization

- Automatic task grouping based on deadlines
- Color-coded priority system for quick identification
- Task filtering and sorting capabilities
- Intuitive date-based navigation

### User Interface

- Clean, modern, and responsive design
- Easy-to-use task creation dialog
- Visual feedback for task status and priority
- Customizable dashboards tailored to user preferences

### AI Integration

- **Smart Suggest:** AI-powered recommendations for scheduling and prioritizing tasks
- **Natural Language Input:** Quickly add tasks by typing or speaking natural language requests
- **Conversational Assistance (ChatGPT API):** Manage tasks through a conversational interface

## Technical Details

### Technologies Used

- **Programming Language:** Kotlin
- **Android Framework:** Android SDK
- **Database:** Room Database (for data persistence)
- **Asynchronous Operations:** Coroutines
- **UI Components:** RecyclerView, View Binding

### Architecture

- **Pattern:** MVVM (Model-View-ViewModel) for separation of concerns
- **Data Layer:** Repository pattern for data management
- **Reactivity:** LiveData for reactive UI updates

### Database Schema

**Task Table:**

- noteId (Primary Key)
- significance (Priority level)
- ddl (Deadline)
- finished (Completion status)
- noteTitle (Task name)
- noteAbstract (Task description)
- importance (Priority rating)

## Installation

1. **Clone the Repository:**
   ```bash
   git clone [https://github.com/lamooji/Memomate.git]
   ```
2. **Open Project:**  
   Open the cloned project in Android Studio.
3. **Build & Run:**  
   Use a connected Android device or an emulator.  
   **Minimum SDK Version:** [Api 34]

## Usage

### Creating a Task

1. Press the "+" button in the main view.
2. Enter the task name and set a deadline.
3. Choose a priority (Red=High, Yellow=Medium, Green=Low).
4. Add an optional description.
5. Click "Add Task" to save.

### Viewing Tasks

- **Main View:** Shows all tasks grouped by their due date.
- **Calendar View:** Accessible from the menu; visualize tasks by date for easier planning.
- **Eisenhower Matrix:** Accessible from the menu; quickly assess tasks by urgency and importance.
- **Task Details:** Tap on any task to edit or view its details.

### Managing Tasks

- **Edit:** Tap the edit icon on any task to modify details.
- **Delete:** Tap the delete icon to remove a task permanently.
- **Complete:** Check the box in task details to mark it as finished.
- **Filter & Sort:** Navigate through calendar or matrix views for focused planning.

## Contributing

Contributions are welcome. Please follow standard GitHub workflows:

1. Fork the repository.
2. Create a new branch for your feature or bugfix.
3. Submit a pull request describing your changes.
