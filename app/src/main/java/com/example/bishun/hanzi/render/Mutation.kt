package com.example.bishun.hanzi.render

typealias RenderStateReducer = (RenderStateSnapshot) -> RenderStateSnapshot

class Mutation(
    val scope: String,
    private val reducer: RenderStateReducer,
    val durationMillis: Long = 0L,
    val force: Boolean = false,
) {
    internal fun targetState(current: RenderStateSnapshot): RenderStateSnapshot {
        return reducer(current)
    }
}
