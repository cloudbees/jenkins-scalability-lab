#!/bin/bash
# Helper that runs Hydra with the standard set of exhaustive tests
# Note: customize the hosts for AWS based testing, using the public host IP
JENKINS_HOST="${IP_JENKINS_MASTER}:8080"
INFLUX_HOST="${IP_UTIL_CONTAINERS}:8086"
java -jar target/hydra-runner-1.0-SNAPSHOT-jar-with-dependencies.jar -t "legacy-pipeline,max-survivability" -f fulltest.txt -w 15000 -i $INFLUX_HOST -j $JENKINS_HOST