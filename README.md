## Linux installation and deploy
Docker file Dockerfile provides execution environment for FullNode.
Bash script deploy.sh manages installation, deploy and lifecycle of FullNode execution.

## Download Dockerfile and deploy.sh files

```shell
wget https://raw.githubusercontent.com/stabilaprotocol/stabila-deployment/master/Dockerfile -O Dockerfile
wget https://raw.githubusercontent.com/stabilaprotocol/stabila-deployment/master/deploy.sh -O deploy.sh
```
## Run deploy.sh script
```shell
bash deploy.sh start 8090
```

## Parameter Illustration

```shell
bash deploy.sh [start|stop|restart] port private-key

start : install (if not already installed) docker, fetch images, build local image and start FullNode.jar application.
stop : stop FullNode.jar application and related docker container.
restart : restart FullNode.jar application.
port : required, port on which docker container will run
private-key : optional, executive private key for seed node, default is peer node
```

## Examples

### Scripts execution

```shell
bash deploy.sh start 8090
bash deploy.sh restart 8090
bash deploy.sh stop 8090
```

### Test execution
```shell
bash deploy.sh start 8090
curl -X POST -k http://127.0.0.1:8090/wallet/listexecutives
```
If you get executive-list json data then FullNode started successfully.

## Windows installation and deploy

## Download and install Docker
Download Docker from https://download.docker.com/win/static/stable/x86_64/docker-20.10.9.zip
Unzip and open docker folder
Double-click docker.exe to run the installer.
Follow the Install Wizard: accept the license, authorize the installer, and proceed with the install.
Click Finish to launch Docker.
Docker starts automatically.
Docker loads a “Welcome” window giving you tips and access to the Docker documentation.

## Download Dockerfile and deploy.sh files

```shell
curl.exe --url https://raw.githubusercontent.com/stabilaprotocol/stabila-deployment/master/Dockerfile --output Dockerfile
curl.exe --url https://raw.githubusercontent.com/stabilaprotocol/stabila-deployment/master/deploy.ps1 --output deploy.ps1
```

## Run deploy.ps1 script
```shell
bash deploy.ps1 start 8090
```

## Parameter Illustration

```shell
.\deploy.ps1 [start|stop|restart] port private-key

start : install (if not already installed) docker, fetch images, build local image and start FullNode.jar application.
stop : stop FullNode.jar application and related docker container.
restart : restart FullNode.jar application.
port : required, port on which docker container will run
private-key : optional, executive private key for seed node, default is peer node
```

## Examples

### Scripts execution

```shell
.\deploy.ps1 start 8090
.\deploy.ps1 restart 8090
.\deploy.ps1 stop 8090
```
