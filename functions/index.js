const { onCall, HttpsError } = require("firebase-functions/v2/https");
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
