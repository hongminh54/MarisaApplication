plugins {
    kotlin("jvm") version "1.9.23"
    id("org.jetbrains.compose") version "1.6.1"
}

group = "net.hongminh54.marisaclient"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://jitpack.io")
}

val composeVersion = "1.7.3"
val skikoVersion = "0.8.19"

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.compose.runtime:runtime:$composeVersion")
    implementation("org.jetbrains.compose.ui:ui:$composeVersion")
    implementation("org.jetbrains.compose.material:material:$composeVersion")
    implementation("org.jetbrains.compose.desktop:desktop:$composeVersion")
    implementation("org.jetbrains.skiko:skiko:$skikoVersion")
    implementation("org.jetbrains.skiko:skiko-awt-runtime-windows-x64:$skikoVersion")

    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3")
}

kotlin {
    jvmToolchain(17)
}

compose.desktop {
    application {
        mainClass = "net.hongminh54.marisaclient.MainKt"

        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb
            )
            packageName = "MarisaClientInstaller"
            packageVersion = "1.0.0"
        }
    }
}

tasks.register<Jar>("fatJar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveBaseName.set("MarisaApplication")
    archiveClassifier.set("")
    archiveVersion.set("1.0-SNAPSHOT")

    manifest {
        attributes["Main-Class"] = "net.hongminh54.marisaclient.MainKt"
    }

    from(sourceSets.main.get().output)

    from({
        configurations.runtimeClasspath.get().filter { it.exists() && it.name.endsWith(".jar") }
            .map { zipTree(it) }
    })

    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
}

tasks.build {
    dependsOn(tasks.named("fatJar"))
}