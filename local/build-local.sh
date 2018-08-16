#!/bin/bash
set -xe
set -o pipefail

# Build for local run

# Create SSH key if absent, set up for secret
if [ ! -f id_rsa ]; then
  ssh-keygen -t rsa -n "" -P "" -C "" -f id_rsa
  chmod 600 id_rsa*
fi

# Create network if absent
if [ $(docker network ls | grep scalability-bridge | wc -l) -eq 0 ]; then
    docker network create --attachable -d bridge scalability-bridge
fi

# Needed to pick up git configs and some other things
CONFIG_DIR=$(cd .. && pwd)
mkdir -p "${CONFIG_DIR}/gitserver/keys/"
cp id_rsa.pub "${CONFIG_DIR}/gitserver/keys/"
cp id_rsa* "${CONFIG_DIR}/jenkins/"
docker build -t temp-jenkins-scalability-master:1.0 ../jenkins
docker build -t temp-gitserver:1.0 ../gitserver
docker build -t temp-grafana:1.0 ../grafana
docker build -t temp-buildagent:1.0 ../buildagent
