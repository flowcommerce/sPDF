pipeline {
  agent {
    kubernetes {
      inheritFrom 'default'
      containerTemplates([
      containerTemplate(name: 'play', image: 'flowdocker/play_builder:latest-java17-jammy', command: 'cat', ttyEnabled: true),
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
      //when { branch 'main' }
      steps {
        script {
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
