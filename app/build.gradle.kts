import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.fishcodex"
    compileSdk = 36

    defaultConfig {
        // TODO
        applicationId = "com.example.fishcodex"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // data/ 是鱼类数据的唯一来源：直接打包进 assets，避免 data/ 与 assets/ 两份 JSON 不同步
    sourceSets {
        getByName("main") {
            assets.srcDirs("src/main/assets", "../data")
        }
    }

    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.08.00"))
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("io.coil-kt:coil-compose:2.7.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
}

// 鱼图随 APK 一起打包（离线），App 不联网下载。构建前检查 assets/fish_images 是否齐全：
// 调试构建只警告；发布构建（release / bundle）缺图会直接失败，避免发出缺图的包。
val verifyFishImages by tasks.registering {
    val imagesDir = file("src/main/assets/fish_images")
    val fishJson = file("../data/fish.json")
    val strict = gradle.startParameter.taskNames.any { it.contains("release", true) || it.contains("bundle", true) }
    doLast {
        @Suppress("UNCHECKED_CAST")
        val fish = groovy.json.JsonSlurper().parseText(fishJson.readText()) as List<Map<String, Any?>>
        val qualities = listOf("standard", "uncommon", "rare", "epic", "legendary")
        val missing = fish.flatMap { f -> qualities.map { q -> "${f["id"]}_$q.webp" } }
            .filter { !File(imagesDir, it).exists() }
        if (missing.isNotEmpty()) {
            val msg = "鱼图不完整：缺少 ${missing.size} 张（应有 ${fish.size * 5} 张）。" +
                "请先运行 tools/download_images.bat，把图片放入 app/src/main/assets/fish_images/。"
            if (strict) throw GradleException(msg) else logger.warn("WARNING: $msg 缺图的鱼会显示占位图。")
        }
    }
}
tasks.named("preBuild") { dependsOn(verifyFishImages) }
