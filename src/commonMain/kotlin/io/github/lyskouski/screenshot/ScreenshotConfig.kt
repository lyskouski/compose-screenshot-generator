package io.github.lyskouski.screenshot

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

public data class ScreenshotConfig(
    val enabledPlatforms: Set<TargetPlatform> = TargetPlatform.entries.toSet(),
    val enabledDeviceTypes: Set<DeviceType> = DeviceType.entries.toSet(),
    /** Devices to pick from; filtered by [enabledPlatforms] and [enabledDeviceTypes]. */
    val devices: List<DeviceConfig> = PlatformDevices.allDevices,
    /** Root folder: `<outputDirectory>/<locale>/<device>/<step>.png`. Relative to the test working directory. */
    val outputDirectory: String = "screenshots",
    /** Time given to animations before the screenshot is taken. */
    val animationSettleTime: Duration = 500.milliseconds,
    val fontScale: Float = 1f,
    /** Writes `<outputDirectory>/index.html` with all captured screenshots after [ScreenshotGenerator.run]. */
    val generateHtmlReport: Boolean = false,
    /** Compares every screenshot with `<goldenDirectory>/<locale>/<device>/<step>.png`; missing goldens are recorded. */
    val enableGoldenComparison: Boolean = false,
    val goldenDirectory: String = "goldens",
    /** Allowed share of different pixels, 0..1. */
    val comparisonThreshold: Double = 0.05,
) {
    /** Devices left after the platform and device type filters. */
    public fun getEnabledDevices(): List<DeviceConfig> =
        devices.filter { it.platform in enabledPlatforms && it.type in enabledDeviceTypes }
}

/** Predefined configurations for different testing scenarios. */
public object ScreenshotPresets {
    private val mobilePlatforms = setOf(TargetPlatform.IOS, TargetPlatform.ANDROID)
    private val desktopPlatforms = setOf(TargetPlatform.MACOS, TargetPlatform.WINDOWS, TargetPlatform.LINUX)

    /** Mobile-only configuration for quick testing. */
    public val mobileOnly: ScreenshotConfig = ScreenshotConfig(
        enabledPlatforms = mobilePlatforms,
        enabledDeviceTypes = setOf(DeviceType.PHONE),
    )

    public val iosOnly: ScreenshotConfig = ScreenshotConfig(
        enabledPlatforms = setOf(TargetPlatform.IOS),
        enabledDeviceTypes = setOf(DeviceType.PHONE, DeviceType.TABLET),
    )

    public val androidOnly: ScreenshotConfig = ScreenshotConfig(
        enabledPlatforms = setOf(TargetPlatform.ANDROID),
        enabledDeviceTypes = setOf(DeviceType.PHONE, DeviceType.TABLET),
    )

    public val desktopOnly: ScreenshotConfig = ScreenshotConfig(
        enabledPlatforms = desktopPlatforms,
        enabledDeviceTypes = setOf(DeviceType.DESKTOP),
    )

    /** CI/CD optimized configuration (fewer devices for faster builds). */
    public val ciOptimized: ScreenshotConfig = ScreenshotConfig(
        enabledPlatforms = mobilePlatforms,
        enabledDeviceTypes = setOf(DeviceType.PHONE),
        comparisonThreshold = 0.1, // More lenient for CI
    )

    /** Full comprehensive testing. */
    public val comprehensive: ScreenshotConfig = ScreenshotConfig(
        generateHtmlReport = true,
        comparisonThreshold = 0.03, // Stricter comparison
    )
}
