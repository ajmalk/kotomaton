plugins {
  kotlin("multiplatform") version "1.9.0"
  id("io.gitlab.arturbosch.detekt") version "1.23.1"
  id("com.diffplug.spotless") version "6.21.0"
}

group = "com.kotomaton"

version = "0.01"

repositories { mavenCentral() }

spotless {
  // limit format enforcement to just the files changed by this feature branch
  ratchetFrom("origin/main")

  format("misc") {
    // define the files to apply `misc` to
    target("*.md", ".gitignore", "**/*.yml")

    // define the steps to apply to those files
    trimTrailingWhitespace()
    indentWithSpaces(2)
    endWithNewline()
  }
  kotlin {
    ktfmt("0.42")
    target("**/*.kt", "**/*.kts")
  }
}

detekt {
  toolVersion = "1.23.1"
  source.setFrom(
      "src/commonMain/kotlin",
      "src/commonTest/kotlin",
      "build.gradle.kts",
      "settings.gradle.kts",
  )

  basePath = projectDir.absolutePath
  config.setFrom(file("config/detekt/detekt.yml"))
  buildUponDefaultConfig = true

  dependencies { detektPlugins("io.gitlab.arturbosch.detekt:detekt-rules-libraries:1.23.1") }
}

@Suppress("UnusedPrivateProperty")
kotlin {
  explicitApi()

  jvm {
    jvmToolchain(17)
    withJava()
    testRuns.named("test") { executionTask.configure { useJUnitPlatform() } }
  }

  js { browser() }

  val hostOs = System.getProperty("os.name")
  val isArm64 = System.getProperty("os.arch") == "aarch64"
  val isMingwX64 = hostOs.startsWith("Windows")
  when {
    hostOs == "Mac OS X" && isArm64 -> macosArm64("native")
    hostOs == "Mac OS X" && !isArm64 -> macosX64("native")
    hostOs == "Linux" && isArm64 -> linuxArm64("native")
    hostOs == "Linux" && !isArm64 -> linuxX64("native")
    isMingwX64 -> mingwX64("native")
    else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
  }

  sourceSets {
    val commonMain by getting {
      dependencies { implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3") }
    }
    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
      }
    }
    val jvmMain by getting
    val jvmTest by getting
    val jsMain by getting
    val jsTest by getting
    val nativeMain by getting
    val nativeTest by getting
  }
}
