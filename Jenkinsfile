pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    stages {
        stage('SCM') {
            steps {
                checkout scm
            }
        }

        stage('Build auth-service') {
            steps {
                dir('auth-service') {
                    bat 'mvn clean package -DskipTests'
                }
            }
        }

        stage('SonarQube auth-service') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    dir('auth-service') {
                        bat 'mvn sonar:sonar -Dsonar.projectKey=galenos-auth -DskipTests'
                    }
                }
            }
        }

        stage('Build farmacia-service') {
            steps {
                dir('farmacia-service') {
                    bat 'mvn clean package -DskipTests'
                }
            }
        }

        stage('SonarQube farmacia-service') {
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