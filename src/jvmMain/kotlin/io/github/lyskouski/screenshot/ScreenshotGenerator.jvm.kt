// Copyright 2026 Viachaslau Lyskouski. All rights reserved.
// Use of this source code is governed by an MIT license that can be found in the LICENSE file.

package io.github.lyskouski.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runSkikoComposeUiTest
import androidx.compose.ui.unit.Density
import java.io.File
import java.util.Locale

internal actual fun readOption(name: String): String? = System.getProperty(name) ?: System.getenv(name)

internal actual fun hasScreenshots(directory: String): Boolean =
    File(directory).listFiles { file -> file.extension == "png" }?.isNotEmpty() == true

internal actual fun writeHtmlReport(outputDirectory: String) {
    ScreenshotHelper.generateHtmlReport(File(outputDirectory))
}

@OptIn(ExperimentalTestApi::class)
internal actual fun captureScreenshot(
    target: ScreenshotTarget,
    onInit: ScreenshotInit?,
    before: ScreenshotStep?,
    content: @Composable (ScreenshotTarget) -> Unit,
) {
    val device = target.device
    val config = target.config
    // Compose resources and date / number formats follow the default locale on JVM.
    val systemLocale = Locale.getDefault()
    Locale.setDefault(Locale.forLanguageTag(target.languageTag))
    try {
        runSkikoComposeUiTest(
            size = Size(device.width.toFloat(), device.height.toFloat()),
            density = Density(device.devicePixelRatio, config.fontScale),
        ) {
            onInit?.invoke(target)
            setContent { ScreenshotFrame(target, content) }
            waitForIdle()

            before?.invoke(ScreenshotScope(target, this))

            mainClock.advanceTimeBy(config.animationSettleTime.inWholeMilliseconds)
            waitForIdle()

            val file = File(target.outputFile)
            ScreenshotHelper.saveScreenshot(captureToImage(), file)
            log("Screenshot saved: ${file.path}")

            if (config.enableGoldenComparison) {
                ScreenshotHelper.compareWithGolden(file, File(target.goldenFile), config.comparisonThreshold)
            }
        }
    } finally {
        Locale.setDefault(systemLocale)
    }
}
