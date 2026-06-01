# 1. Set your credentials
export SCW_SECRET_KEY="your-secret-key"
export REGISTRY_ENDPOINT="rg.nl-ams.scw.cloud"
export NAMESPACE="funcscwnscinqgptw8bb14edb"
export MICROSERVICE_NAME="vestaboard-service"

# 2. Log in to Scaleway Container Registry
echo $SCW_SECRET_KEY | docker login $REGISTRY_ENDPOINT/$NAMESPACE -u nologin --password-stdin

# 3. Build your Docker image
docker build -t $MICROSERVICE_NAME:latest .

# 4. Tag the image for Scaleway
docker tag my-microservice:latest $REGISTRY_ENDPOINT/$NAMESPACE/$MICROSERVICE_NAME:latest

# 5. Push to registry
docker push $REGISTRY_ENDPOINT/$NAMESPACE/$MICROSERVICE_NAME:latest

# 6. Verify (optional)
docker pull $REGISTRY_ENDPOINT/$NAMESPACE/$MICROSERVICE_NAME:latest