package com.yourstudio.hskstroke.bishun.hanzi.core

import java.util.concurrent.atomic.AtomicInteger

object HanziCounter {
    private val counter = AtomicInteger(0)

    fun next(): Int = counter.incrementAndGet()
}

fun fixIndex(index: Int, length: Int): Int {
    return if (index < 0) length + index else index
}

fun <T> selectIndex(list: List<T>, index: Int): T {
    return list[fixIndex(index, list.size)]
}

fun average(values: List<Double>): Double {
    if (values.isEmpty()) return 0.0
    return values.sum() / values.size
}

fun <T> buildIndexedMap(size: Int, builder: (Int) -> T): Map<Int, T> {
    return buildMap {
        repeat(size) { index ->
            put(index, builder(index))
        }
    }
}
