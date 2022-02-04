def call()
{
  pipeline {
      agent any
      environment {
          NEXUS_USER         = credentials('NEXUS-USER')
          NEXUS_PASSWORD     = credentials('NEXUS-PASS')
          GITHUB_TOKEN       = credentials('token_github')
      }
      stages {
          stage("Pipeline"){
              steps {
                  script{
                    // switch(params.pipeline)
                    //   {
                    //       case 'pipeline-IC':
                    //           pipeline-ic.call()
                    //       break;
                    //       case 'pipeline-CD':
                    //           pipeline-cd.call()
                    //       break;
                    //   }
                    def branch = "${env.BRANCH_NAME}"
                    def pom = readMavenPom file: 'pom.xml'
                    def pom_version = pom.version
                    println(branch)
                    echo branch
                    if (branch.startsWith('feature-') || branch == 'develop') {
                        pipelineic.call(pom_version)
                    }
                    if (branch.startsWith('release-')){
                        pipelinecd.call(pom_version)
                    }
                  }
              }
              // post{
              //     success{
              //         slackSend color: 'good', message: "[Esteban Meza] [${JOB_NAME}] [${BUILD_TAG}] Ejecucion Exitosa", teamDomain: 'dipdevopsusac-tr94431', tokenCredentialId: 'token-slack'
              //     }
              //     failure{
              //         slackSend color: 'danger', message: "[Esteban Meza] [${env.JOB_NAME}] [${BUILD_TAG}] Ejecucion fallida", teamDomain: 'dipdevopsusac-tr94431', tokenCredentialId: 'token-slack'
              //     }
              // }
          }
      }
  }
}