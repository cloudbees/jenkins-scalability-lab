#!/bin/bash

source "../settings.sh"

# Launch worker
docker run --rm -d \
  --add-host jenkins:${IP_JENKINS_MASTER} --add-host gitserver:${IP_UTIL_CONTAINERS} \
  --name agent -l role=agent \
  -e "COMMAND_OPTIONS=-master http://jenkins:8080 -executors $(nproc) -description swarm-slave" \
  svanoort/jenkins-swarm-agent-mvn:1.0
