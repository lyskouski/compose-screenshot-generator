// Copyright 2026 Viachaslau Lyskouski. All rights reserved.
// Use of this source code is governed by an MIT license that can be found in the LICENSE file.

package io.github.lyskouski.screenshot

import androidx.compose.runtime.Composable

/** Actions applied to the rendered application before the screenshot is taken. */
public typealias ScreenshotStep = suspend ScreenshotScope.() -> Unit

/** Preparation of a screenshot (data, preferences) before the application is composed. */
public typealias ScreenshotInit = suspend (target: ScreenshotTarget) -> Unit

/**
 * Captures [content] for every locale x device x step combination:
 * ```
 * val gen = ScreenshotGenerator(locales = listOf("en_US", "de_DE")) { App() }
 * gen.inject("1_home")
 * gen.inject("2_tap") { onNodeWithText("+").performClick() }
 * gen.run()
 * ```
 */
public class ScreenshotGenerator(
    locales: List<String>,
    config: ScreenshotConfig = ScreenshotConfig(),
    private val options: ScreenshotOptions = ScreenshotOptions.fromEnvironment(),
    private val onInit: ScreenshotInit? = null,
    private val content: @Composable (target: ScreenshotTarget) -> Unit,
) {
    public val config: ScreenshotConfig =
        options.outputDirectory?.let { config.copy(outputDirectory = it) } ?: config
    public val devices: List<DeviceConfig> = this.config.getEnabledDevices().filter(options::matchesDevice)
    public val locales: List<String> = locales.filter(options::matchesLocale)

    private val steps = LinkedHashMap<String, ScreenshotStep?>()

    init {
        val enabled = this.config.getEnabledDevices()
        log("Devices: ${devices.size} of ${enabled.size} ${devices.map { it.name }}")
        log("Locales: ${this.locales.size} of ${locales.size} ${this.locales}")
    }

    /** Adds a screenshot; steps are captured in the order they were injected. */
    public fun inject(title: String, before: ScreenshotStep? = null) {
        steps[title] = before
    }

    /** Every screenshot left after the filters, to be executed one by one (e.g. as JUnit 5 dynamic tests). */
    public fun cases(): List<ScreenshotCase> = buildList {
        for (locale in locales) {
            for (device in devices) {
                if (options.skipExisting && hasScreenshots("${config.outputDirectory}/$locale/${device.folderName}")) {
                    log("Skipping $locale/${device.name} - folder exists with screenshots")
                    continue
                }
                for ((step, before) in steps) {
                    if (!options.matchesStep(step)) continue
                    val target = ScreenshotTarget(step, device, locale, config)
                    add(ScreenshotCase(target) { captureScreenshot(target, onInit, before, content) })
                }
            }
        }
    }

    /** Captures all screenshots; failures are collected and reported together at the end. */
    public fun run() {
        val cases = cases()
        log("Capturing ${cases.size} screenshot(s) into ${config.outputDirectory}")
        val failures = cases.mapNotNull { case ->
            runCatching { case.execute() }.exceptionOrNull()?.let { case to it }
        }
        if (config.generateHtmlReport) writeReport()
        if (failures.isNotEmpty()) {
            val names = failures.joinToString("\n") { (case, error) -> " - ${case.name}: ${error.message}" }
            throw AssertionError("${failures.size} of ${cases.size} screenshot(s) failed:\n$names").apply {
                failures.forEach { addSuppressed(it.second) }
            }
        }
    }

    /** Writes `<outputDirectory>/index.html` over the screenshots found in the output directory. */
    public fun writeReport() {
        writeHtmlReport(config.outputDirectory)
    }
}

public class ScreenshotCase internal constructor(
    public val target: ScreenshotTarget,
    private val block: () -> Unit,
) {
    public val name: String get() = target.name

    public fun execute() {
        block()
    }

    override fun toString(): String = name
}

internal fun log(message: String) = println("[screenshot] $message")

internal expect fun readOption(name: String): String?

internal expect fun hasScreenshots(directory: String): Boolean

internal expect fun writeHtmlReport(outputDirectory: String)

internal expect fun captureScreenshot(
    target: ScreenshotTarget,
    onInit: ScreenshotInit?,
    before: ScreenshotStep?,
    content: @Composable (ScreenshotTarget) -> Unit,
)
