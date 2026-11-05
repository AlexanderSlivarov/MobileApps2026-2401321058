package com.equilibra.app.domain

/**
 * The self-assessment questions: 6 questions, 3 options each. The answers are kept as one
 * string with a character per question ("0", "1", "2", or "-" when not answered yet).
 * The first 3 questions are about "who am I", the last 3 about "how I manage my time".
 * Plain Kotlin, so it can be unit-tested.
 */
object SelfAssessment {
    const val QUESTION_COUNT = 6
    const val OPTION_COUNT = 3

    /** Question numbers (0-based) for each half of the section question. */
    val WHO_AM_I: IntRange = 0..2
    val HOW_I_MANAGE_TIME: IntRange = 3..5

    private const val NOT_ANSWERED = '-'

    /** Always exactly one character per question, e.g. "" becomes "------". */
    private fun normalize(answers: String): String = answers.padEnd(QUESTION_COUNT, NOT_ANSWERED).take(QUESTION_COUNT)

    /** The chosen option (0-2) of question [index], or null when it isn't answered yet. */
    fun answerAt(
        answers: String,
        index: Int,
    ): Int? = normalize(answers)[index].digitToIntOrNull()?.takeIf { it in 0 until OPTION_COUNT }

    /** The answers after choosing [option] for question [index]. */
    fun withAnswer(
        answers: String,
        index: Int,
        option: Int,
    ): String {
        require(index in 0 until QUESTION_COUNT && option in 0 until OPTION_COUNT) { "No such question or option" }
        val chars = normalize(answers).toCharArray()
        chars[index] = option.digitToChar()
        return String(chars)
    }

    /** How many questions have an answer. */
    fun answeredCount(answers: String): Int = (0 until QUESTION_COUNT).count { answerAt(answers, it) != null }
}
