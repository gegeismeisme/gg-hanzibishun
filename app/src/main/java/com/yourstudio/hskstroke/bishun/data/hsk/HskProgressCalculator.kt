package com.yourstudio.hskstroke.bishun.data.hsk

object HskProgressCalculator {
    fun calculateSummary(completed: Set<String>, catalog: Map<Int, List<String>>): HskProgressSummary {
        if (catalog.isEmpty()) return HskProgressSummary()

        val completedSet = completed
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toHashSet()

        val perLevel = mutableMapOf<Int, HskLevelSummary>()
        val nextTargets = mutableMapOf<Int, String?>()
        catalog.forEach { (level, symbols) ->
            val done = symbols.count { completedSet.contains(it) }
            perLevel[level] = HskLevelSummary(done, symbols.size)
            nextTargets[level] = symbols.firstOrNull { !completedSet.contains(it) }
        }
        return HskProgressSummary(perLevel, nextTargets)
    }
}

