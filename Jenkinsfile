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
            steps {
                script {
                    docker.image('maven:3.9.6-eclipse-temurin-17').inside {
                        sh 'chmod +x mvnw' // ensure it's executable
                        sh './mvnw clean package -DskipTests'
                    }
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    docker.image('maven:3.9.6-eclipse-temurin-17').inside {
                        sh './mvnw test'
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'chmod +x mvnw' // if mvnw is used in Dockerfile, ensure it's executable
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
