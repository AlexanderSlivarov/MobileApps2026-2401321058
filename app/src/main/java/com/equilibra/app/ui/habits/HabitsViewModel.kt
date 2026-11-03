package com.equilibra.app.ui.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equilibra.app.data.local.entity.DayActivityEntity
import com.equilibra.app.data.repository.HabitsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

/** The activities of one chosen day, ordered by start time (the R of CRUD). */
@HiltViewModel
class HabitsViewModel
    @Inject
    constructor(
        habitsRepository: HabitsRepository,
    ) : ViewModel() {
        /** The day shown in the list. Kept here so it survives rotation. */
        val selectedDate = MutableStateFlow(LocalDate.now())

        /** Updates by itself when an activity changes or another day is chosen. */
        val activities: StateFlow<List<DayActivityEntity>> =
            combine(habitsRepository.observeAllActivities(), selectedDate) { all, date ->
                all.filter { it.date == date }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    }
