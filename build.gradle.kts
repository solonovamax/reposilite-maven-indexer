@file:Suppress("UnstableApiUsage")

import ca.solostudios.nyx.util.soloStudios
import com.github.jengelman.gradle.plugins.shadow.internal.JavaJarExec
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    application
    `java-library`
    `maven-publish`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.dokka)
    alias(libs.plugins.ksp)
    alias(libs.plugins.shadow)
    alias(libs.plugins.nyx)
}

nyx {
    info {
        name = "Reposilite Maven Indexer"
        group = "ca.solo-studios"
        module = "reposilite-maven-indexer"
        version = "0.1.0"

        license.useMIT()
    }

    compile {
        withJavadocJar()
        withSourcesJar()

        allWarnings = true
        distributeLicense = true
        reproducibleBuilds = true
        jvmTarget = 17

        kotlin {
            apiVersion = "2.0"
            languageVersion = "2.0"

            withExplicitApi()
        }
    }
}

repositories {
    soloStudios()
    mavenCentral()
    maven("https://maven.reposilite.com/releases")
}

val reposilite by configurations.register("reposilite") {
    isCanBeResolved = true
    isCanBeConsumed = false
    isCanBeDeclared = false
    isTransitive = false
    // configurations.runtimeOnly.configure { extendsFrom(this@register) }
}

dependencies {
    compileOnly(libs.kotlin.stdlib)

    implementation(libs.kotlinx.datetime)

    compileOnly(libs.ksp.service)
    ksp(libs.ksp.service)

    compileOnly(libs.reposilite)
    reposilite(variantOf(libs.reposilite) { classifier("all") })

    implementation(libs.maven.indexer)
    implementation(libs.bundles.lucene)

    compileOnly(libs.javalin.openapi.plugin)
    kapt(libs.javalin.openapi.processor)

    testImplementation(libs.bundles.junit)
    testImplementation(libs.bundles.kotlin.test)
}

application {
    mainClass = "com.reposilite.ReposiliteLauncherKt"
}

tasks {
    test {
        useJUnitPlatform()
    }

    val shadowJar by getting(ShadowJar::class) {
        // archiveFileName.set("reposilite-maven-indexer-plugin.jar")t
        // destinationDirectory.set(buildDir.resolve("run/plugins/"))
        mergeServiceFiles()

        manifest {
            attributes("Multi-Release" to "true") // lucene breaks without this
        }

        metaInf {

        }

        dependencies {
            exclude(dependency("javax.inject:javax.inject"))
            exclude(dependency("org.slf4j:slf4j-api"))
            exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib"))
            exclude(dependency("org.jetbrains:annotations"))
        }
    }

    val runDir = layout.buildDirectory.dir("run")

    val prepRun by creating(Copy::class) {
        dependsOn(shadowJar)

        from(shadowJar.archiveFile) {
            rename { "reposilite-maven-indexer-plugin.jar" }
        }
        into(runDir.map { it.dir("plugins") })
    }

    named<JavaJarExec>("runShadow") {
        dependsOn(prepRun)
        jarFile = reposilite.resolve().single()

        jvmArguments.addAll(
            "--enable-native-access=ALL-UNNAMED",
            "--add-modules=jdk.incubator.vector",
        )

        workingDir(runDir)
        standardInput = System.`in`
    }

    // named<JavaExec>("run") {
    //     dependsOn(prepRun)
    //
    //     classpath(configurations.shadow, configurations.runtimeClasspath)
    //
    //     workingDir(runDir)
    //     standardInput = System.`in`
    // }
}
