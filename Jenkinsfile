node {
    stage('SCM') {
        checkout scm
    }

    stage('Build') {
        dir('farmacia-service') {
            bat 'mvn clean package -DskipTests'
        }
    }

    stage('Test') {
        dir('farmacia-service') {
            bat 'mvn test'
        }
    }

    stage('SonarQube Analysis') {
        def mvn = tool 'Maven'
        withSonarQubeEnv('SonarQube') {
            dir('farmacia-service') {
                bat "${mvn}\\bin\\mvn sonar:sonar -Dsonar.projectKey=galenos-farmacia -Dsonar.projectName='GalenosPro Farmacia Service'"
            }
        }
    }

    stage('Quality Gate') {
        timeout(time: 2, unit: 'MINUTES') {
            waitForQualityGate abortPipeline: true
        }
    }
}