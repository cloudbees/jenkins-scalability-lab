#!/bin/bash
set -xe

source "../settings.sh"
AGENT_NUMBER=${1:-0}

# Launch worker
docker run --rm -d \
  --add-host jenkins:${IP_JENKINS_MASTER} --add-host gitserver:${IP_UTIL_CONTAINERS} \
  --name agent_${AGENT_NUMBER} -l role=agent \
  -e "COMMAND_OPTIONS=-master http://jenkins:8080 -executors $(nproc) -description swarm-slave" \
  svanoort/jenkins-swarm-agent-mvn:1.0
