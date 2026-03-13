pipeline {
    agent any
    
    environment {
        DOCKER_IMAGE = "tglobe-app:latest"
        SONAR_URL = "http://localhost:9000" 
    }

    stages {
        stage('Compile & Test') {
            steps { 
                sh 'mvn clean package -DskipTests' 
            }
        }

        stage('SonarQube Analysis') {
            steps {
                script {
                    withSonarQubeEnv('SonarQube') {
                        sh 'mvn sonar:sonar'
                    }
                }
            }
        }

        stage('Build Image') {
            steps {
                sh "docker build -t ${DOCKER_IMAGE} ."
            }
        }

        stage('Deploy to Minikube') {
            steps {
                echo "Deploying Tglobe Load-Links to Port Harcourt Cluster..."
                sh 'kubectl apply -f k8s/deployment.yaml'
                sh 'kubectl apply -f k8s/service.yaml'
            }
        }
    }
    
    post {
        always {
            echo "Cleaning up workspace..."
            cleanWs()
        }
    }
}
