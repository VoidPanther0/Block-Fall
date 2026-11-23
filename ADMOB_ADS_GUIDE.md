# Block Falls — AdMob Ad Units & Integration Guide

This document lists **every AdMob ad placement** in **Block Falls**, formatted so you can copy each Ad Unit straight into your Google AdMob Console (`Apps → Ad units → Add ad unit`), wire `.env` / AI Studio Secrets for release builds, and inspect every reward item name and reward amount.

---

## 1. App-Level Configuration

| Field | Value / Env Key | Notes |
| --- | --- | --- |
| **Platform** | Android | Package: `com.aistudio.cascadegrid.vqmxrt` |
| **Env Key** | `ADMOB_APP_ID` | Mapped into `AndroidManifest.xml` (`com.google.android.gms.ads.APPLICATION_ID`) |
| **Debug / Test App ID** | `ca-app-pub-3940256099942544~3347511713` | Used automatically in `.env.example` and all `debug` builds |

---

## 2. All Ad Placements Summary

| # | Ad Unit Name | Ad Format | App Area / Screen | `.env` Key | Google Test Ad Unit ID | Reward Item | Reward Amount |
| :-: | :--- | :--- | :--- | :--- | :--- | :--- | :-: |
| **1** | **Gameplay Bottom Banner** | Banner (`320x50`) | Gameplay screen bottom dock (`CascadeGameScreen`) | `ADMOB_BANNER_GAMEPLAY_UNIT_ID` | `ca-app-pub-3940256099942544/6300978111` | None (Banner) | `—` |
| **2** | **Rewarded Gold +50** | Rewarded | Shop → Coins Tab → Free Gold Card (`ShopScreen`) | `ADMOB_REWARDED_GOLD_UNIT_ID` | `ca-app-pub-3940256099942544/5224354917` | `Gold` | `50` |
| **3** | **Rewarded Continue Run** | Rewarded | 5s Continue Countdown before Game Over when `score <= 10,000` (`CascadeGameScreen`) | `ADMOB_REWARDED_CONTINUE_UNIT_ID` | `ca-app-pub-3940256099942544/5224354917` | `Continue` (Board Rescue) | `1` |

---

## 3. AdMob Console Setup Cards (Copy Into "Ad unit settings")

### Placement 1 — Gameplay Bottom Banner

Replaces the bottom instruction footer on the Gameplay screen (`CascadeGameScreen`). Loads an adaptive/standard `320×50` AdMob `AdView`.

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
│ Screen location      │ Gameplay Screen (bottom dock under Shuffle/Tray) │
└──────────────────────┴──────────────────────────────────────────────────┘
```

---

### Placement 2 — Rewarded Gold +50

Triggered from the **Shop → Coins Tab → Free Gold** button (`ShopScreen`).

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

### Placement 3 — Rewarded Continue Run (5s Second-Chance Countdown)

Triggered automatically when a Classic run has **no valid moves remaining**, **score ≤ 10,000**, and the user **has not used Continue yet in that run**.
- Shows a **5-second countdown** (`5s → 1s`) before navigating to the Game Over screen.
- If `score > 10,000` (or already used once this run), the countdown is skipped and Game Over appears immediately.
- Watching the rewarded ad clears the center 4 rows of the 8×8 board, deals 3 fresh playable pieces, and resumes gameplay with the player's current score preserved.

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

## 4. How to Switch from Test IDs to Production IDs

1. Never commit real AdMob IDs to `.env.example` or source code.
2. Open the **Secrets panel in AI Studio** (or create a gitignored `.env` locally matching `.env.example`):

   ```dotenv
   ADMOB_APP_ID=ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY
   ADMOB_BANNER_GAMEPLAY_UNIT_ID=ca-app-pub-XXXXXXXXXXXXXXXX/1111111111
   ADMOB_REWARDED_GOLD_UNIT_ID=ca-app-pub-XXXXXXXXXXXXXXXX/2222222222
   ADMOB_REWARDED_CONTINUE_UNIT_ID=ca-app-pub-XXXXXXXXXXXXXXXX/3333333333
   ```

3. **Debug builds** always force Google's official Test IDs in `AdConfig.kt` to protect your AdMob account from invalid-traffic policy strikes during development.
4. **Release builds** automatically read `BuildConfig.ADMOB_*` from `.env` / Secrets and enforce `verifyReleaseAdConfig` so accidental test IDs cannot ship to Google Play.
