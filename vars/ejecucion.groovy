def call()
{
  pipeline {
      agent any
      environment {
          NEXUS_USER         = credentials('NEXUS-USER')
          NEXUS_PASSWORD     = credentials('NEXUS-PASS')
          GITHUB_TOKEN       = credentials('token_github')
          
      }
      parameters {
        text description: 'Enviar los stages separados por ";" ... vacío si necesita todos los stages', name: 'stages'
      }
      stages {
          stage("Pipeline"){
              steps {
                  script{
                    def branch = "${env.BRANCH_NAME}"
                    def pom = readMavenPom file: 'pom.xml'
                    def pom_version = pom.version
                    env.POM_VERSION = pom.version
                    env.STAGE="" // Usar este para mensaje slack
                    env.DESCRIPTION_STAGE=""
                    println(branch)
                    echo branch
                    if (branch.startsWith('feature-') || branch == 'develop') {
                        pipelineic.call(params.stages)
                    }
                    if (branch.startsWith('release-')){
                        pipelinecd.call(params.stages)
                    }
                  }
              }
              post{
                  success{
                      slack.call('PASS')
                      //slackSend color: 'good', message: "[Esteban Meza] [${JOB_NAME}] [${BUILD_TAG}] Ejecucion Exitosa", teamDomain: 'dipdevopsusac-tr94431', tokenCredentialId: 'token-slack'
                  }
                  failure{
                      slack.call('FAIL')
                      //slackSend color: 'danger', message: "[Esteban Meza] [${env.JOB_NAME}] [${BUILD_TAG}] Ejecucion fallida", teamDomain: 'dipdevopsusac-tr94431', tokenCredentialId: 'token-slack'
                  }
              }
          }
      }
  }
}