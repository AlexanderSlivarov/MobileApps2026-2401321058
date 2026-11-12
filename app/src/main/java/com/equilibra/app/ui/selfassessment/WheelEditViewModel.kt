package com.equilibra.app.ui.selfassessment

import androidx.lifecycle.ViewModel
import com.equilibra.app.data.local.entity.WheelSector
import com.equilibra.app.data.repository.BalanceWheelRepository
import com.equilibra.app.domain.BalanceWheel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/** Rate my state: one score per indicator. There is only one rating; saving overwrites it. */
@HiltViewModel
class WheelEditViewModel
    @Inject
    constructor(
        private val wheelRepository: BalanceWheelRepository,
    ) : ViewModel() {
        // The slider values are kept here, so they survive rotation.
        val scores: MutableMap<WheelSector, Int> =
            WheelSector.entries.associateWith { BalanceWheel.DEFAULT_SCORE }.toMutableMap()

        private var loaded = false

        /** Fills the sliders once from my saved rating (the middle of the scale if there is none). */
        suspend fun load() {
            if (loaded) return
            loaded = true
            wheelRepository.observeScores().first()?.let { scores.putAll(it) }
        }

        suspend fun save() = wheelRepository.saveScores(scores)
    }
