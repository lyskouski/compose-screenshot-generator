package io.github.lyskouski.screenshot

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider

/**
 * Receiver of a [ScreenshotStep]: finds nodes (`onNodeWithText(...).performClick()`) without
 * opting in to the experimental test API; the full [ComposeUiTest] is available as [test].
 */
public class ScreenshotScope internal constructor(
    public val target: ScreenshotTarget,
    public val test: ComposeUiTest,
) : SemanticsNodeInteractionsProvider by test {
    public fun waitForIdle() {
        test.waitForIdle()
    }

    /** Waits (in wall clock time) for work the test cannot observe, e.g. loading on another thread. */
    public fun waitUntil(timeoutMillis: Long = 1_000, condition: () -> Boolean) {
        test.waitUntil(timeoutMillis = timeoutMillis, condition = condition)
    }

    /** Moves the composition clock, e.g. to finish an animation. */
    public fun advanceTimeBy(milliseconds: Long) {
        test.mainClock.advanceTimeBy(milliseconds)
    }
}
