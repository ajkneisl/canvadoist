package dev.ajkneisl

import java.awt.Color
import kotlin.math.pow

const val RC = "\u001b[0m"
const val U = "\u001b[4m"

fun rgbbg(r: Int, g: Int, b: Int) = "\u001b[48;2;$r;$g;${b}m"

private val DEFAULT_COLORS =
    mapOf(
            "#b8256f" to "berry_red",
            "#db4035" to "red",
            "#ff9933" to "orange",
            "#fad000" to "yellow",
            "#afb83b" to "olive_green",
            "#7ecc49" to "lime_green",
            "#299438" to "green",
            "#6accbc" to "mint_green",
            "#158fad" to "teal",
            "#14aaf5" to "sky_blue",
            "#96c3eb" to "light_blue",
            "#4073ff" to "blue",
            "#884dff" to "grape",
            "#af38eb" to "violet",
            "#eb96eb" to "lavender",
            "#e05194" to "magenta",
            "#ff8d85" to "salmon",
            "#808080" to "charcoal",
            "#b8b8b8" to "grey",
            "#ccac93" to "taupe",
        )
        .map { (key, value) -> Color.decode(key) to value }
        .associate { it }

fun nearestColor(selectedColor: Color, colorsMap: Map<Color, String> = DEFAULT_COLORS): String {
    var minDistanceSq = Double.MAX_VALUE
    var closestColor: Color? = null

    for (color in colorsMap.keys) {
        val distanceSq =
            (selectedColor.red - color.red).toDouble().pow(2) +
                (selectedColor.green - color.green).toDouble().pow(2) +
                (selectedColor.blue - color.blue).toDouble().pow(2)

        if (distanceSq < minDistanceSq) {
            minDistanceSq = distanceSq
            closestColor = color
        }
    }

    return colorsMap[closestColor!!]!!
}
