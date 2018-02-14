#!/bin/bash

# Setup
sudo yum install -y docker && sudo service docker start && sudo su

# Launch our Jenkins instance or thereabouts
docker run --cap-add=SYS_PTRACE --rm -it -h jenkins --name jenkins -l role=jenkins \
  --add-host influx:10.0.0.13 --add-host gitserver:10.0.0.13 \
  -p 8080:8080 -p 9011:9011 -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  svanoort/jenkins-scalability-master:cheetah-11Feb-bleedingedge
