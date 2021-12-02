## Scope of use
This script could be used to install and deploy FullNode on Linux.

## Download and run script

```shell
wget https://raw.githubusercontent.com/stabilaprotocol/stabila-deployment/master/Dockerfile -O Dockerfile
wget https://raw.githubusercontent.com/stabilaprotocol/stabila-deployment/master/deploy.sh -O deploy.sh
bash deploy.sh start
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
wget https://raw.githubusercontent.com/stabilaprotocol/StabilaDeployment/master/deploy.sh -O deploy.sh
bash deploy.sh start
bash deploy.sh restart
bash deploy.sh stop
```
