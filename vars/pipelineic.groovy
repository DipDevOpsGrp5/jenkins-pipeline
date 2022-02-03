def call(repositoryName){
    stage("Paso 1: Compilar"){
        sh "echo 'Compile Code!'"
        // Run Maven on a Unix agent.
        sh "mvn clean compile -e"
    }
    stage("Paso 2: Testear"){
            sh "echo 'Test Code!'"
            // Run Maven on a Unix agent.
            sh "mvn clean test -e"
    }
    stage("Paso 3: Build .Jar"){
            sh "echo 'Build .Jar!'"
            // Run Maven on a Unix agent.
            sh "mvn clean package -e"
    }
    stage("Paso 4: Análisis SonarQube"){
        withSonarQubeEnv('sonarqube') {
            sh "echo 'Calling sonar Service in another docker container!'"
            // Run Maven on a Unix agent to execute Sonar.
            def sonarName = repositoryName + "-${env.BRANCH_NAME}-${env.BUILD_NUMBER}"
            println(sonarName)
            sh 'mvn clean verify sonar:sonar -Dsonar.projectKey=github-sonar -Dsonar.projectName=' + sonarName 
        }
    }
    stage("Paso 4: Subir Nexus"){
        nexusPublisher nexusInstanceId: 'nexus',
        nexusRepositoryId: 'devops-laboratorio',
        packages: [
            [$class: 'MavenPackage',
                mavenAssetList: [
                    [classifier: '',
                    extension: 'jar',
                    filePath: 'build/DevOpsUsach2020-0.0.1.jar'
                ]
            ],
                mavenCoordinate: [
                    artifactId: 'DevOpsUsach2020',
                    groupId: 'com.devopsusach2020',
                    packaging: 'jar',
                    version: '0.0.1'
                ]
            ]
        ]
    }
    stage("Crear rama release"){
        sh 'ls'
    }
}
return this;