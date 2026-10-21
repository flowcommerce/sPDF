pipeline {
  agent {
    kubernetes {
      inheritFrom 'default'
      containerTemplates([
      containerTemplate(name: 'play', image: '479720515435.dkr.ecr.us-east-1.amazonaws.com/flowcommerce/play_builder_java17_jammy:latest', command: 'cat', ttyEnabled: true),
      ])
    }
  }
  
  options { 
    disableConcurrentBuilds() 
  }

  stages {
    stage('Checkout') {
      steps {
        checkoutWithTags scm
      }
    }

    stage('Tag new version') {
      when { branch 'main' }
      steps {
        script {
          sh '''
            git config user.email "tech@flow.io"
            git config user.name "flow-tech"
          '''
          VERSION = new flowSemver().calculateSemver()
          new flowSemver().commitSemver(VERSION)
        }
      }
    }

    stage('SBT Test') {
      steps {
        container('play') {
          script {
            sh '''
              sbt clean test:compile test
            '''
          }
        }
      }
    }

    stage('Release') {
      when { branch 'main' }
      steps {
        container('play') {
          withCredentials([
            usernamePassword(
              credentialsId: 'jenkins-x-jfrog',
              usernameVariable: 'ARTIFACTORY_USERNAME',
              passwordVariable: 'ARTIFACTORY_PASSWORD'
            )
          ]) {
            sh 'sbt clean +publish'
          }
        }
      }
    }
  }
}
