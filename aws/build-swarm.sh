#!/bin/bash
# Build for compose

# Create SSH key if absent, set up for secret
if [ ! -f id_rsa ]; then
  ssh-keygen -t rsa -n "" -P "" -C "" -f id_rsa
fi

# Build the docker swarm agent dockerfile if needed
# See https://hub.docker.com/r/vfarcic/jenkins-swarm-agent/ 
#  and https://wiki.jenkins-ci.org/display/JENKINS/Swarm+Plugin
docker pull vfarcic/jenkins-swarm-agent

# Build the Jenkins dockerfile
docker build -t jenkins-scalability-master:2.0 ../jenkins

# Start up a swarm
docker swarm init


# Launch compose
# Launch jenkins manually with resource limits
