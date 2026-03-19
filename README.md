# ⚡ BuildPulse Android

[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/io.github.PatilParas05.buildpulse-android?color=blue&label=Gradle%20Plugin)](https://plugins.gradle.org/plugin/io.github.PatilParas05.buildpulse-android)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-purple.svg)](https://kotlinlang.org/)

A powerful **Gradle Plugin** for Android projects that tracks, compares, and reports build performance metrics — including total build time, per-module durations, per-task durations, and regressions between builds.

**Never let your build times creep up again.** 🚀

![BuildPulse Report](https://raw.githubusercontent.com/PatilParas05/buildpulse-android/main/builld.png)

---

## 🧠 What is a Gradle Plugin?

A **Gradle Plugin** is a build-time tool — it runs *during your build process*, not inside your app. It never becomes part of your APK. It hooks into Gradle's lifecycle to observe, measure, or modify how your project is built.

This is fundamentally different from a regular Android library:

| | Regular Library (e.g. Retrofit) | Gradle Plugin (e.g. BuildPulse) |
|---|---|---|
| Added via | `implementation(...)` in module's `build.gradle.kts` | `id(...)` in root `plugins {}` block |
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
- 🎯 **Zero dependencies** — pure Gradle + Kotlin stdlib, no external libraries

---

## 📦 Installation

### Using Gradle Plugin Portal (Recommended)

Add the plugin to your **root `build.gradle.kts`**:
```kotlin
plugins {
    id("io.github.PatilParas05.buildpulse-android") version "1.0.0"   // ← Add this
}
```

**That's it!** Sync your project and run a build.

### Check Latest Version

Always use the latest version from the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/io.github.PatilParas05.buildpulse-android).

---

## 🚀 Usage

BuildPulse runs **automatically** every time you build your project. No special commands needed.

### First Build (Creates Baseline)
```bash
./gradlew assembleDebug
```

**Console Output:**
```
╔═══════════════════════════════════════════════════╗
║          📊  BuildPulse — Metrics Summary         ║
╚═══════════════════════════════════════════════════╝

Total build time: 25.3s (first run)

Module Breakdown (sorted by time ↓)
  ─────────────────────────────────────
    app                    40,056 ms   
    core-network           22,347 ms   
    feature-login          16,987 ms   

═══════════════════════════════════════════════════════
```

### Second Build (Shows Diffs)
```bash
./gradlew assembleDebug
```

**Console Output:**
```
╔═══════════════════════════════════════════════════╗
║          📊  BuildPulse — Metrics Summary         ║
╚═══════════════════════════════════════════════════╝

Total build time: 18.2s (-7.1s ✅)

Module Breakdown (sorted by time ↓)
  ─────────────────────────────────────
    app                    28,120 ms   (-11,936 ms ✅)
    core-network           15,230 ms   (-7,117 ms  ✅)
    feature-login          12,450 ms   (-4,537 ms  ✅)

  ✅  BuildPulse CI Check: no regressions detected.

═══════════════════════════════════════════════════════
```

### View HTML Report

Open `buildpulse/build-metrics.html` in your browser for an interactive dashboard:
```
buildpulse/
  ├── build-metrics.json   ← raw metrics data
  └── build-metrics.html   ← open this in browser ✅
```

In Android Studio:
1. Find the file in Project panel
2. Right-click → **Open In** → **Browser**

---

## ⚙️ Configuration Options

All options are configured in the `buildPulse { }` block in your root `build.gradle.kts`:

| Option | Type | Default | Description |
|---|---|---|---|
| `enabled` | `Boolean` | `true` | Enable or disable the plugin entirely |
| `trackTasks` | `Boolean` | `true` | Track individual task durations |
| `trackModules` | `Boolean` | `true` | Roll up task times into module totals |
| `failOnRegression` | `Boolean` | `false` | Fail the build if a regression is detected |
| `maxAllowedIncreaseMs` | `Long` | `500L` | Threshold in ms above which a slowdown is a regression |
| `outputDir` | `String` | `"buildpulse"` | Directory where JSON and HTML reports are written |
| `generatedHtmlReport` | `Boolean` | `true` | Whether to generate the HTML visual report |

---

## 🤖 CI Integration

### Enable Enforcement Mode
```kotlin
buildPulse {
    failOnRegression     = true   // ← Fail builds on regression
    maxAllowedIncreaseMs = 500L   // ← Max allowed slowdown
}
```

Now if any module gets slower by more than 500ms, the build **fails**:
```
❌  BuildPulse CI Check FAILED — regression detected!

  ┌─────────────────────────────────────────────────────┐
  │  Regressions  (threshold exceeded)                  │
  ├─────────────────────────────────────────────────────┤
  │  [Module]  app                              +720 ms
  │           prev: 28,120 ms  →  now: 28,840 ms
  └─────────────────────────────────────────────────────┘

BUILD FAILED
```

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
├── app/                           # Sample Android app
├── feature-login/                 # Sample feature module
├── core-network/                  # Sample core module
└── buildpulse/                    # Generated output (after build)
    ├── build-metrics.json
    └── build-metrics.html
```

---

## ⚙️ How It Works

1. **Plugin Registration**: `BuildPulsePlugin` registers a `TaskExecutionListener` via `TaskTimingListener`
2. **Task Tracking**: Before each task runs, it records a start timestamp; after completion, it calculates elapsed time
3. **Build Finished**: When `gradle.buildFinished` fires, `MetricsCollector` aggregates task times into module totals
4. **Load Previous**: `MetricsStore` loads the previous build's metrics from JSON (if it exists)
5. **Comparison**: `MetricsComparator` diffs the two builds to find regressions and improvements
6. **Console Report**: `ReportGenerator` prints a summary to the console
7. **HTML Report**: `HtmlReportGenerator` writes a visual dashboard to disk
8. **CI Enforcement**: `CIEnforcer` optionally throws a `GradleException` if regressions exceed the threshold

---

## 📚 What I Learned Building This

- How Gradle plugins work internally and how they differ from Android libraries
- How `TaskExecutionListener` hooks into the build lifecycle
- How composite builds (`includeBuild`) allow local plugin development without publishing
- How plugin marker artifacts work and the Gradle Plugin Portal submission process
- How to serialize/deserialize build metrics manually (without a JSON library)
- How to generate HTML reports from Kotlin string templates
- How to enforce build quality gates in CI/CD pipelines

---

## 🤝 Contributing

Contributions are welcome! Please:

1. Fork the repo
2. Create a feature branch (`git checkout -b feature/amazing`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing`)
5. Open a Pull Request

---

## 📝 License

MIT License - see [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

**Paras Patil**

- GitHub: [@PatilParas05](https://github.com/PatilParas05)
- LinkedIn: [Paras Patil](https://www.linkedin.com/in/parass-patil/)
- Gradle Plugin: [BuildPulse Android](https://plugins.gradle.org/plugin/io.github.PatilParas05.buildpulse-android)

---

## 🙏 Acknowledgments

Built with ❤️ to solve a real pain point in Android development.

If BuildPulse helped you catch a build regression, give it a ⭐!

---

## 📞 Support

- 🐛 **Bug reports:** [GitHub Issues](https://github.com/PatilParas05/buildpulse-android/issues)
- 💬 **Questions:** [GitHub Discussions](https://github.com/PatilParas05/buildpulse-android/discussions)
- 📖 **Plugin Page:** [Gradle Plugin Portal](https://plugins.gradle.org/plugin/io.github.PatilParas05.buildpulse-android)

---

## ⭐ Show Your Support

If you find BuildPulse useful:
- ⭐ Star this repo
- 🐦 Share on Twitter
- 📝 Write a blog post about it
- 💬 Tell your teammates

---

## 🔗 Quick Links

- **[📖 Gradle Plugin Portal](https://plugins.gradle.org/plugin/io.github.PatilParas05.buildpulse-android)** — Official plugin page
- **[🐛 Report an Issue](https://github.com/PatilParas05/buildpulse-android/issues)** — Found a bug?
- **[💡 Request a Feature](https://github.com/PatilParas05/buildpulse-android/issues/new?labels=enhancement)** — Have an idea?

---

<p align="center">
  <strong>Made with ☕ and Kotlin</strong>
</p>

<p align="center">
  <sub>BuildPulse • Keep Your Builds Fast 🚀</sub>
</p>
