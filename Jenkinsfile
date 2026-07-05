node {
    stage('SCM') {
        checkout scm
    }

    stage('Build') {
        dir('farmacia-service') {
            bat 'mvn clean package -DskipTests'
        }
    }

    stage('SonarQube Analysis') {
        def mvn = tool 'Maven'
        withSonarQubeEnv('SonarQube') {
            dir('farmacia-service') {
                bat "\\bin\\mvn sonar:sonar -Dsonar.projectKey=galenos-farmacia -Dsonar.projectName='GalenosPro Farmacia Service' -DskipTests"
            }
        }
    }
}
