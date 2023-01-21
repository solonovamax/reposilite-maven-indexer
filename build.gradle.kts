import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    application
    `java-library`
    `maven-publish`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.shadow)
    alias(libs.plugins.ksp)
}

group = "ca.solo-studios.reposilite"
version = "0.0.0"

repositories {
    maven("https://maven.solo-studios.ca/releases/")
    // mavenLocal()
    mavenCentral()
    maven("https://maven.reposilite.com/maven-central")
    maven("https://maven.reposilite.com/releases")

    maven("https://maven.reposilite.com/snapshots") {
        mavenContent {
            snapshotsOnly()
        }
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

kapt {
    useBuildCache = false
}

kotlin {
    explicitApi()
    target {
        compilations.configureEach {
            kotlinOptions {
                jvmTarget = "11"
                apiVersion = "1.7"
                languageVersion = "1.7"
            }
        }
    }
}

dependencies {
    // ksp shit
    compileOnly(libs.ksp.service)
    ksp(libs.ksp.service)

    shadow(libs.reposilite)
    compileOnly(libs.reposilite)

    shadow(libs.bundles.kotlin.base)
    compileOnly(libs.bundles.kotlin.base)

    implementation(libs.maven.indexer)
    implementation(libs.bundles.lucene)

    // idk magic shit here
    shadow(libs.javalin.openapi.plugin)
    kapt(libs.javalin.openapi.plugin)

    testImplementation(libs.bundles.junit)
    testImplementation(libs.bundles.kotlin.test)
}

application {
    // mainClass.set("ca.solostudios.reposilite.mvnindexer.MavenIndexerPlugin")
    mainClass.set("com.reposilite.ReposiliteLauncherKt")
}

tasks {
    test {
        useJUnitPlatform()
    }

    val shadowJar by getting(ShadowJar::class) {
        // archiveFileName.set("reposilite-maven-indexer-plugin.jar")t
        // destinationDirectory.set(buildDir.resolve("run/plugins/"))
        mergeServiceFiles()

        // minimize()
    }

    val runDir = buildDir.resolve("run")

    val prepRun by creating(Copy::class) {
        dependsOn(shadowJar)

        from(shadowJar.archiveFile) {
            rename { "reposilite-maven-indexer-plugin.jar" }
        }
        into(runDir.resolve("plugins"))
    }

    withType<JavaExec> {
        dependsOn(prepRun)

        classpath(configurations.shadow)

        workingDir(runDir)
        standardInput = System.`in`
    }
}
