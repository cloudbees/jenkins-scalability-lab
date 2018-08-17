#!/bin/bash

# Launch our Jenkins instance or thereabouts
docker run --cap-add=SYS_PTRACE --rm -it -h jenkins --name jenkins -l role=jenkins \
  --add-host influx:10.0.0.168 --add-host gitserver:10.0.0.168 \
  -p 8080:8080 -p 9011:9011 -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  temp-jenkins-scalability-master:1.0
