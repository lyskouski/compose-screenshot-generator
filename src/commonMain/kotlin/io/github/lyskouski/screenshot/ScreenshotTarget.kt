package io.github.lyskouski.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val rtlLanguages = setOf("ar", "dv", "fa", "he", "iw", "ks", "ku", "ps", "sd", "ug", "ur", "yi")

/** One screenshot: [step] of the application on [device] in [locale]. */
public data class ScreenshotTarget(
    val step: String,
    val device: DeviceConfig,
    /** Locale as it was given to the generator, e.g. "en_US". */
    val locale: String,
    val config: ScreenshotConfig,
) {
    /** BCP 47 language tag of [locale], e.g. "en-US". */
    val languageTag: String get() = locale.replace('_', '-')
    val language: String get() = locale.substringBefore('_').substringBefore('-').lowercase()
    val isRtl: Boolean get() = language in rtlLanguages

    val outputFile: String get() = "${config.outputDirectory}/$relativePath"
    val goldenFile: String get() = "${config.goldenDirectory}/$relativePath"
    val name: String get() = "$step - ${device.name} - $locale"

    private val relativePath: String get() = "$locale/${device.folderName}/${sanitizeFileName(step)}.png"
}

/** The screenshot being rendered; null when the application runs outside the generator. */
public val LocalScreenshotTarget: ProvidableCompositionLocal<ScreenshotTarget?> = staticCompositionLocalOf { null }

@Composable
internal fun ScreenshotFrame(target: ScreenshotTarget, content: @Composable (ScreenshotTarget) -> Unit) {
    CompositionLocalProvider(
        LocalScreenshotTarget provides target,
        LocalLayoutDirection provides if (target.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
    ) {
        content(target)
    }
}
