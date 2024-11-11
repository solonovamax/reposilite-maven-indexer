pipeline {
    agent any

    tools {
        jdk "Temurin Java 17"
    }

    triggers {
        githubPush()
    }

    stages {
        stage('Checkout') {
            steps {
                scmSkip(deleteBuild: true)
            }
        }

        stage('Setup Gradle') {
            steps {
                sh 'chmod +x gradlew'
            }
        }

        stage('Build') {
            steps {
                withGradle {
                    sh './gradlew build --rerun-tasks -x check'
                }
            }

            post {
                success {
                    archiveArtifacts artifacts: '**/build/libs/*.jar', fingerprint: true, onlyIfSuccessful: true

                    javadoc javadocDir: 'build/dokka/html/', keepAll: true
                }
            }
        }

        stage('Deploy to snapshots repositories') {
            when {
                allOf {
                    not { buildingTag() }
                    not { expression { env.TAG_NAME != null && env.TAG_NAME.matches('v\\d+\\.\\d+\\.\\d+') } }
                }
            }

            steps {
                withCredentials([
                        string(credentialsId: 'maven-signing-key', variable: 'ORG_GRADLE_PROJECT_signingKey'),
                        string(credentialsId: 'maven-signing-key-password', variable: 'ORG_GRADLE_PROJECT_signingPassword'),
                        usernamePassword(
                                credentialsId: 'solo-studios-maven',
                                passwordVariable: 'ORG_GRADLE_PROJECT_SoloStudiosSnapshotsPassword',
                                usernameVariable: 'ORG_GRADLE_PROJECT_SoloStudiosSnapshotsUsername'
                        )
                ]) {
                    withGradle {
                        sh './gradlew publishAllPublicationsToSoloStudiosSnapshotsRepository'
                    }
                }
            }
        }

        stage('Deploy to releases repositories') {
            when {
                allOf {
                    buildingTag()
                    expression { env.TAG_NAME != null && env.TAG_NAME.matches('v\\d+\\.\\d+\\.\\d+') }
                }
            }

            steps {
                withCredentials([
                        string(credentialsId: 'maven-signing-key', variable: 'ORG_GRADLE_PROJECT_signingKey'),
                        string(credentialsId: 'maven-signing-key-password', variable: 'ORG_GRADLE_PROJECT_signingPassword'),
                        usernamePassword(
                                credentialsId: 'solo-studios-maven',
                                passwordVariable: 'ORG_GRADLE_PROJECT_SoloStudiosReleasesPassword',
                                usernameVariable: 'ORG_GRADLE_PROJECT_SoloStudiosReleasesUsername'
                        ),
                        usernamePassword(
                                credentialsId: 'sonatype-maven-credentials',
                                passwordVariable: 'ORG_GRADLE_PROJECT_SonatypePassword',
                                usernameVariable: 'ORG_GRADLE_PROJECT_SonatypeUsername'
                        )
                ]) {
                    withGradle {
                        sh './gradlew publishAllPublicationsToSoloStudiosReleasesRepository'
                        sh './gradlew publishAllPublicationsToSonatypeRepository'
                    }
                }
            }
        }
    }

    post {
        always {
            discoverReferenceBuild()

            recordIssues(
                    aggregatingResults: true,
                    enabledForFailure: true,
                    minimumSeverity: 'ERROR',
                    sourceCodeEncoding: 'UTF-8',
                    checksAnnotationScope: 'ALL',
                    sourceCodeRetention: 'LAST_BUILD',
                    tools: [kotlin()]
            )

            cleanWs()
        }
    }
}
