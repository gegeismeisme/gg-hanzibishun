package com.yourstudio.hskstroke.bishun.data.hsk

object HskCourseCatalogBuilder {
    fun build(entries: Collection<HskEntry>): Map<Int, List<String>> {
        if (entries.isEmpty()) return emptyMap()

        return entries
            .asSequence()
            .mapNotNull { entry ->
                val symbol = entry.symbol.trim()
                if (symbol.isBlank()) return@mapNotNull null
                entry.copy(symbol = symbol)
            }
            .groupBy { it.level }
            .mapValues { (_, items) ->
                items
                    .sortedBy { it.writingLevel ?: Int.MAX_VALUE }
                    .map { it.symbol }
            }
    }
}

