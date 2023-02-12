# quarkus-request-intercept-jvm
Intercept every request before it reaches to REST resource
## How to deploy and run on minikube
Start the minikube and up all the resources. On terminal go to vidar-sandbox
```
kubectl apply -f ./k8-resources/quarkus-deployment.yml -n default
```

Check all the resources are up and running
```
kubectl get all -n <namespace> # default in case of minikube
```

In one terminal run service
```
minikube service vidar-sandbox-service-k8svc
```
another terminal window run minikube tunner
```
minikube tunnel
```

again another terminal expose the deployment
```
kubectl expose deployment vidar-sandbox-service-deployment --type=LoadBalancer --port=8080
```

