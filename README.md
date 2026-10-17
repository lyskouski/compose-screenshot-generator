# Compose Screenshot Generator

Generate screenshots for a Compose Multiplatform application across different screens and languages.

A Kotlin port of [flutter_screenshot_generator](https://github.com/lyskouski/flutter_screenshot_generator).
The application is rendered headlessly through the Compose UI test API (Skia), at each device's
physical resolution and density, so no emulator, simulator or window is needed. That makes it
fine to run on CI.

## Installation

```toml
# libs.versions.toml
[versions]
screenshot-generator = "1.0.0"

[libraries]
screenshot-generator = { module = "io.github.lyskouski:compose-screenshot-generator", version.ref = "screenshot-generator" }
```

```kotlin
// build.gradle.kts of a KMP module (jvm target) or a JVM module
kotlin {
    sourceSets {
        jvmTest.dependencies {
            implementation(libs.screenshot.generator)
            implementation(compose.desktop.currentOs) // Skia runtime for the host OS
        }
    }
}
```

To use it from source without publishing, include the build in `settings.gradle.kts`. Gradle then
substitutes the catalog coordinates with the local project:

```kotlin
includeBuild("path/to/compose-screenshot-generator")
```

## Implementation

```kotlin
// ./src/jvmTest/kotlin/CaptureTest.kt
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import io.github.lyskouski.screenshot.ScreenshotGenerator
import kotlin.test.Test

class CaptureTest {
    @Test
    fun capture() {
        val gen = ScreenshotGenerator(
            locales = listOf("en_US", "de_DE"),
            onInit = { target -> /* seed data, preferences for target.locale / target.device */ },
        ) {
            App()
        }
        gen.inject("1_home")
        gen.inject("2_tap") {
            onNodeWithContentDescription("Add").performClick()
        }
        gen.run()
    }
}
```

Screenshots are written to `<outputDirectory>/<locale>/<device>/<step>.png`, for example
`screenshots/en_US/iPhone_15_Pro/1_home.png`.

- `content` receives the `ScreenshotTarget` (`step`, `device`, `locale`, `config`). Inside the
  composition it is also available as `LocalScreenshotTarget.current`.
- A step runs on a `ScreenshotScope`. It offers the usual finders (`onNodeWithText`, `onNodeWithTag`,
  ...), plus `waitForIdle`, `waitUntil`, `advanceTimeBy` and `target`. The full `ComposeUiTest` is
  available as `test`.
- On JVM the default `Locale` is switched to the target locale for each screenshot, so Compose
  resources and date/number formats follow it. RTL locales get `LayoutDirection.Rtl`.
- `run()` captures everything and reports all failures at the end. For one test per screenshot,
  map `cases()` to JUnit 5 dynamic tests:

```kotlin
@TestFactory
fun screenshots() = gen.cases().map { DynamicTest.dynamicTest(it.name) { it.execute() } }
```

## Configuration

```kotlin
ScreenshotGenerator(
    locales = listOf("en_US"),
    config = ScreenshotPresets.mobileOnly.copy(
        outputDirectory = "build/screenshots",
        animationSettleTime = 1.seconds,
        generateHtmlReport = true,      // <outputDirectory>/index.html
        enableGoldenComparison = true,  // compare with <goldenDirectory>/<locale>/<device>/<step>.png
        comparisonThreshold = 0.01,     // share of pixels allowed to differ
    ),
) { App() }
```

Presets: `mobileOnly`, `iosOnly`, `androidOnly`, `desktopOnly`, `ciOptimized`, `comprehensive`.
Devices come from `PlatformDevices`: iPhones, iPads, Android phones and tablets, and macOS, Windows
and Linux desktops (the App Store sizes included). For your own devices, pass
`ScreenshotConfig(devices = listOf(DeviceConfig(...)))`.

## Usage

Filters are read from system properties or environment variables:

```
# Run all
./gradlew :app:jvmTest --tests "*CaptureTest*"

# Limit by devices
SCREENSHOT_DEVICES="iPhone 15 Pro,Pixel 7a" ./gradlew ...

# Limit by languages ("en" matches en_US, en_GB, ...)
SCREENSHOT_LOCALES=en ./gradlew ...

# Limit by steps
SCREENSHOT_STEPS=2_tap ./gradlew ...

# Skip devices for which a folder with screenshots already exists
SKIP_EXISTANT=true ./gradlew ...

# Override the output directory
SCREENSHOT_OUTPUT=/tmp/screenshots ./gradlew ...
```

Gradle does not track environment variables as test inputs. Run with `--rerun` or set
`outputs.upToDateWhen { false }` on the test task.

## Platforms

The API (`commonMain`) is multiplatform. Capturing is implemented for the **JVM** target, which
renders every device profile: phones, tablets and desktops alike.

## License

[MIT](./LICENSE)
