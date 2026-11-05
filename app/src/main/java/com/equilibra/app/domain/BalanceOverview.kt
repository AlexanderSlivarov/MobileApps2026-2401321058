package com.equilibra.app.domain

import com.equilibra.app.data.local.entity.DecisionEntity
import com.equilibra.app.data.local.entity.DecisionResult
import com.equilibra.app.data.local.entity.FindingEntity
import com.equilibra.app.data.local.entity.FindingType

/** Counts for "My system", kept free of Android classes so they can be unit-tested. */
object BalanceOverview {
    /** How many problems, habits and needs I found. Every type is present, 0 if none. */
    fun countFindings(findings: List<FindingEntity>): Map<FindingType, Int> =
        FindingType.entries.associateWith { type -> findings.count { it.type == type } }

    /** How many decisions worked, didn't work, or are still pending. Every result is present. */
    fun countDecisions(decisions: List<DecisionEntity>): Map<DecisionResult, Int> =
        DecisionResult.entries.associateWith { result -> decisions.count { it.result == result } }
}
