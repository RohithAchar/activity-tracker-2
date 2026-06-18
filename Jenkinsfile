pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'activity-tracker'
        DOCKER_TAG = "${BUILD_NUMBER}-${GIT_COMMIT.take(7)}"
        REGISTRY = 'docker.io/yourusername'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean compile -q'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test -q'
            }
        }

        stage('Package') {
            steps {
                sh 'mvn package -DskipTests -q'
            }
        }

        stage('Docker Build') {
            steps {
                script {
                    docker.build("${REGISTRY}/${DOCKER_IMAGE}:${DOCKER_TAG}")
                    docker.build("${REGISTRY}/${DOCKER_IMAGE}:latest")
                }
            }
        }

        stage('Docker Push') {
            steps {
                script {
                    docker.withRegistry('', 'docker-hub-credentials') {
                        docker.image("${REGISTRY}/${DOCKER_IMAGE}:${DOCKER_TAG}").push()
                        docker.image("${REGISTRY}/${DOCKER_IMAGE}:latest").push()
                    }
                }
            }
        }
    }

    post {
        failure {
            emailext(
                subject: "Build Failed: ${env.JOB_NAME} - ${env.BUILD_NUMBER}",
                body: "Build ${env.BUILD_NUMBER} failed. Check: ${env.BUILD_URL}",
                to: 'team@example.com'
            )
        }
        success {
            echo "Build ${env.BUILD_NUMBER} succeeded. Image: ${REGISTRY}/${DOCKER_IMAGE}:${DOCKER_TAG}"
        }
    }
}
