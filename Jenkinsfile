pipeline {
    agent any

    stages {
        stage('SCM') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                dir('farmacia-service') {
                    bat 'mvn clean package -DskipTests'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    dir('farmacia-service') {
                        bat 'mvn sonar:sonar -Dsonar.projectKey=galenos-farmacia -DskipTests'
                    }
                }
            }
        }
    }
}