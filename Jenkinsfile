pipeline {
    agent any

    tools {
        // 📝 MUST CONFIGURE: This must match the Maven name in Jenkins
        // Go to: Jenkins → Manage Jenkins → Tools → Maven
        // Create entry named exactly: 'Maven3.9.8'
        maven 'Maven3.9.8'
    }

    triggers {
        // Poll SCM every minute
        pollSCM('* * * * *')
    }

    parameters {
        choice(
            name: 'DEPLOYMENT_TYPE',
            choices: ['traditional-tomcat', 'docker', 'both'],
            description: 'Select deployment target'
        )
        choice(
            name: 'ENVIRONMENT',
            choices: ['dev', 'staging', 'prod'],
            description: 'Environment to deploy to'
        )
        string(
            name: 'VERSION',
            defaultValue: 'v12',
            description: 'Version tag for Docker image'
        )
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
        timeout(time: 30, unit: 'MINUTES')
        timestamps()
    }

    environment {
        // ============ 📝 CONFIGURE THESE VALUES ============
        
        // Application
        APP_NAME = 'tglobe-app'
        WAR_FILE = 'target/*.war'
        
        // 📝 GIT REPOSITORY CONFIGURATION
        // This is where your Git repo URL goes
        GIT_REPO_URL = 'git credentialsId: 'terrybright80', url: 'https://github.com/Tglobe-LTD/TGLOBE-Pipeline.git'  // ← CHANGE THIS
        GIT_BRANCH = 'master'  // or 'master' - CHANGE if needed
        GIT_CREDENTIALS_ID = 'github-https-pat'  // ← Your Jenkins credential ID for GitHub
        
        // SonarQube
        // 📝 SONARQUBE SERVER CONFIGURATION
        SONAR_HOST = 'http://99.79.62.235:9000'  // ← CHANGE THIS
        SONAR_PROJECT_KEY = "${APP_NAME}-${params.ENVIRONMENT}"
        SONAR_PROJECT_NAME = "${APP_NAME} (${params.ENVIRONMENT})"
        
        // 📝 TOMCAT SERVERS CONFIGURATION
        // Traditional Tomcat servers (SSH format: user@host:port)
        TOMCAT_DEV = 'tomcat@dev-server:22'        // ← CHANGE THIS
        TOMCAT_STAGING = 'tomcat@staging-server:22' // ← CHANGE THIS
        TOMCAT_PROD = 'tomcat@prod-server:22'       // ← CHANGE THIS
        
        // 📝 TOMCAT WEBAPPS PATH
        // This is where your WAR file goes on the Tomcat server
        TOMCAT_WEBAPPS = '/opt/tomcat/webapps'  // ← VERIFY this matches your Tomcat setup
        
        // Docker
        // 📝 DOCKER REGISTRY CONFIGURATION
        DOCKER_REGISTRY = 'your-registry.com'       // ← CHANGE THIS
        DOCKER_IMAGE = "${DOCKER_REGISTRY}/${APP_NAME}:${params.VERSION}"
        DOCKER_IMAGE_LATEST = "${DOCKER_REGISTRY}/${APP_NAME}:latest"
        
        // Kubernetes
        K8S_NAMESPACE = "${APP_NAME}-${params.ENVIRONMENT}"
        K8S_DEPLOYMENT = K8S_DEPLOYMENT = "${APP_NAME}"
        // ====================================================
        
        // Jenkins credentials IDs - 📝 VERIFY these exist in Jenkins
        TOMCAT_SSH_CREDENTIALS = 'tomcat-ssh-key'           // ← SSH key for Tomcat servers
        DOCKER_CREDENTIALS = 'docker-registry-credentials'  // ← Docker login credentials
        SONAR_TOKEN = credentials('sonarqube-token')        // ← SonarQube token
    }

    stages {
        stage('Checkout') {
            steps {
                // 📝 GIT CHECKOUT - This is where your Git repo is actually used
                // Using the variables defined above
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
                    sh 'mvn -B -DskipTests clean install'
                }
            }
        }

        stage('Test') {
            steps {
                withMaven(maven: 'Maven3.9.8') {
                    sh 'mvn -B test'
                }
            }
            post {
                always {
                    junit 'target/surefire-reports/**/*.xml'
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

        stage('Quality Gate') {
            when {
                expression { params.ENVIRONMENT == 'prod' }
            }
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Package') {
            steps {
                withMaven(maven: 'Maven3.9.8') {
                    sh 'mvn -B package -DskipTests'
                }
            }
        }

        stage('Archive') {
            when {
                expression { fileExists('target/*.war') || fileExists('target/*.jar') }
            }
            steps {
                archiveArtifacts artifacts: 'target/*.war,target/*.jar', fingerprint: true
            }
        }

        stage('Docker Build') {
            when {
                expression { params.DEPLOYMENT_TYPE in ['docker', 'both'] }
            }
            steps {
                script {
                    // 📝 TOMCAT IN DOCKER - The WAR file goes here
                    writeFile file: 'Dockerfile', text: """
FROM tomcat:9.0-jdk11
LABEL version="${params.VERSION}" environment="${params.ENVIRONMENT}"
# 📝 This copies your WAR into the Tomcat container
COPY target/*.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \\
  CMD curl -f http://localhost:8080/health || exit 1
CMD ["catalina.sh", "run"]
"""
                    
                    sh """
                        docker build -t ${DOCKER_IMAGE} .
                        docker tag ${DOCKER_IMAGE} ${DOCKER_IMAGE_LATEST}
                    """
                }
            }
        }

        stage('Docker Push') {
            when {
                expression { params.DEPLOYMENT_TYPE in ['docker', 'both'] }
            }
            steps {
                script {
                    docker.withRegistry("https://${DOCKER_REGISTRY}", DOCKER_CREDENTIALS) {
                        sh "docker push ${DOCKER_IMAGE}"
                        sh "docker push ${DOCKER_IMAGE_LATEST}"
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            when {
                expression { params.DEPLOYMENT_TYPE in ['docker', 'both'] }
            }
            steps {
                script {
                    sh """
                        kubectl create namespace ${K8S_NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -
                        
                        cat << EOF | kubectl apply -f -
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ${K8S_DEPLOYMENT}
  namespace: ${K8S_NAMESPACE}
spec:
  replicas: ${params.ENVIRONMENT == 'prod' ? 3 : 1}
  selector:
    matchLabels:
      app: ${K8S_DEPLOYMENT}
  template:
    metadata:
      labels:
        app: ${K8S_DEPLOYMENT}
        version: ${params.VERSION}
    spec:
      containers:
      - name: ${K8S_DEPLOYMENT}
        image: ${DOCKER_IMAGE}
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: ${params.ENVIRONMENT}
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
---
apiVersion: v1
kind: Service
metadata:
  name: ${K8S_DEPLOYMENT}
  namespace: ${K8S_NAMESPACE}
spec:
  selector:
    app: ${K8S_DEPLOYMENT}
  ports:
  - port: 80
    targetPort: 8080
  type: ${params.ENVIRONMENT == 'prod' ? 'LoadBalancer' : 'ClusterIP'}
EOF
                        kubectl rollout status deployment/${K8S_DEPLOYMENT} -n ${K8S_NAMESPACE} --timeout=3m
                    """
                }
            }
        }

stage('Deploy to Traditional Tomcat') {
    when {
        expression { params.DEPLOYMENT_TYPE in ['traditional-tomcat', 'both'] }
    }
    stages {
        stage('Deploy via SSH (Backup Only)') {
            when {
                expression { params.ENVIRONMENT == 'prod' } // Only backup via SSH for prod
            }
            steps {
                script {
                    def tomcatHost = getTomcatHost(params.ENVIRONMENT)
                    sshagent([TOMCAT_SSH_CREDENTIALS]) {
                        sh """
                            # Create backup only (no deployment)
                            ssh ${tomcatHost} "mkdir -p ${TOMCAT_WEBAPPS}/backup/${APP_NAME}"
                            ssh ${tomcatHost} "if [ -f ${TOMCAT_WEBAPPS}/ROOT.war ]; then \\
                                cp ${TOMCAT_WEBAPPS}/ROOT.war ${TOMCAT_WEBAPPS}/backup/${APP_NAME}/ROOT.war.backup-\$(date +%Y%m%d-%H%M%S); \\
                                fi"
                            echo "✅ Backup created on ${tomcatHost}"
                        """
                    }
                }
            }
        }
        
        stage('Deploy via Tomcat Manager API') {
            steps {
                script {
                    // ============ TOMCAT MANAGER API DEPLOYMENT ============
                    // This uses the Deploy to Container plugin
                    deploy adapters: [
                        tomcat9(
                            // Your Tomcat server URL with manager text interface
                            url: 'http://35.183.246.53:8009/manager/text',  // ← USE YOUR SERVER IP
                            // Or use HTTP port if manager is on 8080:
                            // url: 'http://35.183.246.53:8080/manager/text',
                            
                            // Credentials ID from Jenkins (must have manager-script role)
                            credentialsId: 'tomcat-manager-credentials'  // ← CREATE THIS IN JENKINS
                        )
                    ], 
                    // Context path - where your app will be accessible
                    // Use '/' for root, or 'myapp' for /myapp
                    contextPath: "${APP_NAME}",  // Will be accessible at http://server:port/tglobe-app
                    
                    // Your WAR file
                    war: 'target/*.war'
                    // ======================================================
                }
            }
            post {
                success {
                    echo "✅ Deployed via Manager API to http://35.183.246.53:8009/${APP_NAME}"
                }
            }
        }
        
        stage('Verify Deployment') {
            steps {
                script {
                    sh """
                        sleep 10
                        curl -f http://35.183.246.53:8009/${APP_NAME} || \\
                        curl -f http://35.183.246.53:8009/${APP_NAME}/health || \\
                        echo "⚠️ App deployed but health check failed"
                    """
                }
            }
        }
    }
}

        stage('Smoke Tests') {
            when {
                expression { params.DEPLOYMENT_TYPE != 'none' }
            }
            steps {
                script {
                    def testUrl = getTestUrl(params.ENVIRONMENT, params.DEPLOYMENT_TYPE)
                    sh """
                        sleep 15
                        curl -f http://${testUrl}/health || curl -f http://${testUrl}/ || exit 1
                        echo "✅ Smoke tests passed"
                    """
                }
            }
        }
    }

    post {
        always {
            echo "Build finished: ${currentBuild.currentResult}"
            echo "SonarQube Report: ${SONAR_HOST}/dashboard?id=${SONAR_PROJECT_KEY}"
            cleanWs()
        }

        success {
            echo '🎉 Build and deployment succeeded!'
        }

        failure {
            echo '❌ Build or deployment failed'
            // 📝 EMAIL RECIPIENTS - CHANGE THIS
            emailext(
                to: 'team@yourcompany.com',  // ← CHANGE THIS to your team email
                subject: "❌ Deployment Failed: ${APP_NAME} to ${params.ENVIRONMENT}",
                body: "Build failed. Check: ${env.BUILD_URL}"
            )
        }

        unstable {
            echo '⚠️ Build is unstable (tests failed)'
        }
    }
}

// ============ HELPER FUNCTIONS ============

def getTomcatHost(env) {
    switch(env) {
        case 'dev': return TOMCAT_DEV
        case 'staging': return TOMCAT_STAGING
        case 'prod': return TOMCAT_PROD
        default: error "Unknown environment: ${env}"
    }
}

def getTestUrl(env, deploymentType) {
    if (deploymentType in ['docker', 'both']) {
        return "${env == 'prod' ? 'your-prod-domain.com' : 'localhost:8080'}"
    } else {
        switch(env) {
            case 'dev': return 'dev-tomcat:8080'
            case 'staging': return 'staging-tomcat:8080'
            case 'prod': return 'prod-tomcat:8080'
            default: return 'localhost:8080'
        }
    }
}