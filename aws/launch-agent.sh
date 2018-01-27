#!/bin/bash

# Setup
sudo yum install -y docker && sudo service docker start && sudo su

# Launch worker
docker run --rm -it \
  --add-host jenkins:10.0.0.7 --add-host gitserver:10.0.0.13 \
  --name agent -l role=agent \
  -e "COMMAND_OPTIONS=-master http://jenkins:8080 -executors 4 -description swarm-slave" \
  svanoort/jenkins-swarm-agent-mvn:1.0