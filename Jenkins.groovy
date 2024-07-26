pipeline{
    agent any
    environment{
        ACR_AUTH = credentials('acr_creds')
        ACR_LOGIN_SERVER = 'vijay.azurecr.io'
        REGISTRY_NAME = 'vijay'
        REPO_NAME = 'youtu'
    }
    stages{
        stage('git checkout'){
            steps{
                checkout scmGit(branches:[[name: '*/main']], extensions: [], userRemoteConfigs: [[url: 'https://github.com/vijay-3sc/a-youtube-clone-app']])
            }
        }
        stage('build docker image'){
            steps{
                sh 'docker buildx prune  --force'
                sh 'docker build -t ${ACR_LOGIN_SERVER}/${REPO_NAME}:$BUILD_NUMBER .'
            }
        }
        stage('push image'){
            steps{
                withCredentials([usernamepassword(credentialsId: 'acr_creds', passwordVariable: 'password', usernameVariable:'username')]){
                sh 'docker login -u ${username} -p ${password} ${ACR_LOGIN_SERVER}'
                sh 'docker push ${ACR_LOGIN_SERVER}/${REPO_NAME}:$BUILD_NUMBER'

                }
                
            }
        }
    }
}