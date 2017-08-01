#!/bin/bash
set -xe
set -o pipefail

# Build for local run

# Create SSH key if absent, set up for secret
if [ ! -f id_rsa ]; then
  ssh-keygen -t rsa -n "" -P "" -C "" -f id_rsa
fi

# Create network if absent
if [ $(docker network ls | grep scalability-bridge | wc -l) -eq 0]; then
    docker network create --attachable -d bridge scalability-bridge || true
fi

docker build -t jenkins-scalability-master:2.0 ../jenkins

# Needed to pick up git folder, note that the docker-compose fails due to network
CONFIG_DIR=$(cd .. && pwd) # docker-compose up

# Start git server, see keys from https://github.com/jkarlosb/git-server-docker
cp id_rsa.pub gitserver/keys && cp id_rsa* jenkins
LOCAL_DIR="$(pwd)"
docker run --rm -d -p 2222:22 \
   --network scalability-bridge \
   -h gitserver \
   --name gitserver -l role=gitserver \
   -v "$LOCAL_DIR/gitserver/keys:/git-server/keys" \
   -v "$LOCAL_DIR/gitserver/repos:/git-server/repos" \
   jkarlos/git-server-docker

# Autoconnect agents
docker run -it --rm --network scalability-bridge \
  --name agent -l role=agent \
  -e "COMMAND_OPTIONS=-master http://jenkins:8080 -executors 4 -description swarm-slave -deleteExistingClients" \
  vfarcic/jenkins-swarm-agent

# Graphite server
docker run --rm -d --network scalability-bridge \
  -h graphite --name graphite \
  -p 81:80 \
  -p 2003:2003 \
  -p 8125:8125/udp \
  hopsoft/graphite-statsd

ROOT_BLKDEV=/dev/$(docker run --rm -it jenkins-scalability-master:1.0 lsblk -d -o NAME | tail -n 1 | tr -d '\r' | tr -d '\n')

# Run jenkins, specifying a named volume makes it persistent even after container dies
docker run -it --rm  -h jenkins --name jenkins -l role=jenkins --network scalability-bridge \
  --device-write-iops $ROOT_BLKDEV:200 --device-write-bps $ROOT_BLKDEV:100mb --device-read-iops $ROOT_BLKDEV:200 --device-read-bps $ROOT_BLKDEV:100mb \
  -p 8080:8080 -p 9011:9011 \
  -v jenkins_home:/var/jenkins_home \
  jenkins-scalability-master:2.0
#  -v $(pwd)/jenkins/jenkins_home:/var/jenkins_home \
