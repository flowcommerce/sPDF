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
            try {
              sh '''
                sbt clean coverage test:compile test
                sbt coverageAggregate
              '''
            } finally {
                junit allowEmptyResults: true, testResults: '**/target/test-reports/*.xml'
                step([$class: 'ScoveragePublisher', reportDir: 'target/scala-2.13/scoverage-report', reportFile: 'scoverage.xml'])
                publishHTML (target : [allowMissing: false,
                 alwaysLinkToLastBuild: true,
                 keepAll: true,
                 reportDir: 'target/scala-2.13/scoverage-report',
                 reportFiles: 'index.html',
                 reportName: 'Scoverage Code Coverage',
                 reportTitles: 'Scoverage Code Coverage'])
            }
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
            syncDependencyLibrary()
          }
        }
      }
    }
  }
}
