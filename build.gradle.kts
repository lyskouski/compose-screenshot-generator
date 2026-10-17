import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.maven.publish)
}

group = property("GROUP") as String
version = property("VERSION_NAME") as String

kotlin {
    explicitApi()

    compilerOptions {
        optIn.add("androidx.compose.ui.test.ExperimentalTestApi")
    }

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                api(libs.compose.runtime)
                api(libs.compose.ui)
                api(libs.compose.ui.test)
            }
        }

        jvmTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.compose.foundation)
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    pom {
        name.set("Compose Screenshot Generator")
        description.set(
            "Generate screenshots for a Compose Multiplatform application across different screens and languages.",
        )
        url.set("https://github.com/lyskouski/compose-screenshot-generator")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("lyskouski")
                name.set("Viachaslau Lyskouski")
            }
        }
        scm {
            url.set("https://github.com/lyskouski/compose-screenshot-generator")
        }
    }
}
