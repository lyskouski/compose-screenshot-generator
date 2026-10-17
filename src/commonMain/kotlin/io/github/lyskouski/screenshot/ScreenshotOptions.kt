// Copyright 2026 Viachaslau Lyskouski. All rights reserved.
// Use of this source code is governed by an MIT license that can be found in the LICENSE file.

package io.github.lyskouski.screenshot

/**
 * Run-time limits of a generation; empty sets mean "everything".
 *
 * [fromEnvironment] reads them from system properties or environment variables:
 * - `SCREENSHOT_DEVICES` - comma separated device names ("iPhone 15 Pro" or "iPhone_15_Pro");
 * - `SCREENSHOT_LOCALES` - comma separated locales ("en_US"), a language ("en") matches all its regions;
 * - `SCREENSHOT_STEPS` - comma separated step titles;
 * - `SKIP_EXISTANT` (or `SKIP_EXISTING`) - `true` / `1` skips devices whose folder already has screenshots;
 * - `SCREENSHOT_OUTPUT` - overrides [ScreenshotConfig.outputDirectory].
 */
public data class ScreenshotOptions(
    val devices: Set<String> = emptySet(),
    val locales: Set<String> = emptySet(),
    val steps: Set<String> = emptySet(),
    val skipExisting: Boolean = false,
    val outputDirectory: String? = null,
) {
    public fun matchesDevice(device: DeviceConfig): Boolean =
        devices.isEmpty() || device.name in devices || device.folderName in devices

    public fun matchesLocale(locale: String): Boolean {
        if (locales.isEmpty()) return true
        val code = normalizeLocale(locale)
        return locales.map(::normalizeLocale).any { filter -> code == filter || code.startsWith("${filter}_") }
    }

    public fun matchesStep(step: String): Boolean = steps.isEmpty() || step in steps

    public companion object {
        public fun fromEnvironment(): ScreenshotOptions = ScreenshotOptions(
            devices = readCsv("SCREENSHOT_DEVICES"),
            locales = readCsv("SCREENSHOT_LOCALES"),
            steps = readCsv("SCREENSHOT_STEPS"),
            skipExisting = readFlag("SKIP_EXISTANT") || readFlag("SKIP_EXISTING"),
            outputDirectory = readOption("SCREENSHOT_OUTPUT")?.trim()?.takeIf { it.isNotEmpty() },
        )

        private fun readCsv(name: String): Set<String> =
            readOption(name).orEmpty().split(',').map { it.trim() }.filter { it.isNotEmpty() }.toSet()

        private fun readFlag(name: String): Boolean =
            readOption(name)?.trim()?.lowercase().let { it == "true" || it == "1" }

        private fun normalizeLocale(value: String): String = value.trim().replace('-', '_').lowercase()
    }
}
