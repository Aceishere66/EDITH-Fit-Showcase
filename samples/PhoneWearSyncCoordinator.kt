package com.edithdevstudio.edithfit.wear

import com.edithdevstudio.edithfit.data.local.workout.WorkoutPreferencesStore
import com.edithdevstudio.edithfit.domain.model.workout.WorkoutPreferences
import com.edithdevstudio.edithfit.domain.model.workout.WorkoutSessionAggregate
import com.edithdevstudio.edithfit.domain.model.workout.WorkoutSessionState
import com.edithdevstudio.edithfit.domain.workout.WorkoutRepository
import com.edithdevstudio.edithfit.wearprotocol.WearWorkoutSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

sealed interface SnapshotPublicationResult {
    val snapshot: WearWorkoutSnapshot
    val revision: Long

    data class Published(
        override val snapshot: WearWorkoutSnapshot,
        override val revision: Long
    ) : SnapshotPublicationResult

    data class PublicationFailed(
        override val snapshot: WearWorkoutSnapshot,
        override val revision: Long
    ) : SnapshotPublicationResult
}

class PhoneWearSyncCoordinator(
    private val workoutRepository: WorkoutRepository,
    private val workoutPreferencesStore: WorkoutPreferencesStore,
    private val publisher: WearStatePublisher,
    private val scope: CoroutineScope,
    private val syncStateStore: WearSyncStateStore
) {
    private val publishMutex = Mutex()
    private var syncJob: Job? = null

    fun start() {
        if (syncJob?.isActive == true) return
        syncJob = scope.launch {
            combine(
                workoutRepository.observeActiveSession(),
                workoutPreferencesStore.preferences
            ) { session, preferences ->
                session to preferences
            }.collect { (session, preferences) ->
                publishSnapshotInternal(session, preferences)
            }
        }
    }

    fun stop() {
        syncJob?.cancel()
        syncJob = null
    }

    suspend fun publishAuthoritativeSnapshot(): SnapshotPublicationResult = publishMutex.withLock {
        val session = workoutRepository.getActiveSession()
        val preferences = workoutPreferencesStore.preferences.first()
        val rev = syncStateStore.nextRevision()
        val snapshot = mapToSnapshot(session, preferences, rev)
        val success = publisher.publishSnapshot(snapshot)

        if (success) {
            SnapshotPublicationResult.Published(snapshot, rev)
        } else {
            SnapshotPublicationResult.PublicationFailed(snapshot, rev)
        }
    }

    private suspend fun publishSnapshotInternal(
        session: WorkoutSessionAggregate?,
        preferences: WorkoutPreferences
    ): SnapshotPublicationResult = publishMutex.withLock {
        val rev = syncStateStore.nextRevision()
        val snapshot = mapToSnapshot(session, preferences, rev)
        val success = publisher.publishSnapshot(snapshot)

        if (success) {
            SnapshotPublicationResult.Published(snapshot, rev)
        } else {
            SnapshotPublicationResult.PublicationFailed(snapshot, rev)
        }
    }

    fun mapToSnapshot(
        session: WorkoutSessionAggregate?,
        preferences: WorkoutPreferences,
        revision: Long
    ): WearWorkoutSnapshot {
        val now = System.currentTimeMillis()

        if (session == null) {
            return WearWorkoutSnapshot.EMPTY.copy(
                revision = revision,
                publishedAtEpochMs = now
            )
        }

        val currentEx = session.currentExercise
        val exIndex = session.exercises.indexOfFirst {
            it.exercise.id == session.runtimeState.currentWorkoutExerciseId
        }
        val exPosition = if (exIndex >= 0) exIndex + 1 else 0
        val timer = session.runtimeState.activeRestTimer

        return WearWorkoutSnapshot(
            revision = revision,
            sessionId = session.workout.id,
            workoutTitle = session.workout.title,
            sessionState = session.runtimeState.state.name,
            currentExerciseId = currentEx?.exercise?.id,
            currentExerciseName = currentEx?.exercise?.exerciseNameSnapshot,
            exercisePosition = exPosition,
            exerciseCount = session.exercises.size,
            currentSetIndex = session.runtimeState.currentSetIndex,
            setCount = currentEx?.sets?.size ?: 0,
            targetReps = currentEx?.exercise?.targetRepsMax ?: currentEx?.exercise?.targetRepsMin,
            completedSetCount = currentEx?.sets?.count { it.completed } ?: 0,
            isRestActive = session.runtimeState.state == WorkoutSessionState.REST_ACTIVE,
            activeRestTimerSourceSetId = timer?.sourceSetId,
            restStartedAtEpochMs = timer?.startedAtEpochMs,
            restExpiresAtEpochMs = timer?.expiresAtEpochMs,
            restPlannedDurationSeconds = timer?.plannedDurationSeconds,
            autoRestTimerEnabled = preferences.autoRestTimerEnabled,
            restHapticEnabled = preferences.restHapticEnabled,
            hasPendingRestOpportunity = session.runtimeState.pendingRestOpportunity != null,
            publishedAtEpochMs = now,
        )
    }
}
