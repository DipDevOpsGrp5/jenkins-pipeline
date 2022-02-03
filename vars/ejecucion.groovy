def call()
{
  pipeline {
      agent any
      environment {
          NEXUS_USER         = credentials('NEXUS-USER')
          NEXUS_PASSWORD     = credentials('NEXUS-PASS')
      }
      parameters {
          choice(
              name:'pipeline',
              choices: ['pipeline-IC', 'pipeline-CD'],
              description: 'Seleccione pipeline'
          )
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
                    sh env
                    def branch = "${env.BRANCH_NAME}"
                    if (branch.startsWith('feature-') || branch == 'develop') {
                        pipeline-ic.call(repositoryName)
                    }
                    if (branch.startsWith('release-')){
                        pipeline-cd.call()
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