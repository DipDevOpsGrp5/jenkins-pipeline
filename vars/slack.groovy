def call(String result) {

  if (!env.STAGE) {
    env.STAGE = 'Validación Final'
  }

  def pipeline = env.BRANCH_NAME.startsWith("release-v") ? "Release" : "IC"

  if ( result == "PASS" ) {
    slackSend (color: "good", message: "[Grupo5] [Pipeline: ${pipeline}] [Rama: ${env.BRANCH_NAME}] [Stage: ${env.STAGE}] [Resultado: Pass]")
  }
  if( result == "FAIL" ) {
    slackSend (color: "danger", message: "[Grupo5] [Pipeline: ${pipeline}] [Rama: ${env.BRANCH_NAME}] [Stage: ${env.STAGE}] [Resultado: Fail]")
  }
}