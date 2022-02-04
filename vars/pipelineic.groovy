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
            stages.each{ stageToExecute ->//variable as param
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
    env.STAGE = "compile"
    stage("Paso 1: Compilar"){
        sh "echo 'Compile Code!'"
        // Run Maven on a Unix agent.
        sh "mvn clean compile -e"
    }
}

def stageTest() {
    stage("Paso 2: Testear"){
        sh "echo 'Test Code!'"
        // Run Maven on a Unix agent.
        sh "mvn clean test -e"
    }
}

def stageBuild() {
    stage("Paso 3: Build .Jar"){
        sh "echo 'Build .Jar!'"
        // Run Maven on a Unix agent.
        sh "mvn clean package -e"
    }
}

def stageSonar() {
    stage("Paso 4: Análisis SonarQube"){
        withSonarQubeEnv('sonarqube') {
            sh "echo 'Calling sonar Service in another docker container!'"
            // Run Maven on a Unix agent to execute Sonar.
            def sonarName = "repositoryName" + "-${env.BRANCH_NAME}-${env.BUILD_NUMBER}"
            println(sonarName)
            sh 'mvn clean verify sonar:sonar -Dsonar.projectKey=github-sonar -Dsonar.projectName=' + sonarName 
        }
    }
}

def stageUploadNexus() {
  stage("Paso 4: Subir Nexus"){
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
  stage("Crear rama release"){
    sh "echo 'CI pipeline success'"
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
