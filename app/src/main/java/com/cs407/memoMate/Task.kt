// TaskItem.kt
package com.cs407.memoMate

import java.io.Serializable

class Task(var name: String) : Serializable {
    var isChecked: Boolean = false
    var ddl: String = ""
    var note: String = ""
    var importance: Int = 3  // Default importance set to 3 (Green)
}
