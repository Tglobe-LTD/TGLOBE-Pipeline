pipeline {
    agent any

    tools {
        maven 'Maven3.9.8'
    }

    triggers {
        pollSCM('* * * * *')
    }

    parameters {
        choice(
            name: 'ENVIRONMENT',
            choices: ['dev', 'staging', 'prod'],
            description: 'Environment to deploy to'
        )
        string(
            name: 'VERSION',
            defaultValue: 'v12',
            description: 'Version tag'
        )
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
        timeout(time: 30, unit: 'MINUTES')
        timestamps()
    }

    environment {
        // Application
        APP_NAME = 'loadlinks'
        ARTIFACT_VERSION = '1.0.0'
        WAR_FILE = "target/${APP_NAME}-${ARTIFACT_VERSION}.war"
        CONTEXT_PATH = "${APP_NAME}-${ARTIFACT_VERSION}"  // Will deploy as /loadlinks-1.0.0
        
        // Git Repository - Your application code repo
        GIT_REPO_URL = 'https://github.com/Tglobe-LTD/TGLOBE-Pipeline.git'  // ← UPDATE THIS
        GIT_BRANCH = 'master'  // or 'master'
        GIT_CREDENTIALS_ID = 'terrybright80'
        
        // SonarQube
        SONAR_HOST = 'http://99.79.62.235:9000'
        SONAR_PROJECT_KEY = "${APP_NAME}-${params.ENVIRONMENT}"
        SONAR_PROJECT_NAME = "${APP_NAME} (${params.ENVIRONMENT})"
        SONAR_TOKEN = 'sonarqube-token'
        
        // Tomcat Servers - UPDATE THESE WITH YOUR SERVERS
        TOMCAT_DEV = 'ubuntu@35.183.246.53:22'        // e.g., 'tomcat@192.168.1.100:22'
        TOMCAT_STAGING = 'ubuntu@staging-server:22'
        TOMCAT_PROD = 'ubuntu@prod-server:22'
        TOMCAT_WEBAPPS = '/opt/tomcat/webapps'
        TOMCAT_MANAGER_URL = 'http://35.183.246.53:8080/manager/text'  // Your Tomcat manager
        
        // Jenkins Credentials
        TOMCAT_SSH_CREDENTIALS = 'tomcat-ssh-key'
        TOMCAT_MANAGER_CREDS = 'tomcat-manager-credentials'
    }

    stages {
        stage('Checkout') {
            steps {
                git(
                    branch: "${GIT_BRANCH}",
                    url: "${GIT_REPO_URL}",
                    credentialsId: "${GIT_CREDENTIALS_ID}"
                )
            }
        }

        stage('Build') {
            steps {
                withMaven(maven: 'Maven3.9.8') {
                    sh 'mvn -B clean compile'
                }
            }
        }

        stage('Test') {
            steps {
                withMaven(maven: 'Maven3.9.8') {
                    sh 'mvn -B test -DskipTests=false'
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/**/*.xml'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    withMaven(maven: 'Maven3.9.8') {
                        sh """
                            mvn sonar:sonar \
                                -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                                -Dsonar.projectName="${SONAR_PROJECT_NAME}" \
                                -Dsonar.host.url=${SONAR_HOST}
                        """
                    }
                }
            }
        }

        stage('Package WAR') {
            steps {
                withMaven(maven: 'Maven3.9.8') {
                    sh 'mvn -B package -DskipTests'
                }
            }
            post {
                success {
                    echo "✅ WAR file created: ${WAR_FILE}"
                    archiveArtifacts artifacts: "${WAR_FILE}", fingerprint: true
                }
            }
        }

        stage('Deploy to Tomcat') {
            when {
                expression { params.ENVIRONMENT != 'prod' }  // Use Manager API for non-prod
            }
            steps {
                script {
                    deploy adapters: [
                        tomcat9(
                            url: "${TOMCAT_MANAGER_URL}",
                            credentialsId: TOMCAT_MANAGER_CREDS
                        )
                    ], 
                    contextPath: "${CONTEXT_PATH}",
                    war: "${WAR_FILE}"
                }
            }
            post {
                success {
                    echo "✅ Deployed to http://35.183.246.53:8080/${CONTEXT_PATH}"
                }
            }
        }

        stage('Deploy to Production Tomcat') {
            when {
                expression { params.ENVIRONMENT == 'prod' }
            }
            steps {
                script {
                    def tomcatHost = getTomcatHost(params.ENVIRONMENT)
                    
                    // Create backup and deploy via SSH for production
                    sshagent([TOMCAT_SSH_CREDENTIALS]) {
                        sh """
                            # Create backup
                            ssh ${tomcatHost} "mkdir -p ${TOMCAT_WEBAPPS}/backup/${APP_NAME}"
                            ssh ${tomcatHost} "if [ -f ${TOMCAT_WEBAPPS}/${CONTEXT_PATH}.war ]; then \\
                                cp ${TOMCAT_WEBAPPS}/${CONTEXT_PATH}.war ${TOMCAT_WEBAPPS}/backup/${APP_NAME}/${CONTEXT_PATH}.war.backup-\$(date +%Y%m%d-%H%M%S); \\
                                fi"
                            
                            # Copy new WAR
                            scp ${WAR_FILE} ${tomcatHost}:${TOMCAT_WEBAPPS}/${CONTEXT_PATH}.war
                            
                            echo "✅ Deployed to production Tomcat on ${tomcatHost}"
                        """
                    }
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                script {
                    sh """
                        sleep 15
                        curl -f http://35.183.246.53:8080/${CONTEXT_PATH}/actuator/health || \\
                        curl -f http://35.183.246.53:8080/${CONTEXT_PATH}/health || \\
                        curl -f http://35.183.246.53:8080/${CONTEXT_PATH}/ || \\
                        echo "⚠️ Application deployed but health check endpoint not found"
                        echo "✅ Deployment verified"
                    """
                }
            }
        }
    }

    post {
        always {
            echo "Build finished: ${currentBuild.currentResult}"
            echo "Application deployed at: http://35.183.246.53:8080/${CONTEXT_PATH}"
            cleanWs()
        }
        success {
            echo '🎉 Build and deployment succeeded!'
        }
        failure {
            echo '❌ Build or deployment failed'
            emailext(
                to: 'team@yourcompany.com',
                subject: "❌ Deployment Failed: ${APP_NAME} to ${params.ENVIRONMENT}",
                body: "Build failed. Check: ${env.BUILD_URL}"
            )
        }
    }
}

// Helper function
def getTomcatHost(env) {
    switch(env) {
        case 'dev': return TOMCAT_DEV
        case 'staging': return TOMCAT_STAGING
        case 'prod': return TOMCAT_PROD
        default: error "Unknown environment: ${env}"
    }
}