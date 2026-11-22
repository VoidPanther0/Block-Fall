const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validProfilePayload(uid, overrides = {}) {
  const past = new Date(Date.now() - 5000);
  return {
    userId: uid,
    displayName: "BlockMaster",
    classicBestScore: 1200,
    unlockedChallengeLevel: 3,
    totalGamesPlayed: 12,
    totalLinesCleared: 48,
    totalCascadesTriggered: 9,
    maxCascadeDepth: 4,
    totalBlocksPlaced: 180,
    fullBoardClears: 1,
    blockStyleId: "modern",
    boardPaletteId: "deep_ocean",
    soundEnabled: true,
    musicEnabled: true,
    hapticsEnabled: true,
    visualEffectsEnabled: true,
    placementPreviewEnabled: true,
    createdAt: past,
    updatedAt: past,
    ...overrides,
  };
}

function validChallengePayload(levelNumber = 1, overrides = {}) {
  const past = new Date(Date.now() - 5000);
  return {
    levelNumber,
    bestScore: 220,
    stars: 3,
    completed: true,
    createdAt: past,
    updatedAt: past,
    ...overrides,
  };
}

function validAttestationPayload(uid, overrides = {}) {
  const past = new Date(Date.now() - 5000);
  return {
    userId: uid,
    nonce: "nonce_0123456789abcdef_xyz",
    tokenLength: 420,
    tokenDigest: "e3b0c44298fc1c149afbf4c8996fb924",
    appVersionCode: 1,
    verificationStatus: "PENDING_SERVER_VERIFICATION",
    createdAt: past,
    updatedAt: past,
    ...overrides,
  };
}

function validLeaderboardPayload(uid, overrides = {}) {
  const past = new Date(Date.now() - 5000);
  return {
    userId: uid,
    displayName: "BlockMaster",
    visibility: "public",
    classicBestScore: 1200,
    maxCascadeDepth: 4,
    rankTitle: "Cascade Tactician",
    createdAt: past,
    updatedAt: past,
    ...overrides,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated users cannot read or write any collection", async () => {
  const unauth = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauth.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauth.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID))
  );
  await assertFails(
    unauth.collection("leaderboard").where("visibility", "==", "public").get()
  );
  await assertFails(
    unauth
      .collection("leaderboard")
      .doc(ALICE_UID)
      .set(validLeaderboardPayload(ALICE_UID))
  );
});

test("Owner can create and monotonically update their UserCloudProfile; cross-user & rollback fail", async () => {
  const alice = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bob = testEnv.authenticatedContext(BOB_UID).firestore();
  const aliceRef = alice.collection("users").doc(ALICE_UID);

  await assertSucceeds(aliceRef.set(validProfilePayload(ALICE_UID)));
  await assertSucceeds(aliceRef.get());

  // Valid partial update with higher score
  await assertSucceeds(
    aliceRef.update({
      classicBestScore: 1850,
      unlockedChallengeLevel: 4,
      blockStyleId: "retro",
      boardPaletteId: "sunset",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject score rollback
  await assertFails(
    aliceRef.update({
      classicBestScore: 500,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject ghost field / extra attribute
  await assertFails(
    aliceRef.update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject createdAt tamper
  await assertFails(
    aliceRef.update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject future client timestamp
  await assertFails(
    bob
      .collection("users")
      .doc(BOB_UID)
      .set(
        validProfilePayload(BOB_UID, {
          updatedAt: new Date(Date.now() + 60000),
        })
      )
  );

  // Reject cross-user read or write
  await assertFails(bob.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bob.collection("users").doc(ALICE_UID).update({ displayName: "Hijacked" })
  );
});

test("Owner challengeProgress subcollection CRUD obeys monotonic score/stars and rejects @DocumentId injection", async () => {
  const alice = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bob = testEnv.authenticatedContext(BOB_UID).firestore();
  const levelRef = alice
    .collection("users")
    .doc(ALICE_UID)
    .collection("challengeProgress")
    .doc("level_1");

  // Empty subcollection list works without undefined-property errors
  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).collection("challengeProgress").get()
  );

  await assertSucceeds(levelRef.set(validChallengePayload(1)));

  // Reject extra `id` field (@DocumentId parity guard)
  await assertFails(
    alice
      .collection("users")
      .doc(ALICE_UID)
      .collection("challengeProgress")
      .doc("level_2")
      .set(validChallengePayload(2, { id: "level_2" }))
  );

  // Monotonic star/score upgrade succeeds
  await assertSucceeds(
    levelRef.update({
      bestScore: 310,
      stars: 3,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject star downgrade
  await assertFails(
    levelRef.update({
      stars: 1,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Cross-user denied
  await assertFails(
    bob
      .collection("users")
      .doc(ALICE_UID)
      .collection("challengeProgress")
      .get()
  );
});

test("Play Integrity attestations allow PENDING submission only and forbid client self-verification", async () => {
  const alice = testEnv.authenticatedContext(ALICE_UID).firestore();
  const attCol = alice
    .collection("users")
    .doc(ALICE_UID)
    .collection("integrityAttestations");

  await assertSucceeds(
    attCol.doc("att_1").set(validAttestationPayload(ALICE_UID))
  );

  // Client cannot self-mark VERIFIED_SERVER
  await assertFails(
    attCol
      .doc("att_2")
      .set(
        validAttestationPayload(ALICE_UID, {
          verificationStatus: "VERIFIED_SERVER",
        })
      )
  );

  // Client cannot mutate existing attestation
  await assertFails(
    attCol.doc("att_1").update({ verificationStatus: "VERIFIED_SERVER" })
  );
});

test("Public leaderboard enforces query filter visibility=='public' and owner write isolation", async () => {
  const alice = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bob = testEnv.authenticatedContext(BOB_UID).firestore();

  // Reading non-existent own doc succeeds safely (resource == null guard)
  await assertSucceeds(alice.collection("leaderboard").doc(ALICE_UID).get());

  await assertSucceeds(
    alice
      .collection("leaderboard")
      .doc(ALICE_UID)
      .set(validLeaderboardPayload(ALICE_UID))
  );

  // Matches Kotlin query: whereEqualTo("visibility", "public")
  await assertSucceeds(
    bob.collection("leaderboard").where("visibility", "==", "public").get()
  );

  // Unfiltered top-level query fails
  await assertFails(bob.collection("leaderboard").get());

  // Bob cannot overwrite Alice's leaderboard document
  await assertFails(
    bob
      .collection("leaderboard")
      .doc(ALICE_UID)
      .set(validLeaderboardPayload(BOB_UID))
  );

  // Alice can upgrade her own leaderboard score
  await assertSucceeds(
    alice
      .collection("leaderboard")
      .doc(ALICE_UID)
      .update({
        classicBestScore: 2400,
        updatedAt: new Date(Date.now() - 500),
      })
  );
});
