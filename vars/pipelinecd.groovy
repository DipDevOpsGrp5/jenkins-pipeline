def call(stages){
  def stagesList = stages.split(";")
  def listStagesOrder = [
      'git_diff': 'stageGitDiff',
      'download_nexus': 'stageDownloadNexus',
      'run_jar': 'stageRunJar',
      'curl_jar': 'stageCurlJar',
      'merge_main': 'stageMergeMain',
      'merge_develop': 'stageMergeDevelop',
      'tag_main': 'stageTagMain'
  ]

  if (stages==""){
    executeAllStages()
  }
  else {
    echo 'Stages a ejecutar :' + stages
    listStagesOrder.each { stageName, stageFunction ->
      stagesList.each{ stageToExecute ->//variable as param
        if(stageName.equals(stageToExecute)){
          echo 'Ejecutando ' + stageFunction
          "${stageFunction}"()
        }
      }
    }
  } 
}
return this;

def executeAllStages(){
  echo "Ejecutando todos los stages..."
  stageGitDiff();
  stageDownloadNexus();
  stageRunJar();
  stageCurlJar();
  stageMergeMain();
  stageMergeDevelop();
  stageTagMain();
}

def stageGitDiff(){
    env.DESCRIPTION_STAGE = "Paso 0: Git Diff"
    stage("${env.DESCRIPTION_STAGE}"){
      env.STAGE = "git_diff - ${env.DESCRIPTION_STAGE}"
      sh "echo  ${env.STAGE}"
      // TODO: ingresar código de git diff
      }

}

def stageDownloadNexus(){
    env.DESCRIPTION_STAGE = "Paso 1: Descargar Nexus"
    stage("${env.DESCRIPTION_STAGE}"){
      env.STAGE = "download_nexus - ${env.DESCRIPTION_STAGE}"
      sh "echo  ${env.STAGE}"
      sh "curl -X GET -u $NEXUS_USER:$NEXUS_PASSWORD 'http://nexus:8081/repository/devops-laboratorio/com/devopsusach2020/DevOpsUsach2020/${env.POM_VERSION}/DevOpsUsach2020-${env.POM_VERSION}.jar' -O"
    }
}

def stageRunJar(){
    env.DESCRIPTION_STAGE = "Paso 2: Correr Jar"
    stage("${env.DESCRIPTION_STAGE}"){
      env.STAGE = "run_jar - ${env.DESCRIPTION_STAGE}"
      sh "echo  ${env.STAGE}"
      sh "nohup java -jar DevOpsUsach2020-${env.POM_VERSION}.jar & >/dev/null"
  }
}

def stageCurlJar(){
    env.DESCRIPTION_STAGE = "Paso 3: Testear Artefacto - Dormir(Esperar 20sg)"
    stage("${env.DESCRIPTION_STAGE}"){
      env.STAGE = "curl_jar - ${env.DESCRIPTION_STAGE}"
      sh "echo  ${env.STAGE}"
      sh "sleep 20 && curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
  }
}

def stageMergeMain(){
    env.DESCRIPTION_STAGE = "Paso 4: Merge a main"
    stage("${env.DESCRIPTION_STAGE}"){
      env.STAGE = "merge_main - ${env.DESCRIPTION_STAGE}"
      sh "echo  ${env.STAGE}"
      mergeBranch("main")
    }
}

def stageMergeDevelop(){
    env.DESCRIPTION_STAGE = "Paso 4: Merge a develop"
    stage("${env.DESCRIPTION_STAGE}"){
      env.STAGE = "merge_develop - ${env.DESCRIPTION_STAGE}"
      sh "echo  ${env.STAGE}"
      mergeBranch("main")
    }
}

def stageTagMain(){
    env.DESCRIPTION_STAGE = "Paso 4: Tag main"
    stage("${env.DESCRIPTION_STAGE}"){
      env.STAGE = "tag_main - ${env.DESCRIPTION_STAGE}"
      sh "echo  ${env.STAGE}"
      sh "echo  ${env.POM_VERSION}"
      tagMainBranch()
    }
}


def mergeBranch(String baseBranch) {
    print ("merging branch" + BRANCH_NAME + ' into ' + baseBranch)
    SHA = sh (
        script:
            """
                curl -H "Authorization: token $GITHUB_TOKEN" https://api.github.com/repos/DipDevOpsGrp5/ms-iclab/git/refs/heads/$BRANCH_NAME | jq -r '.object.sha'
            """,
        returnStdout: true
    ).trim()

    print (SHA)

    sh """
        curl -X POST -H "Accept 'application/vnd.github.v3+json'" -H "Authorization: token $GITHUB_TOKEN" https://api.github.com/repos/DipDevOpsGrp5/ms-iclab/merges -d '{"head":"$SHA","base":"$baseBranch"}'
    """
}

def tagMainBranch() {
    print ("tagging main branch")
    SHA = sh (
        script:
            """
                curl -H "Authorization: token $GITHUB_TOKEN" https://api.github.com/repos/DipDevOpsGrp5/ms-iclab/git/refs/heads/main | jq -r '.object.sha'
            """,
        returnStdout: true
    ).trim()

    print (SHA)

    SHA_TAG = sh (
        script:
        """
            curl -X POST -H "Accept 'application/vnd.github.v3+json'" -H "Authorization: token $GITHUB_TOKEN" https://api.github.com/repos/DipDevOpsGrp5/ms-iclab/git/tags -d '{"tag":"$env.POM_VERSION", "message":"$env.POM_VERSION", "object": "$SHA", "type": "commit"}' | jq -r '.sha'
        """,
        returnStdout: true

    ).trim()

    print('SHA_TAG: ' + SHA_TAG)

    sh """
        curl -X POST -H "Accept 'application/vnd.github.v3+json'" -H "Authorization: token $GITHUB_TOKEN" https://api.github.com/repos/DipDevOpsGrp5/ms-iclab/git/refs -d '{"ref":"refs/tags/$env.POM_VERSION", "sha": "$SHA_TAG"}'
    """
}