package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.cloud.ChallengeProgressDoc
import com.example.data.cloud.CloudSyncRepository
import com.example.data.cloud.LeaderboardEntry
import com.example.data.cloud.UserCloudProfile
import com.example.game.ChallengeLevelProgress
import com.example.security.IntegrityCheckResult
import com.example.security.IntegrityStatusCode
import com.example.security.PlayIntegrityManager
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface CloudStreamState<out T> {
    data object Loading : CloudStreamState<Nothing>
    data class Success<T>(val data: T) : CloudStreamState<T>
    data class Error(val message: String) : CloudStreamState<Nothing>
}

data class CloudSessionStatus(
    val isSyncing: Boolean = false,
    val lastSyncMessage: String? = null,
    val lastErrorMessage: String? = null,
    val integrityStatus: IntegrityStatusCode = IntegrityStatusCode.NOT_CHECKED,
    val integritySummary: String = "Not verified yet"
)

class CloudSyncViewModel(
    private val repository: CloudSyncRepository,
    private val currentUserId: String,
    private val defaultDisplayName: String,
    private val playIntegrityManager: PlayIntegrityManager
) : ViewModel() {

    private val _sessionStatus = MutableStateFlow(CloudSessionStatus())
    val sessionStatus: StateFlow<CloudSessionStatus> = _sessionStatus.asStateFlow()

    val profileState: StateFlow<CloudStreamState<UserCloudProfile?>> =
        repository.observeUserProfile(currentUserId)
            .map<UserCloudProfile?, CloudStreamState<UserCloudProfile?>> { CloudStreamState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing profile", rootError)
                emit(CloudStreamState.Error(rootError.message ?: "Failed to load cloud profile"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = CloudStreamState.Loading
            )

    val challengeDocsState: StateFlow<CloudStreamState<List<ChallengeProgressDoc>>> =
        repository.observeChallengeProgress(currentUserId)
            .map<List<ChallengeProgressDoc>, CloudStreamState<List<ChallengeProgressDoc>>> {
                CloudStreamState.Success(it)
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing challenge docs", rootError)
                emit(CloudStreamState.Error(rootError.message ?: "Failed to load cloud challenges"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = CloudStreamState.Loading
            )

    val leaderboardState: StateFlow<CloudStreamState<List<LeaderboardEntry>>> =
        repository.observeLeaderboard(limit = 25L)
            .map<List<LeaderboardEntry>, CloudStreamState<List<LeaderboardEntry>>> {
                CloudStreamState.Success(it)
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing leaderboard", rootError)
                emit(CloudStreamState.Error(rootError.message ?: "Failed to load leaderboard"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = CloudStreamState.Loading
            )

    fun syncLocalStateToCloud(uiState: GameUiState) {
        viewModelScope.launch {
            _sessionStatus.update { it.copy(isSyncing = true, lastErrorMessage = null) }

            val effectiveBest = maxOf(uiState.classicBestScore, uiState.statistics.classicBestScore)
            val profileResult = repository.upsertUserProfile(
                displayName = defaultDisplayName,
                classicBestScore = effectiveBest,
                unlockedChallengeLevel = uiState.unlockedChallengeLevel,
                statistics = uiState.statistics,
                settings = uiState.settings
            )

            var firstError: Throwable? = profileResult.exceptionOrNull()

            uiState.challengeProgress.values.forEach { levelProgress ->
                val res = repository.syncChallengeLevel(levelProgress)
                if (firstError == null && res.isFailure) {
                    firstError = res.exceptionOrNull()
                }
            }

            if (effectiveBest > 0) {
                val lbRes = repository.publishLeaderboardEntry(
                    displayName = defaultDisplayName,
                    classicBestScore = effectiveBest,
                    maxCascadeDepth = uiState.statistics.maxCascadeDepth,
                    rankTitle = uiState.playerRank.title
                )
                if (firstError == null && lbRes.isFailure) {
                    firstError = lbRes.exceptionOrNull()
                }
            }

            _sessionStatus.update {
                if (firstError == null) {
                    it.copy(
                        isSyncing = false,
                        lastSyncMessage = "Cloud save up to date",
                        lastErrorMessage = null
                    )
                } else {
                    it.copy(
                        isSyncing = false,
                        lastErrorMessage = firstError?.localizedMessage ?: "Cloud sync failed"
                    )
                }
            }
        }
    }

    fun syncChallengeLevel(progress: ChallengeLevelProgress) {
        viewModelScope.launch {
            repository.syncChallengeLevel(progress)
        }
    }

    fun verifyAppIntegrity() {
        viewModelScope.launch {
            _sessionStatus.update {
                it.copy(
                    integritySummary = "Checking Play Integrity...",
                    lastErrorMessage = null
                )
            }
            val result: IntegrityCheckResult = playIntegrityManager.requestAttestation()
            val saveRes = repository.recordIntegrityAttestation(result)
            _sessionStatus.update {
                it.copy(
                    integrityStatus = result.status,
                    integritySummary = result.summaryMessage,
                    lastErrorMessage = saveRes.exceptionOrNull()?.localizedMessage
                )
            }
        }
    }

    private companion object {
        const val TAG = "CloudSyncVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
