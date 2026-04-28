# SathiX — Native Android (WhatsApp-style) Starter

Production-grade Android starter project with Clean Architecture (MVVM + Repository), feature modules, Hilt DI, Room offline cache, Firebase (Auth/RTDB/FCM/Storage), Cloudinary media, WebRTC audio/video calls, and GitHub Actions APK build.

## What's inside

```
SathiX/
├── .github/workflows/android-build.yml   # CI: builds debug + release APKs
├── app/
│   ├── build.gradle.kts                   # Module build (BuildConfig fields, signing)
│   ├── google-services.json               # Firebase config (callingapp1-f699d)
│   └── src/main/
│       ├── AndroidManifest.xml            # All permissions + services
│       └── java/com/sathix/app/
│           ├── SathiXApp.kt               # @HiltAndroidApp + notif channels + Firebase init
│           ├── core/                      # encryption, network monitor, media compressor, configs
│           ├── data/                      # Room (entities/dao), Firebase data sources, repositories
│           ├── domain/                    # Models, repository interfaces
│           ├── features/                  # auth, chat, calls, status, groups, community, channels, profile
│           ├── services/                  # FCM, Call foreground, Sync, BootReceiver
│           └── di/                        # Hilt modules
├── build.gradle.kts                       # Project plugins
├── settings.gradle.kts                    # Module include
├── gradle.properties                      # Build flags
└── local.properties.example               # Copy to local.properties and fill in
```

## Features included

- Phone number auth (Firebase OTP)
- Real-time chat (Firebase Realtime DB + Room offline cache + DiffUtil RecyclerView)
- Message states: pending, sent, delivered, seen, failed (with auto-retry)
- Typing indicator + presence (online / last seen) with onDisconnect handlers
- 1-to-1 audio + video calls (WebRTC + Socket.IO signaling) with foreground service & lock-screen UI
- Status / Story (24h auto-expire, view tracking)
- Groups (owner/admin/member roles, invite links)
- Communities & Channels scaffolding
- FCM push (messages + high-priority call notifications with full-screen intent)
- Cloudinary signed media upload (image compression before upload)
- AES-GCM message encryption helper
- Network adaptive (NetworkMonitor with quality buckets)
- Multi-device session via Firebase Auth + FCM token registry
- ProGuard/R8 enabled with rules for WebRTC, Firebase, Room, Socket.IO

## Prereqs

- Android Studio Hedgehog or newer
- JDK 17
- Android SDK 34
- Min SDK 24

## Local setup

1. Clone this repo.
2. Copy config:
   ```bash
   cp local.properties.example local.properties
   ```
3. Open `local.properties` and fill in:
   - `sdk.dir` → your Android SDK path
   - `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET`
   - `SIGNALING_SERVER_URL` (your Render-hosted Socket.IO server)
   - `STUN_SERVER` / `TURN_SERVER` / `TURN_USERNAME` / `TURN_PASSWORD`
4. Open in Android Studio → Sync → Run.

## GitHub Actions — auto-build APK

- The workflow at `.github/workflows/android-build.yml` builds debug + release APKs on every push to `main`/`master`/`dev`, and on PRs.
- APKs are uploaded as workflow artifacts you can download from the Actions tab.
- Tag a commit `v1.0.0` to publish a GitHub Release with the APK files attached.

### Required GitHub repo secrets

In your repo → **Settings → Secrets and variables → Actions**, add:

| Secret | Required | Notes |
|---|---|---|
| `CLOUDINARY_CLOUD_NAME` | optional | defaults to `dvqqgqdls` |
| `CLOUDINARY_API_KEY` | recommended | from Cloudinary console |
| `CLOUDINARY_API_SECRET` | recommended | from Cloudinary console |
| `SIGNALING_SERVER_URL` | recommended | your Render Socket.IO URL |
| `STUN_SERVER` | optional | defaults to Google STUN |
| `TURN_SERVER` | optional but recommended for production | TURN URL |
| `TURN_USERNAME` / `TURN_PASSWORD` | optional | TURN creds |
| `SIGNING_KEYSTORE` | required for signed release | base64 of release keystore |
| `KEYSTORE_PASSWORD` / `KEY_ALIAS` / `KEY_PASSWORD` | required for signed release | keystore creds |

To base64 a keystore on macOS/Linux:
```bash
base64 -i release.keystore | tr -d '\n' | pbcopy
```

## Backend (Render) — what you still need

Create a Node.js service on Render that exposes:

1. **Socket.IO signaling** — events: `join`, `offer`, `answer`, `ice`, `hangup` (room-based).
2. **Cloudinary signed upload endpoint** — POST `/upload-signature` returns `{ signature, timestamp, api_key, cloud_name }` for client signed uploads.
3. **(Optional) Push relay** — POST `/sendPush` to send FCM data messages.

Set `SIGNALING_SERVER_URL` to your Render URL (e.g. `https://sathix-signaling.onrender.com`).

## Firebase setup checklist

1. Firebase Console → Authentication → enable **Phone** provider.
2. Add SHA-1 of your debug + release keystores to the Android app config.
3. Realtime Database rules — start with the secure template:
   ```json
   {
     "rules": {
       "users": { "$uid": { ".read": "auth != null", ".write": "auth.uid === $uid" } },
       "messages": { "$chatId": { ".read": "auth != null", ".write": "auth != null" } },
       "presence": { "$uid": { ".read": "auth != null", ".write": "auth.uid === $uid" } },
       "typing":   { ".read": "auth != null", ".write": "auth != null" },
       "calls":    { ".read": "auth != null", ".write": "auth != null" },
       "statuses": { "$uid": { ".read": "auth != null", ".write": "auth.uid === $uid" } },
       "groups":   { ".read": "auth != null", ".write": "auth != null" },
       "fcmTokens":{ "$uid": { ".read": "auth != null", ".write": "auth.uid === $uid" } }
     }
   }
   ```
4. Cloud Messaging — auto-enabled for Android.

## Security note

The Cloudinary **API secret should never ship inside the APK** in production. The starter accepts it via `local.properties` for development convenience only. For release builds, switch to **server-signed uploads** via your Render backend (the recommended setup above).

The Firebase Web API key in `google-services.json` is fine to ship — it's a client identifier, not a secret. Restrict it via Firebase rules + SHA pinning instead.

Rotate the Cloudinary secret if it has been exposed.

## Architecture quick reference

- **MVVM**: `ViewModel` exposes `StateFlow` consumed by `Activity`/`Fragment` via `lifecycleScope`.
- **Repository pattern**: domain interfaces live in `domain/repository`, implementations in `data/repository`, bound via Hilt `@Binds`.
- **Offline-first**: Room is the source of truth for chat list & messages; Firebase listeners push updates into Room and the UI reactively renders Room data.
- **Coroutines**: all async I/O on `Dispatchers.IO` via repositories. Never blocking UI thread.
- **DiffUtil + ListAdapter**: zero full-list rebinds on chat list / messages.
- **ViewBinding**: every layout, no `findViewById`.
- **Hilt**: constructor injection everywhere; modules in `di/`.

## Roadmap items left as TODO

These hooks exist; wire them into your UI as you grow:
- Cloudinary upload UI flow (`CloudinaryConfig.init` already done — call `MediaManager.get().upload(...)` from a viewmodel).
- Group call (multi-peer WebRTC) — extend `WebRTCManager` with multiple `PeerConnection` instances.
- Multi-device sessions — read FCM tokens from `fcmTokens/$uid`; fan-out from your backend.
- Crashlytics + Performance monitoring — already in dependencies; nothing else needed.
- Message edit time-limit — `editMessage` exists in repo; enforce 15-min window in the UI layer.
- Read-receipt privacy toggle — add a flag to `User` and gate `markSeen`.
