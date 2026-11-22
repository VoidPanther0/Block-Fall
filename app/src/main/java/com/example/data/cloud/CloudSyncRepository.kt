package com.example.data.cloud

import android.content.Context
import com.example.R
import com.example.game.ChallengeLevelProgress
import com.example.game.GameSettings
import com.example.game.GameStatistics
import com.example.security.IntegrityCheckResult
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class CloudSyncRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeUserProfile(userId: String): Flow<UserCloudProfile?> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Signed-in user mismatch for profile stream." }
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snapshot ->
                    if (!snapshot.exists()) null
                    else snapshot.toObject(
                        UserCloudProfile::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    logStreamFailure(error, OperationType.GET, path, uid)
                    throw error
                }
        )
    }

    fun observeChallengeProgress(userId: String): Flow<List<ChallengeProgressDoc>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Signed-in user mismatch for challenge stream." }
        val path = "users/$uid/challengeProgress"
        emitAll(
            db.collection("users").document(uid).collection("challengeProgress")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        ChallengeProgressDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    logStreamFailure(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    fun observeLeaderboard(limit: Long = 25L): Flow<List<LeaderboardEntry>> = flow {
        val uid = requireUserId()
        val path = "leaderboard"
        emitAll(
            db.collection("leaderboard")
                .whereEqualTo("visibility", "public")
                .limit(limit.coerceIn(1L, 50L))
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        LeaderboardEntry::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.classicBestScore }
                }
                .catch { error ->
                    logStreamFailure(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    suspend fun upsertUserProfile(
        displayName: String,
        classicBestScore: Int,
        unlockedChallengeLevel: Int,
        statistics: GameStatistics,
        settings: GameSettings
    ): Result<Unit> {
        val uidPreview = auth.currentUser?.uid ?: "unauthenticated"
        val docPath = "users/$uidPreview"
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid)
            val existing = docRef.get().await()
            val prevBest = existing.getLong("classicBestScore") ?: 0L
            val prevUnlocked = existing.getLong("unlockedChallengeLevel") ?: 1L
            val prevGames = existing.getLong("totalGamesPlayed") ?: 0L
            val prevLines = existing.getLong("totalLinesCleared") ?: 0L
            val prevCascades = existing.getLong("totalCascadesTriggered") ?: 0L
            val prevMaxDepth = existing.getLong("maxCascadeDepth") ?: 0L
            val prevBlocks = existing.getLong("totalBlocksPlaced") ?: 0L
            val prevFullClears = existing.getLong("fullBoardClears") ?: 0L

            val merged = UserCloudProfile(
                userId = uid,
                displayName = UserCloudProfile.sanitizeDisplayName(displayName),
                classicBestScore = maxOf(prevBest, classicBestScore.toLong()),
                unlockedChallengeLevel = maxOf(prevUnlocked, unlockedChallengeLevel.toLong()),
                totalGamesPlayed = maxOf(prevGames, statistics.totalGamesPlayed.toLong()),
                totalLinesCleared = maxOf(prevLines, statistics.totalLinesCleared.toLong()),
                totalCascadesTriggered = maxOf(prevCascades, statistics.totalCascadesTriggered.toLong()),
                maxCascadeDepth = maxOf(prevMaxDepth, statistics.maxCascadeDepth.toLong()),
                totalBlocksPlaced = maxOf(prevBlocks, statistics.totalBlocksPlaced.toLong()),
                fullBoardClears = maxOf(prevFullClears, statistics.fullBoardClears.toLong()),
                blockStyleId = UserCloudProfile.sanitizeBlockStyle(settings.blockStyle.id),
                boardPaletteId = UserCloudProfile.sanitizeBoardPalette(settings.boardPalette.id),
                soundEnabled = settings.soundEnabled,
                musicEnabled = settings.musicEnabled,
                hapticsEnabled = settings.hapticsEnabled,
                visualEffectsEnabled = settings.visualEffectsEnabled,
                placementPreviewEnabled = settings.placementPreviewEnabled
            )

            if (!existing.exists()) {
                docRef.set(merged.toCreateMap()).await()
            } else {
                docRef.update(merged.toUpdateMap()).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    suspend fun syncChallengeLevel(progress: ChallengeLevelProgress): Result<Unit> {
        val levelId = "level_${progress.levelNumber}"
        val uidPreview = auth.currentUser?.uid ?: "unauthenticated"
        val docPath = "users/$uidPreview/challengeProgress/$levelId"
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid)
                .collection("challengeProgress").document(levelId)
            val existing = docRef.get().await()
            val prevBest = existing.getLong("bestScore") ?: 0L
            val prevStars = existing.getLong("stars") ?: 0L
            val prevCompleted = existing.getBoolean("completed") == true

            val model = ChallengeProgressDoc(
                levelNumber = progress.levelNumber.toLong().coerceIn(1L, 50L),
                bestScore = maxOf(prevBest, progress.bestScore.toLong()),
                stars = maxOf(prevStars, progress.stars.toLong()),
                completed = prevCompleted || progress.completed
            )
            if (!existing.exists()) {
                docRef.set(model.toCreateMap()).await()
            } else {
                docRef.update(model.toUpdateMap()).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    suspend fun publishLeaderboardEntry(
        displayName: String,
        classicBestScore: Int,
        maxCascadeDepth: Int,
        rankTitle: String
    ): Result<Unit> {
        val uidPreview = auth.currentUser?.uid ?: "unauthenticated"
        val docPath = "leaderboard/$uidPreview"
        return try {
            val uid = requireUserId()
            val docRef = db.collection("leaderboard").document(uid)
            val existing = docRef.get().await()
            val prevBest = existing.getLong("classicBestScore") ?: 0L
            val mergedScore = maxOf(prevBest, classicBestScore.toLong())
                .coerceIn(0L, UserCloudProfile.MAX_SCORE)
            val safeName = UserCloudProfile.sanitizeDisplayName(displayName)
            val safeRank = rankTitle.trim().take(32).ifEmpty { "Block Initiate" }
            val safeDepth = maxCascadeDepth.toLong().coerceIn(0L, UserCloudProfile.MAX_DEPTH)

            if (!existing.exists()) {
                val createMap = mapOf(
                    "userId" to uid,
                    "displayName" to safeName,
                    "visibility" to "public",
                    "classicBestScore" to mergedScore,
                    "maxCascadeDepth" to safeDepth,
                    "rankTitle" to safeRank,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(createMap).await()
            } else {
                val updateMap = mapOf(
                    "displayName" to safeName,
                    "classicBestScore" to mergedScore,
                    "maxCascadeDepth" to safeDepth,
                    "rankTitle" to safeRank,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updateMap).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    suspend fun recordIntegrityAttestation(result: IntegrityCheckResult): Result<Unit> {
        val attId = "att_${System.currentTimeMillis()}"
        val uidPreview = auth.currentUser?.uid ?: "unauthenticated"
        val docPath = "users/$uidPreview/integrityAttestations/$attId"
        return try {
            val uid = requireUserId()
            val allowedStatus = if (result.status.wireValue == "PENDING_SERVER_VERIFICATION") {
                "PENDING_SERVER_VERIFICATION"
            } else {
                "LOCAL_UNAVAILABLE"
            }
            val payload = mapOf(
                "userId" to uid,
                "nonce" to result.nonce.take(128).padEnd(16, '0'),
                "tokenLength" to result.tokenLength.toLong().coerceIn(1L, 8192L),
                "tokenDigest" to result.tokenDigest.take(128).padEnd(16, '0'),
                "appVersionCode" to result.appVersionCode.toLong().coerceAtLeast(1L),
                "verificationStatus" to allowedStatus,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(uid)
                .collection("integrityAttestations").document(attId)
                .set(payload)
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    private fun logStreamFailure(
        error: Throwable,
        op: OperationType,
        path: String,
        uid: String
    ) {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) return
        val toLog = firestoreError ?: (error as? Exception) ?: return
        if (auth.currentUser?.uid == uid) {
            handleFirestoreError(toLog, op, path)
        }
    }
}
