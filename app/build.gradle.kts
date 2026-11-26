import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// 统一版本管理：本地和 GitHub Actions 共用
val appVersionCode = providers.gradleProperty("ciVersionCode").orNull?.toIntOrNull() ?: 1
val appVersionName = providers.gradleProperty("ciVersionName").orNull ?: "1.0.0"
val releaseStorePath = providers.environmentVariable("ANDROID_KEYSTORE_PATH").orNull
val releaseStorePassword = providers.environmentVariable("ANDROID_KEYSTORE_PASSWORD").orNull
val releaseStoreType = providers.environmentVariable("ANDROID_KEYSTORE_TYPE").orNull
    ?.takeIf { it.isNotBlank() } ?: "PKCS12"
val releaseKeyAlias = providers.environmentVariable("ANDROID_KEY_ALIAS").orNull
val releaseKeyPassword = providers.environmentVariable("ANDROID_KEY_PASSWORD").orNull
val hasReleaseSigning = listOf(
    releaseStorePath,
    releaseStorePassword,
    releaseKeyAlias,
    releaseKeyPassword,
).all { !it.isNullOrBlank() }
val requireReleaseSigning = providers.gradleProperty("requireReleaseSigning").orNull == "true"

if (requireReleaseSigning && !hasReleaseSigning) {
    throw GradleException(
        "Release signing is required. Configure ANDROID_KEYSTORE_PATH, ANDROID_KEYSTORE_PASSWORD, " +
            "ANDROID_KEY_ALIAS, and ANDROID_KEY_PASSWORD."
    )
}

android {
    namespace = "org.chlzhong.fishbox"
    compileSdk = 36

    defaultConfig {
        // TODO
        applicationId = "org.chlzhong.fishbox"
        minSdk = 24
        targetSdk = 36
        versionCode = appVersionCode
        versionName = appVersionName
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(requireNotNull(releaseStorePath))
                storeType = releaseStoreType
                storePassword = requireNotNull(releaseStorePassword)
                keyAlias = requireNotNull(releaseKeyAlias)
                keyPassword = requireNotNull(releaseKeyPassword)
            }
        }
    }

    buildTypes {
        release {
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
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
        buildConfig = true
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

// 构建前检查 assets/fish_images 是否齐全：
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
