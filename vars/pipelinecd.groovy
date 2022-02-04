def call(){
  stage('Git Diff'){
    // sh 'git diff main'
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

  }
  stage("Merge develop"){

  }
  stage("Tag main"){

  }
}