## 1.0.0

- Initial release: a Kotlin / Compose Multiplatform port of `flutter_screenshot_generator` 1.1.0.
- Headless capture on JVM through the Compose UI test API, at each device's resolution and density.
- Device catalog, presets, the `SCREENSHOT_DEVICES` / `SCREENSHOT_LOCALES` / `SCREENSHOT_STEPS` / `SKIP_EXISTANT` filters.
- Golden comparison, HTML report, JUnit 5 friendly `cases()`.
