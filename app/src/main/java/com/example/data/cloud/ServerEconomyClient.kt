package com.example.data.cloud

import android.util.Log
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await
import java.util.UUID

/** Wallet snapshot returned by the server-authoritative economy functions. */
data class ServerWallet(
    val goldCoins: Int,
    val diamonds: Int,
    val hammerCount: Int,
    val shuffleCount: Int,
    val refreshCount: Int,
    val wheelSpinPoints: Int
)

data class WheelSpinResult(
    val segmentIndex: Int,
    val prizeLabel: String,
    val wallet: ServerWallet
)

enum class AdRewardType(val serverKey: String) {
    GOLD_50("GOLD_50"),
    DIAMOND_5("DIAMOND_5"),
    WHEEL_POINT("WHEEL_POINT")
}

/**
 * Thin client for the Cloud Functions in `functions/index.js`. Rewards, wheel outcomes and
 * ability consumption are decided on the server; the client only displays the result.
 */
class ServerEconomyClient(
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance("us-central1")
) {

    suspend fun claimAdReward(type: AdRewardType): Result<ServerWallet> = call(
        "claimRewardedAd",
        mapOf("rewardType" to type.serverKey, "transactionId" to newTransactionId())
    ).map { parseWallet(it["wallet"]) }

    suspend fun spinLuckyWheel(): Result<WheelSpinResult> = call("spinLuckyWheel", emptyMap<String, Any>())
        .map {
            WheelSpinResult(
                segmentIndex = (it["segmentIndex"] as? Number)?.toInt() ?: 0,
                prizeLabel = it["prizeLabel"] as? String ?: "",
                wallet = parseWallet(it["wallet"])
            )
        }

    suspend fun fetchWallet(): Result<ServerWallet> = call("initializeServerWallet", emptyMap<String, Any>())
        .map { parseWallet(it["wallet"]) }

    private suspend fun call(name: String, data: Map<String, Any>): Result<Map<*, *>> = try {
        val result = functions.getHttpsCallable(name).call(data).await()
        Result.success(result.getData() as? Map<*, *> ?: emptyMap<Any, Any>())
    } catch (t: Throwable) {
        Log.w(TAG, "Callable $name failed: ${t.message}")
        Result.failure(t)
    }

    private fun parseWallet(raw: Any?): ServerWallet {
        val m = raw as? Map<*, *> ?: emptyMap<Any, Any>()
        fun int(key: String) = (m[key] as? Number)?.toInt() ?: 0
        return ServerWallet(
            goldCoins = int("goldCoins"),
            diamonds = int("diamonds"),
            hammerCount = int("hammerCount"),
            shuffleCount = int("shuffleCount"),
            refreshCount = int("refreshCount"),
            wheelSpinPoints = int("wheelSpinPoints")
        )
    }

    private fun newTransactionId() = UUID.randomUUID().toString().replace("-", "")

    private companion object {
        const val TAG = "ServerEconomyClient"
    }
}
