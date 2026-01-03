package com.yourstudio.hskstroke.bishun.ui.character

sealed interface PracticeStatus {
    data object None : PracticeStatus
    data class StartFromStroke(val strokeNumber: Int) : PracticeStatus
    data class TryAgain(val mistakes: Int) : PracticeStatus
    data object GreatContinue : PracticeStatus
    data object BackwardsAccepted : PracticeStatus
    data object Complete : PracticeStatus
}

