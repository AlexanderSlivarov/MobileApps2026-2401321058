package com.equilibra.app.ui.habits

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.equilibra.app.data.local.entity.ActivityCategory
import com.equilibra.app.data.local.entity.DayActivityEntity
import com.equilibra.app.data.repository.HabitsRepository
import com.equilibra.app.domain.ActivityTimes
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

/** Create, edit and delete one activity (the C, U and D of CRUD). */
@HiltViewModel
class ActivityEditViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val habitsRepository: HabitsRepository,
    ) : ViewModel() {
        // The navigation arguments "activityId" (-1 = new activity) and "date" (the day shown in the list).
        private val activityId: Long = savedStateHandle[ARG_ACTIVITY_ID] ?: NEW_ACTIVITY_ID

        val isNewActivity: Boolean get() = activityId == NEW_ACTIVITY_ID

        // The values chosen with pickers are kept here so they survive rotation.
        // A new activity starts at the current full hour and lasts one hour.
        var date: LocalDate = LocalDate.ofEpochDay(savedStateHandle[ARG_DATE] ?: LocalDate.now().toEpochDay())
        var startMinute: Int = LocalTime.now().hour * 60
        var endMinute: Int = (startMinute + 60) % ActivityTimes.MINUTES_PER_DAY
        var category: ActivityCategory = ActivityCategory.STUDY

        /** The stored activity, so fields that this form doesn't show (tag, planned) are kept on save. */
        private var stored: DayActivityEntity? = null

        /**
         * Loads the stored activity (null for a new one) and takes over its values.
         * Only the first call reads the database, so after a rotation the picked values stay.
         */
        suspend fun loadActivity(): DayActivityEntity? {
            if (isNewActivity || stored != null) return stored
            val activity = habitsRepository.getActivityById(activityId) ?: return null
            stored = activity
            date = activity.date
            startMinute = activity.startMinute
            endMinute = activity.endMinute
            category = activity.category
            return activity
        }

        suspend fun save(title: String) {
            val form =
                DayActivityEntity(
                    date = date,
                    title = title.trim(),
                    category = category,
                    startMinute = startMinute,
                    endMinute = endMinute,
                )
            // When editing, copy onto the stored row so its id, tag and planned flag stay the same.
            val activity =
                stored?.copy(
                    date = form.date,
                    title = form.title,
                    category = form.category,
                    startMinute = form.startMinute,
                    endMinute = form.endMinute,
                ) ?: form
            if (isNewActivity) habitsRepository.addActivity(activity) else habitsRepository.updateActivity(activity)
        }

        suspend fun delete() {
            habitsRepository.getActivityById(activityId)?.let { habitsRepository.deleteActivity(it) }
        }

        companion object {
            /** Must match the argument names in nav_graph.xml. */
            const val ARG_ACTIVITY_ID = "activityId"
            const val ARG_DATE = "date"
            const val NEW_ACTIVITY_ID = -1L
        }
    }
