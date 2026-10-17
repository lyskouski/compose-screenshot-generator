// Copyright 2026 Viachaslau Lyskouski. All rights reserved.
// Use of this source code is governed by an MIT license that can be found in the LICENSE file.

package io.github.lyskouski.screenshot

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import java.io.File
import java.util.Locale
import javax.imageio.ImageIO
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ScreenshotGeneratorTest {
    private val output = createTempDirectory("screenshots").toFile()

    private val phone = DeviceConfig(
        name = "Test Phone", width = 300, height = 600, devicePixelRatio = 3f,
        platform = TargetPlatform.ANDROID, type = DeviceType.PHONE,
    )
    private val desktop = DeviceConfig(
        name = "Test Desktop", width = 800, height = 500, devicePixelRatio = 1f,
        platform = TargetPlatform.LINUX, type = DeviceType.DESKTOP,
    )
    private val config = ScreenshotConfig(devices = listOf(phone, desktop), outputDirectory = output.path)

    @AfterTest
    fun cleanup() {
        output.deleteRecursively()
    }

    @Test
    fun capturesEveryCombinationInDeviceResolution() {
        val gen = ScreenshotGenerator(listOf("en_US", "de_DE"), config, ScreenshotOptions()) {
            Box(Modifier.fillMaxSize().background(Color.Red))
        }
        gen.inject("1_home")
        gen.run()

        for (locale in listOf("en_US", "de_DE")) {
            val phoneImage = ImageIO.read(File(output, "$locale/Test_Phone/1_home.png"))
            assertEquals(300 to 600, phoneImage.width to phoneImage.height)
            assertEquals(Color.Red.toArgb(), phoneImage.getRGB(150, 300))
            val desktopImage = ImageIO.read(File(output, "$locale/Test_Desktop/1_home.png"))
            assertEquals(800 to 500, desktopImage.width to desktopImage.height)
        }
    }

    @Test
    fun appliesStepsBeforeCapturing() {
        val gen = ScreenshotGenerator(listOf("en_US"), config.copy(devices = listOf(phone)), ScreenshotOptions()) {
            var tapped by remember { mutableStateOf(false) }
            Box(
                Modifier.fillMaxSize()
                    .background(if (tapped) Color.Green else Color.Blue)
                    .testTag("root")
                    .clickable(interactionSource = null, indication = null) { tapped = true },
            )
        }
        gen.inject("1_home")
        gen.inject("2_tap") { onNodeWithTag("root").performClick() }
        gen.run()

        assertEquals(Color.Blue.toArgb(), ImageIO.read(File(output, "en_US/Test_Phone/1_home.png")).getRGB(10, 10))
        assertEquals(Color.Green.toArgb(), ImageIO.read(File(output, "en_US/Test_Phone/2_tap.png")).getRGB(10, 10))
    }

    @Test
    fun providesTargetAndDefaultLocaleToContent() {
        val seen = mutableListOf<String>()
        val initialized = mutableListOf<String>()
        val gen = ScreenshotGenerator(
            locales = listOf("de_DE", "ar_EG"),
            config = config.copy(devices = listOf(desktop)),
            options = ScreenshotOptions(),
            onInit = { initialized += it.locale },
        ) { target ->
            seen += "${target.locale}:${Locale.getDefault().toLanguageTag()}:${target.isRtl}"
            Box(Modifier.size(10.dp))
        }
        gen.inject("1_home")
        gen.run()

        assertEquals(listOf("de_DE", "ar_EG"), initialized)
        assertTrue("de_DE:de-DE:false" in seen, seen.toString())
        assertTrue("ar_EG:ar-EG:true" in seen, seen.toString())
    }

    @Test
    fun filtersDevicesLocalesAndSteps() {
        val options = ScreenshotOptions(devices = setOf("Test_Phone"), locales = setOf("en"), steps = setOf("2_b"))
        val gen = ScreenshotGenerator(listOf("en_US", "en_GB", "de_DE"), config, options) { BasicText("x") }
        gen.inject("1_a")
        gen.inject("2_b")

        assertEquals(
            listOf("2_b - Test Phone - en_US", "2_b - Test Phone - en_GB"),
            gen.cases().map { it.name },
        )
    }

    @Test
    fun skipsDevicesWithExistingScreenshots() {
        File(output, "en_US/Test_Phone").mkdirs()
        File(output, "en_US/Test_Phone/1_home.png").writeBytes(byteArrayOf())
        File(output, "en_US/Test_Desktop").mkdirs()

        val gen = ScreenshotGenerator(listOf("en_US"), config, ScreenshotOptions(skipExisting = true)) { BasicText("x") }
        gen.inject("1_home")

        assertEquals(listOf("1_home - Test Desktop - en_US"), gen.cases().map { it.name })
    }

    @Test
    fun comparesWithGoldens() {
        val goldens = File(output, "goldens")
        var color = Color.Red
        val goldenConfig = config.copy(
            devices = listOf(desktop),
            outputDirectory = File(output, "out").path,
            enableGoldenComparison = true,
            goldenDirectory = goldens.path,
        )
        val gen = ScreenshotGenerator(listOf("en_US"), goldenConfig, ScreenshotOptions()) {
            Box(Modifier.fillMaxSize().background(color))
        }
        gen.inject("1_home")

        gen.run() // records
        assertTrue(File(goldens, "en_US/Test_Desktop/1_home.png").exists())
        gen.run() // matches

        color = Color.Black
        val error = assertFailsWith<AssertionError> { gen.run() }
        assertTrue("pixels differ" in error.suppressedExceptions.single().message.orEmpty())
    }

    @Test
    fun generatesHtmlReport() {
        val gen = ScreenshotGenerator(
            listOf("en_US"),
            config.copy(generateHtmlReport = true),
            ScreenshotOptions(),
        ) { BasicText("x") }
        gen.inject("1_home")
        gen.run()

        val report = File(output, "index.html").readText()
        assertTrue("en_US/Test_Phone/1_home.png" in report)
        assertTrue("en_US/Test_Desktop/1_home.png" in report)
        assertFalse("goldens" in report)
    }
}
