package io.github.lyskouski.screenshot

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.imageio.ImageIO

public object ScreenshotHelper {
    public fun encodePng(image: ImageBitmap): ByteArray =
        Image.makeFromBitmap(image.asSkiaBitmap()).encodeToData(EncodedImageFormat.PNG)?.bytes
            ?: error("Failed to encode the screenshot as PNG")

    public fun saveScreenshot(image: ImageBitmap, file: File) {
        file.parentFile?.mkdirs()
        file.writeBytes(encodePng(image))
    }

    /**
     * Fails when more than [threshold] (0..1) of the pixels of [actual] differ from [golden].
     * A missing golden is created from [actual].
     */
    public fun compareWithGolden(actual: File, golden: File, threshold: Double) {
        if (!golden.exists()) {
            golden.parentFile?.mkdirs()
            actual.copyTo(golden)
            log("Golden recorded: ${golden.path}")
            return
        }
        val expectedImage = ImageIO.read(golden)
        val actualImage = ImageIO.read(actual)
        if (expectedImage.width != actualImage.width || expectedImage.height != actualImage.height) {
            throw AssertionError(
                "${actual.path}: size ${actualImage.width}x${actualImage.height} differs from golden " +
                    "${expectedImage.width}x${expectedImage.height} (${golden.path})",
            )
        }
        val width = actualImage.width
        val height = actualImage.height
        val expectedPixels = expectedImage.getRGB(0, 0, width, height, null, 0, width)
        val actualPixels = actualImage.getRGB(0, 0, width, height, null, 0, width)
        val different = expectedPixels.indices.count { expectedPixels[it] != actualPixels[it] }
        val ratio = different.toDouble() / expectedPixels.size
        if (ratio > threshold) {
            throw AssertionError(
                "${actual.path}: ${"%.2f".format(ratio * 100)}% of pixels differ from golden ${golden.path} " +
                    "(threshold ${"%.2f".format(threshold * 100)}%)",
            )
        }
    }

    /** Removes all generated screenshots. */
    public fun cleanupScreenshots(outputDirectory: File) {
        if (outputDirectory.deleteRecursively()) log("Cleaned up screenshots directory: ${outputDirectory.path}")
    }

    /** Writes `index.html` into [outputDirectory]: a gallery grouped by locale and device. */
    public fun generateHtmlReport(outputDirectory: File): File {
        val report = File(outputDirectory, "index.html")
        val html = StringBuilder()
        html.appendLine("<!DOCTYPE html>")
        html.appendLine("<html><head><meta charset=\"utf-8\"><title>Screenshots</title><style>")
        html.appendLine("body{font-family:sans-serif;margin:16px}figure{display:inline-block;margin:8px;text-align:center}")
        html.appendLine("img{max-width:240px;max-height:480px;border:1px solid #ccc}figcaption{font-size:12px}")
        html.appendLine("</style></head><body>")
        html.appendLine("<h1>Screenshots</h1>")
        html.appendLine("<p>Generated on: ${LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}</p>")
        for (locale in outputDirectory.subdirectories()) {
            html.appendLine("<h2>${locale.name}</h2>")
            for (device in locale.subdirectories()) {
                html.appendLine("<h3>${device.name}</h3>")
                device.listFiles { file -> file.extension == "png" }.orEmpty().sortedBy { it.name }.forEach { image ->
                    val path = image.relativeTo(outputDirectory).invariantSeparatorsPath
                    html.appendLine(
                        "<figure><a href=\"$path\"><img src=\"$path\" loading=\"lazy\"></a>" +
                            "<figcaption>${image.nameWithoutExtension}</figcaption></figure>",
                    )
                }
            }
        }
        html.appendLine("</body></html>")
        outputDirectory.mkdirs()
        report.writeText(html.toString())
        log("Report saved: ${report.path}")
        return report
    }

    private fun File.subdirectories(): List<File> =
        listFiles { file -> file.isDirectory }.orEmpty().sortedBy { it.name }
}
