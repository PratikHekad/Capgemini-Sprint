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
//  6. Reports       – mvn allure:report + publish test results
//  7. Archive       – screenshots, CSV, HTML reports
// ============================================================

pipeline {

    agent any

    // ── Build parameters ──────────────────────────────────────────────────────
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

    // ── Options ───────────────────────────────────────────────────────────────
    options {
        buildDiscarder(logRotator(numToKeepStr: '20'))
        timestamps()
        timeout(time: 60, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    // ── Triggers: nightly full run at 2 AM ───────────────────────────────────
    triggers {
        cron('0 2 * * *')
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
                always {
                    // Collect TestNG XML results — same workspace, so this works
                    junit(testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true)
                    echo 'Smoke Tests stage complete.'
                }
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
            post {
                always {
                    junit(testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true)
                }
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
            post {
                always {
                    junit(testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true)
                }
            }
        }

        // ── 6. Generate Allure Report (via Maven plugin — no Jenkins plugin needed)
        stage('Generate Report') {
            steps {
                echo 'Generating Allure HTML report via Maven...'
                bat 'mvn allure:report -q'
            }
        }

        // ── 7. Archive Artifacts ──────────────────────────────────────────────
        stage('Archive Artifacts') {
            steps {
                archiveArtifacts(
                    artifacts:         'target/site/allure-maven-plugin/**, target/cucumber-reports*.html, target/cucumber-smoke-report*.html, test-output/performance/perf-trend.csv, test-output/screenshots/**',
                    allowEmptyArchive: true
                )
                echo "Artifacts archived. Allure report: target/site/allure-maven-plugin/index.html"
            }
        }

    }
    // ═════════════════════════════════════════════════════════════════════════

    // ── Post-build (only echo messages — no file steps to avoid workspace mismatch)
    post {
        success {
            echo "BUILD SUCCESS - All tests passed on Build #${env.BUILD_NUMBER}"
        }
        failure {
            echo "BUILD FAILED - Check archived artifacts and console output"
        }
        unstable {
            echo "BUILD UNSTABLE - Some tests failed, check test results"
        }
        always {
            echo "Pipeline complete — Build #${env.BUILD_NUMBER}"
        }
    }
}
