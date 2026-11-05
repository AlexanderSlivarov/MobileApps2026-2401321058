package com.equilibra.app.domain

import com.equilibra.app.data.local.entity.FindingEntity
import com.equilibra.app.data.local.entity.FindingType

/** The "5 Whys" technique from Lecture 2, kept free of Android classes so it can be unit-tested. */
object FiveWhys {
    const val MAX_WHYS = 5

    /**
     * The deepest answer reached: the last "Why?" that was filled in. Null when no answer
     * was given yet. It is still a guess until checked with real observations (Lecture 2).
     */
    fun rootCause(whys: List<String>): String? = whys.lastOrNull { it.isNotBlank() }?.trim()

    /** How many problems already have at least one "Why?" answer. */
    fun countAnalyzed(findings: List<FindingEntity>): Int =
        findings.count { it.type == FindingType.PROBLEM && rootCause(it.whys) != null }
}
