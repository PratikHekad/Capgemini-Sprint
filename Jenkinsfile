// ============================================================
//  Jenkinsfile — Notes App Automation (Capstone T7B)
//  Pipeline: Declarative
//
//  Stages
//  ──────
//  1. Checkout          – pull source from SCM
//  2. Build / Compile   – mvn compile (no tests yet)
//  3. Smoke Tests       – @smoke scenarios only (~2-3 min)
//  4. Full Regression   – all features + PerformanceTest
//  5. Parallel Run      – optional: full suite with 3 threads
//  6. Allure Report     – generate & publish HTML report
//  7. Archive Artifacts – screenshots, CSV trend, raw results
//
//  Triggers
//  ────────
//  • On every push / PR  → Smoke only
//  • Nightly (2 AM)      → Full Regression (sequential)
//  • Manual parameter    → choose which suite to run
//
//  Prerequisites on Jenkins controller/agent
//  ─────────────────────────────────────────
//  • JDK 21 installed, configured as tool "JDK-21"
//  • Maven 3.9+ installed, configured as tool "Maven-3.9"
//  • Allure plugin installed (for allure {} step)
//  • Chrome + chromedriver available (WebDriverManager handles download)
//  • Credentials: "notes-app-credentials" (Username+Password)
//    storing the test account email and password
// ============================================================

pipeline {

    agent any

    // ── Build-level parameters ───────────────────────────────────────────────
    parameters {
        choice(
            name:        'SUITE',
            choices:     ['smoke', 'full', 'parallel'],
            description: 'Which TestNG suite to run.\n' +
                         '  smoke    → @smoke scenarios only\n' +
                         '  full     → all scenarios + performance tests\n' +
                         '  parallel → all scenarios, 3 browser threads'
        )
        booleanParam(
            name:         'HEADLESS',
            defaultValue: true,
            description:  'Run Chrome in headless mode (recommended on CI)'
        )
        string(
            name:         'BROWSER',
            defaultValue: 'chrome',
            description:  'Browser to use (chrome / firefox)'
        )
    }

    // ── Environment variables ────────────────────────────────────────────────
    environment {
        // Map the Jenkins credential to the properties the framework reads
        NOTES_CREDENTIALS = credentials('notes-app-credentials')
        // These override config.properties values at runtime
        TEST_EMAIL    = "${NOTES_CREDENTIALS_USR}"
        TEST_PASSWORD = "${NOTES_CREDENTIALS_PSW}"

        // Allure results destination (matches pom.xml surefire config)
        ALLURE_RESULTS = 'target/allure-results'

        // Resolve suite XML from the SUITE parameter
        SUITE_XML = "${params.SUITE == 'smoke' ? 'src/test/resources/testng-smoke.xml' : params.SUITE == 'parallel' ? 'src/test/resources/testng-parallel.xml' : 'src/test/resources/testng.xml'}"
    }

    // ── Triggers ─────────────────────────────────────────────────────────────
    triggers {
        // Nightly full regression at 02:00
        cron('0 2 * * *')
    }

    // ── Options ──────────────────────────────────────────────────────────────
    options {
        buildDiscarder(logRotator(numToKeepStr: '20'))
        timestamps()
        timeout(time: 60, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    // ════════════════════════════════════════════════════════════════════════
    stages {

        // ── Stage 1: Checkout ────────────────────────────────────────────────
        stage('Checkout') {
            steps {
                checkout scm
                echo "Branch: ${env.GIT_BRANCH ?: 'local'} | Build: ${env.BUILD_NUMBER}"
                bat 'java -version'
                bat 'mvn -version'
            }
        }

        // ── Stage 2: Compile ─────────────────────────────────────────────────
        stage('Compile') {
            steps {
                bat 'mvn clean compile test-compile -q'
            }
        }

        // ── Stage 3: Smoke Tests (always runs on PR / push) ─────────────────
        stage('Smoke Tests') {
            when {
                anyOf {
                    not { branch 'main' }
                    expression { params.SUITE == 'smoke' }
                }
            }
            steps {
                echo "Running Smoke Suite (testng-smoke.xml) ..."
                bat "mvn test -Dsurefire.suiteXmlFiles=src/test/resources/testng-smoke.xml -Dbrowser=${params.BROWSER} -Dheadless=${params.HEADLESS} -Dtest.email=%TEST_EMAIL% -Dtest.password=%TEST_PASSWORD% -q"
            }
            post {
                always {
                    echo "Smoke stage complete — collecting results ..."
                }
            }
        }

        // ── Stage 4: Full Regression ─────────────────────────────────────────
        stage('Full Regression') {
            when {
                anyOf {
                    branch 'main'
                    expression { params.SUITE == 'full' }
                    triggeredBy 'TimerTrigger'
                }
            }
            steps {
                echo "Running Full Suite (testng.xml) ..."
                bat "mvn test -Dsurefire.suiteXmlFiles=src/test/resources/testng.xml -Dbrowser=${params.BROWSER} -Dheadless=${params.HEADLESS} -Dtest.email=%TEST_EMAIL% -Dtest.password=%TEST_PASSWORD% -q"
            }
        }

        // ── Stage 5: Parallel Suite ──────────────────────────────────────────
        stage('Parallel Suite') {
            when {
                expression { params.SUITE == 'parallel' }
            }
            steps {
                echo "Running Parallel Suite (testng-parallel.xml, 3 threads) ..."
                bat "mvn test -Dsurefire.suiteXmlFiles=src/test/resources/testng-parallel.xml -Dbrowser=${params.BROWSER} -Dheadless=${params.HEADLESS} -Dtest.email=%TEST_EMAIL% -Dtest.password=%TEST_PASSWORD% -q"
            }
        }

        // ── Stage 6: Allure Report ───────────────────────────────────────────
        stage('Allure Report') {
            steps {
                allure([
                    includeProperties: true,
                    jdk:               '',
                    properties:        [],
                    reportBuildPolicy: 'ALWAYS',
                    results:           [[path: "${ALLURE_RESULTS}"]]
                ])
            }
        }

        // ── Stage 7: Archive Artifacts ───────────────────────────────────────
        stage('Archive Artifacts') {
            steps {
                archiveArtifacts(
                    artifacts:         'target/cucumber-reports*.html, target/cucumber-smoke-report*.html, test-output/performance/perf-trend.csv, test-output/screenshots/**',
                    allowEmptyArchive: true
                )
            }
        }

    }
    // ════════════════════════════════════════════════════════════════════════

    // ── Post-build actions ────────────────────────────────────────────────────
    post {

        always {
            junit(
                testResults:          'target/surefire-reports/*.xml',
                allowEmptyResults:    true,
                skipPublishingChecks: false
            )
            echo "Pipeline finished — build #${env.BUILD_NUMBER}"
        }

        success {
            echo "All tests PASSED on build #${env.BUILD_NUMBER}"
        }

        failure {
            echo "Build FAILED — check Allure report and screenshots"
        }

        unstable {
            echo "Build UNSTABLE — some tests failed (check Allure for details)"
        }

        cleanup {
            // Kill any leftover browser/driver processes on Windows
            bat 'taskkill /F /IM chromedriver.exe 2>nul || exit 0'
            bat 'taskkill /F /IM chrome.exe 2>nul || exit 0'
        }
    }
}
