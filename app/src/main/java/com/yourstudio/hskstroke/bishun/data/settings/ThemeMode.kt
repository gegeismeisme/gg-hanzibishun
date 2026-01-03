package com.yourstudio.hskstroke.bishun.data.settings

enum class ThemeMode(val storedValue: Int) {
    System(0),
    Light(1),
    Dark(2),
    ;

    companion object {
        fun fromStoredValue(value: Int?): ThemeMode {
            return entries.firstOrNull { it.storedValue == value } ?: System
        }
    }
}

