const crypto = require("node:crypto");
const { onCall, onRequest, HttpsError } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");
const { getFirestore, FieldValue } = require("firebase-admin/firestore");
const { google } = require("googleapis");

admin.initializeApp();

const DATABASE_ID = "ai-studio-android-blockfal-1b3ea5ab-3835-4dbd-8287-42466096ac30";
const PACKAGE_NAME = "com.aistudio.cascadegrid.vqmxrt";

const MAX_GOLD = 50000;
const MAX_DIAMONDS = 5000;
const MAX_BOOSTER_STACK = 99;
const MAX_WHEEL_POINTS = 99;
const RENAME_DIAMOND_COST = 100;

/**
 * Authoritative 10-segment Lucky Wheel table (sum of weights = 100).
 * Client renders matching segments in exact index order 0..9.
 */
const WHEEL_SEGMENTS = [
  { id: "100_diamonds", label: "100 Diamonds", category: "DIAMONDS", amount: 100, weight: 1 },
  { id: "50_diamonds", label: "50 Diamonds", category: "DIAMONDS", amount: 50, weight: 3 },
  { id: "10_diamonds", label: "10 Diamonds", category: "DIAMONDS", amount: 10, weight: 10 },

  { id: "1000_gold", label: "1,000 Gold", category: "GOLD", amount: 1000, weight: 2 },
  { id: "500_gold", label: "500 Gold", category: "GOLD", amount: 500, weight: 5 },
  { id: "100_gold", label: "100 Gold", category: "GOLD", amount: 100, weight: 20 },
  { id: "20_gold", label: "20 Gold", category: "GOLD", amount: 20, weight: 30 },

  { id: "hammer_1", label: "Hammer +1", category: "HAMMER", amount: 1, weight: 8 },
  { id: "shuffle_1", label: "Shuffle +1", category: "SHUFFLE", amount: 1, weight: 8 },
  { id: "refresh_1", label: "Refresh +1", category: "REFRESH", amount: 1, weight: 8 },
];

const TOTAL_WHEEL_WEIGHT = WHEEL_SEGMENTS.reduce((acc, s) => acc + s.weight, 0);

function getDb() {
  return getFirestore(DATABASE_ID);
}

function walletRefFor(db, uid) {
  return db.collection("users").doc(uid).collection("serverEconomy").doc("wallet");
}

function starterWalletDoc(uid) {
  return {
    userId: uid,
    goldCoins: 250,
    diamonds: 20,
    hammerCount: 3,
    shuffleCount: 3,
    refreshCount: 3,
    wheelSpinPoints: 0,
    totalSpins: 0,
    lastAdClaimAtMs: 0,
    createdAt: FieldValue.serverTimestamp(),
    updatedAt: FieldValue.serverTimestamp(),
  };
}

function sanitizeWalletResponse(doc) {
  return {
    goldCoins: Number(doc.goldCoins || 0),
    diamonds: Number(doc.diamonds || 0),
    hammerCount: Number(doc.hammerCount || 0),
    shuffleCount: Number(doc.shuffleCount || 0),
    refreshCount: Number(doc.refreshCount || 0),
    wheelSpinPoints: Number(doc.wheelSpinPoints || 0),
    totalSpins: Number(doc.totalSpins || 0),
  };
}

function assertValidTransactionId(txId) {
  if (typeof txId !== "string" || !/^[a-zA-Z0-9_-]{8,80}$/.test(txId)) {
    throw new HttpsError("invalid-argument", "Invalid transactionId.");
  }
  return txId;
}

function requireSignedInUid(request) {
  if (!request.auth || !request.auth.uid) {
    throw new HttpsError("unauthenticated", "Google Sign-In required.");
  }
  return request.auth.uid;
}

/**
 * Ensures the signed-in user has a server-authoritative economy wallet document.
 */
exports.initializeServerWallet = onCall(
  { enforceAppCheck: true, region: "us-central1" },
  async (request) => {
    const uid = requireSignedInUid(request);
    const db = getDb();
    const wRef = walletRefFor(db, uid);

    const wallet = await db.runTransaction(async (tx) => {
      const snap = await tx.get(wRef);
      if (snap.exists) {
        return snap.data();
      }
      const initial = starterWalletDoc(uid);
      tx.set(wRef, initial);
      return {
        ...initial,
        goldCoins: 250,
        diamonds: 20,
        hammerCount: 3,
        shuffleCount: 3,
        refreshCount: 3,
        wheelSpinPoints: 0,
        totalSpins: 0,
      };
    });

    return { wallet: sanitizeWalletResponse(wallet) };
  }
);

/**
 * Server-authoritative rewarded ad claim for Gold (+50), Diamonds (+5), and Lucky Wheel (+1 spin point).
 */
exports.claimRewardedAd = onCall(
  { enforceAppCheck: true, region: "us-central1" },
  async (request) => {
    const uid = requireSignedInUid(request);
    const { rewardType, transactionId } = request.data || {};
    const txId = assertValidTransactionId(transactionId);

    const REWARDS = {
      GOLD_50: { goldDelta: 50, diamondDelta: 0, wheelDelta: 0, label: "+50 Gold" },
      DIAMOND_5: { goldDelta: 0, diamondDelta: 5, wheelDelta: 0, label: "+5 Diamonds" },
      WHEEL_POINT: { goldDelta: 0, diamondDelta: 0, wheelDelta: 1, label: "+1 Wheel Spin" },
    };

    const spec = REWARDS[rewardType];
    if (!spec) {
      throw new HttpsError("invalid-argument", "Unsupported rewarded ad type.");
    }

    const db = getDb();
    const wRef = walletRefFor(db, uid);
    const ledgerRef = db.collection("users").doc(uid).collection("rewardLedger").doc(txId);
    const nowMs = Date.now();

    const resultWallet = await db.runTransaction(async (tx) => {
      const [wSnap, lSnap] = await Promise.all([tx.get(wRef), tx.get(ledgerRef)]);
      if (lSnap.exists) {
        throw new HttpsError("already-exists", "Reward already claimed for this transaction.");
      }

      const cur = wSnap.exists ? wSnap.data() : starterWalletDoc(uid);
      const lastClaimMs = Number(cur.lastAdClaimAtMs || 0);
      if (lastClaimMs > 0 && nowMs - lastClaimMs < 5000) {
        throw new HttpsError("resource-exhausted", "Please wait a few seconds before claiming another ad reward.");
      }

      const nextWallet = {
        userId: uid,
        goldCoins: Math.min(MAX_GOLD, Number(cur.goldCoins || 0) + spec.goldDelta),
        diamonds: Math.min(MAX_DIAMONDS, Number(cur.diamonds || 0) + spec.diamondDelta),
        hammerCount: Math.min(MAX_BOOSTER_STACK, Number(cur.hammerCount || 0)),
        shuffleCount: Math.min(MAX_BOOSTER_STACK, Number(cur.shuffleCount || 0)),
        refreshCount: Math.min(MAX_BOOSTER_STACK, Number(cur.refreshCount || 0)),
        wheelSpinPoints: Math.min(MAX_WHEEL_POINTS, Number(cur.wheelSpinPoints || 0) + spec.wheelDelta),
        totalSpins: Number(cur.totalSpins || 0),
        lastAdClaimAtMs: nowMs,
        createdAt: wSnap.exists ? cur.createdAt : FieldValue.serverTimestamp(),
        updatedAt: FieldValue.serverTimestamp(),
      };

      tx.set(wRef, nextWallet, { merge: true });
      tx.set(ledgerRef, {
        userId: uid,
        transactionId: txId,
        action: `AD_${rewardType}`,
        createdAt: FieldValue.serverTimestamp(),
      });
      return nextWallet;
    });

    return {
      rewardLabel: spec.label,
      wallet: sanitizeWalletResponse(resultWallet),
    };
  }
);

/**
 * Server-authoritative Lucky Wheel spin using crypto.randomInt and atomic Firestore transaction.
 */
exports.spinLuckyWheel = onCall(
  { enforceAppCheck: true, region: "us-central1" },
  async (request) => {
    const uid = requireSignedInUid(request);
    const db = getDb();
    const wRef = walletRefFor(db, uid);
    const spinRef = db.collection("users").doc(uid).collection("wheelSpins").doc();

    const roll = crypto.randomInt(0, TOTAL_WHEEL_WEIGHT);
    let cursor = 0;
    let selectedIndex = WHEEL_SEGMENTS.length - 1;
    for (let i = 0; i < WHEEL_SEGMENTS.length; i++) {
      cursor += WHEEL_SEGMENTS[i].weight;
      if (roll < cursor) {
        selectedIndex = i;
        break;
      }
    }
    const prize = WHEEL_SEGMENTS[selectedIndex];

    const outcome = await db.runTransaction(async (tx) => {
      const snap = await tx.get(wRef);
      const cur = snap.exists ? snap.data() : starterWalletDoc(uid);
      const currentPoints = Number(cur.wheelSpinPoints || 0);
      if (currentPoints < 1) {
        throw new HttpsError("failed-precondition", "Watch a Wheel Ad to earn 1 spin point first.");
      }

      let goldCoins = Number(cur.goldCoins || 0);
      let diamonds = Number(cur.diamonds || 0);
      let hammerCount = Number(cur.hammerCount || 0);
      let shuffleCount = Number(cur.shuffleCount || 0);
      let refreshCount = Number(cur.refreshCount || 0);

      if (prize.category === "GOLD") {
        goldCoins = Math.min(MAX_GOLD, goldCoins + prize.amount);
      } else if (prize.category === "DIAMONDS") {
        diamonds = Math.min(MAX_DIAMONDS, diamonds + prize.amount);
      } else if (prize.category === "HAMMER") {
        hammerCount = Math.min(MAX_BOOSTER_STACK, hammerCount + prize.amount);
      } else if (prize.category === "SHUFFLE") {
        shuffleCount = Math.min(MAX_BOOSTER_STACK, shuffleCount + prize.amount);
      } else if (prize.category === "REFRESH") {
        refreshCount = Math.min(MAX_BOOSTER_STACK, refreshCount + prize.amount);
      }

      const updated = {
        userId: uid,
        goldCoins,
        diamonds,
        hammerCount,
        shuffleCount,
        refreshCount,
        wheelSpinPoints: currentPoints - 1,
        totalSpins: Number(cur.totalSpins || 0) + 1,
        lastAdClaimAtMs: Number(cur.lastAdClaimAtMs || 0),
        createdAt: snap.exists ? cur.createdAt : FieldValue.serverTimestamp(),
        updatedAt: FieldValue.serverTimestamp(),
      };

      tx.set(wRef, updated, { merge: true });
      tx.set(spinRef, {
        userId: uid,
        segmentIndex: selectedIndex,
        prizeId: prize.id,
        prizeLabel: prize.label,
        rewardCategory: prize.category,
        rewardAmount: prize.amount,
        createdAt: FieldValue.serverTimestamp(),
      });

      return updated;
    });

    return {
      segmentIndex: selectedIndex,
      prizeId: prize.id,
      prizeLabel: prize.label,
      rewardCategory: prize.category,
      rewardAmount: prize.amount,
      wallet: sanitizeWalletResponse(outcome),
    };
  }
);

/**
 * Server-authoritative consumption of Hammer, Shuffle, or Refresh abilities.
 */
exports.consumeAbilityServer = onCall(
  { enforceAppCheck: true, region: "us-central1" },
  async (request) => {
    const uid = requireSignedInUid(request);
    const { abilityType, transactionId } = request.data || {};
    const txId = assertValidTransactionId(transactionId);

    const fieldMap = {
      hammer: "hammerCount",
      shuffle: "shuffleCount",
      refresh: "refreshCount",
    };
    const countField = fieldMap[abilityType];
    if (!countField) {
      throw new HttpsError("invalid-argument", "Invalid abilityType.");
    }

    const db = getDb();
    const wRef = walletRefFor(db, uid);
    const ledgerRef = db.collection("users").doc(uid).collection("rewardLedger").doc(txId);

    const updated = await db.runTransaction(async (tx) => {
      const [wSnap, lSnap] = await Promise.all([tx.get(wRef), tx.get(ledgerRef)]);
      if (lSnap.exists) {
        throw new HttpsError("already-exists", "Duplicate ability transaction.");
      }
      const cur = wSnap.exists ? wSnap.data() : starterWalletDoc(uid);
      const count = Number(cur[countField] || 0);
      if (count <= 0) {
        throw new HttpsError("failed-precondition", `No ${abilityType} charges remaining.`);
      }

      const nextWallet = {
        ...cur,
        userId: uid,
        [countField]: count - 1,
        createdAt: wSnap.exists ? cur.createdAt : FieldValue.serverTimestamp(),
        updatedAt: FieldValue.serverTimestamp(),
      };
      tx.set(wRef, nextWallet, { merge: true });
      tx.set(ledgerRef, {
        userId: uid,
        transactionId: txId,
        action: `CONSUME_${abilityType.toUpperCase()}`,
        createdAt: FieldValue.serverTimestamp(),
      });
      return nextWallet;
    });

    return { wallet: sanitizeWalletResponse(updated) };
  }
);

/**
 * Server-authoritative rename costing 100 Diamonds.
 */
exports.renameProfileServer = onCall(
  { enforceAppCheck: true, region: "us-central1" },
  async (request) => {
    const uid = requireSignedInUid(request);
    const { newName, transactionId } = request.data || {};
    const txId = assertValidTransactionId(transactionId);

    if (typeof newName !== "string") {
      throw new HttpsError("invalid-argument", "Display name must be a string.");
    }
    const cleanName = newName.trim().replace(/\s+/g, " ");
    if (cleanName.length < 1 || cleanName.length > 24) {
      throw new HttpsError("invalid-argument", "Name must be 1 to 24 characters.");
    }

    const db = getDb();
    const wRef = walletRefFor(db, uid);
    const userRef = db.collection("users").doc(uid);
    const lbRef = db.collection("leaderboard").doc(uid);
    const ledgerRef = db.collection("users").doc(uid).collection("rewardLedger").doc(txId);

    const updated = await db.runTransaction(async (tx) => {
      const [wSnap, lSnap, uSnap, lbSnap] = await Promise.all([
        tx.get(wRef),
        tx.get(ledgerRef),
        tx.get(userRef),
        tx.get(lbRef),
      ]);
      if (lSnap.exists) {
        throw new HttpsError("already-exists", "Duplicate rename transaction.");
      }
      const cur = wSnap.exists ? wSnap.data() : starterWalletDoc(uid);
      const diamonds = Number(cur.diamonds || 0);
      if (diamonds < RENAME_DIAMOND_COST) {
        throw new HttpsError("failed-precondition", `Need ${RENAME_DIAMOND_COST} Diamonds to rename.`);
      }

      const nextWallet = {
        ...cur,
        userId: uid,
        diamonds: diamonds - RENAME_DIAMOND_COST,
        createdAt: wSnap.exists ? cur.createdAt : FieldValue.serverTimestamp(),
        updatedAt: FieldValue.serverTimestamp(),
      };
      tx.set(wRef, nextWallet, { merge: true });
      if (uSnap.exists) {
        tx.update(userRef, { displayName: cleanName, updatedAt: FieldValue.serverTimestamp() });
      }
      if (lbSnap.exists) {
        tx.update(lbRef, { displayName: cleanName, updatedAt: FieldValue.serverTimestamp() });
      }
      tx.set(ledgerRef, {
        userId: uid,
        transactionId: txId,
        action: "RENAME_PROFILE",
        createdAt: FieldValue.serverTimestamp(),
      });
      return nextWallet;
    });

    return {
      displayName: cleanName,
      wallet: sanitizeWalletResponse(updated),
    };
  }
);

/**
 * Bounded gameplay & challenge reward credit on server with idempotency ledger.
 */
exports.creditGameplayRewardServer = onCall(
  { enforceAppCheck: true, region: "us-central1" },
  async (request) => {
    const uid = requireSignedInUid(request);
    const { goldDelta = 0, diamondDelta = 0, transactionId, source = "GAMEPLAY" } = request.data || {};
    const txId = assertValidTransactionId(transactionId);

    const safeGold = Number.isInteger(goldDelta) ? Math.max(0, Math.min(goldDelta, 500)) : 0;
    const safeDiamonds = Number.isInteger(diamondDelta) ? Math.max(0, Math.min(diamondDelta, 10)) : 0;
    if (safeGold === 0 && safeDiamonds === 0) {
      throw new HttpsError("invalid-argument", "Reward amount must be positive.");
    }

    const db = getDb();
    const wRef = walletRefFor(db, uid);
    const ledgerRef = db.collection("users").doc(uid).collection("rewardLedger").doc(txId);

    const updated = await db.runTransaction(async (tx) => {
      const [wSnap, lSnap] = await Promise.all([tx.get(wRef), tx.get(ledgerRef)]);
      if (lSnap.exists) {
        throw new HttpsError("already-exists", "Reward already credited.");
      }
      const cur = wSnap.exists ? wSnap.data() : starterWalletDoc(uid);
      const nextWallet = {
        ...cur,
        userId: uid,
        goldCoins: Math.min(MAX_GOLD, Number(cur.goldCoins || 0) + safeGold),
        diamonds: Math.min(MAX_DIAMONDS, Number(cur.diamonds || 0) + safeDiamonds),
        createdAt: wSnap.exists ? cur.createdAt : FieldValue.serverTimestamp(),
        updatedAt: FieldValue.serverTimestamp(),
      };
      tx.set(wRef, nextWallet, { merge: true });
      tx.set(ledgerRef, {
        userId: uid,
        transactionId: txId,
        action: `GAME_REWARD_${String(source).slice(0, 24)}`,
        createdAt: FieldValue.serverTimestamp(),
      });
      return nextWallet;
    });

    return { wallet: sanitizeWalletResponse(updated) };
  }
);

/**
 * Verifies a Google Play Integrity Standard token on the backend using Play Integrity API
 * and records the trusted verdict in users/{uid}/integrityAttestations/{attestationId}.
 * Requires Firebase Auth & Firebase App Check enforcement in production.
 */
exports.verifyPlayIntegrityToken = onCall(
  { enforceAppCheck: true, region: "us-central1" },
  async (request) => {
    if (!request.auth || !request.auth.uid) {
      throw new HttpsError("unauthenticated", "Google Sign-In required.");
    }

    const uid = request.auth.uid;
    const { integrityToken, nonce, attestationId } = request.data || {};
    if (
      typeof integrityToken !== "string" ||
      integrityToken.length < 20 ||
      typeof nonce !== "string" ||
      nonce.length < 16
    ) {
      throw new HttpsError("invalid-argument", "Invalid integrity payload.");
    }

    const authClient = new google.auth.GoogleAuth({
      scopes: ["https://www.googleapis.com/auth/playintegrity"],
    });
    const playintegrity = google.playintegrity({ version: "v1", auth: authClient });

    let verdictPayload;
    try {
      const response = await playintegrity.v1.decodeIntegrityToken({
        packageName: PACKAGE_NAME,
        requestBody: { integrityToken },
      });
      verdictPayload = response.data && response.data.tokenPayloadExternal;
    } catch (err) {
      throw new HttpsError("permission-denied", "Play Integrity API decode failed.");
    }

    if (!verdictPayload) {
      throw new HttpsError("permission-denied", "Missing tokenPayloadExternal.");
    }

    const reqDetails = verdictPayload.requestDetails || {};
    const appIntegrity = verdictPayload.appIntegrity || {};
    const deviceIntegrity = verdictPayload.deviceIntegrity || {};
    const accountDetails = verdictPayload.accountDetails || {};

    const nonceMatches = reqDetails.requestHash === nonce;
    const packageMatches = reqDetails.requestPackageName === PACKAGE_NAME;
    const appRecognized = appIntegrity.appRecognitionVerdict === "PLAY_RECOGNIZED";
    const deviceRecognized =
      Array.isArray(deviceIntegrity.deviceRecognitionVerdict) &&
      (deviceIntegrity.deviceRecognitionVerdict.includes("MEETS_DEVICE_INTEGRITY") ||
        deviceIntegrity.deviceRecognitionVerdict.includes("MEETS_STRONG_INTEGRITY"));

    const verified = nonceMatches && packageMatches && appRecognized && deviceRecognized;
    const docId =
      typeof attestationId === "string" && /^[a-zA-Z0-9_-]{4,64}$/.test(attestationId)
        ? attestationId
        : `server_${Date.now()}`;

    await getDb()
      .collection("users")
      .doc(uid)
      .collection("integrityAttestations")
      .doc(docId)
      .set(
        {
          userId: uid,
          nonce,
          tokenLength: Math.min(integrityToken.length, 8192),
          tokenDigest: "server_decoded",
          appVersionCode: Number(appIntegrity.versionCode || 1),
          verificationStatus: verified ? "VERIFIED_SERVER" : "REJECTED_SERVER",
          appRecognitionVerdict: appIntegrity.appRecognitionVerdict || "UNKNOWN",
          deviceRecognitionVerdict: deviceIntegrity.deviceRecognitionVerdict || [],
          appLicensingVerdict: accountDetails.appLicensingVerdict || "UNKNOWN",
          updatedAt: FieldValue.serverTimestamp(),
          createdAt: FieldValue.serverTimestamp(),
        },
        { merge: true }
      );

    return {
      verified,
      appRecognitionVerdict: appIntegrity.appRecognitionVerdict || "UNKNOWN",
      deviceRecognitionVerdict: deviceIntegrity.deviceRecognitionVerdict || [],
    };
  }
);

/**
 * AdMob Server-Side Verification (SSV) callback.
 */
exports.admobSsvCallback = onRequest(
  { region: "us-central1" },
  async (req, res) => {
    const {
      ad_network,
      ad_unit,
      custom_data,
      key_id,
      reward_amount,
      reward_item,
      timestamp,
      transaction_id,
      user_id,
      signature,
    } = req.query;

    console.log("SSV callback received", {
      ad_network,
      ad_unit,
      custom_data,
      key_id,
      reward_amount,
      reward_item,
      timestamp,
      transaction_id,
      user_id,
      hasSignature: Boolean(signature),
    });

    res.status(200).send("OK");
  }
);
