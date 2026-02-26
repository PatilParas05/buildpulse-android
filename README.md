# ⚡ BuildPulse Android

> **📚 Learning Project** — This plugin was built for learning purposes to understand how Gradle plugins work internally. It is **not published to the Gradle Plugin Portal** and is not intended for production use. May be in Future

---

A custom **Gradle Plugin** for Android projects that tracks, compares, and reports build performance metrics — including total build time, per-module durations, per-task durations, and regressions between builds.

---

## 🧠 What is a Gradle Plugin?

A **Gradle Plugin** is a build-time tool — it runs *during your build process*, not inside your app. It never becomes part of your APK. It hooks into Gradle's lifecycle to observe, measure, or modify how your project is built.

This is fundamentally different from a regular Android library:

| | Regular Library (e.g. Retrofit) | Gradle Plugin (e.g. BuildPulse) |
|---|---|---|
| Added via | `implementation(...)` in `build.gradle.kts` | `id(...)` in `plugins {}` block |
| Runs | Inside your app, on a device | During the build, on your machine |
| Part of APK? | ✅ Yes | ❌ No |
| Examples | Coil, Room, OkHttp | Hilt, KSP, BuildPulse |

---

## ✨ Features

- ⏱️ **Total build time tracking** — measures how long your entire build takes
- 📦 **Module-level breakdown** — see which modules (`:app`, `:feature-login`, `:core-network`) are slowest
- 🔧 **Task-level breakdown** — drill down into individual Gradle tasks
- 📊 **Regression detection** — compares current build vs previous build and flags slowdowns
- 🚨 **CI enforcement** — optionally fail the build if a regression exceeds your threshold
- 🌐 **HTML report** — generates a beautiful visual report at `buildpulse/build-metrics.html`
- 💾 **Metrics persistence** — saves metrics to `buildpulse/build-metrics.json` for comparison across builds

---

## 🏗️ Project Structure

```
buildpulse-android/
├── plugin/                        # The Gradle plugin source
│   └── src/main/kotlin/
│       └── dev/paraspatil/buildpulse/
│           ├── BuildPulsePlugin.kt        # Plugin entry point
│           ├── BuildPulseExtension.kt     # DSL configuration
│           ├── TaskTimingListener.kt      # Hooks into Gradle task lifecycle
│           ├── MetricsCollector.kt        # Aggregates timing data
│           ├── MetricsStore.kt            # Saves/loads metrics as JSON
│           ├── MetricsComparator.kt       # Diffs current vs previous build
│           ├── ReportGenerator.kt         # Console report
│           ├── HtmlReportGenerator.kt     # HTML report
│           ├── CIEnforcer.kt              # Fails build on regression
│           └── model/
│               └── BuildMetrics.kt        # Data models
├── app/                           # Sample Android app using the plugin
├── feature-login/                 # Sample feature module
├── core-network/                  # Sample core module
└── buildpulse/                    # Generated output (after build)
    ├── build-metrics.json
    └── build-metrics.html
```

---

## ⚙️ How It Works

1. `BuildPulsePlugin` registers a `TaskExecutionListener` via `TaskTimingListener`
2. Before each task runs, it records a start timestamp
3. After each task completes, it calculates the elapsed time
4. When the build finishes (`gradle.buildFinished`), `MetricsCollector` rolls up task times into module totals
5. `MetricsStore` loads the previous build's metrics from JSON (if it exists)
6. `MetricsComparator` diffs the two builds to find regressions and improvements
7. `ReportGenerator` prints a summary to the console
8. `HtmlReportGenerator` writes a visual report to disk
9. `CIEnforcer` optionally throws a `GradleException` if regressions exceed the threshold

---

## 🚀 Usage (Within This Repo)

This plugin is used via **composite build** (`includeBuild`) — meaning the plugin and the sample app live in the same repository and Gradle resolves the plugin locally without any publishing step.

In `settings.gradle.kts` (root):
```kotlin
includeBuild("plugin")  // tells Gradle to use the local plugin source
```

In root `build.gradle.kts`:
```kotlin
plugins {
    id("dev.paraspatil.buildpulse-android")
}

buildPulse {
    enabled = true
    trackTasks = true
    trackModules = true
    failOnRegression = false
    maxAllowedIncreaseMs = 500L
    outputDir = "buildpulse"
    generatedHtmlReport = true
}
```

---

## ⚙️ Configuration Options

| Option | Type | Default | Description |
|---|---|---|---|
| `enabled` | `Boolean` | `true` | Enable or disable the plugin entirely |
| `trackTasks` | `Boolean` | `true` | Track individual task durations |
| `trackModules` | `Boolean` | `true` | Roll up task times into module totals |
| `failOnRegression` | `Boolean` | `false` | Fail the build if a regression is detected |
| `maxAllowedIncreaseMs` | `Long` | `500` | Threshold in ms above which a slowdown is a regression |
| `outputDir` | `String` | `"buildpulse"` | Directory where JSON and HTML reports are written |
| `generatedHtmlReport` | `Boolean` | `true` | Whether to generate the HTML visual report |

---

## ▶️ How to See the Output

No special commands needed — BuildPulse runs **automatically every time you build**.

**Step 1 — Assemble or Rebuild your project:**

In Android Studio, go to the top menu:
```
Build → Assemble/Rebuild Project
```
Or run from terminal:
```bash
./gradlew assembleDebug
```

**Step 2 — Check the console output:**

Once the build finishes, scroll up in the Build output / terminal and you'll see the BuildPulse summary printed automatically:

```
╔═══════════════════════════════════════════════════╗
║          📊  BuildPulse — Metrics Summary         ║
╚═══════════════════════════════════════════════════╝

Total build time: 12.4 seconds (+1200 ms ⚠️ regression)

Module Breakdown (sorted by time ↓)
  ─────────────────────────────────────
     app                    8,210 ms   (+900 ms ⚠️ regression)
     core-network           2,540 ms   (-120 ms ✅)
     feature-login          1,650 ms   (no change)

═══════════════════════════════════════════════════════
```

> 💡 The **first build** will say `first run` since there's no previous build to compare against. **Build a second time** and you'll start seeing diffs, regressions, and improvements.

**Step 3 — Open the HTML report:**

After the build, a report file is generated at:
```
{your-project-root}/buildpulse/build-metrics.html
```

In Android Studio, find it in the **Project** panel on the left:
```
buildpulse/
  ├── build-metrics.json   ← raw metrics data from last build
  └── build-metrics.html   ← open this in your browser ✅
```

Right-click `build-metrics.html` → **Open In** → **Browser** and you'll see the full visual report with module breakdowns, progress bars, and REGRESSION / IMPROVED / NEW badges.

---

## 🌐 HTML Report

After each build, an interactive HTML report is generated at `buildpulse/build-metrics.html` showing:
- Total build time with diff from previous build
- Module count and task count
- Regression count
- Per-module progress bars with REGRESSION / IMPROVED / NEW badges

---

## ⚠️ Why You Can't Use This via `implementation()`

A very common mistake is trying to add this as a regular library dependency:

```kotlin
// ❌ This is WRONG — will not work
implementation("com.github.PatilParas05:buildpulse-android:0.1.6")
```

This fails because BuildPulse is a **Gradle plugin**, not an Android library. It has no Android classes, no Activities, and no UI. It only uses Gradle APIs (`Plugin<Project>`, `TaskExecutionListener`, etc.) which are completely separate from the Android SDK.

---

## ⚠️ Why You Can't Use This via JitPack Plugin ID

```kotlin
// ❌ This will also fail
id("dev.paraspatil.buildpulse-android") version "0.1.6"  // with JitPack in pluginManagement
```

Gradle plugin resolution requires a special **plugin marker artifact** (`pluginId:pluginId.gradle.plugin:version`). JitPack does not auto-generate these markers, so Gradle cannot resolve the plugin by ID from JitPack.

To use it from an **external project**, you need the `resolutionStrategy` workaround in `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "dev.paraspatil.buildpulse-android") {
                useModule("com.github.PatilParas05:buildpulse-android:0.1.6")
            }
        }
    }
}
```

---

## 🛠️ Built With

- **Kotlin** — plugin source language
- **Gradle Plugin API** — `Plugin<Project>`, `TaskExecutionListener`
- **No external dependencies** — pure Gradle + Kotlin stdlib
- **JitPack** — for distribution (via composite build or resolutionStrategy)

---

## 📚 What I Learned Building This

- How Gradle plugins work internally and how they differ from Android libraries
- How `TaskExecutionListener` hooks into the build lifecycle
- How composite builds (`includeBuild`) allow local plugin development without publishing
- How plugin marker artifacts work and why JitPack alone isn't enough for plugin resolution
- How to serialize/deserialize build metrics manually (without a JSON library)
- How to generate HTML reports from Kotlin strings

---

## 📌 Note on Publishing

This plugin is **not published to the Gradle Plugin Portal**. If it were, it would be available to anyone with just:

```kotlin
// This would work if published to plugins.gradle.org
plugins {
    id("dev.paraspatil.buildpulse-android") version "0.1.6"
}
```

Publishing to the Gradle Plugin Portal requires registering at [plugins.gradle.org](https://plugins.gradle.org) and using the `com.gradle.plugin-publish` plugin. That step was intentionally skipped as this is a learning project.

---

## 👨‍💻 Author

**Paras Patil** — [@PatilParas05](https://github.com/PatilParas05)

---

> ⭐ If you found this helpful for learning how Gradle plugins work, feel free to star the repo!

<img src="https://raw.githubusercontent.com/PatilParas05/buildpulse-android/main/builld.png" alt="BuildPulse-Report" height="50" width="5"/>
