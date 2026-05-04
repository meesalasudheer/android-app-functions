import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    jacoco
}

android {
    namespace = "com.example.appfunctionsdemo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.appfunctionsdemo"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

ksp {
    arg("appfunctions:aggregateAppFunctions", "true")
}

tasks.configureEach {
    if (!name.startsWith("merge") || !name.endsWith("Assets")) return@configureEach
    if (name.contains("ArtProfile")) return@configureEach

    val variant = name.removePrefix("merge").removeSuffix("Assets")
    val kspTaskName = "ksp${variant}Kotlin"

    if (tasks.names.contains(kspTaskName)) {
        dependsOn(kspTaskName)
    }
}

dependencies {
    implementation(project(":core:ledger"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)

    implementation(libs.androidx.appfunctions)
    implementation(libs.androidx.appfunctions.service)
    ksp(libs.androidx.appfunctions.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

val jacocoClassIncludes = listOf(
    "com/example/appfunctionsdemo/ExpenseGraph*",
    "com/example/appfunctionsdemo/functions/ExpenseAgentFunctions*",
    "com/example/appfunctionsdemo/ui/ExpenseDemoController*"
)

tasks.register<JacocoReport>("jacocoDebugUnitTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val appClassesJar = layout.buildDirectory.file(
        "intermediates/compile_app_classes_jar/debug/bundleDebugClassesToCompileJar/classes.jar"
    )
    classDirectories.setFrom(
        zipTree(appClassesJar).matching {
            include(jacocoClassIncludes)
        }
    )
    sourceDirectories.setFrom(
        files(
            "src/main/java",
            "src/main/kotlin"
        )
    )
    executionData.setFrom(
        fileTree(layout.buildDirectory.get().asFile) {
            include("outputs/unit_test_code_coverage/**/testDebugUnitTest.exec")
        }
    )
}

tasks.register<JacocoCoverageVerification>("jacocoDebugCoverageVerification") {
    dependsOn("jacocoDebugUnitTestReport")

    val appClassesJar = layout.buildDirectory.file(
        "intermediates/compile_app_classes_jar/debug/bundleDebugClassesToCompileJar/classes.jar"
    )
    classDirectories.setFrom(
        zipTree(appClassesJar).matching {
            include(jacocoClassIncludes)
        }
    )
    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
    executionData.setFrom(
        fileTree(layout.buildDirectory.get().asFile) {
            include("outputs/unit_test_code_coverage/**/testDebugUnitTest.exec")
        }
    )

    violationRules {
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn("jacocoDebugCoverageVerification")
}
