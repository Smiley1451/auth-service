pipeline {
    agent any

    environment {
        IMAGE_NAME = "smileyishere1008/auth-service"
        IMAGE_TAG = "latest"
        DOCKER_REGISTRY = "https://index.docker.io/v1/"
        CREDENTIALS_ID = "77f23b29-8987-4bda-b46b-ab30db0d3610"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'master', url: 'https://github.com/Smiley1451/auth-service.git'
            }
        }

        stage('Build') {
            agent {
                docker {
                    image 'maven:3.9.6-eclipse-temurin-17'
                    args '-v /var/run/docker.sock:/var/run/docker.sock'
                }
            }
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Test') {
            agent {
                docker {
                    image 'maven:3.9.6-eclipse-temurin-17'
                }
            }
            steps {
                sh 'mvn test'
            }
        }

        stage('Build Docker Image') {
            steps {
                sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} ."
            }
        }

        stage('Push to Docker Hub') {
            steps {
                script {
                    withDockerRegistry([url: "${DOCKER_REGISTRY}", credentialsId: "${CREDENTIALS_ID}"]) {
                        sh "docker push ${IMAGE_NAME}:${IMAGE_TAG}"
                    }
                }
            }
        }
    }

    post {
        success {
            echo '✅ Pipeline completed successfully!'
        }
        failure {
            echo '❌ Pipeline failed.'
        }
    }
}
