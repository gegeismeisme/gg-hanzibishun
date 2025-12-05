package com.yourstudio.hskstroke.bishun.hanzi.parser

import com.yourstudio.hskstroke.bishun.data.characters.model.CharacterJsonDto
import com.yourstudio.hskstroke.bishun.hanzi.model.CharacterDefinition
import com.yourstudio.hskstroke.bishun.hanzi.model.Point
import com.yourstudio.hskstroke.bishun.hanzi.model.Stroke

class CharacterParser {

    fun parse(symbol: String, json: CharacterJsonDto): CharacterDefinition {
        require(json.strokes.size == json.medians.size) {
            "Stroke count (${json.strokes.size}) must match medians size (${json.medians.size})."
        }
        val radicalSet = json.radicalStrokes?.toSet() ?: emptySet()

        val strokes = json.strokes.mapIndexed { index, path ->
            val median = json.medians[index]
            val points = median.mapIndexed { _, coords ->
                require(coords.size == 2) { "Median entry must contain 2 coordinates." }
                Point(coords[0].toDouble(), coords[1].toDouble())
            }
            Stroke(
                path = path,
                points = points,
                strokeNum = index,
                isInRadical = radicalSet.contains(index),
            )
        }
        return CharacterDefinition(symbol, strokes)
    }
}
