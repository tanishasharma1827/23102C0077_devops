// Jenkinsfile - Sports Facility Booking System
// Task 8: pipeline as code - checkout, build, package, deploy to Tomcat (parameterised environment).
// The Jenkins agent is Windows, so shell steps use 'bat'.

pipeline {
    agent any

    parameters {
        choice(name: 'DEPLOY_ENV', choices: ['staging', 'qa'], description: 'Environment name shown by /actuator/info')
        string(name: 'TOMCAT_PORT', defaultValue: '8082', description: 'Host port for the Tomcat server')
    }

    triggers {
        pollSCM('H/2 * * * *')   // check GitHub for new commits every 2 minutes
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '5'))
        timestamps()
        disableConcurrentBuilds()
    }

    tools {
        maven 'Maven'            // Maven installation configured in Manage Jenkins > Tools
    }

    environment {
        // Docker Desktop is installed for the user, so add its CLI to PATH for the Jenkins service
        PATH = "C:\\Users\\TANISHA SHARMA\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin;${env.PATH}"
        TOMCAT_CONTAINER = "tomcat-${params.DEPLOY_ENV}"
        APP_URL = "http://localhost:${params.TOMCAT_PORT}/sports-booking"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                bat 'git log -1 --oneline'
            }
        }

        stage('Build') {
            steps {
                bat 'mvn -B clean compile'
            }
        }

        stage('Unit Tests') {
            steps {
                bat 'mvn -B test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                bat 'mvn -B package -DskipTests'
                archiveArtifacts artifacts: 'target/sports-booking.war', fingerprint: true
            }
        }

        stage('Deploy to Tomcat') {
            steps {
                bat """
                    docker rm -f %TOMCAT_CONTAINER% 2>nul
                    docker run -d --name %TOMCAT_CONTAINER% -p ${params.TOMCAT_PORT}:8080 ^
                        -e CATALINA_OPTS="-Dinfo.app.environment=${params.DEPLOY_ENV} -Dinfo.app.build=${env.BUILD_NUMBER}" ^
                        tomcat:10.1-jdk17-temurin
                    docker cp target\\sports-booking.war %TOMCAT_CONTAINER%:/usr/local/tomcat/webapps/sports-booking.war
                """
                script {
                    waitForHealth("${env.APP_URL}/actuator/health")
                }
                bat "curl -s ${env.APP_URL}/actuator/info"
                echo "Deployed to Tomcat (${params.DEPLOY_ENV}): ${env.APP_URL}/"
            }
        }
    }

    post {
        success {
            echo "Pipeline OK - build #${env.BUILD_NUMBER} is running at ${env.APP_URL}/"
        }
        failure {
            echo 'Pipeline FAILED - see the stage that is red.'
        }
    }
}

// Polls a health URL until it returns {"status":"UP"} (up to about 2 minutes).
def waitForHealth(String url) {
    for (int i = 1; i <= 24; i++) {
        def code = bat(returnStatus: true, script: "@curl -fs ${url} | findstr UP >nul")
        if (code == 0) {
            echo "Health check passed: ${url}"
            return
        }
        sleep 5
    }
    error("Health check failed: ${url}")
}
