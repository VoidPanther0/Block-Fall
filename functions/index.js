const { onCall, onRequest, HttpsError } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");
const { getFirestore, FieldValue } = require("firebase-admin/firestore");
const { google } = require("googleapis");

admin.initializeApp();

const DATABASE_ID = "ai-studio-android-blockfal-1b3ea5ab-3835-4dbd-8287-42466096ac30";
const PACKAGE_NAME = "com.aistudio.cascadegrid.vqmxrt";

function getDb() {
  return getFirestore(DATABASE_ID);
}

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
 * AdMob Server-Side Verification (SSV) callback — STUB.
 *
 * Google calls this HTTPS endpoint after a user earns a rewarded-ad reward. Configure the
 * SSV callback URL (with an optional custom_data value identifying the user) in the AdMob
 * console.
 *
 * SECURITY: This stub does not yet verify Google's ECDSA signature, so it MUST NOT be used
 * to credit currency in production. Until signature verification and replay protection are
 * implemented, it only acknowledges the callback so Google does not retry.
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

    // TODO: 1. Fetch Google's public keys from
    //          https://gstatic.com/admob/reward/verifier-keys.json
    // TODO: 2. Rebuild the signed query string and verify the ECDSA signature with key_id.
    // TODO: 3. Reject stale timestamps and check transaction_id uniqueness in Firestore
    //          (replay protection).
    // TODO: 4. Credit gold to the authenticated player via a Firestore atomic transaction.
    // TODO: 5. Append the reward to a server-side reward ledger for auditing.

    res.status(200).send("OK");
  }
);
