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
                checkout scmGit(branches:[[name: '*/main']], extensions: [], userRemoteConfigs: [[url: 'https://github.com/vijay-3sc/a-youtube-clone-app']]){
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
                sh 'docker login -u ${username} -p ${password} ${ACR_LOGIN_SERVER}'
                sh 'docker push ${ACR_LOGIN_SERVER}/${REPO_NAME}:$BUILD_NUMBER'

                }
                
            }
        stage('deploy web app'){
            steps{
                withCredentials([azureServicePrincipal('AZURE_SERVICE_PRINCIPAL')]) {
                sh 'az login --service-principal -u ${AZURE_CLIENT_ID} -p ${AZURE_CLIENT_SECRET} --tenant ${AZURE_TENANT_ID}'
                }
                withCredentials([usernamePassword(credentialsId: 'acrprincipal', passwordVariable: 'password', usernameVariable: 'username')]) {
                sh 'az webapp config container set --name ${REGISTRY_NAME} --resource-group payment --docker-custom-image-name ${ACR_LOGIN_SERVER}/${REPO_NAME}:$BUILD_NUMBER --docker-registry-server-url https://vijay.azurecr.io --docker-registry-server-user ${username} --docker-registry-server-password ${password}'
                }
            }
            
        }   
        
    }

}
}