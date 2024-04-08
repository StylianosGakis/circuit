// Copyright (C) 2023 Slack Technologies, LLC
// SPDX-License-Identifier: Apache-2.0
import org.jetbrains.kotlin.gradle.internal.KaptGenerateStubsTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  alias(libs.plugins.agp.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.kapt)
  alias(libs.plugins.kotlin.plugin.parcelize)
  alias(libs.plugins.ksp)
}

android {
  namespace = "com.slack.circuit.shared.shapes"
  defaultConfig { minSdk = 28 }
}

tasks
  .withType<KotlinCompile>()
  .matching { it !is KaptGenerateStubsTask }
  .configureEach {
    compilerOptions {
      freeCompilerArgs.addAll(
        "-opt-in=androidx.compose.animation.ExperimentalSharedTransitionApi",
        "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
        "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
      )
    }
  }

dependencies {
  ksp(projects.circuitCodegen)

  implementation("androidx.compose.animation:animation:1.7.0-SNAPSHOT")
  implementation(libs.androidx.activity.ktx)
  implementation(libs.androidx.appCompat)
  implementation(libs.androidx.compose.accompanist.systemUi)
  implementation(libs.androidx.compose.animation)
  implementation(libs.androidx.compose.integration.activity)
  implementation(libs.androidx.compose.integration.materialThemeAdapter)
  implementation(libs.androidx.compose.material.material3)
  debugImplementation(libs.androidx.compose.ui.tooling)
  implementation(libs.bundles.compose.ui)
  implementation(libs.dagger)
  implementation(libs.kotlinx.immutable)
  implementation(projects.circuitCodegenAnnotations)
  implementation(projects.circuitFoundation)
  implementation(projects.circuitx.gestureNavigation)
}
