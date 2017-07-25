pipeline {
    agent any
    
    options {
        buildDiscarder(logRotator(numToKeepStr:'5'))
        }
    /*
    environment {
        VARIABLE = "SOMETHING"
    }
    */
    
    stages {
        stage ("netstat -a") {
            steps {
                sh 'netstat -a'
            }
        }
        
        stage ('echo \$PATH') {
            steps {
                sh 'echo $PATH'
            }
        }
        
        stage ('Output of \'set\'') {
            steps {
                sh 'set'
            }
        }
    } // end stages

    post {
        always {
            echo "Runs all the time."
        }
        success {
            echo "Whatever we did, it worked. Yay!"
        }
        failure {
            echo "Failed. Womp womp."
        }
    }
}