def call(pom_version, stages){
    def stagesList = stages.split(";")
    def listStagesOrder = [
        'compile': 'stageCompile',
        'test': 'stageTest',
        'build': 'stageBuild',
        'sonar': 'stageSonar',
        'upload_nexus': 'stageUploadNexus',
        'create_release_branch': 'stageCreateReleaseBranch'
    ]

    if (stages==""){
      executeAllStages(pom_version)
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

def executeAllStages(pom_version){
    echo "Ejecutando todos los stages..."
    stageCompile()
    stageTest()
    stageBuild()
    stageSonar()
    stageUploadNexus()
    if("${env.BRANCH_NAME}" == 'develop'){
      stageCreateReleaseBranch(pom_version)
    }
}

def stageCompile() {
    env.DESCRIPTION_STAGE = "Paso 1: Compilar"
    stage("${env.DESCRTIPTION_STAGE}"){
        env.STAGE = "compile - ${env.DESCRTIPTION_STAGE}"
        sh "echo  ${env.STAGE}"
        sh "mvn clean compile -e"
    }
}

def stageTest() {
    env.DESCRIPTION_STAGE = "Paso 2: Testear"
    stage("${env.DESCRTIPTION_STAGE}"){
        env.STAGE = "test - ${env.DESCRTIPTION_STAGE}"
        sh "echo  ${env.STAGE}"
        sh "mvn clean test -e"
    }
}

def stageBuild() {
    env.DESCRIPTION_STAGE = "Paso 3: Build jar"
    stage("${env.DESCRTIPTION_STAGE}"){
        env.STAGE = "build - ${env.DESCRTIPTION_STAGE}"
        sh "echo  ${env.STAGE}"
        sh "mvn clean package -e"
    }
}

def stageSonar() {
    env.DESCRIPTION_STAGE = "Paso 4: Análisis SonarQube"
    stage("${env.DESCRTIPTION_STAGE}"){
        env.STAGE = "sonar - ${env.DESCRTIPTION_STAGE}"
        withSonarQubeEnv('sonarqube') {
            sh "echo  ${env.STAGE}"
            def sonarName = "repositoryName" + "-${env.BRANCH_NAME}-${env.BUILD_NUMBER}"
            println(sonarName)
            sh 'mvn clean verify sonar:sonar -Dsonar.projectKey=github-sonar -Dsonar.projectName=' + sonarName 
        }
    }
}

def stageUploadNexus() {
  env.DESCRIPTION_STAGE = "Paso 5: Subir Nexus"
  stage("${env.DESCRTIPTION_STAGE}"){
        env.STAGE = "upload_nexus - ${env.DESCRTIPTION_STAGE}"
        sh "echo  ${env.STAGE}"
        nexusPublisher nexusInstanceId: 'nexus',
        nexusRepositoryId: 'devops-laboratorio',
        packages: [
            [$class: 'MavenPackage',
                mavenAssetList: [
                    [classifier: '',
                    extension: 'jar',
                    filePath: "build/DevOpsUsach2020-${pom_version}.jar"
                ]
            ],
                mavenCoordinate: [
                    artifactId: 'DevOpsUsach2020',
                    groupId: 'com.devopsusach2020',
                    packaging: 'jar',
                    version: "${pom_version}"
                ]
            ]
        ]
    }
}

def createPullRequest(pom_version) {
    sh "echo 'CI pipeline success'"
    PR_NUMBER = sh (
        script: 
            """
                curl -X POST -d '{"title":"PR branch $BRANCH_NAME", "body": "$pom_version", "head":"$BRANCH_NAME","base":"develop"}' -H "Accept 'application/vnd.github.v3+json'" -H "Authorization: token $GITHUB_TOKEN" https://api.github.com/repos/DipDevOpsGrp5/ms-iclab/pulls | jq '.number'
            """,
        returnStdout: true
    ).trim()
    print('PR_NUMBER: ' + PR_NUMBER)
    sh """
        curl -X POST -H "Accept: application/vnd.github.v3+json" -H "Authorization: token $GITHUB_TOKEN" https://api.github.com/repos/DipDevOpsGrp5/ms-iclab/pulls/$PR_NUMBER/requested_reviewers -d '{"reviewers":["aarevalo2017","hrojasb", "fran-fcam", "estebanmt", "ClaudioCorreaR"]}'
    """
}

def stageCreateReleaseBranch(pom_version) {
  env.DESCRIPTION_STAGE = "Paso 6: Crear rama release"
  stage("Crear rama release"){
    env.STAGE = "create_release_branch - ${env.DESCRTIPTION_STAGE}"
    sh "echo  ${env.STAGE}"
    SHA = sh (
        script:
            """
                curl -H "Authorization: token $GITHUB_TOKEN" https://api.github.com/repos/DipDevOpsGrp5/ms-iclab/git/refs/heads/$BRANCH_NAME | jq -r '.object.sha'
            """,
        returnStdout: true
    ).trim()

    print (SHA)
    def branchVersion = pom_version.replaceAll("\\.","-")
    sh (
        script:
        """
            curl -X POST -H "Accept 'application/vnd.github.v3+json'" -H "Authorization: token $GITHUB_TOKEN"  https://api.github.com/repos/DipDevOpsGrp5/ms-iclab/git/refs -d '{"ref": "refs/heads/release-v$branchVersion", "sha": "$SHA"}'
        """,
    )
  }
}
