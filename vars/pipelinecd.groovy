def call(){
  stage('Git Diff'){
    // sh 'git diff $BRANCH_NAME main'
    // def pom = readMavenPom file: 'pom.xml'
    // def POM_VERSION = pom.version

    // echo "$POM_VERSION"
    echo env.POM_VERSION
  }
  stage("Paso 5: Descargar Nexus"){
      sh ' curl -X GET -u $NEXUS_USER:$NEXUS_PASSWORD "http://nexus:8081/repository/devops-laboratorio/com/devopsusach2020/DevOpsUsach2020/0.0.1/DevOpsUsach2020-0.0.1.jar" -O'
  }
  stage("Paso 6: Levantar Artefacto Jar"){
      sh 'nohup java -jar DevOpsUsach2020-0.0.1.jar & >/dev/null'
  }
  stage("Paso 7: Testear Artefacto - Dormir(Esperar 20sg) "){
      sh "sleep 20 && curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
  }
  stage("Merge main"){
    //mergeBranch(main)
  }
  stage("Merge develop"){
    //mergeBranch(develop)
  }
  stage("Tag main"){

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

// def tagMainBranch() {
//     print ("tagging main branch")
//     SHA = sh (
//         script:
//             """
//                 curl -H "Authorization: token $GITHUB_TOKEN" https://api.github.com/repos/DipDevOpsGrp5/ms-iclab/git/refs/heads/main | jq -r '.object.sha'
//             """
//         returnStdout: true
//     ).trim()

//     print (SHA)

//     SHA_TAG = sh (
//         script:
//         """
//             curl -X POST -H "Accept 'application/vnd.github.v3+json'" -H "Authorization: token $GITHUB_TOKEN" https://api.github.com/repos/DipDevOpsGrp5/ms-iclab/git/tags -d '{"tag":"$ARTIFACT_VERSION", "message":"$ARTIFACT_VERSION", "object": "$SHA", "type": "commit"}' | jq -r '.sha'
//         """,
//         returnStdout: true

//     ).trim()

//     print('SHA_TAG: ' + SHA_TAG)

//     sh """
//         curl -X POST -H "Accept 'application/vnd.github.v3+json'" -H "Authorization: token $GITHUB_TOKEN" https://api.github.com/repos/DipDevOpsGrp5/ms-iclab/git/refs -d '{"ref":"refs/tags/$ARTIFACT_VERSION", "sha": "$SHA_TAG"}'
//     """
// }