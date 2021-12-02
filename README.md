## Scope of use
This script can be used to install and deploy FullNode on Linux.

## Download Dockerfile and deploy.sh files

```shell
wget https://raw.githubusercontent.com/stabilaprotocol/stabila-deployment/master/Dockerfile -O Dockerfile
wget https://raw.githubusercontent.com/stabilaprotocol/stabila-deployment/master/deploy.sh -O deploy.sh
bash deploy.sh start
```
## Run deploy.sh script
```shell
bash deploy.sh start 8090
```

## Parameter Illustration

```shell
bash deploy.sh [start|stop|restart] port private-key

start Install (if not already installed) docker, fetch images, build local image and start FullNode.jar application.
stop Stop FullNode.jar application and related docker container.
restart Restart FullNode.jar application.
port required Port on which docker container will run
private-key optional Private key of executive for seed node, default is peer node
```

## Examples

### Deployment of FullNode on the one host.

```shell
bash deploy.sh start 8090
bash deploy.sh restart 8090
bash deploy.sh stop 8090
```
