package com.questpdf.helpers

import com.questpdf.infrastructure.Color
import com.questpdf.infrastructure.ImageSize
import java.util.Random

/**
 * Hand-written implementation of QuestPDF.Helpers.Placeholders (excluded from
 * generation via manual-overrides.txt). Unlike the generated no-op stubs, this
 * object returns real placeholder data so sample compositions read naturally.
 */
object Placeholders {
    val Random: Random = Random(2026)

    private val firstNames = listOf("Maria", "James", "Aino", "Lucas", "Sofia", "Henrik", "Emma", "Jan")
    private val lastNames = listOf("Virtanen", "Smith", "Korhonen", "Novak", "Lindqvist", "Weber", "Rossi", "Kowalski")
    private val labels = listOf("Overview", "Details", "Summary", "Notes", "Highlights", "Insights")
    private val questions = listOf(
        "How does pagination behave here?",
        "Does the layout survive long content?",
        "What happens on page overflow?",
    )
    private val words = (
        "lorem ipsum dolor sit amet consectetur adipiscing elit sed do eiusmod tempor incididunt " +
        "ut labore et dolore magna aliqua enim ad minim veniam quis nostrud exercitation ullamco"
    ).split(' ')

    private val palette = listOf(
        Colors.Red.Medium, Colors.Pink.Medium, Colors.Purple.Medium, Colors.Indigo.Medium,
        Colors.Blue.Medium, Colors.Cyan.Medium, Colors.Teal.Medium, Colors.Green.Medium,
        Colors.Amber.Medium, Colors.Orange.Medium,
    )

    private val backgrounds = listOf(
        Colors.Red.Lighten4, Colors.Pink.Lighten4, Colors.Purple.Lighten4, Colors.Indigo.Lighten4,
        Colors.Blue.Lighten4, Colors.Cyan.Lighten4, Colors.Teal.Lighten4, Colors.Green.Lighten4,
        Colors.Amber.Lighten4, Colors.Orange.Lighten4,
    )

    private fun <T> pick(values: List<T>): T = values[Random.nextInt(values.size)]

    private fun words(count: Int): String =
        (1..count).joinToString(" ") { pick(words) }

    fun backgroundColor(): Color = pick(backgrounds)

    fun color(): Color = pick(palette)

    fun dateTime(): String = "2026-07-13 14:30"

    fun decimal(): String = "%.2f".format(Random.nextDouble() * 100)

    fun email(): String =
        "${pick(firstNames).lowercase()}.${pick(lastNames).lowercase()}@example.com"

    fun image(size: ImageSize): ByteArray = image(size.width, size.height)

    /**
     * A real, decodable PNG (the native engine actually decodes it): a diagonal
     * two-color gradient built from the placeholder palette.
     */
    fun image(width: Int, height: Int): ByteArray {
        val w = maxOf(1, width)
        val h = maxOf(1, height)
        val from = pick(palette)
        val to = pick(backgrounds)
        val image = java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_RGB)

        for (y in 0 until h) {
            for (x in 0 until w) {
                val t = (x.toFloat() / w + y.toFloat() / h) / 2f
                val rgb = mix(from.hex.toInt(), to.hex.toInt(), t)
                image.setRGB(x, y, rgb)
            }
        }

        val output = java.io.ByteArrayOutputStream()
        javax.imageio.ImageIO.write(image, "png", output)
        return output.toByteArray()
    }

    private fun mix(from: Int, to: Int, t: Float): Int {
        fun channel(shift: Int): Int {
            val a = (from shr shift) and 0xFF
            val b = (to shr shift) and 0xFF
            return ((a + (b - a) * t).toInt() and 0xFF) shl shift
        }
        return channel(16) or channel(8) or channel(0)
    }

    fun integer(): String = Random.nextInt(1000).toString()

    fun label(): String = pick(labels)

    fun longDate(): String = "Monday, 13 July 2026"

    fun loremIpsum(): String = sentenceCase(words(48)) + "."

    fun name(): String = "${pick(firstNames)} ${pick(lastNames)}"

    fun paragraph(): String = sentenceCase(words(32)) + "."

    fun paragraphs(): String = (1..3).joinToString("\n") { paragraph() }

    fun percent(): String = "${Random.nextInt(100)}%"

    fun phoneNumber(): String = "+358 40 %03d %04d".format(Random.nextInt(1000), Random.nextInt(10000))

    fun price(): String = "€%.2f".format(Random.nextDouble() * 500)

    fun question(): String = pick(questions)

    fun sentence(): String = sentenceCase(words(10)) + "."

    fun shortDate(): String = "2026-07-13"

    fun time(): String = "14:30"

    fun webpageUrl(): String = "https://www.example.com/${pick(labels).lowercase()}"

    private fun sentenceCase(text: String): String =
        text.replaceFirstChar { it.uppercase() }
}
