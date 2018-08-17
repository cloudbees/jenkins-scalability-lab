#!/bin/bash
java -jar target/hydra-runner-1.0-SNAPSHOT-jar-with-dependencies.jar -t "basic-test,sample" -f basic-test.txt -w 30000 -i ${IP_UTIL_CONTAINERS}:8086 -j ${IP_JENKINS_MASTER}:8080