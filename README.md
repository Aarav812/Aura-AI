# Aura AI ✨

A production-grade Android AI assistant built with **Kotlin**, **Jetpack Compose (Material 3)**, **MVVM + Clean Architecture**, **Hilt**, **Coroutines/Flow**, **Room**, **DataStore**, **WorkManager**, **Firebase**, and the **NVIDIA NIM** inference API.

Premium, Apple-inspired UI: soft lavender backgrounds, glassmorphism, gradient buttons, a floating navigation bar, spring animations, streaming token rendering, and full dark mode with Material You dynamic color.

---

## ✨ Features

| Area | What's implemented |
|------|--------------------|
| **Auth** | Firebase Email/Password, Google Sign-In (Credential Manager), Anonymous/Guest, Forgot Password, Logout, Delete Account, persisted + auto-restored session |
| **AI** | NVIDIA NIM (OpenAI-compatible) client, **SSE streaming**, reasoning capture, retry, cancellation, timeouts, client-side rate limiting, token counting, context windowing, error mapping |
| **Chat** | Markdown + code blocks with copy, tables, streaming cursor, typing indicator, regenerate, copy, like/dislike, delete, TTS playback, speech-to-text, image attach, auto-title, auto-scroll, model selector |
| **History** | Room storage, search (chats + messages), rename, duplicate, archive, pin, favorite, delete, swipe actions, offline viewing, auto-save |
| **Screens** | Home, Chat, Explore (prompt catalog), Library (grid/list + filters), Settings, Global Search, Profile, Voice mode |
| **Prefs** | DataStore-backed: theme, dynamic color, model, temperature, top-p, max tokens, streaming, memory, reasoning, internet, voice, TTS, response style, system prompt, high contrast |
| **Platform** | WorkManager daily reminders, connectivity-aware offline UI, Crashlytics + Analytics ready |
| **Quality** | Unit tests (streaming parser, rate limiter, token counter, prompt manager, repository), Compose UI tests, Hilt test runner |

---

## 🏗 Architecture

```
presentation/  →  ViewModels + Compose screens (UiState, StateFlow)
domain/        →  Models, Repository interfaces, Use Cases   (pure Kotlin)
data/          →  Repository impls, Room, Retrofit, DataStore, mappers
core/          →  Resource wrapper, AppError, ErrorMapper, theme, reusable UI
di/            →  Hilt modules (Network, Database, Firebase, Repository)
navigation/    →  NavHost + floating nav bar
work/          →  WorkManager workers & scheduler
utils/         →  Connectivity, TokenCounter, RateLimiter
```

Unidirectional data flow: `UI → ViewModel (intent) → UseCase/Repository → StateFlow → UI`.
All cross-layer results are wrapped in `Resource<T>` / `AppError`; nothing throws across boundaries.

---

## 🚀 Setup

### 1. Prerequisites
- Android Studio **Ladybug** (or newer)
- JDK 17
- Android SDK 34

### 2. Clone & open
Open the project root in Android Studio and let Gradle sync.

### 3. Configure secrets — `local.properties`
Copy the template and fill in your keys (this file is git-ignored):

```bash
cp local.properties.template local.properties
```

```properties
sdk.dir=/path/to/Android/sdk
NVIDIA_API_KEY=nvapi-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
NVIDIA_BASE_URL=https://integrate.api.nvidia.com/
GOOGLE_WEB_CLIENT_ID=xxxxxx-xxxx.apps.googleusercontent.com
```

> 🔑 Get a free NVIDIA key at **https://build.nvidia.com** → *Get API Key*.
> The key is injected as a `BuildConfig` field and attached by an OkHttp interceptor — **never hardcoded** in source.

### 4. Firebase
1. Create a Firebase project → add an Android app with package `com.aura.ai`
   (debug variant is `com.aura.ai.debug` — add both SHA-1s for Google Sign-In).
2. Enable **Authentication** providers: Email/Password, Google, Anonymous.
3. Download `google-services.json` into `app/`.
4. Un-comment the Firebase Gradle plugins in `app/build.gradle.kts`:
   ```kotlin
   alias(libs.plugins.google.services)
   alias(libs.plugins.firebase.crashlytics)
   ```
5. Copy the **Web client ID** (Auth → Sign-in → Google → Web SDK config) into `GOOGLE_WEB_CLIENT_ID`.
6. Apply the Firestore rules in `firebase/firestore.rules`.

### 5. Run
```bash
./gradlew :app:assembleDebug        # build
./gradlew :app:testDebugUnitTest    # unit tests
```
Or hit ▶ in Android Studio.

> The app **builds and runs without Firebase** for local UI work (guest mode + local chats),
> but AI responses require a valid `NVIDIA_API_KEY`, and Google/Email auth require `google-services.json`.

---

## 🧪 Testing
```bash
./gradlew testDebugUnitTest              # JVM unit tests
./gradlew connectedDebugAndroidTest      # instrumented + Compose UI tests
```

---

## 🔐 Security notes
- No API keys in source or VCS — read from `local.properties` / env → `BuildConfig`.
- HTTPS-only; auth token added via interceptor.
- Firestore locked to the authenticated user (see rules).
- `google-services.json`, `local.properties`, keystores are git-ignored.

---

## 🎨 Swapping models
Edit `domain/model/AiModel.kt` to add/remove NIM models — they appear automatically in the model selector and settings.

## 📁 Where things live
- NVIDIA client: `data/remote/NvidiaApiService.kt`, `AiRepositoryImpl.kt`
- Streaming SSE parser: `data/remote/streaming/StreamingParser.kt`
- Prompt/system-prompt builder: `data/remote/PromptManager.kt`
- Chat storage: `data/local/` + `ChatRepositoryImpl.kt`
- Preferences: `data/preferences/PreferencesRepositoryImpl.kt`
- Theme: `core/ui/theme/`
- Reusable components: `core/ui/components/`
