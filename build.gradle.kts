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
        version = "0.1.0-SNAPSHOT"
        description = """
            A plugin for reposilite, which runs Maven Indexer
        """.trimIndent()

        organizationUrl = "https://solo-studios.ca/"
        organizationName = "Solo Studios"

        developer {
            id = "solonovamax"
            name = "solonovamax"
            email = "solonovamax@12oclockpoint.com"
            url = "https://solonovamax.gay"
        }

        repository.fromGithub("solo-studios", "reposilite-maven-indexer")
        license.useMIT()
    }

    compile {
        withJavadocJar()
        withSourcesJar()

        allWarnings = true
        distributeLicense = true
        reproducibleBuilds = true
        jvmTarget = 11

        kotlin {
            apiVersion = "2.0"
            languageVersion = "2.0"

            withExplicitApi()
        }
    }

    publishing {
        withSignedPublishing()

        repositories {
            maven {
                name = "Sonatype"

                val repositoryId: String? by project
                url = when {
                    repositoryId != null -> uri("https://s01.oss.sonatype.org/service/local/staging/deployByRepositoryId/$repositoryId/")
                    else                 -> uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                }

                credentials(PasswordCredentials::class)
            }
            maven {
                name = "SoloStudiosReleases"

                url = uri("https://maven.solo-studios.ca/releases/")

                credentials(PasswordCredentials::class)
                authentication { // publishing doesn't work without this for some reason
                    create<BasicAuthentication>("basic")
                }
            }
            maven {
                name = "SoloStudiosSnapshots"

                url = uri("https://maven.solo-studios.ca/snapshots/")

                credentials(PasswordCredentials::class)
                authentication { // publishing doesn't work without this for some reason
                    create<BasicAuthentication>("basic")
                }
            }
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
}
