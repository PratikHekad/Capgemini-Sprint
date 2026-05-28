// ============================================================
//  Jenkinsfile — Notes App Automation (Capstone T7B)
//  Pipeline: Declarative | Agent: Windows
//
//  Stages
//  ──────
//  1. Checkout      – pull source from GitHub
//  2. Compile       – mvn compile (verify build is clean)
//  3. Smoke Tests   – @smoke tagged scenarios only
//  4. Full Suite    – all features + PerformanceTest
//  5. Parallel Run  – 3-thread concurrent execution
//  6. Allure Report – generate HTML report
//  7. Archive       – screenshots, CSV, HTML reports
//
//  NOTE: Credentials (test.email / test.password) are read
//  directly from src/test/resources/config.properties by the
//  framework. No Jenkins credential store needed for local runs.
// ============================================================

pipeline {

    agent any

    // ── Build parameters (shown on "Build with Parameters" page) ─────────────
    parameters {
        choice(
            name:        'SUITE',
            choices:     ['smoke', 'full', 'parallel'],
            description: 'smoke = @smoke tags only | full = all tests | parallel = 3 threads'
        )
        booleanParam(
            name:         'HEADLESS',
            defaultValue: true,
            description:  'Run Chrome headless (true recommended on CI)'
        )
        string(
            name:         'BROWSER',
            defaultValue: 'chrome',
            description:  'Browser: chrome or firefox'
        )
    }

    // ── Environment ───────────────────────────────────────────────────────────
    environment {
        ALLURE_RESULTS = 'target/allure-results'
    }

    // ── Triggers: nightly full run at 2 AM ───────────────────────────────────
    triggers {
        cron('0 2 * * *')
    }

    // ── Options ───────────────────────────────────────────────────────────────
    options {
        buildDiscarder(logRotator(numToKeepStr: '20'))
        timestamps()
        timeout(time: 60, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    // ═════════════════════════════════════════════════════════════════════════
    stages {

        // ── 1. Checkout ───────────────────────────────────────────────────────
        stage('Checkout') {
            steps {
                checkout scm
                echo "Build #${env.BUILD_NUMBER} | Branch: ${env.GIT_BRANCH ?: 'main'}"
                bat 'java -version'
                bat 'mvn -version'
            }
        }

        // ── 2. Compile ────────────────────────────────────────────────────────
        stage('Compile') {
            steps {
                bat 'mvn clean compile test-compile -q'
            }
        }

        // ── 3. Smoke Tests ────────────────────────────────────────────────────
        stage('Smoke Tests') {
            when {
                anyOf {
                    not { branch 'main' }
                    expression { return params.SUITE == 'smoke' }
                }
            }
            steps {
                echo 'Running Smoke Suite (@smoke scenarios only)...'
                bat "mvn test -Dsurefire.suiteXmlFiles=src/test/resources/testng-smoke.xml -Dbrowser=${params.BROWSER} -Dheadless=${params.HEADLESS} -q"
            }
            post {
                always { echo 'Smoke Tests stage complete.' }
            }
        }

        // ── 4. Full Regression ────────────────────────────────────────────────
        stage('Full Regression') {
            when {
                anyOf {
                    branch 'main'
                    expression { return params.SUITE == 'full' }
                    triggeredBy 'TimerTrigger'
                }
            }
            steps {
                echo 'Running Full Regression Suite...'
                bat "mvn test -Dsurefire.suiteXmlFiles=src/test/resources/testng.xml -Dbrowser=${params.BROWSER} -Dheadless=${params.HEADLESS} -q"
            }
        }

        // ── 5. Parallel Suite ─────────────────────────────────────────────────
        stage('Parallel Suite') {
            when {
                expression { return params.SUITE == 'parallel' }
            }
            steps {
                echo 'Running Parallel Suite (3 browser threads)...'
                bat "mvn test -Dsurefire.suiteXmlFiles=src/test/resources/testng-parallel.xml -Dbrowser=${params.BROWSER} -Dheadless=${params.HEADLESS} -q"
            }
        }

        // ── 6. Allure Report ──────────────────────────────────────────────────
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

        // ── 7. Archive Artifacts ──────────────────────────────────────────────
        stage('Archive Artifacts') {
            steps {
                archiveArtifacts(
                    artifacts:         'target/cucumber-reports*.html, target/cucumber-smoke-report*.html, test-output/performance/perf-trend.csv, test-output/screenshots/**',
                    allowEmptyArchive: true
                )
            }
        }

    }
    // ═════════════════════════════════════════════════════════════════════════

    // ── Post-build ────────────────────────────────────────────────────────────
    post {

        always {
            // junit and bat need an active node/workspace context — wrap in node
            node('') {
                junit(
                    testResults:       'target/surefire-reports/*.xml',
                    allowEmptyResults: true
                )
                bat 'taskkill /F /IM chromedriver.exe 2>nul & exit 0'
                bat 'taskkill /F /IM chrome.exe 2>nul & exit 0'
            }
            echo "Pipeline complete — Build #${env.BUILD_NUMBER}"
        }

        success {
            echo "BUILD SUCCESS - All tests passed on Build #${env.BUILD_NUMBER}"
        }

        failure {
            echo "BUILD FAILED - Check Allure report and console output"
        }

        unstable {
            echo "BUILD UNSTABLE - Some tests failed, check Allure report"
        }
    }
}
