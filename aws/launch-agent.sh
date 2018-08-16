#!/bin/bash

# Setup
sudo yum install -y docker && sudo service docker start && sudo su

# Launch worker
docker run --rm -d \
  --add-host jenkins:10.0.0.12 --add-host gitserver:10.0.0.168 \
  --name agent -l role=agent \
  -e "COMMAND_OPTIONS=-master http://jenkins:8080 -executors $(nproc) -description swarm-slave" \
  svanoort/jenkins-swarm-agent-mvn:1.0
