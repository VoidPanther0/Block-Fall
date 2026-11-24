# Block Fall — AdMob Ad Units & Integration Guide

This document lists **every AdMob ad placement** in **Block Fall** (5 total units), formatted with the user's production AdMob IDs.

---

## 1. App-Level Configuration

| Field | Value / Env Key | Notes |
| --- | --- | --- |
| **Platform** | Android | Package: `com.milo.blockfall` |
| **Env Key** | `ADMOB_APP_ID` | Mapped into `AndroidManifest.xml` (`com.google.android.gms.ads.APPLICATION_ID`) |
| **Production App ID** | `ca-app-pub-3956806922961659~5844639339` | Configured in `.env` and `app/build.gradle.kts` |

---

## 2. All 5 Ad Placements Summary

| # | Ad Unit Name | Ad Format | App Area / Screen | `.env` / Secret Key | Production Ad Unit ID | Reward Item | Reward Amount |
| :-: | :--- | :--- | :--- | :--- | :--- | :--- | :-: |
| **1** | **Gameplay Bottom Banner** | Banner (`320x50`) | Gameplay screen bottom dock (`CascadeGameScreen`) | `ADMOB_BANNER_GAMEPLAY_UNIT_ID` | `ca-app-pub-3956806922961659/2616122603` | None (Banner) | `—` |
| **2** | **Rewarded Gold +50** | Rewarded | Shop → Free Rewards (`ShopScreen`) | `ADMOB_REWARDED_GOLD_UNIT_ID` | `ca-app-pub-3956806922961659/3967743045` | `Gold` | `50` |
| **3** | **Rewarded Continue Run** | Rewarded | 5s Continue Countdown before Game Over when `score <= 10,000` (`CascadeGameScreen`) | `ADMOB_REWARDED_CONTINUE_UNIT_ID` | `ca-app-pub-3956806922961659/1692042840` | `Continue` (Board Rescue) | `1` |
| **4** | **Rewarded Diamonds +5** | Rewarded | Shop → Free Rewards (`ShopScreen`) | `ADMOB_REWARDED_DIAMOND_UNIT_ID` | `ca-app-pub-3956806922961659/6496939128` | `Diamond` | `5` |
| **5** | **Rewarded Lucky Wheel Spin** | Rewarded | Lucky Wheel screen & Shop → Lucky Wheel (`LuckyWheelScreen`) | `ADMOB_REWARDED_WHEEL_UNIT_ID` | `ca-app-pub-3956806922961659/5922224058` | `WheelPoint` | `1` |

---

## 3. AdMob Console Setup Cards (Copy Into "Ad unit settings")

### Placement 1 — Gameplay Bottom Banner

Shown at the bottom dock of the Gameplay screen (`CascadeGameScreen`) under the Tray & Abilities bar. Loads an adaptive/standard `320×50` AdMob `AdView`.

```text
┌─────────────────────────────────────────────────────────────────────────┐
│ Ad unit settings                                                        │
├──────────────────────┬──────────────────────────────────────────────────┤
│ Ad unit name         │ Gameplay Bottom Banner                           │
├──────────────────────┼──────────────────────────────────────────────────┤
│ Ad format            │ Banner                                           │
├──────────────────────┼──────────────────────────────────────────────────┤
│ Ad unit ID (Test)    │ ca-app-pub-3940256099942544/6300978111           │
├──────────────────────┼──────────────────────────────────────────────────┤
│ Env / Secret key     │ ADMOB_BANNER_GAMEPLAY_UNIT_ID                    │
├──────────────────────┼──────────────────────────────────────────────────┤
│ Screen location      │ Gameplay Screen (bottom dock under Abilities)    │
└──────────────────────┴──────────────────────────────────────────────────┘
```

---

### Placement 2 — Rewarded Gold +50

Triggered from the **Shop → Free Rewards → Watch Ad (+50 Gold)** button (`ShopScreen`).

```text
┌─────────────────────────────────────────────────────────────────────────┐
│ Ad unit settings                                                        │
├──────────────────────┬──────────────────────────────────────────────────┤
│ Ad unit name         │ Rewarded Gold +50                                │
├──────────────────────┼──────────────────────────────────────────────────┤
│ Ad format            │ Rewarded                                         │
├──────────────────────┼──────────────────────────────────────────────────┤
│ Ad unit ID (Test)    │ ca-app-pub-3940256099942544/5224354917           │
├──────────────────────┼──────────────────────────────────────────────────┤
│ Env / Secret key     │ ADMOB_REWARDED_GOLD_UNIT_ID                      │
├──────────────────────┼──────────────────────────────────────────────────┤
│ Reward settings      │ 50 Gold   (Reward amount: 50 | Item name: Gold)  │
└──────────────────────┴──────────────────────────────────────────────────┘
```

---

### Placement 3 — Rewarded Continue Run (5s Extra-Life Cooldown)

Triggered automatically when a Classic run has **no valid moves remaining**, **score ≤ 10,000**, and the player **has not used Continue yet in that run**.
- Shows a **5-second countdown** (`5s → 1s`) before navigating to Game Over.
- Watching the rewarded ad clears the center rows of the 8×8 board, deals 3 fresh playable pieces, and resumes gameplay with the current score preserved.

```text
┌─────────────────────────────────────────────────────────────────────────┐
│ Ad unit settings                                                        │
├──────────────────────┬──────────────────────────────────────────────────┤
│ Ad unit name         │ Rewarded Continue Run                            │
├──────────────────────┼──────────────────────────────────────────────────┤
│ Ad format            │ Rewarded                                         │
├──────────────────────┼──────────────────────────────────────────────────┤
│ Ad unit ID (Test)    │ ca-app-pub-3940256099942544/5224354917           │
├──────────────────────┼──────────────────────────────────────────────────┤
│ Env / Secret key     │ ADMOB_REWARDED_CONTINUE_UNIT_ID                  │
├──────────────────────┼──────────────────────────────────────────────────┤
│ Reward settings      │ 1 Continue (Reward amount: 1 | Item: Continue)   │
└──────────────────────┴──────────────────────────────────────────────────┘
```

---

### Placement 4 — Rewarded Diamonds +5 (NEW)

Triggered from the **Shop → Free Rewards → Watch Ad (+5 Diamonds)** button (`ShopScreen`).

```text
┌─────────────────────────────────────────────────────────────────────────┐
│ Ad unit settings                                                        │
├──────────────────────┬──────────────────────────────────────────────────┤
│ Ad unit name         │ Rewarded Diamonds +5                             │
├──────────────────────┼──────────────────────────────────────────────────┤
│ Ad format            │ Rewarded                                         │
├──────────────────────┼──────────────────────────────────────────────────┤
│ Ad unit ID (Test)    │ ca-app-pub-3940256099942544/5224354917           │
├──────────────────────┼──────────────────────────────────────────────────┤
│ Env / Secret key     │ ADMOB_REWARDED_DIAMOND_UNIT_ID                   │
├──────────────────────┼──────────────────────────────────────────────────┤
│ Reward settings      │ 5 Diamond (Reward amount: 5 | Item name: Diamond)│
└──────────────────────┴──────────────────────────────────────────────────┘
```

---

### Placement 5 — Rewarded Lucky Wheel Spin +1 (NEW)

Triggered from **Lucky Wheel → Watch Ad (+1 Spin Point)** (`LuckyWheelScreen`) or **Shop → Free Rewards → Lucky Wheel Ad**. Grants `+1 WheelPoint` server-side via `claimRewardedAd` so the player can spin the server-verified Lucky Wheel once.

```text
┌─────────────────────────────────────────────────────────────────────────┐
│ Ad unit settings                                                        │
├──────────────────────┬──────────────────────────────────────────────────┤
│ Ad unit name         │ Rewarded Lucky Wheel Spin                        │
├──────────────────────┼──────────────────────────────────────────────────┤
│ Ad format            │ Rewarded                                         │
├──────────────────────┼──────────────────────────────────────────────────┤
│ Ad unit ID (Test)    │ ca-app-pub-3940256099942544/5224354917           │
├──────────────────────┼──────────────────────────────────────────────────┤
│ Env / Secret key     │ ADMOB_REWARDED_WHEEL_UNIT_ID                     │
├──────────────────────┼──────────────────────────────────────────────────┤
│ Reward settings      │ 1 WheelPoint (Amount: 1 | Item: WheelPoint)      │
└──────────────────────┴──────────────────────────────────────────────────┘
```

---

## 4. Secret Keys & Release Build Setup

1. Never commit real AdMob IDs into `.env.example` or Kotlin source files.
2. Add **all 6 real secrets** in the **Secrets panel in AI Studio** (and GitHub Actions Secrets for CI release APKs):

   ```dotenv
   ADMOB_APP_ID=ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY
   ADMOB_BANNER_GAMEPLAY_UNIT_ID=ca-app-pub-XXXXXXXXXXXXXXXX/1111111111
   ADMOB_REWARDED_GOLD_UNIT_ID=ca-app-pub-XXXXXXXXXXXXXXXX/2222222222
   ADMOB_REWARDED_CONTINUE_UNIT_ID=ca-app-pub-XXXXXXXXXXXXXXXX/3333333333
   ADMOB_REWARDED_DIAMOND_UNIT_ID=ca-app-pub-XXXXXXXXXXXXXXXX/4444444444
   ADMOB_REWARDED_WHEEL_UNIT_ID=ca-app-pub-XXXXXXXXXXXXXXXX/5555555555
   ```

3. **Debug builds** always force Google's official Test IDs in `AdConfig.kt` to protect your AdMob account from invalid-traffic strikes during development.
4. **Release builds** automatically read `BuildConfig.ADMOB_*` injected by the Secrets Gradle Plugin (`.env` / Secrets) and run `verifyReleaseAdConfig` so accidental test IDs cannot ship to Google Play.

---

## 5. `app-ads.txt` Publisher Verification

Authorized Sellers for Apps (`app-ads.txt`) prevents unauthorized ad inventory spoofing and maximizes bidder fill/revenue.

- **Required Entry** (already added in `/app-ads.txt` and `/hosting/app-ads.txt`):

  ```text
  google.com, pub-3956806922961659, DIRECT, f08c47fec0942fa0
  ```

- **Publish via Firebase Hosting**:
  1. Deploy the `hosting/` directory (configured in `firebase.json`):
     ```sh
     firebase deploy --only hosting
     ```
  2. Verify the live URL responds with the exact line above:
     `https://gen-lang-client-0120665296.web.app/app-ads.txt`
  3. Set `https://gen-lang-client-0120665296.web.app` as the **Developer Website** in **Google Play Console → Store listing** (or upload `app-ads.txt` to your custom domain root matching your Play listing).
  4. Wait at least **24 hours** for AdMob to crawl and verify the file, then check **AdMob Console → Apps → View all apps → app-ads.txt**.

