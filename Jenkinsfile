// Jenkins Declarative Pipeline equivalent of .github/workflows/ci.yml.
// Kept alongside GitHub Actions (the CI actually enforced on this repo) to demonstrate
// Jenkinsfile/Groovy DSL fluency for orgs that run Jenkins on-prem instead of a SaaS CI.
pipeline {
    agent any

    tools {
        jdk 'temurin-21'
        nodejs 'node20'
    }

    environment {
        SONAR_TOKEN = credentials('sonar-token')
    }

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Trivy vulnerability scan') {
            steps {
                sh '''
                    trivy fs --scanners vuln --severity CRITICAL,HIGH \
                        --exit-code 1 --ignore-unfixed --format table .
                '''
            }
        }

        stage('Backend — test & coverage') {
            steps {
                dir('api') {
                    sh './gradlew test jacocoTestCoverageVerification --no-daemon'
                }
            }
        }

        stage('Frontend — typecheck, lint, test') {
            steps {
                dir('web') {
                    sh '''
                        pnpm install --frozen-lockfile
                        pnpm exec tsc --noEmit
                        pnpm lint
                        pnpm test:coverage
                    '''
                }
            }
        }

        stage('Frontend — E2E Playwright') {
            steps {
                dir('web') {
                    sh '''
                        pnpm exec playwright install --with-deps chromium
                        pnpm test:e2e
                    '''
                }
            }
        }

        stage('Docker Compose gateway smoke test') {
            steps {
                sh '''
                    docker compose up -d --build
                    for i in $(seq 1 30); do
                        curl -sf http://localhost/api/v1/q/health/live | grep -q '"status": "UP"' && exit 0
                        sleep 5
                    done
                    docker compose logs
                    exit 1
                '''
            }
            post {
                always {
                    sh 'docker compose down -v'
                }
            }
        }

        stage('SonarCloud analysis') {
            when {
                branch 'main'
            }
            steps {
                withSonarQubeEnv('SonarCloud') {
                    dir('api') {
                        sh './gradlew sonar --no-daemon'
                    }
                }
            }
        }

        stage('Docker build & push') {
            when {
                branch 'main'
            }
            steps {
                sh '''
                    docker build -t ghcr.io/apchavez/product-api:latest ./api
                    docker build -t ghcr.io/apchavez/product-web:latest ./web
                '''
            }
        }
    }

    post {
        always {
            junit testResults: '**/build/test-results/**/*.xml', allowEmptyResults: true
            archiveArtifacts artifacts: 'web/playwright-report/**', allowEmptyArchive: true
        }
    }
}
